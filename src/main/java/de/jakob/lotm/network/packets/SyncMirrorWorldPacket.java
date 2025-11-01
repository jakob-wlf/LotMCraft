package de.jakob.lotm.network.packets;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMirrorWorldPacket(boolean inMirrorWorld) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SyncMirrorWorldPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("yourmodid", "sync_mirror_world"));
    
    public static final StreamCodec<ByteBuf, SyncMirrorWorldPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        SyncMirrorWorldPacket::inMirrorWorld,
        SyncMirrorWorldPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncMirrorWorldPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleMirrorWorldPacket(packet);
            }
        });
    }
}