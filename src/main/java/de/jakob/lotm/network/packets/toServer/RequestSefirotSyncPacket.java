package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerSefirotPacket;
import de.jakob.lotm.util.helper.AllyUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RequestSefirotSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestSefirotSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_sefirot_sync"));

    public static final StreamCodec<ByteBuf, RequestSefirotSyncPacket> STREAM_CODEC = StreamCodec.unit(new RequestSefirotSyncPacket());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestSefirotSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PacketHandler.sendToPlayer(player, new SyncPlayerSefirotPacket(SefirahHandler.getSefirot(player)));
        });
    }
}
