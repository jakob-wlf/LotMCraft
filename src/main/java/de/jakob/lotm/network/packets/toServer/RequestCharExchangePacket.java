package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.CharExchange.CharExchangeHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: player has chosen which characteristic to sacrifice.
 *
 * @param slotIndex  the inventory slot (0-35) containing the chosen characteristic.
 */
public record RequestCharExchangePacket(int slotIndex) implements CustomPacketPayload {

    public static final Type<RequestCharExchangePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_char_exchange"));

    public static final StreamCodec<ByteBuf, RequestCharExchangePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestCharExchangePacket::slotIndex,
            RequestCharExchangePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestCharExchangePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;
            CharExchangeHandler.processExchange(player, packet.slotIndex());
        });
    }
}
