package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.LuckPerceptionOverlayRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncLuckPerceptionPacket(boolean active, int entityId, int luck) implements CustomPacketPayload {
    public static final Type<SyncLuckPerceptionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_luck_perception"));

    public static final StreamCodec<FriendlyByteBuf, SyncLuckPerceptionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncLuckPerceptionPacket::active,
            ByteBufCodecs.INT, SyncLuckPerceptionPacket::entityId,
            ByteBufCodecs.INT, SyncLuckPerceptionPacket::luck,
            SyncLuckPerceptionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncLuckPerceptionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                LuckPerceptionOverlayRenderer.update(packet.active(), packet.entityId(), packet.luck());
            }
        });
    }
}