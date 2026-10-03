package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncActingCapPacket(float capReduction, CompoundTag missedActing) implements CustomPacketPayload {

    public static final Type<SyncActingCapPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_acting_cap"));

    public static final StreamCodec<FriendlyByteBuf, SyncActingCapPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, SyncActingCapPacket::capReduction,
                    ByteBufCodecs.COMPOUND_TAG, SyncActingCapPacket::missedActing,
                    SyncActingCapPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncActingCapPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleActingCapPacket(packet));
    }
}
