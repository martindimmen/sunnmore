"""Smoke-tests the mod on a throwaway dedicated server, then cleans up after itself.

Usage (from the repo root):
    python3 -I .claude/skills/verify-mod/scripts/smoke_server.py "summon sunnmore:bard 0 100 0" "wait:2" "kill @e[type=sunnmore:bard]"

- Starts `./gradlew runServer` in run/ with a fresh world and RCON on a random port and password.
  The password is never printed.
- Force-loads chunk 0,0 in the overworld first, so things you summon at x=0 z=0 stay loaded.
- Runs each argument as a server command and prints the reply. "wait:N" waits N seconds.
- Stops the server and prints every ERROR/exception line from the log, plus warnings that
  mention sunnmore. Exit code 1 if there were any, or if the server didn't start.
- Deletes everything it added to run/ (world, server.properties, eula.txt, ops.json, ...) and
  restores a server.properties or eula.txt that was there before.
"""
import os
import re
import secrets
import shutil
import signal
import socket
import struct
import subprocess
import sys
import tempfile
import time

REPO = os.getcwd()
RUN = os.path.join(REPO, "run")
START_TIMEOUT = 300
STOP_TIMEOUT = 120


def free_port():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


class Rcon:
    def __init__(self, port, password):
        self.sock = socket.create_connection(("127.0.0.1", port), timeout=30)
        self.next_id = 1
        if self._request(3, password)[0] == -1:
            raise RuntimeError("RCON login failed")

    def _request(self, kind, body):
        req_id = self.next_id
        self.next_id += 1
        data = struct.pack("<ii", req_id, kind) + body.encode("utf-8") + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(data)) + data)
        raw = self._read(4)
        (length,) = struct.unpack("<i", raw)
        payload = self._read(length)
        got_id, _ = struct.unpack("<ii", payload[:8])
        return got_id, payload[8:-2].decode("utf-8", "replace")

    def _read(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise RuntimeError("RCON connection closed")
            buf += chunk
        return buf

    def command(self, cmd):
        return self._request(2, cmd)[1]


def main(commands):
    if not os.path.exists(os.path.join(REPO, "gradlew")):
        sys.exit("Run this from the repo root (where gradlew is).")
    os.makedirs(RUN, exist_ok=True)
    before = set(os.listdir(RUN))
    backup_dir = tempfile.mkdtemp(prefix="sunnmore-smoke-")
    for name in ("server.properties", "eula.txt"):
        if name in before:
            shutil.copy2(os.path.join(RUN, name), os.path.join(backup_dir, name))

    password = secrets.token_hex(16)
    rcon_port = free_port()
    with open(os.path.join(RUN, "server.properties"), "w") as f:
        f.write(
            "enable-rcon=true\n"
            f"rcon.port={rcon_port}\n"
            f"rcon.password={password}\n"
            "level-name=sunnmore_smoke_world\n"
            "online-mode=false\n"
            f"server-port={free_port()}\n"
            "spawn-protection=0\n"
        )
    with open(os.path.join(RUN, "eula.txt"), "w") as f:
        f.write("eula=true\n")

    log_path = os.path.join(backup_dir, "server.log")
    failed = False
    proc = None
    try:
        with open(log_path, "w") as log:
            proc = subprocess.Popen(["./gradlew", "runServer", "--args=nogui"], cwd=REPO, stdout=log,
                                    stderr=subprocess.STDOUT, start_new_session=True)
        print("Starting the server (this takes a minute) ...")
        deadline = time.time() + START_TIMEOUT
        while True:
            text = open(log_path, errors="replace").read()
            if "Done (" in text:
                break
            if proc.poll() is not None or time.time() > deadline:
                print("The server did not start. Last log lines:")
                print("\n".join(text.splitlines()[-40:]))
                failed = True
                return 1
            time.sleep(1)

        rcon = Rcon(rcon_port, password)
        for cmd in ["forceload add 0 0", "wait:1", *commands]:
            if cmd.startswith("wait:"):
                time.sleep(float(cmd[5:]))
                continue
            print(f"> {cmd}")
            print(f"  {rcon.command(cmd)}")
        rcon.command("forceload remove all")
        rcon.command("stop")
    finally:
        if proc is not None:
            try:
                proc.wait(timeout=STOP_TIMEOUT)
            except subprocess.TimeoutExpired:
                os.killpg(proc.pid, signal.SIGTERM)
                proc.wait(timeout=30)
        for name in set(os.listdir(RUN)) - before:
            path = os.path.join(RUN, name)
            shutil.rmtree(path) if os.path.isdir(path) else os.remove(path)
        for name in ("server.properties", "eula.txt"):
            saved = os.path.join(backup_dir, name)
            if os.path.exists(saved):
                shutil.copy2(saved, os.path.join(RUN, name))

    problems = [line for line in open(log_path, errors="replace").read().splitlines()
                if re.search(r"/ERROR\]|Exception|Caused by", line)
                or (re.search(r"/WARN\]", line) and "sunnmore" in line.lower())]
    shutil.rmtree(backup_dir, ignore_errors=True)
    if problems:
        print("\nProblems in the server log:")
        print("\n".join(problems[:40]))
        return 1
    print("\nServer log: no errors, no sunnmore warnings. run/ is cleaned up.")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
