package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDangerArrowsOverlayPacket(String direction, int duration) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncDangerArrowsOverlayPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_danger_arrows_overlay"));

    public static final StreamCodec<ByteBuf, SyncDangerArrowsOverlayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncDangerArrowsOverlayPacket::direction,
            ByteBufCodecs.VAR_INT,
            SyncDangerArrowsOverlayPacket::duration,
            SyncDangerArrowsOverlayPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDangerArrowsOverlayPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.syncDangerArrowsOverlay(packet));
    }
}
