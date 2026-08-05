package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCorruptionPacket(float corruption, int entityId) implements CustomPacketPayload {
    
    public static final Type<SyncCorruptionPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_corruption"));
    
    public static final StreamCodec<ByteBuf, SyncCorruptionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncCorruptionPacket::corruption,
            ByteBufCodecs.INT,
            SyncCorruptionPacket::entityId,
        SyncCorruptionPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncCorruptionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleCorruptionPacket(packet);
            }
        });
    }
}
