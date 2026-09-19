# Luck System

This document describes the current server-authoritative luck implementation. The primary API is `LuckManager`; abilities should not modify luck attachments directly.

## Architecture

| Responsibility | Owner |
|---|---|
| Luck limits, mutation, spending, resistance, timed gains/drains | `util/LuckManager.java` |
| Persistent entity state | `attachments/LuckAccumulationComponent.java` |
| Legacy migration | `attachments/LuckComponent.java` through `LuckManager.migrateLegacyLuck` |
| Wheel regeneration | `beyonders/abilities/wheel_of_fortune/passives/PassiveLuckAccumulationAbility.java` |
| World effects and client synchronization | `events/LuckHandler.java` |
| Client state | `util/ClientLuckResourceCache.java` |
| HUD | `rendering/HudProgressBarsRenderer.java` |
| Network payload | `network/packets/toClient/SyncLuckResourcePacket.java` |

`LuckAccumulationComponent.storedLuck` is the current source of truth. The old `LuckComponent` is retained only so existing saves can be migrated on first access.

## Limits

All entities have a minimum of **-10,000 luck**. Positive capacity depends on pathway and Sequence.

### Wheel of Fortune

| Sequence | Capacity | Base regeneration range per minute |
|---:|---:|---:|
| 9 | 250 | 0 |
| 8 | 350 | 0 |
| 7 | 500 | 1-3 |
| 6 | 1,000 | 5-10 |
| 5 | 2,000 | 10-25 |
| 4 | 4,000 | 25-50 |
| 3 | 8,000 | 50-100 |
| 2 | 12,000 | 100-250 |
| 1 | 15,000 | 250-500 |
| 0 | 25,000 | 500-1,000 |

### Other Pathways

| Sequence | Capacity |
|---:|---:|
| 9 | 100 |
| 8 | 125 |
| 7 | 150 |
| 6 | 200 |
| 5 | 250 |
| 4 | 400 |
| 3 | 600 |
| 2 | 900 |
| 1 | 1,200 |
| 0 | 2,000 |

Non-Beyonders have a positive cap of **100**.

## Regeneration

Wheel regeneration begins at Sequence 7. A random base rate inside the Sequence range is selected and retained for 5-30 minutes.

The effective rate is:

$$
R_{effective}=R_{base}\times M_{combat}\times M_{beyonder}\times M_{prophecy}
$$

- Out of combat multiplier: **1.5**.
- In combat multiplier: **0.35** for 15 seconds after combat is marked.
- Beyonder multiplier: `BeyonderData.getMultiplier(entity)`.
- Prophecy multiplier: `ProphecyAbility.getLuckRegenerationMultiplier(entity)`.

Fractional luck is accumulated until at least one whole point can be added. The HUD displays effective regeneration plus timed gains, minus timed drains.

## Spending and Ability Scaling

Use `LuckManager.consumeLuck` for direct costs. Positive costs are modified by active Prophecy cost effects before removal.

Passive Wheel events and fixed-cost luck abilities use `LuckManager.getSequenceScaledCost`:

| Sequence | Cost multiplier |
|---:|---:|
| 7 | 1.50 |
| 6 | 1.35 |
| 5 | 1.20 |
| 4 | 1.00 |
| 3 | 0.85 |
| 2 | 0.70 |
| 1 | 0.55 |
| 0 | 0.40 |

For a base cost $C_{base}$ and the Sequence multiplier $M_{sequence}$, the pre-Prophecy cost is:

$$
C_{sequence}=\left\lceil C_{base}\times M_{sequence}\right\rceil
$$

Fixed-cost abilities that create timed luck gains or drains scale their source rate by the reciprocal of the same multiplier:

$$
R_{sequence}=\frac{R_{base}}{M_{sequence}}
$$

This makes higher-Sequence uses cheaper and stronger. Per-ability caps are applied after source-rate scaling, and harmful effects are then reduced by the target resistance described below.

| Ability | Base stored luck cost | Sequence-scaled luck effect |
|---|---:|---|
| Misfortune Gifting | 50 | Target drain rate, capped at 6,500 luck/minute before target resistance. |
| Blessing | 150 | Target gain rate, capped at 3,000 luck/minute. |
| Misfortune Field | 200 | Area drain rate, capped at 6,500 luck/minute before target resistance. |
| Words of Misfortune | 300 | Entity drain rate, capped at 6,500 luck/minute before target resistance. |

These costs are charged only when an effect is successfully created. Missing targets do not spend luck, and dismissing an existing Words of Misfortune entity is free. `Spiritual Foresight` uses its separately defined exact activation, upkeep, and detection costs rather than this Sequence table.

## Harmful Luck Resistance

Harmful changes from a weaker Beyonder are reduced for stronger targets. For a positive Sequence gap $g$ between source and target:

$$
E=\max(0.15, 0.65^g)
$$

The harmful amount or drain rate is multiplied by $E$. Equal/stronger sources, self-effects, and non-Beyonder comparisons use full effectiveness.

## Timed Gains and Drains

Timed effects are server-side and keyed by entity UUID plus a source ID. Reapplying the same source replaces its rate and expiry while retaining fractional progress.

Canonical source IDs are owned by `LuckManager`:

- `prophecy`
- `borrowed_tomorrow`
- `debt_of_yesterday`

Caster-specific effects should use `LuckManager.sourceForCaster`, such as `misfortune_field:<uuid>` and `curse_of_misfortune:<uuid>`.

Timed state is currently in memory and does not survive a server restart. It expires against level game time.

## Prophecy Interactions

`ProphecyAbility` can:

- Modify positive luck gains and luck costs.
- Spend luck to fund prophecies.
- Apply a direct prophecy drain.
- Grant Borrowed Tomorrow before converting it into a debt.
- Apply Debt of Yesterday before returning luck later.
- Force or weaken ability outcomes through prophecy states.
- Clear all prophecy-owned gain/drain sources when effects end.

Key of Light and Sequence 0 checks add higher authority behavior in Prophecy. See [SEFIROT_SYSTEMS.md](SEFIROT_SYSTEMS.md#key-of-light).

## Luck-Related Abilities

| Ability/system | Interaction |
|---|---|
| Passive Luck Accumulation | Generates the Wheel resource and synchronizes its rate. |
| Luck Release | Spends accumulated luck for an active release. |
| Blessing | Spends Sequence-scaled luck and applies a Sequence-scaled beneficial timed gain. |
| Misfortune Field | Spends Sequence-scaled luck and applies caster-specific, Sequence-scaled drains in an area. |
| Misfortune Gifting | Spends Sequence-scaled luck and applies a Sequence-scaled harmful drain to a chosen target. |
| Curse of Misfortune | Applies a caster-specific timed drain. |
| Prophecy | Spends, borrows, drains, returns, and modifies luck. |
| Words of Misfortune | Spends Sequence-scaled luck and creates an entity that applies a Sequence-scaled drain. |
| Psyche Storm | Reads luck for effect scaling. |
| Luck Perception | Provides luck visualization. |
| Calamity Attraction / Spiritual Foresight | Gain higher-authority behavior from Sequence and Key of Light state. |

## HUD and Synchronization

The server sends `SyncLuckResourcePacket` with:

- ordinary luck,
- stored luck,
- maximum luck,
- total gain/regeneration rate,
- total drain rate,
- whether the Wheel resource bar is active.

`ClientLuckResourceCache` is display-only. Positive net rate uses an up arrow; negative net rate uses a down arrow. Source files use Unicode escapes for these glyphs to avoid encoding corruption.

## Developer Rules

1. Read and mutate luck through `LuckManager`.
2. Use source constants or `sourceForCaster`; do not duplicate source strings.
3. Use source-aware overloads for harmful effects so Sequence resistance is applied.
4. Do not write client cache values from server gameplay code.
5. Keep rates in luck per minute and durations in ticks.
6. When adding a persistent timed effect, do not use the in-memory timed map without adding serialization.

## Command

`/luck <player> [value]` reads or sets luck. Setting is clamped to the target's current capacity and the global -10,000 minimum.
