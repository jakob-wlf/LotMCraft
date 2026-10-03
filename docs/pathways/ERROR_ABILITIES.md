# Error Pathway Abilities

## Spirituality

Spirituality regenerates at **0.06% of max per tick** (1.2% per second) passively.

| Sequence | Max Spirituality | Regen/sec |
|----------|-----------------|-----------|
| 9        | 360             | 4.3/s     |
| 8        | 400             | 4.8/s     |
| 7        | 1,560           | 18.7/s    |
| 6        | 2,400           | 28.8/s    |
| 5        | 3,800           | 45.6/s    |
| 4        | 7,800           | 93.6/s    |
| 3        | 10,000          | 120.0/s   |
| 2        | 20,000          | 240.0/s   |
| 1        | 40,000          | 480.0/s   |
| 0        | 120,000         | 1,440.0/s |

---

## Ability Theft Mechanic

**Ability Theft** is the Error pathway's signature: stealing copies of another Beyonder's abilities and temporarily disabling them on the original owner. The TheftHandler governs success, count, uses, and disable time.

**Theft failure:** Against a same-sequence or weaker target, there is a **15% fail chance per sequence the thief is stronger** than the target (caps at 95%). Against a significantly stronger target, theft automatically fails. Error Beyonders are **immune** to theft — attempting to steal from an Error Beyonder of lower sequence always fails.

**On success**, the thief receives copies with limited uses, and the stolen abilities are disabled on the victim for a duration:

| Thief Sequence | Abilities Stolen | Uses per Copy | Victim Disable Duration |
|----------------|-----------------|---------------|------------------------|
| 6–9            | 1               | 1             | 35 seconds             |
| 5              | 1               | 5             | 60 seconds             |
| 4              | 2               | 10            | 2 minutes              |
| 3              | 3               | 10            | 4 minutes              |
| 2              | 4               | 20            | 8 minutes              |
| 1              | 5               | 20            | ~13 minutes            |

Stolen copies cannot be used if the ability is already owned by the thief at the same sequence.

---

## Active Abilities

---

### Time Manipulation
**Sequence Requirement:** 1
**Spirituality Cost:** 10000
**Cooldown:** 17 seconds
*(Cannot be copied or replicated)*

Three selectable modes:

**Mode 0 — Stop Time**
- Spawns a **Time Change entity** that sets the local time multiplier to **0.001×** (near-complete freeze).
- **Radius:** `50 × caster multiplier` blocks
- **Duration:** 15 seconds

**Mode 1 — Accelerate Time**
- Spawns a **Time Change entity** that sets the local time multiplier to **4×**.
- **Radius:** `50 × caster multiplier` blocks
- **Duration:** 15 seconds

**Mode 2 — Slow Time**
- Spawns a **Time Change entity** that sets the local time multiplier to **0.2×**.
- **Radius:** `50 × caster multiplier` blocks
- **Duration:** 15 seconds

---

### Conceptual Theft
**Sequence Requirement:** 2
**Spirituality Cost:** 10000
**Cooldown:** 10 seconds
*(Cannot be copied, replicated; cannot be used by NPCs)*

Four selectable modes:

**Mode 0 — Day/Night Theft**
- Steals the current day or night from the world.
- If it is currently **day**, advances time to **midnight** and gives the caster a **Sun item**.
- If it is currently **night**, advances time to **noon** and gives the caster a **Moon item**.

**Mode 1 — Area Theft**
- **Target Range:** 50 blocks; **Radius:** 24×9 ellipsoid (requires griefing)
- Removes all blocks in the target area (up to **10,000 blocks**) and stores them as an **Excavated Area Item**.

**Mode 2 — Digestion Theft**
- **Target Range:** `15 × multiplier²` blocks
- Drains the target's **digestion** and partially restores the caster's own.
- Subject to theft failure chance (see Ability Theft mechanic).
- Amount stolen scales with caster's multiplier and sequence difference.

**Mode 3 — Sanity Theft**
- **Target Range:** `15 × multiplier²` blocks
- Drains the target's **sanity** and partially restores the caster's.
- Subject to theft failure chance.
- Amount stolen scales with caster's multiplier.

---

### Fate Siphoning
**Sequence Requirement:** 2
**Spirituality Cost:** 4000
**Cooldown:** 20 seconds
*(Cannot be copied)*

- **Target Range:** 30 blocks
- Links the caster's fate to the target for **14 seconds**.
- While linked:
  - **All incoming damage** to the caster is **cancelled** and redirected to the target instead.
  - **All negative potion effects** applied to the caster are blocked and redirected to the target instead.
- **Resistance:** Higher-sequence targets may resist the link being established. Error Beyonders of lower sequence always resist.
- Only one link can be active per caster at a time.

---

### Loop Hole Creation
**Sequence Requirement:** 3
**Spirituality Cost:** 6000
**Cooldown:** 16 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 40 blocks; **Loophole Radius:** 3 blocks
- **Duration:** 14 seconds
- **Effect Interval:** Every 2 ticks
- Each interval, entities within the loophole radius:
  - Are **teleported to the loophole center** (resistance check based on sequence).
  - If a Beyonder, have an ability theft attempt performed on them (see Ability Theft mechanic).
- While an entity is inside the loophole, any ability they use is **intercepted and cast by the caster instead** (sequence resistance applies; ability must allow NPC use).
- Does not intercept Loop Hole Creation or Avatar Creation.

---

### Deceit
**Sequence Requirement:** 3
**Spirituality Cost:** 4000
**Cooldown:** 15 seconds
*(Cannot be copied or replicated)*

Two selectable modes:

**Mode 0 — Deceive Others**
- **Radius:** 20 blocks
- Applies a **0.65× damage modifier** to all nearby entities for **20 seconds** (does not apply to Error Beyonders of lower sequence than the caster).
- Clears the targets of all mobs in range.
- The caster cannot be targeted by mobs for **12 seconds**.

**Mode 1 — Deceive the World**
- The caster cannot be harmed by **equal or stronger** sources for **7 seconds**.
- Damage from significantly weaker attackers still applies.

---

### Avatar Creation
**Sequence Requirement:** 4
**Spirituality Cost:** 1500
**Cooldown:** 5 seconds
*(Cannot be copied, replicated, used by NPCs, or used in artifacts)*

- Spawns an **Error Avatar** entity at the caster's position.
- The avatar uses the caster's pathway ("error") at **caster sequence + 1** (one sequence weaker).

---

### Parasitation
**Sequence Requirement:** 4
**Spirituality Cost:** 4000
**Cooldown:** 5 seconds
*(Cannot be copied, replicated, used by NPCs, or used in artifacts)*

- **Range:** 8 blocks
- Cannot attach to Beyonders of equal or lower sequence than the caster.
- Merges the caster into a selected host entity:
  - The caster becomes invisible (zero-size bounding box).
  - The host is continuously dragged to move alongside the caster.
  - **All damage** directed at the caster is redirected to the host instead.
  - Mobs targeting the caster have their targets cleared.
  - All **harmful effects** on the caster are removed each tick.
  - The host's fire is cleared on the caster each tick.
  - The host follows the caster's last attack target.
- Deactivates if the host dies, is removed, or moves more than ~11 blocks away.

---

### Host Controlling
**Sequence Requirement:** 4
**Spirituality Cost:** 0
**Cooldown:** 0.5 seconds
*(Cannot be copied, replicated, or used in artifacts)*

*Requires an active Parasitation host.*

Two selectable modes:

**Mode 0 — Drain Health**
- Deals **~19.4 damage × caster multiplier** to the host and heals the caster for the same amount.

**Mode 1 — Kill**
- Sets the host's health to 0.5 and deals 1000 damage, instantly killing it.

---

### Mundane Conceptual Theft
**Sequence Requirement:** 5
**Spirituality Cost:** 50
**Cooldown:** 1 second

Four selectable modes (Mode 0–2 require a target within `15 × multiplier²` blocks; subject to theft failure against Beyonders):

**Mode 0 — Steal Walk**
- Removes the target's ability to move (**removes all movement speed**) and zeroes their velocity every 2 ticks.
- **Duration:** scales with sequence difference:
  - Target weaker: **5–30 seconds** (shorter for larger gaps)
  - Target equal: **30 seconds**
  - Target stronger: **up to 120 seconds**

**Mode 1 — Steal Sight**
- Applies **Blindness (Level 5)** to the target for the same duration as Mode 0.
- Clears mob targeting.

**Mode 2 — Steal Health**
- Deals **~14.2 damage × caster multiplier** to the target and heals the caster for the same amount.

**Mode 3 — Steal Distance**
- *Only usable at Sequence 6 or lower.*
- Teleports the caster to the targeted location within range. Teleport range scales with sequence:
  - Seq 6: **8 blocks** | Seq 5: **16 blocks** | Seq 4: **32 blocks** | Seq 3: **64 blocks** | Seq 2: **128 blocks** | Seq 1: **256 blocks**

---

### Ability Theft
**Sequence Requirement:** 6
**Spirituality Cost:** 200
**Cooldown:** 3 seconds
*(Cannot be copied or replicated)*

Two selectable modes:

**Mode 0 — Steal**
- **Target Range:** 20 blocks
- Attempts to steal abilities from the target (see Ability Theft mechanic above).

**Mode 1 — Use Copied**
- Opens the **Copied Ability Wheel** to select and use a previously stolen ability.

---

### Gift
**Sequence Requirement:** 6
**Spirituality Cost:** 60
**Cooldown:** 1 second
*(Cannot be copied or replicated)*

- **Range:** 20 blocks
- Requires an item in the **off-hand**.
- Transfers the off-hand item to the target.
- **Special items trigger unique effects instead of being transferred normally:**
  - **TNT:** Spawns up to 30 primed TNT (fuse: 0.5s) at the target's location.
  - **Anvil:** Drops one falling anvil from 10+ blocks above the target per anvil in the stack.
  - **Ender Pearl:** Teleports the target to the caster's position.
  - **Fire Charge:** Sets the target on fire (+20 fire ticks per charge).
  - **Lava Bucket:** Sets the target on fire (+30 fire ticks per bucket).
  - **Water Bucket:** Extinguishes the target's fire (once per bucket).

---

### Decryption
**Sequence Requirement:** 7
**Spirituality Cost:** 0.25 per tick (toggle)
*(Cannot be copied, replicated; cannot be used by NPCs)*

- While active:
  - Continuously applies **Night Vision** to the caster.
  - Sends a real-time HUD display showing the Beyonder data of any entity the caster looks at (within **40 blocks**).

---

### Mental Disruption
**Sequence Requirement:** 8
**Spirituality Cost:** 40
**Cooldown:** 2 seconds

- **Target Range:** 20 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~6.3 damage × caster multiplier** (Losing Control damage type).
- Applies **Slowness (Level 9, 6 seconds)** and **Blindness (Level 9, 1.5 seconds)**.
- Zeroes the target's velocity every 2 ticks for **4 seconds**.

---

### Theft
**Sequence Requirement:** 8
**Spirituality Cost:** 400
**Cooldown:** 1.75 seconds
*(Cannot be copied, replicated; cannot be used by NPCs)*

- **Target Range:** `8 × multiplier²` blocks
- Steals a random item directly from the target's inventory (or loot table for mobs).
- Cannot steal from Beyonders that are significantly stronger than the caster, or from Error Beyonders of lower sequence.

---

## Passive Abilities

---

### Physical Enhancements (Error)
**Sequence Requirement:** 9

Includes **Mining Efficiency** at all sequences and **Fire Resistance** at Seq 7.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | —        | —          | +1    | —            | —            | Mining Efficiency +1 |
| 8        | +1       | —          | +2    | +3           | —            | Mining Efficiency +1 |
| 7        | +1       | —          | +3    | +3           | +1           | Fire Resistance +1, Mining Efficiency +2 |
| 6        | +1       | +1         | +3    | +4           | +1           | Mining Efficiency +2 |
| 5        | +2       | +2         | +3    | +5           | +1           | Mining Efficiency +2 |
| 4        | +1       | +4         | +5    | +12          | +2           | Mining Efficiency +4 |
| 3        | +2       | +5         | +5    | +13          | +2           | Mining Efficiency +4 |
| 2        | +3       | +8         | +6    | +21          | +3           | Mining Efficiency +5 |
| 1        | +3       | +8         | +6    | +26          | +3           | Mining Efficiency +5 |
| 0        | +4       | +10        | +7    | +36          | +4           | Mining Efficiency +6 |

---

### Passive Theft
**Sequence Requirement:** 9

- **Passive:** On dealing damage with a melee hit (within 4 blocks), **40% chance** to steal a random item from the target's inventory or loot table.
- Cannot steal from Beyonders that are significantly stronger, or from Error Beyonders of lower sequence.
