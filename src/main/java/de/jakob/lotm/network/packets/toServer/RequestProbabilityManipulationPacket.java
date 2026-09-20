package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.beyonders.sefirah.ProbabilityManipulationManager;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenProbabilityManipulationPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record RequestProbabilityManipulationPacket() implements CustomPacketPayload {
    public static final Type<RequestProbabilityManipulationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_probability_manipulation"));
    public static final StreamCodec<FriendlyByteBuf, RequestProbabilityManipulationPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestProbabilityManipulationPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestProbabilityManipulationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()
                    || !(context.player() instanceof ServerPlayer player)
                    || !"key_of_light".equals(SefirotData.get(player.server)
                    .getClaimedSefirot(player.getUUID()))) return;
            int sequence = BeyonderData.getSequence(player);
            ProbabilityManipulationManager data = ProbabilityManipulationManager.get(player.server);
            ProbabilityManipulationManager.ChanceRange range =
                    ProbabilityManipulationManager.getChanceRange(sequence);
            PacketHandler.sendToPlayer(player, new OpenProbabilityManipulationPacket(
                    new HashMap<>(data.getEffectiveFailureChances(player.server)),
                    ProbabilityManipulationManager.getMaximumAbilities(sequence),
                    range.minimum(), range.maximum(), sequence));
        });
    }
}