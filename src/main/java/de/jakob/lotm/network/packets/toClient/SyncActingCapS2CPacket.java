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

public record SyncActingCapS2CPacket(float capReduction, CompoundTag missedActing) implements CustomPacketPayload {

    public static final Type<SyncActingCapS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_acting_cap"));

    public static final StreamCodec<FriendlyByteBuf, SyncActingCapS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, SyncActingCapS2CPacket::capReduction,
                    ByteBufCodecs.COMPOUND_TAG, SyncActingCapS2CPacket::missedActing,
                    SyncActingCapS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncActingCapS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleActingCapPacket(packet));
    }
}
