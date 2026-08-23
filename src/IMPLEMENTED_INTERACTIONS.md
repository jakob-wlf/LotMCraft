# Implemented Ability Interactions

This document outlines all currently implemented interactions between abilities using the interaction flag system in LotMCraft. These are interactions that exist in code and are actively functional — not design notes or future plans.

---

## How the Interaction System Works

When an ability is used, it fires an `AbilityUsedEvent` containing one or more **interaction flags**, a **radius**, and a **cache duration** (in ticks). These events are stored in the `InteractionHandler` and can be queried by other abilities to determine if a relevant interaction is active nearby.

**Sequence rules:** In most cases, an interaction only takes effect if the caster of the triggering ability is of **equal or higher power** (same or lower sequence number) than the affected ability's caster. Some interactions ignore sequence entirely, and one (Night Domain vs `light_strong`) requires the caster to be **strictly higher** in power (by at least 1 sequence). Death pathway abilities use an extended rule: purification affects them if the purifier is the same sequence, stronger, or **up to 1 sequence weaker** than the Death caster.

Some abilities use `postsUsedAbilityEventManually = true` in their constructor when the event needs to be posted from a spawned entity (e.g., on projectile impact) rather than on ability activation.

---

## All Interaction Flags

| Flag | Description |
|------|-------------|
| `freezing` | Frost/ice-based abilities |
| `burning` | Fire/flame-based abilities |
| `purification` | Holy/purifying abilities (primarily Sun pathway) |
| `purification_holy` | High-powered holy purification — only Flaring Sun, Pure White Light, Sword of Justice, Divine Kingdom Manifestation |
| `drought` | Weather drying effects |
| `light_source` | Any light-emitting ability |
| `light_weak` | Weaker light abilities |
| `light_strong` | Powerful light abilities (high-sequence Sun) |
| `explosion` | Explosive/concussive force |
| `poison` | Toxic/venomous abilities |
| `calming` | Soothing/pacifying effects |
| `water` | Water-based abilities |
| `water_strong` | Powerful water abilities |
| `sealing` | Spatial sealing/binding |
| `darkness` | Dark energy abilities |
| `unluck` | Misfortune/curse of bad luck |
| `morale_boost` | Morale-boosting/inspiring abilities |
| `soul_burn` | Soul-damaging fire |
| `blooming` | Life/nature bloom effects |
| `cleansing` | Spiritual cleansing |
| `corruption` | Corrupting/defiling effects |
| `blink_escape` | Spatial blink/teleport escape |
| `charm` | Mind-controlling charm |
| `petrification` | Turning targets to stone |
| `fog` | Fog/mist-based abilities |
| `curse` | Cursing abilities |
| `disease` | Disease-spreading abilities |
| `plague` | Plague-spreading abilities |
| `lightning` | Electrical/lightning abilities |
| `destruction` | Destruction-aspected abilities |
| `space_warp` | Space-warping abilities |
| `escape` | General escape mechanism |
| `death` | Death-aspected domain abilities |

---

## Abilities That Produce Flags

### Sun Pathway

| Ability | Flags |
|---------|-------|
| Flaring Sun | `purification`, `purification_holy`, `burning`, `light_source`, `light_strong`, `light_weak` |
| Divine Kingdom Manifestation | `purification`, `purification_holy`, `light_source`, `light_strong`, `light_weak` |
| Unshadowed Domain | `purification`, `light_source`, `light_strong`, `light_weak` |
| Unshadowed Spear | `purification`, `light_source`, `light_strong`, `light_weak` |
| Spear of Light | `purification`, `light_source`, `light_strong`, `light_weak` |
| Sword of Justice | `purification`, `purification_holy`, `light_source`, `light_strong`, `light_weak` |
| Pure White Light | `purification`, `purification_holy`, `light_source`, `light_strong`, `light_weak` |
| Holy Light | `purification`, `light_source`, `light_weak` |
| Holy Light Summoning | `purification`, `light_source`, `light_weak` |
| Illuminate | `purification`, `light_source`, `light_weak` |
| Fire of Light | `purification`, `light_source`, `light_weak`, `burning` |
| Purification Halo | `purification`, `light_weak` |
| Cleave of Purification | `purification`, `light_weak` |
| Light of Holiness | `purification`, `light_weak` |
| Holy Song | `purification`, `light_weak`, `morale_boost` |
| Solar Envoy | `light_strong`, `light_weak` |
| Holy Oath | `morale_boost` |

### Darkness Pathway

| Ability | Flags |
|---------|-------|
| Night Domain | `darkness` |
| Surge of Darkness | `darkness` |
| Sword of Darkness | `darkness` |
| Horror Aura | `darkness` |
| Requiem | `calming` |
| Midnight Poem | `calming` |

### Death Pathway

| Ability | Flags |
|---------|-------|
| Divine Kingdom | `death` |
| Nation of the Dead | `death` |
| Instant Death | `death` |
| Endpoint | `death` |

### Tyrant Pathway

| Ability | Flags |
|---------|-------|
| Torrential Downpour | `water`, `water_strong` |
| Tsunami | `water`, `water_strong` |
| Water Mastery | `water`, `water_strong` |
| Water Manipulation | `water` |
| Earthquake | `explosion` |
| Electromagnetic Tornado | `explosion` |
| Hurricane | `explosion` |
| Lightning Storm | `explosion` |
| Calamity Creation (Drought sub-ability) | `drought` |

### Red Priest Pathway

| Ability | Flags |
|---------|-------|
| War Cry | `morale_boost` |
| War Song | `morale_boost` |
| Provoking | `morale_boost` |
| Conquering | `morale_boost` |
| Pyrokinesis | `burning` |
| Flame Mastery | `burning` |
| Flame Authority | `burning` |
| Trap | `explosion` |
| Fog of War | `fog` |
| Weather Manipulation (Drought sub-ability) | `drought` |

### Demoness Pathway

| Ability | Flags |
|---------|-------|
| Frost | `freezing` |
| Petrification | `petrification` |
| Charm | `charm` |
| Curse | `curse` |
| Disease | `disease` |
| Plague | `plague`, `disease` |
| Black Flame | `burning`, `soul_burn` |
| Apocalypse | `destruction` |
| Structural Collapse | `destruction` |
| Disaster Manifestation — Meteor | `burning`, `explosion` |
| Disaster Manifestation — Ice Age | `freezing` |
| Disaster Manifestation — Tornados | `explosion` |

### Door Pathway

| Ability | Flags |
|---------|-------|
| Blink | `blink_escape`, `escape` |
| Sealing | `sealing` |
| Exile | `sealing` |
| Space-Time Storm | `explosion` |
| Black Hole | `space_warp` |

### Abyss Pathway

| Ability | Flags |
|---------|-------|
| Defiling Seed | `corruption` |
| Avatar of Desire | `corruption` |
| Malice Seed | `corruption` |
| Corrupting Voice | `corruption` |
| Fear Aura | `darkness` |
| Mind Fog | `fog` |
| Poisonous Flame | `burning`, `poison` |
| Toxic Smoke | `poison` |
| Flame Spells | `burning` |

### Mother Pathway

| Ability | Flags |
|---------|-------|
| Blooming Area | `blooming` |
| Life Aura | `blooming` |
| Cleansing | `cleansing` |
| Poison Creation | `poison` |
| Wrath of Nature — Lightning | `explosion` |
| Wrath of Nature — Fire | `burning` |
| Wrath of Nature — Moon | `explosion` |

### Visionary Pathway

| Ability | Flags |
|---------|-------|
| Placate | `morale_boost` |
| Mental Plague | `plague` |
| Frenzy | `corruption` |

### Wheel of Fortune / Common

| Ability | Flags |
|---------|-------|
| Blessing | `cleansing` |
| Spiritual Baptism | `cleansing` |
| Curse of Misfortune | `unluck` |

---

## Entities That Post Flags

Some abilities spawn entities that independently post interaction flags on hit or periodically during their lifetime.

| Entity | Flags | Trigger |
|--------|-------|---------|
| Lightning Entity | `lightning`, `explosion` | On impact |
| Giant Lightning Entity | `lightning`, `explosion` | On impact |
| Electric Shock Entity | `lightning` | On entity hit |
| Fireball Entity | `explosion`, `burning` | On impact |
| Flaming Spear Entity | `burning`, `explosion` | On hit (entity or block) |
| Frost Spear Entity | `freezing` | On hit (entity or block) |
| Spear of Destruction Entity | `burning`, `explosion` | On hit (entity or block) |
| Wind Blade Entity | `explosion` | On hit (entity or block) |
| Meteor Entity | `burning`, `explosion` | Every 20 ticks while active |
| Volcano Entity | `burning`, `explosion` | Every 20 ticks while active |
| Distortion Field Entity | `space_warp`, `burning` | Every tick while active |
| Fire Raven Entity | `burning` (+ `explosion` at position targets) | On approach to target |
| Spear of Light Projectile | *(uses parent ability's flags)* | On hit |
| Unshadowed Spear Projectile | *(uses parent ability's flags)* | On hit |
| Justice Sword Entity | *(uses parent ability's flags)* | On ground impact |

---

## Implemented Interactions by Flag

Each section below lists every ability or entity that **reacts** when a given flag is detected nearby.

### `purification`

Purification is the most widely-effective flag, primarily produced by Sun pathway abilities. It counters corruption, mental effects, diseases, darkness, and more.

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Defiling Seed | Abyss | Corruption is completely removed from afflicted entities (handled in InteractionHandler) |
| Toxic Smoke | Abyss | Smoke cloud is completely cancelled |
| Poisonous Flame | Abyss | Poison effect from flame is neutralized |
| Malice Seed | Abyss | Malice Seed effect is purified |
| Avatar of Desire | Abyss | Avatar effect is cancelled |
| Demonic Spells | Abyss | Spells are cancelled |
| Fear Aura | Abyss | Fear aura is cancelled |
| Mind Fog | Abyss | Fog effect is completely cancelled |
| Night Domain | Darkness | Domain is **weakened** — reduced blindness/darkness effects, fewer particles, reduced speed slowdown |
| Divine Kingdom (Death) | Death | Domain entity is cancelled and cleaned up — only reacts to `purification_holy` (Flaring Sun, Pure White Light, Sword of Justice, Divine Kingdom Manifestation); purifier must be same sequence or up to 1 sequence weaker than the Death caster |
| Nation of the Dead | Death | Domain is cancelled — only reacts to `purification_holy`; same sequence rule applies |
| Pale Eye | Death | Ability use is blocked |
| Hand of Death | Death | Ability use is blocked (all sub-modes) |
| Internal Underworld | Death | Ability use is blocked (summon and summon all) |
| Undying Seal | Death | Activation is blocked |
| Door to the Underworld | Death | Open Portal is blocked; Release still works |
| Death Envoy | Death | Ability use is blocked |
| Word of Spirit | Death | Ability use is blocked |
| Restruction | Death | Summon is blocked; Release still works |
| Spirit Channeling | Death | All sub-abilities blocked |
| Spirit Communication | Death | All sub-abilities blocked |
| Zombie Disguise | Death | Toggle is forcibly deactivated each tick while purification is active |
| Eye of Death | Death | Toggle is forcibly deactivated each tick while purification is active |
| Sword of Darkness | Darkness | Darkness attack is purified |
| Horror Aura | Darkness | Horror aura is cancelled, affected entities are freed |
| Charm | Demoness | Charm effect is broken |
| Disease | Demoness | Disease damage tick is completely bypassed |
| Plague | Demoness | Plague damage tick is completely bypassed |
| Poison Creation | Mother | Poison effect is cancelled |
| Mental Plague | Visionary | Duration reduced from 10 seconds to 2 seconds |
| Battle Hypnosis (Weaken) | Visionary | Weakness effect removed, weaken modifier cancelled |
| Battle Hypnosis (Freeze) | Visionary | Movement slowdown removed, ability usage re-enabled |

### `purification_holy`

A subset of purification produced only by Flaring Sun, Pure White Light, Sword of Justice, and Divine Kingdom Manifestation. Used exclusively to interact with Death's two domain abilities, which are immune to ordinary purification.

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Divine Kingdom (Death) | Death | Domain entity is cancelled and cleaned up |
| Nation of the Dead | Death | Domain is cancelled — all effects and overlay immediately removed |

**Sequence rule:** The purifier must be the same sequence or up to 1 sequence weaker than the Death domain caster.

---

### `burning`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Toxic Smoke | Abyss | Smoke **explodes** with massive damage and fire particles |
| Thread Manipulation — Cocoon | Demoness | Cocoon breaks — removes slowdown, damage resistance, and regeneration |
| Thread Manipulation — Binding | Demoness | Binding breaks — removes slowdown, weakness, and dig slowdown |
| Hair Entanglement | Darkness | Entanglement breaks with fire particles and sound effects |
| Plant Controlling | Mother | Trapped entity breaks free with fire effects |
| Poison Creation | Mother | Poison area explodes with damage |
| Frost (toggle) | Demoness | Frost/freeze effect is melted |

### `freezing`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Torrential Downpour | Tyrant | Downpour becomes frozen (handled directly in InteractionHandler) |
| Tsunami Entity | Tyrant | Tsunami freezes — stops moving and dealing damage for up to 5 seconds |
| Water Manipulation | Tyrant | Water manipulation is frozen/disrupted |
| Water Mastery | Tyrant | Water control is frozen/disrupted |

### `drought`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Torrential Downpour | Tyrant | Downpour is completely cancelled (handled directly in InteractionHandler) |

### `light_source`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Invisibility | Demoness | Nearby entities can target the invisible player (position revealed) |
| Shadow Concealment | Demoness | Concealment is broken/revealed |
| Fog of War | Red Priest | Blindness effect is removed/prevented |
| Midnight Poem | Darkness | Damage multiplier reduced from 1.0× to 0.4× (60% reduction) |

### `light_strong`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Invisibility | Demoness | Invisibility completely removed (entity becomes visible, effect stripped) |
| Shadow Concealment | Demoness | Concealment is broken |
| Night Domain | Darkness | Domain is **completely cancelled** (requires caster to be strictly higher sequence by at least 1) |
| Nation of the Dead | Death | All domain effects suspended for that tick (Sun caster may be up to 1 sequence weaker than Death caster) |
| Surge of Darkness | Darkness | Darkness surge is cancelled |
| Demonic Spells | Abyss | Spells are cancelled |

### `darkness`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Flaring Sun | Sun | Sun entity becomes "darkened" — stops damaging entities, only emits smoke particles |

### `explosion`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Petrification | Demoness | Petrification process is disrupted/cancelled |
| Sealing | Door | Seal is broken — removes movement slowdown, restores disabled abilities, restores AI |
| Requiem | Darkness | Calming effect is disrupted |
| Siren Song | Tyrant | Song stops working — no more debuffs, buffs, or damage applied |
| Concealed Domain Entity | Darkness | Domain is completely discarded/cancelled |

### `morale_boost`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Awe | Visionary | Awe debuff removed — movement slowdown and weakness effects cleared (entity-specific check) |
| Horror Aura | Darkness | Horror aura is cancelled, entities are freed (entity-specific check) |

### `cleansing`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Disease | Demoness | Disease damage tick is completely bypassed |
| Plague | Demoness | Plague damage tick is completely bypassed |
| Curse | Demoness | Curse effect is cleansed (entity-specific check) |

### `blooming`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Disease | Demoness | Disease damage reduced to 0.4× multiplier |
| Plague | Demoness | Plague damage reduced to 0.4× multiplier |

### `corruption`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Life Aura | Mother | Aura is cancelled/weakened |

### `calming`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Desire Control | Abyss | Desire Control is cancelled/prevented from affecting target |
| Mind Fog | Abyss | Fog effect is completely cancelled |

### `sealing`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Mind Fog | Abyss | Fog effect is completely cancelled |
| Fear Aura | Abyss | Fear aura is cancelled |

### `water`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Poisonous Flame | Abyss | Flame is extinguished immediately with smoke particles |

### `blink_escape`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Steel Mastery | Red Priest | Target escapes the steel binding (entity-specific check) |
| Wind Manipulation | Tyrant | Entity escapes wind manipulation (entity-specific check) |

### `escape`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Thread Manipulation — Binding | Demoness | Entity escapes thread binding (entity-specific check) |
| Hair Entanglement | Darkness | Entity breaks free from entanglement (entity-specific check) |
| Plant Controlling | Mother | Trapped entity breaks free (entity-specific check) |

### `destruction`

| Reacting Ability | Pathway | Effect When Triggered |
|-----------------|---------|----------------------|
| Concealed Domain Entity | Darkness | Domain is completely discarded/cancelled |

### Flags with no current reactions

The following flags are produced by abilities but no ability currently reacts to them:

- `water_strong` — Produced by Torrential Downpour, Tsunami, Water Mastery
- `lightning` — Produced by Lightning/Giant Lightning/Electric Shock entities
- `poison` — Produced by Toxic Smoke, Poison Creation, Poisonous Flame
- `soul_burn` — Produced by Black Flame
- `petrification` — Produced by Petrification
- `charm` — Produced by Charm
- `fog` — Produced by Fog of War, Mind Fog
- `curse` — Produced by Curse
- `disease` — Produced by Disease, Plague
- `plague` — Produced by Plague, Mental Plague
- `unluck` — Produced by Curse of Misfortune
- `space_warp` — Produced by Black Hole, Distortion Field Entity
- `light_weak` — Produced by many Sun abilities
- `death` — Produced by Divine Kingdom, Nation of the Dead (no ability currently reacts to this flag directly)

These flags are available for future interaction implementations or may be used for conditional logic within the abilities that produce them.

---

## Cross-Pathway Interaction Summary

### Sun ↔ Death
- **All Death active abilities** are blocked or stopped by any `purification` ability if the purifier is the same sequence, stronger, or up to 1 sequence weaker than the Death caster
- **Divine Kingdom and Nation of the Dead** are immune to ordinary purification — only `purification_holy` (Flaring Sun, Pure White Light, Sword of Justice, Divine Kingdom Manifestation) can cancel them, subject to the same sequence rule (purifier must be same or up to 1 weaker)
- Toggle abilities (Zombie Disguise, Eye of Death) are forcibly deactivated each tick while purification is present nearby

### Sun ↔ Darkness
- Sun abilities (`purification`, `light_strong`, `light_source`) counter nearly all Darkness pathway abilities
- `light_strong` completely cancels Night Domain (if strictly higher sequence); `purification` weakens it
- `purification` cancels Horror Aura and Sword of Darkness
- `light_strong` cancels Surge of Darkness
- `light_source` weakens Midnight Poem damage
- In reverse, `darkness` can darken the Flaring Sun, reducing its effectiveness

### Sun ↔ Abyss
- `purification` cleanses Defiling Seed, cancels Toxic Smoke, neutralizes Poisonous Flame poison, purifies Malice Seed, cancels Avatar of Desire, Demonic Spells, Fear Aura, and Mind Fog
- `light_strong` also cancels Demonic Spells

### Sun ↔ Demoness
- `purification` breaks Charm, bypasses Disease/Plague damage ticks
- `light_strong` completely removes Invisibility
- `light_source` reveals invisible and shadow-concealed entities

### Burning ↔ Control/Restraint Effects
- `burning` breaks Thread Manipulation (cocoon and binding), Hair Entanglement, and Plant Controlling restraints
- `burning` causes Toxic Smoke to explode and Poison Creation to detonate
- `burning` melts Frost effects

### Freezing ↔ Water
- `freezing` freezes Torrential Downpour, Tsunami entities, and disrupts Water Manipulation/Mastery
- `drought` completely cancels Torrential Downpour

### Explosion ↔ Containment/Stasis
- `explosion` breaks Petrification, Sealing, Requiem calming, Siren Song, and destroys Concealed Domains

### Morale ↔ Fear/Awe
- `morale_boost` counters Awe (Visionary) and Horror Aura (Darkness)

### Cleansing/Blooming ↔ Afflictions
- `cleansing` completely bypasses Disease and Plague damage and cleanses Curses
- `blooming` reduces Disease and Plague damage to 40%

### Escape Mechanics
- `blink_escape` escapes Steel Mastery and Wind Manipulation bindings
- `escape` breaks Thread Manipulation, Hair Entanglement, and Plant Controlling restraints

### Calming/Sealing ↔ Mental Effects
- `calming` cancels Desire Control and Mind Fog
- `sealing` cancels Mind Fog and Fear Aura