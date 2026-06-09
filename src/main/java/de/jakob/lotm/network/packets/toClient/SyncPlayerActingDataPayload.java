package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerActingDataPayload(CompoundTag data) implements CustomPacketPayload {
    
    public static final Type<SyncPlayerActingDataPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_player_acting_data"));

    public static final StreamCodec<ByteBuf, SyncPlayerActingDataPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncPlayerActingDataPayload::data,
            SyncPlayerActingDataPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPlayerActingDataPayload packet, IPayloadContext context) {
        ClientHandler.handleSyncPlayerData(packet, context);
    }
}