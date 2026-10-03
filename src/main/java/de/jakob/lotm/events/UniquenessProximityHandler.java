package de.jakob.lotm.events;

import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = "lotmcraft")
public class UniquenessProximityHandler {

    private static final int PROXIMITY_CHECK_INTERVAL = 600;
    private static final double PROXIMITY_RANGE = 500.0;

    private static final Map<String, Long> lastProximityMessageTime = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();

        if (level.getGameTime() % PROXIMITY_CHECK_INTERVAL != 0) return;

        for (ServerPlayer player : level.players()) {
            checkPlayerProximity(player, level);
        }
    }

    private static void checkPlayerProximity(ServerPlayer player, ServerLevel level) {
        String playerPathway = BeyonderData.getPathway(player);
        int playerSeq = BeyonderData.getSequence(player);

        if (playerSeq != 1) return;
        if (playerPathway.isEmpty()) return;

        if (!UniquenessEntity.existsInWorld(level, playerPathway)) return;

        if (UniquenessEntity.ACTIVE_ENTITIES.containsKey(playerPathway)) {
            int entityId = UniquenessEntity.ACTIVE_ENTITIES.get(playerPathway);
            net.minecraft.world.entity.Entity uniquenessEntity = level.getEntity(entityId);

            if (uniquenessEntity != null && !uniquenessEntity.isRemoved()) {
                Vec3 playerPos = player.position();
                Vec3 uniquenessPos = uniquenessEntity.position();
                double distance = playerPos.distanceTo(uniquenessPos);

                if (distance <= PROXIMITY_RANGE) {
                    sendProximityMessage(player, distance);
                }
            }
        }
    }

    private static void sendProximityMessage(ServerPlayer player, double distance) {
        String message;
        int color = 0xFFFFFF; // Default white

        // Get pathway color if available
        String pathway = BeyonderData.getPathway(player);
        if (BeyonderData.pathwayInfos.containsKey(pathway)) {
            color = BeyonderData.pathwayInfos.get(pathway).color();
        }

        // Determine message based on proximity
        if (distance <= 50) {
            message = "You feel the uniqueness VERY strongly nearby... It's almost within reach!";
        } else if (distance <= 100) {
            message = "You feel the uniqueness strongly nearby. It draws closer...";
        } else if (distance <= 200) {
            message = "You sense the uniqueness is nearby...";
        } else if (distance <= 350) {
            message = "You faintly feel the uniqueness in this world...";
        } else {
            message = "You vaguely sense your uniqueness exists somewhere in this realm...";
        }

        player.sendSystemMessage(
                Component.literal(message).withColor(color),
                true
        );
    }
}