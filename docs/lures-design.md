# Lure blocks: Glenns Plante and Treningsbenk

Approved by Martin on 2026-11-24 ("do this on both, very good"). The code is shared between the friends; the blocks live next to the friend they lure.

## Intent
Martin wants the friends to show up more often, and in a place the players choose. You craft a lure block for a friend and place it at your base, and that friend starts visiting there. Each block fits the in-joke about its friend.

## The blocks
| Block | Friend | Look | Recipe (my call) | Tooltip |
|---|---|---|---|---|
| **Glenns Plante** | Glenn | A flower pot with a bushy green plant with jagged leaves (the classic weed-leaf shape, Minecraft-style) | flower pot + fern + bone meal (shapeless) | "Berre krydder, lova." |
| **Treningsbenk** | Jinith | A low weight bench with a barbell resting on top | 3 iron ingots on the top row, 3 planks (any wood) on the middle row | (none) |

Both are small, decorative blocks (not full cubes), mined by hand, and they drop themselves.

Martin chose the plant over a sugar bowl ("take something closer to weed"). That removes the old non-goal from `glenn-design.md` about no real-world drug references. The plant is the one exception; the items stay fictional.

## Behaviour
- **Morning roll:** in the first visit check of each in-game day, in daytime, each friend gets one roll:
  - If one of his lures is loaded (a player within 64 blocks of it) and that friend isn't already visiting, there is a **50% chance** he arrives **at the lure**: on a free standing spot within 4 blocks of it, with his arrival line.
  - Extra lures don't stack. With several, one of them is picked.
- **Without a lure:** the natural visits continue as before.
  - Glenn: about every 5 in-game days.
  - **Jinith now visits naturally too**, with the same base rate and rules as Glenn: daytime only, near a random player.
- **Visits:**
  - A visit lasts 2 in-game days, then he leaves with a leave line and a teleport puff.
  - A name tag keeps him for good.
  - Breaking the lure does not end a visit.
  - Friends from a spawn egg or `/summon` stay for good, as before.
- **Test commands:** op-only `/glennvisit` (as before) and a new `/jinithvisit`.

## After review (2026-11-24)
- **No second copy:** a friend who is already near his lure (a name-tagged resident, a spawn egg, or a forced visitor) stops another copy from arriving there.
- **Spot search:** searches at most 1 block above the lure, and only spots visible from it, so he never lands on a roof or behind a wall.
- **Headroom:** Glenn is 2.9 blocks tall and needs 3 free blocks above a spot. If there is no room at the plant, he turns up near the player closest to it instead, and `/glennvisit` tells you there was no room.
- **No leave-and-return:** while he is visiting, and on the day he leaves, the day's lure roll counts as used, so he can't leave and come straight back.
- **Loaded chunks only:** only lures in chunks that are actually running count.
- **Forced visitors:** they are remembered like natural ones.
- **Bench outline:** covers the barbell whichever way the bench faces.

## Constraints
- Glenn's visits must behave as before when there is no plant. The code underneath is shared now.
- New text is written in Sunnmørsk, using the `sunnmorsk` skill.

## Approaches
- **Points of interest (chosen):** each lure is registered as a point-of-interest type, the same mechanism vanilla uses to find bells for the wandering trader. The game indexes them, so finding a loaded lure near a player is cheap.
- **A block entity that ticks** (rejected): it costs something every tick for every lure, and it would duplicate the visit logic.
- **Scanning blocks around players** (rejected): slow, and it doesn't scale.
