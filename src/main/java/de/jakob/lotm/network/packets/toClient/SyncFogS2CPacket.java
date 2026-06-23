package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFogS2CPacket(int entityId, boolean active, int index, float red, float green, float blue) implements CustomPacketPayload {
    
    public static final Type<SyncFogS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_fog"));
    
    public static final StreamCodec<ByteBuf, SyncFogS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncFogS2CPacket::entityId,
        ByteBufCodecs.BOOL,
        SyncFogS2CPacket::active,
        ByteBufCodecs.INT,
        SyncFogS2CPacket::index,
        ByteBufCodecs.FLOAT,
        SyncFogS2CPacket::red,
        ByteBufCodecs.FLOAT,
        SyncFogS2CPacket::green,
        ByteBufCodecs.FLOAT,
        SyncFogS2CPacket::blue,
        SyncFogS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncFogS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleFogPacket(packet);
            }
        });
    }
}