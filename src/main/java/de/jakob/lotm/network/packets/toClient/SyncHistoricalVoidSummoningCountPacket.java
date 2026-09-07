package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncHistoricalVoidSummoningCountPacket(int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHistoricalVoidSummoningCountPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_historical_void_summoning_count"));

    public static final StreamCodec<FriendlyByteBuf, SyncHistoricalVoidSummoningCountPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncHistoricalVoidSummoningCountPacket::amount,
                    SyncHistoricalVoidSummoningCountPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncHistoricalVoidSummoningCountPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleSyncHistoricalVoidSummoningCountPacket(packet);
        });
    }
}
