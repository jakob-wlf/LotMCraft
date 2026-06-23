package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TeleportPlayerToLocationC2SPacket(double x, double y, double z, int id) implements CustomPacketPayload {
    public static final Type<TeleportPlayerToLocationC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "teleport_to_location_packet"));

    public static final StreamCodec<FriendlyByteBuf, TeleportPlayerToLocationC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, TeleportPlayerToLocationC2SPacket::x,
                    ByteBufCodecs.DOUBLE, TeleportPlayerToLocationC2SPacket::y,
                    ByteBufCodecs.DOUBLE, TeleportPlayerToLocationC2SPacket::z,
                    ByteBufCodecs.INT, TeleportPlayerToLocationC2SPacket::id,
                    TeleportPlayerToLocationC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TeleportPlayerToLocationC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                if(context.player().getId() != packet.id) {
                    return;
                }
                context.player().teleportTo(packet.x, packet.y, packet.z);
            }
        });
    }
}