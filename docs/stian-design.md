# Stian (fourth Sunnmøre friend)

Approved by Martin on 2026-11-25 ("try with ur simple one first": build the simple church first, keep a fancier church from the web for later). A builder check was done; its two gaps (materials you can't get, a plot that can't speak without Stian) are settled below as my calls. The code will live in `martidi.sunnmore.stian` (client: `martidi.sunnmore.client.stian`), next to the other friends. Built with the `new-friend` skill; Bård is the reference implementation.

## Intent
Stian is a friend of the group. He drinks all the time, and he is a happy drunk, never a sad one. In Minecraft he loves building churches. In the mod he is the builder of the group. You place a church plot, fill it with materials, and Stian builds a nice small church there, block by block, while you watch. Give him a Bjørneunge (a can of Isbjørn pils) and he builds twice as fast, but sloppier.

The joke is about the building hobby and the beer, not about faith.

## Goals
- **Look** (Martin: brown hair, wears brown, leather shoes, "almost like Indiana Jones without the hat"):
  - A player-sized humanoid model.
  - Brown hair, a brown leather jacket over a khaki shirt, brown trousers, leather boots.
  - The caricature (my call): an oversized Bjørneunge can always in his hand, and rosy cheeks.
- **Happy and tipsy all the time** (Martin chose A):
  - He walks a little crooked: a small random sideways drift while walking.
  - He hiccups about every 30 seconds, with a sound and a small puff.
  - Now and then (about every 2 to 4 minutes) he trips and sits on his backside for a second, then gets up laughing.
  - His lines are cheerful.
- **Smoking** (Martin, added while the design was being reviewed: "can we also make him smoke?"):
  - A cigarette in the corner of his mouth, as part of the model; his hand already holds the can.
  - A small puff of smoke every few seconds. Particles only: smoking never starts a fire.
  - **Røykepause** (my call): while building, every few minutes he stops for a few seconds, takes a drag, blows smoke, then carries on.
- **Building churches** (Martin chose B, "u can give him materials and he will build a nice one, but not too big"):
  - **Kyrkjetomt** (church plot), my name for it: a block you craft and place. It marks where the church goes and holds the materials in a chest-like inventory of 27 slots. Several players can chip in over time.
  - It is also his lure block: like the other lures, there is a 50% chance each morning that he comes to it.
  - **The plot speaks for itself** (my call, after review): Stian only visits now and then, so when you place or right-click the plot it shows two things in chat, in its own voice ("[Kyrkjetomt] ..."), not Stian's:
    - what is still missing
    - whether anything is in the way, and roughly where

    It also outlines the church's space with particles for a few seconds.
  - **Where the church goes:** it faces the player who placed the plot. The plot block sits just outside the front door, and the church extends behind it.
  - **The church** (our own design, my call): a small Norwegian wooden church, about 7 × 11 blocks on the ground and 15 high including the tower and spire, about 370 blocks (sketch 2026-11-25: 172 birch planks, 40 birch logs, 12 birch stairs, 1 birch door, 51 cobblestone, 68 cobblestone stairs, 1 cobblestone wall, 15 glass panes, 1 lantern, 4 torches). The tower stands over the front door.
    - Light walls of birch planks with birch log corners, a grey roof of cobblestone stairs and a cobblestone foundation.
    - A short tower with a lantern, glass-pane windows, a birch door, a birch floor, a few birch-stair pews and some torches.
  - **Simple materials** (Martin: "the materials dont need to be so hard to find, simple"): everything comes from birch trees, stone and sand, plus a lantern and torches.
    - Birch planks, logs, stairs and a door.
    - Cobblestone and cobblestone stairs.
    - Glass panes.
    - One lantern, a few torches.

    No bell (vanilla has no recipe for one) and no rare wood.
  - **The materials list** comes from the church drawing itself, counted per item. Right-clicking the plot opens its inventory. Stian says what is still missing ("Ej treng 40 plankar til").
  - **How he builds:**
    - He walks to a plot within 32 blocks that has materials and an unfinished church.
    - He places one block about every half second, bottom layer first, taking each block from the plot's inventory.
    - When the plot runs out of what the next block needs, he stops and says what is missing. He resumes when it is filled.
  - **Never wrecks anything:**
    - Before he starts, he checks every position of the church. He only replaces air and soft plants (grass, flowers, ferns, snow layers).
    - If anything else is in the way (stone, dirt, a player's build, a chest), he doesn't start and says roughly where it is.
    - He never breaks a block, never builds outside the church's footprint, and never takes items from anywhere but the plot.
  - **Afterwards** the church stays. It belongs to whoever owns the land; he never comes back to change it. Breaking the plot drops whatever materials are still in it, like a chest.
  - A visiting Stian doesn't leave while he is actively building (the plot has what the next block needs). If he is waiting for materials, he leaves at the normal time, so a missing item can never keep him forever.
- **The beer: Bjørneunge** (Martin: "Isbjørn pils, call it bjørneunge"):
  - A can of pils. Our own can texture with a polar bear on it, not the real logo.
  - Recipe (my call): an iron nugget (the can), wheat and sugar, shapeless. (Not a water bottle: a recipe can't tell a water bottle from any other potion.)
  - **In the plot** (Martin chose C): each Bjørneunge in the plot is used up for 50 blocks at double speed.
    - While he is fast, about 1 block in 20 goes slightly wrong. Mistakes are only wrong rotations: a stair facing the wrong way or upside down, or a door hinge on the wrong side. Then the materials always add up.
    - Mistakes are always inside his own church. Players can fix them by hand.
  - **Drunk by a player** (my call): a few seconds of wobbly vision (Nausea) and a little food.
- **Getting hit** (Martin chose A; "he is a happy drunk"):
  - He throws his can at the attacker. The can is a projectile with no damage, a "klonk" sound and a small knockback. He says a cheerful line.
  - At most one can every 3 seconds.
  - The can disappears on impact. It never drops as an item, so you can't farm beer by hitting him.
  - He doesn't stop building because of it.
- **Killing him** (my call, same as the other friends):
  - He drops nothing and gives no XP.
  - The killer gets Slowness I and Weakness I for 5 minutes.
  - Everyone sees "<killer> drepte Stian! Skam dej." in red.
  - 40 health.
- **Visits:** the shared visit system.
  - He shows up on his own in daytime, stays 2 days, and leaves with a line.
  - A name tag keeps him. Egg and `/summon` Stians stay for good.
  - `/stianvisit` forces a visit (op).
- **Chat:** `<Stian>`, 16 blocks (arrival 64), Sunnmørsk via the `sunnmorsk` skill, his own lines (none of the other friends' signature lines). Martin fixes the wording after the first play test.
- **Spawn egg** (my call): brown with khaki spots, in the Sunnmøre tab and the Spawn Eggs tab.

## Non-goals
- No other church designs or sizes, and no choosing the style. One church first.
- No shop or currency: his "payment" is the materials and the beer.
- No real Isbjørn logo.
- No advancements yet.

## Anti-goals (a failure even if it works)
- He replaces or breaks any block that isn't air or a soft plant, or builds outside the church's footprint.
- Materials disappear without being placed: every block he places came from the plot, and every item taken from the plot became a block.
- Hitting him gives free beer.
- His smoking starts a fire or changes a block.
- The joke turns into mocking faith, or into a sad or pitiful drunk.
- Glenn's, Jinith's or Bård's behaviour changes.

## Constraints
- Minecraft 26.3 and Fabric, as for the other friends (see `CLAUDE.md`).
- Multiplayer: the server decides everything. The tipsy pose (sitting after a trip) is a synced flag.
- The plot's inventory must survive saving, chunk unloading and server restarts.
- Text in Sunnmørsk, following the `sunnmorsk` skill.

## Approaches
- **A (my pick, Martin handed me the how): the church as a drawing in a data file.** A JSON file in the mod's resources with one text grid per layer, plus a legend that maps each symbol to a block state. The code reads it, builds bottom layer first and counts the materials from it.
  - I can design it without the game, and a friend can tweak it later.
  - The materials list and the build order can be unit tested.
- **B: build it in the game and save it with a structure block** (an `.nbt` file). Easier to see while building, but someone has to build it in game first, and Martin can't play right now.
- **C: reuse a vanilla village church.** Rejected by Martin.

## Decisions
- **Martin:**
  - Who he is: drinks all the time, a happy drunk, loves building churches.
  - Building: give him materials and he builds a nice church that isn't too big, where players choose (B).
  - Simple materials that are easy to find.
  - Tipsy look (A), and beer for speed with sloppiness (C).
  - The beer is Isbjørn pils, called Bjørneunge.
  - Look: brown hair, brown clothes, leather shoes, Indiana Jones without the hat.
  - Hit reaction: he throws his can, with no sulking.
  - He smokes too.
- **My calls:**
  - Kyrkjetomt as the plot name, and that it is also the lure.
  - The plot shows the missing materials and any obstacles itself, and outlines the church's space.
  - He doesn't stay past his visit while waiting for materials.
  - A 27-slot inventory.
  - The church design and size.
  - One block per half second, 32-block plot search.
  - Only air and soft plants may be replaced.
  - The Bjørneunge recipe (iron nugget + wheat + sugar) and what it does to a player.
  - 50 blocks per can, 1 in 20 mistakes, and mistakes are rotation only.
  - The oversized can and rosy cheeks.
  - The cigarette in his mouth, the smoke puffs and the røykepause while building.
  - The kill penalty and 40 health.
  - Egg colours.

## Left to the builder
- The church drawing: the exact layers, the build order within a layer, and making sure the door and the lantern place correctly (a two-block-tall door places both halves from one item; a hanging lantern needs a block above it).
- How the plot stores its items (a block entity with a container, saved with the block) and its screen (the vanilla chest screen is fine).
- How Stian moves around the site while building, and how far from a block he may place it.
- The can projectile: a thrown-item projectile that shows the Bjørneunge.
- The drift, hiccup and trip timers (pure, unit-tested values in `StianMath`).
- Whether a stopped build resumes after a server restart (it should: progress can be worked out from which church blocks are already in place).

## Where to change things
(Filled in when built: the church drawing, lines, timers, recipe and textures.)
