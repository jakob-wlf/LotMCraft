package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.AllyAbility;
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
public record AllyRequestResponseC2SPacket(UUID requesterUUID, boolean accept) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<AllyRequestResponseC2SPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ally_request_response"));

    public static final StreamCodec<ByteBuf, AllyRequestResponseC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            AllyRequestResponseC2SPacket::requesterUUID,
            ByteBufCodecs.BOOL,
            AllyRequestResponseC2SPacket::accept,
            AllyRequestResponseC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AllyRequestResponseC2SPacket packet, IPayloadContext context) {
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