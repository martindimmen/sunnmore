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
- **Wording to check.** These words are guesses; confirmed words go into `.claude/skills/sunnmorsk`.
  - Stian: "manglar" vs "mangle", "Fyll han" vs "ho", "1 blokker".
  - Earlier guesses: står, går, kven, nåken, ølen, kjem, sei, opphald, timar, minutt.
  - Wago: klypte, i hel, ein, mens, såg, betre, ekte, i vei, signert.

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
