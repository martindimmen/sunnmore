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

## Getting started
### 1. Install what you need
- **Java 25 or newer, the JDK** (not only the JRE). Get Temurin 25 from https://adoptium.net. Check it with `java -version`.
- **Git**, and the GitHub CLI **`gh`** if you want to send in changes (https://cli.github.com).
- **Claude Code** (optional) if you want Claude to help you add things. It reads `CLAUDE.md` and the skills in `.claude/skills` by itself.

### 2. Get the code
```
git clone https://github.com/martindimmen/sunnmore.git
cd sunnmore
gh auth login
```
`gh auth login` lets you push your branch and open pull requests (choose HTTPS when it asks). How changes get into `main` is in `CLAUDE.md`.

### 3. Build and start the game
| Command | What it does |
|---|---|
| `./gradlew build` | Compiles the mod and runs every test. The first time it downloads Minecraft, which takes a few minutes. |
| `./gradlew runClient` | Starts Minecraft with the mod. It uses a test account, so you can't join normal online servers with it. Worlds are saved in `run/saves`. |
| `./gradlew runServer` | Starts a server with the mod, also in `run/`. |

On Windows, use `gradlew.bat` instead of `./gradlew`.

### 4. Try things in game
Make a world with cheats on. These commands are for testing:
- `/glennvisit`, `/jinithvisit`, `/bardvisit`, `/stianvisit`: the friend comes to you.
- `/jopalobby`: takes you to the JOPA lobby.

All the items are in the **Sunnmøre** creative tab. What still needs a play test is in `docs/open.md`.

## Playing in your normal Minecraft
1. **Get the mod file**, one of these:
   - Build it: `./gradlew build` makes `build/libs/sunnmore-1.0.0.jar`. Use that one, not the `-sources` jar.
   - Download it: on GitHub, open **Actions**, then the latest green **build** run on `main`, and download **Artifacts** (you must be logged in). It is a zip with the jar in it.
2. **Install Fabric Loader** 0.20.1 or newer for Minecraft 26.3 with the installer from https://fabricmc.net/use/installer. It adds a Fabric profile to the Minecraft launcher.
3. **Download Fabric API** 0.167.0+26.3, or a newer one for 26.3, from https://modrinth.com/mod/fabric-api.
4. **Put both jars in the `mods` folder.** Make the folder if it isn't there.
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Windows: `%APPDATA%\.minecraft\mods`
5. Start the launcher, pick the Fabric profile and play.

## License
Our own code is CC0. Anything copied from other mods keeps its own license and credit; see `docs/ideas.md` for which mods allow that.
