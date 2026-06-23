package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartStopDiscernmentS2CPacket(boolean active, int range) implements CustomPacketPayload {
    public static final Type<StartStopDiscernmentS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "start_stop_discernment"));

    public static final StreamCodec<FriendlyByteBuf, StartStopDiscernmentS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    StartStopDiscernmentS2CPacket::active,
                    ByteBufCodecs.INT,
                    StartStopDiscernmentS2CPacket::range,
                    StartStopDiscernmentS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartStopDiscernmentS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncDiscernmentAbility(packet, context.player());
            }
        });
    }
}
