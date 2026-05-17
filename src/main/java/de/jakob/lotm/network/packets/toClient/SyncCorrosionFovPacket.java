package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCorrosionFovPacket(float fovMultiplier) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncCorrosionFovPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_corrosion_fov"));

    public static final StreamCodec<ByteBuf, SyncCorrosionFovPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncCorrosionFovPacket::fovMultiplier,
            SyncCorrosionFovPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCorrosionFovPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            de.jakob.lotm.util.ClientCorrosionFovCache.setFovMultiplier(packet.fovMultiplier());
        });
    }
}
