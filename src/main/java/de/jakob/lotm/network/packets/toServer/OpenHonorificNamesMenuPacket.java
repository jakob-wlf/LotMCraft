package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.gui.custom.HonorificNames.HonorificNamesMenuProvider;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.beyonderMap.HonorificName;
import de.jakob.lotm.util.beyonderMap.PendingPrayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedList;

public record OpenHonorificNamesMenuPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenHonorificNamesMenuPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_honorific_names_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenHonorificNamesMenuPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> { /* no data */ },
                    buf -> new OpenHonorificNamesMenuPacket()
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenHonorificNamesMenuPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();

                var dataOpt = BeyonderData.beyonderMap.get(player);
                var honorificName = dataOpt.isPresent() ? dataOpt.get().honorificName()
                        : HonorificName.EMPTY;
                String pathway = BeyonderData.getPathway(player);
                int sequence = BeyonderData.getSequence(player);

                LinkedList<PendingPrayer> pendingPrayers =
                        HonorificNamesEventHandler.getPendingPrayers(player.getUUID());

                player.openMenu(
                        new HonorificNamesMenuProvider(honorificName, pathway, sequence, pendingPrayers),
                        buf -> {
                            honorificName.toNetwork(buf);
                            buf.writeUtf(pathway, 64);
                            buf.writeInt(sequence);
                            buf.writeCollection(pendingPrayers, (b, p) -> p.toNetwork(b));
                        }
                );
            }
        });
    }
}
