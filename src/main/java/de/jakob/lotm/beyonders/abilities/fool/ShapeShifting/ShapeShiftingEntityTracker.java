package de.jakob.lotm.beyonders.abilities.fool.ShapeShifting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MemorisedEntities;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

import static de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil.getEntityTypeString;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShapeShiftingEntityTracker {
    private static final Map<UUID, Map<String, Integer>> trackingData = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // run every 20 ticks
            if (serverPlayer.tickCount % 20 == 0){
                if (BeyonderData.isBeyonder(serverPlayer)){
                    int radius = 5;
                    int requiredTime = 800;
                    if (BeyonderData.getPathway(serverPlayer).equals("fool")) {
                        int sequence = BeyonderData.getSequence(serverPlayer);
                        radius = 5 + (10 - sequence);
                        requiredTime = 800 - ((10 - sequence) * 20);
                    }


                    LivingEntity lookedAtEntity = AbilityUtil.getTargetEntity(serverPlayer, radius, 2);
                    if (lookedAtEntity == null) return;

                    String entityType = getEntityTypeString(lookedAtEntity);

                    // to exclude any other entities in the future
                    switch (entityType) {
                        case "minecraft:ender_dragon" : return;
                        case "minecraft:wither" : return;
                    }

                    MemorisedEntities memorisedEntities = serverPlayer.getData(ModAttachments.MEMORISED_ENTITIES.get());
                    if (memorisedEntities.getMemorisedEntityTypes().contains(entityType)) return;

                    UUID playerId = serverPlayer.getUUID();
                    trackingData.putIfAbsent(playerId, new HashMap<>());
                    Map<String, Integer> playerTracking = trackingData.get(playerId);

                    int currentTime = playerTracking.getOrDefault(entityType, 0) + 20; // add 20 for every second the player is looking at the target
                    playerTracking.put(entityType, currentTime);

                    if (currentTime >= requiredTime) {
                        memorisedEntities.addMemorisedEntity(entityType);
                        sendSuccessMessage(serverPlayer, entityType);
                        playerTracking.remove(entityType);
                    }
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

    private static void sendSuccessMessage(ServerPlayer player, String entityName) {
        if (BeyonderData.getPathway(player).equals("fool")) {
            String name = entityName.split(":")[1];
            player.sendSystemMessage(Component.literal("§fYou memorised the shape of §b" + name));
        }
    }
}