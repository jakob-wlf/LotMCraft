package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.DemonHuntingGazeOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDemonHuntingGazePacket(int entityId, int pulses) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncDemonHuntingGazePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_demon_hunting_gaze"));

    public static final StreamCodec<ByteBuf, SyncDemonHuntingGazePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncDemonHuntingGazePacket::entityId,
            ByteBufCodecs.VAR_INT,
            SyncDemonHuntingGazePacket::pulses,
            SyncDemonHuntingGazePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDemonHuntingGazePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> DemonHuntingGazeOverlay.show(packet.entityId(), packet.pulses()));
    }
}
