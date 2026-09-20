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
 * Server → Client: tells the client to open the characteristic slot-roll screen.
 *
 * Carries all seq-9 characteristic entries (pathway, display name) that can appear
 * in the roll, plus how many rerolls the player has remaining.
 */
public record OpenCharSlotRollPacket(List<String> pathways, List<String> charNames, int rerollsLeft) implements CustomPacketPayload {

    public static final Type<OpenCharSlotRollPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_char_slot_roll"));

    public static final StreamCodec<ByteBuf, OpenCharSlotRollPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenCharSlotRollPacket::pathways,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenCharSlotRollPacket::charNames,
            ByteBufCodecs.VAR_INT,                                 OpenCharSlotRollPacket::rerollsLeft,
            OpenCharSlotRollPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenCharSlotRollPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isClient()) return;
            ClientHandler.openCharSlotRollScreen(packet);
        });
    }
}
