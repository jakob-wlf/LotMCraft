package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncKillCountS2CPacket(int killCount) implements CustomPacketPayload {
    public static final Type<SyncKillCountS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_kill_count"));

    public static final StreamCodec<FriendlyByteBuf, SyncKillCountS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncKillCountS2CPacket::killCount,
            SyncKillCountS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncKillCountS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncKillCount(packet.killCount());
            }
        });
    }
}
