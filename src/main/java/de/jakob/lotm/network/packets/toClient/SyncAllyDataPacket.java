package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Packet to sync ally data from server to client
 */

public record SyncAllyDataPacket(Set<String> allies) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SyncAllyDataPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ally_data"));

    public static final StreamCodec<ByteBuf, SyncAllyDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
            SyncAllyDataPacket::allies,
            SyncAllyDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAllyDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleAllyPacket(packet);
        });
    }
}