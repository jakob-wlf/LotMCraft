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

### Endpoint
**Sequence Requirement:** 0  
**Spirituality Cost:** 40,000  
**Cooldown:** 4 minutes  
*(Cannot be copied, replicated, or stolen)*

- **Targeting Range:** 5 blocks (single target)

Blocked entirely if `purification` is active nearby (caster must be same sequence or up to 1 sequence weaker than any purifier present, or the cast fails).

On hit, applies the **Endpoint** marker to the target permanently (does not expire on its own):
- **All healing is blocked** while the marker is active — this includes natural regeneration, potions, totems, and any ability or effect that directly sets the target's health, not just standard heal events.
- The marker is checked continuously; if `purification` of sufficient strength (same sequence or up to 1 sequence weaker than the caster who applied it) comes within range of the marked target, the marker is cured and removed.
- Otherwise, the only way to remove it is the target's death.

---

### Soul Control
**Sequence Requirement:** 1  
**Spirituality Cost:** 10,000  
**Cooldown:** 45 seconds  
*(Cannot be copied, replicated, or stolen; not usable in artifacts)*

- **Targeting Range:** 20 blocks (line-of-sight)

Blocked entirely if `purification` is active nearby (same sequence-weakness rule as Endpoint). Fails outright (no effect) if the target is a **stronger** sequence than the caster.

**Instant Kill:**
- Target is **2+ sequences weaker** than the caster: always instant kill.
- Target is **1 sequence weaker**: **50%** instant kill chance.
- Target is the **same sequence**: **25%** instant kill chance.
  - Exception: caster and target both at **Sequence 0** — always fails (0% chance), forcing the drain outcome below.

**On Failed Kill (or the guaranteed-fail Sequence 0 vs Sequence 0 case):**
- True damage equal to **20%** of the target's max HP.
- Sanity drained by a flat **40%** of the target's max sanity/Acting cap.

---

### Death Decree
**Sequence Requirement:** 1  
**Spirituality Cost:** 15,000  
*(Cannot be copied, replicated, or stolen; not usable by NPCs or in artifacts)*

- **Targeting Range:** 3 blocks (single target)

Blocked entirely if `purification` is active nearby (same sequence-weakness rule as Endpoint).

On hit:
- Target is **2+ sequences weaker** than the caster: instant kill (true damage equal to max HP).
- Target is **stronger** or **1 sequence weaker**: too tough to kill outright — deals true damage equal to **50%** of max HP instead.
- Target is the **same sequence**: applies a stacking Death Decree mark (a small `hurt()` call accompanies each stack so it counts as combat).
  - At **3 stacks**, the target is instantly killed (true damage equal to max HP) and the mark is consumed.

---

### Divine Kingdom
**Sequence Requirement:** 1  
**Spirituality Cost:** 20,000  
**Cooldown:** 5 minutes  
*(Cannot be copied, replicated, or stolen)*
*(Currently disabled — commented out of ability registration)*

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
*(Cannot be copied, replicated, or stolen; not usable in artifacts)*

- **Radius:** `35 × max(multiplier/4, 1)` blocks (scales with multiplier)
- **Duration:** 85 seconds (1,700 ticks)
- **Effect Interval:** Every tick; damage every 20 ticks (once per second)

Blocked/ended early if `purification_holy` of sufficient strength (same sequence or up to 1 sequence weaker than the caster) is active nearby — the domain is torn down immediately.

**Substitution Block:**
- All non-allied entities currently inside the domain are marked as substitution-suppressed for that tick — they cannot use Paper Figurine Substitute (Fool), Door Substitution (Door), or Mirror Substitute (Demoness) to escape death while inside.
- The suppression set is refreshed every tick and fully cleared when the domain ends.

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

### Enslavement
**Sequence Requirement:** 2  
**Spirituality Cost:** 6,000  
**Cooldown:** 30 seconds  
*(Cannot be copied, replicated, or stolen; not usable in artifacts)*

- **Targeting Range:** 20 blocks (line-of-sight)

Four selectable modes:

**Enslave**
- The caster may only have **one enslaved target at a time** — the cast fails if they already have one (release or kill the current slave first).
- At Sequence 2, the target must be a Beyonder on the **Death** pathway. At Sequence 1, the pathway restriction is lifted — any target (any pathway, or a non-Beyonder) is valid.
- The target must be **weaker** (higher sequence) than the caster, or the cast fails.
- Marks the target as enslaved to the caster. A target can only be enslaved by one master at a time.

**Seal Abilities** *(target must already be enslaved by the caster)*
- Disables all Beyonder ability usage for the enslaved target indefinitely, until released.

**Kill**
- Instantly kills the enslaved target (true damage equal to their max HP) and clears the enslavement.

**Release**
- Removes the enslavement mark (and lifts the ability seal if it was applied), restoring the target's free will.

**Passive restriction:**
- While enslaved, the target cannot deal damage to their master or to anyone on their master's ally list — all such damage is blocked outright, regardless of the method used.

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
**Spirituality Cost:** 128 every 5 ticks (0.25s) — 512/sec while active  
*(Toggle — not shareable)*

At Sequence 3 (15,000 max spirituality, 180/sec regen), sustaining the toggle continuously drains the pool in roughly **45 seconds**.

While active:
- Every melee/ability hit the caster lands deals bonus true-ish damage on top of the normal hit, added directly to the damage event:
  - Same sequence as victim: **+30%** of the victim's max HP.
  - Each sequence the victim is weaker (higher sequence number): **+10%** more per step.
  - Each sequence the victim is stronger: **−10%** per step (no bonus below 0%).
- Player victims get a large soul/reverse-portal particle-and-sound burst on hit; non-player victims get a smaller soul burst and a Wither hurt sound.
- Automatically deactivates if `purification` of sufficient strength comes within range (same sequence-weakness rule as Endpoint).

---

### Hand of Life
**Sequence Requirement:** 3  
**Spirituality Cost:** 2000  
**Cooldown:** 60 seconds  
*(Not shareable)*

Blocked entirely if `purification` is active nearby (same sequence-weakness rule as Endpoint).

Two selectable modes:

**Right Hand — Self**
- Heals the caster for **25%** of their max HP instantly.

**Right Hand — Others** *(targeted)*
- **Targeting Range:** 30 blocks (line-of-sight)
- Heals the targeted entity for **25%** of their max HP instantly.

---

### Internal Underworld
**Sequence Requirement:** 5  
**Spirituality Cost:** 400  
*(Cannot be copied, replicated, or stolen; not usable by NPCs or in artifacts)*

Requires Death pathway at Sequence 5 or stronger to actually capture (checked at interaction time, not just cast time).

**Soul Capacity by Sequence:**

| Sequence | Max Stored Souls |
|----------|-----------------|
| 5        | 5               |
| 4        | 15              |
| 3        | 20              |
| 2        | 35              |
| 1        | 45              |
| 0        | 53              |

Four selectable modes:

**Capture**
- Arms "capture mode" for the caster's next entity interaction (right-click); the mode is consumed whether or not the attempt succeeds.
- Right-clicking an eligible entity attempts capture:
  - **Eligible entities:** most undead (Zombie, Husk, Drowned, Zombie Villager, Zombified Piglin, Skeleton, Wither Skeleton, Stray, Phantom, Vex, Hoglin), most spirit mobs (Dervish, Blue Wizard, Malmouth, Translucent Wizard, Ghost), and **ghost beyonders** (see below).
  - Fails immediately if storage is already full for the caster's sequence.
  - **Ghost beyonders additionally require** the caster to hold a Beyonder Characteristic item matching the ghost's exact pathway and sequence — capture fails (with a message) if it's missing, and the matching item is **consumed** on any capture attempt (success or failure).
  - **Capture chance:** `55% + 5% × caster's sequence` — counterintuitively *weaker* casters (higher sequence number) have better odds: 80% at Seq 5 down to 55% at Seq 0.
  - On success: the entity is discarded and its data (entity type, display name, full NBT) is stored.
  - On failure: normal mobs simply escape (no permanent loss). **Ghost beyonders are destroyed outright on a failed attempt** — they only get one shot at capture — and the consumed characteristic item is refunded to the caster.

**Release** *(opens a GUI)*
- Lists stored souls as player-head items; click one to summon it near the caster as a subordinate ally to any other currently-summoned souls.
- A "Release All" item summons every stored soul at once.
- A "Discard Mode" toggle switches clicks to permanently deleting the selected soul instead of summoning it.
- Summoned souls spawn with `VoidSummoned` and an internal "underworld soul" tag (always treated as undead for other effects) and have invisibility stripped if present.

**Release All** *(direct cast, no GUI)*
- Immediately summons every currently stored soul as described above.

**Recall**
- Instantly discards all of the caster's currently-summoned active souls and returns them to storage (subject to the capacity cap).
- Also triggered automatically on logout; on the caster's death, active souls are discarded outright instead of returned to storage.

**Ghost Beyonders:** killing a Beyonder NPC has a chance to leave behind an invisible "ghost" copy of it (same pathway/sequence/skin) that lingers in the world. Ghosts are only visible via Eye of Death's glow highlight and only capturable through this ability's Capture mode, consuming a matching characteristic item.

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
- Highlights the looked-at entity for the caster's HUD within **40 blocks**.
- If the looked-at entity is a **ghost beyonder** (an invisible remnant sometimes left behind when a Beyonder NPC dies — see Internal Underworld), it glows for the caster only, for as long as they keep looking at it.
- **+35% damage** to all undead (including summoned Internal Underworld souls) and spirit entities while active.

Deactivates automatically if `purification` of sufficient strength comes within range (same sequence-weakness rule as Endpoint).

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
