# Glennderman (first Sunnmøre feature)

First approved by Martin on 2026-11-23. Extended on 2026-11-24 with trading, personality, items and visits. The code lives in `martidi.sunnmore.glenn`.

## Intent
Glenn is a tall, ginger, social guy whose friends joke that he sells drugs. In the game he is a friendly Enderman. Stare at him and he comes over and asks "Yppa du eller?" (Sunnmørsk for "you want beef?"). He sells silly made-up items for sugar. Every item is fictional.

## Goals
- **Looks:**
  - Vanilla Enderman model with a ginger skin, a cake in his hands, and no purple eyes or particles.
  - A faint orange sparkle now and then.
  - A ginger spawn egg.
- **Friendly:**
  - He never attacks and doesn't get angry.
  - Golems leave him alone, and beds work next to him.
- **Greeting:**
  - Stare at him from within 16 blocks and he walks over and greets you by name. Then he stays with you for about 30 seconds so you can trade. He wanders slowly and seldom.
  - Close his shop without buying and he asks "Hekje du penga daaa?". After a purchase he says "Rike kar altso.". When he is sold out he says "Vent til i morga, då fe ej nytt shit."
  - He greets each player at most once every 60 seconds.
- **Trading:**
  - Right-click opens a trade screen.
  - Each in-game morning every Glenn picks 4 deals: 2 of his own items and 2 silly vanilla ones.
  - Each deal has limited uses until the next morning. Price is always sugar.
- **Stamkunde:**
  - Every trade counts. The count is stored per player and survives death.
  - Levels use the vanilla trade levels and the screen's progress bar, so the screen title reads Novice up to Master.
  - Each level takes 1 sugar off every deal.
  - Advancements: "Yppa du eller?" (first meeting) and "Stamkunde" (10 trades).
- **His own items**, which in survival come only from him. They have no recipes but are listed in the Sunnmøre creative tab:
  - **Teleport-tyggis:** a short random teleport.
  - **Glenns Mystery Brus:** one random effect, either speed, jump boost, levitation or nausea.
  - **Ginger-parykk:** a hat. Real Endermen don't get angry when you wear it.
  - **Premium Jord:** the scam deal. It is just dirt with a purple name.
- **Personality:**
  - He chats in Sunnmørsk when greeting, trading, getting hit, dodging, saying goodbye, seeing sugar, dancing, levelling you up, being sold out, arriving, leaving and dying.
  - He follows players holding sugar.
  - He dances to a jukebox that is playing within 8 blocks.
- **Getting hit:**
  - He hops 2 to 6 blocks away with an angry puff.
  - Projectiles never hurt him; he dodges them.
- **Killing him:**
  - He drops nothing and gives no XP.
  - The killer gets Slowness and Weakness for 5 minutes.
  - Everyone sees "<killer> drepte Glennderman! Skam dej." in red.
- **Visits:**
  - Like the wandering trader, he sometimes shows up near a random player, in daytime only.
  - He leaves after 2 in-game days unless he gets a name tag.
  - Egg or `/summon` Glenns stay for good.
  - `/glennvisit` (op) forces a visit.

## Non-goals
- No custom model and no voice clip yet.
- No real-world drug references in his trade items or text. The one exception is his lure block, **Glenns Plante**, which Martin chose on purpose (see `lures-design.md`).

## Approaches
- A (chosen): subclass vanilla `Enderman`, replace its AI goals, and implement `Merchant`.
- B: a brand-new entity (rejected: it needs a model and much more code before anything shows).
- C: a resource-pack reskin (rejected: it gives no behaviour).

## Decisions
**Martin's decisions:**
- **Payment:** sugar only.
- **Particles:** a situational mix.
- **Drops:** none.
- **Items:** in creative too.
- **Stock:** a daily lineup instead of a re-roll on every visit.
- **Visits:** daytime only.
- **Killer:** gets a debuff and a chat message.
- **Chat:** shows as `<Glennderman>`.
- **Text:** in Sunnmørsk: ej, e, igjen, morga, he.

**My calls:**
- **Who hears his chat:** players within 16 blocks. The arrival line reaches 64 blocks.
- **Trade-screen level names:** vanilla.
- **Prices:** tyggis ×2 costs 6 sugar, brus 8, wig 24, Premium Jord 12, cake 10, 6 cookies 4, 3 party rockets 6, an ender pearl 10.
- **Killer debuff:** Slowness I and Weakness I.
- **Visit timing:** the game checks once per in-game day. The chance starts at 25%, rises by 25% after each day without a visit (up to 75%), and then has an extra 1-in-3 roll. That comes to about one visit a week. The wandering trader's extra roll is 1-in-10. Change `EXTRA_ROLL` in `visits/VisitMath` to tune it. Visits now run in the shared `visits/FriendVisits`, and **Glenns Plante** near a player gives a 50% chance each morning that he comes to it (see `lures-design.md`).

## Wago (added 2026-11-25)
Glenn loves clicking around with a Wago, the clicky lever connector electricians use. Get it wrong and it pinches you, and that hurts a lot.

**Martin's decisions:**
- **Two items that work the same way:**
  - **Wago:** anyone can craft it from 1 iron ingot and 1 glass.
  - **Glenndermans Wago:** comes only from Glenn, as a rare deal. You pay with 2 glass and 2 iron ingots instead of sugar. It works like the plain one; you pay extra for his name, the same joke as Premium Jord.
- **Using it:** right-click and it clicks. Sometimes it pinches you.
- **The pinch can kill you** if you are low on health. This is a deliberate exception to the mod's "nothing kills you unexpectedly" rule.

**My calls (Martin approved the short design):**
- **Pinch:** 1 click in 10 pinches you for 2 hearts (4 damage).
  - Armor and Protection don't help, because it's your finger. The damage type is in the `bypasses_armor` and `bypasses_enchantments` tags.
  - It doesn't knock you back (tag `no_knockback`). Vanilla would push you in a random direction, off a cliff or into lava, and that would be an unexpected death.
  - It has its own death message.
- **Clicking:** the vanilla lever click sound and an arm swing.
  - There is no cooldown, so holding right-click clicks about 5 times a second.
- **Rare deal:**
  - Each morning there is a 1 in 5 chance that Glenn adds Glenndermans Wago as a fifth deal. He sells it once a day, on top of his normal four.
  - Stamkunde discounts only apply to sugar prices, so the Wago deal always costs 2 glass and 2 iron.
- **Glenn's reaction:** when you buy his Wago he says one of two Wago lines instead of his normal trade line.
- **Looks:**
  - Both are a small clear connector with orange levers. Glenn's has hoodie-green levers.
  - Glenndermans Wago has a yellow name (uncommon rarity) and its own lore line.
- **Left out for now:** Glenn reacting when someone near him gets pinched.

**Built** on 2026-11-25:
- **Code:** `WagoItem`, `WagoMath`, `WagoDamage` and the rare deal in `GlennTrades.rollLineup`.
- **Discount:** the Stamkunde discount checks the item you pay with (`GlennTrades.discountApplies`).

## Where to change things
- **Chat lines, item names and advancement texts:** `src/main/resources/assets/sunnmore/lang/en_us.json`. If you add a line to a pool, also raise that pool's count in `GlennLines.Pool`.
- **Deals and prices:** `GlennTrades`.
- **Wago pinch chance, damage and how rare Glenn's Wago deal is:** `WagoMath`. The recipe is `data/sunnmore/recipe/wago.json`.
- **Stamkunde numbers:** `StamkundeMath`.
- **Textures:** under `assets/sunnmore/textures`.
