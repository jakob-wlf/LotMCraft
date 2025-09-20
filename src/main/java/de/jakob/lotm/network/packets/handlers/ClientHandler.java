package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.abilities.*;
import de.jakob.lotm.gui.custom.AbilitySelectionMenuProvider;
import de.jakob.lotm.gui.custom.CoordinateInputScreen;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.OpenAbilitySelectionPacket;
import de.jakob.lotm.network.packets.RingEffectPacket;
import de.jakob.lotm.network.packets.SyncAbilityMenuPacket;
import de.jakob.lotm.network.packets.SyncLivingEntityBeyonderDataPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

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
            ClientBeyonderCache.updatePlayerData(
                    living.getUUID(),
                    packet.pathway(),
                    packet.sequence(),
                    packet.spirituality(),
                    false
            );
        }
    }

    public static void handleRingPacket(RingEffectPacket packet) {
        RingExpansionRenderer.handleRingEffectPacket(packet);
    }
}