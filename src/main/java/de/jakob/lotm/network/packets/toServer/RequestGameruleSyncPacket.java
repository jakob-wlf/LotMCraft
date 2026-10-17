package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncGriefingGamerulePacket;
import de.jakob.lotm.network.packets.toClient.SyncPreLoadGamerulePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestGameruleSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestGameruleSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_gamerule_sync"));

    public static final StreamCodec<ByteBuf, RequestGameruleSyncPacket> STREAM_CODEC = StreamCodec.unit(new RequestGameruleSyncPacket());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestGameruleSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PacketHandler.sendToPlayer(player, new SyncGriefingGamerulePacket(player.level().getGameRules().getBoolean(ModGameRules.ALLOW_GRIEFING)));
            PacketHandler.sendToPlayer(player, new SyncPreLoadGamerulePacket(player.level().getGameRules().getBoolean(ModGameRules.PRE_LOAD_EFFECTS)));
        });
    }
}
