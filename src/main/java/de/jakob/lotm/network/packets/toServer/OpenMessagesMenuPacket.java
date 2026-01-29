package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.Messages.MessageMenuProvider;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenMessagesMenuPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMessagesMenuPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_messages_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenMessagesMenuPacket> STREAM_CODEC
            = StreamCodec.of(
            (buf, pkt) -> {
            },
            buf -> new OpenMessagesMenuPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMessagesMenuPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();

                player.openMenu(new MessageMenuProvider(BeyonderData.beyonderMap.get(player).get()),
                        buf -> {
                            var data = BeyonderData.beyonderMap.get(player).get();

                            buf.writeCollection(
                                    data.knownNames(),
                                    (b, name) -> name.toNetwork(b)
                            );

                            data.honorificName().toNetwork(buf);

                            buf.writeCollection(
                                    data.msgs(),
                                    (b, msg) -> msg.toNetwork(b)
                            );
                        });
            }
        });
    }
}
