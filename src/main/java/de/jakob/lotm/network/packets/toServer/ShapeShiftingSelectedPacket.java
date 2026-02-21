package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;


import static de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil.shapeShift;

public record ShapeShiftingSelectedPacket (String entityType)implements CustomPacketPayload {

    public static final Type<ShapeShiftingSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "player_shape_shifting_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapeShiftingSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ShapeShiftingSelectedPacket::entityType,
                    ShapeShiftingSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShapeShiftingSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            shapeShift(player, packet.entityType);
        });
    }
}