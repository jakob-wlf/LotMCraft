package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSelectedMarionettePacket(boolean active, String name, double health, double maxHealth) implements CustomPacketPayload {


    public static final Type<SyncSelectedMarionettePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_selected_marionette"));

    public static final StreamCodec<FriendlyByteBuf, SyncSelectedMarionettePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncSelectedMarionettePacket::active,
                    ByteBufCodecs.STRING_UTF8, SyncSelectedMarionettePacket::name,
                    ByteBufCodecs.DOUBLE, SyncSelectedMarionettePacket::health,
                    ByteBufCodecs.DOUBLE, SyncSelectedMarionettePacket::maxHealth,
                    SyncSelectedMarionettePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSelectedMarionettePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSelectedMarionette(packet, context.player());
            }
        });
    }
}
