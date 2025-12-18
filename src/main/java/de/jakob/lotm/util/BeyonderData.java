package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncBeyonderDataPacket;
import de.jakob.lotm.network.packets.toClient.SyncLivingEntityBeyonderDataPacket;
import de.jakob.lotm.util.beyonderMap.BeyonderMap;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BeyonderData {
    public static final String NBT_PATHWAY = "beyonder_pathway";
    public static final String NBT_SEQUENCE = "beyonder_sequence";
    public static final String NBT_SPIRITUALITY = "beyonder_spirituality";
    public static final String NBT_GRIEFING_ENABLED = "beyonder_griefing_enabled";
    public static final String NBT_DIGESTION_PROGRESS = "beyonder_digestion_progress";

    private static final int[] spiritualityLookup = {150000, 20000, 10000, 5000, 3900, 1900, 1200, 780, 200, 180};
    private static final double[] multiplier = {9, 4.25, 3.25, 2.15, 1.85, 1.4, 1.25, 1.1, 1.0, 1.0};

    private static final HashMap<UUID, HashMap<String, Double>> multiplierModifier = new HashMap<>();
    private static final HashMap<UUID, HashMap<String, Long>> modifierTimeouts = new HashMap<>();

    private static final HashMap<UUID, HashMap<String, Long>> abilityDisablingTimeouts = new HashMap<>();
    private static final HashMap<UUID, HashSet<String>> disabledBeyonders = new HashMap<>();

    public static final HashMap<String, List<Integer>> implementedRecipes = new HashMap<>();

    public static BeyonderMap beyonderMap;

    static {
        implementedRecipes.put("fool", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3}));
        implementedRecipes.put("door", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1}));
        implementedRecipes.put("sun", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1}));
        implementedRecipes.put("tyrant", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1}));
        implementedRecipes.put("darkness", List.of(new Integer[]{9, 8, 7, 6, 5}));
        implementedRecipes.put("demoness", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1}));
        implementedRecipes.put("red_priest", List.of(new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1}));
        implementedRecipes.put("visionary", List.of(new Integer[]{9, 8, 7, 6, 5}));
        implementedRecipes.put("mother", List.of(new Integer[]{9, 8, 7, 6, 5}));
        implementedRecipes.put("abyss", List.of(new Integer[]{9, 8, 7, 6, 5}));
        implementedRecipes.put("wheel_of_fortune", List.of(new Integer[]{9, 8, 7, 6, 5}));

    }

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
            "error",
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
            case "darkness" -> 6;
            case "abyss", "wheel_of_fortune", "visionary" -> 5;
            case "fool" -> 3;
            case "error" -> 2;
            case "demoness", "red_priest", "sun", "tyrant", "door" -> 1;
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

    public static void initBeyonderMap(ServerLevel level){
        beyonderMap = BeyonderMap.get(level);
    }

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
        pathwayInfos.put("demoness", new PathwayInfos("demoness", 0xFFc014c9, new String[]{"demoness", "demoness_of_apocalypse", "demoness_of_catastrophe", "demoness_of_unaging", "demoness_of_despair", "demoness_of_affliction", "demoness_of_pleasure", "witch", "instigator", "assassin"}));
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
        if(entity.level() instanceof ServerLevel serverLevel) {
            callPassiveEffectsOnRemoved(entity, serverLevel);
        }

        if(entity instanceof ServerPlayer) {
            if(!beyonderMap.check(pathway, sequence)) return;
        }

        boolean griefing = !BeyonderData.isBeyonder(entity) || BeyonderData.isGriefingEnabled(entity);

        CompoundTag tag = entity.getPersistentData();
        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);
        tag.putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));
        tag.putBoolean(NBT_GRIEFING_ENABLED, griefing);
        tag.putFloat(NBT_DIGESTION_PROGRESS, 0.0f);

        if(entity instanceof Player player)
            SpiritualityProgressTracker.setProgress(player.getUUID(), 1.0f);

        // Sync to client if this is server-side
        if (entity.level() instanceof ServerLevel serverLevel) {
            callPassiveEffectsOnAdd(entity, serverLevel);

            if(entity instanceof ServerPlayer serverPlayer) {
                PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
                beyonderMap.put(serverPlayer);
            }
            else {
                PacketHandler.syncBeyonderDataToEntity(entity);
            }
        }


    }

    private static void callPassiveEffectsOnRemoved(LivingEntity entity, ServerLevel serverLevel) {
        List<PassiveAbilityItem> passiveAbilities = new ArrayList<>(PassiveAbilityHandler.ITEMS.getEntries().stream().filter(entry -> {
            if (!(entry.get() instanceof PassiveAbilityItem abilityItem))
                return false;
            return abilityItem.shouldApplyTo(entity);
        }).map(entry -> (PassiveAbilityItem) entry.get()).toList());

        for (PassiveAbilityItem ability : passiveAbilities) {
            ability.onPassiveAbilityRemoved(entity, serverLevel);
        }
    }

    private static void callPassiveEffectsOnAdd(LivingEntity entity, ServerLevel serverLevel) {
        List<PassiveAbilityItem> passiveAbilities = new ArrayList<>(PassiveAbilityHandler.ITEMS.getEntries().stream().filter(entry -> {
            if (!(entry.get() instanceof PassiveAbilityItem abilityItem))
                return false;
            return abilityItem.shouldApplyTo(entity);
        }).map(entry -> (PassiveAbilityItem) entry.get()).toList());

        for (PassiveAbilityItem ability : passiveAbilities) {
            ability.onPassiveAbilityGained(entity, serverLevel);
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
        if (sequence < 0 || sequence >= multiplier.length)
            return 1.0;

        UUID uuid = entity.getUUID();

        // --- CLEANUP EXPIRED MODIFIERS ---
        if(modifierTimeouts.containsKey(uuid)) {
            HashSet<String> expired = new HashSet<>();

            for(Map.Entry<String, Long> entry : modifierTimeouts.get(uuid).entrySet()) {
                if(System.currentTimeMillis() >= entry.getValue()) {
                    expired.add(entry.getKey());
                }
            }

            for(String id : expired) {
                modifierTimeouts.get(uuid).remove(id);
                if(multiplierModifier.containsKey(uuid)) {
                    multiplierModifier.get(uuid).remove(id);
                }
            }

            if(modifierTimeouts.get(uuid).isEmpty())
                modifierTimeouts.remove(uuid);
        }

        double damageMultiplier = multiplier[sequence];

        if(!multiplierModifier.containsKey(uuid))
            return damageMultiplier;

        for(double d : multiplierModifier.get(uuid).values()) {
            damageMultiplier *= d;
        }

        return damageMultiplier;
    }


    public static double getMultiplierForSequence(int sequence) {
        return multiplier[sequence];
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
        entity.getPersistentData().remove(NBT_DIGESTION_PROGRESS);
        if(entity instanceof Player player) {
            SpiritualityProgressTracker.removeProgress(player);
            beyonderMap.put(player);
        }

        // Sync to client if this is server-side
        if (!entity.level().isClientSide()) {
            if(entity instanceof ServerPlayer serverPlayer) {
                // Send empty data to clear client cache
                SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket("none", -1, 0.0f, false, 0.0f);
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

        if(multiplierModifier.containsKey(uuid)) {
            multiplierModifier.get(uuid).remove(id);
            if(multiplierModifier.get(uuid).isEmpty())
                multiplierModifier.remove(uuid);
        }

        if(modifierTimeouts.containsKey(uuid)) {
            modifierTimeouts.get(uuid).remove(id);
            if(modifierTimeouts.get(uuid).isEmpty())
                modifierTimeouts.remove(uuid);
        }
    }


    public static void addModifierWithTimeLimit(
            LivingEntity entity,
            String id,
            double modifier,
            long millis
    ) {
        UUID uuid = entity.getUUID();

        multiplierModifier.putIfAbsent(uuid, new HashMap<>());
        multiplierModifier.get(uuid).put(id, modifier);

        modifierTimeouts.putIfAbsent(uuid, new HashMap<>());
        modifierTimeouts.get(uuid).put(id, System.currentTimeMillis() + millis);
    }



    public static boolean isAbilityDisabled(LivingEntity entity) {
        final HashSet<String> idsToRemoveDueToTimeLimit = new HashSet<>();
        if(abilityDisablingTimeouts.containsKey(entity.getUUID())) {
            for(Map.Entry<String, Long> entry : abilityDisablingTimeouts.get(entity.getUUID()).entrySet()) {
                if(System.currentTimeMillis() >= entry.getValue()) {
                    idsToRemoveDueToTimeLimit.add(entry.getKey());
                }
            }

            for (String id : idsToRemoveDueToTimeLimit) {
                abilityDisablingTimeouts.get(entity.getUUID()).remove(id);
                if(disabledBeyonders.containsKey(entity.getUUID())) {
                    disabledBeyonders.get(entity.getUUID()).remove(id);
                }
            }

            if(disabledBeyonders.containsKey(entity.getUUID()) && disabledBeyonders.get(entity.getUUID()).isEmpty()) {
                disabledBeyonders.remove(entity.getUUID());
            }
        }

        return disabledBeyonders.containsKey(entity.getUUID());
    }

    public static void disableAbilityUse(LivingEntity entity, String id) {
        UUID uuid = entity.getUUID();

        disabledBeyonders.putIfAbsent(uuid, new HashSet<>());
        disabledBeyonders.get(uuid).add(id);
    }

    public static void disableAbilityUseWithTimeLimit(LivingEntity entity, String id, long millis) {
        UUID uuid = entity.getUUID();

        disabledBeyonders.putIfAbsent(uuid, new HashSet<>());
        disabledBeyonders.get(uuid).add(id);

        abilityDisablingTimeouts.putIfAbsent(uuid, new HashMap<>());
        abilityDisablingTimeouts.get(uuid).put(id, System.currentTimeMillis() + millis);
    }

    public static void enableAbilityUse(LivingEntity entity, String id) {
        UUID uuid = entity.getUUID();

        disabledBeyonders.putIfAbsent(uuid, new HashSet<>());
        disabledBeyonders.get(uuid).remove(id);
        if(disabledBeyonders.get(uuid).isEmpty())
            disabledBeyonders.remove(uuid);

        abilityDisablingTimeouts.putIfAbsent(uuid, new HashMap<>());
        abilityDisablingTimeouts.get(uuid).remove(id);
        if(abilityDisablingTimeouts.get(uuid).isEmpty())
            abilityDisablingTimeouts.remove(uuid);
    }

    public static boolean isGriefingEnabled(Player player) {
        if (player.level().isClientSide()) {
            // On client side, read from cache instead of NBT
            return ClientBeyonderCache.isGriefingEnabled(player.getUUID());
        }

        if(!player.level().getGameRules().getBoolean(ModGameRules.ALLOW_GRIEFING)) {
            return false;
        }

        return player.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);
    }

    public static float getDigestionProgress(Player player) {
        if(player.level().isClientSide) {
            return ClientBeyonderCache.getDigestionProgress(player.getUUID());
        }

        if(!player.getPersistentData().contains(NBT_DIGESTION_PROGRESS)) {
            return 0.0f;
        }
        return player.getPersistentData().getFloat(NBT_DIGESTION_PROGRESS);
    }

    public static void digest(Player player, float amount) {
        float current = getDigestionProgress(player);
        float newAmount = Math.min(1.0f, current + amount);
        if(newAmount == 1.0f) {
            AbilityUtil.sendActionBar(player, Component.translatable("lotm.digested").withColor(0xbd64d1));
            if(player.level() instanceof ServerLevel serverLevel) {
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, player.position().add(0, player.getEyeHeight() / 2, 0), 30, .5, player.getEyeHeight() / 2, .5, 0.06);
            }
            else {
                player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1, 1);
            }
        }
        player.getPersistentData().putFloat(NBT_DIGESTION_PROGRESS, newAmount);

        // Sync to client if this is server-side
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
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

        // Get sanity value (0 = no sanity, 1 = full sanity)
        SanityComponent sanityComp = entity.getData(ModAttachments.SANITY_COMPONENT);
        float sanity = sanityComp.getSanity();

        if(!isBeyonder(entity)) {
            setBeyonder(entity, pathway, sequence);
            int difference = 10 - sequence;
            int amplifier = calculateAmplifier(difference, 0, sanity);
            int duration = calculateDuration(difference, sanity);
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier));
            return;
        }

        String prevPathway = getPathway(entity);
        if(!prevPathway.equals(pathway)) {
            setBeyonder(entity, pathway, sequence);
            int amplifier = calculateAmplifier(10, 0, sanity); // Maximum risk for wrong pathway
            int duration = calculateDuration(10, sanity);
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier));
            return;
        }

        float digestionProgress = 0;
        if(entity instanceof Player player) {
            digestionProgress = BeyonderData.getDigestionProgress(player);
        }

        if(digestionProgress < 0.95) {
            setBeyonder(entity, pathway, sequence);
            int prevSequence = getSequence(entity);
            int difference = prevSequence - sequence;
            int amplifier = calculateAmplifier(difference, digestionProgress, sanity);
            int duration = calculateDuration(difference, sanity);
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier));
            return;
        }

        int prevSequence = getSequence(entity);
        if(prevSequence <= sequence) {
            int duration = 20 * 5; // Standard duration for invalid advancement
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, 2));
            return;
        }

        int difference = prevSequence - sequence;

        setBeyonder(entity, pathway, sequence);
        int amplifier = calculateAmplifier(difference, digestionProgress, sanity);
        int duration = calculateDuration(difference, sanity);
        entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier));
    }

    /**
     * Calculate amplifier based on sequence difference, digestion progress, and sanity
     * Returns 0 for perfect conditions (difference=1, full digestion, full sanity)
     */
    private static int calculateAmplifier(int sequenceDifference, float digestion, float sanity) {
        // Base amplifier from sequence difference
        int baseAmplifier = getAmplifierBySequenceDifference(sequenceDifference);

        // Increase risk based on incomplete digestion (0.95 to 1.0 = no penalty, below 0.95 = penalty)
        float digestionPenalty = digestion < 0.95 ? (0.95f - digestion) * 10 : 0; // 0 to ~9.5 range

        // Increase risk based on low sanity (lower sanity = higher penalty)
        float sanityPenalty = (1.0f - sanity) * 5; // 0 to 5 range

        // Combine penalties
        int totalAmplifier = baseAmplifier + Math.round(digestionPenalty + sanityPenalty);

        // Cap at 9 (100% death rate above 8)
        return Math.min(9, totalAmplifier);
    }

    /**
     * Calculate duration based on sequence difference and sanity
     * Higher sequence advancement (lower number) = longer duration
     * Lower sanity = longer duration
     */
    private static int calculateDuration(int sequenceDifference, float sanity) {
        // Base duration increases with sequence difference
        // Higher sequences (numerically lower) mean bigger jumps in power
        int baseDuration = 20 * 5 * sequenceDifference; // 5 seconds per sequence difference

        // Multiply by sanity penalty (lower sanity = longer duration)
        // At full sanity (1.0): multiplier = 1.0
        // At no sanity (0.0): multiplier = 3.0
        float sanityMultiplier = 1.0f + (2.0f * (1.0f - sanity));

        return Math.round(baseDuration * sanityMultiplier);
    }

    // Add these fields after the existing disabledBeyonders field
    private static final HashMap<UUID, HashMap<String, HashSet<String>>> disabledAbilities = new HashMap<>();
    private static final HashMap<UUID, HashMap<String, HashMap<String, Long>>> abilitySpecificDisablingTimeouts = new HashMap<>();
// Structure: UUID -> abilityId -> reasonId -> timeout

    // Method to check if a specific ability is disabled
    public static boolean isSpecificAbilityDisabled(LivingEntity entity, String abilityId) {
        UUID uuid = entity.getUUID();

        // Clean up expired timeouts first
        if(abilitySpecificDisablingTimeouts.containsKey(uuid)) {
            HashMap<String, HashMap<String, Long>> abilitiesMap = abilitySpecificDisablingTimeouts.get(uuid);

            if(abilitiesMap.containsKey(abilityId)) {
                HashMap<String, Long> reasonTimeouts = abilitiesMap.get(abilityId);
                final HashSet<String> reasonIdsToRemove = new HashSet<>();

                for(Map.Entry<String, Long> entry : reasonTimeouts.entrySet()) {
                    if(System.currentTimeMillis() >= entry.getValue()) {
                        reasonIdsToRemove.add(entry.getKey());
                    }
                }

                for(String reasonId : reasonIdsToRemove) {
                    reasonTimeouts.remove(reasonId);
                    if(disabledAbilities.containsKey(uuid) && disabledAbilities.get(uuid).containsKey(reasonId)) {
                        disabledAbilities.get(uuid).get(reasonId).remove(abilityId);
                        if(disabledAbilities.get(uuid).get(reasonId).isEmpty()) {
                            disabledAbilities.get(uuid).remove(reasonId);
                        }
                    }
                }

                if(reasonTimeouts.isEmpty()) {
                    abilitiesMap.remove(abilityId);
                }
            }

            if(abilitiesMap.isEmpty()) {
                abilitySpecificDisablingTimeouts.remove(uuid);
            }
        }

        if(disabledAbilities.containsKey(uuid) && disabledAbilities.get(uuid).isEmpty()) {
            disabledAbilities.remove(uuid);
        }

        // Check if ability is disabled
        if(!disabledAbilities.containsKey(uuid)) {
            return false;
        }

        for(HashSet<String> abilities : disabledAbilities.get(uuid).values()) {
            if(abilities.contains(abilityId)) {
                return true;
            }
        }
        return false;
    }

    // Disable a specific ability with a reason ID
    public static void disableSpecificAbility(LivingEntity entity, String reasonId, String abilityId) {
        UUID uuid = entity.getUUID();

        disabledAbilities.putIfAbsent(uuid, new HashMap<>());
        disabledAbilities.get(uuid).putIfAbsent(reasonId, new HashSet<>());
        disabledAbilities.get(uuid).get(reasonId).add(abilityId);
    }

    // Disable specific abilities with a reason ID and time limit
    public static void disableSpecificAbilityWithTimeLimit(LivingEntity entity, String reasonId, String abilityId, long millis) {
        disableSpecificAbility(entity, reasonId, abilityId);

        UUID uuid = entity.getUUID();
        abilitySpecificDisablingTimeouts.putIfAbsent(uuid, new HashMap<>());
        abilitySpecificDisablingTimeouts.get(uuid).putIfAbsent(abilityId, new HashMap<>());
        abilitySpecificDisablingTimeouts.get(uuid).get(abilityId).put(reasonId, System.currentTimeMillis() + millis);
    }

    // Disable multiple abilities at once with a reason ID
    public static void disableSpecificAbilities(LivingEntity entity, String reasonId, String... abilityIds) {
        UUID uuid = entity.getUUID();

        disabledAbilities.putIfAbsent(uuid, new HashMap<>());
        disabledAbilities.get(uuid).putIfAbsent(reasonId, new HashSet<>());

        for(String abilityId : abilityIds) {
            disabledAbilities.get(uuid).get(reasonId).add(abilityId);
        }
    }

    // Disable multiple abilities with a reason ID and time limit
    public static void disableSpecificAbilitiesWithTimeLimit(LivingEntity entity, String reasonId, long millis, String... abilityIds) {
        disableSpecificAbilities(entity, reasonId, abilityIds);

        UUID uuid = entity.getUUID();
        abilitySpecificDisablingTimeouts.putIfAbsent(uuid, new HashMap<>());

        for(String abilityId : abilityIds) {
            abilitySpecificDisablingTimeouts.get(uuid).putIfAbsent(abilityId, new HashMap<>());
            abilitySpecificDisablingTimeouts.get(uuid).get(abilityId).put(reasonId, System.currentTimeMillis() + millis);
        }
    }

    // Enable specific ability by removing a reason ID
    public static void enableSpecificAbility(LivingEntity entity, String reasonId) {
        UUID uuid = entity.getUUID();

        if(disabledAbilities.containsKey(uuid) && disabledAbilities.get(uuid).containsKey(reasonId)) {
            HashSet<String> abilitiesToRemove = new HashSet<>(disabledAbilities.get(uuid).get(reasonId));
            disabledAbilities.get(uuid).remove(reasonId);

            if(disabledAbilities.get(uuid).isEmpty()) {
                disabledAbilities.remove(uuid);
            }

            // Remove the timeouts for this reason ID from all affected abilities
            if(abilitySpecificDisablingTimeouts.containsKey(uuid)) {
                for(String abilityId : abilitiesToRemove) {
                    if(abilitySpecificDisablingTimeouts.get(uuid).containsKey(abilityId)) {
                        abilitySpecificDisablingTimeouts.get(uuid).get(abilityId).remove(reasonId);
                        if(abilitySpecificDisablingTimeouts.get(uuid).get(abilityId).isEmpty()) {
                            abilitySpecificDisablingTimeouts.get(uuid).remove(abilityId);
                        }
                    }
                }

                if(abilitySpecificDisablingTimeouts.get(uuid).isEmpty()) {
                    abilitySpecificDisablingTimeouts.remove(uuid);
                }
            }
        }
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