package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDreamDivinationCoordinatesPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SyncDreamDivinationCoordinatesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_dream_divination_coordinates"));

    public static final StreamCodec<FriendlyByteBuf, SyncDreamDivinationCoordinatesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SyncDreamDivinationCoordinatesPacket::x,
                    ByteBufCodecs.DOUBLE, SyncDreamDivinationCoordinatesPacket::y,
                    ByteBufCodecs.DOUBLE, SyncDreamDivinationCoordinatesPacket::z,
                    SyncDreamDivinationCoordinatesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDreamDivinationCoordinatesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(!DivinationAbility.dreamDivinationUsers.containsKey(context.player().getUUID())) {
                DivinationAbility.dreamDivinationUsers.put(context.player().getUUID(), BlockPos.containing(packet.x, packet.y, packet.z));
            }
        });
    }
}