package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncTransformationS2CPacket(int entityId, boolean isTransformed, int transformationIndex, String additionalData) implements CustomPacketPayload {
    
    public static final Type<SyncTransformationS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_transformation"));
    
    public static final StreamCodec<ByteBuf, SyncTransformationS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncTransformationS2CPacket::entityId,
        ByteBufCodecs.BOOL,
        SyncTransformationS2CPacket::isTransformed,
        ByteBufCodecs.INT,
        SyncTransformationS2CPacket::transformationIndex,
        ByteBufCodecs.STRING_UTF8,
        SyncTransformationS2CPacket::additionalData,
        SyncTransformationS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncTransformationS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleTransformationPacket(packet);
            }
        });
    }
}