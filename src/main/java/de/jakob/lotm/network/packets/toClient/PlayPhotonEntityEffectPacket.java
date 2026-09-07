package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Quaternionf;

public record PlayPhotonEntityEffectPacket(String effectPath, int entityId, double xOffset, double yOffset, double zOffset,
                                           double scale, Quaternionf rot, boolean checkState, boolean allowMulti) implements CustomPacketPayload {

    public static final Type<PlayPhotonEntityEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "play_photon_entity_effect"));

    public static final StreamCodec<ByteBuf, PlayPhotonEntityEffectPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.effectPath());
                ByteBufCodecs.VAR_INT.encode(buf, packet.entityId());
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
                ByteBufCodecs.BOOL.encode(buf, packet.checkState());
                ByteBufCodecs.BOOL.encode(buf, packet.allowMulti());
            },
            buf -> {
                String effectPath = ByteBufCodecs.STRING_UTF8.decode(buf);
                int entityId = ByteBufCodecs.VAR_INT.decode(buf);
                double xOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double yOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double zOffset = ByteBufCodecs.DOUBLE.decode(buf);
                double scale = ByteBufCodecs.DOUBLE.decode(buf);
                float x = ByteBufCodecs.FLOAT.decode(buf);
                float y = ByteBufCodecs.FLOAT.decode(buf);
                float z = ByteBufCodecs.FLOAT.decode(buf);
                float w = ByteBufCodecs.FLOAT.decode(buf);
                Quaternionf rot = x == -1 && y == -1 && z == -1 && w == -1 ? null : new Quaternionf(x, y, z, w);
                boolean checkState = ByteBufCodecs.BOOL.decode(buf);
                boolean allowMulti = ByteBufCodecs.BOOL.decode(buf);
                return new PlayPhotonEntityEffectPacket(effectPath, entityId, xOffset, yOffset, zOffset, scale, rot, checkState, allowMulti);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PlayPhotonEntityEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientHandler.playPhotonEntityEffect(packet);
            }
        });
    }
}