# Death Pathway Abilities

## Spirituality

Spirituality regenerates at **0.06% of max per tick** (1.2% per second) passively.

| Sequence | Max Spirituality | Regen/sec |
|----------|-----------------|-----------|
| 9        | 540             | 6.5/s     |
| 8        | 600             | 7.2/s     |
| 7        | 2,340           | 28.1/s    |
| 6        | 3,600           | 43.2/s    |
| 5        | 5,700           | 68.4/s    |
| 4        | 11,700          | 140.4/s   |
| 3        | 15,000          | 180.0/s   |
| 2        | 30,000          | 360.0/s   |
| 1        | 60,000          | 720.0/s   |
| 0        | 180,000         | 2,160.0/s |

---

## Active Abilities

---

### Divine Kingdom
**Sequence Requirement:** 1  
**Spirituality Cost:** 30, 000
**Cooldown:** 5 minutes  
*(Cannot be copied, replicated, or stolen)*

- **Radius:** 120 blocks
- **Duration:** 3 minutes (3,600 ticks)
- **Effect Interval:** Every tick

**Projectile Destruction:**
- All non-allied projectiles inside the domain are discarded every tick with a smoke particle burst.

**Damage Debuff:**
- Applies a `divine_kingdom_debuff` outgoing damage modifier to all non-allied entities in range each tick.
- At same sequence: **−30%** (0.70× multiplier).
- Each sequence the target is weaker: additional **−5%** (minimum 0×).
- Modifier is removed when the target leaves the domain.

**Durability Drain:**
- Every second (every 20 ticks), all equipped items in every slot (head, chest, legs, feet, mainhand, offhand) lose **15 durability** via `hurtAndBreak`.

**Instant Kill:**
- Entities that are **2+ sequences weaker** than the caster are killed instantly on the first tick they are in range.

**Countdown & Death:**
- Each non-allied entity (not instantly killed) that enters the domain receives a personal countdown timer.
- Base timer: **45 seconds**, reduced by **5 seconds per sequence the target is weaker** (minimum 1 second).
- Timer is shown on the target's action bar every second as `☠ Divine Kingdom: Xs ☠`.
- The countdown **pauses when the target leaves the domain** and **resets on re-entry**.
- When the countdown reaches 0, the target is killed instantly.

---

### Nation of the Dead
**Sequence Requirement:** 2  
**Spirituality Cost:** 8,000  
**Cooldown:** 3 minutes  
*(Cannot be copied, replicated, or stolen)*

- **Radius:** `35 × max(multiplier/4, 1)` blocks (scales with multiplier)
- **Duration:** 1 minute 40 seconds (2,000 ticks)
- **Effect Interval:** Every tick; damage every 20 ticks (once per second)

**Instant Kill:**
- Entities that are **2+ sequences weaker** than the caster are killed instantly on the first tick they are in range.

**Persistent Debuffs** (applied to all non-allied entities in range, refreshed each tick):
- **Wither II** (2-tick duration, refreshed)
- **Slowness III** (2-tick duration, refreshed)
- **Weakness II** (2-tick duration, refreshed)
- **Darkness** (3-tick duration, refreshed — players only)
- **Regeneration suppressed** — targets cannot regenerate health while inside the domain.

**Per-Second Damage** (sequence-scaled, does not affect allies):
- Same sequence: **3.0%** of target's max HP per second.
- Each sequence weaker (target seq > caster seq): **+0.5%** per step.
- Each sequence stronger (target seq < caster seq): **−0.5%** per step.
- No damage if the scaled percentage reaches 0% or below.

| Sequence Difference | Damage/sec |
|---|---|
| Target 3 seq weaker | 4.5% max HP |
| Target 2 seq weaker | 4.0% max HP |
| Target 1 seq weaker | 3.5% max HP |
| Same sequence        | 3.0% max HP |
| Target 1 seq stronger | 2.5% max HP |
| Target 2+ seq stronger | No damage  |

**Death Skeleton** (triggered on kill within the domain):
- When any non-allied, non-subordinate entity dies inside the domain, an iron-armoured skeleton subordinate spawns at the death location.
- The skeleton has **6× base attack damage** and **6× base max HP**.
- Particle and sound burst plays at the death position.

---

### Death Flame
**Sequence Requirement:** 2  
**Spirituality Cost:** 10,000  

- **Duration:** 7 seconds (140 ticks)
- **Cone Length:** 16 blocks
- **Cone Max Radius:** 5 blocks (widens linearly from origin)

Each tick for the duration:
- Fires a cone of white flame particles from the caster's eye in their look direction.
- Deals damage every tick to all entities within the cone: `DamageLookup.lookupDps(2, 0.8, 1, 30) × multiplier`.
- Sets targets on fire (adds 30 fire ticks per hit).
- Invulnerability frames are reset each tick so damage lands every tick.
- If griefing is enabled, randomly places fire on blocks within the cone.

**Visual:**
- White flame particles ring-spread along the cone, with central fill particles and dense particles near the origin.

---

### Pale Eye
**Sequence Requirement:** 3  
**Spirituality Cost:** 400/activation  
*(Toggle — cannot be copied, not usable in artifacts)*

- **Targeting Range:** 25 blocks (line-of-sight)

While active, each tick:
- Particles spawn around the caster's eye.
- Looks for a target within 25 blocks.
- If a target is found:

**Instant Kill:**
- Target is **2+ sequences weaker** (casterSeq + 1 < targetSeq): instant kill with a large particle burst.

**Otherwise (target is same or stronger sequence):**
- **Blindness II** for 3 seconds.
- **Slowness III** for 3 seconds.
- Deals damage per tick using `DamageLookup.lookupDps(3, 0.9, 5, 35) × multiplier`.

---

### Hand of Death
**Sequence Requirement:** 3  
**Spirituality Cost:** 2000  
**Cooldown:** 60 seconds  
*(Cannot be copied)*

Three selectable sub-abilities:

**Left Hand** *(targeted)*
- **Targeting Range:** 30 blocks (line-of-sight)
- Applies to the target for 30 seconds:
  - **Wither II**
  - **Weakness II**
  - **Blindness II**
- After the 30-second duration expires, deals damage as a percentage of the target's max HP, scaled by the sequence difference at the moment of casting, and suppresses **regeneration for 10 seconds**:
  - Base (same sequence): **25%**
  - Each sequence weaker: **+10%** (e.g. 1 weaker → 35%, 2 weaker → 45%)
  - Each sequence stronger: **−10%** (e.g. 1 stronger → 15%, 2 stronger → 5%), minimum 0%
- Re-casting on the same target cancels the pending hit and restarts the timer.

**Right Hand — Self**
- Heals the caster for **25%** of their max HP instantly.

**Right Hand — Others** *(targeted)*
- **Targeting Range:** 30 blocks (line-of-sight)
- Heals the targeted entity for **25%** of their max HP instantly.

---

### Internal Underworld
**Sequence Requirement:** 4  
**Spirituality Cost:** 3000
**Cooldown:** 1 tick (effectively instant)  
*(Cannot be copied, replicated, or stolen)*

- **Capture Chance:** 50%

**Soul Capacity by Sequence:**

| Sequence | Max Stored Souls |
|----------|-----------------|
| 5        | 5               |
| 4        | 15              |
| 3        | 20              |
| 2        | 35              |
| 1        | 45              |
| 0        | 53              |

**Soul Capture** (passive, triggered on kill):
- When the caster kills a Beyonder NPC of higher sequence (weaker), there is a 50% chance to absorb their soul into the Internal Underworld.
- Cannot capture souls of equal or stronger sequence.
- Captured soul data includes entity type, display name, pathway, and sequence.

**Summon Soul** (active sub-ability):
- Opens a GUI to select and summon a stored soul as a subordinate.
- Summons appear near the caster and can be released or recalled.

**Summon All Souls** (active sub-ability):
- Instantly summons all stored souls simultaneously as subordinates, up to the caster's current sequence capacity.
- Each soul is removed from storage upon summoning.

---

### Undying Seal
**Sequence Requirement:** 4  
**Spirituality Cost:** 350  
**Cooldown:** 80 seconds

- **Duration:** 60 seconds

While active, suppresses all negative artifact effects on the caster for the duration.

---

### Death Spells
**Sequence Requirement:** 4  
**Spirituality Cost:** 750  
**Cooldown:** 3 seconds

Two selectable modes:

**Withering Wind**
- Fires a slow-moving wind projectile forward from the caster's eye level, traveling 0.5 blocks/tick for **4 seconds (80 ticks)**.
- Every tick, deals `DamageLookup(4, 0.8) × multiplier` damage to all entities within a **9-block radius** of the projectile's current position.
- If griefing is enabled, converts blocks in an **8-block sphere** around the projectile's path into Soul Soil or Basalt (~80% chance per block, skipping indestructible/Soul Soil/Basalt blocks).
- Ends early if the caster leaves their current area.

**Soul Siphon**
- Targets an entity within **20 blocks** (line-of-sight).
- Drains the target over **3 seconds (60 ticks)**:
  - Every 4 ticks: deals `DamageLookup(4, 0.6) × multiplier` damage to the target; on hit, heals the caster for **1.5 HP**.
- Breaks if the target moves more than **25 blocks** away or either party dies.
- Ends early if the caster leaves their current area.

---

### Door to the Underworld
**Sequence Requirement:** 5  
**Spirituality Cost:** 600  
**Cooldown:** 10 seconds

Three selectable modes:

**Spirits**
- Spawns a visual portal near the caster's look target.
- Every 4 ticks for 20 seconds (400 ticks), spawns one undead mob and one spirit mob from the portal as subordinates.
  - **Undead:** Zombie, Skeleton, Husk, Drowned, Stray, or Wither Skeleton (random).
  - **Spirits:** Spirit Ghost, Spirit Dervish, Spirit Bubbles, Blue Wizard, or Translucent Wizard (random).
- Summoned mobs despawn after 60 seconds.
- Re-using the mode while a portal is open closes it instead.

**Tentacles**
- Spawns a portal near the caster's look target that deals AoE damage every 10 ticks for 20 seconds.
- **Damage radius:** 6.5 blocks from the portal's front face.
- **Damage per hit:** `DamageLookup.lookupDamage(5, 0.85) × multiplier`
- Re-using the mode while a portal is open closes it instead.

**Release**
- Despawns the active portal and all summoned mobs linked to the caster.
- Bypasses cooldown and spirituality cost.

---

### Death Envoy
**Sequence Requirement:** 5  
**Spirituality Cost:** 800  
**Cooldown:** 10 seconds  
*(Cannot be copied or stolen)*

- **Radius:** 5 blocks
- **Effect Duration:** 20 seconds on all affected entities

Applies the following to all nearby non-allied entities on use. Entities **2+ sequences stronger** than the caster are unaffected:
- **Weakness II** for 20 seconds
- **Slowness III** for 20 seconds
- **Freeze ticks** (+400 ticks)
- **Spirit Called** for 10 seconds (full stun, armour bypass, ability block — see Word of Spirit)

---

### Word of Spirit
**Sequence Requirement:** 6  
**Spirituality Cost:** 300  
**Cooldown:** 45 seconds

- **Targeting Range:** 25 blocks

Applies the **Spirit Called** custom effect (Level 0) to the target for 10 seconds. Has no effect on targets **1+ sequences stronger** than the caster.

> **Spirit Called** is a harmful custom effect. While active, every tick:
> - The target's movement is zeroed (full stun).
> - **Slowness Level 100** and **Jump Boost Level 128** are refreshed, preventing all movement and jumping.
> - All Beyonder ability usage is blocked.
> - Item use and block interaction are cancelled.
> - All incoming damage **bypasses armour** — hits are re-dealt next tick using an armour-ignoring damage source.

---


### Restruction
**Sequence Requirement:** 6  
**Spirituality Cost:** 1,500  
**Cooldown:** 20 seconds (Release bypasses cooldown)  
*(Cannot be copied or stolen)*

Two selectable modes:

**Summon**
- Summons up to **`8 × max(multiplier/4, 1)`** Skeletons and the same number of Zombies near the caster (within a 4-block radius), each wearing a full set of iron armour (no drop chance).
- All summoned mobs are registered as subordinates of the caster.

**Release**
- Despawns all currently summoned mobs linked to the caster.
- Bypasses cooldown and spirituality cost.

---


### Spirit Channeling
**Sequence Requirement:** 7  
**Spirituality Cost:** 300  
**Cooldown:** 20 seconds

Captures a spirit from the environment (75% success chance at Seq ≤ 6, 50% otherwise). The spirit type is randomized between **Frost Ghost** and **Earth Spirit**, unlocking different sub-abilities.

**Base Modes (no spirit captured):**
- Get Spirit
- Release Spirit

**Frost Ghost Modes:**

*Frozen Domain*
- Spawns an expanding frost ring from the caster's position over 3 seconds (radius grows by 0.5 every 2 ticks).
- Entities caught in the ring receive: **Slowness Level 100** (stun) and **Jump Boost Level 128** (prevents jumping) for 3 seconds, then **Slowness III** for 5 seconds.
- Applies visual freeze ticks.
- Has no effect on targets **2+ sequences stronger** than the caster.

*Glacial Aegis*
- Active for up to 10 seconds.
- **Negates the next hit** taken by the caster entirely (consumes the aegis).
- Has no effect on targets **2+ sequences stronger** than the caster.

**Earth Spirit Modes:**

*Stone Restrainment*
- **Targeting Range:** 20 blocks
- Encases the target in stone for 4 seconds: **Slowness Level 100** + **Jump Boost Level 128** every 2 ticks, plus **1 damage** every 2 ticks (with invulnerability frames reset).
- Has no effect on targets **2+ sequences stronger** than the caster.

*Earthen Fist*
- Launches two fists with a slight left/right spread at **0.8 blocks/tick** for up to 40 ticks.
- Each fist deals **6 damage** on contact within a **1.2-block radius**.

*Quicksand*
- **Targeting Range:** 25 blocks
- Creates a swirling quicksand zone with **5-block radius** for 10 seconds (200 ticks, applied every 2 ticks).
- Entities in range receive **Slowness IV** and are pulled downward.
- Has no effect on targets **2+ sequences stronger** than the caster.

*Earth Heal*
- Heals the caster for **10%** of their max HP instantly.

---

### Zombie Disguise
**Sequence Requirement:** 7  
**Spirituality Cost:** 7/tick  
*(Toggle — cannot be copied, replicated, or stolen)*

While active:
- Transforms the caster's appearance to a Zombie.
- Grants **+28 max HP** (equivalent to Sequence 6 physical enhancements).
- Grants **+6 attack damage**.
- Continuously refreshes **Resistance I** every 2 ticks.

On deactivation, all bonuses and appearance changes are reverted.

---

### Eye of Death
**Sequence Requirement:** 8  
**Spirituality Cost:** 0.5/tick  
*(Toggle — players only; not usable in artifacts)*

While active:
- Grants **Night Vision** (refreshed every 25 seconds).
- All nearby entities within **30 blocks** glow for the caster only.
- Highlights the looked-at entity for the caster's HUD within **40 blocks**.
- **+35% damage** to all undead and spirit entities while active.

---

### Spirit Communication
**Sequence Requirement:** 8  
**Spirituality Cost:** 10  
**Cooldown:** 10 seconds  
*(Players only; not usable in artifacts)*

Four selectable modes:

**Danger Premonition**
- Toggle. While active, drains **0.5 spirituality every 2 ticks**.
- Activates a client-side premonition HUD effect.

**Structure Divination**
- Opens a structure divination screen to locate nearby structures.

**Player Divination**
- Opens a screen listing all other online players for targeting/tracking.

**Spectral Bind**
- **Targeting Range:** 20 blocks (line-of-sight)
- Applies to the target:
  - **Slowness Level 100** for 3 seconds — full movement stun
  - **Freeze ticks** (+60) — powder snow freezing visual
  - **Weakness II** for 30 seconds
- Has no effect on targets **2+ sequences stronger** than the caster.

---

## Passive Abilities

---

### Physical Enhancements (Death)
**Sequence Requirement:** 9

**Passively reduces poison and freeze damage by 50%** at all sequences. No Night Vision or Fire Resistance in the table.

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration |
|----------|----------|------------|-------|--------------|--------------|
| 9        | +1       | —          | —     | —            | —            |
| 8        | +1       | —          | +1    | +5           | —            |
| 7        | +2       | —          | +1    | +6           | +1           |
| 6        | +2       | +1         | +2    | +7           | +2           |
| 5        | +2       | +2         | +2    | +9           | +2           |
| 4        | +3       | +7         | +4    | +18          | +3           |
| 3        | +3       | +9         | +4    | +19          | +3           |
| 2        | +4       | +12        | +5    | +27          | +4           |
| 1        | +4       | +13        | +5    | +32          | +4           |
| 0        | +6       | +16        | +6    | +4           | +6           |

---

### Undead Ignorance
**Sequence Requirement:** 9

Passive. All undead mobs will never target the caster — they are completely ignored as a valid attack target.

---

### Solar Sensitivity
**Sequence Requirement:** 7

Passive. During daytime (while the sun is up), the caster receives **Weakness I** (refreshed every 1.5 seconds).

---

### Reincarnation
**Sequence Requirement:** 4  
*(Cannot be copied or replicated)*

Passive. When the caster would die (excluding loss-of-control deaths):
- Death is cancelled and the caster is fully healed.
- The caster is teleported to a random safe location at least **500 blocks** from the death point, within the world border.
- The caster is granted **Invisibility for 5 minutes**.
- All current-sequence abilities are sealed for a duration based on the caster's sequence.
- **Cooldown** persists through server restarts.

| Sequence | Cooldown | Ability Seal Duration |
|----------|----------|-----------------------|
| 3        | 12 hours | 15 minutes            |
| 4        | 24 hours | 30 minutes            |
