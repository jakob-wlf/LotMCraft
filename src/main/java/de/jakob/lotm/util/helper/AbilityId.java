package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Canonical representation of an ability-slot identifier, as stored in
 * AbilityWheelComponent, AbilityBarComponent, and the copied/recorded-ability lists.
 *
 * Wire format: "<baseAbilityId>[:<subIndex>][:copied]"
 *   baseAbilityId - required, matches Ability#getId()
 *   subIndex      - optional index into a SelectableAbility's names (NO_SUB_INDEX = n/a)
 *   "copied"      - optional trailing literal marking a copied/recorded ability
 *
 * Examples: "clairvoyance:-1", "read_and_write:2", "read_and_write:2:copied"
 *
 * NOTE: the shared-ability pool (ClientTeamData contributions) stores *plain*
 * ability ids with no encoding at all — parse() still handles that correctly
 * (subIndex = NO_SUB_INDEX, copied = false), but toString() on the result
 * won't round-trip back to the plain form. Don't run shared-pool ids through
 * AbilityId.toString(); only wheel/bar slots use the full encoded format.
 */
public record AbilityId(String baseId, int subIndex, boolean copied) {

    public static final int NO_SUB_INDEX = -1;
    private static final String COPIED_SUFFIX = ":copied";

    public static AbilityId parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new AbilityId(raw, NO_SUB_INDEX, false);
        }

        boolean isCopied = raw.endsWith(COPIED_SUFFIX);
        String withoutCopied = isCopied ? raw.substring(0, raw.length() - COPIED_SUFFIX.length()) : raw;

        int lastColon = withoutCopied.lastIndexOf(':');
        if (lastColon >= 0) {
            String potentialIndex = withoutCopied.substring(lastColon + 1);
            try {
                int subIdx = Integer.parseInt(potentialIndex);
                return new AbilityId(withoutCopied.substring(0, lastColon), subIdx, isCopied);
            } catch (NumberFormatException ignored) {
                // trailing segment isn't numeric -> the whole thing is the base id
            }
        }

        return new AbilityId(withoutCopied, NO_SUB_INDEX, isCopied);
    }

    public static AbilityId of(Ability ability, int subIndex, boolean copied) {
        return new AbilityId(ability.getId(), subIndex, copied);
    }

    public static AbilityId of(Ability ability) {
        return of(ability, NO_SUB_INDEX, false);
    }

    public boolean hasSubIndex() {
        return subIndex >= 0;
    }

    /** Looks up the Ability this id refers to, or null if it no longer exists. */
    @Nullable
    public Ability resolve() {
        return LOTMCraft.abilityHandler.getById(baseId);
    }

    /** Mirrors the check used when pruning a player's wheel/bar of stale entries. */
    public boolean isUsableBy(LivingEntity entity) {
        Ability ability = resolve();
        return ability != null && ability.hasAbility(entity);
    }

    public AbilityId withCopied(boolean newCopied) {
        return new AbilityId(baseId, subIndex, newCopied);
    }

    public AbilityId withSubIndex(int newSubIndex) {
        return new AbilityId(baseId, newSubIndex, copied);
    }

    /** Canonical wheel/bar wire format: "baseId:subIndex[:copied]". */
    @Override
    public String toString() {
        return baseId + ":" + subIndex + (copied ? COPIED_SUFFIX : "");
    }
}