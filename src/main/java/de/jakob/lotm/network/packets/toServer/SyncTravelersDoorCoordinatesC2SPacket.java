package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.door.TravelersDoorAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncTravelersDoorCoordinatesC2SPacket(double x, double y, double z, int id) implements CustomPacketPayload {
    public static final Type<SyncTravelersDoorCoordinatesC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_travelers_door_coordinates"));

    public static final StreamCodec<FriendlyByteBuf, SyncTravelersDoorCoordinatesC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesC2SPacket::x,
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesC2SPacket::y,
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesC2SPacket::z,
                    ByteBufCodecs.INT, SyncTravelersDoorCoordinatesC2SPacket::id,
                    SyncTravelersDoorCoordinatesC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTravelersDoorCoordinatesC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                if(context.player().getId() != packet.id) {
                    return;
                }
                TravelersDoorAbility.travelersDoorUsers.put(context.player().getUUID(), BlockPos.containing(packet.x, packet.y, packet.z));
            }
        });
    }
}