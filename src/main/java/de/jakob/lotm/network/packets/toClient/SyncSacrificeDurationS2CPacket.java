package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSacrificeDurationS2CPacket(int totalTicks) implements CustomPacketPayload {
    public static final Type<SyncSacrificeDurationS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_sacrifice_duration"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSacrificeDurationS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncSacrificeDurationS2CPacket::totalTicks,
                    SyncSacrificeDurationS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSacrificeDurationS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncSacrificeDuration(packet.totalTicks());
            }
        });
    }
}
