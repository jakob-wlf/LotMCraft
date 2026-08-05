# Wheel of Fortune Pathway Abilities

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

## Luck and Unluck

### Above the Sequence: Key of Light

- A Sequence 0 Wheel of Fortune Beyonder who owns the **Key of Light** Sefirot can begin the standard Great Old One transcendence ritual from the Introspect screen.
- Transcendence requires Sequence 0 in the player's own pathway, ownership of its Sefirot, and one Sequence-1 characteristic from every other pathway in that Sefirot domain. It no longer requires Sequence 0 in neighboring pathways.
- Because Key of Light's domain contains only Wheel of Fortune, an eligible Wheel Sequence 0 needs no additional neighboring characteristic and transforms to **Sequence -1: Key of Light**.
- Sequence -1 receives the shared Above-the-Sequence authority and Key of Light Sefirot authority.

The Wheel of Fortune pathway revolves around the **Luck** and **Unluck** custom effects. Both are persistent status effects with a level (amplifier) that scales their per-tick chance-based behaviors.

### Absolute Perception
**Sequence Requirement:** 4

- Passively reveals every Sefirot owner in the observer's render distance with a client-side, full-bright glow.
- The glow continuously blends through the colors of every pathway belonging to that Sefirot. For example, Sefirah Castle cycles smoothly through Fool purple, Error blue, and Door cyan.
- The visible-owner snapshot refreshes every 10 ticks and clears immediately when the passive is removed.

### Spiritual Foresight

- At Sequences 9 and 8, Spirit Vision is permanently active, costs no spirituality, and cannot be disabled.
- At Sequence 7 and beyond, Spirit Vision becomes a normal ability that can be toggled on and off and uses its standard upkeep.
- While active, nearby players and immediate calamity threats are rescanned every 5 ticks.
- The action bar continuously displays the current player count, threat count, and sensing range.
- Its activation, detection, and upkeep costs are unchanged.

### Luck
Positive stored luck provides several passive probabilistic bonuses every tick or event. Its amplifier is `floor(stored luck / 500)`, so every 500 luck increases all favorable effects by one tier. Individual probabilities stop at their listed caps, while uncapped effect strength continues increasing. All formulas below use the 0-indexed **amplifier** value.

| Effect | Formula |
|--------|---------|
| **Dodge incoming damage** | `3.5% × (amplifier + 1)`, max 65% |
| **Critical hit on outgoing damage** (×1.75) | `4% × (amplifier + 1)`, min 5%, max 90% |
| **Enemy trips** — deals `2.5 × (amplifier + 1)` damage to the last entity that hit you (within 6s), with knockback | lerp amplifier 0→19: 0.2%→3.5% per tick |
| **Remove a random harmful effect** (excluding Losing Control and Unluck) | lerp amplifier 0→19: 0.25%→5% per tick |
| **Double mining drops** — duplicates a random drop twice when breaking blocks | `10% + 4.5% × (amplifier + 1)`, max 99% |
| **Bonus item drop when mining** — drops a random valuable item (gold ingots, emeralds, diamonds, nether stars, etc.) | `1% × amplifier + 1%`, max 20% |
| **Hero of the Village** effect (amplifier + 1) | Applied continuously if amplifier > 1 |

### Unluck
Unluck is a harmful effect that triggers various negative events per tick or per action. All chances scale with amplifier (0-indexed).

| Effect | Formula |
|--------|---------|
| **Incoming damage amplified** (×1.5 + 0.25 per amplifier level) | `6% × (amplifier + 1)`, max 85% per hit |
| **Outgoing damage reduced to 40%** (weak hit) | `7% × (amplifier + 1)`, max 90% per hit |
| **Mining drops destroyed** | `3% + 2% × amplifier`, max 40% per block |
| **Tool damaged by 5 × (amplifier + 1)** extra durability on mining | `8% + 4% × amplifier`, max 60% per block |
| **Random harmful effect applied** (Weakness, Hunger, Poison, Wither, Blindness, Slowness, or Mining Fatigue — level 1, 5 seconds) | lerp amplifier 0→19: 0.1%→4% per tick |
| **Trip and take damage** — `10 + 1.875 × amplifier` damage, knockback, 2s cooldown | lerp amplifier 0→19: 0.05%→1.5% per tick |
| **Hostile mob spawned** within 5 blocks (Zombie, Skeleton, Spider, or Creeper), 15s cooldown | lerp amplifier 0→19: 0.05%→0.8% per tick |
| **Random inventory item dropped** | lerp amplifier 0→19: 0.08%→1.2% per tick |
| **Slip** — velocity becomes erratic, 3s cooldown | lerp amplifier 0→19: 0.2%→2.5% per tick |
| **Beyonder multiplier reduced by 40–70%** for 3–6s, 8s cooldown (scales with amplifier) | lerp amplifier 0→19: 0.08%→1% per tick |
| **Beyonder ability activation fails** without spending its costs | Scales linearly from 0% at neutral luck to 80% at -10000 luck |
| **Bad Omen** effect (amplifier − 1, capped at 5) | Applied continuously if amplifier > 1 |

---

## Active Abilities

---

### Connection
**Sequence Requirement:** 4
**Spirituality Cost:** 50
**Cooldown:** 1 second

- **Create Connected Item:** Converts one item from the main-hand stack into a unique creator-bound connection and appends `.` to its existing name without replacing its font or formatting.
- **Use Next Ability on Connections:** Arms the next non-toggle ability. It pays costs and enters cooldown once, then uses every online player carrying one of the caster's connected items as a remote target, regardless of distance or dimension.
- Multiple connected items in one inventory still produce only one cast for that player.
- Abilities that do not query an entity or location target execute only once and do not multiply their self or area effects.

---

### Cycle of Fate
**Sequence Requirement:** 1
**Spirituality Cost:** 2000
**Cooldown:** 1 second
*(Cannot be copied, replicated, stolen; cannot be used by NPCs)*

Two selectable modes:

**Mode 0 — Create Cycle**
- Spawns a **Cycle of Fate entity** at the caster's position.
- On creation, records a **40-block radius sphere** of the world: all blocks, block entities, and all living entities within range (positions, health, potion effects, inventories).
- Entities inside the cycle cannot leave its boundary — if they reach the edge they are pushed back.
- The cycle persists as long as the caster is alive, in the same dimension, and within **100 blocks** of it.
- Only one cycle can exist per caster. Two cycles cannot overlap.

**Mode 1 — Trigger Cycle**
- Restores the recorded world state:
  - All blocks and block entities in the 40-block radius are reset to their recorded state.
  - All tracked entities are restored to their recorded position, health, effects, and inventory.
  - Entities that were not present when the recording was made (newly spawned) are removed.
  - All active Marionette Controlling possessions within the area are cancelled.
- The cycle entity is consumed on trigger.

---

### Prophecy
**Sequence Requirement:** 2
*(Cannot be copied, replicated, stolen, shared, or used through an artifact)*

- Toggle Prophecy on, then write a prophecy in chat using:
  - `<target> will be affected by <modifier> at the price of <amount> luck [in <minutes> mins]`
- The optional timer accepts 1–60 minutes. Without it, the prophecy activates immediately.
- Only one pending or active prophecy can occupy a target at once. Additional prophecies skip that target.
- A delayed prophecy reserves its target immediately, but its per-target cooldown begins only when the prophecy activates.
- On activation, the caster privately sees the selected outcome and duration. The target is told only that a prophecy has taken hold.
- Valid targets are an online player name, `Nearby` for players within 64 blocks, or `All` for every online player.
- `All` requires Sequence 0 and ownership of the **Key of Light** Sefirot.
- Each caster has a separate cooldown for every player affected: **30 minutes** at Sequence 2, **20 minutes** at Sequence 1, **10 minutes** at Sequence 0, or **5 minutes** at Sequence 0 while owning the **Key of Light**.
- Self-targeted prophecies receive half the caster's normal per-target cooldown.
- `Nearby` and `All` skip players still on that caster's cooldown. Luck is not charged if every resolved target is unavailable.
- The luck actually consumed determines the outcome tier, effect strength, and duration. Higher payments produce stronger effects lasting from **1 to 5 minutes**.
- Every target is assessed separately. A higher-sequence target requires at least **80% of the caster's maximum luck** and has only a **15% success chance**, rising linearly to **30%** at full luck.
- A Sefirot owner outside a protected realm requires **100% of the caster's maximum luck** and has a **10% success chance**. If that owner is also higher-sequence, the chance is **5%**.
- Players protected by active concealment cannot be reached. Prophecy is also completely blocked inside every Sefirot realm, Concealment World, Dream Maze, Space, Mausoleum, Space-Time Labyrinth, World Creation, and the Spirit World.
- Protected targets are removed before payment. A delayed prophecy rechecks protection when its timer expires and dissolves without starting cooldown if the target became unreachable.
- Underfunded targets resist completely, receive no cooldown, and the authored luck price remains spent. The caster is told the lowest required price among resisted targets.
- For ordinary reachable targets, outcome access uses the luck actually consumed as a percentage of the resistance-adjusted full-strength price. Let `M` be the Wheel of Fortune caster's maximum luck and `R` the target's sequence resistance. The full-strength price is `M / (1 - R)`; every threshold is rounded up. Each band has an exclusive outcome pool: low payments only roll low outcomes, medium payments only roll medium outcomes, and high payments only roll high outcomes.
  - **Low investment:** Fortune requires `5%` of full strength. Misfortune requires `10%` (`ceil(M × 0.10 / (1 - R))`).
  - **Medium investment:** requires `35%` (`ceil(M × 0.35 / (1 - R))`). Convergence also uses this minimum.
  - **High investment:** requires `70%` (`ceil(M × 0.70 / (1 - R))`).
  - **Disaster** requires at least 50%.
- Writing is private: valid and invalid prophecy text is intercepted instead of broadcast as ordinary chat.

**Fortune** randomly applies one outcome to each target:
- Increases Wheel luck regeneration by **1.5x to 4x**.
- Improves mob drops.
- Multiplies block drops.
- Gives a ward that can make abilities targeting that player fail to acquire them.
- **Fate's Correction:** grants 1–3 corrections. Each correction mitigates potentially lethal damage by **40% to 70%**, but does not guarantee survival.
- **Borrowed Tomorrow:** grants greatly increased luck regeneration and **500–3000 effective luck** during the first half. The borrowed amount is then repaid through an equal negative luck rate during the second half.
- **Golden Thread:** redirects one potentially fatal hit to a nearby valid hostile target. Stronger targets resist part of the transfer, and the resisted damage remains on the protected player.
- **Perfect Opportunity:** the next ability costs no spirituality and gains **1.35x to 2x strength**, then the prophecy ends.
- **Fortune's Interest:** multiplies all positive luck gains by **1.25x to 2x**. Spending luck shortens the remaining prophecy duration in proportion to the amount spent.
- **Chosen Outcome:** foretells three futures: softened lethal damage, one Perfect Opportunity, or redirection of a hostile targeted ability. The first matching event becomes real and cancels the other two.

**Misfortune** randomly applies one outcome to each target:
- Drains up to the paid amount of luck over the prophecy's duration.
  - While active, its exact luck drain per minute appears as a red downward rate beneath positive regeneration on the luck HUD.
- Gives each ability activation a **15% to 80%** failure chance without consuming its normal costs.
- Gives standard targeted abilities a **15% to 80%** chance to redirect to the caster or another nearby entity.
- Reveals the target's exact coordinates and dimension to Beyonders within 128 blocks, refreshing every 10 seconds.
- **Low pool:** Faltering Step periodically applies Slowness and Mining Fatigue; Meager Harvest can halve renewable stacked drops without deleting unique single items; Spiritual Leakage continuously drains a small percentage of maximum spirituality.
- **Medium pool:** Twisted Opportunity increases the next successful ability's cost and cooldown; Fractured Fortune reduces positive luck gains; Hostile Attention draws nearby hostile mobs; Uncertain Aim may redirect targeted abilities while preserving ally restrictions; Misfortunate Recovery reduces health and spirituality recovery.
- **High pool:** only the six major outcomes below can be selected; low and medium outcomes are excluded.
- **Fate's Burden:** the next 2–4 heavy nonlethal hits deal **1.75x to 2.5x damage**.
- **Debt of Yesterday:** drains **75% to 150% of the target's maximum luck** during the first half, then returns the same amount during the second half.
- **Broken Thread:** the next **2–4 successful abilities** retain only **25% to 10% strength**, a **75% to 90% reduction**. One charge is consumed after each successful cast.
- **Calamitous Opening:** after the target's next successful ability, they suffer Slowness III–IV, Weakness II–III, and **2x to 3x incoming damage** for **10 seconds**.
- **Chosen Misfortune:** the first matching event becomes a **1.75x to 2.5x heavy hit**, an ability with **2x to 3x cost and cooldown**, or a guaranteed valid target redirection.
- **Fate Reversal:** denies the next passive luck event and immediately removes **10% to 25% of maximum luck**, or the event's normal cost if that is greater. This loss can push luck below zero.

**Disaster** immediately spawns a random tornado, earthquake, or meteor on each target. Damage and size scale with the luck price, caster multiplier, and caster sequence.

**Convergence** immediately forces the target's normal Sefirot convergence check with a guaranteed successful roll. The target must carry an eligible Sefirot piece associated with their pathway, and another online player in the same dimension must carry a piece they are missing. On success, the target learns that holder's direction and estimated distance, while the holder is warned that their Sefirot power was sensed. Both `convergence` and `convergance` are accepted spellings.

---

### Words of Misfortune
**Sequence Requirement:** 2
**Spirituality Cost:** 1000
**Base Stored Luck Cost:** 300
**Cooldown:** 4 seconds
*(Cannot be copied; cannot be used by NPCs)*

- Spawns a **Misfortune Words entity** at 1 block above the caster's position.
- The entity applies **Unluck (Level 13, 5 minutes)** to all enemies that come near it.
- Accumulates an "affected count" (+10 per player, +1 per mob) and **self-destructs** when the total reaches **30**.
- Immune to Wheel of Fortune Beyonders of Sequence 2 or stronger.
- Casting again within 15 blocks removes the existing entity.

---

### Spiritual Baptism
**Sequence Requirement:** 3
**Spirituality Cost:** 900
**Cooldown:** 5 seconds
*(Cannot be copied or replicated; registers as a cleansing interaction)*

Two selectable modes:

**Mode 0 — On Self**
- Applies the baptism to the caster.

**Mode 1 — On Target**
- **Range:** 20 blocks (allies only; falls back to self if no ally found)
- Applies the baptism to the target.

**Baptism Effect:**
- Instantly heals a large amount of health (Instant Health Level 41).
- Removes all **harmful potion effects**.
- Extinguishes fire.
- Restores food to **20 (full)** and saturation to **20** (for players).
- Restores **15% sanity**.
- Removes controlling and concealed parasites, safely returning the parasite player from control or spectator mode.
- Releases marionette control and restores native NPC behavior.
- Turns temporary puppet/soul-like Beyonder NPCs back into normal NPCs instead of allowing their puppet lifetime to expire.
- Resets player transformations and disguises, including Zombie Disguise, shape-shifting, and mythical/Seer forms. Active transformation abilities perform their normal attribute and flight cleanup.

---

### Blessing
**Sequence Requirement:** 4
**Spirituality Cost:** 750
**Base Stored Luck Cost:** 150
**Cooldown:** 4 seconds
*(Cannot be copied; cannot be used by NPCs; registers as a cleansing interaction)*

- **Range:** 20 blocks (allies only)
- Applies **Luck** to the target for **17 minutes**.
- Luck level scales with caster's multiplier: `round(multiplier × 6.25)`.

---

### Misfortune Field
**Sequence Requirement:** 4
**Spirituality Cost:** 600
**Base Stored Luck Cost:** 200
**Cooldown:** 30 seconds
*(Cannot be copied)*

- **Radius:** 20 blocks
- **Duration:** 20 seconds
- **Effect Interval:** Every 2 ticks
- Applies **Unluck** to all nearby entities each interval. Unluck level scales with caster's multiplier: `round(multiplier × 3)`.

---

### Luck Perception
**Sequence Requirement:** 5
**Spirituality Cost:** 0
**Type:** Passive

- Automatically displays the **name and Luck value** of a looked-at entity within **20 blocks** in a dedicated top-screen panel every 10 ticks.
- The panel is anchored **6 pixels to the right of the Spirit Vision health panel position**, whether or not Spirit Vision is active.
- Does not occupy an ability slot and does not display anything when no entity is targeted.
- Blocked if the target is a Wheel of Fortune Beyonder of lower sequence than the caster, or significantly stronger.

---

### Luck Release
**Sequence Requirement:** 5
**Spirituality Cost:** 100
**Cooldown:** 2 minutes
*(Cannot be replicated or used in artifacts)*

- Consumes up to **240 stored luck** from the Luck resource bar.
- Adds the consumed amount directly to the caster's active Luck value, capped by the global Luck limit.
- Does nothing when the resource is empty.

---

### Spiritual Foresight
**Sequence Requirement:** 7
**Spirituality Cost:** 0.25 per toggle tick (1 per second), plus 5 per newly detected player
**Cooldown:** 0 seconds (toggle)

- Activating Spiritual Foresight costs **25 luck**. While active, it costs **5 luck per minute**.
- Detecting a new player costs an additional **20 luck and 5 spirituality per player**. Detection waits until those burst costs can be paid.
- On activation, sends one active message. It sends chat warnings only when a new player or calamity is sensed, with no persistent action-bar display.
- At base, senses and reports players within **24 blocks** and warns about nearby projectiles, primed TNT, and an attracted calamity up to **30 seconds** before it arrives.
- At **25 or more stored luck**, the range increases to **64 blocks**.
- At Sequence 4 or stronger, warnings include direction and distance, and sensed players and manifested calamity threats glow only for the caster.
- Enhanced foresight automatically returns to base foresight when the reserve drops below 25.

---

### Misfortune Gifting
**Sequence Requirement:** 5
**Spirituality Cost:** 120
**Base Stored Luck Cost:** 50
**Cooldown:** 5 seconds
*(Cannot be copied)*

- **Range:** 20 blocks
- Additively applies a negative luck modifier to the target, including other players. This directly offsets a Wheel target's stored luck and can push net luck below zero.
- The modifier scales with the caster's multiplier and the target's sequence resistance.

---

### Calamity Attraction
**Sequence Requirement:** 6
**Spirituality Cost:** 190
**Cooldown:** 10 seconds
*(Cannot be copied or replicated)*

- Summons the calamity where the caster is looking or targeting, up to the sequence range: Seq 6 **50 blocks**, Seq 5 **100**, Seq 4 **250**, Seq 3 **400**, Seq 2 **600**, Seq 1 **800**, and Seq 0 **1000**.
- Sequence 0 Key of Light owners and Above-the-Sequence entities have unrestricted explicit targeting. Looking into open space uses a 4096-block safety ray.
- After a **1.5–3 second** delay, summons a **random calamity** at the targeted location:
  - **Tornado** — damage **16 × multiplier**.
  - **Earthquake** — **~9.5 × multiplier damage** every 8 ticks for **15 seconds**, radius 34. Launches falling blocks. (Requires griefing for block interaction.)
  - **Meteor** — damage **15 × multiplier**, explosion radius 7, knockback 12.
- Calamity damage and range gain additional sequence scaling:

| Sequence | Damage Scale | Range Scale |
|----------|-------------:|------------:|
| 6        | 1.00× | 1.00× |
| 5        | 1.10× | 1.10× |
| 4        | 1.25× | 1.20× |
| 3        | 1.45× | 1.35× |
| 2        | 1.70× | 1.50× |
| 1        | 2.00× | 1.70× |
| 0        | 2.40× | 2.00× |

- Damage scale multiplies the normal Beyonder multiplier. Range scale affects the Tornado interaction size, Earthquake terrain and damage radius, and Meteor explosion and impact radius.

---

### Psyche Storm
**Sequence Requirement:** 6
**Spirituality Cost:** 80
**Cooldown:** 7 seconds

- **Radius:** 10 blocks
- **Hits:** **1 hit** per cast
- **Damage:** **~13.3 damage × caster multiplier**.
- Applies **Losing Control** to all hit entities for **7 seconds**. Level scales with sequence difference:
  - Significantly weaker target: **Level 7**
  - Weaker target (same category): **Level 3**
  - Equal target: **Level 3–5** (random)
  - Stronger target: **Level 2**

---

## Passive Abilities

---

### Physical Enhancements (Wheel of Fortune)
**Sequence Requirement:** 9

Includes **Fire Resistance** at Seq 6 and below. No Night Vision at any sequence. Luck is provided separately by the **Passive Luck** ability (Seq 7).

| Sequence | Strength | Resistance | Speed | Bonus Health | Regeneration | Other |
|----------|----------|------------|-------|--------------|--------------|-------|
| 9        | —        | —          | +1    | —            | —            | — |
| 8        | +1       | —          | +2    | +5           | —            | — |
| 7        | +2       | —          | +2    | +6           | +1           | — |
| 6        | +2       | +1         | +2    | +7           | +2           | Fire Resistance +1 |
| 5        | +2       | +2         | +2    | +9           | +2           | Fire Resistance +2 |
| 4        | +3       | +7         | +4    | +16          | +3           | Fire Resistance +2 |
| 3        | +3       | +8         | +4    | +17          | +3           | Fire Resistance +3 |
| 2        | +4       | +11        | +5    | +25          | +4           | Fire Resistance +3 |
| 1        | +4       | +12        | +5    | +30          | +4           | Fire Resistance +4 |
| 0        | +6       | +15        | +6    | +45          | +6           | Fire Resistance +6 |

---

### Mercury Body
**Sequence Requirement:** 4

- A standalone passive gained by the **Misfortune Mage**.
- Grants high **Anti-Divination** and **Anti-Prophecy** resistance against equal or weaker casters.
- A stronger caster whose numeric sequence is lower than the holder's bypasses the resistance. Their divination, hostile Wheel prophecy, or Visionary Story Writing effect still succeeds.
- On a successful bypass, the holder receives a warning identifying the caster and their sequence. Prophecy warnings also describe the hostile outcome or the Story Writing trigger and action.
- Players and NPCs with Mercury Body have a subtle animated silver-blue liquid-metal sheen over their normal appearance.
- Self-directed fortune and untargeted disaster prophecies remain available to the holder.

---

### Luck Accumulation
**Sequence Requirement:** 7

- Wheel of Fortune Beyonders receive bonus luck capacity from Sequence 9 onward. Sequences 9 and 8 do **not** regenerate luck; passive regeneration begins at Sequence 7.
- **Passive:** Charges the pathway's persistent, signed **luck resource**, shown next to the spirituality bar as its exact current value and capacity fill. All fortune, misfortune, gifts, theft, cleansing, passive events, and ability costs read or modify this one value.
- Every **5–30 minutes**, the regeneration rate randomly rerolls between the current sequence's minimum and maximum. Regeneration never becomes dormant.
- Advancing to a new sequence immediately rerolls any saved rate that falls outside the new sequence's range.
- Outside PvP combat, luck regenerates at **1.5×** the rolled rate. Damaging or being damaged by another player starts a **15-second PvP combat period**, during which regeneration falls to **0.35×**. Mobs and environmental damage do not reduce regeneration.
- Regeneration also scales with the caster's Beyonder multiplier.
- Luck is consumed by favorable passive events, Luck Release, and enhanced Spiritual Foresight.

| Sequence | Luck Cap | Regeneration per Minute |
|----------|---------:|------------------------:|
| 9        | 200      | None                    |
| 8        | 350      | None                    |
| 7        | 500      | 1–3                     |
| 6        | 1000     | 5–10                    |
| 5        | 2000     | 10–25                   |
| 4        | 4000     | 25–50                   |
| 3        | 8000     | 50–100                  |
| 2        | 12000    | 100–250                 |
| 1        | 15000    | 250–500                 |
| 0        | 25000    | 500–1000                |

---

### Passive Luck
**Sequence Requirement:** 7

- **Passive:** Uses the current luck-resource value directly as favorable-event strength. Spending luck immediately lowers that strength until it regenerates.
- Curses subtract from the same resource point-for-point and can push it below zero, down to **-10000**. Negative resource luck activates misfortune effects and recovers toward zero over time.
- Every successful event spends luck; if the resource does not contain the full cost, that event does not activate.

| Positive Luck Event | Base Stored Luck Cost |
|---------------------|----------------------:|
| Improved block drops | 10 |
| Random item drop | 5 |
| Enemy combat trip | 15 |
| Critical hit | 20 |
| Dodge | 25 |
| Cleanse harmful effect | `10 + 5 × effect level` |

- Cleansing a non-vanilla/modded effect costs an additional **15 stored luck**.
- Each harmful effect is priced and removed separately. Effects the caster cannot afford remain active.
- Hero of the Village refreshes from passive luck without consuming the luck resource.
- Hero of the Village becomes active at 50% net luck-resource fill and scales up to amplifier 3 at full capacity.
- Final costs are the base cost multiplied by the sequence modifier below, rounded up to the next whole point with a minimum cost of 1:

| Sequence | Cost Multiplier |
|----------|----------------:|
| 7        | 1.50× |
| 6        | 1.35× |
| 5        | 1.20× |
| 4        | 1.00× |
| 3        | 0.85× |
| 2        | 0.70× |
| 1        | 0.55× |
| 0        | 0.40× |

Fixed-cost luck abilities use this multiplier for their stored luck cost. Their applied luck gain or drain rate uses its reciprocal, so stronger Sequences produce stronger effects while paying their reduced Sequence cost.

- The ambient Luck value scales with sequence:

| Sequence | Luck Level |
|----------|-----------|
| 9–8      | —         |
| 7        | +3        |
| 6        | +5        |
| 5        | +6        |
| 4        | +10       |
| 3        | +13       |
| 2        | +17       |
| 1        | +20       |

---

### Passive Calamity Attraction
**Sequence Requirement:** 6

- **Passive:** At Sequence 6, every **20–90 seconds**, a random calamity automatically spawns at a random horizontal position **4–20 blocks** from the caster instead of directly on them:
  - **Tornado** — damage **16 × multiplier**.
  - **Earthquake** — **~9.5 × multiplier damage** for 15 seconds, radius 34.
  - **Meteor** — damage **15 × multiplier**, explosion radius 7.
- When a calamity is **12 seconds** away from spawning, a warning is shown in the action bar.
