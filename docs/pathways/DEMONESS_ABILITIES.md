# Demoness Pathway Abilities

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

### Apocalypse
**Sequence Requirement:** 1
**Spirituality Cost:** 2500
*(Cannot be copied)*

- **Radius:** Expanding sphere — starts at 2 blocks, grows by 0.8 blocks every 2 ticks.
- **Effect Interval:** Every 2 ticks
- **Hits:** **55 hits** total over the duration
- **Damage:** **~54.9 damage per hit**.
- **Block Destruction:** Blocks below the caster are replaced with Obsidian; blocks above are cleared (if griefing is enabled).

---

### Disaster Manifestation
**Sequence Requirement:** 2
**Spirituality Cost:** 1200
*(Cannot be copied)*

Three selectable modes:

**Mode 0 — Meteor**
- **Range:** 85 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~45.7 damage per hit**.
- **Explosion Radius:** 18 blocks with 2.5× knockback; destroys blocks if griefing is enabled.

**Mode 1 — Ice Age**
- **Radius:** Expanding sphere — starts at 0.5 blocks, grows by 0.5 blocks every tick.
- **Effect Interval:** Every tick
- **Hits:** **110 hits** over the full duration
- **Damage:** **~42 damage per hit** (in an expanding ring).
- **Block Transformation:** Converts blocks to Packed Ice if griefing is enabled.
- Applies a blizzard fog effect to nearby players.

**Mode 2 — Tornados**
- **Range:** 12 blocks
- Spawns **1 main tornado** + **30 additional tornadoes** across a ±60 block area.
- **Tornado damage:** **~39.6 damage** each.

---

### Structural Collapse
**Sequence Requirement:** 2
**Spirituality Cost:** 1200
**Cooldown:** 15 seconds
*(Cannot be copied)*

- **Target Range:** 20 blocks; **Damage Radius:** 35 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~43.8 damage per hit**.
- **Block Destruction:** Collapses unsupported blocks in a 40×23 ellipsoid at the target, making them fall level by level (if griefing is enabled).

---

### Petrification
**Sequence Requirement:** 3
**Spirituality Cost:** 500
**Cooldown:** 60 seconds
*(Cannot be copied)*

Two selectable modes:

**Mode 0 — Single Target**
- **Range:** 15 blocks
- **Duration:**
  - Significantly stronger target: **2 seconds**
  - Equal target: **2 minutes**
  - Significantly weaker target: **10 minutes**
- Applies the **PETRIFICATION** effect (see below).
- **Block Transformation:** Converts blocks in a 2-block radius at the target to Stone if griefing is enabled.

**Mode 1 — Area**
- **Requires griefing to be enabled.** If disabled, the cast is cancelled with an error message.
- **Radius:** Expanding sphere — starts at 0.5 blocks, grows by 0.5 blocks every tick for 6 seconds.
- **Duration:**
  - Significantly stronger target: **1 second**
  - Equal target: **45 seconds**
  - Significantly weaker target: **10 minutes**
- Applies the **PETRIFICATION** effect to all entities in the expanding radius.
- Converts all blocks in the expanding sphere to Stone.

**Petrification Status** (every tick while active):
- Zeroes the target's velocity — complete inability to move.
- Disables Beyonder abilities for 1 second, refreshed every tick (effectively permanent for the duration).
- Disables AI of non-Beyonder mobs.
- **Blocks all incoming damage** entirely (movement speed attribute also set to −10 for client sync purposes).
- Cancels player item use (right-click).
- The effect is stored server-side and re-synced to nearby clients every 10 ticks to prevent client desync.

---

### Plague
**Sequence Requirement:** 4
**Spirituality Cost:** 1200
**Cooldown:** 180 seconds

- **Radius:** `45 × max(multiplier/4,1)` blocks
- **Duration:** `40 × max(multiplier/4,1)` seconds
- **Hit Interval:** Every second
- **Damage:** `DamageLookup(4, 0.3)` DPS × max(multiplier/6,1) per hit.
- Applies every hit to all nearby entities:
  - **Wither:** Level 4, 1 second
  - **Blindness:** Level 5, 1 second
  - **Slowness:** Level 3, 1 second
- **Purification / Cleansing Interaction:** Cancels damage and effects.
- **Blooming Interaction:** Damage reduced to 0.4×.

---

### Curse
**Sequence Requirement:** 4
**Spirituality Cost:** 300
**Cooldown:** 80 seconds
*(Cannot be copied, replicated, or used by NPCs)*

- **Requires a Blood item in the off-hand** belonging to the target.
- **Backfire:** If the target is significantly stronger, the caster takes **10 damage** and **Losing Control (Level 3, 5 seconds)**.
- **Duration:** 2 minutes (ticks every 8 ticks).
- Each interval has a 1-in-3 chance of one of the following:
  - **Damage:** `DamageLookup(4, 0.6) × max(multiplier/5,1)`.
  - **Slowness (Level 3, 2 seconds)** + **Blindness (Level 3, 2 seconds)**.
  - *(Nothing on the third outcome.)*
- **Cleansing Interaction:** Cancels the curse.

---

### Mirror World Traversal
**Sequence Requirement:** 4
**Spirituality Cost:** 200
**Cooldown:** 2 seconds
*(Cannot be copied, replicated, used by NPCs, or used in artifacts)*

- Requires a glass block or pane within **20 blocks**.
- Puts the caster into **spectator mode**, placing them inside the nearest glass block.
- While in the mirror world, the caster is locked within **35 blocks** of their entry point.
- Exits automatically when the caster moves within **4 blocks** of any glass block (teleports them to it) or if spectator mode is removed.

---

### Disease
**Sequence Requirement:** 5
**Spirituality Cost:** 600
**Cooldown:** 120 seconds

- **Radius:** `20 × max(multiplier/4,1)` blocks
- **Duration:** `40 × max(multiplier/4,1)` seconds
- **Hit Interval:** Every second
- **Damage:** `DamageLookup(5, 0.2)` DPS × max(multiplier/6,1) per hit.
- Applies **Poison (Level 1, 1 second)** every hit to all nearby entities.
- **Purification / Cleansing Interaction:** Cancels damage and effects.
- **Blooming Interaction:** Damage reduced to 0.4×.

---

### Thread Manipulation
**Sequence Requirement:** 6
**Spirituality Cost:** 45
**Cooldown:** 1.5 seconds

Three selectable modes:

**Mode 0 — Binding**
- **Range:** 16 blocks
- **Duration:** `20 × max(multiplier/4,1)` seconds
- Pins the target completely — zeroes velocity every 5 ticks and applies:
  - **Asleep:** Level 10, 1 second (refreshed every 5 ticks)
  - **Weakness:** Level 10, 1 second
  - **Mining Fatigue:** Level 10, 1 second
- Disables AI of non-Beyonder mobs for the duration.
- **Burning Interaction:** Burns and removes the threads immediately.
- **Blink_escape Interaction:** Breaks the binding.
- Animated teal particle lines travel from the caster to the target.

**Mode 1 — Cocoon**
- Toggles a cocoon on/off for the **caster**.
- While in the cocoon (up to 20 seconds):
  - **Slowness:** Level 10, 1 second (refreshed every tick)
  - **Resistance:** Level 2, 1 second (continuous)
  - **Regeneration:** Level 2, 6 seconds (continuous)
  - Velocity zeroed every tick.
- **Burning Interaction:** Destroys the cocoon immediately.

**Mode 2 — Shoot**
- **Range:** 10 blocks (projectile)
- **Hits:** **1 hit** on contact
- **Damage:** **~11.6 damage per hit**.
- Applies **Slowness (Level 5, 8 seconds)** on hit.

---

### Charm
**Sequence Requirement:** 6
**Spirituality Cost:** 40
**Cooldown:** 2 seconds
*(Cannot be copied or replicated)*

- **Range:** 18 blocks
- **Duration:** 15 seconds (refreshed every 5 ticks with heart particles + **Slowness Level 3**).
- Charms the target — while charmed, if the target attempts to attack the caster, the hit is cancelled and the charm breaks.
- Only one charm can be active per target at a time (30-second cooldown after charm ends).
- **Battle Hypnosis Interaction:** Charm overrides Battle Hypnosis if the caster is equal or stronger sequence.
- Overrides Instigation, suppresses Losing Control, can break puppet soldier loyalty.
- **Marionette Interaction:** If the target is a marionette, charm kills it if the caster is at least one sequence stronger than the controller.

---

### Invisibility
**Sequence Requirement:** 7
**Spirituality Cost:** 13
**Cooldown:** 180 seconds
*(Cannot be copied)*

- Makes the caster **invisible** (Invisibility Level 20) and prevents mobs from targeting them for `60 × max(multiplier/4, 1)` seconds.
- A **light_strong** interaction (from a stronger Beyonder) can reveal the caster and cancel the effect early.
- Spirit Vision, Spectating, or Cull abilities can see through this invisibility.

---

### Black Flame
**Sequence Requirement:** 7
**Spirituality Cost:** 30
**Cooldown:** 1 second
*(Registers as soul_burn and burning interactions)*

Three selectable modes:

**Mode 0 — Burn**
- **Range:** 10 blocks (at target position)
- **Hit Radius:** 2.5 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **`DamageLookup(7, 0.7)` × max(multiplier/4,1)**.
- Places a temporary light block at the target position (removed after ~1.25 seconds).
- **Soul Burn / Burning Interaction:** Triggered on cast.

**Mode 1 — Shoot**
- **Range:** Travels until contact (up to long range)
- **Hit Radius:** 2.5 blocks
- **Hits:** **1 hit** on contact
- **Damage:** **`DamageLookup(7, 1.1)` × max(multiplier/4,1)**.
- On block contact: places fire if griefing is enabled.
- **Soul Burn / Burning Interaction:** Triggered on contact.

**Mode 2 — Expel**
- Launches an expanding ring of black flame upward from the caster.
- **Radius:** Expanding outward over 4 seconds (40 hits at 2-tick intervals).
- **Hits:** **40 hits** over the full duration
- **Damage:** **`DamageLookup(7, 0.8)` × max(multiplier/4,1)** (in ring zone).
- **Soul Burn / Burning Interaction:** Triggered on cast.

---

### Frost
**Sequence Requirement:** 7
**Spirituality Cost:** 30
**Cooldown:** 1 second
*(Registers as freezing interaction)*

Three selectable modes:

**Mode 0 — Shoot**
- **Range:** Travels until contact
- **Hit Radius:** 2.5 blocks
- **Hits:** **1 hit** on contact
- **Damage:** **~10.3 damage per hit**.
- Applies **Slowness (Level 6, 3 seconds)** on hit.
- On block contact: converts block to Packed Ice if griefing is enabled.

**Mode 1 — Frost Spear**
- **Range:** 50 blocks
- **Hit Radius:** 2.5 blocks (projectile entity)
- **Hits:** **1 hit** on contact
- **Damage:** **~10.5 damage per hit**.
- **Projectile Speed:** 1.6 blocks/tick.
- On block contact: converts block to Packed Ice if griefing is enabled.

**Mode 2 — Freeze Area**
- **Radius:** Expanding outward over 3 seconds (30 hits every 2 ticks)
- **Hit Zone:** Ring at current radius (±0.4 blocks)
- **Hits:** **30 hits** over the full duration
- **Damage:** **`DamageLookup(7, 0.8)` × max(multiplier/4,1)** (in expanding ring).
- Applies **Slowness (Level 10, 5 seconds)** to all nearby entities within the full radius.
- Creates Packed Ice rings at the current radius (3 Y-levels) if griefing is enabled.
- **Freezing Interaction:** Triggered on cast.

---

### Mirror Substitute
**Sequence Requirement:** 7
**Spirituality Cost:** 20
**Cooldown:** 10 seconds
*(Cannot be copied, replicated, or used in artifacts)*

- Creates a **mirror figurine** (item given to the caster). Up to **5 figurines** can be held at a time.
- When the caster takes damage (except Losing Control damage), one figurine is **consumed**:
  - The damage is **completely cancelled**.
  - The caster is **teleported** up to 7 blocks away randomly.
- Each figurine is a single-use damage negation.

---

### Instigation
**Sequence Requirement:** 8
**Spirituality Cost:** 40
**Cooldown:** 1 second
*(Cannot be copied, used by NPCs)*

- **Range:** 20 blocks
- Used twice in sequence — first use selects a target, second use selects another target.
- Makes the two selected mobs attack each other. Sets their attack damage to 5 if they have no attack attribute, and adds melee AI if absent.
- Cannot instigate a mob against itself; previous target must still be alive.

---

### Shadow Concealment
**Sequence Requirement:** 9
**Spirituality Cost:** 13
**Cooldown:** 45 seconds

- Makes the caster **invisible** (Invisibility Level 20) and prevents mobs from targeting them for **20 seconds**.
- A **light_source** interaction (any light source nearby) can reveal the caster and cancel the effect early.
- Spirit Vision, Spectating, or Cull abilities can see through this concealment.
- Displays shadow particle effects on the client.

---

### Mighty Blow
**Sequence Requirement:** 9
**Spirituality Cost:** 15
**Cooldown:** 2 seconds

- **Range:** Melee (optimal distance: 1.5 blocks)
- **Hit Radius:** 3.5 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~7.2 damage per hit**.

---

## Passive Abilities

---

### Physical Enhancements (Demoness)
**Sequence Requirement:** 9

Includes **Night Vision** at all sequences. No Fire Resistance at any sequence.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration |
|----------|----------|------------|-------|--------------|--------------|
| 9        | +1       | —          | +1    | —            | —            |
| 8        | +1       | —          | +2    | +5           | —            |
| 7        | +1       | —          | +2    | +5           | +1           |
| 6        | +2       | +1         | +3    | +7           | +1           |
| 5        | +2       | +2         | +3    | +9           | +1           |
| 4        | +3       | +7         | +5    | +16          | +2           |
| 3        | +3       | +8         | +5    | +17          | +2           |
| 2        | +4       | +11        | +6    | +25          | +4           |
| 1        | +4       | +12        | +6    | +30          | +4           |
| 0        | +6       | +15        | +7    | +45          | +6           |

---

### Mirror Revival
**Sequence Requirement:** 3

- **Passive:** When the caster would die, the death is cancelled if there is a glass block or pane within **100 blocks**.
- The caster is **fully healed** and **teleported** to the nearest glass block.
- Does not trigger if the killing blow was from Losing Control damage.

---

### Blood Loss
**Sequence Requirement:** 7

- **Passive:** On melee hit (within 2 blocks), **40% chance** to drop a **Blood item** tagged with the hit target's UUID.
- The Blood item can be used with **Curse** to curse that specific target.

---

### Feather Fall
**Sequence Requirement:** 9

- **Passive:** When falling more than 3 blocks, automatically applies **Slow Falling (Level 2, 1.5 seconds)** to prevent fall damage.
