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

public record OpenWanderingSelectionScreenPacket(List<String> dimensionIds) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenWanderingSelectionScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_wandering_selection_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWanderingSelectionScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    OpenWanderingSelectionScreenPacket::dimensionIds,
                    OpenWanderingSelectionScreenPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWanderingSelectionScreenPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleWanderingSelectionScreenPacket(payload));
    }
}
