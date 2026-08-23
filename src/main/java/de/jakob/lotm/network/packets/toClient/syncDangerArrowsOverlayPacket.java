package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record syncDangerArrowsOverlayPacket(String direction, int duration) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<syncDangerArrowsOverlayPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_danger_arrows_overlay"));

    public static final StreamCodec<ByteBuf, syncDangerArrowsOverlayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            syncDangerArrowsOverlayPacket::direction,
            ByteBufCodecs.VAR_INT,
            syncDangerArrowsOverlayPacket::duration,
            syncDangerArrowsOverlayPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(syncDangerArrowsOverlayPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.syncDangerArrowsOverlay(packet));
    }
}
