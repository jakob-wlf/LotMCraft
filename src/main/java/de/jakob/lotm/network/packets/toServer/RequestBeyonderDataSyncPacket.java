package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerSefirotPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestBeyonderDataSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestBeyonderDataSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_beyonder_data_sync"));

    public static final StreamCodec<ByteBuf, RequestBeyonderDataSyncPacket> STREAM_CODEC = StreamCodec.unit(new RequestBeyonderDataSyncPacket());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestBeyonderDataSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PacketHandler.syncBeyonderDataToPlayer(player);
        });
    }
}
