package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.VFXRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record CancelEffectPacket(UUID effectId) implements CustomPacketPayload {

    public static final Type<CancelEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cancel_effect"));

    public static final StreamCodec<ByteBuf, CancelEffectPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CancelEffectPacket::effectId,
            CancelEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                VFXRenderer.cancelEffect(packet.effectId());
            }
        });
    }
}