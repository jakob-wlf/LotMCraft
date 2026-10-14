package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ClientPuppetTimerCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PuppetTimerPacket(int targetId, int totalTicks, int remainingTicks) implements CustomPacketPayload {

    public static final Type<PuppetTimerPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "puppet_timer"));

    public static final StreamCodec<ByteBuf, PuppetTimerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PuppetTimerPacket::targetId,
            ByteBufCodecs.VAR_INT, PuppetTimerPacket::totalTicks,
            ByteBufCodecs.VAR_INT, PuppetTimerPacket::remainingTicks,
            PuppetTimerPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PuppetTimerPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                ClientPuppetTimerCache.update(packet.targetId(), packet.totalTicks(), packet.remainingTicks()));
    }
}