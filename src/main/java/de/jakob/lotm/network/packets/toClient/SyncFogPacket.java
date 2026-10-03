package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncFogPacket(int entityId, boolean active, int index, float red, float green, float blue) implements CustomPacketPayload {
    
    public static final Type<SyncFogPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_fog"));
    
    public static final StreamCodec<ByteBuf, SyncFogPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncFogPacket::entityId,
        ByteBufCodecs.BOOL,
        SyncFogPacket::active,
        ByteBufCodecs.INT,
        SyncFogPacket::index,
        ByteBufCodecs.FLOAT,
        SyncFogPacket::red,
        ByteBufCodecs.FLOAT,
        SyncFogPacket::green,
        ByteBufCodecs.FLOAT,
        SyncFogPacket::blue,
        SyncFogPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncFogPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleFogPacket(packet);
            }
        });
    }
}