package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.VFXRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;


public record UpdateEffectPositionPacket(UUID effectId, double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<UpdateEffectPositionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_effect_position"));

    public static final StreamCodec<ByteBuf, UpdateEffectPositionPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UpdateEffectPositionPacket::effectId,
            ByteBufCodecs.DOUBLE,  UpdateEffectPositionPacket::x,
            ByteBufCodecs.DOUBLE,  UpdateEffectPositionPacket::y,
            ByteBufCodecs.DOUBLE,  UpdateEffectPositionPacket::z,
            UpdateEffectPositionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateEffectPositionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                VFXRenderer.updateEffectPosition(packet.effectId(), packet.x(), packet.y(), packet.z());
            }
        });
    }
}