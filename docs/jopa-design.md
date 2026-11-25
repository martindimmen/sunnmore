# JOPA portal (first step towards the JOPA dimension)

Agreed with Martin on 2026-11-25; he said "start building" without a separate review of this doc. The code lives in `martidi.sunnmore.jopa`.

## Where to change things
- **Texts** (names, card messages, the bed message): `src/main/resources/assets/sunnmore/lang/en_us.json`, keys `*.sunnmore.jopa*`.
- **Frame rules** (smallest and largest size, corners optional): `JopaFrame`.
- **The lobby's shape and materials:** `JopaLobby` (layout, unit tested) and `JopaLobbyBuilder` (which block goes where).
- **Who may travel, where you arrive and where you return:** `JopaTravel`.
- **The dimension itself** (sky, beds, height): `data/sunnmore/dimension_type/jopa.json` and `data/sunnmore/dimension/jopa.json`.
- **Recipes:** `data/sunnmore/recipe/jopa_kort.json` and `jopa_ramme.json`.
- **Textures:** `assets/sunnmore/textures/block/jopa_ramme*.png`, `jopa_portal.png` (animated) and `textures/item/jopa_kort.png`.

## Intent
JOPA is the friend group's fictional company ("JOPA – alt innen alt", the best company in the world). It gets its own dimension. This first step is the way in: a portal that looks like a Nether portal, opened by swiping a JOPA access card, which leads to an empty JOPA lobby. The real content of the dimension comes later; see `docs/ideas.md`.

## Goals
- **JOPA-ramme (frame block):**
  - Build a rectangle with it, the same as a Nether portal: upright, inside at least 2 wide and 3 tall, at most 21 by 21. Corners are optional.
  - Unlit, it is a grey steel panel with a card reader.
  - Lit, it turns to the JOPA colours and gives off light.
- **JOPA-kort (access card):**
  - Swipe it on the frame (right-click) and you hear a card-reader beep. The frame lights up and the portal fills in.
  - A wrong frame gives a low beep and "Tilgang nekta." on screen.
  - The card is not used up.
- **The portal:**
  - It looks like a Nether portal in JOPA colours.
  - Stand in it and the screen wobbles; you travel after the same delay as a Nether portal (vanilla game rules).
  - Breaking any frame block closes it, and the frame goes back to grey.
- **The JOPA lobby:**
  - Every Overworld portal leads to the same lobby in the JOPA dimension: a closed white office room with windows, lights and a lit return portal.
  - The return portal takes you back to the portal you came from.
  - The lobby is built the first time someone arrives, and any missing parts are rebuilt on later arrivals.
- **The dimension:**
  - An empty void with the lobby in the middle. It has an Overworld-like sky and no weather.
  - Beds don't work there ("Ingen soving på jobb."), and they never explode.

## Non-goals (for now)
- No content in the dimension beyond the lobby. Peter and Jakob, the JOPA items and the headquarters come later.
- No portal in the Nether or the End.
- No mobs or items through the portal.

## Anti-goals (a failure even if it works)
- **Losing items in the void.** The lobby is closed, and in survival nobody can break or place blocks in it, so nobody falls out.
- **Getting stuck in JOPA.** The return portal can't be broken in survival, and it is rebuilt if missing.

## Constraints
- Minecraft 26.3 only, using vanilla's own portal hook (`Portal`, the same one the Nether portal uses). No portal library: Custom Portal API stopped at 1.21.8.
- Text is in Sunnmørsk; new words are listed as guesses.

## Approaches
- **A (chosen): our own blocks on vanilla's `Portal` hook.** Vanilla handles the waiting, the wobble and the travel; we write the frame check, the lighting, the lobby and the way back.
- **B: port Custom Portal API (MIT).** Rejected: 33 files written against old Minecraft, more code than we need.

## Decisions
**Martin's decisions:**
- **Portal:** a portal like the Nether's.
- **Card:** you light it by swiping a JOPA access card.
- **Frame:** our own frame block that changes colour when the card is swiped.
- **First destination:** an empty lobby to begin with.

**My calls:**
- **JOPA colours:** gold (`#F2B705`) and dark navy (`#1B2A4A`).
- **Names:** "JOPA-kort" (lore "Alt innen alt."), "JOPA-ramme" and "JOPA-portal".
- **Card messages:** "Velkommen til JOPA." and "Tilgang nekta." There is a separate "Tilgang nekta her." outside the Overworld.
- **Recipes:**
  - **JOPA-kort:** paper, gold nugget and redstone.
  - **JOPA-ramme:** smooth stone, iron ingots and redstone (`SIS / IRI / SIS`), which gives 4. A frame without corners needs 10.
- **Who may travel:** players only, not while riding. Mobs and items stay behind.
- **One shared lobby at JOPA (0, 64, 0).** The return trip uses the spot each player came from, saved on the player. If there is none, you go to the world spawn.
- **Lobby protection:** a check on vanilla's `mayInteract`, the same check spawn protection uses. It covers breaking, placing and buckets. Creative players can still build.
- **Light:** the lit frame and the portal give off light level 11.
- **Portal particles:** gold dust.

## Built (2026-11-25)
- **Code:** `JopaFrame` (frame check) and `JopaLobby` (layout) are pure and unit tested. `JopaPortals` lights a frame, `JopaPortalBlock` uses vanilla's `Portal` hook, `JopaTravel` decides where you go, and `JopaLobbyBuilder` builds the lobby. `mixin/ServerLevelMixin` protects the lobby.
- **Lobby on arrival:** missing parts are put back; air is left alone, so things built inside in creative stay.
- **Falling into the void:** anyone in JOPA who falls 24 blocks below the lobby floor is put back in the lobby. This also catches players who get out over the roof, for example with a Teleport-tyggis.
- **`/jopalobby` (op):** builds the lobby, and takes the player there with the way back set to where they stood. It is meant for testing, and works the same way as the friends' `/<friend>visit`.

## Left to the builder
- The exact lobby layout and materials (white office: smooth stone floor, white concrete, glass, sea lanterns).
- The texture details.
