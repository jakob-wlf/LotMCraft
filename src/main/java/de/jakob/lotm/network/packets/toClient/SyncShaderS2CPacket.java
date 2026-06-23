package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncShaderS2CPacket(int entityId, boolean shaderActive, int shaderIndex) implements CustomPacketPayload {
    
    public static final Type<SyncShaderS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_shader"));
    
    public static final StreamCodec<ByteBuf, SyncShaderS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncShaderS2CPacket::entityId,
        ByteBufCodecs.BOOL,
        SyncShaderS2CPacket::shaderActive,
        ByteBufCodecs.INT,
        SyncShaderS2CPacket::shaderIndex,
        SyncShaderS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncShaderS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleShaderPacket(packet);
            }
        });
    }
}