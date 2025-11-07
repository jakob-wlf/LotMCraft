package de.jakob.lotm.util;

import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncBeyonderDataPacket;
import de.jakob.lotm.network.packets.SyncLivingEntityBeyonderDataPacket;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class BeyonderData {
    public static final String NBT_PATHWAY = "beyonder_pathway";
    public static final String NBT_SEQUENCE = "beyonder_sequence";
    public static final String NBT_SPIRITUALITY = "beyonder_spirituality";
    public static final String NBT_GRIEFING_ENABLED = "beyonder_griefing_enabled";

    private static final int[] spiritualityLookup = {150000, 20000, 10000, 5000, 3900, 1900, 1200, 780, 200, 180};
    private static final double[] multiplier = {5, 3.5, 3, 2.4, 2.0, 1.6, 1.4, 1.2, 1.0, 1.0};

    private static final HashMap<UUID, HashMap<String, Double>> multiplierModifier = new HashMap<>();
    private static final HashMap<UUID, HashSet<String>> disabledBeyonders = new HashMap<>();

    public static final HashMap<String, List<Integer>> implementedRecipes = new HashMap<>(Map.of(
            "fool", List.of(new Integer[]{9, 8, 7, 6, 5, 4}),
            "door", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3}),
            "sun", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3}),
            "tyrant", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3}),
            "darkness", List.of(new Integer[]{9, 8, 7, 6, 5}),
            "demoness", List.of(new Integer[]{9, 8, 7, 6, 5}),
            "red_priest", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3}),
            "visionary", List.of(new Integer[]{9, 8, 7, 6, 5})
    ));

    public static final List<String> pathways = List.of(
            "fool",
            "error",
            "door",
            "visionary",
            "sun",
            "tyrant",
            "white_tower",
            "hanged_man",
            "darkness",
            "death",
            "twilight_giant",
            "demoness",
            "red_priest",
            "hermit",
            "paragon",
            "wheel_of_fortune",
            "mother",
            "moon",
            "abyss",
            "chained",
            "black_emperor",
            "justiciar"
    );

    public static final List<String> implementedPathways = List.of(
            "fool",
            "door",
            "sun",
            "tyrant",
            "darkness",
            "demoness",
            "red_priest",
            "mother",
            "abyss",
            "visionary",
            "wheel_of_fortune"
    );

    public static int getHighestImplementedSequence(String pathway) {
        return switch (pathway) {
            case "mother" -> 7;
            case "darkness", "visionary" -> 6;
            case "abyss", "wheel_of_fortune" -> 5;
            case "fool" -> 4;
            case "demoness", "red_priest", "sun", "tyrant" -> 3;
            case "door" -> 2;
            default -> 9;
        };
    }

    public static String getSequenceName(String pathway, int sequence) {
        if(!pathwayInfos.containsKey(pathway))
            return "Unknown";

        PathwayInfos infos = pathwayInfos.get(pathway);
        if(sequence < 0 || sequence >= infos.sequenceNames().length)
            return "Unknown";

        return infos.getSequenceName(sequence);
    }

    public static final HashMap<String, PathwayInfos> pathwayInfos = new HashMap<>();

    public static void initPathwayInfos() {
        pathwayInfos.put("fool", new PathwayInfos("fool", 0xFF864ec7, new String[]{"fool", "attendant_of_mysteries", "miracle_invoker", "scholar_of_yore", "bizarro_sorcerer", "marionettist", "faceless", "magician", "clown", "seer"}));
        pathwayInfos.put("error", new PathwayInfos("error", 0xFF0018b8, new String[]{"error", "worm_of_time", "trojan_horse_of_destiny", "mentor_of_deceit", "parasite", "dream_stealer", "prometheus", "cryptologist", "swindler", "marauder"}));
        pathwayInfos.put("door", new PathwayInfos("door", 0xFF89f5f5, new String[]{"door", "key_of_stars", "planeswalker", "wanderer", "secrets_sorcerer", "traveler", "scribe", "astrologer", "trickmaster", "apprentice"}));
        pathwayInfos.put("visionary", new PathwayInfos("visionary", 0xFFe3ffff, new String[]{"visionary", "author", "discerner", "dream_weaver", "manipulator", "dreamwalker", "hypnotist", "psychiatrist", "telepathist", "spectator"}));
        pathwayInfos.put("sun", new PathwayInfos("sun", 0xFFffad33, new String[]{"sun", "white_angel", "lightseeker", "justice_mentor", "unshadowed", "priest_of_light", "notary", "solar_high_priest", "light_supplicant", "bard"}));
        pathwayInfos.put("tyrant", new PathwayInfos("tyrant", 0xFF336dff, new String[]{"tyrant", "thunder_god", "calamity", "sea_king", "cataclysmic_interrer", "ocean_songster", "wind_blessed", "seafarer", "folk_of_rage", "sailor"}));
        pathwayInfos.put("white_tower", new PathwayInfos("white_tower", 0xFF8cadff, new String[]{"white_tower", "omniscient_eye", "wisdom_angel", "cognizer", "prophet", "mysticism_magister", "polymath", "detective", "student_of_ratiocination", "reader"}));
        pathwayInfos.put("hanged_man", new PathwayInfos("hanged_man", 0xFF8a0a0a, new String[]{"hanged_man", "dark_angel", "profane_presbyter", "trinity_templar", "black_knight", "shepherd", "rose_bishop", "shadow_ascetic", "listener", "secrets_supplicant"}));
        pathwayInfos.put("darkness", new PathwayInfos("darkness", 0xFF3300b5, new String[]{"darkness", "knight_of_misfortune", "servant_of_concealment", "horror_bishop", "nightwatcher", "spirit_warlock", "soul_assurer", "nightmare", "midnight_poet", "sleepless"}));
        pathwayInfos.put("death", new PathwayInfos("death", 0xFF334f23, new String[]{"death", "pale_emperor", "death_consul", "ferryman", "undying", "gatekeeper", "spirit_guide", "spirit_medium", "gravedigger", "corpse_collector"}));
        pathwayInfos.put("twilight_giant", new PathwayInfos("twilight_giant", 0xFF944b16, new String[]{"twilight_giant", "hand_of_god", "glory", "silver_knight", "demon_hunter", "guardian", "dawn_paladin", "weapon_master", "pugilist", "warrior"}));
        pathwayInfos.put("demoness", new PathwayInfos("demoness", 0xFFc014c9, new String[]{"demoness", "demoness_of_apocalypse", "demoness_of_catastrophe", "demoness_of_unaging", "demoness_of_despair", "demoness_of_affliction", "demoness_of_pleasure", "witch", "instigator", "asassin"}));
        pathwayInfos.put("red_priest", new PathwayInfos("red_priest", 0xFFb80000, new String[]{"red_priest", "conqueror", "weather_warlock", "war_bishop", "iron_blooded_knight", "reaper", "conspirer", "pyromaniac", "provoker", "hunter"}));
        pathwayInfos.put("hermit", new PathwayInfos("hermit", 0xFF832ed9, new String[]{"hermit", "knowledge_emperor", "sage", "clairvoyant", "mysticologist", "constellations_master", "scrolls_professor", "warlock", "melee_scholar", "mystery_pryer"}));
        pathwayInfos.put("paragon", new PathwayInfos("paragon", 0xFFf58e40, new String[]{"paragon", "illuminator", "knowledge_master", "arcane_scholar", "alchemist", "astronomer", "artisan", "appraiser", "archeologist", "savant"}));
        pathwayInfos.put("wheel_of_fortune", new PathwayInfos("wheel_of_fortune", 0xFFbad2f5, new String[]{"wheel_of_fortune", "snake_of_mercury", "soothsayer", "chaoswalker", "misfortune_mage", "winner", "calamity_priest", "lucky_one", "robot", "monster"}));
        pathwayInfos.put("mother", new PathwayInfos("mother", 0xFF6bdb94, new String[]{"mother", "naturewalker", "desolate_matriarch", "pallbearer", "classical_alchemist", "druid", "biologist", "harvest_priest", "doctor", "planter"}));
        pathwayInfos.put("moon", new PathwayInfos("moon", 0xFFf5384b, new String[]{"moon", "beauty_goddess", "life-giver", "high_summoner", "shaman_king", "scarlet_scholar", "potions_professor", "vampire", "beast_tamer", "apothecary"}));
        pathwayInfos.put("abyss", new PathwayInfos("abyss", 0xFFa3070c, new String[]{"abyss", "filthy_monarch", "bloody_archduke", "blatherer", "demon", "desire_apostle", "devil", "serial_killer", "unwinged_angel", "criminal"}));
        pathwayInfos.put("chained", new PathwayInfos("chained", 0xFFb18fbf, new String[]{"chained", "abomination", "ancient_bane", "disciple_of_silence", "puppet", "wraith", "zombie", "werewolf", "lunatic", "prisoner"}));
        pathwayInfos.put("black_emperor", new PathwayInfos("black_emperor", 0xFF181040, new String[]{"black_emperor", "prince_of_abolition", "duke_of_entropy", "frenzied_mage", "ear_of_the_fallen", "mentor_of_disorder", "baron_of_corruption", "briber", "barbarian", "lawyer"}));
        pathwayInfos.put("justiciar", new PathwayInfos("justiciar", 0xFFfcd99f, new String[]{"justiciar", "hand_of_order", "balancer", "chaos_hunter", "imperative_mage", "disciplinary_paladin", "judge", "interrogator", "sheriff", "arbiter"}));


    }

    public static void setBeyonder(LivingEntity entity, String pathway, int sequence) {
        CompoundTag tag = entity.getPersistentData();
        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);
        tag.putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));
        tag.putBoolean(NBT_GRIEFING_ENABLED, true);

        if(entity instanceof Player player)
            SpiritualityProgressTracker.setProgress(player.getUUID(), 1.0f);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide()) {
            if(entity instanceof ServerPlayer serverPlayer)
                PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
            else {
                PacketHandler.syncBeyonderDataToEntity(entity);
            }
        }
    }

    public static String getPathway(LivingEntity entity) {
        if(entity.level().isClientSide) {
            return ClientBeyonderCache.getPathway(entity.getUUID());
        }
        if(!entity.getPersistentData().contains(NBT_PATHWAY)) {
            return "none";
        }

        String pathway = entity.getPersistentData().getString(NBT_PATHWAY);

        if(pathway.isBlank() || pathway.isEmpty())
            return "none";
        return pathway;
    }

    public static int getSequence(LivingEntity entity) {
        if(entity.level().isClientSide) {
            return ClientBeyonderCache.getSequence(entity.getUUID());
        }
        if (!entity.getPersistentData().contains(NBT_SEQUENCE)) {
            return -1;
        }
        return entity.getPersistentData().getInt(NBT_SEQUENCE);
    }

    public static float getSpirituality(LivingEntity entity) {
        if(entity.level().isClientSide) {
            return ClientBeyonderCache.getSpirituality(entity.getUUID());
        }
        if(!(entity instanceof Player player))
            return getMaxSpirituality(getSequence(entity));
        float spirituality = entity.getPersistentData().getFloat(NBT_SPIRITUALITY);
        float maxSpirituality = getMaxSpirituality(getSequence(entity));

        if(maxSpirituality <= 0) {
            return 0.0f;
        }

        float progress = spirituality / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player.getUUID(), progress);

        return Math.max(0, spirituality);
    }

    public static void reduceSpirituality(LivingEntity entity, float amount) {
        if(!(entity instanceof Player player))
            return;
        float current = getSpirituality(entity);
        entity.getPersistentData().putFloat(NBT_SPIRITUALITY, Math.max(0, current - amount));

        float maxSpirituality = getMaxSpirituality(getSequence(entity));

        if(maxSpirituality <= 0) {
            return;
        }

        float progress = (current - amount) / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player.getUUID(), progress);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static double getMultiplier(LivingEntity entity) {
        if(!BeyonderData.isBeyonder(entity))
            return 1;
        int sequence = getSequence(entity);

        if (sequence < 0 || sequence >= multiplier.length) {
            return 1.0; // Default multiplier if sequence is invalid
        }

        double damageMultiplier = multiplier[sequence];

        if(!multiplierModifier.containsKey(entity.getUUID()))
            return damageMultiplier;

        for(double d : multiplierModifier.get(entity.getUUID()).values()) {
            damageMultiplier *= d;
        }

        return damageMultiplier;
    }

    public static void incrementSpirituality(LivingEntity entity, float amount) {
        if(!(entity instanceof Player player))
            return;

        float current = getSpirituality(player);
        float newAmount = Math.min(getMaxSpirituality(getSequence(player)), current + amount);
        player.getPersistentData().putFloat(NBT_SPIRITUALITY, newAmount);

        float maxSpirituality = getMaxSpirituality(getSequence(player));

        if(maxSpirituality <= 0) {
            return;
        }

        float progress = newAmount / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player.getUUID(), progress);

        // Sync to client if this is server-side
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static void resetSpirituality(LivingEntity entity) {
        if(!(entity instanceof Player player))
            return;

        int sequence = getSequence(player);
        player.getPersistentData().putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));

        if(getMaxSpirituality(sequence) <= 0) {
            return;
        }

        float progress = player.getPersistentData().getFloat(NBT_SPIRITUALITY) / getMaxSpirituality(sequence);
        SpiritualityProgressTracker.setProgress(player.getUUID(), progress);
    }

    public static float getMaxSpirituality(int sequence) {
        return sequence >= 0 && sequence < spiritualityLookup.length ? spiritualityLookup[sequence] : 0.0f;
    }

    public static void clearBeyonderData(LivingEntity entity) {
        entity.getPersistentData().remove(NBT_PATHWAY);
        entity.getPersistentData().remove(NBT_SEQUENCE);
        entity.getPersistentData().remove(NBT_SPIRITUALITY);
        entity.getPersistentData().remove(NBT_GRIEFING_ENABLED);
        if(entity instanceof Player player)
            SpiritualityProgressTracker.removeProgress(player);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide()) {
            if(entity instanceof ServerPlayer serverPlayer) {
                // Send empty data to clear client cache
                SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket("none", -1, 0.0f, false);
                PacketHandler.sendToPlayer(serverPlayer, packet);
            }
            else {
                SyncLivingEntityBeyonderDataPacket packet =
                        new SyncLivingEntityBeyonderDataPacket(entity.getId(), "none", -1, 0.0f);
                PacketHandler.sendToAllPlayers(packet); // broadcast to all players tracking this entity
            }
        }
    }

    public static boolean isBeyonder(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return ClientBeyonderCache.isBeyonder(entity.getUUID());
        }
        return (entity.getPersistentData().contains(NBT_PATHWAY) && entity.getPersistentData().contains(NBT_SEQUENCE));
    }

    public static void addModifier(LivingEntity entity, String id, double modifier) {
        UUID uuid = entity.getUUID();

        multiplierModifier.putIfAbsent(uuid, new HashMap<>());
        multiplierModifier.get(uuid).put(id, modifier);
    }

    public static void removeModifier(LivingEntity entity, String id) {
        UUID uuid = entity.getUUID();

        multiplierModifier.putIfAbsent(uuid, new HashMap<>());
        multiplierModifier.get(uuid).remove(id);
    }

    public static boolean isAbilityDisabled(LivingEntity entity) {
        return disabledBeyonders.containsKey(entity.getUUID());
    }

    public static void disableAbilityUse(LivingEntity entity, String id) {
        UUID uuid = entity.getUUID();

        disabledBeyonders.putIfAbsent(uuid, new HashSet<>());
        disabledBeyonders.get(uuid).add(id);
    }

    public static void enableAbilityUse(LivingEntity entity, String id) {
        UUID uuid = entity.getUUID();

        disabledBeyonders.putIfAbsent(uuid, new HashSet<>());
        disabledBeyonders.get(uuid).remove(id);
        if(disabledBeyonders.get(uuid).isEmpty())
            disabledBeyonders.remove(uuid);
    }

    public static boolean isGriefingEnabled(Player player) {
        if (player.level().isClientSide()) {
            // On client side, read from cache instead of NBT
            return ClientBeyonderCache.isGriefingEnabled(player.getUUID());
        }
        return player.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);
    }

    public static boolean isGriefingEnabled(LivingEntity entity) {
        if(!(entity instanceof Player player)) {
            return false;
        }
        return isGriefingEnabled(player);
    }

    public static void setPathway(LivingEntity entity, String pathway) {
        entity.getPersistentData().putString(NBT_PATHWAY, pathway);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide()) {
            if(entity instanceof ServerPlayer serverPlayer)
                PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
            else
                PacketHandler.syncBeyonderDataToEntity(entity);
        }
    }

    public static void setSequence(LivingEntity entity, int sequence) {
        entity.getPersistentData().putInt(NBT_SEQUENCE, sequence);
        entity.getPersistentData().putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));

        // Sync to client if this is server-side
        if (!entity.level().isClientSide()) {
            if(entity instanceof ServerPlayer serverPlayer)
                PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
            else
                PacketHandler.syncBeyonderDataToEntity(entity);
        }
    }

    private static float getRelativeSpirituality(Player player) {
        float maxSpirituality = getMaxSpirituality(getSequence(player));
        if (maxSpirituality <= 0) {
            return 0.0f;
        }
        return getSpirituality(player) / maxSpirituality;
    }

    public static void setGriefingEnabled(Player player, boolean enabled) {
        player.getPersistentData().putBoolean(NBT_GRIEFING_ENABLED, enabled);

        // Sync to client if this is server-side
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static void advance(LivingEntity entity, String pathway, int sequence) {
        if(entity instanceof Player player && player.isCreative()) {
            setBeyonder(entity, pathway, sequence);
            return;
        }
        if(!isBeyonder(entity)) {
            setBeyonder(entity, pathway, sequence);
            int difference = 10 - sequence;
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, getAmplifierBySequenceDifference(difference)));
            return;
        }
        String prevPathway = getPathway(entity);
        if(!prevPathway.equals(pathway)) {
            setBeyonder(entity, pathway, sequence);
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 9));
            return;
        }

        int prevSequence = getSequence(entity);
        if(prevSequence <= sequence) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 2));
            return;
        }

        int difference = prevSequence - sequence;

        setBeyonder(entity, pathway, sequence);
        entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, getAmplifierBySequenceDifference(difference)));
    }

    private static int getAmplifierBySequenceDifference(int difference) {
        return switch (difference) {
            case 1 -> 0;
            case 2 -> 4;
            case 3 -> 7;
            case 4 -> 8;
            default -> 9;
        };
    }
}