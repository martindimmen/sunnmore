"""Starts the game client, waits until it has loaded all models and textures, then closes it and
reports any warning or error about the mod's assets (missing model, missing texture, bad JSON).

Usage (from the repo root):  python3 -I .claude/skills/verify-mod/scripts/client_check.py

A game window opens for about a minute; tell the person before you run it. It does not log in
to a world, so it checks that assets load, not how things look. Exit code 1 if there were problems.
"""
import os
import re
import signal
import subprocess
import sys
import tempfile
import time

REPO = os.getcwd()
LOAD_TIMEOUT = 300
SETTLE_SECONDS = 8
# Lines that mean the client finished loading resources.
LOADED = ("Sound engine started", "OpenAL initialized")


def main():
    if not os.path.exists(os.path.join(REPO, "gradlew")):
        sys.exit("Run this from the repo root (where gradlew is).")
    log_path = os.path.join(tempfile.mkdtemp(prefix="sunnmore-client-"), "client.log")
    with open(log_path, "w") as log:
        proc = subprocess.Popen(["./gradlew", "runClient"], cwd=REPO, stdout=log, stderr=subprocess.STDOUT,
                                start_new_session=True)
    print("Starting the client (a game window opens for about a minute) ...")
    deadline = time.time() + LOAD_TIMEOUT
    loaded = False
    try:
        while time.time() < deadline and proc.poll() is None:
            text = open(log_path, errors="replace").read()
            if "Created:" in text and any(marker in text for marker in LOADED):
                loaded = True
                time.sleep(SETTLE_SECONDS)
                break
            time.sleep(1)
    finally:
        if proc.poll() is None:
            os.killpg(proc.pid, signal.SIGTERM)
            try:
                proc.wait(timeout=30)
            except subprocess.TimeoutExpired:
                os.killpg(proc.pid, signal.SIGKILL)

    text = open(log_path, errors="replace").read()
    if not loaded:
        print("The client did not finish loading. Last log lines:")
        print("\n".join(text.splitlines()[-40:]))
        return 1
    problems = [line for line in text.splitlines()
                if re.search(r"/(WARN|ERROR)\]", line) and "sunnmore" in line.lower()]
    problems += [line for line in text.splitlines() if "Exception" in line and "sunnmore" in line.lower()]
    if problems:
        print("Problems with the mod's assets:")
        print("\n".join(problems[:40]))
        return 1
    print("Client loaded: no warnings or errors about sunnmore assets.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
