# Fool Pathway Abilities

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

### Fooling
**Sequence Requirement:** 0
**Spirituality Cost:** 80000
**Cooldown:** 45 seconds
*(Cannot be shared)*

- **Radius:** `50 × max(multiplier/4, 1)` blocks.
- **Duration:** `60 × max(multiplier/4, 1)` seconds.
- Applies the **Fooling** status to all entities in range for the duration.

**Fooling Status:**
- **Inverted movement** — all directional input is reversed (forward↔back, left↔right, jump↔sneak). Stored as an attachment so it cannot be removed by milk or effect-clearing mechanics.
- **Scrambled abilities** — on every ability use, **25% chance** the ability fails entirely; otherwise a random ability from the target's own pathway is cast instead of the intended one.
- **Stun** — every 10 seconds, the target is stunned for **3 seconds** (complete inability to act).

---

### Grafting
**Sequence Requirement:** 1
**Spirituality Cost:** 10000
**Cooldown:** 1 second
*(Cannot be copied, replicated; cannot be used by NPCs)*

Four selectable modes. All graft effects last **30 seconds** and are used in a two-click selection process (first click: select source; second click: select target/destination).

**Mode 0 — Graft Locations**
- **Range:** 30 blocks
- First use: marks a location. Second use: marks a second location.
- Creates a **Location Grafting entity** linking the two positions — abilities targeting one location are redirected to the other.
- *Shift+Hold* to view active grafting entities nearby. Shift while activating to remove the nearest one.

**Mode 1 — Graft Damage**
- **Range:** 30 blocks
- First use: selects a source entity. Second use: selects a target entity.
- All **incoming damage** to the source entity is **cancelled** and redirected to the target entity instead.
- Higher-sequence sources may resist the graft being applied.

**Mode 2 — Graft Abilities**
- **Range:** 30 blocks (or self if no target)
- First use: selects a source entity. Second use: selects a target entity.
- All **abilities used** by the source entity are redirected — the target entity casts them instead.
- Higher-sequence sources may resist per-ability-use (resistance check each time).

**Mode 3 — Change Target**
- **Range:** 30 blocks
- First use: selects a source entity. Second use: selects a target entity (or location).
- All **targeting** by the source entity (ability targets and entity targets) is **redirected** to the selected target/location for 30 seconds.

---

### Miracle Creation
**Sequence Requirement:** 2
**Spirituality Cost:** 7000
**Cooldown:** 5 seconds
*(Cannot be copied)*

Opens a selection wheel for one of four miracle categories. Costs are shared across all sub-miracles.

**Mode 0 — Structure Summoning** *(requires griefing)*
Choose one structure to summon at the caster's location:
- Village, End City, Pillager Outpost, Desert Temple, Evernight Church

**Mode 1 — Calamity Creation**
Choose one calamity:
- **Meteor** — **Target Range:** 85 blocks. Spawns a meteor. **~45.7 × multiplier damage**, explosion radius 17, knockback 45.
- **Tornados** — Spawns 1 main tornado + 5 additional tornadoes in a ±20 block area. Targeted tornado: **32.5 × multiplier damage**. Others: **~43.3 × multiplier damage**.
- **Volcano** — **Target Range:** 60 blocks. Spawns a volcano entity. **~41 × multiplier damage**.
- **Lightning** — **Target Range:** 70 blocks. Summons giant lightning. **~44.3 × multiplier damage**. 35 branches.

**Mode 2 — Area Manipulation**
Choose one area effect (70-block radius, 15 seconds):
- **Reverse Gravity** — All entities in a **60-block radius** have gravity reversed for **30 seconds** (−2× gravity multiplier).
- **Slow Time** — Creates a Time Change entity at the caster's position with **0.2× time multiplier**, **50-block radius**, for **15 seconds**.
- **Make Ground Hot** — Entities within **70 blocks** on the ground take **~6.9 × multiplier damage every 4 ticks** and are set on fire for 2 seconds. Ground blocks visually blacken. Lasts 15 seconds.
- **Darkness** — Applies **Blindness (Level 6, 10 seconds)** and drains sanity every 4 ticks to all entities within **70 blocks**. Blocks visually darken. Lasts 15 seconds.

**Mode 3 — Teleportation**
Opens a coordinate entry UI; teleports the caster to the specified location.

---

### Historical Void Hiding
**Sequence Requirement:** 3
**Spirituality Cost:** 250 per tick (toggle)
*(Cannot be copied, replicated, stolen, or used in artifacts)*

- Enters a **hidden state** in the Historical Void — the caster is lifted 5 blocks upward, frozen in place, and becomes invisible.
- While hidden:
  - The caster is **completely immune to damage** (Resistance Level 11).
  - All nearby mobs' targets are cleared (cannot be targeted).
  - **All abilities are disabled** for the duration (refreshed every tick).
  - Item use (right-click) is blocked.
- Deactivates if another transformation overrides it.

---

### Historical Void Summoning
**Sequence Requirement:** 3
**Spirituality Cost:** 5000
**Cooldown:** 1 second
*(Cannot be copied, replicated, stolen, used by NPCs, or used in artifacts)*

Four selectable modes. Maximum simultaneous summons and duration scale with sequence:

| Sequence | Max Summons | Summon Duration |
|----------|-------------|-----------------|
| 3        | 5           | 20 seconds      |
| 2        | 10          | 40 seconds      |
| 1        | 15          | 60 seconds      |

**Mode 0 — Summon Item**
- Opens the caster's **Ender Chest**. Clicking an item creates a temporary copy in the caster's inventory.
- Summoned items disappear after the duration and cannot be thrown.
- Blocks placed using summoned items are tracked and removed at expiry.

**Mode 1 — Summon Entity**
- Opens a menu of **marked entities**. Clicking one spawns a temporary copy in the world (2 blocks ahead of the caster).
- Summoned entities are allied with the caster. They disappear after the duration.

**Mode 2 — Mark Items**
- Opens the caster's **Ender Chest** for them to store items they wish to be able to summon later.

**Mode 3 — Mark Entity**
- Records the **nearest living entity** within 10 blocks. Saves its full data (up to 54 entities stored persistently).

---

### Marionette Controlling
**Sequence Requirement:** 4
**Spirituality Cost:** 0
**Cooldown:** 0.5 seconds
*(Cannot be copied, replicated; cannot be used by NPCs)*

Four selectable modes. Requires the caster to have one or more active marionettes.

**Mode 0 — Swap**
- Swaps positions with the currently selected marionette (cross-dimension capable).

**Mode 1 — Damage Auto-Swap**
- Toggles automatic position-swapping. When **the caster would take damage**, the swap triggers instead — the caster moves to the marionette's position and the marionette takes the damage.

**Mode 2 — Control**
- **Range:** 5 blocks (or use a Marionette Controller item)
- Possesses the targeted marionette — takes direct control of it.

**Mode 3 — Get Item**
- Gives the caster a **Marionette Controller item** linked to the currently selected marionette.

*Hold* to view the selected marionette's name and health. *Shift+Hold* to cycle through marionettes.

---

### Puppeteering
**Sequence Requirement:** 5
**Spirituality Cost:** 40
**Cooldown:** 1 second
*(Cannot be copied or replicated)*

- **Range:** 7 blocks (Seq 5–9), 130 blocks (Seq 4), 250 blocks (Seq 3), 500 blocks (Seq 0–2)
- Establishes puppet threads connecting the caster to the target for a duration, then either turns the target into a marionette (success) or kills it (failure).
- While threads are active:
  - Target receives **Slowness (Level 5, 1 second)** and **Blindness (Level 6, 5 seconds)** every 2 ticks.
  - Threads break early if the target takes damage, the caster's health drops below 50% of its value when threading began, or the target moves out of range.
- **Against Beyonders:** If the target is significantly stronger, threads fail and the caster receives **Losing Control (Level 6, 8 seconds)**.
- **Against weaker Beyonders:** Threading time is 8× longer (harder to resist).
- **Duration** (until marionette conversion):
  - Same seq (Seq 5+): **~120 seconds**
  - Caster Seq 5+ vs. weaker target: default scaling (reduced per sequence gap)
  - Caster Seq 3–4 vs. Seq 5+ target: **~25 seconds**
  - Caster Seq 1–2 vs. Seq 3–4 target: **~10 seconds**
  - Caster Seq 1–2 vs. Seq 5+ target: **~5 seconds**
- If the caster is already a marionette, the target is killed instead of turned.
- **On success:** Target is turned into a marionette controlled by the caster. Player targets are replaced with a BeyonderNPC of the same pathway and sequence.

---

### Shape Shifting
**Sequence Requirement:** 6
**Spirituality Cost:** 1000
**Cooldown:** 5 seconds
*(Cannot be copied, replicated; cannot be used by NPCs)*

Two selectable modes:

**Mode 0 — Change Shape**
- Opens a selection screen showing all memorised entity types.
- Transforms the caster's appearance to match the selected entity.

**Mode 1 — Reset Shape**
- Reverts the caster to their original form.

---

### Air Bullet
**Sequence Requirement:** 7
**Spirituality Cost:** 100
**Cooldown:** 1.5 seconds

- **Range:** 10 blocks; **Hit Radius:** 2.5 blocks
- Fires a compressed air projectile that travels 1 block/tick.
- **Hits:** **1 hit** on contact
- **Damage:** **~10.7 damage × caster multiplier** (capped at Sequence 3 power).
- On block contact: triggers an explosion (radius scales with sequence, up to 5.5 at Seq 3+).
- Visual bullet radius scales with sequence (0.2 blocks at Seq 9 up to 1.0 at Seq 3+).

---

### Flame Controlling
**Sequence Requirement:** 7
**Spirituality Cost:** 100
**Cooldown:** 1.5 seconds

- **Range:** 10 blocks; **Hit Radius:** 2.5 blocks
- Fires a flame projectile that travels 1 block/tick.
- **Hits:** **1 hit** on contact
- **Damage:** **~10.5 damage × caster multiplier**.
- Sets the target on fire for **5 seconds** on hit.
- On block contact: places fire at the block if griefing is enabled.

---

### Flaming Jump
**Sequence Requirement:** 7
**Spirituality Cost:** 60
**Cooldown:** 1 second
*(Cannot be used by NPCs)*

- Teleports the caster to a **targeted fire block** within 50 blocks.
- If no fire is targeted directly, teleports to the nearest fire within 20 blocks.
- Applies **Fire Resistance (Level 2, 3 seconds)** and clears fire ticks for 3 seconds after teleporting.
- *Hold* to highlight the targeted fire block.

---

### Paper Figurine Substitute
**Sequence Requirement:** 7
**Spirituality Cost:** 20
**Cooldown:** 10 seconds
*(Cannot be replicated or used in artifacts)*

- Gives the caster a **Paper Figurine Substitute item** (up to **5** held at a time).
- When the caster takes damage (except Losing Control damage):
  - The damage is **completely cancelled**.
  - One figurine is **consumed**.
  - The caster is **teleported** up to 7 blocks away randomly.

---

### Underwater Breathing
**Sequence Requirement:** 7
**Spirituality Cost:** 2 per tick (toggle)
*(Cannot be used by NPCs)*

- Can only be activated while standing in water — deactivates automatically if the caster leaves water.
- Continuously applies **Water Breathing** to the caster while active.
- Emits bubble particles in the direction the caster is looking.

---

## Passive Abilities

---

### Physical Enhancements (Fool)
**Sequence Requirement:** 9

Includes **Fire Resistance** at Seq 7 and below.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | —        | —          | +1    | —            | —            | — |
| 8        | +1       | —          | +2    | +3           | —            | — |
| 7        | +1       | —          | +3    | +3           | +1           | Fire Resistance +1 |
| 6        | +1       | +1         | +3    | +4           | +1           | Fire Resistance +1 |
| 5        | +2       | +2         | +3    | +5           | +1           | Fire Resistance +2 |
| 4        | +1       | +4         | +5    | +12          | +2           | Fire Resistance +2 |
| 3        | +2       | +5         | +5    | +13          | +2           | Fire Resistance +3 |
| 2        | +3       | +8         | +6    | +21          | +3           | Fire Resistance +4 |
| 1        | +3       | +8         | +6    | +26          | +3           | Fire Resistance +4 |
| 0        | +4       | +10        | +7    | +36          | +4           | Fire Resistance +5 |

---

### Miracle of Resurrection
**Sequence Requirement:** 2

- **Passive:** When the caster would die, if they have resurrection attempts remaining:
  - Death is **cancelled**.
  - Inventory is **dropped** at the death location.
  - Caster is **teleported** to a random position up to 50 blocks away in the Overworld (surface level).
  - Caster is immediately placed into **Historical Void Hiding**.
  - Caster is **fully healed** and all effects are removed.
  - All abilities are **disabled for 10 minutes**.
  - One resurrection attempt is consumed.

---

### Puppeteering Enhancements
**Sequence Requirement:** 4

- **Passive:** Enhances the Puppeteering ability. *(Specific mechanical effects are applied through the broader marionette system.)*

---

### Paper Daggers
**Sequence Requirement:** 8

- **Passive:** When the caster **right-clicks with a Paper item** in their main hand, one sheet of paper is consumed and a **Paper Dagger projectile** is launched.
- **Projectile Speed:** 1.2 blocks/tick; **Range:** 50 blocks.
- Works in creative mode without consuming paper.
