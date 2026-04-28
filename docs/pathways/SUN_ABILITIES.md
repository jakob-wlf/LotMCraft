# Sun Pathway Abilities

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

## Purification Damage

Sun pathway abilities at **Sequence 3 or stronger** deal a special damage type called **Purification Damage**. In addition to dealing normal damage, every hit drains the target's **digestion** — a hidden meter that represents how well a Beyonder has digested their potion. This only applies to **Beyonder players** (not mobs or non-Beyonders).

**Digestion drain per hit:**

There are two categories of Purification damage:

- **Direct** (melee hits, projectiles, spawned entities — `PURIFICATION` damage type):
  - Base drain: **5%** per hit.
  - **+1%** for each sequence the attacker is stronger than the victim.
  - **−1%** for each sequence the attacker is weaker than the victim.
  - Minimum: **1%** per hit.

- **Indirect** (ticking AoE abilities — `PURIFICATION_INDIRECT` damage type):
  - Base drain: **0.7%** per hit.
  - **+0.1%** for each sequence the attacker is stronger.
  - **−0.1%** for each sequence the attacker is weaker.
  - Minimum: **0.1%** per hit.

**When digestion reaches 0%**, each subsequent hit has a **10% chance** to:
- **Regress the victim by 1 sequence** (e.g. Sequence 5 → Sequence 6).
- Give the attacker the corresponding **Beyonder Characteristic item** for that sequence.
- Reset the victim's digestion to **100%** so they are not immediately vulnerable again.

Abilities marked as *purification damage type* above use `PURIFICATION` (direct). Abilities marked *purification damage type (indirect)* use `PURIFICATION_INDIRECT` (AoE ticks).

This mechanic also applies to **team members sharing Sun abilities** — if a non-Sun player uses a shared Sun ability from a Sequence 3 or stronger Sun teammate, the digestion drain still occurs.

---

## Active Abilities

---

### Pure White Light
**Sequence Requirement:** 1
**Spirituality Cost:** 2200
**Cooldown:** 4 seconds
*(Cannot be copied)*

- **Target Range:** 50 blocks; **Damage Radius:** expanding sphere
- **Radius:** Starts at 2 blocks, grows by 0.8 blocks every 2 ticks.
- **Hits:** **55 hits** over the full duration
- **Damage:** **~54.9 damage per hit**.
- Converts blocks to Light blocks within the expanding sphere if griefing is enabled.
- **Interactions:** Registers purification, light_source, light_strong, light_weak in a **25-block radius**.

---

### Divine Kingdom Manifestation
**Sequence Requirement:** 1
**Spirituality Cost:** 2900
**Cooldown:** 3 minutes
*(Cannot be copied, replicated, or used by NPCs)*

- Summons a **Sun Kingdom entity** lasting **2 minutes** with a **120-block radius**.
- Every tick, affects all entities in range:
  - **Non-Beyonders:** Instantly killed.
  - **Beyonders Sequence 4 or weaker:** Abilities disabled for 5 seconds (refreshed every tick), effectively permanent for the duration. Made to Glow.
  - **Beyonders Sequence 3 or stronger:** Damage modifier reduced to **0.2×** for the duration. Made to Glow.
- **Interactions:** Registers purification, light_source, light_strong, light_weak.

---

### Spear of Light
**Sequence Requirement:** 2
**Spirituality Cost:** 1250
**Cooldown:** 2.5 seconds
*(Cannot be copied)*

- **Range:** 50 blocks (projectile, spawns offset 4.5–8 blocks to the side)
- **Projectile Speed:** 3 blocks/tick
- **Hits:** **1 hit** on contact
- **Damage:** **~43.3 damage per hit**.
- **Interactions:** Registers purification, light_source, light_strong, light_weak at the impact point.

---

### Solar Envoy
**Sequence Requirement:** 2
**Spirituality Cost:** 20 per tick (toggle)
*(Cannot be copied, replicated, or used in artifacts)*

- Transforms the caster into a levitating Solar Envoy form, hovering 5 blocks above their activation position.
- The caster is locked in place — velocity zeroed every tick, no gravity.
- **Radius:** 37 blocks
- **Hit Interval:** Every tick
- **Damage:** **~11.2 damage per tick** (purification damage type).
- **Block Destruction:** Clears all blocks in a 25-block sphere and scatters fire if griefing is enabled (on activation).
- **Interactions:** Registers light_strong and light_weak continuously.

---

### Wings of Light
**Sequence Requirement:** 2
**Spirituality Cost:** 5 per tick (toggle)
*(Cannot be copied)*

- Toggles sustained flight for the caster (flying speed 0.45).
- Deactivates if flight is externally disabled.
- Displays a Wings of Light transformation visual on the caster.

---

### Sword of Justice
**Sequence Requirement:** 3
**Spirituality Cost:** 500
**Cooldown:** 2.5 seconds
*(Cannot be copied)*

- **Target Range:** 20 blocks
- Summons a sword entity that descends from 15 blocks above the target.
- Applies **Slowness (Level 5, 1 second)** to the target on cast.
- **Hits:** **1 hit** on impact
- **Damage:** **~22.7 damage per hit**.
- **Interactions:** Registers purification, light_source, light_strong, light_weak.

---

### Wall of Light
**Sequence Requirement:** 3
**Spirituality Cost:** 800
**Cooldown:** 5 seconds
*(Cannot be copied)*

- **Target Range:** 12 blocks; wall is **60 blocks wide** and **19 blocks tall**
- **Duration:** 20 seconds
- **Hit Interval:** Every 7 ticks per block position
- **Damage:** **~16 × sequence multiplier** per hit (scales with caster's sequence).
- **Knockback:** 1.4× away from each wall block on hit.
- Made of invisible Barrier blocks — persists through the duration, then removed.

---

### Flaring Sun
**Sequence Requirement:** 4
**Spirituality Cost:** 500
**Cooldown:** 8 seconds
*(Cannot be copied)*

- **Target Range:** 25 blocks; **Damage Radius:** 17 blocks
- **Duration:** 19 seconds
- **Hit Interval:** Every 4 ticks
- **Hits:** **~95 hits** over the full duration (~19 base DPS)
- **Damage:** **~4 damage per hit** (purification damage type).
- Creates a Sun entity at the target position for the duration.
- **Block Destruction:** Clears a 7-block sphere, fills with fire, and surrounds with basalt at radius 8 if griefing is enabled.
- **Interactions:** Registers purification, burning, light_source, light_strong, light_weak in a 14-block radius.

---

### Unshadowed Spear
**Sequence Requirement:** 4
**Spirituality Cost:** 250
**Cooldown:** 1.5 seconds
*(Cannot be copied)*

- **Range:** 50 blocks (projectile, spawns offset 3.5–6 blocks to the side)
- **Projectile Speed:** 3 blocks/tick
- **Hits:** **1 hit** on contact
- **Damage:** **~19.6 damage per hit**.
- **Interactions:** Registers purification, light_source, light_strong, light_weak at the impact point.

---

### Unshadowed Domain
**Sequence Requirement:** 4
**Spirituality Cost:** 800
**Cooldown:** 50 seconds
*(Cannot be copied or replicated)*

- **Radius:** 40 blocks
- **Duration:** 30 seconds
- **Hit Interval:** Every 10 ticks
- **Hits:** **60 hits** over the full duration (~18 base DPS)
- **Damage:** **~9 damage per hit** (purification damage type; only damages enemies weaker than the caster).
- Fills all air blocks adjacent to solid blocks within 40 blocks with Light blocks on activation (removed on expiry).
- Applies **Glowing (Level 2, 2 seconds)** to all nearby entities every interval.
- **Interactions:** Registers purification, light_source, light_strong, light_weak in a **40-block radius** for 30 seconds.

---

### Light of Holiness
**Sequence Requirement:** 5
**Spirituality Cost:** 150
**Cooldown:** 1.85 seconds

- **Target Range:** 45 blocks; beam descends from **18 blocks above** target
- **Hit Radius:** 8 blocks (per step of the beam)
- **Hits:** **22 hits** as the beam descends step by step over 22 ticks
- **Damage:** **~13.6 damage per hit** (purification damage type).
- Places Light blocks along the beam path (removed after ~2 seconds).
- **Interactions:** Registers purification and light_weak at the target point.

---

### Purification Halo
**Sequence Requirement:** 5
**Spirituality Cost:** 120
**Cooldown:** 9 seconds

- **Radius:** Expanding ring — starts at 0.5 blocks, grows by 0.25 blocks every 2 ticks, over 5 seconds.
- **Hit Zone:** Ring at current radius (±0.25 blocks)
- **Hits:** **50 hits** over the full duration
- **Damage:** **~15 × sequence multiplier** per hit (scales with caster's sequence; purification damage type).
- Creates a visible golden ring visual (radius 30) lasting 20 seconds.
- **Interactions:** Registers purification and light_weak in a 10-block radius.

---

### God Says It's Effective
**Sequence Requirement:** 6
**Spirituality Cost:** 25
**Cooldown:** 20 seconds
*(Cannot be copied or replicated)*

- Applies a **+1.35× damage modifier** to the caster for **20 seconds**.
- Displays a golden particle ring around the caster every 35 ticks while active.

---

### God Says It's Not Effective
**Sequence Requirement:** 6
**Spirituality Cost:** 25
**Cooldown:** 20 seconds
*(Cannot be copied or replicated)*

- **Radius:** 15 blocks
- Applies a **0.8× damage modifier** to all Beyonders currently nearby for **20 seconds**.
- Targets are captured at cast time — entities that enter or leave the radius after activation are unaffected.
- Displays a golden particle ring around the caster every 35 ticks while active.

---

### Cleave of Purification
**Sequence Requirement:** 7
**Spirituality Cost:** 20
**Cooldown:** 1.8 seconds

- **Range:** Melee (optimal distance: 1 block); **Hit Radius:** 2.75 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~10.7 damage per hit** (purification damage type).
- Places a temporary Light block at the impact point (~1.25 seconds).
- **Interactions:** Registers purification and light_weak in a 2-block radius.

---

### Fire of Light
**Sequence Requirement:** 7
**Spirituality Cost:** 50
**Cooldown:** 1.75 seconds

- **Range:** 10 blocks; **Hit Radius:** 2.5 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~10.3 damage per hit** (purification damage type).
- Places a temporary Light block at the target position (~1.25 seconds).
- **Interactions:** Registers purification, burning, light_source, light_weak at the target point.

---

### Holy Oath
**Sequence Requirement:** 7
**Spirituality Cost:** 1.5 per tick (toggle)
*(Cannot be copied or replicated)*
*(Registers as a morale_boost interaction)*

- Continuously applies the following to the caster each tick:
  - **Strength:** Level 2
  - **Resistance:** Level 1
  - **Speed:** Level 3
  - **Health Boost:** Level 7 (+14 bonus hearts)
  - **Regeneration:** Level 2
  - **Night Vision:** Level 2

---

### Holy Light Summoning
**Sequence Requirement:** 7
**Spirituality Cost:** 64
**Cooldown:** 1.5 seconds

- **Target Range:** 40 blocks; beam descends from **18 blocks above** target
- **Hit Radius:** 5 blocks (per step of the beam)
- **Hits:** **22 hits** as the beam descends step by step over 22 ticks
- **Damage:** **~10.5 damage per hit** (purification damage type).
- Places Light blocks along the beam path (removed after ~2 seconds).
- **Interactions:** Registers purification, light_source, light_weak at the target point.

---

### Holy Light
**Sequence Requirement:** 8
**Spirituality Cost:** 35
**Cooldown:** 1.25 seconds

- **Target Range:** 16 blocks; beam descends from **14 blocks above** target
- **Hit Radius:** 2.5 blocks (per step of the beam)
- **Hits:** **18 hits** as the beam descends step by step over 18 ticks
- **Damage:** **~7 damage per hit** (purification damage type).
- Places Light blocks along the beam path (removed after ~1 second).
- **Interactions:** Registers purification, light_source, light_weak at the target point.

---

### Illuminate
**Sequence Requirement:** 8
**Spirituality Cost:** 12
**Cooldown:** 0.25 seconds
*(Cannot be used by NPCs)*

- **Range:** 12 blocks
- Places a **Light block** at the targeted position lasting **25 seconds**.
- Spawns three perpendicular golden particle rings at the location for the duration.
- **Interactions:** Registers purification, light_source, light_weak at the block location for **20 seconds**.

---

### Holy Song
**Sequence Requirement:** 9
**Spirituality Cost:** 24
**Cooldown:** 20 seconds
*(Registers as a morale_boost interaction)*

- Immediately removes **Losing Control** from the caster and all allies within **10 blocks**.
- Applies to the caster for **20 seconds**:
  - **Strength:** Level 1
  - **Speed:** Level 2
  - **Resistance:** Level 1
- Plays music for the duration (audible within ~64 blocks).
- **Interactions:** Registers purification, light_weak, morale_boost.

---

## Passive Abilities

---

### Physical Enhancements (Sun)
**Sequence Requirement:** 9

No Night Vision (Sun pathway has no Night Vision in physical enhancements). Includes **Fire Resistance** at Seq 6 and below.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | +1       | —          | +1    | +4           | —            | — |
| 8        | +1       | —          | +1    | +5           | —            | — |
| 7        | +2       | +1         | +2    | +6           | +1           | — |
| 6        | +3       | +2         | +2    | +7           | +1           | Fire Resistance +1 |
| 5        | +3       | +3         | +2    | +9           | +2           | Fire Resistance +1 |
| 4        | +4       | +8         | +5    | +18          | +3           | Fire Resistance +2 |
| 3        | +4       | +9         | +5    | +19          | +3           | Fire Resistance +3 |
| 2        | +5       | +12        | +6    | +27          | +4           | Fire Resistance +3 |
| 1        | +5       | +13        | +6    | +32          | +4           | Fire Resistance +4 |
| 0        | +6       | +16        | +6    | +47          | +6           | Fire Resistance +6 |
