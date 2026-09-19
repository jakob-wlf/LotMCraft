package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PerformPreyPacket(UUID targetUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PerformPreyPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "perform_prey"));

    public static final StreamCodec<FriendlyByteBuf, PerformPreyPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.targetUUID()),
                    buf -> new PerformPreyPacket(buf.readUUID())
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PerformPreyPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                HonorificNamesEventHandler.performPrayer(player, pkt.targetUUID());
            }
        });
    }
}
