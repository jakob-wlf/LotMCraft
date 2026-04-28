# Mother Pathway Abilities

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

### World Creation
**Sequence Requirement:** 1
**Spirituality Cost:** 1000
**Cooldown:** None
*(Cannot be copied, replicated, or used by NPCs)*

- Teleports the caster to their personal **Nature pocket dimension**.
- Each player has a fixed location in the Nature dimension that persists across visits.
- On arrival, a **Return from Nature portal** entity is spawned which can teleport the player back to their original position.
- If the caster is already in the Nature dimension, casting again searches for the nearest Return portal within 200 blocks and activates it.

---

### Wrath of Nature
**Sequence Requirement:** 1
**Spirituality Cost:** 1000
**Cooldown:** None
*(Cannot be copied)*

Three selectable modes:

**Mode 0 — Lightning**
- **Target Range:** 70 blocks (targeted location)
- Summons a giant lightning strike with **35 branches** once every 20 ticks for **4 seconds** (4 total strikes).
- Each strike deals **~50.0 × multiplier damage**.

**Mode 1 — Fire**
- Creates a large fire effect in an **ellipsoid area (radius 45×18)** around the caster.
- Deals **~10.25 × multiplier damage** to all enemies within 55 blocks every **4 ticks** for **15 seconds** (~19 hits).
- Visually spreads outward from the caster as a wave.
- Restores original visual state after **~10 seconds** post-spread.

**Mode 2 — Moon**
- **Target Range:** 30 blocks (targeted position)
- Spawns a **Big Moon entity** at the target position + 25 blocks up, which persists for **30 seconds**.
- Only one Big Moon can exist within 100 blocks of the caster at a time.
- Every tick the moon: deals **~4.3 × multiplier damage** (Seq 2 scale) to all entities within **120 blocks**, applies **Slowness IV** for 5 seconds, and drains **5 spirituality** from all nearby entities.
- Uses the Seq 2 caster multiplier value as a fixed baseline (not the actual caster's sequence).

---

### Maternal Embrace
**Sequence Requirement:** 3
**Spirituality Cost:** 1600
**Cooldown:** 20 seconds
*(Cannot be copied)*

- **Target Range:** 20 blocks
- Spawns a **Coffin entity** around the target.
- Cannot affect targets that are significantly stronger than the caster, or same-sequence Mother Beyonders.
- On a valid target:
  - Disables all abilities for **30 seconds**.
  - Applies **Resistance XX, Slowness XX, and Blindness XX** for 30 seconds (entity is incapacitated but nearly invulnerable).
  - The target is continuously moved to the coffin position each tick.
  - After 30 seconds, the coffin and all effects are removed.
- If no entity is targeted, the coffin is spawned at the targeted location with no effect applied.

---

### Life Deprivation
**Sequence Requirement:** 3
**Spirituality Cost:** 1200
**Cooldown:** 15 seconds
*(Cannot be copied)*

Two selectable modes:

**Mode 0 — Target Entity**
- **Target Range:** 20 blocks
- Drains life from a single target every **2 ticks** for **2.5 seconds** (~12 hits).
- Deals **~1.74 × multiplier damage per hit** (soul particles fly toward the caster).

**Mode 1 — Area**
- Drains life from all enemies within **55 blocks** every **2 ticks** for **2.5 seconds** (~12 hits).
- Deals **~1.57 × multiplier damage per hit** per target.
- Also corrupts blocks in the area (55×10 ellipsoid) to Soul Soil over 3 seconds (requires griefing).

---

### Mutation Creation
**Sequence Requirement:** 4
**Spirituality Cost:** 800
**Cooldown:** None
*(Cannot be copied)*

- **Target Range:** 20 blocks
- Applies the **Mutated** effect to the target. Cannot be applied if the target is already Mutated.
- **Mutated effect:** `amplifier = 2 × multiplier`
  - Every tick: 5% chance to drain 1% sanity; 5% chance to deal **~18.4 damage** (flat, `lookupDamage(4, .5)`).
- **Duration** scales by relative strength:
  - Target significantly weaker or non-Beyonder: **60 seconds**
  - Target equal or weaker (same tier): **30 seconds**
  - Target significantly stronger: **5 seconds**

---

### Golem Creation
**Sequence Requirement:** 4
**Spirituality Cost:** 600
**Cooldown:** 10 seconds
*(Cannot be copied)*

- Spawns an **Iron Golem** at the caster's position, allied to the caster.
- The golem has **200 max health** and **40 attack damage** (vs vanilla 100 HP and 7.5–21.5 damage).

---

### Nature Spells
**Sequence Requirement:** 5
**Spirituality Cost:** 350
**Cooldown:** 3 seconds

Three selectable modes:

**Mode 0 — Swamp**
- Creates a swamp effect in a **20-block radius sphere** around the caster for **10 seconds** (200 ticks).
- Every 10 ticks: applies **Slowness III** to all nearby enemies, and pushes them slightly downward if they are airborne.
- Spawns ambient brown/earthquake particles across the area.
- The effect cancels if the caster moves too far from the origin.

**Mode 1 — Child of Oak**
- Toggle. Activating again cancels the effect.
- While active, every tick: grants the caster a **1.25× multiplier boost** for 1.5 seconds, **Speed II**, and **Strength I**.
- Continuously maintained as long as the toggle is on and the caster is alive.

**Mode 2 — Nature's Wrath**
- **Target Range:** 25 blocks
- Attaches a lingering nature curse to the target for **25 seconds**.
- Every **2 ticks**, while the caster remains in range:
  - 1-in-20 chance: deals **~13.5 × multiplier damage** directly.
  - 1-in-25 chance: phases the target slightly downward (floor trap effect).
  - 1-in-30 chance: launches a vine attack dealing **~13.7 × multiplier damage** from a random direction.
- Cannot be applied to a target already afflicted.

---

### Crossbreeding
**Sequence Requirement:** 6
**Spirituality Cost:** 220
**Cooldown:** 1 second
*(Cannot be copied, replicated, or used by NPCs)*

- **Target Range:** 20 blocks
- A two-click ability. First click selects the first mob; second click selects a different mob.
- Cannot target players, Beyonder NPCs, Ender Dragons, or Withers.
- On the second click: spawns a **Hybrid Mob** — a new entity of the second mob's type that visually renders the first mob's body (client-side hybrid rendering).
- The two selected mobs are not harmed or removed.

---

### Poison Creation
**Sequence Requirement:** 6
**Spirituality Cost:** 250
**Cooldown:** 3 seconds

Two selectable modes:

**Mode 0 — Poison Area**
- Creates an expanding poison cloud centered on the caster for **5 seconds**.
- The cloud radius grows **0.5 blocks per 2 ticks**, starting from 0.
- Every 2 ticks: deals **~1.27 × multiplier damage** and applies **Poison IX** for 5 seconds to all entities in the cloud.

**Mode 1 — Poison Threads**
- **Target Range:** 16 blocks
- Fires 8 animated poison threads at the target location.
- Deals **~12.3 × multiplier damage** to the target entity.
- Applies **Poison IX** for **5 seconds**.

---

### Plant Controlling
**Sequence Requirement:** 7
**Spirituality Cost:** 45
**Cooldown:** 2 seconds

Two selectable modes:

**Mode 0 — Entrap**
- **Target Range:** 16 blocks
- Binds the target with plant tendrils for **20 seconds**.
  - Continuously applies **Slowness XI, Weakness XI, and Mining Fatigue XI** every 5 ticks.
  - Cancels all movement (velocity zeroed each tick).
  - If the target is a non-player mob that is not significantly stronger than the caster, it is fully AI-disabled.
- Does not apply to the same entity twice simultaneously.
- The entrap effect cancels immediately if the target uses a "Blink Escape"-type interaction.

**Mode 1 — Attack**
- **Target Range:** 16 blocks
- Fires an animated vine attack at the target.
- Deals **~10.3 × multiplier damage**.

---

### Healing
**Sequence Requirement:** 8
**Spirituality Cost:** 25
**Cooldown:** 10 seconds

Two selectable modes. Non-player casters always use Mode 0.

**Mode 0 — Heal Self**
- Restores `10 × multiplier²` health to the caster.
  - Seq 9: ~10 HP | Seq 7–8: ~18–43 HP | Seq 5: ~34 HP | Seq 4: ~34 HP | Seq 3: ~42 HP | Seq 2: ~180 HP | Seq 1: ~272 HP

**Mode 1 — Heal Others**
- **Radius:** 6 blocks (allies only)
- Restores `10 × multiplier²` health to all allies in range (same formula as above).

---

### Cleansing
**Sequence Requirement:** 8
**Spirituality Cost:** 25
**Cooldown:** 14 seconds
*(Cannot be copied; registers as a cleansing interaction)*

Two selectable modes. Non-player casters always use Mode 0.

**Mode 0 — Cleanse Self**
- Removes all harmful potion effects from the caster.
- Extinguishes fire.
- Restores food to **20 (full)** and saturation to **20** (for players).

**Mode 1 — Cleanse Others**
- **Radius:** 6 blocks (allies only)
- Removes all harmful effects, extinguishes fire, and restores food/saturation for all allies in range.

---

### Area Desolation
**Sequence Requirement:** 2 (for entity placement)
**Spirituality Cost:** 1400
**Cooldown:** 5 seconds
*(Cannot be copied; requires griefing enabled)*

- Spawns a persistent **Desolate Area entity** at the caster's position.
- Casting again within 30 blocks removes the existing entity.
- The entity continuously corrupts a **200×200 area** (80 blocks per tick), converting:
  - Grass/Dirt/Podzol/Mud → Soul Sand / Soul Soil / Basalt (noise-based patches)
  - Flowers → Wither Roses
  - Logs → Polished Basalt
  - Leaves → Mangrove Roots
  - Crops/Saplings/Farmland → Soul Sand (crops destroyed without drops)
  - Vines → Removed
  - Bush blocks → Dead Bush (70%) or removed (30%)
  - Stone/Cobblestone/Sand/etc. → Basalt or Blackstone
- Every **1 second**: applies **Slowness, Weakness, Mining Fatigue, Hunger, and Wither** (0–3 amplifier based on distance from center) to all entities in a **100-block radius** (except Mother Beyonders Seq 2 or stronger).
- Every **2 seconds**: deals **~41 flat damage** (no multiplier, `lookupDamage(2, .5)`) to all entities in range.

---

### Blooming Area
**Sequence Requirement:** 2 (for entity placement)
**Spirituality Cost:** 1400
**Cooldown:** 5 seconds
*(Cannot be copied; cannot be used by NPCs; requires griefing enabled)*

- Spawns a persistent **Blooming Area entity** at the caster's position.
- Casting again within 30 blocks removes the existing entity.
- The entity continuously nurtures a **200×200 area**:
  - Applies bonemeal to 15–30 random growable blocks per second (crops, saplings, grass, sugar cane).
  - Spawns 3–7 flowers or mushrooms every 0.5 seconds in designated patches (15–25 random patches, 5–10 block radius each).
  - Instantly grows all crops and saplings in the area every second.
- Every 10 seconds: applies **Saturation XXI, Regeneration XXI, and Hero of the Village VI** to all entities within the 200-block radius.

---

## Passive Abilities

---

### Physical Enhancements (Mother)
**Sequence Requirement:** 9

No Fire Resistance or Night Vision at any sequence. Emphasizes high Health and Regeneration over combat stats.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration |
|----------|----------|------------|-------|--------------|--------------|
| 9        | +1       | —          | —     | +2           | —            |
| 8        | +1       | —          | —     | +6           | —            |
| 7        | +1       | —          | +1    | +8           | +1           |
| 6        | +2       | +1         | +1    | +10          | +2           |
| 5        | +2       | +2         | +1    | +12          | +2           |
| 4        | +3       | +7         | +2    | +24          | +4           |
| 3        | +3       | +8         | +2    | +28          | +4           |
| 2        | +4       | +11        | +3    | +36          | +5           |
| 1        | +4       | +12        | +3    | +42          | +6           |
| 0        | +6       | +15        | +6    | +64          | +7           |

---

### Plant Nurturing
**Sequence Requirement:** 9

- **Active use:** Costs **10 spirituality**, 2-second cooldown.
- Applies bonemeal twice to all growable blocks (crops, saplings, sugar cane) within a **4.5-block radius circle** at three vertical levels (below, at, and above the caster's feet) — effectively a thick disk of ~4.5 blocks radius.
- NPCs only use this when they have no combat target.

---

### Life Aura
**Sequence Requirement:** 4 (requires `blooming` state)
**Spirituality Cost:** 3/tick (toggle)
*(Cannot be copied or used by NPCs)*

- Toggle ability. While active:
  - Every tick: applies bonemeal to all growable blocks in a **30×7 ellipsoid** around the caster (excluding short grass; skips fully-grown cocoa).
  - Every tick: puts all animals within **35 blocks** into "love mode" (breeds them).
  - Every tick: applies **Regeneration IV** for 2 seconds to all entities within 35 blocks.
