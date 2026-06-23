package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenEnvisionLocationScreenS2CPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenEnvisionLocationScreenS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_envision_location_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenEnvisionLocationScreenS2CPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenEnvisionLocationScreenS2CPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenEnvisionLocationScreenS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openCoordinateScreen(context.player(), "envision_location");
            }
        });
    }
}
