# Sunnmøre

A Fabric mod for Minecraft 26.3, made for Martin's friend group from Sunnmøre. It is only for the friends: it is not published on Modrinth or CurseForge. The GitHub repo itself is public, though, so anything in it can be seen and downloaded.

## The idea
- **The friends are in the game.** Each friend is an NPC that caricatures them, built around the group's in-jokes:
  - **Glennderman** sells silly things for sugar, including the Wago.
  - **Jinith** (BigChestPapi) is a medicine student who heals people near him. Hit him and he beats you down to half a heart, then patches you up again.
  - **Bård** checks the weather report all the time and sells weather.
  - **Stian** is a happy drunk who builds churches.
  - Each friend has a lure block that makes them come by, and they all speak Sunnmørsk.
- **Vanilla+:** the rest should still feel like Minecraft, just with a bit more. It is not a big overhaul. See `docs/ideas.md`.
- **Our own area as the world (idea):** the Overworld generated from the real map, so people can live where they live in real life. See `docs/ideas.md`.
- **Friends can add things themselves** with Claude, on a branch with a pull request that Martin approves. See `CLAUDE.md`.

## Where things are
| What | Where |
|---|---|
| Rules for working in the repo, for people and Claude | `CLAUDE.md` |
| Each friend: what they do and why, and where to change it | `docs/<friend>-design.md`, `docs/lures-design.md` |
| Ideas not started yet | `docs/ideas.md` |
| What is waiting: play tests, wording checks, known small issues | `docs/open.md` |
| Build plans | `docs/superpowers/plans/` |

## Build and play
- `./gradlew build` compiles and runs all tests.
- `./gradlew runClient` starts the game with the mod.

## License
Our own code is CC0. Anything copied from other mods keeps its own license and credit; see `docs/ideas.md` for which mods allow that.
