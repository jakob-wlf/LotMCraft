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
- **Damage:** Hits entities within a **3-block radius** of each point along the slash path. Deals **~`DamageLookup(seq, 1.2)` × max(multiplier/2,1)** damage per hit, applied every tick for **6 seconds** (up to **120 hits** on a stationary target in the slash path).
- **Purification Interaction:** Damage reduced to **0.5×** if target is purified.
- **On-hit effects** applied to entities within `20 × max(multiplier/2,1)` blocks of the caster each tick:
  - **Asleep** (Level 1)
  - **Darkness** (Level 5)
  - **Blindness** (Level 4)
  - Velocity zeroed.
  - **Sanity Drain:** −0.000163 × max(multiplier/4,1) per tick.
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

### Concealed Domain
**Sequence Requirement:** 2  
**Spirituality Cost:** 5,500  
**Cooldown:** 2 seconds  
*(Cannot be copied or used by NPCs)*

Summon a spherical concealed domain with a **30-block radius** centered on the caster's position. The domain hollows out a sphere of blocks, replacing them with concrete walls (1 block thick shell). Only the owner and their allies are allowed inside — non-allowed entities are physically blocked by the shell.

- **Night Vision:** Granted to all allowed entities inside every 3 ticks.
- **Top exit point:** Any entity near the top of the sphere (~27 blocks above center) is ejected outward.
- **Owner limit:** Discards if the owner moves more than **50 blocks** from the domain center, leaves the dimension, or dies.
- **Toggle:** Re-casting while within 60 blocks of an active domain removes it and restores all displaced blocks.
- **Interaction:** Destroyed by a `destruction` interaction from a stronger Beyonder.

---

### Surge of Darkness
**Sequence Requirement:** 3  
**Spirituality Cost:** 3000
**Cooldown:** 11 seconds
**Duration:** 15 seconds (300 ticks)  
**Effect Interval:** Every 4 ticks

- **Radius:** 45 blocks flat
- **Sanity Drain:** −0.0525 × max(multiplier/2,1) per interval to affected entities.
- **Damage:** `DamageLookup(3, 0.5)` DPS × multiplier — applied every 4 ticks.
- **Purification Interaction:** Damage reduced to **0.3×**.
- **Block Darkening:** Darkens blocks in expanding waves; each block reverts after **10 seconds**.

---

### Horror Aura
**Sequence Requirement:** 3  
**Spirituality Cost:** 2500
**Cooldown:** 30 seconds
**Duration:** `15 × max(multiplier/3,1)` seconds  
**Effect Interval:** Every tick

- **Radius:** `20 × max(multiplier/2,1)` blocks
- **Effects applied each tick:**
  - **Darkness:** Level 5, 3 seconds
  - **Blindness:** Level 4, 3 seconds
  - **Slowness:** Level 4, 3 seconds
- **Sanity Drain:** −0.02168 × max(multiplier/4,1) per tick.
- **Damage (vs. significantly weaker targets only):** `DamageLookup(3, 0.95)` DPS × max(multiplier/4,1) — every **10 ticks**.
- **Purification Interaction:** Fully suppressed against purified targets.

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
  - **Asleep** (Level 10, 2 seconds, refreshed every interval)
  - **Weakness** (Level 10, 2 seconds)
  - **Mining Fatigue** (Level 10, 2 seconds)
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
  - **Asleep** (Level 10, 2 seconds, refreshed every interval)
  - **Weakness** (Level 10, 2 seconds)
  - **Mining Fatigue** (Level 10, 2 seconds)
  - **Blindness** (Level 4, 3 seconds)
  - **Darkness** (Level 5, 3 seconds)
  - Zeroes the target's velocity every interval.
  - Disables mob AI and Beyonder abilities.
- **Visuals:** 8 animated particle lines + spiral particles around the target.

---

### Nightmare
**Sequence Requirement:** 7  
**Spirituality Cost:** 0  
**Cooldown:** 1 second  
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
- **Range:** `20 × max(multiplier/2,1)` blocks (must be within nightmare)
- **Duration:** `20 × max(multiplier/2,1)` seconds
- **Effect Interval:** Every 2 ticks
- Applies **Asleep (Level 10)** for 2 seconds per interval — complete immobility.
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

Five selectable modes:

**Mode 0 — Lullaby**
- **Radius:** `35 × max(multiplier/20,1)` blocks (≈35 at Seq 8).
- Applies **Asleep (Level 1)** and zeroes velocity every 2 ticks for the duration.
- Also applies **Darkness (Level 5)** and **Blindness (Level 4)** for the duration.
- **Duration:**
  - Significantly stronger target: `35 × max(multiplier/2,1)` ticks
  - Equal target: `20 × 5 × max(multiplier/2,1)` ticks
  - Significantly weaker target: **25 seconds**

**Mode 1 — Wilt**
- **Radius:** `20 × multiplier` blocks.
- **Damage:** `DamageLookup(8, 1.1) × multiplier` per hit — **1 hit** per cast.
- **Light_source Interaction:** Damage reduced to **0.4×**.

**Mode 2 — Agitate**
- **Radius:** `10 × max(multiplier/10,1)` blocks.
- Applies a **0.6× damage multiplier** debuff and **Asleep + Darkness + Blindness** to all enemies in range.
- Duration scales inversely with the target's multiplier relative to the caster's.

**Mode 3 — Console**
- **Radius:** `10 × max(multiplier/2,1)` blocks (allies only).
- Restores **15% sanity** to the caster and nearby allies.
- Applies a healing song effect (Regeneration scaled with multiplier, up to amplifier 3000) to allies.

**Mode 4 — Pacify**
- **Radius:** `10 × max(multiplier/10,1)` blocks.
- Disables AI of non-Beyonder mobs and disables Beyonder abilities for the duration.
- Applies **Blindness (Level 4), Darkness (Level 4), Asleep (Level 4)** for the duration.
- Duration scales inversely with the target's multiplier relative to the caster's. Halved if a purification interaction is active.

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
