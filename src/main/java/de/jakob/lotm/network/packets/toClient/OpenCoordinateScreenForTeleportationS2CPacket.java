package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCoordinateScreenForTeleportationS2CPacket() implements CustomPacketPayload {
    public static final Type<OpenCoordinateScreenForTeleportationS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_coordinate_screen_teleportation"));

    public static final StreamCodec<FriendlyByteBuf, OpenCoordinateScreenForTeleportationS2CPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenCoordinateScreenForTeleportationS2CPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(OpenCoordinateScreenForTeleportationS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openCoordinateScreen(context.player(), "teleportation");
            }
        });
    }
}