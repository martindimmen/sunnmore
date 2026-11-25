---
name: verify-mod
description: Use before saying a change to the Sunnmøre mod works, is done, or is ready for a pull request - after adding or changing an NPC, item, block, recipe, loot table, chat line, model or texture - and when asked to "test it", "check it works", "smoke test" or "sjekk at det funkar".
---

# Verify a change to the Sunnmøre mod

## Overview
Nothing is "done" until the mod has been built, started on a server and loaded in a client, and every output has been read. The tools are in this skill's `scripts/` folder, and they clean up after themselves. What only a player can check goes on a play-test list for the person; never claim those parts work.

## The ladder: run every step, in order, from the repo root

| # | Command | Passes when |
|---|---|---|
| 1 | `./gradlew build` | `BUILD SUCCESSFUL`. This compiles and runs every JUnit test, including `LangKeysTest`, which fails if a chat line is missing from `en_us.json`. Say the test count. |
| 2 | `python3 -I .claude/skills/verify-mod/scripts/smoke_server.py "<cmd>" ...` | Exit code 0 and every command replies as expected. |
| 3 | `python3 -I .claude/skills/verify-mod/scripts/client_check.py` | Exit code 0. **Tell the person first:** a game window opens for about a minute. |
| 4 | A play-test list in your final message | Written as numbered steps the person can follow in game. |

## Step 2: commands that touch what you changed
The script force-loads chunk 0,0, so summon things at x=0, z=0. Summon at y 100 or lower: a mob that falls from higher can die from the fall.

| You changed | Commands |
|---|---|
| a mob | `summon sunnmore:<id> 0 100 0`, `wait:2`, `execute as @e[type=sunnmore:<id>] run data get entity @s Health`, `kill @e[type=sunnmore:<id>]` |
| an item | `summon item 0 100 0 {Item:{id:"sunnmore:<id>",count:1}}`, `wait:1`, `execute as @e[type=item] run data get entity @s Item.id` |
| a block or lure | `setblock 0 100 0 sunnmore:<id>`, `locate poi sunnmore:<id>_lure` (for a lure), `setblock 0 100 0 air` |
| a command | `help <command>`. A registered op command replies with an empty line; an unknown one says "Unknown command". |
| a recipe or loot table | Nothing extra: a broken data file shows up as an ERROR in the log, which the script prints. |

## Step 4: the play-test list
Anything that needs a player: trading, hitting, chat lines, looks, sounds, visits, lure arrivals. Give exact steps (`/<friend>visit`, `/time add 24000`, "hit him next to a lightning rod"). In your report, say plainly that these are untested until the person has played them.

## Reporting
Give each command you ran and its result, the test count, and the play-test list. If a step failed, show the output; don't round it up to "mostly works".

## Rules
- Use the scripts. Don't start `runServer` with a server.properties you wrote yourself: the script picks a random RCON password and port, never prints them, and restores `run/` afterwards.
- Never print an RCON password or any other secret.
- A red build is never "done", even if "only a test" fails.
