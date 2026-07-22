package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Server → Client: open the Characteristics Exchange wheel with the predetermined outcome.
 * Server-side effects (item removal, reward giving) happen BEFORE this packet is sent.
 *
 * Outcomes:
 *   0 = Garbage       (85%) — player received the Garbage item
 *   1 = GarbageCollect (10%) — all Garbage removed from inventory
 *   2 = Upgrade         (5%) — player received a characteristic 1 sequence higher
 */
public record OpenCharExchangeWheelPacket(
        List<String> reelNames,
        int landingIndex,
        int outcome,
        String rewardName,
        String title
) implements CustomPacketPayload {

    public static final Type<OpenCharExchangeWheelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_char_exchange_wheel"));

    public static final StreamCodec<ByteBuf, OpenCharExchangeWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenCharExchangeWheelPacket::reelNames,
            ByteBufCodecs.VAR_INT,                                  OpenCharExchangeWheelPacket::landingIndex,
            ByteBufCodecs.VAR_INT,                                  OpenCharExchangeWheelPacket::outcome,
            ByteBufCodecs.STRING_UTF8,                              OpenCharExchangeWheelPacket::rewardName,
            ByteBufCodecs.STRING_UTF8,                              OpenCharExchangeWheelPacket::title,
            OpenCharExchangeWheelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenCharExchangeWheelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openCharExchangeWheelScreen(packet);
        });
    }
}
