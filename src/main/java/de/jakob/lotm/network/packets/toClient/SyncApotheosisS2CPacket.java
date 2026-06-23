package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncApotheosisS2CPacket(int entityId, int ticks, String pathway) implements CustomPacketPayload {
    
    public static final Type<SyncApotheosisS2CPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_apotheosis"));
    
    public static final StreamCodec<ByteBuf, SyncApotheosisS2CPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncApotheosisS2CPacket::entityId,
        ByteBufCodecs.INT,
        SyncApotheosisS2CPacket::ticks,
        ByteBufCodecs.STRING_UTF8,
        SyncApotheosisS2CPacket::pathway,
        SyncApotheosisS2CPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncApotheosisS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleApotheosisPacket(packet);
            }
        });
    }
}