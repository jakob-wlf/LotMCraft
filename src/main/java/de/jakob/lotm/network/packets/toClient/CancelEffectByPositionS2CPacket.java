package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CancelEffectByPositionS2CPacket(double x, double y, double z, double radius)
        implements CustomPacketPayload {

    public static final Type<CancelEffectByPositionS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "cancel_effect_by_position"));

    public static final StreamCodec<ByteBuf, CancelEffectByPositionS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, CancelEffectByPositionS2CPacket::x,
            ByteBufCodecs.DOUBLE, CancelEffectByPositionS2CPacket::y,
            ByteBufCodecs.DOUBLE, CancelEffectByPositionS2CPacket::z,
            ByteBufCodecs.DOUBLE, CancelEffectByPositionS2CPacket::radius,
            CancelEffectByPositionS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelEffectByPositionS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.cancelEffectsNear(packet.x(), packet.y(), packet.z(), packet.radius());
            }
        });
    }
}
