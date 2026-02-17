package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.SelectionGui.StructureSelectionGui;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenStructureDivinationScreenPacket(List<String> structureIds) implements CustomPacketPayload {
    public static final Type<OpenStructureDivinationScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_structure_divination_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenStructureDivinationScreenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    OpenStructureDivinationScreenPacket::structureIds,
                    OpenStructureDivinationScreenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenStructureDivinationScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isClient()) {
                ClientHandler.handleStructureDivinationScreenPacket(packet);
            }
        });
    }
}