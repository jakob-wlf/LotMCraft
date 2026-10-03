package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncApotheosisPacket(int entityId, int ticks, String pathway) implements CustomPacketPayload {
    
    public static final Type<SyncApotheosisPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_apotheosis"));
    
    public static final StreamCodec<ByteBuf, SyncApotheosisPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SyncApotheosisPacket::entityId,
        ByteBufCodecs.INT,
        SyncApotheosisPacket::ticks,
        ByteBufCodecs.STRING_UTF8,
        SyncApotheosisPacket::pathway,
        SyncApotheosisPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncApotheosisPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.handleApotheosisPacket(packet);
            }
        });
    }
}