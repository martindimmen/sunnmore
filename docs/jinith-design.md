# Jinith (second Sunnmøre friend)

Approved by Martin on 2026-11-24 and built the same day. The code lives in `martidi.sunnmore.jinith` (client: `martidi.sunnmore.client.jinith`), next to `glenn`. The build plan is in `docs/superpowers/plans/2026-11-24-jinith.md`.

## Where to change things
- **Chat lines and the death message:** `src/main/resources/assets/sunnmore/lang/en_us.json`, keys `chat.sunnmore.jinith.*`. If you add a line to a pool, raise that pool's count in `JinithLines.Pool`.
- **Fight rules:** half a heart, give-up distance and time, the safety window and when Regeneration is renewed are in `JinithMath`.
- **Stats:** health, damage, knockback and speed are in `JinithEntity.createAttributes()`. Heal radius and durations are the constants at the top of `JinithEntity`.
- **Body and textures:** the shape is in `JinithModel`. The skins are `textures/entity/jinith/jinith.png` and `jinith_angry.png` (128×64, using the UV layout described in `JinithModel`).

## Intent
Jinith is a friend of the group. He is a small, buff Sri Lankan guy who studies medicine. In the game he is a muscle caricature: huge arms, a wide chest, a short body. He is friendly and heals people near him. Punch him and he turns red, chases you, and beats you easily. He always stops at half a heart, and then the medicine student patches you up again. The joke is "he beats you up, then he's your doctor."

## Goals
- **Look:**
  - His own model: a small head, huge arms, a broad chest, short stubby legs, about 1.6 blocks tall.
  - The caricature is in the muscles and the build.
  - There is no reference photo. Martin said a typical look is good enough: medium-brown skin, short black hair, light black stubble, and a plain Minecraft-style face (two eyes, eyebrows, a mouth). Only the muscles are exaggerated.
  - My call on clothes: medical scrubs that are far too tight, with a stethoscope around his neck.
- **Friendly mode:**
  - He wanders around calmly and looks at players.
  - He heals any player who stands within 6 blocks and is hurt, unless that player is his current target.
  - The heal is Regeneration I for 5 seconds, renewed while you stay close and are still hurt, with heart particles.
  - He says a doctor line now and then, at most once a minute per player.
  - He doesn't heal anyone while he is angry.
- **Getting punched:**
  - A player who damages him (melee, or a projectile they shot) makes him angry at that player only.
  - He turns red: his whole skin switches to a red-tinted version. He chases the player fast and punches hard, about 4 hearts a hit, with a big arm swing.
  - Everyone else around him stays safe.
- **His punches:**
  - They use their own damage type, which is tagged so that it skips armor, shields and protection enchantments. Armor, shields and their durability are never touched.
  - Damage is capped so it never takes a player below 1 health (half a heart). He cannot kill anyone.
  - **Strong knockback** (Martin): each punch clearly throws the player several blocks back and a little upward, much more than a normal mob hit. After the second play test Martin asked for maximum knockback. The punch now sets the player's speed directly (4 blocks per tick sideways, 0.6 up; see `JinithMath.LAUNCH_SPEED`), which is far past the vanilla knockback cap. That is about 40 blocks of flight, and the landing never does fall damage (the wind-charge rule). Because of the long flight, he now gives up at 64 blocks instead of 32 and can find his way that far.
- **Sounds** (Martin asked for them; I chose vanilla ones):
  - a deep piglin-brute grunt when he turns red
  - an iron-golem thud plus the knockback whoosh on every punch
  - an amethyst chime when his healing starts on you
  - the zombie-villager "cured" sound when he patches you up
  - Real voice clips can replace these later.
  - **Safety window** (Martin): for 5 seconds after each of his punches (3 originally, extended to cover the long flight), and after the half-heart patch-up, nothing can take that player below half a heart. That includes fall damage, lava, fire, cactus and other mobs. The one exception is falling into the void, which cannot be stopped.
- **Fast and relentless** (Martin, after the third play test):
  - When angry he runs at twice his normal speed and steps up full blocks.
  - He punches every half second, the fastest a player can take damage again.
  - He only gives up after 2 minutes without landing a hit, or at 96 blocks away.
- **Scare phase** (Martin): when the target reaches half a heart, he keeps chasing and punching for 5 more seconds. Those punches still launch you and show the hurt flash, but deal no damage. Then come the flex and the patch-up. The target is protected from the first half-heart moment until 5 seconds after the patch-up. The flex and patch-up lines reach the target even if he launched them out of normal hearing range.
- **Flex first** (added after the first play test): when his punch leaves the player at half a heart, he stops, stays red and flexes both arms up for 2.5 seconds, saying "BigChestPapi e faen på jobb!" (Martin's line). The player is protected the whole time. If they hit him during the flex, the fight is back on. After the flex comes the joke below.
- **The half-heart joke:** after the flex he calms down.
  1. His normal colour comes back.
  2. He gives that player Regeneration II for 10 seconds, with heart particles.
  3. He says a line like "Ka du tenkte på? Ej e jo lege."
- **Calming down otherwise:** he also calms down quietly when the target dies some other way, logs out, changes dimension, gets more than 32 blocks away, or he hasn't landed a hit for 60 seconds.
  - If another player punches him while he is angry, he switches to the newest attacker.
- **Tough:** 100 health (50 hearts) and strong resistance to knockback. He can be killed, but it takes work. He drops nothing and gives no XP.
- **Chat:**
  - He never repeats the same kind of line to the same player within 5 seconds, even in group fights.
  - He speaks Sunnmørsk, shown as `<Jinith>`, with lines in `en_us.json` (keys `chat.sunnmore.jinith.<pool>.<n>`).
  - The dialect rules are the same as Glenn's: ej, e, igjen, morga, he.
  - Martin fixes the wording after the first play test.
  - Starting pools: heal, angry (when punched), hit (while chasing), patched_up (the half-heart joke), calm (calmed down without the joke).
- **Spawning:**
  - A spawn egg in the Sunnmøre creative tab and the Spawn Eggs tab: scrub-blue with skin-brown spots.
  - `/summon sunnmore:jinith` also works.
  - Spawn-egg and `/summon` Jiniths never despawn.
  - Since the lures (`lures-design.md`), he also visits by himself like Glenn, and the **Treningsbenk** lures him. A visitor leaves after 2 days (never mid-fight) unless he gets a name tag. `/jinithvisit` forces a visit.

- **Pets:** wolves don't retaliate against his punch (the `minecraft:no_wolf_retaliation` tag). Owned pets still attacking him stop when he calms down.

## Non-goals
- No trading, no items, no advancements. Those can come later, as they did for Glenn.
- No voice clips.

## Anti-goals (a failure even if it works)
- He kills a player, either directly or through his knockback within the 3-second safety window (the void excepted). Or he damages armor or shield durability.
- He hurts anyone other than the player who punched him.
- The caricature mocks his ethnicity rather than exaggerating his muscles.
- Glenn's behaviour changes.

## Constraints
- Minecraft 26.3 with Fabric (Loom 1.19, Fabric API 0.167.0+26.3, Java 25), the same as Glenn.
- He must work in multiplayer: the server decides everything, and the "angry" state is synced so every client sees him red.
- Vanilla rules to respect:
  - Mobs can't attack players in Peaceful or in Creative. In Peaceful he only turns red and gives up. Test the fight in Survival.

## Approaches
- **A (chosen by Martin): his own muscle model.** He gets his own geometry, but with the same part names as the vanilla humanoid model (head, body, arms, legs), so walking and the attack swing animate the way vanilla does. A red skin variant comes from a texture swap. It is the most work, but the only option that gives the caricature.
- **B: a shrunk Iron Golem with a reskin.** Rejected: it reads as a golem with a face (long arms, long head), not a buff guy.
- **C: a stretched player model.** Rejected: it looks like a fat Steve, and the muscles would only be painted on.

## Decisions
- **Martin:** a muscle caricature with a wide body, healing when you are close (he studies medicine), red when angry, stops at half a heart, ignores armor so it doesn't get ruined, the half-heart patch-up joke, approach A, a typical look with no reference needed, strong knockback, and a 3-second safety window after his punches.
- **My calls (Martin can change them):**
  - the scrubs and stethoscope
  - the numbers: 6-block heal radius, Regeneration I for 5 s, 100 health, about 4 hearts per punch, a 32-block / 60-second give-up, Regeneration II for 10 s after the joke
  - shields and protection enchantments are skipped as well as armor
  - he switches to the newest attacker
  - no drops or XP

## Left to the builder
- The exact model geometry and the UV layout. Generate the textures (normal, red and spawn egg) with a Pillow script, as for Glenn.
- Movement speed and chase speed. He must be able to catch a sprinting player in the open.
- The sounds: vanilla ones that fit a punch and a hurt grunt.
- How the creative tab gets his egg. The Sunnmøre tab is currently built in `GlennItems`. A shared place or an output event are both fine, as long as Glenn's items stay listed.
- Whether to share a small chat helper with Glenn (`GlennLines`), as long as Glenn's lines and behaviour stay the same.
- Unit tests for the plain logic: the damage cap, and when anger ends.
