package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientCorrosionFovCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCorrosionFovS2CPacket(float fovMultiplier) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncCorrosionFovS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_corrosion_fov"));

    public static final StreamCodec<ByteBuf, SyncCorrosionFovS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SyncCorrosionFovS2CPacket::fovMultiplier,
            SyncCorrosionFovS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCorrosionFovS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientCorrosionFovCache.setFovMultiplier(packet.fovMultiplier());
        });
    }
}
