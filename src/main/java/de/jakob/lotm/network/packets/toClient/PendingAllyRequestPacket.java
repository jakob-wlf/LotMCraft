package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AllyRequestManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet sent from server to client to notify of a pending ally request
 */
public record PendingAllyRequestPacket(UUID requesterUUID, String requesterName) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<PendingAllyRequestPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "pending_ally_request"));

    public static final StreamCodec<ByteBuf, PendingAllyRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            PendingAllyRequestPacket::requesterUUID,
            ByteBufCodecs.STRING_UTF8,
            PendingAllyRequestPacket::requesterName,
            PendingAllyRequestPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PendingAllyRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AllyRequestManager.addPendingRequest(packet.requesterUUID(), packet.requesterName());
        });
    }
}