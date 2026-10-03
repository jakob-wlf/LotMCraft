# Red Priest Pathway Abilities

## Spirituality

Spirituality regenerates at **0.06% of max per tick** (1.2% per second) passively.

| Sequence | Max Spirituality | Regen/sec |
|----------|-----------------|-----------|
| 9        | 144             | 1.7/s     |
| 8        | 160             | 1.9/s     |
| 7        | 624             | 7.5/s     |
| 6        | 960             | 11.5/s    |
| 5        | 1,520           | 18.2/s    |
| 4        | 3,120           | 37.4/s    |
| 3        | 4,000           | 48.0/s    |
| 2        | 8,000           | 96.0/s    |
| 1        | 16,000          | 192.0/s   |
| 0        | 48,000          | 576.0/s   |

---

## Active Abilities

---

### Sacrifice
**Sequence Requirement:** 1–3
**Spirituality Cost:** 3000
**Cooldown:** 100 minutes
*(Cannot be copied, replicated, used in artifacts, stolen, shared with teammates, or used by NPCs)*

- Requires at least **500 kills** to activate.
- Consumes accumulated kills at a rate of 500 kills per second of duration, up to a maximum of 60 seconds. The ability will always consume enough kills to activate the maximum possible duration.
- After the animation (35 ticks), the caster's sequence is **temporarily reduced by 1** (e.g. Seq 1 → Seq 0) and gains the corresponding rank and abilities.
- While Sacrifice is active:
  - A red bar on the HUD shows remaining duration.
  - Sun Pathway's purification powers cannot drain digestion or regress the caster's sequence.
- When the duration expires, the caster **reverts to their original sequence** with digestion restored.
---

### Conquering
**Sequence Requirement:** 1
**Spirituality Cost:** 8000
**Cooldown:** 40 seconds
*(Cannot be copied or replicated)*

- **Radius:** 3.75 blocks
- Applies the **CONQUERED** effect (Level 7, 3 hours) to all nearby entities with a lower sequence than the caster.
- Only affects entities weaker than the caster.

**CONQUERED Effect** (while active, every tick):
- Zeroes the target's velocity — complete inability to move.
- Forces the target's health down to **1 HP** if it is above that.
- Disables the target's Beyonder abilities for 1 second, refreshed every tick (effectively permanent for the duration).
- Lasts **3 hours** — functionally permanent in any combat scenario.

---

### Flame Authority
**Sequence Requirement:** 1
**Spirituality Cost:** 1800
**Cooldown:** 8 seconds
*(Cannot be copied)*

Three selectable modes:

**Mode 0 — Destruction Spear**
- **Range:** 120 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~62.2 damage per hit**.

**Mode 1 — Inferno**
- **Radius:** 22.5 blocks
- **Duration:** 4 seconds (80 ticks)
- **Hits:** **16 hits** — one every 5 ticks (~62.2 base DPS)
- **Damage:** **~15.6 damage per hit**.

**Mode 2 — Flame Vortex**
- **Radius:** 9 blocks
- **Duration:** 6 seconds (120 ticks)
- **Hits:** **24 hits** — one every 5 ticks (~69.5 base DPS)
- **Damage:** **~17.4 damage per hit**.

---

### Puppet Soldier Creation
**Sequence Requirement:** 1
**Spirituality Cost:** 2000
**Cooldown:** 120 seconds
*(Cannot be copied or replicated)*

- Summons **6 knight subordinates** (Red Priest, Sequence 4) within 3 blocks of the caster.
- Each subordinate has a **1.8× damage multiplier**.
- Subordinates persist indefinitely.
- Damage taken by the caster is split equally among all active subordinates.

---

### Flight
**Sequence Requirement:** 2
**Spirituality Cost:** 12 per tick (toggle)
*(Cannot be copied, replicated, or used by NPCs)*

- Toggles sustained flight for the caster.
- Deactivates automatically if flight is externally disabled.

---

### Weather Manipulation
**Sequence Requirement:** 2
**Spirituality Cost:** 1200
**Cooldown:** 25 seconds
*(Cannot be copied)*

Three selectable modes:

**Mode 0 — Snow Storm**
- **Radius:** 60 blocks
- **Duration:** 30 seconds
- **Hits:** **150 hits** — one every 4 ticks (~41 base DPS)
- **Damage:** **~5.9 damage per hit**.
- Applies **Weakness (Level 1)** and **Slowness (Level 7)** for 5 seconds per hit.
- Creates snow layers and snow blocks if griefing is enabled.
- Forces rain weather for 5 minutes.

**Mode 1 — Drought**
- **Radius:** 90 blocks
- **Duration:** 30 seconds
- **Hits:** **150 hits** — one every 4 ticks (~41 base DPS)
- **Damage:** **~5.9 damage per hit**.
- Applies **Weakness (Level 1)** and **Slowness (Level 4)** for 5 seconds per hit.
- Creates fire, sand, and sandstone blocks if griefing is enabled.
- Forces rain weather for 5 minutes.

**Mode 2 — Tornados**
- Spawns **1 main tornado** + **30 additional tornadoes** across a ±60 block area.
- **Main tornado damage:** **~36.8 damage** (at target) or **~48.7 damage** (untargeted).
- **Additional tornado damage:** 17 each (fixed, unscaled).

---

### Essence of War
**Sequence Requirement:** 3
**Spirituality Cost:** 1000
**Cooldown:** 180 seconds
*(Cannot be copied)*

- Summons a **War Banner** at the caster's position lasting **120 seconds (2400 ticks)**.
- **Radius:** 25 blocks
- **Effect Interval:** Every tick

**War Banner effects on the caster (within radius):**
- **Strength:** Level 7, refreshed every second
- **Speed:** Level 7, refreshed every second
- **+1.5× damage modifier** (removed if caster leaves radius or dies)

**War Banner effects on all other nearby entities (within radius):**
- **Weakness:** Level 4, refreshed every second
- **Slowness:** Level 4, refreshed every second
- **−25% damage modifier** (removed if entity leaves radius or dies)

---

### Fog of War
**Sequence Requirement:** 3
**Spirituality Cost:** 800 per tick (toggle)
*(Cannot be copied)*

- **Radius:** 20 blocks
- **Effect Interval:** Every 2 ticks
- Applies to all nearby enemies while active:
  - No light source nearby: **Blindness (Level 0, 2s)**, **Slowness (Level 2, 2s)**, **−30% damage modifier**
  - Light source nearby: **Slowness (Level 0, 2s)**, **−15% damage modifier**
- **Light_source Interaction:** Weakens the fog effect if a light source interaction is present.

---

### War Cry
**Sequence Requirement:** 3
**Spirituality Cost:** 350
**Cooldown:** 18 seconds
*(Registers as a morale_boost interaction)*

- **Radius:** 19 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~22.5 damage per hit**.
- **Knockback:** 1.5× applied to hit entities.
- Applies to the caster for **12 seconds (240 ticks)**:
  - **Strength:** Level 3
  - **Speed:** Level 4
  - **Resistance:** Level 1
  - **+1.5× damage modifier**
- **Block Destruction:** Launches falling blocks in circle outlines (radii 3 and 5) if griefing is enabled.

---

### War Song
**Sequence Requirement:** 3
**Spirituality Cost:** 400
**Cooldown:** 70 seconds
*(Registers as a morale_boost interaction)*

- **Duration:** 30 seconds (600 ticks)
- Applies to the caster for the duration:
  - **Strength:** +2 levels on top of existing
  - **Speed:** +2 levels on top of existing
  - **+1.5× damage modifier**
  - **+1.5× buff modifier**

---

### Chain of Command
**Sequence Requirement:** 4
**Spirituality Cost:** 900
**Cooldown:** 5 seconds
*(Cannot be copied, replicated, or used by NPCs)*

- **Range:** 3 blocks
- Converts a nearby non-player entity with a lower sequence into a subordinate.
- Damage taken by the caster is distributed equally among all active subordinates.

---

### Flame Mastery
**Sequence Requirement:** 4
**Spirituality Cost:** 100
**Cooldown:** 5 seconds

Three selectable modes:

**Mode 0 — Fireball Barrage**
- Fires **15 × sequence multiplier** fireballs, one every 7 ticks.
- **Hits:** Up to ~15 hits per cast (one per fireball)
- **Damage per fireball:** **~17.2 damage**.
- **Projectile Speed:** 1.85 blocks/tick; **Range:** 50 blocks.

**Mode 1 — Eruption**
- **Radius:** 9 blocks
- **Hits:** **1 hit** per cast (3 stacked explosions)
- **Damage:** **~22.6 damage per hit**.
- Launches 25 falling magma/basalt blocks into the air if griefing is enabled.

**Mode 2 — Flame Transformation**
- Toggles a fire transformation state; the caster propels forward continuously while transformed.
- No direct damage — used for movement.

---

### Steel Mastery
**Sequence Requirement:** 4
**Spirituality Cost:** 500
**Cooldown:** 2 seconds

Two selectable modes:

**Mode 0 — Steel Skin**
- **Additional Cost:** 4 spirituality per tick while active (toggle)
- **Effect Interval:** Every 2 ticks
- Applies **Resistance (Level 2, 1 second)** continuously while active.

**Mode 1 — Steel Chains**
- **Range:** 25 blocks
- **Duration:** 8 seconds (160 ticks)
- **Effect Interval:** Every 5 ticks
- Applies **Slowness (Level 20, 0.5 seconds)** — complete immobility.
- **Blink_escape Interaction:** Chains are broken if the target has a blink_escape interaction.

---

### Cull
**Sequence Requirement:** 5
**Spirituality Cost:** 3 per tick (toggle)
*(Cannot be copied or replicated)*

- **Radius:** 30 blocks
- While active, applies a **+1.7× damage modifier** to the caster.
- Makes all nearby entities glow (visible outline) to the caster.

---

### Pyrokinesis
**Sequence Requirement:** 7
**Spirituality Cost:** 30
**Cooldown:** 0.75 seconds

Five selectable modes:

**Mode 0 — Fireball**
- **Range:** 50 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~11.7 damage per hit**.
- **Projectile Speed:** 1.2 blocks/tick.

**Mode 1 — Flame Wave**
- **Radius:** 5.5 blocks
- **Hits:** **1 hit** (applied at 18-tick delay)
- **Damage:** **~12.5 damage per hit**.

**Mode 2 — Wall of Fire**
- **Size:** 15 blocks wide, 7 blocks tall
- **Duration:** 20 seconds (400 ticks)
- **Hit Interval:** Every 1 tick (15-tick immunity between hits per entity)
- **Damage per hit:** **~7.7 damage**.
- **Knockback:** 0.8× toward each entity on hit.

**Mode 3 — Fire Ravens**
- **Range:** 40 blocks
- Fires **8 ravens** simultaneously.
- **Hits:** **1 hit per raven** (up to 8 total)
- **Damage per raven:** **~9.8 damage**.

**Mode 4 — Flaming Spear**
- **Range:** 50 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~11.8 damage per hit**.
- **Projectile Speed:** 1.6 blocks/tick.

---

### Provoking
**Sequence Requirement:** 8
**Spirituality Cost:** 15
**Cooldown:** 5 seconds

- **Radius:** 18 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~7.3 damage per hit**.
- **Knockback:** 1.5× applied to all hit entities.
- Applies to all nearby entities for **6 seconds (120 ticks)**:
  - **Slowness:** Level 1
  - **Weakness:** Level 1
- Sets all nearby mobs to target the caster.

---

### Trap
**Sequence Requirement:** 9
**Spirituality Cost:** 10
**Cooldown:** 1 second

- Places a trap at the caster's position, marked by a **red particle circle** (radius 1.6 blocks, 22 particles per tick).
- **Trigger Radius:** 1.35 blocks
- **Duration:** 30 seconds (600 ticks) before expiring
- **Hits:** **1 hit** when triggered
- **Damage:** **~7.8 damage per hit**.
- Triggers a size-4 explosion on detonation. Destroys blocks if griefing is enabled.

---

## Passive Abilities

---

### Physical Enhancements (Red Priest)
**Sequence Requirement:** 9

Includes **Fire Resistance** at Seq 7 and below. Includes **Regeneration** at all sequences.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | +1       | —          | +2    | —            | +1           | — |
| 8        | +2       | —          | +3    | +5           | +1           | — |
| 7        | +2       | —          | +3    | +6           | +1           | Fire Resistance +1 |
| 6        | +2       | +1         | +3    | +8           | +2           | Fire Resistance +1 |
| 5        | +3       | +2         | +4    | +10          | +2           | Fire Resistance +2 |
| 4        | +4       | +7         | +4    | +18          | +3           | Fire Resistance +2 |
| 3        | +4       | +8         | +4    | +19          | +3           | Fire Resistance +3 |
| 2        | +6       | +11        | +6    | +26          | +4           | Fire Resistance +3 |
| 1        | +6       | +12        | +6    | +31          | +4           | Fire Resistance +4 |
| 0        | +7       | +15        | +6    | +46          | +6           | Fire Resistance +6 |

---

### Flaming Hit
**Sequence Requirement:** 7

- **Passive:** Each melee hit sets the target on fire for **+50 fire ticks**.

---

### Fire Resistance
**Sequence Requirement:** 7

- **Passive:** The caster takes no damage from fire or burning while this passive is active.
