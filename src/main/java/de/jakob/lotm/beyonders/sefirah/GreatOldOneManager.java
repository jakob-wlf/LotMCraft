package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ApotheosisComponent;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.ModAttachments;
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
 *   1. Own a sefirot that has a GOO form (sefirah_castle -> Lord of Mysteries,
 *      river_of_eternal_darkness -> Eternal Darkness, chaos_sea -> God Almighty,
 *      key_of_light -> Key of Light).
 *   2. Be seq 0 of your OWN path (charList contains own-path entry at seq 0).
 *   3. Have at least one seq-1 characteristic from every other pathway in the
 *      owned sefirot's domain. Neighboring seq-0 characteristics are not required.
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
    public static final int greatOldOneSeq = LOTMCraft.GREAT_OLD_ONE_SEQ;

    /** Display name per sefirot. */
    private static final Map<String, String> sefirotToName;

    /** Display name per pathway (for getSequenceName without sefirot context). */
    public static final Map<String, String> pathwayToName;

    static {
        Map<String, String> s = new HashMap<>();
        s.put("sefirah_castle",            "Lord of Mysteries");
        s.put("river_of_eternal_darkness", "Eternal Darkness");
        s.put("chaos_sea",                 "God Almighty");
        s.put("key_of_light",              "Key of Light");
        sefirotToName = Collections.unmodifiableMap(s);

        Map<String, String> p = new HashMap<>();
        // Sefirah Castle neighbors (fool, error, door)
        p.put("fool",          "Lord of Mysteries");
        p.put("error",         "Lord of Mysteries");
        p.put("door",          "Lord of Mysteries");
        // River of Eternal Darkness neighbors (darkness, death, twilight_giant)
        p.put("darkness",      "Eternal Darkness");
        p.put("death",         "Eternal Darkness");
        p.put("twilight_giant","Eternal Darkness");
        // Chaos Sea neighbors (sun, tyrant, visionary, hanged_man, white_tower)
        p.put("sun",           "God Almighty");
        p.put("tyrant",        "God Almighty");
        p.put("visionary",     "God Almighty");
        p.put("hanged_man",    "God Almighty");
        p.put("white_tower",   "God Almighty");
        // Key of Light domain
        p.put("wheel_of_fortune", "Key of Light");
        pathwayToName = Collections.unmodifiableMap(p);
    }

    /** True if this player is currently in the Transcendence ritual (apotheosis component flagged). */
    public static boolean isTranscending(ServerPlayer player) {
        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        return comp.isTranscendence() && comp.getApotheosisTicksLeft() > 0;
    }

    /** True if this player is currently a Great Old One (sequence == -1). */
    public static boolean isGreatOldOne(ServerPlayer player) {
        return BeyonderData.getSequence(player) == greatOldOneSeq;
    }

    /** Returns the GOO display name for a pathway, e.g. "Lord of Mysteries". */
    public static String getNameByPathway(String pathway) {
        return pathwayToName.getOrDefault(pathway, "Great Old One");
    }

    /** Returns the GOO display name for a sefirot. */
    public static String getNameBySefirot(String sefirot) {
        return sefirotToName.getOrDefault(sefirot, "Great Old One");
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
        if (!sefirotToName.containsKey(sefirot)) {
            missing.add("You do not own a GOO-eligible sefirot (sefirah_castle, river_of_eternal_darkness, chaos_sea, or key_of_light). Current: \"" + sefirot + "\"");
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

        // 2. Must have one seq-1 characteristic from every other pathway in the sefirot domain
        for (String path : SefirotAuthorityManager.neighboringPaths
            .getOrDefault(sefirot, Collections.emptyList())) {
            if (path.equals(ownPath)) continue;
            int seq1Stack = charList.stream()
                    .filter(c -> c.pathway().equals(path) && c.sequence() == 1)
                    .mapToInt(Characteristic::stack)
                    .findFirst()
                    .orElse(0);
            if (seq1Stack < 1) {
            missing.add("You need one Sequence-1 characteristic of neighboring path \"" + path + "\".");
            }
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
                Component.literal("The boundaries of sequence have dissolved. Transcendence begins - \"").
                        append(Component.literal(gooName).withStyle(s -> s.withColor(0xFFFFAA00)))
                        .append(Component.literal("\".")));
    }

    /**
     * Called internally by {@link de.jakob.lotm.events.ApotheosisTickHandler} when
     * the Transcendence ritual completes. Do not call directly - use {@link #startTranscendence}.
     */
    public static void transform(ServerPlayer player) {
        String pathway = BeyonderData.getPathway(player);
        String sefirot  = SefirahHandler.getClaimedSefirot(player);
        String gooName = getNameBySefirot(sefirot);
        transformInternal(player, pathway, gooName);
    }

    /**
     * OP/admin force-transform. {@code gooType} must be either
    * {@code "lord-of-mysteries"}, {@code "eternal-darkness"},
    * {@code "god-almighty"}, or {@code "key-of-light"}.
     */
    public static void transformAs(ServerPlayer player, String gooType) {
        String pathway = BeyonderData.getPathway(player);
        String normalizedType = gooType.replace('_', '-');
        String gooName = switch (normalizedType.toLowerCase(java.util.Locale.ROOT)) {
            case "god-almighty" -> "God Almighty";
            case "eternal-darkness" -> "Eternal Darkness";
            case "key-of-light" -> "Key of Light";
            default -> "Lord of Mysteries";
        };
        transformInternal(player, pathway, gooName);
    }

    private static void transformInternal(ServerPlayer player, String pathway, String gooName) {

        // Update BeyonderComponent
        BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
        component.setCharacteristic(1, greatOldOneSeq, pathway);

        // Update PlayerMap StoredData
        BeyonderData.playerMap.get(player).ifPresent(data -> {
            StoredData updated = StoredData.builder
                    .copyFrom(data)
                    .sequence(greatOldOneSeq)
                    .characteristic(1, greatOldOneSeq, pathway)
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
        component.setCharacteristic(0, greatOldOneSeq, pathway);

        // Update PlayerMap StoredData
        BeyonderData.playerMap.get(player).ifPresent(data -> {
            StoredData updated = StoredData.builder
                    .copyFrom(data)
                    .sequence(0)
                    .characteristic(0, greatOldOneSeq, pathway)
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
