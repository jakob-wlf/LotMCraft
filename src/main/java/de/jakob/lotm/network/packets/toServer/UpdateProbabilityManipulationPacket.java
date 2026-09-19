package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.ProbabilityManipulationManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateProbabilityManipulationPacket(String abilityId, int chance)
        implements CustomPacketPayload {
    public static final Type<UpdateProbabilityManipulationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_probability_manipulation"));
    public static final StreamCodec<ByteBuf, UpdateProbabilityManipulationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UpdateProbabilityManipulationPacket::abilityId,
                    ByteBufCodecs.INT, UpdateProbabilityManipulationPacket::chance,
                    UpdateProbabilityManipulationPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateProbabilityManipulationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()
                    && context.player() instanceof ServerPlayer player) {
                ProbabilityManipulationManager.get(player.server)
                        .updateRule(player, packet.abilityId, packet.chance);
            }
        });
    }
}