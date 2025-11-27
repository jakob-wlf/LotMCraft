package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncTransformationPacket(int entityId, boolean isTransformed, int transformationIndex) implements CustomPacketPayload {
    
    public static final Type<SyncTransformationPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_transformation"));
    
    public static final StreamCodec<ByteBuf, SyncTransformationPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncTransformationPacket::entityId,
        ByteBufCodecs.BOOL,
        SyncTransformationPacket::isTransformed,
        ByteBufCodecs.INT,
        SyncTransformationPacket::transformationIndex,
        SyncTransformationPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncTransformationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleTransformationPacket(packet);
            }
        });
    }
}