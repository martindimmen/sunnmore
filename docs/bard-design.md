# Bård (third Sunnmøre friend)

Approved by Martin on 2026-11-25 ("yes have this design"). The code will live in `martidi.sunnmore.bard` (client: `martidi.sunnmore.client.bard`), next to `glenn` and `jinith`. In code and ids he is `bard` (no å); everything players see says **Bård**.

## Intent
Bård is a friend of the group: a blonde guy of average height with a big butt. The joke is that he always has to check the weather report. In the game he checks his phone all the time and tells everyone what the weather will do. And he is right, because Minecraft decides the weather in advance. For diamonds he can change the weather, once per in-game day for the whole world. Hit him and lightning strikes next to you.

## Goals
- **Look:**
  - His own model: player height, blonde hair, a plain Minecraft-style face, and a properly oversized butt. The butt is the caricature, the way the muscles are Jinith's.
  - My call on clothes: a navy rain jacket and jeans, so the butt reads clearly.
  - When he checks the forecast he holds a phone up in front of his face.
- **The forecast** (Martin):
  - Every few minutes, if a player is within 16 blocks, he stops, holds up the phone for about 3 seconds, then says the forecast to players within 16 blocks. The interval is my call: 2 to 4 minutes at random.
  - He also says the forecast when you open his shop, so you know what you are buying against (my call).
  - **The forecast is real.** It is read from the game's own weather countdowns, so "Regn om 4 minutt" means rain really starts in about 4 minutes. Times are in real minutes and hours, rounded. Under a minute he says it is coming right now.
  - **What the game knows:** it decides only when the *current* weather ends. How long the *next* spell lasts is rolled when that spell starts. So he forecasts the next change ("Regn om 4 minutt", "Opphald om 2 minutt", "Torden om 6 minutt"), never how long it will last. He forecasts a thunderstorm only when the rain and thunder countdowns are known to overlap.
  - **Stress** (my call): when rain is less than a minute away he gets stressed and says so, once per rain spell. When a thunderstorm starts he panics in chat.
  - If the `doWeatherCycle` game rule is off, he says the weather is standing still.
  - Things he can't see coming: players sleeping through the night (that clears rain in vanilla), `/weather`, or another mod. Those make the forecast out of date, and that's fine.
- **Changing the weather** (Martin):
  - Right-click opens a trade screen titled "Bård" with three deals:
    - **Sol** (clear): 1 diamond
    - **Regn** (rain): 1 diamond
    - **Torden** (thunderstorm): 5 diamonds
  - Buying one changes the weather right away. It lasts a normal vanilla weather spell: clear for 10 minutes to 2.5 hours, rain for 10 to 20 minutes, a thunderstorm for 3 to 13 minutes (vanilla's ranges).
  - **One change per in-game day for the whole world** (Martin). After anyone has bought a change, all three deals show as sold out until the next morning, at every Bård (spawn eggs can make several). The day is counted the same way as Glenn's daily lineup, so both reset at the same moment. Sleeping through the night starts a new day, as for Glenn.
  - Opening his shop when the day's change is used gives a sold-out line.
  - Buying the weather you already have is allowed (my call). It never makes that weather shorter: it lasts the longer of the time it had left and a fresh spell. So Sol on a sunny day buys a guaranteed dry spell.
  - **Weather frozen** (`doWeatherCycle` off, my call): all three deals are closed, and he says the weather is standing still. Otherwise one purchase would be permanent and would override an admin who froze the weather on purpose.
  - **Outside the Overworld** (my call): no signal, the same as the Værmelding. He gives no forecast, the deals are closed, and he says "Ingen dekning her".
  - On a Torden purchase, a harmless lightning bolt strikes near him, for show (my call).
  - No Stamkunde levels and no progress bar (my call).
- **Værmelding** (the receipt, my call):
  - Each deal gives a **Værmelding** note, named for what you bought ("Værmelding: Sol" and so on).
  - Right-click it anywhere in the Overworld and it shows the current forecast in your own chat, only to you, in Bård's words.
  - Outside the Overworld it says there is no signal ("Ingen dekning her").
  - In survival it comes only from Bård. It is also in the Sunnmøre creative tab.
- **Getting hit** (Martin chose lightning):
  - When a player hits him (melee, or a projectile they shot), a lightning bolt strikes right next to that player, 1 to 2 blocks away, not on them. It has the flash and the thunder sound.
  - The bolt is visual only: no damage, no fire, no charged creepers, no pigs turning into piglins, no villagers turning into witches.
  - He says a line, stops trading, and steps away a few blocks.
  - At most one bolt every 3 seconds per Bård, so spam-clicking doesn't become a strobe light (my call).
  - Damage from anything that isn't a player (a zombie, a fall) gives no bolt.
- **Killing him:** same as Glenn (my call).
  - He drops nothing and gives no XP.
  - The killer gets Slowness I and Weakness I for 5 minutes.
  - Everyone sees "<killer> drepte Bård! Skam dej." in red.
  - Health: 40 (20 hearts), like Glenn, so a few hits don't kill him by accident.
- **Visits:** the shared visit system, same rules as Glenn and Jinith.
  - He shows up on his own in daytime and stays 2 in-game days.
  - A name tag keeps him; spawn-egg and `/summon` Bårds stay for good.
  - Op-only `/bardvisit` forces a visit.
- **Lure block: Værstasjon** (my call):
  - A small weather station: a thin pole with three cups on top (an anemometer). The cups are static.
  - Recipe: lightning rod + compass, shapeless.
  - It behaves exactly like Glenns Plante and the Treningsbenk: a 50% chance each morning that he comes to it.
- **Chat:**
  - It shows as `<Bård>`, reaches players within 16 blocks, and the arrival line reaches 64 blocks, as for Glenn.
  - All lines are in Sunnmørsk using the `sunnmorsk` skill, in `en_us.json` under `chat.sunnmore.bard.<pool>.<n>`.
  - Martin fixes the wording after the first play test.
- **Spawning:** a spawn egg in the Sunnmøre tab and the Spawn Eggs tab. My call on colours: blonde with navy spots.

## Non-goals
- No advancements and no loyalty levels for now.
- No voice clips.
- Spinning anemometer cups.
- Weather in the Nether or the End. Those dimensions have none.

## Anti-goals (a failure even if it works)
- His lightning hurts anyone, starts a fire, charges a creeper or converts a mob.
- The weather changes more than once in one in-game day through Bård, for example with two Bårds or two players buying at the same moment.
- Diamonds are taken but the weather doesn't change.
- The forecast is wrong when nothing outside it (sleeping, `/weather`, a mod) changed the weather.
- The butt goes from caricature to crude. It is a silly big shape, like Jinith's muscles, and nothing more.
- Glenn's or Jinith's behaviour changes.

## Constraints
- Minecraft 26.3 with Fabric (Loom 1.19, Fabric API 0.167.0+26.3, Java 25), the same as the other friends.
- Multiplayer: the server decides everything. The weather and its countdowns are global: in 26.3 they live on the server (`MinecraftServer.getWeatherData()`, `setWeatherParameters`). The phone pose must be synced so every client sees it.
- Text in Sunnmørsk, following the `sunnmorsk` skill.

## Approaches
- **A (my pick): his own model, built like Jinith, with Glenn's kind of shop.** A new mob with a player-sized humanoid model plus a butt part attached to the body, so walking animates the way vanilla does. A trade screen as Glenn has, and the shared visit and lure system. It is the most work, but the only way to get the caricature and the weather shop.
- **B: a reskinned wandering trader.** You get the trade screen and the walking for free. Rejected: it brings villager baggage (zombies convert him, restocking, the villager model with no butt), and the weather deals still need custom code.
- **C: a stretched player model.** Rejected: the butt would only be painted on.

## Decisions
- **Martin's decisions:**
  - Blonde, average height, big butt.
  - He always checks the weather (forecast), and can change it (C).
  - Changing costs diamonds: Sol 1, Regn 1, Torden 5.
  - Only one weather change per day, not several.
  - Lightning when hit.
  - "yes do this" to the rest of the list on 2026-11-25.
- **My calls (Martin can change them):**
  - the rain jacket and jeans
  - forecast every 2 to 4 minutes, plus when the shop opens
  - the stress lines
  - buying the weather you already have is allowed, and never makes it shorter
  - deals closed when the weather is frozen or he is outside the Overworld
  - a show bolt on Torden
  - no Stamkunde
  - the Værmelding receipt and its no-signal line
  - one bolt per 3 seconds
  - the same kill penalty as Glenn, and 40 health
  - the Værstasjon look and recipe
  - the egg colours

## Left to the builder
- The exact model geometry, the UV layout and the phone. Generate the textures (skin, spawn egg, Værmelding, Værstasjon) with a Pillow script in the scratchpad, as before.
- **Sol needs care.** Don't use vanilla's forced-clear timer (what `/weather clear` sets). Reading `ServerLevel.advanceWeatherCycle`, when that timer runs out it flips both rain and thunder on, so a storm would follow. Verified by the design reviewer. There is a second trap: `MinecraftServer.setWeatherParameters` (used by `/weather`) gives thunder the same countdown as rain, so a clear spell started through it also ends in a storm. Set the weather data's countdowns directly, so each one is natural.
- The forecast as a plain, unit-tested function of the countdowns (clear time, rain time, thunder time, raining, thundering). Test cases: clear with rain coming, rain ending, a storm overlap, a forced-clear timer, and countdowns at 0, where the next spell is not decided yet.
- How to keep the daily limit safe when two players or two Bårds buy at once. The world-wide "day of the last change" must be checked and set at the moment of the trade, and open shop screens must be refreshed.
- Whether to share code with Glenn (the shop boilerplate, chat, the kill penalty), as long as Glenn's and Jinith's behaviour stays the same.
- How the Sunnmøre creative tab gets the new items, as long as the existing items stay listed.
