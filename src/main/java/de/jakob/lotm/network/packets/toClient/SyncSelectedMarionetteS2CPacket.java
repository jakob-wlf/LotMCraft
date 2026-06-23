package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSelectedMarionetteS2CPacket(boolean active, String name, double health, double maxHealth) implements CustomPacketPayload {


    public static final Type<SyncSelectedMarionetteS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_selected_marionette"));

    public static final StreamCodec<FriendlyByteBuf, SyncSelectedMarionetteS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncSelectedMarionetteS2CPacket::active,
                    ByteBufCodecs.STRING_UTF8, SyncSelectedMarionetteS2CPacket::name,
                    ByteBufCodecs.DOUBLE, SyncSelectedMarionetteS2CPacket::health,
                    ByteBufCodecs.DOUBLE, SyncSelectedMarionetteS2CPacket::maxHealth,
                    SyncSelectedMarionetteS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSelectedMarionetteS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSelectedMarionette(packet, context.player());
            }
        });
    }
}
