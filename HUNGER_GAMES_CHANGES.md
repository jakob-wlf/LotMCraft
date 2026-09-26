# Hunger Games Branch — Mechanism Changes

Summary of gameplay-mechanism changes made on the `hunger-games` branch, relative to `main`. Intent: compress the normal Beyonder progression curve for a faster, combat-driven game mode.

## New items

- **`CharacteristicEssenceItem`** (`item/custom/CharacteristicEssenceItem.java`) — usable by a Beyonder at sequence ≥ 1. Grants a characteristic stack for the player's current sequence and consumes one item on use.
- **`SequenceAdvancementItem`** (`item/custom/SequenceAdvancementItem.java`) — usable by a Beyonder at sequence ≥ 2. Instantly advances the player one sequence on their current pathway (`sequence - 1`), sets their digestion progress to 100%, and consumes one item on use. Calls `BeyonderData.setBeyonder` directly, so it has never played the advancement animation.

Both are wired into `ModItems`, creative tabs, item models/textures, lang entries, and (for the advancement item) a crafting recipe + advancement. A new `beyonder_potions` item tag was also added.

## Combat digestion drain (`BeyonderEventHandler.java`)

Digestion (the resource that gates sequence advancement) now fills much faster from being hit:

| | Before | After |
|---|---|---|
| Direct-hit base drain | 0.3% | 3% |
| Direct-hit per-sequence-level scaling | ±0.1% | ±1% |
| Indirect-hit base drain | 0.05% | 1% |
| Indirect-hit per-sequence-level scaling | ±0.01% / ±0.001% | ±0.5% |

## Kill-triggered sequence advancement (`BeyonderEventHandler.java`)

- When a player kills another player and their digestion progress has already reached 100% (via the drain above), they now instantly advance one sequence and their digestion resets to 0.
- Gated by a 7-minute cooldown per player (`lotm_last_kill_advance` persistent NBT tag, `20*60*7` ticks) to prevent chain-kill abuse.
- Advancement is applied via `setBeyonder` directly — no animation.

## First-time advancement failure chance (`AdvancementUtil.calculateFailureChanceForFirstTime`)

- Sequence ≥ 7 now **always succeeds** (0% failure), unconditionally — bypasses the sanity check entirely.
- All other sequences still start from a 100% base failure chance before sanity penalties (unchanged from before).
- Previously: sequence ≥ 9 was 0%, sequence 7-8 was 85%, everything else 100%.

## Digestion removed from advancement odds (`AdvancementUtil.calculateFailureChance`)

Digestion no longer factors into the failure chance for normal (non-first-time) sequence/pathway advancement — only sanity does:

- Sequence difference ≤ 1: 0% failure if sanity ≥ 0.8, else 60% base (plus sanity penalty below 0.8).
- Sequence difference == 2: 90% base (unchanged).
- The old "good digestion" branches (0%/65% baseline depending on digestion ≥ 0.95) were removed.

Digestion is still tracked and still drives the kill-advance trigger above — it just no longer affects success odds for potion-driven advancement.

## Digestion set to 100% on successful advancement

Every successful advancement path now maxes the player's digestion progress as part of completing:

- `AdvancementUtil.executeAdvancement` (first-time, pathway switch, sequence-up via potion)
- `AdvancementUtil.advanceSameSequence` (same-sequence characteristic-stack gain via potion)
- `SequenceAdvancementItem` (direct item use)

## Advancement animation skipped for potions (`AdvancementUtil`)

Previously, drinking a Beyonder potion (`AdvancementUtil.advance`) triggered a multi-second-to-tens-of-seconds sequence: converging/sphere/fade particles, floating, forced third-person camera, colored fog, and random damage ticks, before resolving success/failure at the end of that window.

This has been removed. All existing gating logic is unchanged (failure-chance rolls, sanity checks, pathway-switch domain restrictions, death-on-failure via `LOOSING_CONTROL` damage), but it now resolves **immediately** in the same tick instead of being scheduled over `calculateAdvancementDuration(sequence)` ticks. Removed the now-dead particle/fog/floating/camera/random-damage scheduling code (~180 lines) and their unused imports from `AdvancementUtil.java`.

Net effect: `SequenceAdvancementItem` and kill-triggered advancement were already instant (they bypass `AdvancementUtil` and call `setBeyonder` directly); potion-driven advancement via `AdvancementUtil.advance` is now instant too.
