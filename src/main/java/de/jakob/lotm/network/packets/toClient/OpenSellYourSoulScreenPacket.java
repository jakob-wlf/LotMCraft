package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: open the Sell Your Soul wheel screen with the predetermined outcome.
 * Server has already applied all server-side effects before sending this packet.
 *
 * Outcomes:
 *   0 = sanity set to 50%
 *   1 = digestion set to 0
 *   2 = ad overlay (client-side effect only)
 *   3 = received a same-seq characteristic
 *   4 = reverted to Sequence 9 of current pathway
 */
public record OpenSellYourSoulScreenPacket(int outcome, String rewardName)
        implements CustomPacketPayload {

    public static final Type<OpenSellYourSoulScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_sell_your_soul_screen"));

    public static final StreamCodec<ByteBuf, OpenSellYourSoulScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,     OpenSellYourSoulScreenPacket::outcome,
            ByteBufCodecs.STRING_UTF8, OpenSellYourSoulScreenPacket::rewardName,
            OpenSellYourSoulScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenSellYourSoulScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openSellYourSoulScreen(packet);
        });
    }
}
