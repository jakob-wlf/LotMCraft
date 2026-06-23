package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenShapeShiftingScreenS2CPacket(List<String> entityTypes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenShapeShiftingScreenS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_shape_shifting_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShapeShiftingScreenS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    OpenShapeShiftingScreenS2CPacket::entityTypes,
                    OpenShapeShiftingScreenS2CPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenShapeShiftingScreenS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleShapeShiftingScreenPacket(payload);
        });
    }
}