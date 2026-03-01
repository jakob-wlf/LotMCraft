# LotMCraft Developer Guide

Welcome to the **LotMCraft** developer documentation. This mod is built on **NeoForge 21.1** for **Minecraft 1.21.1** using **Java 21** and implements a Beyonder power system inspired by the webnovel *Lord of the Mysteries*.

This guide covers everything you need to know to contribute to or extend the mod.

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Naming Conventions](#2-naming-conventions)
3. [The Language File](#3-the-language-file)
4. [Ability System](#4-ability-system)
   - [Base Ability](#41-base-ability)
   - [ToggleAbility](#42-toggleability)
   - [SelectableAbility](#43-selectableability)
   - [Registering an Ability](#44-registering-an-ability)
5. [Damage Lookup](#5-damage-lookup)
6. [The Multiplier System](#6-the-multiplier-system)
7. [Utility Classes](#7-utility-classes)
   - [AbilityUtil](#71-abilityutil)
   - [ParticleUtil](#72-particleutil)
   - [VectorUtil](#73-vectorutil)
   - [AllyUtil](#74-allyutil)
   - [ServerScheduler & ClientScheduler](#75-serverscheduler--clientscheduler)
   - [StructureHelper](#76-structurehelper)
8. [Custom Effect Rendering System (VFX)](#8-custom-effect-rendering-system-vfx)
9. [BeyonderMap](#9-beyondermap)
10. [Shader Rendering System](#10-shader-rendering-system)
11. [Particle System](#11-particle-system)
12. [Ring Effect System](#12-ring-effect-system)
13. [Network & Packets](#13-network--packets)
14. [Event Handling](#14-event-handling)
15. [Keybinding System](#15-keybinding-system)

---

## 1. Project Structure

```
src/main/java/de/jakob/lotm/
├── abilities/          # All ability implementations, organized by pathway
│   ├── core/           # Base classes: Ability, ToggleAbility, SelectableAbility, AbilityHandler
│   ├── common/         # Abilities shared across pathways
│   ├── sun/            # Sun pathway abilities
│   ├── red_priest/     # Red Priest pathway abilities
│   ├── tyrant/         # Tyrant pathway abilities
│   └── ...             # Other pathway folders
├── attachments/        # Entity/item data attachments (components)
├── block/              # Custom blocks
├── datagen/            # Data generation for recipes, loot tables, etc.
├── dimension/          # Custom dimensions
├── effect/             # Custom status effects
├── entity/             # Custom entities
├── events/             # Event handlers (21 handlers)
├── gui/                # GUI screens (ability wheel, quest screens, etc.)
├── item/               # Custom items
├── network/            # Network packets for client-server communication
│   └── packets/
│       ├── toClient/   # Server → Client packets
│       └── toServer/   # Client → Server packets
├── particle/           # Custom particle types
├── potions/            # Custom potions
├── quest/              # Quest system
├── rendering/          # VFX and effect rendering
│   └── effectRendering/
├── sefirah/            # Sefirot system
├── util/               # Utility classes
│   ├── beyonderMap/    # BeyonderMap persistence system
│   ├── helper/         # Helper utilities (AbilityUtil, ParticleUtil, etc.)
│   ├── pathways/       # Pathway metadata
│   └── scheduling/     # ServerScheduler & ClientScheduler
└── LOTMCraft.java      # Main mod class (MOD_ID = "lotmcraft")

src/main/resources/assets/lotmcraft/
├── lang/               # Language files (en_us.json, de_de.json)
├── shaders/program/    # Custom shader files (.vsh, .fsh)
├── textures/           # Textures (abilities, items, gui, etc.)
│   └── abilities/      # Ability icons ({ability_id}.png)
└── ...
```

---

## 2. Naming Conventions

Consistent naming is critical throughout the mod. All identifiers use **snake_case**.

| Element | Pattern | Examples |
|---------|---------|---------|
| **Ability IDs** | `{name}_ability` | `holy_light_ability`, `pyrokinesis_ability` |
| **Pathway IDs** | `{name}` | `fool`, `sun`, `red_priest`, `white_tower` |
| **Sequence names** | `{name}` | `seer`, `attendant_of_mysteries`, `miracle_invoker` |
| **Item IDs** | `{name}` | `fool_card`, `guiding_book`, `blood` |
| **Effect IDs** | `{name}` | `asleep`, `petrification`, `mental_plague` |
| **Block IDs** | `{name}` | Standard snake_case |
| **Particle IDs** | `{name}` | `holy_flame`, `purple_flame`, `toxic_smoke` |
| **NBT keys** | `beyonder_{name}` | `beyonder_pathway`, `beyonder_sequence`, `beyonder_spirituality` |
| **Language keys (abilities)** | `lotmcraft.{ability_id}` | `lotmcraft.holy_light_ability` |
| **Language keys (descriptions)** | `lotmcraft.{ability_id}.description` | `lotmcraft.holy_light_ability.description` |
| **Language keys (sub-abilities)** | `ability.lotmcraft.{parent}.{sub}` | `ability.lotmcraft.pyrokinesis.fireball` |
| **Language keys (pathways)** | `lotm.pathway.{id}` | `lotm.pathway.fool`, `lotm.pathway.sun` |
| **Language keys (sequences)** | `lotm.sequence.{name}` | `lotm.sequence.seer` |
| **Language keys (items)** | `item.lotmcraft.{id}` | `item.lotmcraft.fool_card` |
| **Language keys (effects)** | `effect.lotmcraft.{id}` | `effect.lotmcraft.asleep` |
| **Texture paths (abilities)** | `textures/abilities/{ability_id}.png` | `textures/abilities/holy_light_ability.png` |

---

## 3. The Language File

**Location:** `src/main/resources/assets/lotmcraft/lang/en_us.json`

The language file is the **single source of truth** for all display names, descriptions, and UI text in the mod. Every user-facing string is defined here and referenced via translation keys.

### Ability Names & Descriptions

Every ability has a name entry and an optional description entry:

```json
{
  "lotmcraft.holy_light_ability": "Holy Light",
  "lotmcraft.holy_light_ability.description": "A focused beam of holy light that deals damage to enemies",

  "lotmcraft.cogitation_ability": "Cogitation",
  "lotmcraft.cogitation_ability.description": "Cogitate to replenish your Spirituality"
}
```

The `Ability` base class resolves these automatically:
- **`getName()`** → looks up `lotmcraft.{ability_id}` (e.g., `lotmcraft.holy_light_ability`)
- **`getDescription()`** → looks up `lotmcraft.{ability_id}.description` — returns `null` if no entry exists

### Sub-Ability Names (SelectableAbility)

Sub-abilities of a `SelectableAbility` use a different pattern:

```json
{
  "ability.lotmcraft.pyrokinesis.fireball": "Fireball",
  "ability.lotmcraft.pyrokinesis.wall_of_fire": "Wall of Fire",
  "ability.lotmcraft.pyrokinesis.fire_ravens": "Fire Ravens",
  "ability.lotmcraft.pyrokinesis.flaming_spear": "Flaming Spear",
  "ability.lotmcraft.pyrokinesis.flame_wave": "Flame Wave"
}
```

These keys are the exact strings returned by `getAbilityNames()` in a `SelectableAbility`.

### Pathways & Sequences

```json
{
  "lotm.pathway.fool": "Fool",
  "lotm.pathway.sun": "Sun",
  "lotm.pathway.red_priest": "Red Priest",

  "lotm.sequence.seer": "Seer",
  "lotm.sequence.attendant_of_mysteries": "Attendant of Mysteries",
  "lotm.sequence.miracle_invoker": "Miracle Invoker"
}
```

### Items, Effects, Blocks

```json
{
  "item.lotmcraft.fool_card": "Fool Card",
  "effect.lotmcraft.asleep": "Asleep",
  "block.lotmcraft.example_block": "Example Block"
}
```

> **Important:** When adding a new ability, item, or effect, always add the corresponding entry in the language file, or the game will display the raw translation key instead of a readable name.

---

## 4. Ability System

All abilities extend one of three base classes located in `de.jakob.lotm.abilities.core`:

### 4.1 Base Ability

**Class:** `Ability` (abstract)

This is the foundation for all one-shot abilities. It handles cooldowns, spirituality costs, pathway requirements, and lifecycle management.

#### Creating a Simple Ability

```java
public class FireOfLightAbility extends Ability {

    public FireOfLightAbility(String id) {
        super(id, 0.75f); // 0.75 seconds cooldown (converted to ticks internally)
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 7); // Requires Sun pathway, sequence 7 or lower
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 23; // Costs 23 spirituality per use
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return; // Return on client — ability logic runs server-side only

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 10, 1.4f);

        // Spawn particles
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(),
            targetPos, 140, 0.4, 0.04);

        // Deal damage — ALWAYS use DamageLookup for damage values
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5,
            DamageLookup.lookupDamage(7, 0.75) * multiplier(entity),
            targetPos, true, false, true, 0, 20 * 2);
    }
}
```

#### Key Methods

| Method | Description |
|--------|-------------|
| `onAbilityUse(Level, LivingEntity)` | **Abstract.** Your ability logic goes here. |
| `getRequirements()` | **Abstract.** Returns a map of `pathway → minimum sequence` needed. |
| `getSpiritualityCost()` | **Abstract.** How much spirituality is consumed per use. |
| `multiplier(entity)` | Returns the entity's damage multiplier (see [Multiplier System](#6-the-multiplier-system)). |
| `useAbility()` | Called by the framework — validates requirements, consumes spirituality, sets cooldown, then calls `onAbilityUse()`. Do not call `onAbilityUse()` directly. |
| `canUse(entity, ...)` | Checks cooldown, spirituality, creative mode, and disabled state. |
| `hasAbility()` | Checks whether the entity meets the pathway/sequence requirements. |
| `getTextureLocation()` | Returns `textures/abilities/{id}.png` as a `ResourceLocation`. |
| `getName()` | Returns the localized ability name from the language file. |
| `getDescription()` | Returns the localized description (or `null` if none). |

#### Configuration Flags

Set these in your constructor to change behavior:

```java
canBeUsedByNPC = true;           // Can NPCs use this ability? (default: true)
canBeCopied = true;              // Can this ability be copied by other abilities? (default: true)
canAlwaysBeUsed = false;         // Skip all validation checks? (default: false)
doesNotIncreaseDigestion = false;// Does this ability skip digestion progress? (default: false)
hasOptimalDistance = true;        // Does this ability have a preferred casting distance? (default: true)
optimalDistance = 5f;             // Preferred distance for NPC AI (default: 5)
```

#### How Requirements Work

The `getRequirements()` map uses **pathway IDs as keys** and **sequence numbers as values**. The ability is available when the entity's current sequence is **less than or equal to** the requirement value (lower sequence = stronger).

```java
// Available to Sun pathway at sequence 7 or lower (stronger)
reqs.put("sun", 7);

// Available to multiple pathways
reqs.put("sun", 5);
reqs.put("tyrant", 5);
```

---

### 4.2 ToggleAbility

**Class:** `ToggleAbility` (abstract, extends `Ability`)

Toggle abilities activate when used once and deactivate when used again. While active, they consume spirituality each tick and call a `tick()` method.

#### Creating a Toggle Ability

```java
public class WingsOfLightAbility extends ToggleAbility {

    public WingsOfLightAbility(String id) {
        super(id); // ToggleAbilities have no cooldown
        canBeUsedByNPC = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 5; // Costs 5 spirituality PER TICK while active (= 100/second at 20 tps)
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 2));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        // Called once when the ability is activated
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        // Called every tick while the ability is active
        if (entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        // Called once when the ability is deactivated
        if (entity instanceof Player player && !player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}
```

#### Lifecycle

1. **First use** → `start()` is called, ability is marked as active
2. **Each tick while active** → `prepareTick()` drains spirituality, then `tick()` is called
3. **Second use** (or spirituality runs out) → `stop()` is called, ability is deactivated

#### Static Tracking

Active toggle abilities are tracked globally per entity:

```java
// Check if a specific toggle ability is active for an entity
ToggleAbility.isActiveForEntity(entity, abilityInstance);

// Get all active toggle abilities for an entity
ToggleAbility.getActiveAbilitiesForEntity(entity);

// Cancel all active toggle abilities for an entity
ToggleAbility.cleanUp(entity);
```

---

### 4.3 SelectableAbility

**Class:** `SelectableAbility` (abstract, extends `Ability`)

Selectable abilities contain multiple sub-abilities that the player can cycle through and choose from.

#### Creating a Selectable Ability

```java
public class PyrokinesisAbility extends SelectableAbility {

    public PyrokinesisAbility(String id) {
        super(id, 0.75f); // 0.75 seconds cooldown
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        // These are language file translation keys
        return new String[]{
            "ability.lotmcraft.pyrokinesis.fireball",
            "ability.lotmcraft.pyrokinesis.flame_wave",
            "ability.lotmcraft.pyrokinesis.wall_of_fire",
            "ability.lotmcraft.pyrokinesis.fire_ravens",
            "ability.lotmcraft.pyrokinesis.flaming_spear"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> fireball(level, entity);
            case 1 -> flameWave(level, entity);
            case 2 -> wallOfFire(level, entity);
            case 3 -> fireRavens(level, entity);
            case 4 -> flamingSpear(level, entity);
        }
    }

    private void fireball(Level level, LivingEntity entity) {
        // Use DamageLookup and multiplier for damage
        double damage = DamageLookup.lookupDamage(7, 0.775) * multiplier(entity);
        // ... create and launch fireball entity
    }

    // ... other sub-ability methods
}
```

#### Sub-Ability Navigation

Players cycle through sub-abilities using keybinds:

```java
nextAbility(entity)           // Cycle forward (V key)
previousAbility(entity)       // Cycle backward (X key)
setSelectedAbility(player, i) // Set specific index
getSelectedAbility(entity)    // Get current sub-ability name
```

For NPCs, a random sub-ability is selected automatically.

> **Important:** The strings in `getAbilityNames()` must match exactly the translation keys in the language file.

---

### 4.4 Registering an Ability

All abilities are registered in `AbilityHandler.java` located at `de.jakob.lotm.abilities.core.AbilityHandler`.

Add your ability to the `registerAbilities()` method:

```java
private void registerAbilities() {
    // COMMON
    abilities.add(new CogitationAbility("cogitation_ability"));
    abilities.add(new AllyAbility("ally_ability"));

    // SUN PATHWAY
    abilities.add(new HolySongAbility("holy_song_ability"));
    abilities.add(new HolyLightAbility("holy_light_ability"));
    abilities.add(new FireOfLightAbility("fire_of_light_ability"));

    // Add your ability here:
    abilities.add(new YourAbility("your_ability"));
}
```

#### Checklist for Adding a New Ability

1. **Create the ability class** in the appropriate pathway folder under `abilities/`
2. **Register it** in `AbilityHandler.registerAbilities()`
3. **Add the language entries** in `en_us.json`:
   ```json
   "lotmcraft.your_ability": "Your Ability Name",
   "lotmcraft.your_ability.description": "What this ability does"
   ```
4. **Add an icon texture** at `textures/abilities/your_ability.png`
5. For `SelectableAbility`, also add sub-ability entries:
   ```json
   "ability.lotmcraft.your_ability.sub_one": "Sub One",
   "ability.lotmcraft.your_ability.sub_two": "Sub Two"
   ```

---

## 5. Damage Lookup

**Class:** `DamageLookup` — located at `de.jakob.lotm.util.helper.DamageLookup`

> **Rule: Always use `DamageLookup` for damage values.** Never hardcode damage numbers in abilities. This ensures consistent damage scaling across all sequences.

### How It Works

`DamageLookup` provides interpolated damage values based on **sequence** (which tier of power the ability belongs to) and **scale** (where within that tier the damage falls).

#### Damage Ranges by Sequence

| Sequence | Min Damage | Max Damage | Power Level |
|----------|-----------|-----------|-------------|
| 9 | 3 | 8 | Weakest |
| 8 | 4 | 9 | |
| 7 | 6.35 | 13.2 | |
| 6 | 8 | 15.2 | |
| 5 | 8.3 | 17.1 | |
| 4 | 12.4 | 24.4 | |
| 3 | 14 | 27 | |
| 2 | 27 | 55 | |
| 1 | 33 | 69.5 | |
| 0 | 33 | 69.5 | Strongest |

#### Scale Parameter

The `scale` parameter controls where the damage falls within (and beyond) the current sequence's range:

| Scale | Meaning |
|-------|---------|
| `-1` | Previous (weaker) sequence's minimum |
| `0` | Current sequence's minimum |
| `0.5` | Midpoint of current sequence's range |
| `1` | Current sequence's maximum |
| `2` | Next (stronger) sequence's maximum |

Values between these anchors are linearly interpolated.

#### Usage

```java
// Basic damage for a sequence 7 ability at 75% of its range
double damage = DamageLookup.lookupDamage(7, 0.75);

// Weaker variant of a sequence 5 ability
double weakDamage = DamageLookup.lookupDamage(5, 0.2);

// Strong variant that exceeds normal range
double strongDamage = DamageLookup.lookupDamage(7, 1.5);

// Always multiply by the entity's multiplier
double finalDamage = DamageLookup.lookupDamage(7, 0.75) * multiplier(entity);
```

#### DPS Calculation

For abilities that deal damage over time:

```java
// Calculates damage per hit for a total duration
// sequence=5, scale=0.85, hits every 10 ticks, total 60 ticks
double dps = DamageLookup.lookupDps(5, 0.85, 10, 60);
```

---

## 6. The Multiplier System

**Class:** `BeyonderData` — located at `de.jakob.lotm.util.BeyonderData`

The multiplier system scales damage based on the entity's Beyonder sequence. It is accessed via the `multiplier(entity)` method available in all `Ability` subclasses.

### Base Multipliers

| Sequence | Multiplier | Description |
|----------|-----------|-------------|
| 9 | 1.0× | Base (weakest) |
| 8 | 1.0× | Base |
| 7 | 1.1× | Slight boost |
| 6 | 1.25× | |
| 5 | 1.4× | |
| 4 | 1.85× | |
| 3 | 2.15× | |
| 2 | 3.25× | |
| 1 | 4.25× | |
| 0 | 9.0× | Strongest |

### Temporary Modifiers

The multiplier system supports temporary modifiers that stack multiplicatively:

```java
// Add a 50% damage boost for 5 seconds
BeyonderData.addModifierWithTimeLimit(entity, "my_buff_id", 1.5, 5000);

// Add a 30% damage reduction for 3 seconds
BeyonderData.addModifierWithTimeLimit(entity, "my_debuff_id", 0.7, 3000);

// Remove a modifier early
BeyonderData.removeModifier(entity, "my_buff_id");
```

Modifiers automatically expire based on their timeout and are cleaned up when `getMultiplier()` is called.

### How It All Fits Together

Every damage calculation in an ability should follow this pattern:

```java
double damage = DamageLookup.lookupDamage(sequence, scale) * multiplier(entity);
```

This ensures damage:
1. Is appropriate for the ability's sequence (via `DamageLookup`)
2. Scales with the entity's actual power level (via base multiplier)
3. Incorporates any temporary buffs/debuffs (via modifier system)

---

## 7. Utility Classes

### 7.1 AbilityUtil

**Location:** `de.jakob.lotm.util.helper.AbilityUtil`

The most important utility class for ability development. Provides targeting, damage application, sequence comparison, and geometric helpers.

#### Targeting

```java
// Raycast to find the entity the player is looking at
LivingEntity target = AbilityUtil.getTargetEntity(entity, maxDistance, inflateHitbox);

// Raycast to find a location (entity center or block surface)
Vec3 location = AbilityUtil.getTargetLocation(entity, maxDistance, inflateHitbox);

// Raycast to find a block
BlockHitResult block = AbilityUtil.getTargetBlock(entity, maxDistance);
```

#### Damage Checks & Application

```java
// Check if source is allowed to damage target (respects allies, marionettes, creative mode)
boolean canDamage = AbilityUtil.mayDamage(source, target);

// Stricter check (also considers support abilities)
boolean canTarget = AbilityUtil.mayTarget(source, target, isSupportAbility);

// Damage all entities in a radius with falloff
AbilityUtil.damageNearbyEntities(
    (ServerLevel) level,
    source,               // Who's dealing the damage
    radius,               // Damage radius
    damage,               // Damage amount
    center,               // Center position
    damageSource,         // Whether to use ability damage source
    ignoreAllies,         // Skip ally checks
    applyKnockback,       // Apply knockback
    fireTicks,            // Set entities on fire (0 = no fire)
    damageCooldown        // Invulnerability ticks after damage
);

// Damage a single entity
AbilityUtil.applyDamage(source, target, damage);
```

#### Sequence Comparison

```java
// Get the sequence difference between two entities
int diff = AbilityUtil.getSequenceDifference(source, target);

// Check if target is significantly weaker/stronger
boolean weaker = AbilityUtil.isTargetSignificantlyWeaker(source, target);
boolean stronger = AbilityUtil.isTargetSignificantlyStronger(source, target);

// Get resistance factor based on sequence gap (for control abilities)
double resistance = AbilityUtil.getSequenceResistanceFactor(source, target);

// Get failure chance for abilities against higher-sequence targets
double failChance = AbilityUtil.getSequenceFailureChance(source, target);
```

#### Entity Finding

```java
// Get all living entities within radius
List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(level, center, radius);

// Get all entities (including non-living) within radius
List<Entity> all = AbilityUtil.getAllNearbyEntities(level, center, radius);
```

#### Block Geometry

```java
// Get block positions in geometric shapes
List<BlockPos> circle = AbilityUtil.getBlocksInCircle(center, radius);
List<BlockPos> outline = AbilityUtil.getBlocksInCircleOutline(center, radius);
List<BlockPos> sphere = AbilityUtil.getBlocksInSphereRadius(center, radius, filled);
List<BlockPos> ellipsoid = AbilityUtil.getBlocksInEllipsoid(center, rx, ry, rz);
```

#### UI

```java
// Display a message on the player's action bar
AbilityUtil.sendActionBar(player, Component.literal("Message"));
```

---

### 7.2 ParticleUtil

**Location:** `de.jakob.lotm.util.helper.ParticleUtil`

Server-side particle spawning with geometric patterns and animation support.

#### Basic Spawning

```java
// Spawn particles at a position
ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(),
    position, count, spread, speed);

// Spawn particles repeatedly over a duration
ParticleUtil.spawnParticlesForDuration((ServerLevel) level, particleType,
    position, durationTicks, intervalTicks, countPerSpawn, spread);
```

#### Geometric Patterns

```java
// Sphere of particles
ParticleUtil.spawnSphereParticles((ServerLevel) level, particleType,
    center, radius, count);

// Circle (flat, horizontal)
ParticleUtil.spawnCircleParticles((ServerLevel) level, particleType,
    center, radius, count);

// 3D oriented circle (any direction)
ParticleUtil.spawnCircleParticles((ServerLevel) level, particleType,
    center, direction, radius, count);

// Line of particles between two points
ParticleUtil.drawParticleLine((ServerLevel) level, particleType,
    start, end, stepSize, countPerStep);
```

#### Animated Spirals

```java
// Create spiraling particles (returns AtomicBooleans to stop individual spirals)
List<AtomicBoolean> spirals = ParticleUtil.createParticleSpirals(
    (ServerLevel) level, particleType,
    center, startRadius, endRadius,
    height, speed, density,
    durationTicks, spiralCount, delayBetweenSpirals);

// Cocoon-shaped particle animation
ParticleUtil.createParticleCocoons((ServerLevel) level, particleType,
    center, startRadius, endRadius,
    height, speed, density,
    durationTicks, cocoonCount, delayBetween);
```

---

### 7.3 VectorUtil

**Location:** `de.jakob.lotm.util.helper.VectorUtil`

Vector math utilities for 3D calculations.

```java
// Generate smooth Bézier curves between two points
List<Vec3> curve = VectorUtil.createBezierCurve(start, end, step, controlPoints);

// Get a random perpendicular vector
Vec3 perp = VectorUtil.getRandomPerpendicular(direction);

// Get a consistent perpendicular vector
Vec3 perp = VectorUtil.getPerpendicularVector(lookAngle);

// Calculate a position relative to an entity's facing direction
Vec3 pos = VectorUtil.getRelativePosition(entityPos, lookDirection,
    forward, right, up);
```

---

### 7.4 AllyUtil

**Location:** `de.jakob.lotm.util.helper.AllyUtil`

Manages bidirectional ally relationships between entities.

```java
// Create/remove ally relationships
AllyUtil.makeAllies(entity1, entity2);
AllyUtil.removeAllies(entity1, entity2);

// Query relationships
boolean allies = AllyUtil.areAllies(entity1, entity2);
boolean hasAny = AllyUtil.hasAllies(entity);
int count = AllyUtil.getAllyCount(entity);

// Bulk operations
AllyUtil.clearAllAllies(entity);
```

Ally relationships affect ability targeting — `AbilityUtil.mayDamage()` respects ally status.

---

### 7.5 ServerScheduler & ClientScheduler

**Location:** `de.jakob.lotm.util.scheduling.ServerScheduler` / `ClientScheduler`

Tick-based task scheduling for delayed and repeating operations.

```java
// One-time delayed execution
UUID taskId = ServerScheduler.scheduleDelayed(delayTicks, () -> {
    // Runs once after delay
});

// Repeated execution
UUID taskId = ServerScheduler.scheduleRepeating(initialDelay, intervalTicks, maxExecutions, () -> {
    // Runs multiple times
});

// Duration-based execution with completion callback
UUID taskId = ServerScheduler.scheduleForDuration(initialDelay, intervalTicks, durationTicks,
    () -> { /* runs each interval */ },
    () -> { /* runs on completion */ },
    serverLevel);

// Conditional loop (runs until condition is true)
AtomicBoolean stopCondition = new AtomicBoolean(false);
UUID taskId = ServerScheduler.scheduleUntil(serverLevel,
    () -> { /* runs each tick */ },
    () -> { /* runs on completion */ },
    stopCondition);

// Cancel a scheduled task
ServerScheduler.cancel(taskId);
```

`ClientScheduler` has the same API and additionally provides particle-spawning convenience methods.

---

### 7.6 StructureHelper

**Location:** `de.jakob.lotm.util.helper.StructureHelper`

Places `.nbt` structure files into the world.

```java
StructureHelper.placeEvernightChurch(serverLevel, blockPos);
```

Structures are loaded from `data/lotmcraft/structures/` via Minecraft's `StructureTemplateManager`.

---

## 8. Custom Effect Rendering System (VFX)

**Location:** `de.jakob.lotm.rendering.effectRendering/`

The VFX system renders complex visual effects in the world (explosions, beams, persistent auras, etc.) independently from Minecraft's particle system.

### Architecture

```
Server                              Client
──────                              ──────
EffectManager.playEffect()    →     VFXRenderer.addActiveEffect()
  └─ sends AddEffectPacket          └─ EffectFactory.createEffect(index)
                                        └─ ThunderExplosionEffect, HolyLightEffect, etc.
                                    └─ Renders in AFTER_PARTICLES stage
```

### Three Effect Types

| Type | Base Class | Description |
|------|-----------|-------------|
| **Static** | `ActiveEffect` | Effect at a fixed position (explosions, blasts) |
| **Directional** | `ActiveDirectionalEffect` | Effect traveling between two points (beams, projectile trails) |
| **Movable** | `ActiveMovableEffect` | Effect that follows/tracks an entity (auras, persistent effects) |

### Creating a Custom Effect

1. **Create your effect class** extending one of the three base classes:

```java
public class MyCustomEffect extends ActiveEffect {

    public MyCustomEffect(double x, double y, double z) {
        super(x, y, z, 60); // 60 ticks = 3 seconds duration
    }

    @Override
    public void render(PoseStack poseStack, float partialTick) {
        float progress = getProgress(); // 0.0 → 1.0 over duration

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                              GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Your rendering code using BufferBuilder / Tesselator
        // ...

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
```

2. **Register it in `EffectFactory.java`** by adding a new case to the switch statement:

```java
case 27 -> new MyCustomEffect(x, y, z);
```

3. **Trigger it from server code:**

```java
EffectManager.playEffect(27, x, y, z, serverLevel);
// or send to a specific player:
EffectManager.playEffect(27, x, y, z, serverPlayer);
```

### How Effects Are Rendered

`VFXRenderer` is the central client-side renderer:
- Subscribes to `RenderLevelStageEvent.Stage.AFTER_PARTICLES`
- Translates the `PoseStack` to camera-relative coordinates
- Iterates all active effects, calls `tick()` and `render()`
- Automatically removes effects when `isFinished()` returns `true`

### Movable Effects

Movable effects can be updated dynamically after creation:

```java
// Server: Create the effect
MovableEffectManager.playEffect(effectIndex, x, y, z, uuid, serverLevel);

// Server: Update position each tick
MovableEffectManager.updatePosition(uuid, newX, newY, newZ, serverLevel);

// Server: Remove when done
MovableEffectManager.removeEffect(uuid, serverLevel);
```

---

## 9. BeyonderMap

**Location:** `de.jakob.lotm.util.beyonderMap/`

The `BeyonderMap` is a **persistent data store per world** that tracks all players' Beyonder progression. It extends Minecraft's `SavedData` and is saved to the world's NBT data.

### Core Concept

```
BeyonderMap (HashMap<UUID, StoredData>)
  └─ StoredData (per player)
       ├── pathway          (String: "fool", "sun", etc.)
       ├── sequence         (Integer: 0-9, lower = stronger)
       ├── honorificName    (HonorificName: multi-line title)
       ├── trueName          (String: player's username)
       ├── msgs             (LinkedList<MessageType>: message queue)
       └── knownNames       (LinkedList<HonorificName>: known names)
```

### Key Operations

```java
BeyonderMap map = BeyonderData.beyonderMap;

// Store/retrieve player data
map.put(player, storedData);
Optional<StoredData> data = map.get(player);

// Check slot availability (game rules limit how many seq-0/1/2 exist)
boolean available = map.check("fool", 0);

// Count players in a pathway/sequence
int count = map.count("fool", 5);

// Search for players by honorific name keywords
Optional<StoredData> candidate = map.findCandidate(keywords);
```

### Building StoredData

Use the `StoredDataBuilder`:

```java
StoredData data = new StoredDataBuilder()
    .pathway("fool")
    .sequence(7)
    .trueName(player.getName().getString())
    .modified(true)
    .build();
```

Or copy from existing data:

```java
StoredData updated = new StoredDataBuilder()
    .copyFrom(existingData)
    .sequence(6) // Promote to sequence 6
    .modified(true)
    .build();
```

### Honorific Names

Honorific names are multi-line titles validated against pathway-specific keywords:

```java
// Validation checks that lines contain pathway-specific keywords
// e.g., for "fool" pathway: History, Mystery, Bizarreness, Change, Wishes, etc.
boolean valid = HonorificName.validate("fool", lines);
```

### Messages

The BeyonderMap includes a per-player message queue:

```java
map.addMessage(playerUUID, new MessageType("sender", timestamp, "Title", "Description", false));
MessageType msg = map.popMessage(playerUUID);
map.markRead(playerUUID, 0); // Mark message at index 0 as read
```

---

## 10. Shader Rendering System

**Location:** `de.jakob.lotm.rendering.ShaderManager` and `src/main/resources/assets/lotmcraft/shaders/program/`

The mod uses custom post-processing shaders that overlay visual effects on the player's screen.

### How Shaders Work

1. `ShaderManager` subscribes to `RenderLevelStageEvent.Stage.AFTER_PARTICLES`
2. Each tick, it checks conditions to determine which shader (if any) should be active
3. Only **one shader** can be active at a time (priority-based selection)
4. Shaders are loaded via `mc.gameRenderer.loadEffect(ResourceLocation)`

### Shader Priority (highest to lowest)

| Priority | Shader | Condition | Visual Effect |
|----------|--------|-----------|---------------|
| 1 | `shattered_glass` | Shattered glass state | Fractured screen |
| 2 | `sanity_loss` | Low sanity | Wavy distortion, grain, desaturation |
| 3 | `abyssal_distortion` | Abyss corruption | Fractal warping, chromatic aberration |
| 4 | `holy_effect` | Holy state active | Golden glow, bloom, god rays |
| 5 | `drought_effect` | Drought active | Heat haze |
| 6 | `blizzard_effect` | Blizzard active | Ice/frost overlay |
| 7 | `fog_of_history` | Historical Void | Dense fog |
| 8 | `holy_effect` | Sun Kingdom | Golden glow (reused) |

### Shader File Structure

Each shader consists of paired vertex (`.vsh`) and fragment (`.fsh`) files:

```
shaders/program/
├── holy_effect.vsh          # Vertex shader (position pass-through)
├── holy_effect.fsh          # Fragment shader (golden bloom, sparkles)
├── sanity_loss.fsh          # Wavy distortion + desaturation
├── abyssal_distortion.fsh   # Fractal noise warping + chromatic aberration
├── blizzard_effect.fsh      # Frost/ice overlay
├── drought_effect.fsh       # Heat shimmer
├── fog_of_history.fsh       # Dense fog overlay
└── shattered_glass.fsh      # Screen fracture effect
```

### Shader Conditions

Shaders are activated based on player attachment data:

```java
// Conditions checked in ShaderManager:
entity.getData(ModAttachments.SHADER_COMPONENT)         // General shader state
entity.getData(ModAttachments.TRANSFORMATION_COMPONENT) // Transformation state
entity.getData(ModAttachments.MIRROR_WORLD_COMPONENT)   // Mirror world state
```

---

## 11. Particle System

**Location:** `de.jakob.lotm.particle/`

The mod registers 16 custom particle types.

### Registered Particles

| Particle | Visual Style |
|----------|-------------|
| `HOLY_FLAME` | Golden rising flame |
| `PURPLE_FLAME` | Purple rising flame |
| `DARKER_FLAME` | Dark flame |
| `GREEN_FLAME` | Green flame |
| `BLACK_FLAME` | Black flame |
| `TOXIC_SMOKE` | Toxic green smoke |
| `HEALING` | Green healing sparkles |
| `CRIMSON_LEAF` | Red falling leaf |
| `BLACK_NOTE` | Black music note |
| `GOLDEN_NOTE` | Golden music note |
| `BLACK` | Black particle |
| `DISEASE` | Sickly particle |
| `EARTHQUAKE` | Ground debris |
| `LIGHTNING` | Electric particle |
| `STAR` | Star-shaped particle |
| `FOG_OF_WAR` | Fog particle |

### Using Custom Particles

```java
// Server-side spawning
serverLevel.sendParticles(ModParticles.HOLY_FLAME.get(), x, y, z,
    count, spreadX, spreadY, spreadZ, speed);

// Via ParticleUtil
ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(),
    position, count, spread, speed);
```

### Creating a New Particle Type

1. Register in `ModParticles.java`:
   ```java
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MY_PARTICLE =
       PARTICLE_TYPES.register("my_particle", () -> new SimpleParticleType(true));
   ```
2. Create a particle class extending an appropriate base (e.g., `RisingParticle`)
3. Create a `Provider` inner class for the factory registration
4. Add particle textures in `textures/particle/`
5. Define the particle JSON in `particles/my_particle.json`

---

## 12. Ring Effect System

**Location:** `de.jakob.lotm.util.helper.RingEffectManager` (server) and `RingExpansionRenderer` (client)

Expanding ring visual effects commonly used for area-of-effect abilities.

### Server-Side (Sending to Players)

```java
// Create a ring visible to all nearby players (within 64 blocks)
RingEffectManager.createRingForAll(
    center,          // Vec3 position
    maxRadius,       // Maximum expansion radius
    durationTicks,   // How long the ring lasts
    red, green, blue, alpha,  // Color (0.0-1.0)
    ringThickness,   // Width of the ring
    ringHeight,      // Vertical height of the ring
    serverLevel
);

// Pulsing rings (multiple sequential expansions)
RingEffectManager.createPulsingRingForAll(
    center, maxRadius,
    pulseCount, pulseDuration, delayBetweenPulses,
    red, green, blue, alpha,
    ringThickness, ringHeight,
    serverLevel
);
```

### Presets

```java
RingEffectManager.Presets.explosionRing(center, serverLevel);    // Orange
RingEffectManager.Presets.healingRing(center, serverLevel);      // Green
RingEffectManager.Presets.shockwave(center, serverLevel);        // White, fast
RingEffectManager.Presets.portalRing(center, serverLevel);       // Purple, tall
RingEffectManager.Presets.magicRipple(center, serverLevel);      // Purple pulsing
RingEffectManager.Presets.beyonderAbility(center, pathway, serverLevel); // Pathway-colored
```

### Client-Only Rings

For effects only visible to the local player:

```java
RingExpansionRenderer.createRingClientOnly(center, maxRadius, duration,
    red, green, blue, alpha, thickness, height);

RingExpansionRenderer.PresetsClientOnly.explosionRing(center);
RingExpansionRenderer.PresetsClientOnly.healingRing(center);
```

---

## 13. Network & Packets

**Location:** `de.jakob.lotm.network/`

The mod uses NeoForge's payload system with **80 registered packets** (56 client-bound, 24 server-bound).

### Packet Structure

Every packet is a Java `record` implementing `CustomPacketPayload`:

```java
public record MyPacket(String data, int value) implements CustomPacketPayload {

    public static final Type<MyPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "my_packet"));

    public static final StreamCodec<FriendlyByteBuf, MyPacket> STREAM_CODEC =
        StreamCodec.of(MyPacket::write, MyPacket::read);

    private static void write(FriendlyByteBuf buf, MyPacket packet) {
        buf.writeUtf(packet.data);
        buf.writeInt(packet.value);
    }

    private static MyPacket read(FriendlyByteBuf buf) {
        return new MyPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(MyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Handle on appropriate thread
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

### Registration

Register packets in `PacketHandler.java`:

```java
// Client → Server
registrar.playToServer(MyPacket.TYPE, MyPacket.STREAM_CODEC, MyPacket::handle);

// Server → Client
registrar.playToClient(MyPacket.TYPE, MyPacket.STREAM_CODEC, MyPacket::handle);
```

### Sending Packets

```java
// Server → specific player
PacketHandler.sendToPlayer(serverPlayer, new MyPacket("hello", 42));

// Server → all players tracking an entity
PacketHandler.sendToTracking(entity, new MyPacket("hello", 42));

// Server → all players in the world
PacketHandler.sendToAllPlayers(new MyPacket("hello", 42));

// Server → all players in same level
PacketHandler.sendToAllPlayersInSameLevel(new MyPacket("hello", 42), serverLevel);

// Client → server
PacketHandler.sendToServer(new MyPacket("hello", 42));
```

---

## 14. Event Handling

**Location:** `de.jakob.lotm.events/`

The mod uses NeoForge's event bus system with 21 event handler classes.

### Key Event Handlers

| Handler | Purpose |
|---------|---------|
| `PlayerEvents` | Player join, damage, death, interactions |
| `BeyonderEventHandler` | Beyonder-specific events (potions, drops) |
| `BeyonderDataTickHandler` | Per-tick data synchronization |
| `ModEvents` | Entity attributes, commands, model layers |
| `ClientEvents` | Client-side rendering and input |
| `KeyInputHandler` | Keybind processing |
| `ExplosionEventHandler` | Custom explosion handling |
| `CameraHandler` | Camera transformations |
| `SanityEventHandler` | Sanity system rendering |
| `AbilityWheelEvents` | Ability wheel UI |

### Event Handler Pattern

```java
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MyEventHandler {

    @SubscribeEvent
    public static void onPlayerDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        // Handle damage event
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        // Runs every tick on server
    }
}
```

For client-only events:

```java
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class MyClientEventHandler {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            // Render custom visuals
        }
    }
}
```

---

## 15. Keybinding System

**Location:** `de.jakob.lotm.events.ClientEvents`

### Registered Keybinds

| Key | Default | Action |
|-----|---------|--------|
| J | Open pathway info screen |
| K | Toggle griefing (block destruction) |
| V | Next sub-ability |
| X | Previous sub-ability |
| U | Enter Sefirot |
| Left Alt | Open ability wheel (toggle) |
| Y | Open ability wheel (hold) |
| M | Use selected ability from wheel |
| 1-6 | Use ability bar slot 1-6 |
| P | Return to main body |

All keybinds are registered under the `key.categories.beyonders` category.

---

## Quick Reference: Adding a New Feature

### New Ability Checklist
1. Create class in `abilities/{pathway}/YourAbility.java`
2. Extend `Ability`, `ToggleAbility`, or `SelectableAbility`
3. Register in `AbilityHandler.registerAbilities()`
4. Add language entries in `lang/en_us.json`
5. Add icon at `textures/abilities/your_ability.png`
6. Use `DamageLookup.lookupDamage()` for damage values
7. Always multiply damage by `multiplier(entity)`
8. Use `AbilityUtil.mayDamage()` before dealing damage
9. Guard server-side code with `if (level.isClientSide) return;`

### New Packet Checklist
1. Create record in `network/packets/toClient/` or `toServer/`
2. Implement `CustomPacketPayload` with `TYPE`, `STREAM_CODEC`, `handle()`
3. Register in `PacketHandler.java`

### New Particle Checklist
1. Register in `ModParticles.java`
2. Create particle class with `Provider` inner class
3. Add texture in `textures/particle/`
4. Add particle JSON definition

### New VFX Effect Checklist
1. Create class extending `ActiveEffect`, `ActiveDirectionalEffect`, or `ActiveMovableEffect`
2. Add case in `EffectFactory.java` (or appropriate factory)
3. Trigger via `EffectManager.playEffect()` from server code
