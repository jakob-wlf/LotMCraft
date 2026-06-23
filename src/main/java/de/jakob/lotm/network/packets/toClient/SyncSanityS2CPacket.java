package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSanityS2CPacket(float sanity, int entityId) implements CustomPacketPayload {
    
    public static final Type<SyncSanityS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_sanity"));
    
    public static final StreamCodec<ByteBuf, SyncSanityS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncSanityS2CPacket::sanity,
            ByteBufCodecs.INT,
            SyncSanityS2CPacket::entityId,
        SyncSanityS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncSanityS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleSanityPacket(packet);
            }
        });
    }
}