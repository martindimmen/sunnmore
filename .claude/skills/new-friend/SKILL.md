---
name: new-friend
description: Use when adding a new friend NPC to the Sunnmøre mod (a real person from the friend group, as a caricature with their in-jokes), or a big new feature for an existing friend - for example "add my friend X", "make an NPC of Y", "lag ein kompis", "legg til ein ny venn".
---

# Add a friend to the Sunnmøre mod

## Overview
Every friend in the mod is a caricature of a real person, built around one or two in-jokes the group already makes about them. They are all built the same way, so they work together: they visit, they have a lure block, they speak Sunnmørsk in their own voice, and they never wreck anyone's stuff.

**Reference implementation: Bård** (`martidi.sunnmore.bard`, `docs/bard-design.md`, `docs/superpowers/plans/2026-11-25-bard.md`). He is the most complete, so copy his structure. Jinith (`jinith/`) shows a friend who fights; Glenn (`glenn/`) is built on a vanilla Enderman.

## Steps
1. **Ask before building**, one question at a time. Only the person knows their friend:
   - Who is it, and what is the joke the group makes about them?
   - One look detail to exaggerate (Jinith: muscles; Bård: butt).
   - What do they do in the game (sell, heal, fight, forecast)? What do they want as payment?
   - What happens when someone hits or kills them?

   Never make up traits about a real person. Exaggerate the joke, never ethnicity, religion or anything the person didn't bring up.
2. **Write `docs/<friend>-design.md`** with the same sections as `docs/bard-design.md`:
   - Intent, Goals, Non-goals, Anti-goals, Constraints, Approaches, Decisions (theirs vs. "my call"), Left to the builder
   - **Where to change things**

   Get the person's approval before writing any code.
3. **Plan, then build test-first.** Put pure logic in `<Friend>Math` with JUnit tests in `src/test/java/martidi/sunnmore/<friend>/`.
4. **Verify** with the `verify-mod` skill, then **open a pull request** as `CLAUDE.md` says.

## Build checklist
Copy each piece from Bård's version and change the names. "Shared" files only get additions, never changes to existing lines.

| Piece | Where |
|---|---|
| Entity type (`sunnmore:<friend>`, `.noLootTable()`), spawn egg, attributes, egg in `CreativeModeTabs.SPAWN_EGGS` and `GlennItems.SUNNMORE_TAB_KEY` | `<friend>/<Friend>Entities.java` |
| Entity: `xpReward = 0`, empty `dropCustomDeathLoot`, `removeWhenFarAway` false, `VisitingFriend` (despawnDelay saved as `DespawnDelay`, `maybeLeave`, `sayArrival` at 64 blocks), `Merchant` if they sell | `<friend>/<Friend>Entity.java` |
| Chat: a `Pool` enum with line counts, `<Name>` prefix, 16-block range, the cooldowns from `BardEntity.speak` | `<friend>/<Friend>Lines.java` |
| Lines, entity and egg names, item names | `assets/sunnmore/lang/en_us.json`, written with the `sunnmorsk` skill. `LangKeysTest` checks every pool line exists. |
| Model (`HumanoidModel` with vanilla part names, so walking animates), renderer, render state; register in `SunnmoreClient` | `src/client/java/martidi/sunnmore/client/<friend>/` |
| Textures: skin, egg, items | Paint with `scripts/texture_kit.py` from this skill (`faces()` gives the box-UV layout, `recolor_spawn_egg()` the egg). Keep your paint script out of the repo; commit only the PNGs. |
| Item and block JSON | `assets/sunnmore/items/`, `models/item/`, `blockstates/`, `models/block/` |
| Visits: `state("<friend>_visits")` and `FRIENDS.add(new Friend("<friend>", ...))`. This gives `/<friend>visit` for free. | `visits/FriendVisits.java` |
| Lure block: block, item, `PoiHelper.register(id("<friend>_lure"), ...)`, tab entry, recipe, loot table | `visits/LureBlocks.java`, `visits/<Name>Block.java`, `data/sunnmore/recipe/`, `data/sunnmore/loot_table/blocks/` |
| Registration: `<Friend>Entities.register()` after `BardEntities.register()` and before `LureBlocks.register()` | `Sunnmore.java` |
| Docs: the design doc with "Where to change things", plus a row in the `docs/lures-design.md` table | `docs/` |

## Rules
- **Their own voice.** Never reuse another friend's signature lines. They belong to them:
  - Glenn: "Yppa du eller?", "Faen du stirra på?", "Hekje du penga daaa?", "Rike kar altso.", "Vent til i morga, då fe ej nytt shit."
  - Jinith: "BigChestPapi e faen på jobb!", "Ej e jo lege."
  - Bård: the Yr and weather lines.

  Write new lines from the new friend's own joke.
- **Their own currency and their own hit reaction.** Taken so far:
  - currencies: sugar (Glenn), diamonds (Bård), building materials and Bjørneunge (Stian)
  - hit reactions: hop away (Glenn), beat you to half a heart (Jinith), lightning (Bård), throws his can (Stian)
- **Never wreck anyone's stuff.** Effects are visual-only or capped. Jinith skips armor and never kills; Bård's lightning never lands on copper or lightning rods. Check vanilla side effects (fire, block changes, redstone) in the 26.3 sources before using a vanilla effect.
- **Multiplayer:** the server decides everything. Anything the client must draw (poses, colours) is a synced `EntityDataAccessor` flag.
- **Leave the other friends alone:** no changes to their behaviour or lines unless the person who owns them (Martin) asks.

## Common mistakes
| Mistake | Instead |
|---|---|
| Reusing "Rike kar altso" or "Yppa du eller?" for the new friend | Write the new friend's own lines |
| Inventing what the real friend is like | Ask; use only what the person told you |
| Skipping "Where to change things" or the lure table row | Both are on the checklist |
| Guessing a 26.3 API name | Read the vanilla source (see `CLAUDE.md`) |
| Saying it works after `./gradlew build` only | Run the whole `verify-mod` ladder |
