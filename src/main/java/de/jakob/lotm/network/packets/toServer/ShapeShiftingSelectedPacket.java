package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.ShapeShiftingSyncPacket;
import de.jakob.lotm.util.shapeShifting.DimensionsRefresher;
import de.jakob.lotm.util.shapeShifting.NameUtils;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

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

    public static void handle(ShapeShiftingSelectedPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            // reset fly for any unexpected exploits just in case
            if (!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }

            TransformData data = (TransformData) player;
            data.setCurrentShape(msg.entityType());

            ((DimensionsRefresher) player).shape_refreshDimensions();
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new ShapeShiftingSyncPacket(player.getUUID(), msg.entityType()));

            if (msg.entityType.startsWith("player:")){
                String playerName = msg.entityType().split(":")[1];
                NameUtils.setPlayerName(player, playerName);
            }else {
                String entityName = msg.entityType();
                entityName = entityName.contains(":") ? entityName.split(":")[1] : entityName;

                if (List.of("bat", "phantom", "blaze", "allay", "bee", "ghast", "parrot", "vex").contains(entityName)) {
                    player.getAbilities().mayfly = true;
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                }
            }
        });
    }
}