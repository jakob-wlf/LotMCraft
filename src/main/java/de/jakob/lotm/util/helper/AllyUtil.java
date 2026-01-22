package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAllyDataPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

/**
 * Utility class for managing ally relationships between entities.
 * Allies cannot target or damage each other.
 */
public class AllyUtil {

    /**
     * Make two entities allies (bidirectional)
     */
    public static void makeAllies(LivingEntity entity1, LivingEntity entity2) {
        makeAllies(entity1, entity2, true);
    }

    public static void makeAllies(LivingEntity entity1, LivingEntity entity2, boolean sendMessage) {
        if (entity1 == null || entity2 == null) return;
        if (entity1.getUUID().equals(entity2.getUUID())) return;

        // Add each other as allies
        AllyComponent comp1 = entity1.getData(ModAttachments.ALLY_COMPONENT.get());
        entity1.setData(ModAttachments.ALLY_COMPONENT.get(), comp1.addAlly(entity2.getUUID()));

        AllyComponent comp2 = entity2.getData(ModAttachments.ALLY_COMPONENT.get());
        entity2.setData(ModAttachments.ALLY_COMPONENT.get(), comp2.addAlly(entity1.getUUID()));

        // Sync to clients if they're players
        if (entity1 instanceof ServerPlayer player1) {
            syncAllyData(player1);
            if (sendMessage)
                player1.sendSystemMessage(Component.translatable("lotm.ally.added", entity2.getName()).withColor(0x4CAF50));
        }
        if (entity2 instanceof ServerPlayer player2) {
            syncAllyData(player2);
            if (sendMessage)
                player2.sendSystemMessage(Component.translatable("lotm.ally.added", entity1.getName()).withColor(0x4CAF50));
        }
    }

    /**
     * Remove ally relationship between two entities (bidirectional)
     */
    public static void removeAllies(LivingEntity entity1, LivingEntity entity2) {
        if (entity1 == null || entity2 == null) return;

        AllyComponent comp1 = entity1.getData(ModAttachments.ALLY_COMPONENT.get());
        entity1.setData(ModAttachments.ALLY_COMPONENT.get(), comp1.removeAlly(entity2.getUUID()));

        AllyComponent comp2 = entity2.getData(ModAttachments.ALLY_COMPONENT.get());
        entity2.setData(ModAttachments.ALLY_COMPONENT.get(), comp2.removeAlly(entity1.getUUID()));

        // Sync to clients if they're players
        if (entity1 instanceof ServerPlayer player1) {
            syncAllyData(player1);
            player1.sendSystemMessage(Component.translatable("lotm.ally.removed", entity2.getName()).withColor(0xFF9800));
        }
        if (entity2 instanceof ServerPlayer player2) {
            syncAllyData(player2);
            player2.sendSystemMessage(Component.translatable("lotm.ally.removed", entity1.getName()).withColor(0xFF9800));
        }
    }

    /**
     * Check if two entities are allies
     */
    public static boolean areAllies(LivingEntity entity1, LivingEntity entity2) {
        if (entity1 == null || entity2 == null) return false;
        if (entity1.getUUID().equals(entity2.getUUID())) return true;

        AllyComponent comp1 = entity1.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp1.isAlly(entity2.getUUID());
    }

    /**
     * Check if an entity is an ally by UUID
     */
    public static boolean isAlly(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return false;
        if (entity.getUUID().equals(allyUUID)) return true;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp.isAlly(allyUUID);
    }

    /**
     * Clear all allies for an entity
     */
    public static void clearAllAllies(LivingEntity entity) {
        if (entity == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        
        // Remove bidirectional relationships
        for (String allyUUIDStr : comp.allies()) {
            try {
                UUID allyUUID = UUID.fromString(allyUUIDStr);
                LivingEntity ally = (LivingEntity) entity.level().getPlayerByUUID(allyUUID);
                if (ally != null) {
                    AllyComponent allyComp = ally.getData(ModAttachments.ALLY_COMPONENT.get());
                    ally.setData(ModAttachments.ALLY_COMPONENT.get(), allyComp.removeAlly(entity.getUUID()));
                    
                    if (ally instanceof ServerPlayer serverPlayer) {
                        syncAllyData(serverPlayer);
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }

        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.clearAllies());

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }

    /**
     * Get ally count for an entity
     */
    public static int getAllyCount(LivingEntity entity) {
        if (entity == null) return 0;
        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp.allyCount();
    }

    /**
     * Check if entity has any allies
     */
    public static boolean hasAllies(LivingEntity entity) {
        if (entity == null) return false;
        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp.hasAllies();
    }

    /**
     * Check if entity can be allied (non-beyonder entities can always be allied,
     * beyonder entities/players need to accept)
     */
    public static boolean canBeAllied(LivingEntity entity) {
        if (entity == null) return false;
        return !BeyonderData.isBeyonder(entity);
    }

    /**
     * Sync ally data to client
     */
    private static void syncAllyData(ServerPlayer player) {
        AllyComponent comp = player.getData(ModAttachments.ALLY_COMPONENT.get());
        SyncAllyDataPacket packet = new SyncAllyDataPacket(comp.allies());
        PacketHandler.sendToPlayer(player, packet);
    }

    /**
     * Add an ally to an entity without making it bidirectional
     * (Use with caution - prefer makeAllies for standard use)
     */
    public static void addAllyOneWay(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.addAlly(allyUUID));

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }

    /**
     * Remove an ally from an entity without removing the reverse relationship
     * (Use with caution - prefer removeAllies for standard use)
     */
    public static void removeAllyOneWay(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.removeAlly(allyUUID));

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }
}