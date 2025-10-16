package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.gui.custom.CoordinateInputScreen;
import de.jakob.lotm.network.packets.*;
import de.jakob.lotm.overlay.MarionetteOverlayRenderer;
import de.jakob.lotm.overlay.SpectatingOverlayRenderer;
import de.jakob.lotm.overlay.SpiritVisionOverlayRenderer;
import de.jakob.lotm.overlay.TelepathyOverlayRenderer;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    public static void openCoordinateScreen(Player player, String use) {
        Minecraft.getInstance().setScreen(new CoordinateInputScreen(player, use));
    }

    public static void syncLivingEntityBeyonderData(SyncLivingEntityBeyonderDataPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            ClientBeyonderCache.updateData(
                    living.getUUID(),
                    packet.pathway(),
                    packet.sequence(),
                    packet.spirituality(),
                    false,
                    false
            );
        }
    }

    public static void syncSpectatingAbility(SyncSpectatingAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            SpectatingOverlayRenderer.entitiesLookedAtByPlayerWithActiveSpectating.put(player.getUUID(), living);
        }
        else {
            SpectatingOverlayRenderer.entitiesLookedAtByPlayerWithActiveSpectating.remove(player.getUUID());
        }
    }

    public static void syncSpiritVisionAbility(SyncSpiritVisionAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            SpiritVisionOverlayRenderer.entitiesLookedAt.put(player.getUUID(), living);
        }
        else {
            SpiritVisionOverlayRenderer.entitiesLookedAt.remove(player.getUUID());
        }
    }

    public static void handleRingPacket(RingEffectPacket packet) {
        RingExpansionRenderer.handleRingEffectPacket(packet);
    }

    public static void removeDreamDivinationUser(Player player) {
        DivinationAbility.dreamDivinationUsers.remove(player.getUUID());
    }

    public static void syncTelepathyAbility(SyncTelepathyAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            TelepathyOverlayRenderer.entitiesLookedAtByPlayerWithActiveTelepathy.put(player.getUUID(), packet.goalNames());
        }
        else {
            TelepathyOverlayRenderer.entitiesLookedAtByPlayerWithActiveTelepathy.remove(player.getUUID());
        }
    }

    public static void syncSelectedMarionette(SyncSelectedMarionettePacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            MarionetteOverlayRenderer.currentMarionette.put(player.getUUID(), living);
        }
        else {
            MarionetteOverlayRenderer.currentMarionette.remove(player.getUUID());
        }
    }
}