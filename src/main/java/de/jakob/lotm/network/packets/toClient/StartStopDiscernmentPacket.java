package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartStopDiscernmentPacket(boolean active, int range) implements CustomPacketPayload {
    public static final Type<StartStopDiscernmentPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "start_stop_discernment"));

    public static final StreamCodec<FriendlyByteBuf, StartStopDiscernmentPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    StartStopDiscernmentPacket::active,
                    ByteBufCodecs.INT,
                    StartStopDiscernmentPacket::range,
                    StartStopDiscernmentPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartStopDiscernmentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.syncDiscernmentAbility(packet, context.player());
            }
        });
    }
}
