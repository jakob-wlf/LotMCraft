package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ApotheosisComponent;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.Characteristic;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the Great Old One transformation.
 *
 * Requirements to transform into a Great Old One:
 *   1. Own a sefirot that has a GOO form (sefirah_castle → Lord of Mysteries,
 *      river_of_eternal_darkness → Eternal Darkness).
 *   2. Be seq 0 of your OWN path (charList contains own-path entry at seq 0).
 *   3. Have ≥3 seq-1 characteristics of your own path in the charList.
 *   4. Be seq 0 of EVERY neighboring path (charList has each neighbor at seq 0).
 *
 * State is stored as a seq-(-1) entry in BeyonderComponent.charList so that
 * syncHighest() naturally resolves sequence to -1. The PlayerMap StoredData
 * sequence field is also kept at -1 for consistency.
 *
 * Death by a seq-0 beyonder reverts the GOO back to seq 0 and unclaims the sefirot.
 * All other deaths are ignored (GOO is immortal to lesser forces).
 */
public class GreatOldOneManager {

    /** The special sequence value representing a Great Old One. */
    public static final int GREAT_OLD_ONE_SEQ = LOTMCraft.GREAT_OLD_ONE_SEQ;

    /** Display name per sefirot. */
    private static final Map<String, String> SEFIROT_TO_NAME;

    /** Display name per pathway (for getSequenceName without sefirot context). */
    public static final Map<String, String> PATHWAY_TO_NAME;

    static {
        Map<String, String> s = new HashMap<>();
        s.put("sefirah_castle",            "Lord of Mysteries");
        s.put("river_of_eternal_darkness", "Eternal Darkness");
        SEFIROT_TO_NAME = Collections.unmodifiableMap(s);

        Map<String, String> p = new HashMap<>();
        // Sefirah Castle neighbors (fool, error, door)
        p.put("fool",          "Lord of Mysteries");
        p.put("error",         "Lord of Mysteries");
        p.put("door",          "Lord of Mysteries");
        // River of Eternal Darkness neighbors (darkness, death, twilight_giant)
        p.put("darkness",      "Eternal Darkness");
        p.put("death",         "Eternal Darkness");
        p.put("twilight_giant","Eternal Darkness");
        PATHWAY_TO_NAME = Collections.unmodifiableMap(p);
    }

    /** True if this player is currently in the Transcendence ritual (apotheosis component flagged). */
    public static boolean isTranscending(ServerPlayer player) {
        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        return comp.isTranscendence() && comp.getApotheosisTicksLeft() > 0;
    }

    /** True if this player is currently a Great Old One (sequence == -1). */
    public static boolean isGreatOldOne(ServerPlayer player) {
        return BeyonderData.getSequence(player) == GREAT_OLD_ONE_SEQ;
    }

    /** Returns the GOO display name for a pathway, e.g. "Lord of Mysteries". */
    public static String getNameByPathway(String pathway) {
        return PATHWAY_TO_NAME.getOrDefault(pathway, "Great Old One");
    }

    /** Returns the GOO display name for a sefirot. */
    public static String getNameBySefirot(String sefirot) {
        return SEFIROT_TO_NAME.getOrDefault(sefirot, "Great Old One");
    }

    /**
     * Returns true if the player satisfies all transformation requirements but
     * has not yet transformed.
     */
    public static boolean meetsConditions(ServerPlayer player) {
        return getMissingRequirements(player).isEmpty();
    }

    /**
     * Returns a list of human-readable strings describing unmet conditions.
     * Empty list means the player qualifies for transcendence.
     */
    public static java.util.List<String> getMissingRequirements(ServerPlayer player) {
        java.util.List<String> missing = new java.util.ArrayList<>();

        String sefirot = SefirahHandler.getClaimedSefirot(player);
        if (!SEFIROT_TO_NAME.containsKey(sefirot)) {
            missing.add("You do not own a GOO-eligible sefirot (sefirah_castle or river_of_eternal_darkness). Current: \"" + sefirot + "\"");
            return missing; // rest of checks are meaningless without a sefirot
        }

        String ownPath = BeyonderData.getPathway(player);
        List<Characteristic> charList = BeyonderData.getCharList(player);

        // 1. Must be seq 0 of own path
        int ownSeq = charList.stream()
                .filter(c -> c.pathway().equals(ownPath))
                .mapToInt(Characteristic::sequence)
                .min()
                .orElse(LOTMCraft.NON_BEYONDER_SEQ);
        if (ownSeq != 0) missing.add("You must be Sequence 0 of your own path (" + ownPath + "). Current best: " + ownSeq);

        // 2. Must have ≥3 seq-1 characteristics of all domain pathways
        int requiredSeq1 = player.serverLevel().getGameRules().getInt(ModGameRules.CHARSTACK_REQUIRED_FOR_APOTHEOSIS);
        for(String path : SefirotAuthorityManager.NEIGHBORING_PATHS.getOrDefault(sefirot, Collections.emptyList())) {
            int seq1Stack = charList.stream()
                    .filter(c -> c.pathway().equals(path) && c.sequence() == 1)
                    .mapToInt(Characteristic::stack)
                    .findFirst()
                    .orElse(0);
            if (seq1Stack < requiredSeq1)
                missing.add("You need " + requiredSeq1 + " Sequence-1 characteristics of " + ownPath + ". Current: " + seq1Stack + "/3");
        }

        // 3. Must be seq 0 of every neighboring path
        List<String> neighbors = SefirotAuthorityManager.NEIGHBORING_PATHS
                .getOrDefault(sefirot, Collections.emptyList());
        for (String neighborPath : neighbors) {
            if (neighborPath.equals(ownPath)) continue;
            int neighborSeq = charList.stream()
                    .filter(c -> c.pathway().equals(neighborPath))
                    .mapToInt(Characteristic::sequence)
                    .min()
                    .orElse(LOTMCraft.NON_BEYONDER_SEQ);
            if (neighborSeq != 0) missing.add("You must be Sequence 0 of neighboring path \"" + neighborPath + "\". Current best: " + neighborSeq);
        }

        return missing;
    }

    /**
     * Begins the 10-minute Transcendence ritual.
     * colored apotheosis particles appear and nearby observers lose spirit/sanity.
     * On completion {@link ApotheosisTickHandler} will call {@link #transform(ServerPlayer)}.
     */
    public static void startTranscendence(ServerPlayer player) {
        String sefirot = SefirahHandler.getClaimedSefirot(player);
        String gooName = getNameBySefirot(sefirot);

        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        comp.setPathway(BeyonderData.getPathway(player));
        comp.setTranscendence(true);
        // 10 minutes
        comp.setApotheosisTicksLeftAndSync(20 * 60 * 10, (net.minecraft.server.level.ServerLevel) player.level(), player);

        player.level().players().forEach(p -> p.playSound(net.minecraft.sounds.SoundEvents.WITHER_SPAWN));

        player.sendSystemMessage(
                Component.literal("The boundaries of sequence have dissolved. Transcendence begins — \"").
                        append(Component.literal(gooName).withStyle(s -> s.withColor(0xFFFFAA00)))
                        .append(Component.literal("\".")));
    }

    /**
     * Called internally by {@link de.jakob.lotm.events.ApotheosisTickHandler} when
     * the Transcendence ritual completes. Do not call directly — use {@link #startTranscendence}.
     */
    public static void transform(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        String sefirot  = SefirahHandler.getClaimedSefirot(player);
        String gooName = getNameBySefirot(sefirot);
        transformInternal(player, pathway, gooName);
    }

    /**
     * OP/admin force-transform. {@code gooType} must be either
     * {@code "lord-of-mysteries"} or {@code "eternal-darkness"}.
     */
    public static void transformAs(ServerPlayer player, String gooType) {
        String pathway = BeyonderData.getPathway(player);
        String gooName = gooType.equalsIgnoreCase("eternal-darkness") ? "Eternal Darkness" : "Lord of Mysteries";
        transformInternal(player, pathway, gooName);
    }

    private static void transformInternal(ServerPlayer player, String pathway, String gooName) {

        // Update BeyonderComponent
        BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
        component.setCharacteristic(1, GREAT_OLD_ONE_SEQ, pathway);

        // Update PlayerMap StoredData
        BeyonderData.playerMap.get(player).ifPresent(data -> {
            StoredData updated = StoredData.builder
                    .copyFrom(data)
                    .sequence(GREAT_OLD_ONE_SEQ)
                    .characteristic(1, GREAT_OLD_ONE_SEQ, pathway)
                    .build();
            BeyonderData.playerMap.put(player, updated);
        });

        // Refresh cross-path authority (now includes seq-0 abilities)
        SefirotAuthorityManager.updatePlayerAuthority(player);
        PacketHandler.syncBeyonderDataToPlayer(player);

        player.sendSystemMessage(
                Component.literal("You have transcended sequence. You are now ")
                        .append(Component.literal(gooName)
                                .withStyle(s -> s.withColor(0xFFFFAA00)))
                        .append(Component.literal(".")));
    }

    /**
     * Reverts a Great Old One back to seq 0, unclaiming their sefirot.
     * All neighboring-path seq-0 charList entries are preserved.
     */
    public static void revert(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);

        // Remove the -1 entry from BeyonderComponent; syncHighest() will settle on seq 0
        BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
        component.setCharacteristic(0, GREAT_OLD_ONE_SEQ, pathway);

        // Update PlayerMap StoredData
        BeyonderData.playerMap.get(player).ifPresent(data -> {
            StoredData updated = StoredData.builder
                    .copyFrom(data)
                    .sequence(0)
                    .characteristic(0, GREAT_OLD_ONE_SEQ, pathway)
                    .build();
            BeyonderData.playerMap.put(player, updated);
        });

        SefirahHandler.unclaimSefirot(player);
        SefirotAuthorityManager.clearPlayerAuthority(player);
        PacketHandler.syncBeyonderDataToPlayer(player);

        player.sendSystemMessage(
                Component.literal("Your transcendence has been shattered. You return to Sequence 0.")
                        .withStyle(s -> s.withColor(0xFFAA0000)));
    }
}
