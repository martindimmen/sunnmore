# Open threads

What is built but waiting on Martin, as of 2026-11-25. Remove a line once it is done.

## Waiting on Martin
- **GitHub branch protection on `main`:** friends must use pull requests, with the `build` check green and Martin's approval; Martin can bypass.
  - This needs `gh` logged in as `martindimmen`, because the account it uses now can only read: `gh auth login --hostname github.com --git-protocol ssh --web`.
- **Play tests:**
  - **Bård:**
    - forecast, weather shop (Sol, Regn, Torden), bread
    - lightning when hit
    - one weather change per day
  - **Stian:**
    - give him materials in a Kyrkjetomt, with and without Bjørneunge
    - hitting him (can throw)
    - smoking break, tipsy stumbles
  - **Stian, never tested in game at all:**
    - giving up when he can't walk to the plot
    - not leaving while building
    - the "something in the doorway" line
    - plots facing east, south and west
  - **Wago:** the play-test list at the end of `docs/superpowers/plans/2026-11-25-glennderman-wago.md`.
  - **JOPA portal:**
    1. Craft a JOPA-kort and 12 JOPA-ramme.
    2. Build an upright frame, at least 4 wide and 5 tall (corners optional), and swipe the card on it: a high beep, "Velkommen til JOPA.", and the frame goes gold and navy with a portal inside.
    3. Swipe a broken or blocked frame: a low beep and "Tilgang nekta."
    4. Stand in the portal: the screen wobbles, and after about 4 seconds you are in the JOPA lobby, facing the return portal.
    5. In the lobby in survival, try to break a wall or place a block: it is refused. A bed doesn't work there: "Ingen soving på jobb."
    6. Take the return portal: you land back at the portal you came from.
    7. Break one frame block: the portal closes and the frame goes grey.
    8. Mobs and dropped items don't go through.
    9. Swipe the card on a frame in the Nether: "Tilgang nekta her."
- **Wording to check.** These words are guesses; confirmed words go into `.claude/skills/sunnmorsk`.
  - Stian: "manglar" vs "mangle", "Fyll han" vs "ho", "1 blokker".
  - Earlier guesses: står, går, kven, nåken, ølen, kjem, sei, opphald, timar, minutt.
  - Wago: klypte, i hel, ein, mens, såg, betre, ekte, i vei, signert.
  - JOPA: nekta, soving, Velkommen.

## Known small issues (Martin decides whether to fix)
- **Bård:**
  - Minutes in the forecast are rounded up.
  - His lightning makes a sculk vibration.
  - The sold-out line can be skipped by the chat cooldown.
  - Mobs hitting him don't stop a trade.
  - He can say a panic line right after you buy Torden.
  - "om 1 timar" should be singular.
  - The Værstasjon recipe uses one lightning rod item, not the `lightning_rods` tag.
- **Stian:**
  - The Bjørneunge speed boost is lost on a server restart.
  - Item frames, paintings and minecarts don't count as "in the way".
  - A church near the build height limit isn't handled.
  - Looking for plots can load chunks.
  - The "blocked" line doesn't say where.
  - Blocks he gives back lose any custom name.
- **Wago:**
  - Glenn's Wago line can be skipped if he said something in the last 3 seconds, for example when you buy cookies and then the Wago.
  - The test only checks that the death messages exist, not that they have the right placeholders.
