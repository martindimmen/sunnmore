# Sunnmøre mod

A Fabric mod for Minecraft 26.3, built around Martin's friends from Sunnmøre. Each friend is an NPC caricature with the group's in-jokes: Glenn (Glennderman), Jinith and Bård. Martin (`martindimmen`) owns the repo.

## How work gets in
- **Martin** may commit to `main` directly.
- **Everyone else** works on a branch (`<name>/<what>`, e.g. `ola/fisherman`) and opens a pull request into `main` with `gh pr create`. GitHub only lets it in when the `build` check (compile plus every test) is green and Martin has approved. Don't push to `main` and don't merge a pull request yourself; GitHub refuses both.
- Commit under the name and email of the person you work for (their `git config user.name` / `user.email`), never as Martin.
- Before opening the pull request, run the `verify-mod` skill and put its play-test list in the description.

## Skills: use them
| When | Skill |
|---|---|
| Adding a friend NPC, or a big feature for one | `new-friend` |
| Any Norwegian text players see: chat lines, item names, lore | `sunnmorsk`. Write Sunnmørsk, not Nynorsk or Bokmål. |
| Before saying anything works, or opening a pull request | `verify-mod` |

## Stack and commands
- Minecraft 26.3 (unobfuscated Mojang names), Fabric Loader 0.20.1, Loom 1.19, Fabric API 0.167.0+26.3, Java 25, JUnit 5.
- `./gradlew build` compiles and runs all tests. `./gradlew runClient` starts the game; `runServer` starts a server (both use `run/`; for servers use the `verify-mod` scripts).
- **Vanilla source:** `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-common-*/26.3/*-sources.jar` (client classes are in the `clientOnly` jar next to it). Read a class with `unzip -p <jar> net/minecraft/...java`. Check the real 26.3 API there before you guess a name.

## 26.3 traps we already hit
- Entity type constants live in `EntityTypes` (`EntityTypes.LIGHTNING_BOLT`), not `EntityType`.
- Ids are `Identifier`; use `Sunnmore.id("name")`.
- Loot tables use a singular `"condition"` and `"modifier"` in pools. Recipes use `"result": {"id": "..."}`.
- `PushReaction.POPPED` (there is no `DESTROY`). Use `Vec3.atCenterOf(pos)` (there is no `BlockPos.getCenter`).
- The in-game day number is `GlennTrades.currentDay(level)`. The weather lives on the server: `MinecraftServer.getWeatherData()`.
- `TemptGoal` crashes without `Attributes.TEMPT_RANGE`.
- Never use `Long.MIN_VALUE` as a "never happened" time: `now - last` overflows. Use `-cooldown`.
- A block's render layer comes from its texture's transparency.

## Where things are
- One package per friend: `martidi.sunnmore.<friend>` on the server side, `martidi.sunnmore.client.<friend>` under `src/client/java` for the model and renderer. Pure logic goes in `<Friend>Math` with tests in `src/test`.
- Shared code: `visits/` (visits, lure blocks), the Sunnmøre creative tab (`GlennItems.SUNNMORE_TAB_KEY`), `Sunnmore.java` (registration order).
- All text: `src/main/resources/assets/sunnmore/lang/en_us.json` (en_us on purpose; every language falls back to it).
- Design docs: `docs/<friend>-design.md` (each has "Where to change things"), `docs/lures-design.md`. Build plans: `docs/superpowers/plans/`. Ideas not started yet: `docs/ideas.md`.

## Don't
- Change another friend's behaviour or lines without Martin's say-so.
- Make anything that destroys players' blocks, items or armour, or kills them unexpectedly. Effects are visual-only or capped (see the friends' anti-goals). The one exception is the Wago pinch, which can kill a player on low health. That is Martin's call (see `docs/glenn-design.md`), so don't "fix" it.
- Print secrets (RCON passwords, tokens).
