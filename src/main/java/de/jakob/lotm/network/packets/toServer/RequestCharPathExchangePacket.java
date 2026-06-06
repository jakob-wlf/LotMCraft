package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.CharExchange.CharPathExchangeHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: player has chosen which characteristic to sacrifice in the
 * Char Path Exchange (jackpot = random char at any sequence from any pathway).
 *
 * @param slotIndex  the inventory slot (0-35) containing the chosen characteristic.
 */
public record RequestCharPathExchangePacket(int slotIndex) implements CustomPacketPayload {

    public static final Type<RequestCharPathExchangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_char_path_exchange"));

    public static final StreamCodec<ByteBuf, RequestCharPathExchangePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestCharPathExchangePacket::slotIndex,
            RequestCharPathExchangePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestCharPathExchangePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;
            CharPathExchangeHandler.processExchange(player, packet.slotIndex());
        });
    }
}
