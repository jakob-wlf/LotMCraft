package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMirrorWorldS2CPacket(boolean inMirrorWorld) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SyncMirrorWorldS2CPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_mirror_world"));
    
    public static final StreamCodec<ByteBuf, SyncMirrorWorldS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        SyncMirrorWorldS2CPacket::inMirrorWorld,
        SyncMirrorWorldS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncMirrorWorldS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleMirrorWorldPacket(packet);
            }
        });
    }
}