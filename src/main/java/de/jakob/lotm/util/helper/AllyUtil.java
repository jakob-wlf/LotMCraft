package de.jakob.lotm.util.helper;

import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAllyDataPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

import java.util.Set;
import java.util.UUID;

public class AllyUtil {

    public static void makeAllies(LivingEntity entity1, LivingEntity entity2) {
        makeAllies(entity1, entity2, true);
    }

    public static void makeAllies(LivingEntity entity1, LivingEntity entity2, boolean sendMessage) {
        if (entity1 == null || entity2 == null) return;
        if (entity1.getUUID().equals(entity2.getUUID())) return;

        // Add each other as allies
        AllyComponent comp1 = entity1.getData(ModAttachments.ALLY_COMPONENT.get());
        entity1.setData(ModAttachments.ALLY_COMPONENT.get(), comp1.addAlly(entity2.getUUID(), entity2.getDisplayName().getString(), entity2 instanceof Player));

        AllyComponent comp2 = entity2.getData(ModAttachments.ALLY_COMPONENT.get());
        entity2.setData(ModAttachments.ALLY_COMPONENT.get(), comp2.addAlly(entity1.getUUID(), entity1.getDisplayName().getString(), entity1 instanceof Player));

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

    public static void declineAllyRequest(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.removeRequest(allyUUID));

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }

    public static void acceptAllyRequest(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.removeRequest(allyUUID));

        LivingEntity allyEntity = null;
        if (entity instanceof ServerPlayer player) {
            allyEntity = player.serverLevel().getPlayerByUUID(allyUUID);
        }

        if (allyEntity != null) {
            makeAllies(entity, allyEntity);
        }

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }

    public static void sendAllyRequest(LivingEntity entity, LivingEntity target) {
        if (entity == null || target == null) return;

        AllyComponent comp = target.getData(ModAttachments.ALLY_COMPONENT.get());
        target.setData(ModAttachments.ALLY_COMPONENT.get(), comp.addRequest(entity.getUUID(), entity.getDisplayName().getString(), entity instanceof Player));

        if (target instanceof ServerPlayer player) {
            syncAllyData(player);
            player.sendSystemMessage(Component.translatable("lotm.ally.request.received", entity.getName()).withColor(0x2196F3));
        }
    }

    public static boolean areAllies(LivingEntity entity1, LivingEntity entity2) {
        if (entity1 == null || entity2 == null) return false;
        if (entity1.getUUID().equals(entity2.getUUID())) return true;

        if(AbilityUtil.ignoreAllies.containsKey(entity1.getUUID())) return false;

        AllyComponent comp1 = entity1.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp1.isAlly(entity2.getUUID());
    }

    public static boolean isAlly(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return false;
        if (entity.getUUID().equals(allyUUID)) return true;

        if(AbilityUtil.ignoreAllies.containsKey(entity.getUUID())) return false;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        return comp.isAlly(allyUUID);
    }


    private static void syncAllyData(ServerPlayer player) {
        AllyComponent comp = player.getData(ModAttachments.ALLY_COMPONENT.get());
        SyncAllyDataPacket packet = new SyncAllyDataPacket(comp.allies(), comp.requests());
        PacketHandler.sendToPlayer(player, packet);
    }

    public static void addAllyOneWay(LivingEntity entity, LivingEntity ally) {
        if (entity == null || ally == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.addAlly(ally.getUUID(), ally.getDisplayName().getString(), ally instanceof Player));

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }

    public static void removeAllyOneWay(LivingEntity entity, UUID allyUUID) {
        if (entity == null || allyUUID == null) return;

        AllyComponent comp = entity.getData(ModAttachments.ALLY_COMPONENT.get());
        entity.setData(ModAttachments.ALLY_COMPONENT.get(), comp.removeAlly(allyUUID));

        if (entity instanceof ServerPlayer player) {
            syncAllyData(player);
        }
    }
}