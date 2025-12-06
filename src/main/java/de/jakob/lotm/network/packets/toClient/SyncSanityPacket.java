package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSanityPacket(float sanity, int entityId) implements CustomPacketPayload {
    
    public static final Type<SyncSanityPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_sanity"));
    
    public static final StreamCodec<ByteBuf, SyncSanityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncSanityPacket::sanity,
            ByteBufCodecs.INT,
            SyncSanityPacket::entityId,
        SyncSanityPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncSanityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleSanityPacket(packet);
            }
        });
    }
}