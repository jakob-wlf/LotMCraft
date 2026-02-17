package de.jakob.lotm.abilities.fool.ShapeShifting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MemorisedEntities;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShapeShiftingEntityTracker {
    private static final Map<UUID, Map<String, Integer>> trackingData = new HashMap<>();
    private static int CHECK_INTERVAL = 20, REQUIRED_TIME = 400;
    private static float RADIUS = 5.0f;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.tickCount % CHECK_INTERVAL == 0){
                String pathway = player.getPersistentData().getString("beyonder_pathway");
                int sequence = player.getPersistentData().getInt("beyonder_sequence");
                if (sequence <= 9){
                    if (pathway.equals("fool")){
                        RADIUS = 5.0f + (10 - (float) sequence);
                        REQUIRED_TIME = 400 - ((10 - sequence) * 20);
                    }
                    MemorisedEntities memorisedEntities = player.getData(ModAttachments.MEMORISED_ENTITIES.get());
                    updateTracking(player, memorisedEntities);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            trackingData.remove(player.getUUID());
        }
    }

    private static void updateTracking(ServerPlayer player, MemorisedEntities memorisedEntities) {
        UUID playerId = player.getUUID();
        trackingData.putIfAbsent(playerId, new HashMap<>());
        Map<String, Integer> playerTracking = trackingData.get(playerId);

        List<Entity> nearbyEntities = player.level().getEntities(player,
                player.getBoundingBox().inflate(RADIUS),
                e -> e != player && e.isAlive() && (e instanceof Mob || e instanceof ServerPlayer));

        Set<String> currentlyNearby = new HashSet<>();

        for (Entity entity : nearbyEntities) {
            String entityType = getEntityTypeString(entity);
            currentlyNearby.add(entityType);

            // to exclude any other entities in the future
            switch (entityType) {
                case "minecraft:ender_dragon" : continue;
                case "minecraft:wither" : continue;
            }

            if (memorisedEntities.getMemorisedEntityTypes().contains(entityType)) continue;


            int currentTime = playerTracking.getOrDefault(entityType, 0) + CHECK_INTERVAL;
            playerTracking.put(entityType, currentTime);

            if (currentTime >= REQUIRED_TIME) {
                memorisedEntities.addMemorisedEntity(entityType);
                sendSuccessMessage(player, entityType);
                playerTracking.remove(entityType);
            }
        }

        playerTracking.keySet().removeIf(type -> !currentlyNearby.contains(type));
        if (playerTracking.isEmpty()) trackingData.remove(playerId);
    }

    private static String getEntityTypeString(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return String.format("player:%s:%s", player.getGameProfile().getName(), player.getUUID());
        }
        if (entity instanceof BeyonderNPCEntity npc) {
            return "lotmcraft:beyonder_npc:" + npc.getSkinName();
        }
        return EntityType.getKey(entity.getType()).toString();
    }

    private static void sendSuccessMessage(ServerPlayer player, String entityName) {
        String name = entityName.split(":")[1];
        player.sendSystemMessage(Component.literal("§fYou memorised the shape of §b" + name));
    }
}