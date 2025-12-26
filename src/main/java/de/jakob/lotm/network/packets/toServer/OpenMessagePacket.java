package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenMessagePacket(int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMessagePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_message"));

    public static final StreamCodec<FriendlyByteBuf, OpenMessagePacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            FriendlyByteBuf::writeInt,
                            FriendlyByteBuf::readInt
                    ),
                    OpenMessagePacket::index,
                    OpenMessagePacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMessagePacket pkt, IPayloadContext context) {
        if (pkt.index() < 0 ||
                pkt.index() >= BeyonderData.beyonderMap.get(context.player()).get().msgs().size()) {
            return;
        }

        BeyonderData.beyonderMap.markRead(context.player(), pkt.index);
    }

}
