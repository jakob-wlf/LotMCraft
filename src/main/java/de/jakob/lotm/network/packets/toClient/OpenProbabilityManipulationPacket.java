package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record OpenProbabilityManipulationPacket(
    Map<String, Integer> rules, int maximumAbilities, int minimumChance, int maximumChance,
    int ownerSequence)
        implements CustomPacketPayload {
    public static final Type<OpenProbabilityManipulationPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_probability_manipulation"));
    public static final StreamCodec<ByteBuf, OpenProbabilityManipulationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT),
                    OpenProbabilityManipulationPacket::rules,
                    ByteBufCodecs.INT, OpenProbabilityManipulationPacket::maximumAbilities,
                    ByteBufCodecs.INT, OpenProbabilityManipulationPacket::minimumChance,
                    ByteBufCodecs.INT, OpenProbabilityManipulationPacket::maximumChance,
                    ByteBufCodecs.INT, OpenProbabilityManipulationPacket::ownerSequence,
                    OpenProbabilityManipulationPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenProbabilityManipulationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openProbabilityManipulationScreen(packet);
            }
        });
    }
}