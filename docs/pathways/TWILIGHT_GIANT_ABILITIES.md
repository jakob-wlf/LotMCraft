# Twilight Giant Pathway Abilities

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

## Twilight Aging

Most Sequence 2+ abilities "age" their targets instead of (or on top of) dealing damage. Aging is tracked in **years** per entity (`TwilightAging`).

- **Year scaling:** Years applied are multiplied by the sequence gap:
  - Target weaker or equal: **×(1 + 0.25 × difference)**, capped at a 4-sequence difference (×2.0).
  - Target stronger: **×(1 − 0.4 × difference)**, minimum **×0.1**.
- **Immunity:** A Twilight Giant Beyonder cannot be aged by a weaker Twilight Giant Beyonder.
- **Debuffs:** **Weakness, Slowness and Mining Fatigue**, amplifier `years / 4` (max Level 5), refreshed for 2 seconds.
- **Multiplier penalty:** Beyonder multiplier modifier of `1 − years/limit` (minimum 0.1).
- **Healing cap:** Healing cannot raise health above `maxHealth × (1 − years/limit)`.
- **Death:** Reaching the year limit kills the target outright (or makes it fade for 2 seconds first, for Hurricane of Light). Sequence 0 targets cannot die of old age.

| Target Sequence | Year Limit |
|-----------------|-----------|
| 0               | 200       |
| 1               | 150       |
| 2               | 120       |
| 3               | 100       |
| 4               | 80        |
| 5               | 50        |
| 6               | 40        |
| 7               | 30        |
| 8               | 20        |
| 9 / non-Beyonder| 10        |

- **Recovery:** **1 year per minute**. Above **100 years**, nothing recovers until all aging clears at once after **5 minutes**.
- Death clears all aging.

---

## Active Abilities

---

### Twilight Authority
**Sequence Requirement:** 0
**Spirituality Cost:** 5,000
**Cooldown:** 1 second
*(Cannot be used by NPCs)*

Three selectable modes, plus a passive effect.

**Passive — Twilight Strikes**
- Every melee hit from a Sequence 0 Twilight Giant ages the target **10 years**.

**Mode 0 — Twilight Aura** (toggle)
- **Radius:** 16 blocks around the caster.
- **Upkeep:** 40 spirituality per second. Ends when spirituality runs out. Toggling off refunds 5,000.
- Ages all damageable entities in range **3 years per second**.
- **Dissipation:** Removes Fog of War, Volcano, Tornado and Electromagnetic Tornado effects in range unless their owner is significantly stronger.

**Mode 1 — Twilight Domain** (toggle)
- **Radius:** 40 blocks, fixed at the cast position.
- **Upkeep:** 15 spirituality per second. Toggling off does **not** refund.
- Ages all damageable entities inside **5 years per second**.
- **Time slowdown:** Enemy mobs inside tick at **20% speed**; enemy players get **Slowness V** and **Mining Fatigue V**.
- **Twilight Sword:** Hitting a target in the domain with the Twilight Sword **stops** a significantly weaker target or **slows** any other target for **5 seconds**. No effect on significantly stronger targets.

**Mode 2 — Land of Twilight** (toggle, requires an open Domain)
- **Upkeep:** +10 spirituality per second on top of the Domain. Toggling off refunds 5,000.
- Enemies inside the Domain are **frozen in place**. Aging is still 5 years per second.
- **Escape:** Using an escape ability (Exile, Blink, Player Teleportation, Traveler's Door, Teleportation Authority, Envision Position, Time Manipulation mode 1, or anything flagged `escape`/`blink_escape`) frees the target and gives **12 seconds** of immunity.

**Pressure Effects** (Aura, Domain, and a 4-second, 16-block pulse left by every "twilight" ability use)
- **Light suppression:** Light and purification abilities, Holiness abilities and Sun Sequence 2+ abilities cast by an equal or weaker Beyonder inside are **cancelled**.
- **Door delay:** Door and Underworld teleport abilities cast by a not-significantly-stronger Beyonder inside are **delayed 4 seconds**.

---

### Devastation Authority
**Sequence Requirement:** 0
**Spirituality Cost:** 5,000
**Cooldown:** 60 seconds
*(Cannot be used by NPCs)*

Two selectable modes:

**Mode 0 — Devastating Attacks**
- Arms **3 charges**. Each of the caster's next 3 ability uses strips **all effects and active toggle abilities** from every enemy within **16 blocks**.
- For **2 seconds** after each charged ability, every melee hit strips the target the same way.

**Mode 1 — Dehumanization**
- Arms the caster's next melee hit.
- On hit: target loses **35% sanity**, and a player target loses their honorific name and any pending prayers.
- After **10 seconds** the target loses another **35% sanity** unless a prayer reaches them first.

---

### Proxy
**Sequence Requirement:** 1
**Spirituality Cost:** 6,000
**Cooldown:** 60 seconds
*(Cannot be copied, stolen, replicated, or used by NPCs)*

**Sequence 1 — Stand In for a Patron**
- Opens a selection screen of online players within **50 blocks** who are **Sequence 0** or hold a **uniqueness**.
- The chosen patron gets a chat prompt to **accept or decline**.
- **On accept:** The caster gets **+25%** attack damage, attack speed, armor, armor toughness, movement speed, max health and knockback resistance.
- Only one patron at a time. The bond ends if either player dies.
- **Commands:** `/lotm_proxy_break` ends your bond (or releases your proxies); `/lotm_proxy_who` lists your patron, pledge and proxies.

**Sequence 0 — River of Eternal Darkness**
- Pledges the caster to the River of Eternal Darkness for **10 minutes**, letting them use every ability of that Sefirot's pathways.
- Afterward: **2 hours of extreme weakness**: **−80%** to the stats above and a **0.1×** Beyonder multiplier.
- Cannot be recast while the pledge or the weakness lasts (spirituality is refunded).

---

### Last Stand
**Sequence Requirement:** 1
**Spirituality Cost:** 14,000
**Cooldown:** 150 seconds
*(Cannot be copied, stolen, replicated, or used by NPCs)*

Two selectable modes (Sequence 0 only has Last Fight). Both fully heal the caster and show a boss-bar timer.

**Mode 0 — KoKoA**
- **Duration:** 60 seconds.
- Sets spirituality to **0**.
- Raises the caster's multiplier to an effective **2.25×** (Sequence 0 level) and grants **+125%** to the Proxy stats.

**Mode 1 — Last Fight**
- **Duration:** 30 seconds (3 seconds at Sequence 0).
- Raises the caster's multiplier to an effective **3.375×** and grants **+237.5%** to the Proxy stats.
- Sequence 0 Beyonders count as **significantly weaker** than the caster.
- **The caster dies when it ends.**
- **Sequence 0 — World Fight:** When it ends, every living entity in the dimension dies, except Sequence 0 Mother and Darkness Beyonders.

- **Strain:** Using a patron's abilities 3+ times during either mode costs **10% sanity** per use from the third onward.
- Logging out ends the stand (and kills the caster during Last Fight).

---

### Servants
**Sequence Requirement:** 1
**Spirituality Cost:** 4,000
**Cooldown:** 20 seconds
*(Cannot be copied, stolen, replicated, or used by NPCs)*

Two selectable modes:

**Mode 0 — Mark**
- **Range:** 30 blocks.
- Non-player targets are **boosted** automatically.
- Player targets get a chat prompt to choose:
  - **Boost:** Beyonder multiplier raised to that of one sequence higher.
  - **Borrow:** Receives up to **5** of the master's copyable abilities (Twilight Giant and patron-pathway abilities, lowest sequence first).
- An entity can only be marked by one master at a time.

**Mode 1 — Manage**
- Opens a menu listing every servant's health and coordinates, or "concealed" if they are hidden by Light Concealment, Mind Concealment or Visionary invisibility.
- Servants can be dismissed from the menu.

All marks clear when the master or the servant dies.

---

### Giantification
**Sequence Requirement:** 2
**Spirituality Cost:** 100 per pulse (400/s)
**Type:** Toggle

- **Size:** Scale and step height **×10**.
- **Stats:** **+25%** attack damage and movement speed.
- **Defense:** Incoming damage **÷1.25**. Immune to fall damage.
- **Stomp:** Every **4 blocks** walked on the ground:
  - Hits grounded entities within **20 blocks** (and no more than 5 blocks above the caster's feet).
  - **Damage:** `DamageLookup(2, 1.0) × multiplier`.
  - Significantly weaker targets are **killed outright**.
  - Camera shake for players within **48 blocks**.

---

### Basic Twilight Authority
**Sequence Requirement:** 2
**Spirituality Cost:** 5,000
**Cooldown:** 60 seconds
*(Cannot be used by NPCs)*

Three selectable modes:

**Mode 0 — Reborn**
- **Channel:** 5 seconds of growing Slowness (up to Level 5). Recasting cancels and refunds 5,000.
- **On completion:** Clears all effects, fire and aging; restores full health and hunger; teleports the caster to a random spot in the **Spirit World**.

**Mode 1 — The Grateful Dead** (toggle)
- **Upkeep:** 200 spirituality per second. Toggling off refunds 5,000.
- **Radius:** 50 blocks (Seq 2), 75 blocks (Seq 1), 100 blocks (Seq 0).
- Ages all damageable entities in range **5 years per second**.

**Mode 2 — Twilight Imbue**
- Imbues the main-hand item for **60 seconds**.
- Each melee hit with it ages the target **10 years**.

---

### Combat Authority
**Sequence Requirement:** 2
**Spirituality Cost:** 4,500
**Cooldown:** 40 seconds
*(Cannot be used by NPCs)*

Two selectable modes (three at Sequence 0):

**Mode 0 — Protection Seal**
- **Targeted seal:** Looking at an entity within 30 blocks seals it in a cylinder of radius **4 + target width**. The caster is pushed outside.
- **Area seal:** Otherwise, seals a **50-block** radius at the targeted location.
- **Upgraded seal (Seq ≤ 1, area only):** **100-block** radius, lasts **5 minutes**. Divination into it and teleporting into it are blocked. Right-clicking its edge with the Twilight Sword adds **2 minutes**.
- **Inside the seal:**
  - Equal or weaker entities cannot leave (pulled back in), teleport (including Light Concealment and Blink) or change dimension.
  - Unguarded entities age **1 year per second**.
  - Attacks from inside against targets outside are cancelled.
- **Shatter:** A stronger Beyonder leaving the seal breaks it.
- **Sneak-cast:** Gathers the caster and allies within **20 blocks** into the seal (creating one if needed). Guarded members take no damage from anyone unguarded.
- **Lift:** Recast while looking at the seal. It also fades if the caster moves more than **10 blocks** past its edge.

**Mode 1 — Fighting Cage**
- **Range:** 30 blocks.
- Places the caster and target **4 blocks** to either side of an **8-block** radius cage.
- Neither can leave, teleport or change dimension.
- A stronger target breaks the cage after **5 seconds**.
- Recast to lift it.

**Mode 2 — Pure Instinct** (Sequence 0)
- **Range:** 30 blocks; **Duration:** 8 seconds.
- The target cannot use abilities or items, cannot block with a shield, and can only attack with an empty hand.

---

### Twilight Sword
**Sequence Requirement:** 2
**Spirituality Cost:** 800
**Cooldown:** 15 seconds
*(Cannot be used by NPCs)*

Summons the Twilight Sword. Recast to dismiss it (refunds 800).

- **Stats:** **14** attack damage, **−2.8** attack speed, **+4** reach.
- **Upkeep:** 80 spirituality per second while held in inventory.
- **Guard-piercing:** Ignores invulnerability frames. If a hit is cancelled or deals no damage, the damage is subtracted from health directly.
- **Blink Slash** (right-click): Teleports behind a target within **40 blocks** and strikes for the sword's attack damage. Costs **600** spirituality, 8-second internal cooldown.
- **Charged Strike** (hold right-click for 1.5 seconds): A crescent slash **30 blocks** long and **8 blocks** wide that travels over 12 ticks. Costs **2,500** spirituality.
  - **Damage:** `DamageLookup(2, 1.2) × multiplier`, guard-piercing, once per target.
- Counts as a Sword of Dawn for Hurricane of Light and Protection.

---

### Holiness Authority
**Sequence Requirement:** 2
**Spirituality Cost:** 2,500
**Cooldown:** 18 seconds
*(Cannot be used by NPCs)*

Three selectable modes:

**Mode 0 — Radiance of Twilight**
- **Duration:** 30 seconds. Recast to end early (refunds 2,500).
- Every 10 ticks: Glowing and Night Vision on the caster.
- **Damage:** `DamageLookup(2, 0.6) × multiplier` purification damage to evil entities within **30 blocks**, every 10 ticks.
- Emits a `purification` / `light_source` interaction.

**Mode 1 — Twilight Beam**
- **Range:** 50 blocks; **Duration:** 4 seconds (follows the caster's aim).
- **Damage:** `DamageLookup(2, 1.0) / 20 × multiplier` per tick (×2 vs. evil), **80 hits** in total.
- Ages the target **2 years** every 5 ticks.

**Mode 2 — Holy Cage** (toggle)
- Combat Authority seals and cages deal `DamageLookup(2, 0.5)` purification damage to evil entities inside every second.
- Toggling off refunds 2,500.

---

### Mercury Liquefaction
**Sequence Requirement:** 3
**Spirituality Cost:** 55 per pulse (220/s)
**Type:** Toggle

- **Movement:** **+60%** speed, **−40%** size.
- **Evasion:** **30%** chance to fully dodge non-supernatural damage from an attacker.
- **Climbing:** Climbs walls on contact (sneak to hold position).
- **Flight:** Up to **5 seconds** of flight, reset on touching the ground.
- Immune to fall damage.
- Ending it releases any active Mercury Armory coat.

---

### Mercury Armory
**Sequence Requirement:** 3
**Spirituality Cost:** 1,600
**Cooldown:** 16 seconds
*(Cannot be used by NPCs)*

Requires Mercury Liquefaction to be active. The caster becomes invisible and attached to the target. Recast to release.

**Mode 0 — Turn into Armor**
- **Range:** 16 blocks (allies only).
- Ally gains **+20 armor**, **+12 armor toughness** and **+0.4 knockback resistance**.

**Mode 1 — Suffocate Target**
- **Range:** 16 blocks.
- **Slowness III** on the target, or **Slowness X** if the target is 2+ sequences weaker ("severe").
- **Damage:** `DamageLookup(3, 0.15)` every second.
- Target cannot chat or use voice abilities (Siren Song, Holy Song, Language of Foulness, Corrupting Voice, Commanding Orders, Midnight Poem). Severe targets cannot use any abilities.
- **Struggle:** The target breaks free after swinging at the air **8 + 4 × (sequence difference)** times, flinging the caster away.

---

### Silver Rapier
**Sequence Requirement:** 3
**Spirituality Cost:** 1,400
**Cooldown:** 12 seconds
*(Cannot be used by NPCs)*

Four selectable modes:

**Mode 0 — Condense**
- Summons **3** invisible silver rapiers per cast, up to **12**. They orbit the caster.

**Mode 1 — Attack**
- Targets the entity looked at within **40 blocks**, or the nearest enemy within **30 blocks**.
- Rapiers blink around the target and each strike every **12 ticks** (staggered).
- **Damage:** `DamageLookup(4, 0.25)` per strike, ignoring invulnerability frames.
- Rapiers retarget if the target is more than **64 blocks** away.

**Mode 2 — Defend**
- Rapiers orbit the caster and retaliate against anyone who attacks them.

**Mode 3 — Dilute**
- Dismisses all rapiers.

- **Rapier dodge:** **40%** chance to blink away from an incoming hit.
- **Blocked blinking:** Inside a foreign Protection dome or under a Teleporting prohibition, rapiers fly to the target instead.
- Rapiers vanish on logout, death or dimension change.

---

### Light Concealment
**Sequence Requirement:** 3
**Spirituality Cost:** 1,100
**Cooldown:** 8 seconds
*(Cannot be used by NPCs)*

- **Search:** Finds the nearest light level **10+** block within **24 blocks**.
- **Hidden:** Invisible, invulnerable and held inside the light.
- **Sneak-cast:** Also hides allies within **8 blocks**.
- **Exit:** Recast to leave (refunds 1,100). Also ends if the light goes out.
- **Unshadowed Domain:** Ejects hidden players within **40 blocks** and blocks the ability there for **30 seconds**.

---

### Silver Armor
**Sequence Requirement:** 3
**Spirituality Cost:** 100 per pulse (100/s)
**Type:** Toggle (pulses every 20 ticks)

- Equips a full silver armor set: **20 armor**, **12 armor toughness**, **0.4 knockback resistance**.

| Piece | Armor | Toughness | Knockback Resistance |
|-------|-------|-----------|----------------------|
| Helmet | 3 | 3 | 0.1 |
| Chestplate | 8 | 3 | 0.1 |
| Leggings | 6 | 3 | 0.1 |
| Boots | 3 | 3 | 0.1 |

- **High-tier guard:** The first **3** hits from stronger Beyonders deal **50%** damage.
- Armor is removed when the toggle ends.

---

### Eye of Demon Hunting
**Sequence Requirement:** 4
**Spirituality Cost:** 15 per pulse (60/s)
**Type:** Toggle

- **Critical hits:** **25%** chance to turn a non-critical melee hit into a critical (×1.5).
- **Sanity sight:** Shows a sanity orb above every player and Beyonder within **32 blocks**.
- **Footprints:** Shows other players' footprints within **24 blocks**, up to **20 minutes** old (max 400 shown).
- **Gaze warning:** Warns of enemies within **40 blocks** who are looking at you. Stronger threats pulse more (1–6 pulses). Mind-concealed threats are hidden.

---

### Knowledge
**Sequence Requirement:** 4
**Spirituality Cost:** 20 per pulse (20/s)
**Type:** Toggle (pulses every 20 ticks)

While active, the caster's crafting table accepts two extra recipes:

- **Beyonder potion:** Potion recipe + its 3 ingredients → the potion. The recipe is not consumed. Only works if the potion is still available on the server.
- **Demon-hunting oil:** Glass bottle + 2 ingredients:

| Oil | Ingredients | On-hit effect |
|-----|-------------|---------------|
| Lightning Strike | Lightning Rod + Glowstone Dust | Lightning bolt on the target (wielder immune) |
| Freezing | Blue Ice + Snowball | Freezes target, Slowness X for 3 seconds |
| Purification | Gold Ingot + Sunflower | ×2 damage vs. evil, purification interaction |
| Burning | Blaze Powder + Magma Cream | Sets target on fire for 8 seconds |
| Decay | Rotten Flesh + Fermented Spider Eye | Wither II for 10 seconds |
| Exorcism | Ghast Tear + Amethyst Shard | Expels a possessor or parasite. A weaker possessor dies, others are flung away |

- **Applying oil:** Hold the oil in the off-hand and a weapon in the main hand, then right-click.
- Oils last **6 hits** or **10–15 minutes**.

---

### Mind Concealment
**Sequence Requirement:** 4
**Spirituality Cost:** 40 per pulse (40/s)
**Type:** Toggle (pulses every 20 ticks)

- Hidden from Danger Premonition, Eye of Demon Hunting gaze warnings and Servants tracking for observers of **equal or weaker** sequence.
- Adds **+14** concealment power against divination.

---

### Light of Dawn Coating
**Sequence Requirement:** 4
**Spirituality Cost:** 500
**Cooldown:** 8 seconds
*(Cannot be used by NPCs)*

- Coats the main-hand weapon for **2 minutes**. It becomes unbreakable for the duration.
- **Damage:** Melee hits deal **×1.5**.
- Non-dawn weapons also gain dawn's **×2 vs. evil** and purification interaction.

---

### Protection
**Sequence Requirement:** 5
**Spirituality Cost:** 8 per pulse (32/s)
**Type:** Toggle

Requires a Sword of Dawn or Twilight Sword in the main hand. Ends if the sword is put away. The form depends on sequence.

**Sequence 5 — Walls of Light**
- Two light walls, **3 blocks** to each side of the caster and **4 blocks** tall, running **8 blocks** behind and **10 blocks** in front.
- **Shelter:** Damage to anyone in the 6 × 8 area behind the caster is **cancelled** at full strength, or reduced proportionally as the walls weaken.
- **Hold:** Enemies in the front corridor are held inside it and forced to face the caster.
- **Decay:** Strength drops by **0.05 per block** the caster moves and **0.2 per non-toggle ability** they use. Ends at 0.

**Sequence 4 and below — Dome**
- **Radius:** 15 blocks (Seq 4), 20 blocks (Seq 3), 25 blocks (Seq 2 and below).
- Nobody can cross the border in either direction (pushed back). Projectiles crossing it are destroyed.
- **Allies inside are immune to damage.**
- **Curses** cast at or from inside are blocked (only 50% of the time against stronger casters).
- Spirit World teleports out of the dome are blocked.
- **Shatter:** A stronger Beyonder crossing the border breaks it. Two overlapping domes break each other.

---

### Light of Dawn
**Sequence Requirement:** 6
**Spirituality Cost:** 350
**Cooldown:** 35 seconds
**Duration:** 30 seconds

- **Radius:** 45 blocks around the cast position.
- Places light blocks across the area every **9 blocks**. They are removed when it ends.
- **Effects on evil (undead and spirit) entities**, every second:
  - **Slowness II**, 2 seconds
  - **Weakness II**, 2 seconds
- Emits `purification`, `light_source` and `light_strong` interactions.

---

### Dawn Armor
**Sequence Requirement:** 6
**Spirituality Cost:** 80
**Cooldown:** 10 seconds
*(Cannot be used by NPCs)*

Equips a full set of dawn armor. Recast to dismiss it (refunds 80).

| Piece | Armor | Toughness |
|-------|-------|-----------|
| Helmet | 3 | 1 |
| Chestplate | 7 | 1 |
| Leggings | 6 | 1 |
| Boots | 6 | 1 |

- **Upkeep:** 8 spirituality per second while any piece is held. Removed if spirituality runs out.
- Cannot be dropped. Removed on death.

---

### Arsenal of Dawn
**Sequence Requirement:** 6
**Spirituality Cost:** 50
**Cooldown:** None
*(Cannot be used by NPCs)*

Condenses one dawn weapon at a time. Recasting the same mode dismisses it (refunds 50).

**Mode 0 — Two-handed Axe of Dawn**
- **8** attack damage, **−3.05** attack speed.

**Mode 1 — Spear of Dawn**
- **5.5** attack damage, **−2.9** attack speed, **+1** reach.
- Can be thrown like a trident. It vanishes on impact.

**Mode 2 — Sword of Dawn**
- **5.5** attack damage, **−2.4** attack speed.
- Required for Hurricane of Light and Protection.

**Mode 3 — Bow of Dawn** (Sequence 3)
- Needs no arrows and fully draws in **4 ticks**.
- Fires a hitscan shot up to **48 blocks**.
- **Damage:** `DamageLookup(3, 0.6) × draw power` purification damage (×2 vs. evil).

- **All melee dawn weapons:** **×2** damage vs. evil and a purification interaction on hit.
- **Upkeep:** 8 spirituality per second while a weapon is held.
- **Sharing:** Weapons cannot be dropped until **Sequence 3**, when they can be shared with others. They cannot be placed in item frames or on armor stands.

---

### Hurricane of Light
**Sequence Requirement:** 6
**Spirituality Cost:** 20 per pulse (80/s)
**Type:** Toggle

Requires a Sword of Dawn or Twilight Sword in the main hand. Ends if the sword is put away.

- **Rooted:** The caster cannot move or jump.
- **Radius:** 20 blocks.
- **Damage:** `DamageLookup(6, 0.5) × multiplier × 0.5` purification damage per pulse (4 per second), ×2 vs. evil.

**Sequence 2 and below — Twilight Hurricane**
- **Radius:** 45 blocks.
- Each pulse also ages targets **2 years**.
- Targets that would die from a pulse **fade away** into old age instead.

---

## Passive Abilities

---

### Physical Enhancements (Twilight Giant)
**Sequence Requirement:** 9

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Fire Resistance |
|----------|----------|------------|-------|--------------|--------------|-----------------|
| 9        | +1       | —          | +2    | —            | +1           | —               |
| 8        | +2       | —          | +3    | +5           | +1           | —               |
| 7        | +2       | —          | +3    | +6           | +1           | +1              |
| 6        | +2       | +1         | +3    | +8           | +2           | +1              |
| 5        | +3       | +2         | +4    | +10          | +2           | +2              |
| 4        | +4       | +7         | +4    | +18          | +3           | +2              |
| 3        | +4       | +8         | +4    | +19          | +3           | +3              |
| 2        | +6       | +11        | +6    | +26          | +4           | +3              |
| 1        | +6       | +12        | +6    | +31          | +4           | +4              |
| 0        | +7       | +15        | +6    | +46          | +6           | +6              |

---

### Combat Mastery
**Sequence Requirement:** 9

- **Stacks:** Each melee hit that deals damage adds a stack. Each stack gives **+5%** attack damage and attack speed.
- **Max stacks:** 3 (Seq 9), 5 (Seq 8–1), unlimited (Seq 0).
- **Duration:** Stacks last **30 seconds**. A missed swing (air or block) removes one stack.
- **Knockback immunity (Seq ≤ 8):** No knockback while within 1 block of the entity you are fighting.
- Shown as **God of Combat** at Sequence 0.

---

### Supernatural Resistance
**Sequence Requirement:** 8

- **Base reduction:** **30%** less damage from supernatural sources (magic damage and all LotMCraft damage types except Losing Control).
- **Sequence scaling:** **+15%** reduction per sequence the attacker is below you, or **−15%** per sequence they are above you.
- At Sequence 0 the scaling is **37.5%** per sequence.

---

### Weapon Mastery
**Sequence Requirement:** 7

- **Damage:** Melee and thrown weapons deal **×1.5**.
- **Attack speed:** **+0.25** while holding a weapon.
- **Bows and crossbows:** Draw **twice as fast**.
- **Thrown tridents:** Fly **1.5×** faster.
- **Shields:** Blocking knocks the attacker back (strength 1.4).
- **Armor:** **15%** chance per piece to take no durability damage.

---

### Strength of Giants
**Sequence Requirement:** 6

- **Size:** **2.5 blocks** tall.
- **Knockback:** Immune to knockback from attackers of your sequence or weaker, unless their weapon has the Knockback enchantment.
- **Cobwebs:** Do not slow you.
- **Hunger:** Drains **50%** slower for 20 seconds after combat.

---

### Illusion Immunity
**Sequence Requirement:** 5

- Resists **Nausea, Blindness, Darkness and Fooling** from casters at or below your sequence.
- Resist chance drops **25%** per sequence the caster is above you (0% at 4+ sequences above).
