package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.AbsolutePerceptionOutlineColors;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncAbsolutePerceptionColorsPacket(Map<Integer, Integer> colors)
        implements CustomPacketPayload {
    public static final Type<SyncAbsolutePerceptionColorsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_absolute_perception_colors"));

    public static final StreamCodec<ByteBuf, SyncAbsolutePerceptionColorsPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, ByteBufCodecs.INT, 256),
                    SyncAbsolutePerceptionColorsPacket::colors,
                    SyncAbsolutePerceptionColorsPacket::new);

    public static void handle(SyncAbsolutePerceptionColorsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> AbsolutePerceptionOutlineColors.replace(packet.colors));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}