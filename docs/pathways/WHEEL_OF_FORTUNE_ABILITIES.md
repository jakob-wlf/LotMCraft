# Wheel of Fortune Pathway Abilities

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

## Luck and Unluck

The Wheel of Fortune pathway revolves around the **Luck** and **Unluck** custom effects. Both are persistent status effects with a level (amplifier) that scales their per-tick chance-based behaviors.

### Luck
Luck is a beneficial effect that provides several passive probabilistic bonuses every tick or event. The level (amplifier) determines how strong each chance is, scaling linearly up to an amplifier of 19 (level 20). All chances below use the effect's **amplifier** value (0-indexed, so level 1 = amplifier 0).

| Effect | Formula |
|--------|---------|
| **Dodge incoming damage** | `3.5% × (amplifier + 1)`, max 65% |
| **Critical hit on outgoing damage** (×1.75) | `4% × (amplifier + 1)`, min 5%, max 90% |
| **Enemy trips** — deals `2.5 × (amplifier + 1)` damage to the last entity that hit you (within 6s), with knockback | lerp amplifier 0→19: 0.2%→3.5% per tick |
| **Remove a random harmful effect** (excluding Losing Control and Unluck) | lerp amplifier 0→19: 0.25%→5% per tick |
| **Double mining drops** — duplicates a random drop twice when breaking blocks | `10% + 4.5% × (amplifier + 1)`, max 99% |
| **Bonus item drop when mining** — drops a random valuable item (gold ingots, emeralds, diamonds, nether stars, etc.) | `1% × amplifier + 1%`, max 20% |
| **Hero of the Village** effect (amplifier + 1) | Applied continuously if amplifier > 1 |

### Unluck
Unluck is a harmful effect that triggers various negative events per tick or per action. All chances scale with amplifier (0-indexed).

| Effect | Formula |
|--------|---------|
| **Incoming damage amplified** (×1.5 + 0.25 per amplifier level) | `6% × (amplifier + 1)`, max 85% per hit |
| **Outgoing damage reduced to 40%** (weak hit) | `7% × (amplifier + 1)`, max 90% per hit |
| **Mining drops destroyed** | `3% + 2% × amplifier`, max 40% per block |
| **Tool damaged by 5 × (amplifier + 1)** extra durability on mining | `8% + 4% × amplifier`, max 60% per block |
| **Random harmful effect applied** (Weakness, Hunger, Poison, Wither, Blindness, Slowness, or Mining Fatigue — level 1, 5 seconds) | lerp amplifier 0→19: 0.1%→4% per tick |
| **Trip and take damage** — `10 + 1.875 × amplifier` damage, knockback, 2s cooldown | lerp amplifier 0→19: 0.05%→1.5% per tick |
| **Hostile mob spawned** within 5 blocks (Zombie, Skeleton, Spider, or Creeper), 15s cooldown | lerp amplifier 0→19: 0.05%→0.8% per tick |
| **Random inventory item dropped** | lerp amplifier 0→19: 0.08%→1.2% per tick |
| **Slip** — velocity becomes erratic, 3s cooldown | lerp amplifier 0→19: 0.2%→2.5% per tick |
| **Beyonder multiplier reduced by 40–70%** for 3–6s, 8s cooldown (scales with amplifier) | lerp amplifier 0→19: 0.08%→1% per tick |
| **Beyonder abilities disabled** for 2–5s, 15s cooldown (scales with amplifier) | lerp amplifier 0→19: 0.03%→0.5% per tick |
| **Bad Omen** effect (amplifier − 1, capped at 5) | Applied continuously if amplifier > 1 |

---

## Active Abilities

---

### Cycle of Fate
**Sequence Requirement:** 1
**Spirituality Cost:** 2000
**Cooldown:** 1 second
*(Cannot be copied, replicated, stolen; cannot be used by NPCs)*

Two selectable modes:

**Mode 0 — Create Cycle**
- Spawns a **Cycle of Fate entity** at the caster's position.
- On creation, records a **40-block radius sphere** of the world: all blocks, block entities, and all living entities within range (positions, health, potion effects, inventories).
- Entities inside the cycle cannot leave its boundary — if they reach the edge they are pushed back.
- The cycle persists as long as the caster is alive, in the same dimension, and within **100 blocks** of it.
- Only one cycle can exist per caster. Two cycles cannot overlap.

**Mode 1 — Trigger Cycle**
- Restores the recorded world state:
  - All blocks and block entities in the 40-block radius are reset to their recorded state.
  - All tracked entities are restored to their recorded position, health, effects, and inventory.
  - Entities that were not present when the recording was made (newly spawned) are removed.
  - All active Marionette Controlling possessions within the area are cancelled.
- The cycle entity is consumed on trigger.

---

### Prophecy
**Sequence Requirement:** 2
**Spirituality Cost:** 1000
**Cooldown:** 4 seconds
*(Cannot be copied or replicated)*

Three selectable modes. All outcomes are **randomly chosen** each cast.

**Mode 0 — Manifest Disaster** (1 of 4 random outcomes)
- **Meteor** — **Target Range:** 85 blocks. **~45.7 × multiplier damage**, explosion radius 20, knockback 34.
- **Lightning** — **Target Range:** 70 blocks. Giant lightning strike with 35 branches. **~44.3 × multiplier damage**.
- **Tornados** — 1 main + 5 additional tornados in a ±20 block area. Targeted: **32.5 × multiplier**. Others: **~43.3 × multiplier**.
- **Tsunami** — **Target Range:** 40 blocks. **~43.3 × multiplier damage**.

**Mode 1 — Manifest Fortune** (1 of 2 random outcomes)
- Rains **50 random valuable items** in a 12-block area around the caster (emeralds, diamonds, gold, iron blocks, ancient debris, ender pearls, etc.).
- Or: applies **Luck (Level 27, 20 seconds)** to the caster.

**Mode 2 — Manifest Misfortune for Enemy** *(Target Range: 40 blocks)*
(1 of 3 random outcomes)
- Applies **Weakness V, Blindness V, Nausea V, Slowness V, Wither VII, Poison VII, Hunger VII**, and a **0.6× damage modifier** for **40 seconds**.
- Or: applies **Unluck (Level 21, 40 seconds)** to the target.
- Or: teleports the target to a random position up to 30 blocks away horizontally and up to 60 blocks upward.

---

### Words of Misfortune
**Sequence Requirement:** 2
**Spirituality Cost:** 1000
**Cooldown:** 4 seconds
*(Cannot be copied; cannot be used by NPCs)*

- Spawns a **Misfortune Words entity** at 1 block above the caster's position.
- The entity applies **Unluck (Level 13, 5 minutes)** to all enemies that come near it.
- Accumulates an "affected count" (+10 per player, +1 per mob) and **self-destructs** when the total reaches **30**.
- Immune to Wheel of Fortune Beyonders of Sequence 2 or stronger.
- Casting again within 15 blocks removes the existing entity.

---

### Spiritual Baptism
**Sequence Requirement:** 3
**Spirituality Cost:** 900
**Cooldown:** 5 seconds
*(Cannot be copied or replicated; registers as a cleansing interaction)*

Two selectable modes:

**Mode 0 — On Self**
- Applies the baptism to the caster.

**Mode 1 — On Target**
- **Range:** 20 blocks (allies only; falls back to self if no ally found)
- Applies the baptism to the target.

**Baptism Effect:**
- Instantly heals a large amount of health (Instant Health Level 41).
- Removes all **harmful potion effects**.
- Extinguishes fire.
- Restores food to **20 (full)** and saturation to **20** (for players).
- Restores **15% sanity**.

---

### Blessing
**Sequence Requirement:** 4
**Spirituality Cost:** 750
**Cooldown:** 4 seconds
*(Cannot be copied; cannot be used by NPCs; registers as a cleansing interaction)*

- **Range:** 20 blocks (allies only)
- Applies **Luck** to the target for **17 minutes**.
- Luck level scales with caster's multiplier: `round(multiplier × 6.25)`.

---

### Misfortune Field
**Sequence Requirement:** 4
**Spirituality Cost:** 600
**Cooldown:** 30 seconds
*(Cannot be copied)*

- **Radius:** 20 blocks
- **Duration:** 20 seconds
- **Effect Interval:** Every 2 ticks
- Applies **Unluck** to all nearby entities each interval. Unluck level scales with caster's multiplier: `round(multiplier × 3)`.

---

### Luck Perception
**Sequence Requirement:** 5
**Spirituality Cost:** 0
**Cooldown:** 0 seconds (toggle)
*(Cannot be copied; cannot be used by NPCs; cannot be shared)*

- While active, displays the **Luck value** of the looked-at entity (within **20 blocks**) in the action bar every 10 ticks.
- If no entity is looked at, shows the caster's own Luck value instead.
- Blocked if the target is a Wheel of Fortune Beyonder of lower sequence than the caster, or significantly stronger.

---

### Luck Release
**Sequence Requirement:** 5
**Spirituality Cost:** 100
**Cooldown:** 2 minutes
*(Cannot be replicated or used in artifacts)*

- Consumes all accumulated luck (from the Luck Accumulation passive) and converts it to a powerful **Luck** effect on the caster.
- Additional luck level gained scales with accumulated ticks: **1 level per 2 minutes of accumulation**, capped at **+8 levels**.
- The Luck effect's duration scales with the final level: `20 × 35 × (level / 2)` ticks.
- Stacks on top of any existing Luck the caster already has.

---

### Misfortune Gifting
**Sequence Requirement:** 5
**Spirituality Cost:** 120
**Cooldown:** 5 seconds
*(Cannot be copied)*

- **Range:** 20 blocks
- Applies **Unluck** to the target for **20 seconds**.
- Unluck level scales with caster's multiplier: `round(multiplier × 2.5)`.

---

### Calamity Attraction
**Sequence Requirement:** 6
**Spirituality Cost:** 190
**Cooldown:** 10 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 14 blocks
- After a **1.5–3 second** delay, summons a **random calamity** at the targeted location:
  - **Tornado** — damage **16 × multiplier**.
  - **Earthquake** — **~9.5 × multiplier damage** every 8 ticks for **15 seconds**, radius 34. Launches falling blocks. (Requires griefing for block interaction.)
  - **Meteor** — damage **15 × multiplier**, explosion radius 7, knockback 12.

---

### Psyche Storm
**Sequence Requirement:** 6
**Spirituality Cost:** 80
**Cooldown:** 7 seconds

- **Radius:** 10 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~13.3 damage × caster multiplier**.
- Applies **Losing Control** to all hit entities for **7 seconds**. Level scales with sequence difference:
  - Significantly weaker target: **Level 7**
  - Weaker target (same category): **Level 3**
  - Equal target: **Level 3–5** (random)
  - Stronger target: **Level 2**

---

## Passive Abilities

---

### Physical Enhancements (Wheel of Fortune)
**Sequence Requirement:** 9

Includes **Fire Resistance** at Seq 6 and below. No Night Vision at any sequence. Luck is provided separately by the **Passive Luck** ability (Seq 7).

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | —        | —          | +1    | —            | —            | — |
| 8        | +1       | —          | +2    | +5           | —            | — |
| 7        | +2       | —          | +2    | +6           | +1           | — |
| 6        | +2       | +1         | +2    | +7           | +2           | Fire Resistance +1 |
| 5        | +2       | +2         | +2    | +9           | +2           | Fire Resistance +2 |
| 4        | +3       | +7         | +4    | +16          | +3           | Fire Resistance +2 |
| 3        | +3       | +8         | +4    | +17          | +3           | Fire Resistance +3 |
| 2        | +4       | +11        | +5    | +25          | +4           | Fire Resistance +3 |
| 1        | +4       | +12        | +5    | +30          | +4           | Fire Resistance +4 |
| 0        | +6       | +15        | +6    | +45          | +6           | Fire Resistance +6 |

---

### Luck Accumulation
**Sequence Requirement:** 5

- **Passive:** Continuously accumulates **luck** over time at **5 ticks per server tick** (5× real-time rate).
- Accumulated ticks are consumed by **Luck Release** to determine the bonus Luck level.

---

### Passive Luck
**Sequence Requirement:** 7

- **Passive:** Continuously applies a **Luck** effect to the caster. Level scales with sequence:

| Sequence | Luck Level |
|----------|-----------|
| 9–8      | —         |
| 7        | +3        |
| 6        | +5        |
| 5        | +6        |
| 4        | +10       |
| 3        | +13       |
| 2        | +17       |
| 1        | +20       |

---

### Passive Calamity Attraction
**Sequence Requirement:** 6

- **Passive:** Every **20–90 seconds**, a random calamity automatically spawns near the caster (within 6 blocks):
  - **Tornado** — damage **16 × multiplier**.
  - **Earthquake** — **~9.5 × multiplier damage** for 15 seconds, radius 34.
  - **Meteor** — damage **15 × multiplier**, explosion radius 7.
- When a calamity is **12 seconds** away from spawning, a warning is shown in the action bar.
