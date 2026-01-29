package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncDreamDivinationCoordinatesPacket(int x, int y, int z, int id) implements CustomPacketPayload {
    public static final Type<SyncDreamDivinationCoordinatesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_dream_divination_coordinates"));

    public static final StreamCodec<FriendlyByteBuf, SyncDreamDivinationCoordinatesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncDreamDivinationCoordinatesPacket::x,
                    ByteBufCodecs.INT, SyncDreamDivinationCoordinatesPacket::y,
                    ByteBufCodecs.INT, SyncDreamDivinationCoordinatesPacket::z,
                    ByteBufCodecs.INT, SyncDreamDivinationCoordinatesPacket::id,
                    SyncDreamDivinationCoordinatesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDreamDivinationCoordinatesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                if(context.player().getId() != packet.id) {
                    return;
                }

                DivinationAbility.performDreamDivination(context.player().level(), context.player(), new BlockPos(packet.x, packet.y, packet.z));
            }
        });
    }
}