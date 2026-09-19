package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.ConnectionAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClearConnectionPacket(String connectionId) implements CustomPacketPayload {
    public static final Type<ClearConnectionPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "clear_connection"));

    public static final StreamCodec<ByteBuf, ClearConnectionPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClearConnectionPacket::connectionId,
        ClearConnectionPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearConnectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()
                    || !(context.player() instanceof ServerPlayer player)) return;
            UUID connectionId;
            try {
                connectionId = UUID.fromString(packet.connectionId);
            } catch (IllegalArgumentException ignored) {
                return;
            }
            if (ConnectionAbility.clearConnection(player, connectionId)) {
                ConnectionAbility.openConnectionManager(player);
            }
        });
    }
}
