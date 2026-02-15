package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.StructureDivinationSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class StructureSelectionGui extends ButtonListGui<String> {
    public StructureSelectionGui(List<String> structureIds) {
        super(Component.literal("Select a Structure"), structureIds);
    }

    @Override
    protected Component getItemName(String structure) {
        return Component.literal(structure);
    }

    @Override
    protected void onItemSelected(String structureId) {
        PacketDistributor.sendToServer(new StructureDivinationSelectedPacket(structureId));
        minecraft.setScreen(null);
    }
}