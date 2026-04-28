# Darkness Pathway Abilities

## Spirituality

Spirituality regenerates at **0.06% of max per tick** (1.2% per second) passively.

| Sequence | Max Spirituality | Regen/sec |
|----------|-----------------|-----------|
| 9        | 630             | 7.6/s     |
| 8        | 700             | 8.4/s     |
| 7        | 2,730           | 32.8/s    |
| 6        | 4,200           | 50.4/s    |
| 5        | 6,650           | 79.8/s    |
| 4        | 13,650          | 163.8/s   |
| 3        | 17,500          | 210.0/s   |
| 2        | 35,000          | 420.0/s   |
| 1        | 70,000          | 840.0/s   |
| 0        | 210,000         | 2,520.0/s |

---

## Active Abilities

---

### Sword of Darkness
**Sequence Requirement:** 1
**Spirituality Cost:** 3000
**Cooldown:** 8 seconds

- **Range:** 60 blocks
- **Mechanics:** Launches an offset slash attack from the caster's eyes toward the target location. The slash travels along a path offset 8 blocks perpendicularly, moving 0.5 blocks per iteration at 3 iterations per tick.
- **Damage:** Hits entities within a **3-block radius** of each point along the slash path. Deals **~64 damage** per hit, applied every tick for 6 seconds (up to **120 hits** on a stationary target in the slash path).
- **Blindness:** Applies **Blindness (Level 0, 1 second)** to all entities within **25 blocks** of the impact area.
- **Purification Interaction:** Damage reduced to **0.3×** if target is purified.
- **Block Destruction:** Can destroy blocks if server griefing is enabled.

---

### Identity Concealment
**Sequence Requirement:** 2
**Spirituality Cost:** 3000
**Cooldown:** 5 seconds
*(Cannot be copied, replicated, stolen, or used in artifacts)*

One selectable mode.

**Mode 0 — Conceal Identity**
- **Range:** 16 blocks (targets a nearby player; falls back to self if none found).
- Toggles the target's **nametag visibility** — hides them from the tab list and removes their nametag from all other players' clients.
- Using the ability again on a hidden player **reveals** them again.

---

### Concealment
**Sequence Requirement:** 2  
**Spirituality Cost:** 6000  
**Cooldown:** 5 seconds  
*(Cannot be copied)*

Two selectable modes:

**Mode 0 — Conceal Surroundings**
- Moves blocks into a concealment dimension in an expanding sphere, starting at radius **2 blocks** and growing by **0.5 blocks** every 20 ticks.
- Each expansion phase lasts **5 seconds (100 ticks)**; effects are applied every **2 ticks**.
- Entities caught inside are teleported to the concealment world:
  - Weaker target: **120 seconds**
  - Equal target: **25 seconds**
  - Significantly stronger target: **1.75 seconds**

**Mode 1 — Enter Concealed Area**
- Toggles the caster between the overworld and the concealment dimension.
- Searches for a safe landing position (solid ground below, air above).

---

### Surge of Darkness
**Sequence Requirement:** 3  
**Spirituality Cost:** 3000
**Cooldown:** 11 seconds
**Duration:** 15 seconds (300 ticks)  
**Effect Interval:** Every 4 ticks

- **Radius:** 45 × 18 blocks (ellipsoid)
- **Blindness:** Applies **Blindness** to all entities in range:
  - Normal: **Level 5, 10 seconds**
  - Weakened (light interaction): **Level 1, 10 seconds**
- **Sanity Drain:** −0.0025 per tick to affected entities.
- **Damage:** **~4 damage every 4 ticks** — **75 hits** over the full duration (~20 base DPS).
- **Block Darkening:** Darkens blocks in expanding waves; each block reverts after **10 seconds**.
- **Purification Interaction:** Damage and blindness are reduced when weakened by light_strong.

---

### Horror Aura
**Sequence Requirement:** 3  
**Spirituality Cost:** 2500
**Cooldown:** 30 seconds
**Duration:** 30 seconds (600 ticks)  
**Effect Interval:** Every tick

- **Radius:** 20 blocks
- **Effects applied each tick:**
  - **Darkness:** Level 5, 1.5 seconds
  - **Blindness:** Level 4, 1 second
  - **Slowness:** Level 4, 1 second
- **Beyonder Modifier:** −40% to affected Beyonders.
- **Sanity Drain:** −0.0033 per tick.
- **Damage (vs. weaker targets only):** **~13 damage every 10 ticks** — **60 hits** over the full duration (~26 base DPS).
- **Purification Interaction:** Fully suppressed against purified targets.
- **Morale Override:** Overridden by morale-boosting abilities on targets.

---

### Night Domain
**Sequence Requirement:** 4  
**Spirituality Cost:** 1800
**Cooldown:** 30 seconds
**Duration:** 25 seconds (500 ticks)  
**Effect Interval:** Every 2 ticks

- **Radius:** 35 blocks
- **Effects on enemies (if not purified):**
  - **Blindness:** Level 20, 1 second
  - **Darkness:** Level 20, 1 second
  - **Unluck:** Level 4 (Level 1 if purified), 1 second
  - **Slowness:** Level 5 (Level 1 if purified), 1 second
  - **Damage:** **~2.3 damage every 2 ticks** — **250 hits** over the full duration (~22.6 base DPS). Scales with the caster's sequence multiplier (×1.0 at Seq 9 → ×4.25 at Seq 1)
  - **Debuff Modifier:** −35% applied for 2 seconds
- **Self-buffs:**
  - **Speed:** Level 2, 2 seconds
  - **Buff Modifier:** +35% for 2 seconds
- **Particle Count:** 30–80 particles per tick (30 if purified).
- **Light_strong Interaction:** Completely cancelled if the opposing Beyonder is 1+ sequences higher and uses light_strong.

---

### Hair Entanglement
**Sequence Requirement:** 4  
**Spirituality Cost:** 1200
**Cooldown:** 3 seconds

- **Targeting Range:** 16 blocks; particle animation spans up to **35 blocks**.
- **Effect Interval:** Every 5 ticks for the duration.
- **Duration:**
  - Significantly stronger target: **1.75 seconds**
  - Equal target: **60 seconds**
  - Significantly weaker target: **90 seconds**
- **Effects on target:**
  - **Slowness:** Level 10, 1 second
  - **Weakness:** Level 10, 1 second
  - **Mining Fatigue:** Level 10, 1 second
  - Zeroes the target's velocity every interval.
  - Disables mob AI.
  - Disables Beyonder abilities for the duration.
- **Visuals:** 8 animated particle lines traveling toward the target.

---

### Spirit Commanding
**Sequence Requirement:** 5  
**Spirituality Cost:** 400  
**Status:** *Not yet implemented.*

---

### Requiem
**Sequence Requirement:** 6  
**Spirituality Cost:** 45
**Cooldown:** 3 seconds

- **Range:** 16 blocks
- **Effect Interval:** Every 5 ticks for the duration.
- **Duration:**
  - Significantly stronger target: **1.75 seconds**
  - Equal target: **15 seconds**
  - Significantly weaker target: **65 seconds**
- **Effects on target:**
  - **Slowness:** Level 10, 1 second
  - **Weakness:** Level 10, 1 second
  - **Mining Fatigue:** Level 10, 1 second
  - Zeroes the target's velocity every interval.
  - Disables mob AI and Beyonder abilities.
- **Visuals:** 8 animated particle lines + spiral particles around the target.

---

### Nightmare
**Sequence Requirement:** 7  
**Spirituality Cost:** 0  
**Cooldown:** 0.15 seconds  
*(Cannot be copied, used by NPCs, or replicated)*

Five selectable modes:

**Mode 0 — Create Nightmare**
- Creates a persistent **40-block radius** nightmare domain centered on the caster.
- Spawns **50 particles** every 5 ticks throughout the domain.
- Active until the caster moves more than 40 blocks away or changes dimension.
- Only one nightmare can be active at a time.

**Mode 1 — Reshape**
- **Range:** 35 blocks; affects blocks in a **3-block sphere** at the target location.
- Only affects passable blocks within the nightmare domain.
- Captures replaced blocks for restoration when the nightmare ends.

**Mode 2 — Restrict**
- **Range:** 20 blocks (must be within nightmare)
- **Duration:** 20 seconds (400 ticks)
- **Effect Interval:** Every 2 ticks
- Applies **Slowness Level 10** for 0.5 seconds per interval.
- Spawns particle spirals and line effects on the target.

**Mode 3 — Attack**
- **Range:** 20 blocks (must be within nightmare)
- Fires a projectile at **0.6 blocks/tick**.
- **Damage:** **~13 damage per hit** — **1 hit** per cast.

**Mode 4 — Teleport**
- **Range:** 8 blocks (must be within nightmare)
- Instantly teleports the caster to the targeted block.
- Spawns 30 particles on arrival.

---

### Midnight Poem
**Sequence Requirement:** 8  
**Spirituality Cost:** 40  
**Cooldown:** 4 seconds

Two selectable modes:

**Mode 0 — Lullaby**
- **Range:** 35 blocks
- **Duration:**
  - Significantly stronger target: **1.75 seconds**
  - Equal target: **5 × multiplier seconds**
  - Significantly weaker target: **25 seconds**
- Applies the **ASLEEP** effect (Level 1) and zeroes velocity every **3 ticks**.
- **Visuals:** 800 particles at the caster's eyes + 500 crimson leaf particles.

**Mode 1 — Wilt**
- **Range:** 20 blocks
- **Damage:** **~9.4 damage per hit** — **1 hit** per cast.
- **Light Interaction:** Damage reduced to **0.4×** if weakened by a light_source.
- **Visuals:** 800 particles at eyes + 500 crimson leaf particles.

---

## Passive Abilities

---

### Physical Enhancements (Darkness)
**Sequence Requirement:** 9

Includes **Night Vision** at all sequences. No Fire Resistance at any sequence.

**Night/Domain Bonus** (active during in-game night OR while inside a Concealed Domain): **+2 Strength, +2 Resistance, +2 Speed** on top of all values below.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration |
|----------|----------|------------|-------|--------------|--------------|
| 9        | —        | —          | —     | —            | —            |
| 8        | +1       | —          | +1    | +5           | +2           |
| 7        | +2       | —          | +2    | +6           | +2           |
| 6        | +2       | +1         | +2    | +7           | +2           |
| 5        | +2       | +2         | +2    | +9           | +2           |
| 4        | +3       | +7         | +4    | +16          | +3           |
| 3        | +3       | +8         | +4    | +17          | +3           |
| 2        | +4       | +11        | +5    | +25          | +4           |
| 1        | +5       | +13        | +5    | +32          | +4           |
| 0        | +6       | +15        | +6    | +45          | +6           |

---

### Nocturnality
**Sequence Requirement:** 9  
**Status:** *Not yet implemented.*
