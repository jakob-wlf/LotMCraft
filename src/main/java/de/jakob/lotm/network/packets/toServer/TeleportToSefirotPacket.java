package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.*;
import de.jakob.lotm.gui.custom.AbilitySelectionMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityMenuPacket;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record TeleportToSefirotPacket() implements CustomPacketPayload {
    public static final Type<TeleportToSefirotPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "teleport_to_sefirot"));

    public static final StreamCodec<FriendlyByteBuf, TeleportToSefirotPacket> STREAM_CODEC =
            StreamCodec.unit(new TeleportToSefirotPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    // TODO: Claiming here is temporary, add proper ritual for claiming
    public static void handle(TeleportToSefirotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer() && context.player() instanceof ServerPlayer serverPlayer) {

                if(!SefirahHandler.hasSefirot(serverPlayer)) {
                    SefirahHandler.claimSefirot(serverPlayer, "sefirah_castle");
                }
                SefirahHandler.teleportToSefirot(serverPlayer);
            }
        });
    }
}