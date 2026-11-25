# Glenndermans Wago Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the Wago, a lever connector that clicks on right-click and sometimes pinches you. Anyone can craft the plain Wago. Glenn sometimes sells Glenndermans Wago for glass and iron.

**Architecture:**
- One `WagoItem` class serves both items, which are registered in `GlennItems`.
- The pinch is a data-driven damage type, `sunnmore:wago_pinch`.
- Glenn's rare deal is an extra offer added in `GlennTrades.rollLineup`.
- Pure numbers go in `WagoMath`, which is unit tested.

**Tech Stack:** Minecraft 26.3 (Mojang names), Fabric API 0.167.0+26.3, Java 25, JUnit 5.

**Spec:** `docs/glenn-design.md`, section "Wago (added 2026-11-25)".

## Global Constraints
- Item ids are `sunnmore:wago` and `sunnmore:glennderman_wago`. The names are "Wago" and "Glenndermans Wago".
- The recipe is shapeless: 1 `minecraft:iron_ingot` + 1 `minecraft:glass` gives 1 `sunnmore:wago`.
- **Pinch:**
  - 1 click in 10 (`PINCH_ONE_IN = 10`) does 4.0 damage (2 hearts).
  - It ignores armor and Protection.
  - It can kill. This is deliberate and Martin's decision.
- **Rare deal:**
  - Each morning there is a 1 in 5 chance (`RARE_DEAL_ONE_IN = 5`) of a fifth deal: 1 Glenndermans Wago for 2 `minecraft:glass` plus 2 `minecraft:iron_ingot`.
  - It sells once a day and gives the normal trade XP.
  - It never gets the Stamkunde discount.
- **Sound:** `SoundEvents.LEVER_CLICK`.
- **No cooldown:** holding right-click repeats.
- **Text:** all player-facing text is Sunnmørsk (the `sunnmorsk` skill). New words that are not in its table are listed as guesses.
- **Commits:** on branch `feature/wago`, as Martin (`git config user.name`).

## Review Focus
1. **Dying from a pinch after someone else hit you.** If a mob or player hurt the victim recently, vanilla looks up the `death.attack.sunnmore.wago.player` key. It must exist, or players see a raw key. `WagoDataTest` covers it.
2. **A Stamkunde level 5 player opening Glenn's screen.** The Wago deal must still cost 2 glass and 2 iron. Today the discount is applied to every offer. `GlennTradesTest.discountOnlyOnSugar` covers it.
3. **A creative-mode player clicking.** They hear the click and take no damage. Vanilla handles this, because creative players ignore damage that isn't tagged `bypasses_invulnerability`. Don't add the damage type to that tag. This goes on the play-test list.
4. **Glenns saved before this change.** Their offers load as they were, and the Wago can only appear from the next morning's roll. No migration is needed. This goes on the play-test list.
5. **A Wago in the offhand.** It clicks and pinches the same as in the main hand: `use` gets the hand, and the item does not care which one. This goes on the play-test list.

---

### Task 1: The Wago item (math, data, item, looks)

**Files:**
- Create:
  - `src/main/java/martidi/sunnmore/glenn/WagoMath.java`
  - `src/main/java/martidi/sunnmore/glenn/WagoItem.java`
  - `src/main/java/martidi/sunnmore/glenn/WagoDamage.java`
- Create data:
  - `src/main/resources/data/sunnmore/damage_type/wago_pinch.json`
  - `src/main/resources/data/sunnmore/recipe/wago.json`
- Create assets:
  - `src/main/resources/assets/sunnmore/items/wago.json` and `glennderman_wago.json`
  - `src/main/resources/assets/sunnmore/models/item/wago.json` and `glennderman_wago.json`
  - `src/main/resources/assets/sunnmore/textures/item/wago.png` and `glennderman_wago.png`
- Modify:
  - `src/main/resources/data/minecraft/tags/damage_type/bypasses_armor.json` and `bypasses_enchantments.json` (add `"sunnmore:wago_pinch"`)
  - `src/main/java/martidi/sunnmore/glenn/GlennItems.java` (two items and the tab entries; fix the class comment that says "There are no recipes")
  - `src/main/resources/assets/sunnmore/lang/en_us.json`
- Test:
  - `src/test/java/martidi/sunnmore/glenn/WagoMathTest.java`
  - `src/test/java/martidi/sunnmore/glenn/WagoDataTest.java`

**Interfaces:**
- Produces `WagoMath`:
  - `public static final int PINCH_ONE_IN = 10;`
  - `public static final float PINCH_DAMAGE = 4.0F;`
  - `public static final int RARE_DEAL_ONE_IN = 5;`
  - `static boolean isPinch(int roll)`: true only for `roll == 0`.
  - `static boolean rareDealToday(int roll)`: true only for `roll == 0`.
- Produces `WagoDamage.PINCH`: `ResourceKey<DamageType>` for `sunnmore:wago_pinch`, built the same way as `JinithDamage.PUNCH`.
- Produces `GlennItems.WAGO` and `GlennItems.GLENNDERMAN_WAGO`. Both are `WagoItem`, `stacksTo(16)`, with lore via `withLore(...)`. Glenn's has `Rarity.UNCOMMON`.

- [ ] **Step 1: Write the failing tests.**
  - `WagoMathTest`, in the same style as `StianMathTest`:
    - `pinchOneIn10`: `isPinch(0)` is true, and `isPinch(1..9)` is false.
    - `rareDealOneIn5`: `rareDealToday(0)` is true, and `rareDealToday(1..4)` is false.
    - `pinchIsTwoHearts`: `assertEquals(4.0F, WagoMath.PINCH_DAMAGE)`.
  - `WagoDataTest` reads files from the test classpath with Gson, the same way `LangKeysTest` loads its resources:
    - `damageTypeMatchesDeathMessages`:
      - `data/sunnmore/damage_type/wago_pinch.json` has `message_id` `"sunnmore.wago"`.
      - `en_us.json` has `death.attack.sunnmore.wago` and `death.attack.sunnmore.wago.player`.
    - `pinchIgnoresArmor`: both `data/minecraft/tags/damage_type/bypasses_armor.json` and `bypasses_enchantments.json` list `sunnmore:wago_pinch`.
    - `recipeIsIronAndGlass`:
      - `data/sunnmore/recipe/wago.json` has type `minecraft:crafting_shapeless`.
      - Its ingredients are exactly `["minecraft:iron_ingot", "minecraft:glass"]`, in either order.
      - Its result `id` is `sunnmore:wago`.
    - `itemNamesExist`: `item.sunnmore.wago`, `item.sunnmore.wago.lore`, `item.sunnmore.glennderman_wago` and `item.sunnmore.glennderman_wago.lore` are in `en_us.json`.

- [ ] **Step 2: Run the tests and see them fail.**
  - Run: `./gradlew test --tests 'martidi.sunnmore.glenn.Wago*' > .superpowers/wago-t1.log 2>&1; tail -30 .superpowers/wago-t1.log`
  - Expected: a compile failure, because `WagoMath` doesn't exist. After the stub, `WagoDataTest` fails on the missing files.

- [ ] **Step 3: Implement `WagoMath`, then add the data files and lang lines.**
  - `wago_pinch.json`: `{"message_id": "sunnmore.wago", "scaling": "never", "exhaustion": 0.1}`.
  - Lang text (Sunnmørsk; the guesses are listed in the final message):
    - `item.sunnmore.wago`: "Wago"
    - `item.sunnmore.wago.lore`: "Klikk, klikk... AU!"
    - `item.sunnmore.glennderman_wago`: "Glenndermans Wago"
    - `item.sunnmore.glennderman_wago.lore`: "Signert Glennderman. Klikke betre."
    - `death.attack.sunnmore.wago`: "%1$s klypte seg i hel på ein Wago"
    - `death.attack.sunnmore.wago.player`: "%1$s klypte seg i hel på ein Wago mens %2$s såg på"

- [ ] **Step 4: Run the tests and see them pass.**
  - Run: the same command as step 2.
  - Expected: `BUILD SUCCESSFUL`, with every `Wago*` test passing.

- [ ] **Step 5: Implement `WagoItem.use(Level, Player, InteractionHand)` and register both items.**
  - **On a `ServerLevel`:**
    1. Play `SoundEvents.LEVER_CLICK` at the player for everyone (`playSound(null, ...)`, `SoundSource.PLAYERS`, volume 0.6). Pick the pitch at random between 0.6 and 0.5, the lever's on and off pitches.
    2. If `WagoMath.isPinch(player.getRandom().nextInt(WagoMath.PINCH_ONE_IN))`, call `player.hurtServer(level, level.damageSources().source(WagoDamage.PINCH), WagoMath.PINCH_DAMAGE)`.
  - **On both sides:** return `InteractionResult.SUCCESS`, so the arm swings.
  - **Registration:**
    - Register `wago` and `glennderman_wago` in `GlennItems`.
    - Add both to the Sunnmøre tab after `PREMIUM_JORD`.
    - Add the item and model JSONs, in the same form as `premium_jord`.

- [ ] **Step 6: Paint the two 16x16 textures with `.claude/skills/new-friend/scripts/texture_kit.py`.**
  - Keep the paint script in the scratchpad. Use opaque pixels only.
  - Draw a WAGO 221-style connector, seen from the front:
    - a light grey-blue body (`#C9D6DE`) with a darker outline (`#6E7F8A`)
    - three lever tabs on top, orange (`#F28C28`) on `wago.png` and hoodie green (`#387A54`) on `glennderman_wago.png`
    - three small dark wire holes (`#2B2B2B`) on the front face
  - View both PNGs with the Read tool before committing.

- [ ] **Step 7: Run the build.**
  - Run: `./gradlew build > .superpowers/wago-build.log 2>&1; tail -20 .superpowers/wago-build.log`
  - Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit.**
  - Run: `git add src docs && git commit -m "Glenn: Wago item, recipe and pinch damage"`

### Task 2: Glenn's rare Wago deal

**Files:**
- Modify:
  - `src/main/java/martidi/sunnmore/glenn/GlennTrades.java` (the rare offer in `rollLineup`, plus `static boolean discountApplies(MerchantOffer)`)
  - `src/main/java/martidi/sunnmore/glenn/GlenndermanEntity.java` (`applyStamkundePrices` skips offers where `discountApplies` is false; `notifyTrade` says the `WAGO` line for `GLENNDERMAN_WAGO`)
  - `src/main/java/martidi/sunnmore/glenn/GlennLines.java` (`WAGO(2)` in `Pool`)
  - `src/main/resources/assets/sunnmore/lang/en_us.json`
- Test: `src/test/java/martidi/sunnmore/glenn/GlennTradesTest.java`

**Interfaces:**
- Consumes: `WagoMath.rareDealToday`, `WagoMath.RARE_DEAL_ONE_IN`, `GlennItems.GLENNDERMAN_WAGO` (Task 1).
- Produces:
  - `GlennTrades.discountApplies(MerchantOffer offer)`: true when the base cost A is sugar.
  - `GlennLines.Pool.WAGO`.

- [ ] **Step 1: Write the failing test `GlennTradesTest.discountOnlyOnSugar`.**
  - Bootstrap vanilla in `@BeforeAll` the same way as `ChurchSiteBlocksTest`: `SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();`.
  - A `new MerchantOffer(new ItemCost(Items.SUGAR, 6), new ItemStack(Items.CAKE), 2, 5, 0.0F)` gives `discountApplies` true.
  - A `new MerchantOffer(new ItemCost(Items.GLASS, 2), Optional.of(new ItemCost(Items.IRON_INGOT, 2)), new ItemStack(Items.CAKE), 1, 5, 0.0F)` gives false.
  - Use only vanilla items, because mod items can't register into the frozen registries in a unit test.

- [ ] **Step 2: Run it and see it fail.**
  - Run: `./gradlew test --tests 'martidi.sunnmore.glenn.GlennTradesTest' > .superpowers/wago-t2.log 2>&1; tail -30 .superpowers/wago-t2.log`
  - Expected: a compile failure, because `discountApplies` doesn't exist.

- [ ] **Step 3: Implement.**
  - `discountApplies`: `offer.getBaseCostA().is(Items.SUGAR)`.
  - In `rollLineup`, after the two loops:
    - if `WagoMath.rareDealToday(random.nextInt(WagoMath.RARE_DEAL_ONE_IN))`
    - add `new MerchantOffer(new ItemCost(Items.GLASS, 2), Optional.of(new ItemCost(Items.IRON_INGOT, 2)), new ItemStack(GlennItems.GLENNDERMAN_WAGO), 1, XP_PER_TRADE, 0.0F)`.
  - Update the class comment to mention the rare fifth deal.
  - Entity:
    - In `applyStamkundePrices`, skip offers where `discountApplies` is false.
    - In `notifyTrade`, add a `GLENNDERMAN_WAGO` branch next to the Premium Jord one that says `Pool.WAGO`.
  - Lang:
    - `chat.sunnmore.glenn.wago.0`: "Ein ekte Glennderman-Wago. Klikk i vei, %s!"
    - `chat.sunnmore.glenn.wago.1`: "Den der klikke betre enn alt du he."
    - Check the `%s` against how `sayLine` fills the player name. The Premium Jord line `scam.1` uses `%s`, so use the same form.

- [ ] **Step 4: Run all tests and see them pass.**
  - Run: `./gradlew build > .superpowers/wago-build.log 2>&1; tail -20 .superpowers/wago-build.log`
  - Expected:
    - `BUILD SUCCESSFUL`
    - `LangKeysTest` passes, so both WAGO lines exist.
    - The test count is 87 plus the new tests.

- [ ] **Step 5: Commit.**
  - Run: `git add src && git commit -m "Glenn: rare Wago deal for glass and iron"`

### Task 3: Verify and document

**Files:**
- Modify:
  - `CLAUDE.md`: in the "Don't" rule about killing, add "(exception: the Wago pinch can kill, Martin's call; see `docs/glenn-design.md`)".
  - `docs/glenn-design.md`: add the built status under the Wago section if anything changed while building.

- [ ] **Step 1: Run the smoke server.**
  - Run:
    ```
    python3 -I .claude/skills/verify-mod/scripts/smoke_server.py \
      'summon item 0 100 0 {Item:{id:"sunnmore:wago",count:1}}' \
      'summon item 0 100 1 {Item:{id:"sunnmore:glennderman_wago",count:1}}' 'wait:1' \
      'execute as @e[type=item] run data get entity @s Item.id' \
      'summon villager 0 100 2 {CustomName:"Wagotest",NoAI:1b}' 'wait:1' \
      'damage @e[type=villager,name=Wagotest,limit=1] 100 sunnmore:wago_pinch' \
      'recipe give @a sunnmore:wago'
    ```
  - Expected:
    - exit code 0
    - both item ids are printed
    - `damage` replies "Applied 100.0 damage", not "Unknown damage type"
    - no ERROR lines from `sunnmore` in the log
    - `recipe give` with no players may reply "No player was found". That is fine; a broken recipe shows as a log ERROR.

- [ ] **Step 2: Run the client check.**
  - Tell Martin first that a game window opens for about a minute.
  - Run: `python3 -I .claude/skills/verify-mod/scripts/client_check.py`
  - Expected: exit code 0, with no missing model or texture warnings for `wago` or `glennderman_wago`.

- [ ] **Step 3: Commit the docs.**
  - Run: `git add CLAUDE.md docs && git commit -m "Wago: docs"`

- [ ] **Step 4: Report.**
  - Give the test count and the smoke and client results.
  - List the Sunnmørsk guesses: klypte, i hel, ein, mens, såg, betre, ekte, i vei, signert.
  - Give this play-test list:
    1. Craft a Wago from 1 iron + 1 glass.
    2. Right-click it: lever click and an arm swing. Hold right-click and it repeats.
    3. Keep clicking in survival: about 1 click in 10 takes 2 hearts, even in diamond armor.
    4. Die from it at low health and read the death message.
    5. Click in creative: no damage.
    6. Click from the offhand.
    7. `/glennvisit`, then `/time add 24000` a few times: about 1 morning in 5 he has a fifth deal, Glenndermans Wago for 2 glass and 2 iron. Buy it and hear the Wago line.
    8. As a Stamkunde of level 2 or higher, the Wago deal still costs 2 glass and 2 iron.
