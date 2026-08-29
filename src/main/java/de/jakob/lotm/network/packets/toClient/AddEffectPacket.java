package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record AddEffectPacket(UUID effectId, int index, double x, double y, double z,
                              int entityId, boolean followEntity, int duration, boolean infinite, boolean infiniteOverridden,
                              float[] params) implements CustomPacketPayload {

    public static final int NO_ENTITY = -1;
    public static final int NO_DURATION_OVERRIDE = -1;

    public static final Type<AddEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "add_effect"));

    private static final StreamCodec<ByteBuf, float[]> PARAMS_CODEC = StreamCodec.of(
            (buf, arr) -> { for (float f : arr) buf.writeFloat(f); },
            buf -> {
                float[] arr = new float[EffectParams.PARAM_COUNT];
                for (int i = 0; i < arr.length; i++) arr[i] = buf.readFloat();
                return arr;
            }
    );

    public static final StreamCodec<ByteBuf, AddEffectPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, packet.effectId());
                ByteBufCodecs.INT.encode(buf, packet.index());
                ByteBufCodecs.DOUBLE.encode(buf, packet.x());
                ByteBufCodecs.DOUBLE.encode(buf, packet.y());
                ByteBufCodecs.DOUBLE.encode(buf, packet.z());
                ByteBufCodecs.INT.encode(buf, packet.entityId());
                ByteBufCodecs.BOOL.encode(buf, packet.followEntity());
                ByteBufCodecs.INT.encode(buf, packet.duration());
                ByteBufCodecs.BOOL.encode(buf, packet.infinite());
                ByteBufCodecs.BOOL.encode(buf, packet.infiniteOverridden());
                PARAMS_CODEC.encode(buf, packet.params());
            },
            buf -> {
                UUID effectId = UUIDUtil.STREAM_CODEC.decode(buf);
                int index = ByteBufCodecs.INT.decode(buf);
                double x = ByteBufCodecs.DOUBLE.decode(buf);
                double y = ByteBufCodecs.DOUBLE.decode(buf);
                double z = ByteBufCodecs.DOUBLE.decode(buf);
                int entityId = ByteBufCodecs.INT.decode(buf);
                boolean followEntity = ByteBufCodecs.BOOL.decode(buf);
                int duration = ByteBufCodecs.INT.decode(buf);
                boolean infinite = ByteBufCodecs.BOOL.decode(buf);
                boolean infiniteOverridden = ByteBufCodecs.BOOL.decode(buf);
                float[] params = PARAMS_CODEC.decode(buf);
                return new AddEffectPacket(effectId, index, x, y, z, entityId, followEntity, duration, infinite, infiniteOverridden, params);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(AddEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.addEffect(packet);
            }
        });
    }
}