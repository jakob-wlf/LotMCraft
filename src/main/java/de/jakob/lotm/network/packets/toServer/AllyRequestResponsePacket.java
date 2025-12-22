package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.AllyAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Packet sent from client to server to respond to an ally request
 */
public record AllyRequestResponsePacket(UUID requesterUUID, boolean accept) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<AllyRequestResponsePacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ally_request_response"));

    public static final StreamCodec<ByteBuf, AllyRequestResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            AllyRequestResponsePacket::requesterUUID,
            ByteBufCodecs.BOOL,
            AllyRequestResponsePacket::accept,
            AllyRequestResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllyRequestResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (packet.accept()) {
                    AllyAbility.acceptAllyRequest(serverPlayer, packet.requesterUUID());
                } else {
                    AllyAbility.denyAllyRequest(serverPlayer, packet.requesterUUID());
                }
            }
        });
    }
}