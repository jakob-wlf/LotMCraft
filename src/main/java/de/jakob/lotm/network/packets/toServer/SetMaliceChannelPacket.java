package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed.MaliceSeedAbility;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SetMaliceChannelPacket(UUID hostUUID, int mode) implements CustomPacketPayload {
    public static final Type<SetMaliceChannelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "set_malice_channel"));

    public static final StreamCodec<ByteBuf, SetMaliceChannelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), SetMaliceChannelPacket::hostUUID,
            ByteBufCodecs.VAR_INT, SetMaliceChannelPacket::mode,
            SetMaliceChannelPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
    public static void handle(SetMaliceChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {if (context.player() instanceof ServerPlayer player) {MaliceSeedAbility.setChannel(player, packet.hostUUID(), packet.mode());}});
    }
}