# Visionary Pathway Abilities

## Spirituality

Spirituality regenerates at **0.06% of max per tick** (1.2% per second) passively.

| Sequence | Max Spirituality | Regen/sec |
|----------|-----------------|-----------|
| 9        | 180             | 2.2/s     |
| 8        | 200             | 2.4/s     |
| 7        | 780             | 9.4/s     |
| 6        | 1,200           | 14.4/s    |
| 5        | 1,900           | 22.8/s    |
| 4        | 3,900           | 46.8/s    |
| 3        | 5,000           | 60.0/s    |
| 2        | 10,000          | 120.0/s   |
| 1        | 20,000          | 240.0/s   |
| 0        | 60,000          | 720.0/s   |

---

## Active Abilities

---

### Envisioning Position
**Sequence Requirement:** 0
**Spirituality Cost:** 1000
**Cooldown:** 0.5 seconds
*(Cannot be copied, replicated, used by NPCs, or used in artifacts)*

Two selectable modes. Teleport range scales with sequence: `2^(9−seq)` blocks (e.g. Seq 9 = 1 block, Seq 0 = 512 blocks).

**Mode 0 — On Sight**
- Teleports the caster to the targeted block within range (line-of-sight targeting).

**Mode 1 — Coordinates**
- Opens a coordinate entry UI; teleports the caster to the specified location.

---

### Mind World Authority
**Sequence Requirement:** 0
**Spirituality Cost:** 1000
**Cooldown:** 1 second
*(Cannot be copied, replicated, stolen, used by NPCs, or used in artifacts)*

Two selectable modes.

**Mode 0 — Envisioning**
- Toggles the **Mind World Authority Envisioning** state (10 spirituality per tick while active).

**Mode 1 — Seal Mind World**
- *(Not yet fully implemented)*

---

### Story Writing
**Sequence Requirement:** 1
**Spirituality Cost:** 200
*(Cannot be used by NPCs)*

Five selectable modes. Requires an active story book target to use most modes.

**Mode 0 — Write Player**
- Opens a screen to select a player as the story target.
- Creates a **Story Book** item for that player (3 uses).

**Mode 1 — Write Target**
- Targets the nearest entity within **20 blocks**.
- Creates a **Story Book** item for that entity (3 uses).

**Mode 2 — Disaster**
- Triggers the currently selected **Disaster Fantasia** mode at the story target's position.
- Consumes 1 book use.

**Mode 3 — Assault**
- Triggers **Group Incite** (from Manipulation) centered on the story target.
- Consumes 1 book use.

**Mode 4 — Guidance**
- Sends the story target a compulsion to move to a chosen location (**85 block range**).
- Sanity drains every second after 30s, ramping ×1.5 every 30s — ends after the 4th ramp or on arrival.
- Consumes 1 book use.

---

### Disaster Fantasia
**Sequence Requirement:** 1
**Spirituality Cost:** 2500
**Cooldown:** 11 seconds

Two selectable modes. **Target range:** 150 blocks.

**Mode 0 — Earthquake**
- Spawns an earthquake calamity at the target location.

**Mode 1 — Meteor Shower**
- Spawns **7 meteors** scattered within a **50 block radius** of the target location.

---

### Discernment
**Sequence Requirement:** 2
**Spirituality Cost:** 200
**Cooldown:** 3 seconds
*(Cannot be copied, replicated, stolen, used by NPCs, or used in artifacts)*

Three selectable modes.

**Mode 0 — Believe Self**
- Randomly applies one of four powerful self-buffs for **60 seconds**:
  - **Strength IV**, **Regeneration V**, **Resistance III**, or **Speed IV**.

**Mode 1 — Spectate Ability Usage**
- Watches for any ability used within **40 blocks** over the next **10 seconds**.
- On detection, has a **1 in 8 chance** (harder than Recording/Replicating) to begin envisioning it:
  - After **2 minutes** of meditation, stores an **unlimited-use envisioned copy** of the ability.
- Fails if the ability cannot be replicated or if the caster is more than 2 sequences weaker than the user.

**Mode 2 — Envision Skill**
- Opens the **Copied Ability Wheel** to use a previously envisioned ability.

---

### Dream Maze
**Sequence Requirement:** 2
**Spirituality Cost:** 1000
**Cooldown:** 7 seconds
*(Cannot be copied, replicated, or used by NPCs)*

Two selectable modes.

**Mode 0 — Self**
- Sends the caster into their own Dream Maze dimension.
- If already inside, ejects them back.

**Mode 1 — Others**
- Sends all **sleeping** entities within **25 blocks** into the caster's Dream Maze.
- Cannot be used from inside the maze.
- Targets find an exit by interacting with **oak doors** inside the maze.

---

### Dream Weave
**Sequence Requirement:** 3
**Spirituality Cost:** 750
**Cooldown:** 20 seconds

Two selectable modes. **Target range:** 20 blocks.

**Mode 0 — Strong**
- Spawns **1 BeyonderNPC** at the target's position, **1 sequence below the caster**.
- The NPC is passive until the target attacks it, at which point it turns hostile.
- Lasts **10 seconds**, then discards.

**Mode 1 — Weak**
- Spawns **3 BeyonderNPCs** arranged in a triangle around the target, **3 sequences below the caster**.
- Same passive-until-attacked behavior.
- Lasts **5 seconds**, then discards.

---

### Mind Invasion
**Sequence Requirement:** 4
**Spirituality Cost:** 1200
**Cooldown:** 10 seconds
*(Cannot be stolen, cannot be used in artifacts — not yet implemented)*

- Currently sends a "not implemented" message on use.

---

### Mental Plague
**Sequence Requirement:** 4
**Spirituality Cost:** 1200
**Cooldown:** 20 seconds
*(Cannot be copied or replicated)*

- **Target range:** 30 blocks.
- Fails if the target is significantly stronger than the caster.
- Applies **Mental Plague** effect (level 4):
  - **Duration:** 10 minutes normally; reduced to **2 minutes** if a purification interaction is active at the target.

**Mental Plague Status** (every tick while active):
- Drains **0.1% sanity** per tick (scales with amplifier: `0.1% × (amplifier + 1)`).
- **Spreads** — infects nearby entities within **15 blocks** that don't already have it, for **8 minutes** at the same amplifier. Visionaries of Sequence 4 or stronger are immune to the spread.

---

### Manipulation
**Sequence Requirement:** 4
**Spirituality Cost:** 750
**Cooldown:** 5 seconds

Two selectable modes. **Target range:** 20 blocks.

**Mode 0 — Group Incite**
- Causes all nearby entities within **20 blocks** to attack the target for **10 seconds**.
- Bypasses Beyonder players/mobs of weaker sequence; non-Beyonder mobs always affected.

**Mode 1 — Control**
- Marks a single target (weaker sequence only) as a marionette for **7 seconds**.
- Gives the caster a **Marionette Controller** item (movement-only).
- *(Players only)*

---

### Virtual Persona
**Sequence Requirement:** 4
**Spirituality Cost:** 500
**Cooldown:** 3 seconds
*(Cannot be used by NPCs, copied, replicated, or used in artifacts)*

Two selectable modes. **Target range (others):** 20 blocks.

**Mode 0 — Self**
- Adds **1 Virtual Persona stack** to the caster (max 10).

**Mode 1 — Others**
- Adds **1 Virtual Persona stack** to the target.
- If no target is found and the caster is **sequence 3 or below**, spawns a **Visionary Avatar** (2 sequences weaker than the caster) instead.

---

### Dream Traversal
**Sequence Requirement:** 5
**Spirituality Cost:** 60
**Cooldown:** 1 second

Three selectable modes. **Target range:** 20–200 blocks depending on mode.
- At sequence 4 or higher, target must be **asleep** for Jump and Hide.

**Mode 0 — Jump**
- **Range:** 200 blocks.
- Teleports the caster to the target's position.
- If currently hiding inside a host, switches to the new host without breaking hide.

**Mode 1 — Hide**
- **Range:** 20 blocks.
- Parasites into the target — caster becomes invisible and floats above the host.
- Caster is ejected if the host dies, is no longer asleep (at sequence 4+), or is removed.
- Toggle off to cancel.

**Mode 2 — Guidance**
- **Range:** 200 blocks.
- Reveals the target's **pathway and sequence** (if caster is stronger) or **pathway only** (if weaker).
- Only works on Beyonder targets.

---

### Nightmare Spectator
**Sequence Requirement:** 5
**Spirituality Cost:** 110
**Cooldown:** 5 seconds
*(Cannot be copied)*

- **Target range:** 200 blocks. Target must be **asleep**.
- **Damage:** **~`DamageLookup(5, 1.1)`** × multiplier per hit (~18–20 damage at sequence 5).
- Applies **Losing Control** effect (level 1, 4 seconds).
- Decreases target sanity by **~8.25% × (multiplier/2)**.

---

### Sleep Inducement
**Sequence Requirement:** 5
**Spirituality Cost:** 90
**Cooldown:** 2 seconds
*(Cannot be copied)*

- **Target range:** 80 blocks.
- Only works if the caster's sequence is **weaker** than the target's (higher number).
- Applies **Asleep** status (level 1, 12 seconds).
- Animates particle lines from caster to target.

**Asleep Status:**
- Zeroes the target's velocity every tick — complete inability to move.
- Applies a **blurred screen** effect for the sleeping player (client-side).
- Jumping is cancelled while asleep.
- **Breaks immediately** if the sleeping entity takes any damage.

---

### Battle Hypnosis
**Sequence Requirement:** 6
**Spirituality Cost:** 150
**Cooldown:** 2 seconds
*(Cannot be copied or replicated)*

- **Target range:** 20 blocks.
- If the target is charmed and the caster's sequence is equal or lower, removes the charm.
- Randomly applies one of three effects:

**Effect 0 — Freeze**
- Stops the target completely for **5 seconds** (movement set to zero, max Slowness).
- Disables ability usage for **3 seconds**.

**Effect 1 — Weaken**
- Applies a **0.4× damage multiplier** debuff for **12 seconds**.
- Moves the target around randomly for **8 seconds** with Weakness level 5.

**Effect 2 — Stop Beyonder Powers**
- Disables ability usage for **9 seconds** (Beyonder targets only; falls back to other effects otherwise).

---

### Dragon Scales
**Sequence Requirement:** 6
**Spirituality Cost:** 2 per tick (toggle)
*(Cannot be copied, replicated, used in artifacts, or stolen)*

- Passively applies **Resistance (Level 1)** every tick while active.

---

### Psychological Invisibility
**Sequence Requirement:** 6
**Spirituality Cost:** 13 per tick (toggle)
*(Cannot be copied or replicated)*

- Makes the caster **invisible** and prevents mobs from targeting them while active.
- Applies **Invisibility** effect (level 20) continuously while active.

---

### Awe
**Sequence Requirement:** 7
**Spirituality Cost:** 40
**Cooldown:** 10 seconds

- **Radius:** 25 blocks.
- **Damage:** **~`DamageLookup(7, 0.675)`** × multiplier per hit (~11–12 damage at sequence 7).
- Applies **Slowness (Level 11)** and **Weakness (Level 6)** for 10 seconds to all nearby enemies.
- Applies a **0.625× damage multiplier** debuff to Beyonder targets for **10 seconds**.
- Continuously moves affected entities in random directions every 8 ticks for 10 seconds.

---

### Frenzy
**Sequence Requirement:** 7
**Spirituality Cost:** 35
**Cooldown:** 5 seconds

- **Target range:** 20 blocks.
- **Damage:** **~`DamageLookup(7, 0.85)`** × multiplier per hit (~12–13 damage at sequence 7).
- Applies **Losing Control** effect with amplifier scaling by relative sequence:
  - Significantly weaker target: level 6
  - Equal/same strength: level 2–4
  - Significantly stronger target: level 1
- Decreases target sanity by **6.5% × multiplier**.

---

### Placate
**Sequence Requirement:** 7
**Spirituality Cost:** 50
**Cooldown:** 5 seconds
*(Cannot be copied or replicated)*

Two selectable modes.

**Mode 0 — Self**
- Restores **15% sanity** to the caster.
- Removes the **Losing Control** effect from the caster.

**Mode 1 — Others**
- **Radius:** 40 blocks.
- Restores **15% sanity** and removes **Losing Control** from all nearby allies.

---

### Psychological Cue
**Sequence Requirement:** 7
**Spirituality Cost:** 5 per tick (toggle)
*(Cannot be copied, replicated, stolen)*

- While active, the next chat message the caster sends is **intercepted** (not broadcast) and parsed as a **prophecy trigger statement**.
- If the message matches a valid trigger pattern and the target is within range, it plants a **Prophecy** on the target player.
- Range scales with sequence: Seq 7 = 10 blocks, Seq 6 = 20, Seq 5 = 30, Seq 4 = 50, Seq 3 = 75, Seq 2 = 125, Seq 1 = 200, Seq 0 = 300 blocks.
- If the target is significantly stronger, the cast fails and applies **Losing Control (Level 4, 25 seconds)** to the caster.
- Cannot be activated if a Story Writing target is already selected.
- Cannot target a Visionary of lower sequence than the caster.

**Prophecy System:**

A Prophecy is a persistent conditional effect planted on a target player. It consists of a **trigger** (a condition checked every tick) paired with an **action** (executed when the trigger fires). The prophecy is written as a natural-language chat message in the format:

`[PlayerName] [trigger keyword] [trigger args] then/and [action keyword] [action args]`

The number of simultaneous prophecies the caster can maintain scales with sequence:

| Caster Sequence | Max Active Prophecies |
|---|---|
| 7 | 5 |
| 6–5 | 10 |
| 4–3 | 15 |
| 2 | 25 |
| 1 | 40 |
| 0 | 80 |

**Triggers** (the condition that causes the action to fire):

| Keyword | Required Seq | Condition |
|---------|-------------|-----------|
| `instant` | 4 | Fires immediately when planted |
| `health [op] [value]` | 4 | Target's health crosses a threshold (operators: `<`, `<=`, `=`, `>=`, `>`) |
| `sanity [op] [value]` | 4 | Target's sanity crosses a threshold |
| `player [names...]` | 6 | Target enters a certain proximity to a named player |
| `on [x y z] [range]` | 7 | Target enters a position within `range` blocks |
| `has [item]` | 7 | Target picks up a specific item |
| `sealed` | 1 | Target is currently sealed (by Sealing Authority) |

**Actions** (what happens when the trigger fires):

| Keyword | Required Seq | Effect |
|---------|-------------|--------|
| `teleport [x y z]` | 0 | Teleports the target to the given coordinates |
| `health [±value]` | 0 | Heals or damages the target by the given amount |
| `sanity [±value]` | 0 | Increases or decreases the target's sanity |
| `weather [clear/rain/thunder]` | 0 | Changes the weather |
| `time [day/night/value]` | 0 | Changes the world time |
| `spawn [entity]` | 0 | Spawns an entity at the target's position |
| `calamity [type]` | 1 | Spawns a calamity (meteor/tornado/earthquake) at the target |
| `digest [±value]` | 1 | Drains or restores the target's digestion |
| `seal` | 1 | Seals the target (disables abilities) |
| `unseal` | 1 | Removes a seal from the target |
| `say [message]` | 4 | Forces the target to broadcast a chat message |
| `stun` | 4 | Stuns the target briefly |
| `confusion` | 4 | Applies confusion to the target |
| `skill [ability_id]` | 4 | Forces the target to use a specific ability |
| `drop [item]` | 7 | Forces the target to drop a specific item |

---

### Telepathy
**Sequence Requirement:** 8
**Spirituality Cost:** 1 per tick (toggle)

- **Range:** 20 blocks.
- Displays the **name** and **sanity percentage** (color-coded) of the looked-at entity in the action bar, updated every 10 ticks.

---

### Spectating
**Sequence Requirement:** 9
**Spirituality Cost:** 0.125 per tick (toggle)
*(Cannot be used by NPCs, copied, or replicated)*

- **Range:** 40 blocks.
- Grants **Night Vision** while active.
- Sends the looked-at entity's ID to the client for visual spectating effects.

---

## Passive Abilities

---

### Meta Awareness
**Sequence Requirement:** 1

- **Passive:** When any player says the caster's **username or honorific name** in chat, the caster receives an automatic prayer notification showing the speaker's name, pathway, sequence, and coordinates (with a 5-second cooldown).
- Also triggers when another player successfully **divines** the caster.
- The caster can respond via the Honorific Names menu.

---

### Pure Idealism
**Sequence Requirement:** 2

- **Passive:** Provides a **sanity-scaling damage multiplier** bonus. The bonus scales linearly with current sanity (0% sanity = ×1.0 multiplier, 100% sanity = full bonus).
- Multiplier per sequence:
  - Sequence 2: up to **+5%** (×1.05 at full sanity)
  - Sequence 1: up to **+15%** (×1.15 at full sanity)
  - Sequence 0: up to **+25%** (×1.25 at full sanity)

---

### Physical Enhancements (Visionary)
**Sequence Requirement:** 9

Includes **Night Vision** at Seq 7 and below. Includes **Concealment** (reduces detection/concealment power) at Seq 3 and below. **Fire Resistance** at Seq 1 only.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | —        | —          | +1    | —            | —            | — |
| 8        | +1       | —          | +1    | +1           | —            | — |
| 7        | +1       | —          | +2    | +2           | +1           | Night Vision +1 |
| 6        | +1       | +1         | +2    | +4           | +1           | Night Vision +1 |
| 5        | +1       | +2         | +2    | +6           | +1           | Night Vision +1 |
| 4        | +3       | +5         | +4    | +12          | +2           | Night Vision +1 |
| 3        | +3       | +6         | +4    | +15          | +2           | Night Vision +1, Concealment |
| 2        | +4       | +9         | +5    | +23          | +3           | Night Vision +1, Concealment |
| 1        | +4       | +12        | +5    | +30          | +4           | Fire Resistance +4, Concealment |
| 0        | +6       | +12        | +6    | +40          | +4           | Night Vision +1, Concealment |
