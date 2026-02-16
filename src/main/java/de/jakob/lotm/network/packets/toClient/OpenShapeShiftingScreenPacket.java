package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.SelectionGui.ShapeShiftingSelectionGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenShapeShiftingScreenPacket(List<String> entityTypes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenShapeShiftingScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_shape_shifting_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShapeShiftingScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    OpenShapeShiftingScreenPacket::entityTypes,
                    OpenShapeShiftingScreenPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenShapeShiftingScreenPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ShapeShiftingSelectionGui(payload.entityTypes()));
        });
    }
}