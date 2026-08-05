package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: open the "Above the Sequence Authority" (Coming Soon) screen.
 */
public record OpenAboveSeqAuthorityScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenAboveSeqAuthorityScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_above_seq_authority_screen"));

    public static final StreamCodec<ByteBuf, OpenAboveSeqAuthorityScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenAboveSeqAuthorityScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenAboveSeqAuthorityScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openAboveSeqAuthorityScreen();
        });
    }
}
