# Abyss Pathway Abilities

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

### Flames of the Abyss
**Sequence Requirement:** 1
**Spirituality Cost:** 800
**Cooldown:** 10 seconds

Two selectable modes:

**Mode 0 — Meteor Rain**
- Fires **8 meteors** staggered every 15 ticks (over ~5 seconds).
- Each meteor has a **16-block explosion radius** and **2.5× knockback**.
- **Hits:** **1 hit per meteor** — up to 8 total
- **Damage per meteor:** **~64 damage**.

**Mode 1 — Abyss Pillars**
- **Radius:** 18 blocks
- **Hits:** **1 hit** (direct damage burst) + pillar spawns
- **Damage:** **~54.9 damage per hit**.
- Spawns **12 pillars** in a ring (3–10 blocks out), then **20 random pillars** within 16 blocks at tick 12.
- Applies to all nearby entities:
  - **Poison:** Level 4, 20 seconds
  - **Wither:** Level 1, 6 seconds
  - **Slowness:** Level 3, 10 seconds
  - **Blindness:** Level 0, 5 seconds

---

### Malice Seed
**Sequence Requirement:** 2
**Spirituality Cost:** 150
**Cooldown:** 400 seconds
*(Cannot be copied, replicated, or used in artifacts)*

- **Range:** 25 blocks
- Plants an invisible seed inside a target. Only one seed can be active at a time.
- **Backfire:** If the target is significantly stronger, the caster takes **10 damage** and **Losing Control (Level 3, 5 seconds)**.
- **Growth Interval:** Every 30 seconds — up to **10 growth stages** over 5 minutes.
- **If the caster dies before the seed fully grows:**
  - The caster's death is cancelled and they are healed to **50% max health**.
  - The caster teleports to the target.
  - The target takes **5 + (growth × 3) damage** (5–35 depending on growth stage).
  - The seed is removed.

---

### Fear Aura
**Sequence Requirement:** 2
**Spirituality Cost:** 1000
**Cooldown:** 30 seconds
*(Cannot be copied)*

- **Radius:** 20 blocks
- **Duration:** 30 seconds (600 ticks)
- **Effect Interval:** Every tick
- Applies to all nearby entities each tick:
  - **Darkness:** Level 5, 1.5 seconds
  - **Blindness:** Level 4, 1 second
  - **Slowness:** Level 4, 1 second
  - **−60% damage modifier**
- **Sanity Drain:** −0.0033 per tick per entity.
- **Damage (vs. weaker targets only):** **~10.9 damage every 10 ticks** — **60 hits** over the full duration (~21.8 base DPS).
- **Purification Interaction:** Fully suppressed.

---

### Corrupting Voice
**Sequence Requirement:** 3
**Spirituality Cost:** 0 (toggle)
*(Cannot be copied, replicated, or used by NPCs)*

- **Range:** 50 blocks
- **Chat Cooldown:** 3 seconds between activations
- While active, each chat message sent by the caster deals damage and applies effects to all nearby entities:
  - **Damage:** `(2 + characters × 0.5) × 2` — scales with message length, capped at 30 characters (max **~32 damage** per message).
  - Applies **3 random negative effects** per target, drawn from: Slowness, Weakness, Blindness, Nausea, Hunger, Wither, Poison, Mining Fatigue, Losing Control. Random duration (10–30 seconds) and amplifier (0–4).

---

### Demonic Spells
**Sequence Requirement:** 4
**Spirituality Cost:** 400
**Cooldown:** 3 seconds

Three selectable modes:

**Mode 0 — Acid Swamp**
- **Radius:** 15 blocks
- **Duration:** 8 seconds (160 ticks)
- **Hits:** **8 hits** — one every 20 ticks (~20.8 base DPS)
- **Damage:** **~20.8 damage per hit**.
- Applies every hit to entities in range:
  - **Poison:** Level 2, 6 seconds
  - **Wither:** Level 1, 4 seconds
  - **Slowness:** Level 2, 4 seconds
- **Purification Interaction:** Cancels damage.

**Mode 1 — Filthy Illusion**
- Grants the caster **Invisibility** for 8 seconds.
- Spawns a **decoy clone** at the caster's position that lasts 5 seconds, then explodes:
  - **Explosion radius:** 15 blocks
  - **Damage:** **~20.2 damage**.
  - **Knockback:** 1.5×
- **Light_strong Interaction:** Destroys the clone early.

**Mode 2 — Hellfire Wall**
- **Radius:** 13 blocks (wall ring, ~164 barrier blocks, 18 blocks tall)
- **Duration:** 8 seconds (160 ticks)
- **Hit Interval:** Every 5 ticks (entities between 11–15 blocks from center)
- **Hits:** Up to **32 hits** over the full duration
- **Damage:** **~19.6 damage per hit**.

---

### Desire Control
**Sequence Requirement:** 4
**Spirituality Cost:** 150
**Cooldown:** 10 seconds
*(Cannot be copied or replicated)*

Two selectable modes:

**Mode 0 — Single Target**
- **Range:** 25 blocks
- **Duration:** 8 seconds (if target has sanity), 6 seconds otherwise
- Applies to target:
  - **Losing Control:** Level 1–4 (scales with sequence difference), for the duration
  - **Weakness:** Level 2, for the duration
- **Sanity Drain:** −0.3 × sequence multiplier (instant).

**Mode 1 — AOE**
- **Radius:** 20 blocks
- **Duration:** 6 seconds
- Applies to all nearby entities:
  - **Losing Control:** Level 0–3 (1 less than single-target equivalent)
  - **Nausea:** Level 1, 4 seconds
  - **Slowness:** Level 2, 4 seconds
- **Sanity Drain:** −0.15 × sequence multiplier (instant).

---

### Mind Fog
**Sequence Requirement:** 4
**Spirituality Cost:** 25 per tick (toggle)
*(Cannot be copied, replicated, or used by NPCs)*

- **Radius:** 20 blocks
- **Effect Interval:** Every tick
- Each tick, each entity in range:
  - **Sanity Drain:** −0.05
  - **20% chance** of receiving one random effect (5 seconds each): Slowness I, Weakness I, Blindness I, Nausea I, Hunger II, or Wither I
- **Purification / Calming Interaction:** Cancels effect.

---

### Avatar of Desire
**Sequence Requirement:** 5
**Spirituality Cost:** 0 (toggle)
*(Cannot be copied, replicated, or used in artifacts)*

- Transforms the caster into a miniaturized form (scale 0.3×).
- Grants **Speed VIII** continuously while active.
- **Radius:** 3.75 blocks
- Applies **Losing Control** to nearby entities each tick:
  - Significantly weaker target: **Level 5, 10 seconds**
  - Significantly stronger target: Effect reflects onto caster (**Level 1, 2 seconds**) and the ability cancels.
  - Equal target: **Level 2–3, 5 seconds**
- **Purification Interaction:** Cancels the transformation.

---

### Defiling Seed
**Sequence Requirement:** 5
**Spirituality Cost:** 60
**Cooldown:** 7 seconds
*(Cannot be copied)*

- **Range:** 25 blocks
- Plants a seed in the target. Cannot be applied if already defiled.
- **Backfire:** If the target is significantly stronger, the caster takes **10 damage** and **Losing Control (Level 3, 5 seconds)**.
- **Duration:** 2 minutes (2400 ticks); ticks every 8 ticks.
- Each interval has a random chance (~14%) of dealing **~2 × sequence multiplier damage**, or inflicting **Losing Control** or **Slowness (Level 2–6, 9 seconds)**.

---

### Devil Transformation
**Sequence Requirement:** 6
**Status:** *Not yet implemented.*

---

### Flame Spells
**Sequence Requirement:** 6
**Spirituality Cost:** 30
**Cooldown:** 1.75 seconds

Two selectable modes:

**Mode 0 — Sulfur Fireball**
- **Range:** 50 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~13.4 damage per hit**.
- **Projectile Speed:** 1.2 blocks/tick.
- **Block Destruction:** Respects server griefing setting.

**Mode 1 — Volcanic Eruption**
- **Radius:** 5 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~14.3 damage per hit**.
- Sets targets on fire for **3 seconds**.
- Launches falling magma/basalt blocks from two circles (radii 3 and 5) if griefing is enabled.

---

### Language of Foulness
**Sequence Requirement:** 6
**Spirituality Cost:** 100
**Cooldown:** 3 seconds
*(Cannot be copied or replicated)*

- **Range:** 15 blocks

Three selectable modes:

**Mode 0 — Slow**
- **Duration:** 8 seconds (10 ticks if target is significantly stronger)
- **Effect Interval:** Every tick — zeroes the target's velocity
- Applies **Slowness (Level 20, 0.75 seconds)** — complete immobility.

**Mode 1 — Corruption**
- Applies once:
  - **Nausea:** Level 1, 8 seconds
  - **Weakness:** Level 1, 8 seconds
  - **Losing Control:** Level 0–2 (random), 8 seconds

**Mode 2 — Death**
- Cancelled if target is significantly stronger.
- **Duration:** 8 seconds (160 ticks)
- **Effect Interval:** Every tick
- Each tick applies **Wither (Level 3, 0.75 seconds)** (stacking continuously).
- **1 in 8 chance per tick** of dealing **~13.8 damage**.

---

### Poisonous Flame
**Sequence Requirement:** 8
**Spirituality Cost:** 12
**Cooldown:** 0.8 seconds
*(Registers as burning and poison interactions)*

- **Range:** ~35 blocks (shoots green flame along look direction at 0.5 blocks/iteration)
- **Damage Radius:** `2.75 × multiplier` blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~`DamageLookup(8, 0.9)` × multiplier per hit**.
- Applies **Poison (Level 2, 8 × max(multiplier/2, 1) seconds)** if not purified.
- **Water Interaction:** Extinguished — no damage or poison.
- **Purification Interaction:** Poison effect is neutralized.

---

### Toxic Smoke
**Sequence Requirement:** 8
**Spirituality Cost:** 22
**Cooldown:** 5.5 seconds

- **Radius:** 6.5 blocks (eye position)
- **Duration:** 5 seconds (100 ticks)
- **Hit Interval:** Every 6 ticks
- **Hits:** ~**16 hits** over the full duration (~8 base DPS)
- **Damage:** **~2.7 damage per hit**.
- Applies every hit:
  - **Poison:** Level 1, 1.5 seconds
  - **Blindness:** Level 0, 0.5 seconds
- **Purification Interaction:** Completely cancels effect.
- **Burning Interaction:** Triggers an explosion (**~9.8 damage**, radius 9 blocks).

---

## Passive Abilities

---

### Physical Enhancements (Abyss)
**Sequence Requirement:** 9

Includes **Fire Resistance** at Seq 6 and below. No Night Vision at any sequence.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | +1       | —          | +1    | —            | —            | — |
| 8        | +2       | —          | +2    | +5           | —            | — |
| 7        | +2       | —          | +2    | +6           | +1           | — |
| 6        | +3       | +3         | +3    | +7           | +2           | Fire Resistance +1 |
| 5        | +3       | +4         | +3    | +9           | +2           | Fire Resistance +2 |
| 4        | +4       | +8         | +4    | +18          | +3           | Fire Resistance +2 |
| 3        | +4       | +9         | +4    | +19          | +3           | Fire Resistance +3 |
| 2        | +5       | +12        | +5    | +27          | +4           | Fire Resistance +3 |
| 1        | +5       | +13        | +5    | +32          | +4           | Fire Resistance +4 |
| 0        | +6       | +14        | +6    | +47          | +6           | Fire Resistance +6 |

---

### Word Immunity
**Sequence Requirement:** 3

- **Passive:** Blocks the application of **Blindness** and **Nausea** effects entirely.
- Actively removes these effects every tick if they somehow get applied.

---

### Fire Resistance (Abyss)
**Sequence Requirement:** 6

- **Passive:** The caster takes no damage from fire or burning.
