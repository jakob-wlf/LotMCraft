# Justiciar Pathway — Abilities Reference

## Overview

The Justiciar pathway focuses on law enforcement, restriction, and judgment. Justiciars excel at controlling zones, restricting enemy actions, and delivering swift punishment to those who break their laws. Their abilities scale with sequence rank (lower sequence number = higher rank).

---

## Active Abilities

### Ancient Court of Judgment
- **Sequence Required:** 0
- **Spirituality Cost:** 10,000
- **Cooldown:** 2 minutes
- **Zone Radius:** 120 blocks | **Duration:** 2 minutes
- **Cannot be copied, replicated, or used by NPCs**

Summon the Ancient Court of Judgment, a massive permanent tribunal zone. Every 10 seconds, the Court randomly selects a new Prohibition from a wide list and enforces it on all entities within 120 blocks (except the caster). The active Prohibition rotates continuously until the Court expires.

**Possible Court Prohibitions:**

| Prohibition | Enforcement |
|---|---|
| Moving | Zeroes all entity velocity each tick |
| Running | Applies Slowness VI and disables sprinting |
| Breathing | Drains air supply to 0 each tick (restored when lifted) |
| Regeneration | Removes Regeneration effect each tick |
| Hostile Mobs | Pushes all hostile mobs out of the zone |
| Mobs | Pushes all non-player entities out of the zone |
| Undead | Pushes all undead entities out of the zone |
| Escaping | Pushes entities near the zone boundary back inward |
| Teleporting | Prevents teleportation |
| Projectiles | Blocks projectile firing |
| Sleeping | Prevents sleeping |
| Eating | Blocks food consumption |
| Drinking | Blocks potion drinking |
| Building | Blocks block placement |
| Destruction | Blocks block breaking |
| Explosions | Prevents explosions |
| Fall Damage | Disables fall damage |
| Speaking | Prevents chat |
| Swapping Abilities | Prevents ability swapping |
| Healing | Blocks all healing |
| Positive Effects | Blocks beneficial effects |
| Negative Effects | Blocks harmful effects |
| Interaction | Prevents entity/block interaction |
| Blinking | Prevents short-range teleportation |
| Concealment | Prevents invisibility |
| Sealing | Prevents ability sealing |

---

### World Judgment
- **Sequence Required:** 0
- **Spirituality Cost:** 20,000
- **Cooldown:** 45 seconds
- **Interaction Radius:** 30 blocks
- **Cannot be copied, replicated, or used by NPCs**

Designate a single target for World Judgment. Each time the designated target violates a Prohibition or Law, the judgment escalates in severity. Re-cast to release the target early. The target is notified when designated and when each tier triggers.

**Escalating Tiers:**

| Tier | Effect |
|---|---|
| 1 | 15 magic damage |
| 2 | 15 magic damage + Slowness IV (10 sec) |
| 3 | 15 magic damage + Slowness IV (10 sec) + all beneficial effects stripped |
| 4 | 15 magic damage + full paralysis (Slowness 127 + Levitation, 10 sec) |
| 5+ | Instant death |

---

### Order Proxy
- **Sequence Required:** 1
- **Spirituality Cost:** 200
- **Cooldown:** 20 seconds
- **Interaction Radius:** 50 blocks
- **Type:** Selectable (choose one variant per cast)

Act as a proxy of Order, invoking permanent prohibitions and laws or sacrificing yourself for overwhelming power. Choose one effect:

| Variant | Effect |
|---|---|
| Order Sacrifice | Grants a 3.0× ability multiplier boost and full damage immunity for 15 sec; caster is killed after the 15 sec expire |
| Prohibition: Resurrecting | Places a **permanent** zone (radius 50) prohibiting resurrection; max 3 zones |
| Prohibition: Dying | Places a **permanent** zone (radius 50) prohibiting death; max 3 zones |
| Prohibition: Demigods | Places a **permanent** zone (radius 50) prohibiting demigod-tier entities; max 3 zones |
| Law: Combat | Places a **permanent** law zone (radius 15) disabling combat; max 2 zones |
| Law: Losing Control | Places a **permanent** law zone (radius 15) preventing Losing Control; max 2 zones |

Re-casting while standing inside an existing zone of the same type removes it. Prohibition zones broadcast to players within 50 blocks. Order Sacrifice also removes all Losing Control effects during the active window.

---

### Individual Balance
- **Sequence Required:** 2
- **Spirituality Cost:** 2,000
- **Cooldown:** 40 seconds
- **Interaction Radius:** 20 blocks (optimal: 10 blocks)
- **Duration:** 60 sec (1200 ticks)

Impose individual balance on a single Beyonder target. The target's abilities are sealed for the duration. Broadcasts a message to players within 60 blocks upon activation. Cannot target non-Beyonders.

---

### Exile of Balance
- **Sequence Required:** 2
- **Spirituality Cost:** 1,200
- **Cooldown:** 60 seconds
- **Interaction Radius:** 40 blocks

Assess the power balance between all nearby Beyonders and forcibly exile members of the dominant side until both sides are within 10% of each other in total power (measured as the sum of (10 − sequence) per Beyonder).

Exiled entities are removed from active participation for a random duration between **2 minutes and 4 minutes**. The caster can never be exiled by this ability. Enemies are sorted by power, with the strongest exiled first. If both sides are already balanced (within 10%), the ability fizzles. Broadcasts a pale blue ring effect on activation.

---

### Sword of Judgment
- **Sequence Required:** 3
- **Spirituality Cost:** 400
- **Cooldown:** 5 seconds
- **Interaction Radius:** 20 blocks (optimal: 10 blocks)

Issue a direct Judgment that cannot be avoided. Spawns a JudgmentSwordEntity 15 blocks above the target, which falls and deals **50% of the target's max health** on impact.

**Success Chance:** 40% base + 10% per time the target previously resisted a Prohibition.  
On a successful hit, the target's Prohibition resistance count is cleared. Creates a gold-white ring effect and camera shake on impact.

---

### Delivering Judgment
- **Sequence Required:** 3
- **Spirituality Cost:** 300
- **Cooldown:** 5 seconds

Teleport instantly to the entity that has resisted your Prohibition abilities the most times. Requires at least one entity to have previously resisted a Prohibition zone. Uses a waypoint visual effect on arrival.

---

### Balancing
- **Sequence Required:** 3
- **Spirituality Cost:** 1,600
- **Cooldown:** 30 seconds
- **Zone Radius:** 120 blocks | **Duration:** 3 minutes

Declare Balance over the lands. Within the zone, all Beyonders' ability multipliers are equalized toward the target value of 2.625 every 5 ticks — both buffs and penalties are neutralized. Broadcasts a message to all nearby players when declared.

---

### Law
- **Sequence Required:** 4
- **Spirituality Cost:** 400
- **Cooldown:** 5 seconds
- **Interaction Radius:** 40 blocks
- **Type:** Selectable (choose one variant per cast)

Declare a Law that reshapes the rules of engagement. Choose one effect:

| Variant | Effect |
|---|---|
| Weaken Mysticism, Enhance Reality | All Beyonders' (including caster's) ability multiplier reduced to 0.25× for 30 sec |
| Weaken Reality, Enhance Mysticism | All Beyonders' (including caster's) ability multiplier increased to 2.5× for 60 sec |
| Solace | Instantly kills all undead entities within 40 blocks (bypasses revival) |
| Law of Sealing | Seals the target's last used ability for 2 minutes (scales with multiplier) |

---

### Execution
- **Sequence Required:** 4
- **Spirituality Cost:** 1,200
- **Cooldown:** 40 seconds
- **Interaction Radius:** 20 blocks
- **Fail Chance:** Varies by sequence difference

Execute a target from afar via guillotine animation. If successful, the target is instantly killed and cannot be revived. Success bypasses all defenses.

| Situation | Fail Chance |
|---|---|
| Target is weaker (higher seq number) | 0% (always succeeds) |
| Same sequence | 70% |
| Target is stronger (lower seq number) | 85% |

---

### Punishment
- **Sequence Required:** 5
- **Spirituality Cost:** 150
- **Cooldown:** 5 seconds
- **Interaction Radius:** 20 blocks
- **Duration:** 5 minutes

Mark a target for Punishment (caster gains Glowing while active). Re-cast to cancel. Whenever the target commits any of the following acts, the caster gains a random buff and the target receives a random debuff (each lasting 10 sec):

| Trigger | Condition |
|---|---|
| Thorns | Marked target attacks the caster |
| Area Ability | Target uses an ability with radius ≥ 30 blocks within 60 blocks |
| Restriction | Target uses Imprison or Confinement nearby |
| Killing | Target kills any entity |
| Arson | Target places fire or soul fire blocks |

**Possible Buffs (caster):** Strength II, Speed II, Resistance II, Regeneration II, Absorption II  
**Possible Debuffs (target):** Slowness II, Weakness II, Blindness, Poison II, Wither

---

### Prohibition
- **Sequence Required:** 6
- **Spirituality Cost:** 800
- **Cooldown:** 15 seconds
- **Interaction Radius:** 40 blocks
- **Zone Radius:** 40 blocks | **Zone Duration:** 3 minutes | **Max Zones:** 3 per type

Declare a type of activity Prohibited within a zone. Beyonders of the same or higher rank can resist:
- Sequence ≤ 4 caster: 15% resist chance per resister
- Sequence > 4 caster: 40% resist chance per resister

Entities that resist are tracked; this count is used by Sword of Judgment and Delivering Judgment.

The available variants are gated by sequence rank. Higher-rank Justiciars (lower sequence number) unlock more powerful Prohibitions:

| Variant | Restriction | Unlocked At |
|---|---|---|
| Prohibit Beyonder Abilities | Blocks ability usage | Seq 6 |
| Prohibit Combat | Disables combat | Seq 6 |
| Prohibit Flying | Prevents flight | Seq 6 |
| Prohibit Item Use | Blocks item usage | Seq 6 |
| Prohibit Players | Restricts player interaction | Seq 6 |
| Prohibit Outside World | Prevents leaving the zone | Seq 6 |
| Law: Prohibit Stand-ins | Blocks stand-in mechanics | Seq > 6 only (restricted) |
| Law: Prohibit Marionette Interchange | Blocks marionette swapping | Seq > 4 only (restricted) |

---

### Imprison
- **Sequence Required:** 6
- **Spirituality Cost:** 50 (initial) + 300 per 4 sec (ongoing drain)
- **Cooldown:** 3 seconds
- **Interaction Radius:** 15 blocks

Freeze a single target completely in place. While active, the caster continuously drains spirituality. Re-cast to release the target early.

---

### Confinement
- **Sequence Required:** 6
- **Spirituality Cost:** 600
- **Cooldown:** 30 seconds
- **Interaction Radius:** 15 blocks
- **Cage Radius:** 6 blocks (hollow cube) | **Duration:** 60 sec

Erect invisible barrier walls forming a hollow cage around a targeted location. Targets inside cannot teleport out. Only one confinement can be active per caster; casting again removes the previous cage.

---

### Justice Language
- **Sequence Required:** 6
- **Spirituality Cost:** 100
- **Cooldown:** 4 seconds
- **Interaction Radius:** 20 blocks
- **Type:** Selectable (choose one variant per cast)

Speak verdicts in the Language of Justice. Choose one effect:

| Variant | Effect |
|---|---|
| Maintain Secrecy | Blindness III (30 sec) + Confusion II (30 sec) |
| Death | Reduce target's health to 50% — fail chance: (sequence + 1) / 10 (Seq 6 = 70% fail; Seq 1 = 20% fail) |
| Flog | Prepare caster's next attack to inflict bleeding (5 hits × 4% max health damage over ~5 sec) |

---

### Verdict: Exile
- **Sequence Required:** 6
- **Spirituality Cost:** 200
- **Cooldown:** 12 seconds
- **Interaction Radius:** 25 blocks

Launch a target upward with a burst of force (+3.5 Y velocity), applying Slow Falling for 10 sec. Accompanied by cone-shaped Holy Flame and Golden Note particle effects.

---

### Illusionary Torture Devices
- **Sequence Required:** 7
- **Spirituality Cost:** 100
- **Cooldown:** 1.5 seconds
- **Type:** Selectable (choose one variant per cast)

Employ illusionary torture instruments to punish and weaken targets. Choose one effect:

| Variant | Effect |
|---|---|
| Branding Iron | Places a Branding Iron (iron sword) in the caster's hand. On a fully-charged swing within `4×multiplier` blocks, deals `DamageLookup(7, 0.6)×multiplier` damage + Weakness I (10 sec) + 0.12 sanity drain. Lasts 3–15 sec depending on sequence (seq 7→3s, scales down to seq 0→15s), then vanishes. |
| Psychic Lashing | Enchants the held weapon with Sharpness V (seq ≤ 4) or Sharpness III (seq > 4). |
| Psychic Piercing | Ranged attack within `18×multiplier` blocks. Deals `DamageLookup(7, 0.8)` + up to 0.1% of target's max HP (scales with multiplier). Applies Slowness II + Weakness II (6 sec) and Confusion (5 sec). Drains 0.20 sanity. |
| Whip of Pain | Ranged attack within `12×multiplier` blocks. Deals `DamageLookup(7, 0.6)×multiplier` damage + Weakness I (4 sec). Drains 0.15 sanity. |

---

### Recognition
- **Sequence Required:** 8
- **Spirituality Cost:** 0
- **Type:** Toggle (re-cast to deactivate)
- **Can be used in artifacts**

While active, scan for a target within `40×multiplier` blocks every 10 ticks. Displays the target's name, pathway, and sequence on the action bar. Information is hidden if the target is a fellow Justiciar of equal or higher sequence, or if the caster is weaker than the target (higher sequence number).

---

### Jurisdiction
- **Sequence Required:** 8
- **Spirituality Cost:** 0
- **Type:** Toggle (re-cast to dismiss)

Establish a Jurisdiction zone with a radius of `300×multiplier` blocks centered on the caster's position. While active:
- The boundary is marked with glow particles every 10 ticks.
- The caster is notified when any player enters or leaves the zone (checked every 5 ticks).
- Eye of Order radius is **doubled** while the caster is inside their own Jurisdiction.
- Eye of Order remains passively active (no spirituality drain) while inside the Jurisdiction.

Re-casting dismisses the active zone.

---

### Eye of Order
- **Sequence Required:** 8
- **Spirituality Cost:** 36 (initial) + 3 per 10 ticks (ongoing drain)
- **Duration:** 5 minutes max (ends early if spirituality reaches 0)
- **Type:** Toggle (re-cast to deactivate)

Open the Eye of Order, revealing all entities within a radius scaled to your sequence and color-coding them by threat level. Radius scales from 15 blocks at Sequence 8 up to 250 blocks at Sequence 0.

All revealed entities glow with a color indicating their nature:

| Color | Criteria |
|---|---|
| Red | Hostile mobs, or any mob actively targeting something |
| Black | Evil, disorder, or madness-pathway entities |
| Gold | All other living entities |

Entities are also made visible (invisibility stripped). Gold, red, and black dust particles continuously mark each highlighted entity.

---

### Authority
- **Sequence Required:** 9
- **Spirituality Cost:** 30
- **Cooldown:** 3 seconds
- **Interaction Radius:** 15 blocks
- **Type:** Selectable (choose one variant per cast)

Exert the authority of an Arbiter over nearby entities. Choose one effect:

| Variant | Effect |
|---|---|
| Strip Defense | Removes all beneficial effects; applies Confusion for 8 sec |
| Slow | Applies Slowness II (20 sec) and Confusion (8 sec) |
| Armor Remove | 40% chance to drop 2 random armor pieces from target |

**Sequence 4+ Enhancement:** All variants additionally apply a stun (Movement Slowdown 127 for 2 sec).

---

## Passive Abilities

### Physical Enhancements (Justiciar)
- **Sequence Required:** 9
- **Rarity:** Uncommon

Grants scaling physical bonuses based on current sequence rank:

| Sequence | Strength | Resistance | Speed | Health | Regeneration |
|---|---|---|---|---|---|
| 9 | +1 | — | +2 | — | +1 |
| 8–7 | +2 | +4 | +2 | +5 | +2 |
| 6 | +2 | +6 | +2 | +7 | +2 |
| 5 | +2 | +8 | +2 | +9 | +2 |
| 4 | +3 | +13 | +4 | +16 | +3 |
| 3 | +3 | +14 | +4 | +17 | +3 |
| 2 | +4 | +17 | +5 | +25 | +4 |
| 1 | +4 | +18 | +5 | +30 | +4 |
| 0 | +5 | +18 | +5 | +20 | +5 |

---

### Order
- **Sequence Required:** 9
- **Rarity:** Uncommon

Subconsciously maintain Order. When any player within range commits murder (kills another player), a broadcast notification is triggered. Detection radius scales with your Beyonder multiplier: `40 × multiplier³` blocks.

---

### Enhanced Mental Attributes (Justiciar)
- **Sequence Required:** 5
- **Rarity:** Uncommon

Passive resistances against mental and harmful effects:
- **Divination Immunity:** Cannot be targeted by Divination abilities.
- **Losing Control Resistance:** Duration of Losing Control effects is reduced by 40 ticks per tick event.
- **General Harmful Effect Resistance:** All other harmful effect durations are reduced by 10 ticks per tick event.

---

### Chaos Hunting
- **Sequence Required:** 3
- **Rarity:** Uncommon

Continuously scans a 60-block radius. Beyonders who outrank you (lower sequence number) and all entities of disaster pathways are revealed with a glow outline **visible only to you**.

---

## Cross-Ability Mechanics

Several abilities interact with shared state:

| Mechanic | Set By | Read By |
|---|---|---|
| Prohibition Resistance Count | Prohibition (target resists) | Sword of Judgment (+10% success per count), Delivering Judgment (selects highest-count target) |
| Last Used Ability | Any ability use | Law: Law of Sealing |
| Solace / Execution Death Flag | Law: Solace, Execution | Revival prevention systems |
| Individually Balanced | Individual Balance | Ability use prevention systems |
| Exiled Entities | Exile of Balance | Ability use / combat systems |
| World Judgment Tier | World Judgment | WorldJudgmentHandler (escalates on Prohibition violations) |
| Balancing Zones | Balancing | Prohibition (blocks effect application inside zone) |
| Confinement Zones | Confinement | Teleport event handler |
| Ancient Court Zones | Ancient Court of Judgment | Court enforcement tick |
| Order Proxy Zones | Order Proxy | Resurrection / death / combat event handlers |
| Flog State | Justice Language: Flog | LivingDamageEvent (triggers bleeding on next hit) |

---

## Ability Unlock Summary

| Sequence Required | Abilities Unlocked |
|---|---|
| 0 | Ancient Court of Judgment, World Judgment |
| 1 | Order Proxy |
| 2 | Individual Balance, Exile of Balance |
| 3 | Sword of Judgment, Delivering Judgment, Balancing, Chaos Hunting |
| 4 | Law, Execution |
| 5 | Punishment, Enhanced Mental Attributes |
| 6 | Prohibition, Imprison, Confinement, Justice Language, Verdict: Exile |
| 7 | Illusionary Torture Devices |
| 8 | Recognition, Jurisdiction, Eye of Order |
| 9 | Authority, Physical Enhancements, Order |
