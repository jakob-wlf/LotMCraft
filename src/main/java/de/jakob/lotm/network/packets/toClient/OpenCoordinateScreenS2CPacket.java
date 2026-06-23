package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCoordinateScreenS2CPacket(String use) implements CustomPacketPayload {
    public static final Type<OpenCoordinateScreenS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_coordinate_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenCoordinateScreenS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenCoordinateScreenS2CPacket::use,
            OpenCoordinateScreenS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(OpenCoordinateScreenS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.openCoordinateScreen(context.player(), packet.use);
            }
        });
    }
}