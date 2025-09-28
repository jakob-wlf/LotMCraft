package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.gui.custom.CoordinateInputScreen;
import de.jakob.lotm.network.packets.RingEffectPacket;
import de.jakob.lotm.network.packets.SyncLivingEntityBeyonderDataPacket;
import de.jakob.lotm.network.packets.SyncSpectatingAbilityPacket;
import de.jakob.lotm.network.packets.SyncSpiritVisionAbilityPacket;
import de.jakob.lotm.overlay.SpectatingOverlayRenderer;
import de.jakob.lotm.overlay.SpiritVisionOverlayRenderer;
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
    public static void openCoordinateScreen(Player player) {
        Minecraft.getInstance().setScreen(new CoordinateInputScreen(player));
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

    public static void syncSpectatingAbility(SyncSpiritVisionAbilityPacket packet, Player player) {
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
}