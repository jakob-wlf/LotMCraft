package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCoordinateScreenTravelersDoorS2CPacket() implements CustomPacketPayload {
    public static final Type<OpenCoordinateScreenTravelersDoorS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_coordinate_travelers_door_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenCoordinateScreenTravelersDoorS2CPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenCoordinateScreenTravelersDoorS2CPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(OpenCoordinateScreenTravelersDoorS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openCoordinateScreen(context.player(), "travelers_door");
            }
        });
    }
}