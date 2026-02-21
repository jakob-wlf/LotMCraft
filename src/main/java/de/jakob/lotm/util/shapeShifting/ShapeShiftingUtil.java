package de.jakob.lotm.util.shapeShifting;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.network.packets.toClient.ShapeShiftingSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ShapeShiftingUtil {

    public static void shapeShift(ServerPlayer player, Entity entity) {
        String entityType = getEntityTypeString(entity);
        shapeShift(player,entityType);
    }

    public static void shapeShift(ServerPlayer player, String entityType) {
        if (!player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        TransformData data = (TransformData) player;
        data.setCurrentShape(entityType);
        ((DimensionsRefresher) player).shape_refreshDimensions();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new ShapeShiftingSyncPacket(player.getUUID(), entityType));

        if (entityType.startsWith("player:")){
            String playerName = entityType.split(":")[1];
            NameUtils.setPlayerName(player, playerName);
        }else {
            String entityName = entityType;
            entityName = entityName.contains(":") ? entityName.split(":")[1] : entityName;

            if (List.of("bat", "phantom", "blaze", "allay", "bee", "ghast", "parrot", "vex").contains(entityName)) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }
        }
    }
    public static void resetShape(ServerPlayer player){
        TransformData data = (TransformData) player;
        data.setCurrentShape(null);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new ShapeShiftingSyncPacket(player.getUUID(), null));
        NameUtils.resetPlayerName(player);
        if (!player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static String getEntityTypeString(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return String.format("player:%s:%s", player.getGameProfile().getName(), player.getUUID());
        }
        if (entity instanceof BeyonderNPCEntity npc) {
            return "lotmcraft:beyonder_npc:" + npc.getSkinName();
        }
        return EntityType.getKey(entity.getType()).toString();
    }
}
