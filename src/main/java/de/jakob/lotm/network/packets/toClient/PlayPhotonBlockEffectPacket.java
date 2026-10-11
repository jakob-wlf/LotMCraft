package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;

public record PlayPhotonBlockEffectPacket(String effectPath, BlockPos pos, double xOffset, double yOffset, double zOffset,
                                          double scale, Quaternionf rot, int duration, boolean checkState, boolean allowMulti, Vec3 scaleVector) implements CustomPacketPayload {

    public static final Type<PlayPhotonBlockEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_photon_block_effect"));

    public static final StreamCodec<ByteBuf, PlayPhotonBlockEffectPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.effectPath());
                ByteBufCodecs.VAR_LONG.encode(buf, packet.pos().asLong());
                ByteBufCodecs.DOUBLE.encode(buf, packet.xOffset());
                ByteBufCodecs.DOUBLE.encode(buf, packet.yOffset());
                ByteBufCodecs.DOUBLE.encode(buf, packet.zOffset());
                ByteBufCodecs.DOUBLE.encode(buf, packet.scale());
                if(packet.rot() != null) {
                    ByteBufCodecs.FLOAT.encode(buf, packet.rot().x());
                    ByteBufCodecs.FLOAT.encode(buf, packet.rot().y());
                    ByteBufCodecs.FLOAT.encode(buf, packet.rot().z());
                    ByteBufCodecs.FLOAT.encode(buf, packet.rot().w());
                } else {
                    ByteBufCodecs.FLOAT.encode(buf, -1f);
                    ByteBufCodecs.FLOAT.encode(buf, -1f);
                    ByteBufCodecs.FLOAT.encode(buf, -1f);
                    ByteBufCodecs.FLOAT.encode(buf, -1f);
                }
                ByteBufCodecs.INT.encode(buf, packet.duration());
                ByteBufCodecs.BOOL.encode(buf, packet.checkState());
                ByteBufCodecs.BOOL.encode(buf, packet.allowMulti());
                if(packet.scaleVector != null) {
                    ByteBufCodecs.DOUBLE.encode(buf, packet.scaleVector.x());
                    ByteBufCodecs.DOUBLE.encode(buf, packet.scaleVector.y());
                    ByteBufCodecs.DOUBLE.encode(buf, packet.scaleVector.z());
                }
                else {
                    ByteBufCodecs.DOUBLE.encode(buf, -1.0);
                    ByteBufCodecs.DOUBLE.encode(buf, -1.0);
                    ByteBufCodecs.DOUBLE.encode(buf, -1.0);
                }
            },
            buf -> {
                String effectPath = ByteBufCodecs.STRING_UTF8.decode(buf);
                BlockPos pos = BlockPos.of(ByteBufCodecs.VAR_LONG.decode(buf));
                double xOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double yOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double zOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double scale = ByteBufCodecs.DOUBLE.decode(buf);
                float x = ByteBufCodecs.FLOAT.decode(buf);
                float y = ByteBufCodecs.FLOAT.decode(buf);
                float z = ByteBufCodecs.FLOAT.decode(buf);
                float w = ByteBufCodecs.FLOAT.decode(buf);
                Quaternionf rot = x == -1 && y == -1 && z == -1 && w == -1 ? null : new Quaternionf(x, y, z, w);
                int duration = ByteBufCodecs.INT.decode(buf);
                boolean checkState = ByteBufCodecs.BOOL.decode(buf);
                boolean allowMulti = ByteBufCodecs.BOOL.decode(buf);
                Vec3 scaleVector = null;
                double scaleX = ByteBufCodecs.DOUBLE.decode(buf);
                double scaleY = ByteBufCodecs.DOUBLE.decode(buf);
                double scaleZ = ByteBufCodecs.DOUBLE.decode(buf);
                if(scaleX != -1 && scaleY != -1 && scaleZ != -1 ) {
                    scaleVector = new Vec3(scaleX, scaleY, scaleZ);
                }
                return new PlayPhotonBlockEffectPacket(effectPath, pos, xOffset, yOffset, zOffset, scale, rot, duration, checkState, allowMulti, scaleVector);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PlayPhotonBlockEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.playPhotonBlockEffect(packet);
            }
        });
    }
}