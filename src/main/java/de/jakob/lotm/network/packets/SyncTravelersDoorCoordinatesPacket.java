package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.door.TravelersDoorAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncTravelersDoorCoordinatesPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SyncTravelersDoorCoordinatesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_travelers_door_coordinates"));

    public static final StreamCodec<FriendlyByteBuf, SyncTravelersDoorCoordinatesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesPacket::x,
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesPacket::y,
                    ByteBufCodecs.DOUBLE, SyncTravelersDoorCoordinatesPacket::z,
                    SyncTravelersDoorCoordinatesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTravelersDoorCoordinatesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(!TravelersDoorAbility.travelersDoorUsers.containsKey(context.player().getUUID())) {
                TravelersDoorAbility.travelersDoorUsers.put(context.player().getUUID(), BlockPos.containing(packet.x, packet.y, packet.z));
            }
        });
    }
}