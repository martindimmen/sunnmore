# Ideas not started yet

Check with Martin before building any of these.

## JOPA items, sold by Peter and Jakob
The way into the JOPA dimension is built: see `docs/jopa-design.md`. It is a portal opened with the JOPA-kort, leading to an empty lobby. The content of the dimension is still open.

JOPA is the friend group's fictional company. Its slogan is "JOPA – alt innen alt", and the running joke is that it is the best company in the world. Martin wants a JOPA line of items that are actually useful, not joke items.

Decided so far (Martin, 2026-11-24):
- **JOPA Alt-i-ett:** one tool that works as pickaxe, axe, shovel and hoe. Martin liked this one.
- **Sellers:** two future friend NPCs, **Peter** and **Jakob**, sell the JOPA items. Martin said "don't worry about that yet", so they are not designed.

Still open: which other items, and whether they are fair or overpowered.
- Other ideas floated (not decided): Proteinshake, Førstehjelp, Hjelm (night vision + water breathing), Magnet, Ryggsekk.

## A fancier church for Stian
Martin's idea (2026-11-25): once the simple church works, let Stian build the church part of **Hilltop Abbey** by an author on Planet Minecraft: https://www.planetminecraft.com/project/hilltop-abbey/ ("pretty advanced").
- When we searched, the page said the author couldn't share the schematic yet, so it may not be downloadable.
- The repo is public, so we need the author's permission to include it, and we credit them.
- Stian's church is a drawing in a data file (see `docs/stian-design.md`), so a second church means a second drawing, converted from the schematic and switched to simpler materials.

## More vanilla+ (2026-11-25)
Martin wants more "vanilla+" in the game: things that still feel like Minecraft. Nothing is decided yet.

**What vanilla+ modpacks contain.** We looked at 12 popular vanilla+ packs on Modrinth, 5 of them on 26.3.
- Most of a pack is small conveniences:
  - right-click to harvest crops (RightClickHarvest)
  - XP orbs that clump together (Clumps)
  - faster leaf decay
  - crops that grow faster in rain
  - fairer anvils
  - picking up chests and animals (Carry On)
  - food values on screen (AppleSkin)
  - maps
- Real new content is rare. When it is there, it is usually Farmer's Delight (cooking), structures and new biomes.

**What we may copy.** The repo is public, so anything copied into it is published.
- **Allowed, keeping the original copyright notice:** MIT and public-domain mods.
  - MIT: Farmer's Delight, including its Fabric port "Farmer's Delight Refabricated", which has a 26.3 branch. Also RightClickHarvest, Crops Love Rain, Clumps and Snow! Real Magic.
  - Public domain: AppleSkin.
- **Not allowed:**
  - Supplementaries: its license bans copies, even AI-made ones.
  - Friends & Foes: no modified copies.
  - Quark: its license would force ours.
  - GPL mods: they would force our whole mod onto GPL.
  - All-rights-reserved mods: Waystones, Croptopia, Small Ships, Xaero's maps.
- **LGPL mods** (Comforts, Universal Graves, Carry On): fine as separate mods next to ours, but their code can't be copied into our CC0 code.

**Suggested, not decided:**
- Make a friends' modpack: our mod plus a handful of existing mods installed side by side. Nothing gets copied, so licenses are no problem.
- Our mod sticks to what nobody else has: the friends and Sunnmøre things.
- Sunnmøre food (svele, brunost, rømmegraut, klippfisk) could be an add-on for Farmer's Delight, cooked in its cooking pot.

## Our own area as the Overworld
Martin (2026-11-25): he wants "a mod that gets our area and generates it, so people can live where they live in real life". It is parked until the Wago is done.
- **Tellus** (https://modrinth.com/mod/tellus, LGPL, has a Fabric build for 26.3):
  - What it does: it generates the real world while you play, from real elevation data, OpenStreetMap roads and buildings, and climate-based biomes. You choose the scale, and there is an in-game map to teleport to a place.
  - **Internet:** the server needs internet all the time.
  - **Updates:** the author warns that updates may break survival worlds. Pin the version for the friends' world.
  - **Servers:** there is no proper server support yet. Create the world in singleplayer, then move it to the server.
  - **Real weather and time must stay off,** or Bård's weather shop stops working.
  - **Height:** at true scale, Sunnmøre's mountains of about 1,500 m are far taller than a normal world. Use its taller-world setting, or scale down.
- **Arnis** (https://github.com/louis-e/arnis, Apache-2.0): a separate program that builds a box you draw on the map once, as a finished world.
  - It works offline and is stable.
  - It only covers the box.
  - It is made for 1.21.4+. Opening that world in 26.3 is untested.
- **Suggested next step:** try Tellus in the test client and fly over the area before deciding.
