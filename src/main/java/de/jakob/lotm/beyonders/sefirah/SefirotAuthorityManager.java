package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.passives.ElevatedConcealmentAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotUnlockedAbilitiesComponent;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAuthorityDataPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for Sefirot Authority effects.
 *
 * Each sefirot grants the owner:
 *   1. A passive protection – divination (and for River: concealment) fails for non-sefirot attackers.
 *   2. Cross-path ability access based on sequence:
 *        Seq 2 → abilities at exactly (seq + 2) = seq 4 from neighbouring paths
 *        Seq 1 → abilities at exactly (seq + 1) = seq 2 from neighbouring paths
 *        Seq 0 → ALL abilities at seq 1+ from neighbouring paths
 *        Seq 3+ → no cross-path bonus
 *
 * Neighbouring paths per sefirot (edit these lists to customise):
 *   sefirah_castle              → fool, error, door
 *   river_of_eternal_darkness   → darkness, death, twilight_giant
 */
public class SefirotAuthorityManager {

    // ── Neighbouring paths ────────────────────────────────────────────────────

    /** Pathways whose abilities can be borrowed by the owner of each sefirot. */
    public static final Map<String, List<String>> NEIGHBORING_PATHS;
    static {
        Map<String, List<String>> m = new HashMap<>();
        m.put("sefirah_castle",            Arrays.asList("fool", "error", "door"));
        m.put("river_of_eternal_darkness", Arrays.asList("darkness", "death", "twilight_giant"));
        m.put("chaos_sea",                 Arrays.asList("sun", "tyrant", "visionary"));
        NEIGHBORING_PATHS = Collections.unmodifiableMap(m);
    }

    // ── Passive protection sets (server-only, in-memory) ─────────────────────

    /**
     * Players whose divination protection is active.
     * Divination from a NON-sefirot owner targeting a player in this set will fail.
     */
    public static final Set<UUID> SEFIROT_DIVINATION_IMMUNE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Players with River-of-Eternal-Darkness passive concealment.
     * Can be checked by teleportation / location abilities to also block those.
     */
    public static final Set<UUID> RIVER_CONCEALMENT_ACTIVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the neighbour paths applicable to the player's sefirot, excluding the player's own pathway.
     */
    public static List<String> getNeighborPaths(ServerPlayer player) {
        String sefirot = SefirahHandler.getClaimedSefirot(player);
        String playerPathway = BeyonderData.getPathway(player);
        List<String> all = NEIGHBORING_PATHS.getOrDefault(sefirot, Collections.emptyList());
        List<String> result = new ArrayList<>();
        for (String p : all) {
            if (!p.equals(playerPathway)) result.add(p);
        }
        return result;
    }

    /**
     * Returns the IDs of all abilities the player <em>could</em> unlock via their sefirot authority
     * (calculated from sefirot owned + current sequence + pathway, excluding own path).
     */
    public static Set<String> getAvailableAbilityIds(ServerPlayer player) {
        String sefirot = SefirahHandler.getClaimedSefirot(player);
        if (sefirot.isEmpty()) return Collections.emptySet();
        int sequence = BeyonderData.getSequence(player);
        String playerPathway = BeyonderData.getPathway(player);
        return calculateCrossPathAbilities(sefirot, playerPathway, sequence);
    }

    /**
     * Returns the IDs of abilities the player has chosen to unlock, filtered to only currently valid ones.
     */
    public static Set<String> getUnlockedAbilityIds(ServerPlayer player) {
        SefirotUnlockedAbilitiesComponent comp = player.getData(ModAttachments.SEFIROT_UNLOCKED_ABILITIES);
        Set<String> available = getAvailableAbilityIds(player);
        Set<String> result = new HashSet<>(comp.getUnlockedAbilities());
        result.retainAll(available);
        return result;
    }

    /**
     * Enables a cross-path ability for {@code player}. Only succeeds if the ability is currently available.
     * Sends a client sync after the change.
     */
    public static void unlockAbility(ServerPlayer player, String abilityId) {
        Set<String> available = getAvailableAbilityIds(player);
        if (!available.contains(abilityId)) return;
        player.getData(ModAttachments.SEFIROT_UNLOCKED_ABILITIES).addAbility(abilityId);
        syncToClient(player);
    }

    /**
     * Disables a cross-path ability for {@code player}.
     * Sends a client sync after the change.
     */
    public static void lockAbility(ServerPlayer player, String abilityId) {
        player.getData(ModAttachments.SEFIROT_UNLOCKED_ABILITIES).removeAbility(abilityId);
        syncToClient(player);
    }

    /** Sends the current available+unlocked ability lists to the client. */
    public static void syncToClient(ServerPlayer player) {
        List<String> available = new ArrayList<>(getAvailableAbilityIds(player));
        List<String> unlocked  = new ArrayList<>(getUnlockedAbilityIds(player));
        boolean hasSefirot = SefirahHandler.hasSefirot(player);
        String claimedSefirot = SefirahHandler.getClaimedSefirot(player);
        PacketHandler.sendToPlayer(player, new SyncSefirotAuthorityDataPacket(available, unlocked, hasSefirot, claimedSefirot));
    }

    /**
     * Returns true if the divination attempt on {@code targetUUID} should be blocked.
     *
     * Covers two cases:
     *   1. The target owns a sefirot (SEFIROT_DIVINATION_IMMUNE set).
     *   2. The target has been blessed by the River of Eternal Darkness
     *      ({@link RiverBlessingManager#blocksDivination}).
     *
     * Rules for the Sefirah Castle owner (SEFIROT_DIVINATION_IMMUNE):
     *   – Inside the castle dimension: nobody can divine them (absolute block).
     *   – Outside the castle: only diviners who are 4+ sequences stronger can pierce the protection.
     *     Other sefirot owners are NOT exempt — the sequence gap rule applies to everyone.
     */
    public static boolean blocksDivination(UUID targetUUID, ServerPlayer diviner) {
        // Check River blessing first (separate rule set)
        if (RiverBlessingManager.blocksDivination(targetUUID, diviner)) return true;
        if (!SEFIROT_DIVINATION_IMMUNE.contains(targetUUID)) return false;

        // Check whether the target player is currently in the Sefirah Castle dimension
        ServerPlayer targetPlayer = diviner.server.getPlayerList().getPlayer(targetUUID);
        boolean targetInCastle = targetPlayer != null
                && targetPlayer.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY);

        // Inside the castle: absolutely no divination succeeds
        if (targetInCastle) return true;

        // Outside the castle: allow divination only if the diviner is at least 4 sequences
        // stronger than the owner (lower seq number = more powerful). Everyone else is blocked.
        // Example: owner is seq 9 → only seq 5 or stronger (seq ≤ 5) can divine them.
        int divinerSeq = BeyonderData.getSequence(diviner);
        int targetSeq = BeyonderData.playerMap != null
                ? BeyonderData.playerMap.get(targetUUID).map(d -> d.sequence()).orElse(LOTMCraft.NON_BEYONDER_SEQ)
                : LOTMCraft.NON_BEYONDER_SEQ;
        if (targetSeq != LOTMCraft.NON_BEYONDER_SEQ && divinerSeq <= targetSeq - 4) return false;
        return true;
    }

    /**
     * Returns true if a targeting ability on {@code targetUUID} should be blocked because:
     *   – the target has Elevated Concealment active (River of Eternal Darkness owner), AND
     *   – the caster does not own any sefirot.
     */
    public static boolean blocksConcealment(UUID targetUUID, ServerPlayer caster) {
        return ElevatedConcealmentAbility.ELEVATED_CONCEALMENT_ACTIVE.contains(targetUUID)
                && !SefirahHandler.hasSefirot(caster);
    }

    /**
     * Recalculates authority effects for {@code player}.
     * Cleans up stale unlocked abilities (e.g. after sequence change), updates passive effects,
     * and syncs data to the client. Called every ~20 ticks and on login.
     */
    public static void updatePlayerAuthority(ServerPlayer player) {
        String sefirot = SefirahHandler.getClaimedSefirot(player);
        if (sefirot.isEmpty()) {
            clearPlayerAuthority(player);
            return;
        }

        // Remove any unlocked abilities that are no longer available
        Set<String> available = getAvailableAbilityIds(player);
        SefirotUnlockedAbilitiesComponent comp = player.getData(ModAttachments.SEFIROT_UNLOCKED_ABILITIES);
        List<String> stale = comp.getUnlockedAbilities().stream()
                .filter(id -> !available.contains(id)).toList();
        stale.forEach(comp::removeAbility);

        // Update passive protection sets
        updatePassiveEffects(player, sefirot);

        // Sync fresh data to client
        syncToClient(player);
    }

    /**
     * Removes ALL authority effects for {@code player}.
     * Call on sefirot unclaim or player disconnect.
     */
    public static void clearPlayerAuthority(ServerPlayer player) {
        player.getData(ModAttachments.SEFIROT_UNLOCKED_ABILITIES).clear();
        SEFIROT_DIVINATION_IMMUNE.remove(player.getUUID());
        RIVER_CONCEALMENT_ACTIVE.remove(player.getUUID());
        // Sync empty state to client
        PacketHandler.sendToPlayer(player, new SyncSefirotAuthorityDataPacket(Collections.emptyList(), Collections.emptyList(), false, ""));
    }

    // ── Blacklisted ability IDs (never offered as cross-path abilities) ──────

    private static final Set<String> BLACKLISTED_ABILITY_IDS = Set.of(
            "angel_authority_ability",
            "spirit_vision_ability",
            "ally_ability",
            "cogitation_ability",
            "mythical_creature_form_ability",
            "divination_ability"
    );

    // ── Private helpers ───────────────────────────────────────────────────────

    private static Set<String> calculateCrossPathAbilities(String sefirot,
                                                            String playerPathway,
                                                            int    playerSequence) {
        Set<String> result    = new HashSet<>();

        List<String> neighbors = NEIGHBORING_PATHS.getOrDefault(sefirot, Collections.emptyList());
        if (neighbors.isEmpty()) {
            return result;
        }

        if (playerSequence > 2) {
            return result; // no cross-path bonus above seq 2
        }

        // Great Old One: access ALL abilities (including seq 0) from neighboring paths
        if (playerSequence == de.jakob.lotm.LOTMCraft.GREAT_OLD_ONE_SEQ) {
            for (String neighborPath : neighbors) {
                if (neighborPath.equals(playerPathway)) continue;
                Set<Ability> toGrant = LOTMCraft.abilityHandler.getByPathwayAndSequence(neighborPath, 0);
                for (Ability ability : toGrant) {
                    if (!BLACKLISTED_ABILITY_IDS.contains(ability.getId())) {
                        result.add(ability.getId());
                    }
                }
            }
            return result;
        }

        // Minimum sequence required on the neighbouring path:
        //   Own seq 0  → neighbours at seq 1 and above
        //   Own seq 1  → neighbours at seq 2 and above
        //   Own seq 2  → neighbours at seq 4 and above
        int minNeighbourSeq = (playerSequence == 0) ? 1
                            : (playerSequence == 1) ? 2
                            : 4;

        for (String neighborPath : neighbors) {
            if (neighborPath.equals(playerPathway)) continue;

            Set<Ability> toGrant = LOTMCraft.abilityHandler.getByPathwayAndSequence(neighborPath, minNeighbourSeq);
            for (Ability ability : toGrant) {
                if (!BLACKLISTED_ABILITY_IDS.contains(ability.getId())) {
                    result.add(ability.getId());
                }
            }
        }

        return result;
    }

    private static void updatePassiveEffects(ServerPlayer player, String sefirot) {
        UUID uuid = player.getUUID();

        // Clear previous passive state for this player
        SEFIROT_DIVINATION_IMMUNE.remove(uuid);
        RIVER_CONCEALMENT_ACTIVE.remove(uuid);

        switch (sefirot) {
            case "sefirah_castle" -> {
                // Sefirah Castle Authority: divination on owner fails for non-sefirot diviners
                SEFIROT_DIVINATION_IMMUNE.add(uuid);
            }
            case "river_of_eternal_darkness" -> {
                // River Authority: divination fails + passive concealment from detection
                SEFIROT_DIVINATION_IMMUNE.add(uuid);
                RIVER_CONCEALMENT_ACTIVE.add(uuid);
            }
        }
    }
}
