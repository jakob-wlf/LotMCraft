package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncBeyonderDataPacket;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BeyonderData {
    public static final List<String> tempImplementedPathwayGUIs = List.of("");

    public static final String NBT_PATHWAY = "beyonder_pathway";
    public static final String NBT_SEQUENCE = "beyonder_sequence";
    public static final String NBT_SPIRITUALITY = "beyonder_spirituality";
    public static final String NBT_GRIEFING_ENABLED = "beyonder_griefing_enabled";

    private static final int[] spiritualityLookup = {150000, 20000, 10000, 5000, 3900, 1900, 1200, 780, 200, 180};
    private static final double[] multiplier = {5, 3.5, 3, 2.4, 2.0, 1.6, 1.4, 1.2, 1.0, 1.0};

    private static final HashMap<UUID, HashMap<String, Double>> multiplierModifier = new HashMap<>();
    private static final HashMap<UUID, HashSet<String>> disabledBeyonders = new HashMap<>();

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

    public static String getSequenceName(String pathway, int sequence) {
        if(!pathwayInfos.containsKey(pathway))
            return "Unknown";

        PathwayInfos infos = pathwayInfos.get(pathway);
        if(sequence < 0 || sequence >= infos.sequenceNames().length)
            return "Unknown";

        return infos.sequenceNames()[sequence];
    }

    public static final HashMap<String, PathwayInfos> pathwayInfos = new HashMap<>();

    public static void initPathwayInfos() {
        pathwayInfos.put("fool", new PathwayInfos("Fool", "fool", 0xFF864ec7, new String[]{"Fool", "Attendant of Mysteries", "Miracle Invoker", "Scholar of Yore", "Bizarro Sorcerer", "Marionettist", "Faceless", "Magician", "Clown", "Seer"}));
        pathwayInfos.put("error", new PathwayInfos("Error", "error", 0xFF0018b8, new String[]{"Error", "Worm of Time", "Trojan Horse of Destiny", "Mentor of Deceit", "Parasite", "Dream Stealer", "Prometheus", "Cryptologist", "Swindler", "Marauder"}));
        pathwayInfos.put("door", new PathwayInfos("Door", "door", 0xFF89f5f5, new String[]{"Door", "Key of Stars", "Planeswalker", "Wanderer", "Secrets Sorcerer", "Traveler", "Scribe", "Astrologer", "Trickmaster", "Apprentice"}));
        pathwayInfos.put("visionary", new PathwayInfos("Visionary", "visionary", 0xFFe3ffff, new String[]{"Visionary", "Author", "Discerner", "Dream Weaver", "Manipulator", "Dreamwalker", "Hypnotist", "Psychiatrist", "Telepathist", "Spectator"}));
        pathwayInfos.put("sun", new PathwayInfos("Sun", "sun", 0xFFffad33, new String[]{"Sun", "White Angel", "Lightseeker", "Justice Mentor", "Unshadowed", "Priest of Light", "Notary", "Solar High Priest", "Light Supplicant", "Bard"}));
        pathwayInfos.put("tyrant", new PathwayInfos("Tyrant", "tyrant", 0xFF336dff, new String[]{"Tyrant", "Thunder God", "Calamity", "Sea King", "Cataclysmic Interrer", "Ocean Songster", "Wind Blessed", "Seafarer", "Folk of Rage", "Sailor"}));
        pathwayInfos.put("white_tower", new PathwayInfos("White Tower", "white_tower", 0xFF8cadff, new String[]{"White Tower", "Omniscient Eye", "Wisdom Angel", "Cognizer", "Prophet", "Mysticism Magister", "Polymath", "Detective", "Student of Ratiocination", "Reader"}));
        pathwayInfos.put("hanged_man", new PathwayInfos("Hanged Man", "hanged_man", 0xFF8a0a0a, new String[]{"Hanged Man", "Dark Angel", "Profane Presbyter", "Trinity Templar", "Black Knight", "Shepherd", "Rose Bishop", "Shadow Ascetic", "Listener", "Secrets Supplicant"}));
        pathwayInfos.put("darkness", new PathwayInfos("Darkness", "darkness", 0xFF3300b5, new String[]{"Darkness", "Knight of Misfortune", "Servant of Concealment", "Horror Bishop", "Nightwatcher", "Spirit Warlock", "Soul Assurer", "Nightmare", "Midnight Poet", "Sleepless"}));
        pathwayInfos.put("death", new PathwayInfos("Death", "death", 0xFF334f23, new String[]{"Death", "Pale Emperor", "Death Consul", "Ferryman", "Undying", "Gatekeeper", "Spirit Guide", "Spirit Medium", "Gravedigger", "Corpse Collector"}));
        pathwayInfos.put("twilight_giant", new PathwayInfos("Twilight Giant", "twilight_giant", 0xFF944b16, new String[]{"Twilight Giant", "Hand of God", "Glory", "Silver Knight", "Demon Hunter", "Guardian", "Dawn Paladin", "Weapon Master", "Pugilist", "Warrior"}));
        pathwayInfos.put("demoness", new PathwayInfos("Demoness", "demoness", 0xFFc014c9, new String[]{"Demoness", "Demoness of Apocalypse", "Demoness of Catastrophe", "Demoness of Unaging", "Demoness of Despair", "Demoness of Affliction", "Demoness of Pleasure", "Witch", "Instigator", "Asassin"}));
        pathwayInfos.put("red_priest", new PathwayInfos("Red Priest", "red_priest", 0xFFb80000, new String[]{"Red Priest", "Conqueror", "Weather Warlock", "War Bishop", "Iron Blooded Knight", "Reaper", "Conspirer", "Pyromaniac", "Provoker", "Hunter"}));
        pathwayInfos.put("hermit", new PathwayInfos("Hermit", "hermit", 0xFF832ed9, new String[]{"Hermit", "Knowledge Emperor", "Sage", "Clairvoyant", "Mysticologist", "Constellations Master", "Scrolls Professor", "Warlock", "Melee Scholar", "Mystery Pryer"}));
        pathwayInfos.put("paragon", new PathwayInfos("Paragon", "paragon", 0xFFf58e40, new String[]{"Paragon", "Illuminator", "Knowledge Master", "Arcane Scholar", "Alchemist", "Astronomer", "Artisan", "Appraiser", "Archeologist", "Savant"}));
        pathwayInfos.put("wheel_of_fortune", new PathwayInfos("Wheel of Fortune", "wheel_of_fortune", 0xFFbad2f5, new String[]{"Wheel of Fortune", "Snake of Mercury", "Soothsayer", "Chaoswalker", "Misfortune Mage", "Winner", "Calamity Priest", "Lucky One", "Robot", "Monster"}));
        pathwayInfos.put("mother", new PathwayInfos("Mother", "mother", 0xFF6bdb94, new String[]{"Mother", "Naturewalker", "Desolate Matriarch", "Pallbearer", "Classical Alchemist", "Druid", "Biologist", "Harvest Priest", "Doctor", "Planter"}));
        pathwayInfos.put("moon", new PathwayInfos("Moon", "moon", 0xFFf5384b, new String[]{"Moon", "Beauty Goddess", "Life-Giver", "High Summoner", "Shaman King", "Scarlet Scholar", "Potions Professor", "Vampire", "Beast Tamer", "Apothecary"}));
        pathwayInfos.put("abyss", new PathwayInfos("Abyss", "abyss", 0xFFa3070c, new String[]{"Abyss", "Filthy Monarch", "Bloody Archduke", "Blatherer", "Demon", "Desire Apostle", "Devil", "Serial Killer", "Unwinged Angel", "Criminal"}));
        pathwayInfos.put("chained", new PathwayInfos("Chained", "chained", 0xFFb18fbf, new String[]{"Chained", "Abomination", "Ancient Bane", "Disciple of Silence", "Puppet", "Wraith", "Zombie", "Werewolf", "Lunatic", "Prisoner"}));
        pathwayInfos.put("black_emperor", new PathwayInfos("Black Emperor", "black_emperor", 0xFF181040, new String[]{"Black Emperor", "Prince of Abolition", "Duke of Entropy", "Frenzied Mage", "Ear of the Fallen", "Mentor of Disorder", "Baron of Corruption", "Briber", "Barbarian", "Lawyer"}));
        pathwayInfos.put("justiciar", new PathwayInfos("Justiciar", "justiciar", 0xFFfcd99f, new String[]{"Justiciar", "Hand of Order", "Balancer", "Chaos Hunter", "Imperative Mage", "Disciplinary Paladin", "Judge", "Interrogator", "Sheriff", "Arbiter"}));
        pathwayInfos.put("debug", new PathwayInfos("Debug", "debug", 0xFFf5baba, new String[]{"", "", "", "", "", "", "", "", "", ""}));

    }

    public static void setBeyonder(LivingEntity entity, String pathway, int sequence) {
        CompoundTag tag = entity.getPersistentData();
        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);
        tag.putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));
        tag.putBoolean(NBT_GRIEFING_ENABLED, false);

        if(entity instanceof Player player)
            SpiritualityProgressTracker.setProgress(player, 1.0f);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static String getPathway(LivingEntity entity) {
        String pathway = entity.getPersistentData().getString(NBT_PATHWAY);
        if(pathway.isBlank() || pathway.equalsIgnoreCase("") || pathway.isEmpty())
            return "none";
        return pathway;
    }

    public static int getSequence(LivingEntity entity) {
        if (!entity.getPersistentData().contains(NBT_SEQUENCE)) {
            return -1;
        }
        return entity.getPersistentData().getInt(NBT_SEQUENCE);
    }

    public static float getSpirituality(Player player) {
        float spirituality = player.getPersistentData().getFloat(NBT_SPIRITUALITY);
        float maxSpirituality = getMaxSpirituality(getSequence(player));

        if(maxSpirituality <= 0) {
            return 0.0f;
        }

        float progress = spirituality / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player, progress);

        return Math.max(0, spirituality);
    }

    public static void reduceSpirituality(Player player, float amount) {
        float current = getSpirituality(player);
        player.getPersistentData().putFloat(NBT_SPIRITUALITY, Math.max(0, current - amount));

        float maxSpirituality = getMaxSpirituality(getSequence(player));

        if(maxSpirituality <= 0) {
            return;
        }

        float progress = (current - amount) / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player, progress);

        // Sync to client if this is server-side
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static double getMultiplier(LivingEntity entity) {
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

    public static void incrementSpirituality(Player player, float amount) {
        float current = getSpirituality(player);
        float newAmount = Math.min(getMaxSpirituality(getSequence(player)), current + amount);
        player.getPersistentData().putFloat(NBT_SPIRITUALITY, newAmount);

        float maxSpirituality = getMaxSpirituality(getSequence(player));

        if(maxSpirituality <= 0) {
            return;
        }

        float progress = newAmount / maxSpirituality;
        SpiritualityProgressTracker.setProgress(player, progress);

        // Sync to client if this is server-side
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static void resetSpirituality(Player player) {
        int sequence = getSequence(player);
        player.getPersistentData().putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));

        if(getMaxSpirituality(sequence) <= 0) {
            return;
        }

        float progress = player.getPersistentData().getFloat(NBT_SPIRITUALITY) / getMaxSpirituality(sequence);
        SpiritualityProgressTracker.setProgress(player, progress);
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
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            // Send empty data to clear client cache
            SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket("none", -1, 0.0f, false);
            PacketHandler.sendToPlayer(serverPlayer, packet);
        }
    }

    public static boolean isBeyonder(LivingEntity entity) {
        return entity.getPersistentData().contains(NBT_PATHWAY) && entity.getPersistentData().contains(NBT_SEQUENCE);
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
        return player.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);
    }

    public static void setPathway(LivingEntity entity, String pathway) {
        entity.getPersistentData().putString(NBT_PATHWAY, pathway);

        // Sync to client if this is server-side
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    public static void setSequence(LivingEntity entity, int sequence) {
        entity.getPersistentData().putInt(NBT_SEQUENCE, sequence);
        entity.getPersistentData().putFloat(NBT_SPIRITUALITY, getMaxSpirituality(sequence));

        // Sync to client if this is server-side
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
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
            case 1 -> 1;
            case 2 -> 4;
            case 3 -> 7;
            case 4 -> 8;
            default -> 9;
        };
    }
}