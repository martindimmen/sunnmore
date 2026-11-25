---
name: sunnmorsk
description: Use when writing or editing any Norwegian text in the Sunnmøre mod - NPC chat lines (Glenn, Jinith, future friends), item names, lore, advancement titles - or when Martin asks for something "på sunnmørsk", "i dialekt" or corrects a word. Default Bokmål/Nynorsk (eg, er, har, ser, morgon, att, kva) is wrong here.
---

# Sunnmørsk for the Sunnmøre mod

## Overview
The mod is for Martin's friend group from Sunnmøre, so every player-facing line must sound like them. Your instinct produces textbook Nynorsk ("Eg ser du har…", "Kom att i morgon"), and they don't talk like that. The rule: **use Martin's forms from the table below. A word that is not in the table is a guess, so mark it as one.**

## Word table (verified by Martin)

| Don't write | Write | Example |
|---|---|---|
| eg | **ej** | "Ej e tilbake!" |
| er | **e** | "Den der e verdt kvar sukkerbit." |
| har | **he** | "Ej he nytt i dag." |
| har ikkje | **hekje** | "Hekje du penga daaa?" |
| får | **fe** | "Vent til i morga, då fe ej nytt shit." |
| se / ser | **sjå** | "Du sjå jo heilt ødelagt ut." |
| att, in both senses ("again" and "left over") | **igjen** | "Kom igjen…", "Ej he ingenting igjen i dag." |
| morgon | **morga** | "i morga" |
| altså | **altso** | "Rike kar altso." |
| stirte | **stirra** | "Faen du stirra på?" |
| deg | **dej** | "Skam dej.", "Kul dej ned, kompis!" |
| Ka glo du på? | **Faen du stirra på?** | |
| Ha det / Ses på fest (after a purchase) | **Rike kar altso.** | |
| Ha det (when closing a shop without buying) | **Hekje du penga daaa?** | |

The present tense often drops the -r ending, the same way "he" and "fe" do. "handlar" becomes "handle" and "gløymer" becomes "gløyme".

## Tone
- Short, blunt and cocky, like friends teasing each other. Casual swearing is normal: "faen", "shit".
- Stretch a vowel for attitude: "daaa", "altso".
- **"Yppa du eller?"** means "you want beef?" (picking a fight). It is Glenn's catchphrase, and it fits whenever someone stares at, hits or challenges a character.
- In-jokes are allowed: Jinith calls himself **BigChestPapi** ("BigChestPapi e faen på jobb!"). **JOPA** is the friends' fictional company, "JOPA – alt innen alt", and the joke is that it is the best company in the world.
- Chat shows as `<Glennderman> …` and `<Jinith> …`; the code adds the prefix.

## Not verified yet (used, not confirmed)
"ikkje", "ka" (hva), "kjem", "då", "no", "heilt", "kvar", "gonger", "Sjåast!" (see you), "såg", "nåken/nokon", "te" (til), "lege" (doctor; Jinith's lines use it).
When you use one of these, or any new word that is not in the table above, **list it under "Guesses to check" in your reply**. When Martin confirms or corrects a word, move it into the table.

## Writing lines for the mod (the steps)
1. Write the line in English first, then translate it with the table.
2. Check it again for eg, er, har, ser, se, morgon, att, kva, får and altså. Any of these means you missed a rule.
3. Put it in `src/main/resources/assets/sunnmore/lang/en_us.json` under `chat.sunnmore.<npc>.<pool>.<n>`. The file is en_us on purpose, because Minecraft falls back to it for every language.
4. If a pool gains or loses lines, update its count in the NPC's `Pool` enum (`GlennLines.Pool`, `JinithLines.Pool`), then check that every key exists.
5. In your reply, show Martin the new lines and the "Guesses to check".

## Common mistakes
- Polishing a line Martin wrote, for example turning "Hekje du penga daaa?" into "Har du ikkje pengar?". Keep his spelling exactly.
- Writing a long or polite line ("Kunne du tenke deg å…"). Make it shorter and ruder.
- Explaining the joke in the line. The friends already know it.
