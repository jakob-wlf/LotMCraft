# Black Emperor Pathway Abilities

> **Damage type:** All ability damage uses `mobAttack` — reduced by armor, Resistance, and Protection enchants. No ability deals true damage.

## Active Abilities

---


### Mausoleum Domain
**Sequence Requirement:** 1
**Spirituality Cost:** 25% of max
**Cooldown:** 120 second
*(Cannot be copied or replicated)*

- **Cast Range:** 25 blocks
- Teleports the caster and all nearby players into a sealed mausoleum dimension (`lotmcraft:mausoleum`).
- One active session per caster — cannot be recast while a session is open.

**Behavior inside the mausoleum:**
- All block breaking and placing is prevented.
- The room is continuously restored to its original snapshot every second (prevents any permanent modification).
- Players who escape the room bounds are clamped back inside every second.
- Players who disconnect during a session are queued for death on next login.

**Spirituality Drain (every second):**
| Target | Drain |
|--------|-------|
| Caster | 550 per second |
| Other players (Beyonders) | `0.5 + max(0, targetSeq − casterSeq) × 0.15` |

**Ability Seal:** All abilities are blocked for anyone inside the mausoleum domain (`isInsideMausoleumDomain` check in `Ability.canUse`).

**Session End Conditions:**
- Caster's spirituality drops to 0.
- Session ends → all members are teleported back to their original positions, dimensions, and motion.

---

### Entropy *(sub-ability of Disorder)*
**Sequence Requirement:** 2 *(accessible via Disorder Mode 6)*
**Duration:** 60 seconds
**Pulse Interval:** Every 20 seconds (3 pulses total)
**Aura Radius:** 25 blocks

**Scale Multiplier:** `1.0 + (2 − seq) × 0.25` (stronger at lower sequences)
**Spirituality Drain per Pulse:** `8%`

**Pulse Outcomes (roll-based, scale increases with stack):**

| Roll | Outcome | Effect |
|------|---------|--------|
| < 25 | Minor Entropy | Slowness + Blindness + Confusion; randomizes the selected mode of the target's currently active ability |
| 25–49 | Control Collapse | Losing Control (6 + stack levels); shuffles the order of abilities on the target's wheel |
| 50–69 | Sensory Decay | Blindness + Confusion + Slowness III; all ability cooldowns inflated by a random 10–50% for the duration |
| 70–85 | Entropy Drain | Blindness + Confusion + Slowness III + Losing Control; spirituality cost of all abilities inflated by a random 10–50% for the duration |
| 86–95 | Entropy Effect | Every harmful Minecraft effect applied at Level II (max potion-obtainable) for 45 seconds |
| 96+ | Entropy Damage | 12% max HP + (stack × 2.5) damage (reduced by armor/resistance) + Losing Control |

---

### Commanding Orders
**Sequence Requirement:** 3
**Spirituality Cost:** 10.0 per second (toggle)
*(Cannot be used by NPCs)*

- **Range:** 25 blocks
- **Chat Command Cooldown:** 4.5 seconds between uses
- While active, sending a chat message in the format `<TargetName> <command>` issues an order:

| Command | Effects |
|---------|---------|
| `kneel` | Slowness II (2.5s), Weakness (2s), Confusion (1.25s), velocity reduced to 20% |
| `halt` | Slowness III (3s), zeroed velocity, mobs get No-AI for 3s |
| `retreat` | Slowness (2s), pushed away at 0.65 force with 0.12 upward |
| `advance` | Slowness (1.25s), mobs navigate to caster at 1.0 speed, players pulled at 0.35 force |
| `silence` | Weakness (2.5s), Confusion (1.75s), all active toggle abilities stopped, all Beyonder abilities disabled for 5 seconds |

**Head Lock:** Targets 2+ sequences weaker have their head locked downward (36–54° based on gap).

**Damage Reflection:** When the caster takes damage from a Beyonder of equal or weaker sequence, the attacker's outgoing damage is reduced by 50% for 3 seconds and they are pushed away. Stronger Beyonders (lower sequence) are immune.

---

### Commanding Presence
**Sequence Requirement:** 3
**Spirituality Cost:** 4.0 per second (toggle)
*(Cannot be used by NPCs)*

- Emits a passive aura affecting all nearby entities weaker than the caster.
- **Aura Radius:** 22 blocks × scale (scale = `1.0 + (3 − seq) × 0.20`)

**Aura Effects (Seq 3 and below):**
- **Confusion:** Level 0, 1.5 seconds — applied every 2 seconds
- **Reverence messages** sent to players every 5 seconds
- Head-down pressure on targets 2+ sequences weaker

**Legacy Version (Seq 5):**
- **Slowness:** Level 0, 2.5 seconds — applied every 2 seconds to players
- **Weakness:** Level 0, 2.5 seconds — applied every 2 seconds to Beyonders of equal or lower sequence

**Combat Spirituality Drain (Seq 5 and below):** Targets in combat with the caster lose `1.0 + (seq_gap × 0.25)` spirituality per second.

**Damage Reflection:** Same as Commanding Orders — attacker's outgoing damage reduced by 50% for 3 seconds and knockback applied. Stronger Beyonders immune.

---

### Frenzy *(sub-ability of Disorder)*
**Sequence Requirement:** 3 *(accessible via Disorder Mode 5)*
**Cooldown:** 60 seconds
**Duration:** 60 seconds
**Pulse Interval:** Every 20 seconds (3 pulses total)
**Aura Radius:** 18 blocks

**Caster pulses** (random outcome each pulse):
- **Boon (70–88% chance):** Random buff — Speed II, Jump Boost II, Strength II, Luck II, Absorption II, or Regeneration II + minor heal
- **Neutral (92–98%):** Damage Resistance or Slow Falling
- **Minor Bad (2–6%):** Blindness + Confusion
- **Major Bad (0.2%):** Losing Control

**Target pulses** (outcome varies by sequence gap):
| Outcome | Effect |
|---------|--------|
| Minor Disorder | 3 random harmful effects (2–4s) + random movement nudge |
| Control Loss | Target's inventory order is shuffled; Losing Control / Confusion / Weakness for 3–7s |
| Seal | Slowness III + Weakness + ability seal for 4–7s |

---

### Bestowment
**Sequence Requirement:** 4
**Spirituality Cost:** 20% of max
**Cooldown:** 45.0 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 15 blocks
- **Resistance:** ~40% at 1-sequence gap, 0% at 2+ gap

Five selectable modes. Duration of each effect scales with caster sequence (stronger = longer):

**Mode 0 — Money Focus**
- **Duration:** 20–40 seconds (scales with sequence)
- Pulls the target toward the nearest ore within 192 blocks (iron, gold, diamond).
- Mobs navigate to ore at 1.0 speed; players are lightly pulled every 0.25 seconds.
- Message: *"Money... ore... treasure..."*

**Mode 1 — Rash**
- **Duration:** 10–20 seconds (scales with sequence)
- Applies **Confusion** (1.5s) + **Slowness II** (1s) every 0.4 seconds.
- Causes chaotic movement nudges every second.
- Drops a random equipped item (armor, mainhand, or offhand) every 7 seconds.
- Pulses damage to the nearest entity within 10 blocks every 4 seconds.

**Mode 2 — Sluggish**
- **Duration:** 12–18 seconds (scales with sequence)
- Applies **Slowness II** (2s) every 2 seconds.
- Drains spirituality every 2.5 seconds.
- Cancels regeneration for the duration.

**Mode 3 — Anxiety**
- **Duration:** 10 seconds
- Applies **Confusion** (2s) + **Weakness** (2s) every second.
- Drains 2% sanity every second.

**Mode 4 — Will to Fight Seal**
- **Duration:** 10 seconds base, +1 second per sequence above 9 (weaker target = longer)
- Applies **Weakness II** + **Slowness II** for the full duration.
- Seals all Beyonder abilities until the effect expires.

---

### Exploit
**Sequence Requirement:** 4
**Spirituality Cost:** 15% of max
**Cooldown:** 5.0 seconds
*(Cannot be copied or replicated; players only)*

Three selectable modes:

**Mode 0 — Flight** *(toggle)*
- Enables sustained flight. Drains 0.03–0.08 spirituality per second.
- **Flight Speed by Sequence:**
  - Seq 2: 0.12
  - Seq 3: 0.075
  - Seq 4: 0.060
- Deactivates when spirituality drops below 0.5.

**Mode 1 — Jump Up** *(vertical launch)*
- Launches the caster straight upward (Y velocity +10.5, horizontal retained at 20%).
- Additional +3.5 Y boost for 0.15 seconds.
- **Fall damage immunity:** 6 seconds.

**Mode 2 — Jump Forward**
- Launches the caster forward (look × 10.0 + 25% current horizontal, Y +8.0).
- Additional look-direction boost for 0.15 seconds.
- **Fall damage immunity:** 6 seconds.

---

### Magnify
**Sequence Requirement:** 4
**Spirituality Cost:** 20% of max
**Cooldown:** 20.0 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 96 blocks
- **Resistance:** ~35% at 1-sequence gap, ~20% at 2-sequence gap

Four selectable modes:

**Mode 0 — Magnify Self** *(self-cast)*
- **Duration:** 12 seconds
- Grants: Speed III, Jump Boost III, Strength III, Damage Resistance II, Regeneration III, Absorption II, Haste III.

**Mode 1 — Magnify Weather**
- **Raining/thundering:** Triggers a weakened Lightning Storm at the target location.
- **Clear weather:** Spawns a Tornado at the target location.

**Mode 2 — Magnify Grab**
- **Duration:** 4 seconds
- Pulls the target toward the caster at variable speed (1.35–3.2 based on distance).
- On arrival (within 2.1 blocks): Slowness VI (1.5s) + Weakness II (1.5s) + Blindness (1.5s) + Confusion (1.5s).

**Mode 3 — Magnify Execution**
- Deals damage scaled by sequence gap (`target_seq − caster_seq`):

| Sequence Gap | Damage |
|-------------|--------|
| +2 or more (weaker) | target current HP + 20 (execution) |
| +1 (slightly weaker) | 30% of target max HP |
| 0 (equal) | 20% of target max HP |
| −1 (slightly stronger) | 10% of target max HP |
| −2 or more (stronger) | 0% (no damage) |

---

### Disorder
**Sequence Requirement:** 5
**Spirituality Cost:** 65
**Cooldown:** 7.0 seconds
*(Cannot be copied or replicated)*

**Resistance:** ~30% at 1-sequence gap, 0% at 2+ gap

Seven selectable modes:

**Mode 0 — Disordered Actions**
- The target's next outgoing hit is redirected to a random nearby entity within 8 blocks (not the attacker or original target).
- **Duration:** 6 seconds

**Mode 1 — Disordered Perception**
- Applies **Confusion** (7 seconds, Level 0).
- The target's next 3 ability uses are replaced with a random ability from their own pathway (DisorderAbility excluded).
- **Duration:** 7 seconds (or until all 3 charges are consumed)

**Mode 2 — Defensive Veil** *(self-cast)*
- Grants the caster a **35% chance to negate any incoming hit**.
- **Duration:** 8 seconds

**Mode 3 — Break Bonds** *(no target required)*
- Cleanses all harmful effects and fire from the chosen entity (self or other).
- Players receive full saturation + hunger restoration.

**Mode 4 — Distance Warp** *(self-cast)*
- Teleports the caster forward up to **8–34 blocks** (scales with sequence: `min(34, 8 + (8 − seq) × 3)`).
- Checks for a safe landing position block-by-block.

**Mode 5 — Frenzy** *(Seq 3+ only)*
- See **Frenzy** sub-ability above.

**Mode 6 — Entropy** *(Seq 2+ only)*
- See **Entropy** sub-ability above.

---

### Corrosion
**Sequence Requirement:** 6
**Spirituality Cost:** 2.0 per second (toggle)
*(Cannot be used by NPCs)*

- **Aura Radius:** 10 blocks
- Entities in range accumulate exposure time. Stronger Beyonders (lower sequence than caster) are fully immune.

**Corruption Stages (by exposure duration):**

| Stage | Threshold | Effects (every 2–3 seconds) | Message |
|-------|-----------|----------------------------|---------|
| 1 | 3+ seconds | Slowness I; mobs randomly halt navigation (35% chance) | "You feel an inexplicable greed stirring..." |
| 2 | 8+ seconds | Confusion; mobs randomly retarget nearby entities (50% chance); players' FOV randomized every 5 seconds | "The greed is overwhelming — your thoughts are scattered." |
| 3 | 15+ seconds | Weakness II; targets lose 1% sanity every 3 seconds | "You can no longer control yourself. Darkness consumes you." |

**Exposure Decay:** Decays at half rate when leaving the aura.

---

### Weakness Detection
**Sequence Requirement:** 6
**Spirituality Cost:** 2.5 per second (toggle)
*(Cannot be used by NPCs)*

- **Detection Range:** 20 blocks
- **Debuff Range:** 12 blocks
- Scans nearby entities and assigns violation tiers visually. Stronger Beyonders (lower sequence) are ignored.

**Violation conditions** (each adds +1 tier):
- Armor below 10
- Health below 30% of max
- Speed above 0.22

**Tier Effects (applied within 12 blocks, every second):**

| Tier | Color | Damage Boost | Debuffs |
|------|-------|-------------|---------|
| 1 | Yellow | +15% | Slowness I, 2s |
| 2 | Orange | +30% | Slowness II (2s) + Weakness I (2s) |
| 3 | Red | +50% | Slowness III (2s) + Weakness II (2s) + 5 direct damage every second |

---

### Distortion
**Sequence Requirement:** 6
**Spirituality Cost:** 55
**Cooldown:** 6.0 seconds
*(Cannot be copied or replicated)*

**Target Range:** 12 blocks (14 blocks for Distort Intent at Seq 5+)
**Resistance:** ~35% at 1-sequence gap, 0% at 2+ gap

Five selectable modes:

**Mode 0 — Distort Action**
- Negates the target's next outgoing hit (0 damage).
- **Duration:** 8 seconds (Seq 5+) / 6 seconds (Seq 6+)
- **Seq 5+:** Also applies Weakness (3s) + Slowness (2s).

**Mode 1 — Distort Intent**
- Mobs lose target and navigation; retarget a random nearby entity.
- Players are randomly nudged every 0.2–0.25 seconds at 0.18–0.24 force.
- **Duration:** 5 seconds (Seq 5+) / 4 seconds (Seq 6+)
- **Seq 5+:** Also applies Confusion (3s) + Weakness (3s); radius extends to 14 blocks.

**Mode 2 — Distort Trajectory**
- Each projectile the target fires is rotated by 60–120° randomly.
- **Duration:** 8 seconds (Seq 5+) / 6 seconds (Seq 6+)
- **Charges:** 2 (Seq 5+) / 1 (Seq 6+)

**Mode 3 — Distort Concept** *(Seq 5+ only)*
- Links caster and target for 8 seconds.
- All damage dealt by either party is split 50/50 between both.

**Mode 4 — Distort Wound** *(Seq 4+ only)*
- Self-cast. Heals the caster for **35% of missing HP**.

---

### Briber
**Sequence Requirement:** 7
**Spirituality Cost:** 75
**Cooldown:** 8.5 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 18 blocks
- **Requirement:** Must hold an item in the off-hand (transferred to target on cast).
- **Resistance:** ~50% at 1-sequence gap, 0% at 2+ gap

Three selectable modes:

**Mode 0 — Weaken** *(30 seconds)*
- **Slowness II** (30s) + **Weakness** (30s).
- Target's armor reduced by 20%; outgoing damage reduced by 20%.

**Mode 1 — Arrogance** *(30 seconds)*
- **Confusion** (2s) every second.
- Random movement nudges every 10 seconds.
- **20% chance per incoming hit** to completely dodge the damage.

**Mode 2 — Charm** *(30 seconds)*
- **Slowness III** (1s) every 0.5 seconds.
- **On hit:** if the charmed target attacks the caster, the hit is cancelled and charm is broken.
- If the target never attacks the caster, charm expires naturally after 30 seconds.

---

### Eloquence
**Sequence Requirement:** 9
**Spirituality Cost:** 5
**Cooldown:** 5.5 seconds

- **Radius:** 5 blocks
- Applies to all nearby entities (excluding caster):
  - **Weakness:** Level 1, 10 seconds

---
### Law Proficiency
**Sequence Requirement:** 9 *(superseded by Weakness Detection at Sequence 6 and below)*
**Spirituality Cost:** 1.5 per second (toggle)

**Detection Radius:** 6 blocks

**Violation conditions** (each adds +1 tier):
- Armor below 10
- Health below 30% of max
- Speed above 0.25

**On violation detected:**
- **Weakness:** Level 1, 2 seconds
- **Slowness:** Level 0, 2 seconds
- Outgoing damage against the violator is boosted by **1.5×** within 6 blocks

**Note:** Beyonders of higher sequence (Seq < 9) are ignored entirely.

---

## Passive Abilities

---

### Physical Enhancements (Black Emperor)
**Sequence Requirement:** 9

Passive buffs that scale with the caster's current sequence.

| Sequence | Strength | Speed | Regeneration | Bonus Health | Resistance |
|----------|----------|-------|--------------|--------------|------------|
| 9        | —        | +1    | +1           | —            | —          |
| 8        | +2       | +2    | +2           | +7           | +5         |
| 7        | +2       | +2    | +2           | +6           | +5         |
| 6        | +3       | +2    | +2           | +8           | +6         |
| 5        | +3       | +2    | +2           | +10          | +8         |
| 4        | +3       | +4    | +3           | +16          | +13        |
| 3        | +4       | +4    | +3           | +18          | +5         |
| 2        | +5       | +4    | +4           | +24          | +6         |
| 1        | +4       | +4    | +4           | +30          | +18        |
| 0        | +6       | +5    | +5           | +36          | +8         |
