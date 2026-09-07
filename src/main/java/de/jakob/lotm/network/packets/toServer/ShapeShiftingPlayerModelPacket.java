package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record ShapeShiftingPlayerModelPacket(String entityType, boolean isPlayerModel, boolean sequenceRestrict) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShapeShiftingPlayerModelPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shapeshifting_player_model"));

    public static final StreamCodec<ByteBuf, ShapeShiftingPlayerModelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ShapeShiftingPlayerModelPacket::entityType,
            ByteBufCodecs.BOOL,
            ShapeShiftingPlayerModelPacket::isPlayerModel,
            ByteBufCodecs.BOOL,
            ShapeShiftingPlayerModelPacket::sequenceRestrict,
            ShapeShiftingPlayerModelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShapeShiftingPlayerModelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ShapeShiftingUtil.performShapeShifting(serverPlayer, packet.entityType, packet.isPlayerModel, packet.sequenceRestrict);
            }
        });
    }
}
