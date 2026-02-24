package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.ShapeShiftingSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ShapeShiftingSelectionGui extends ButtonListGui<String> {
    public ShapeShiftingSelectionGui(List<String> entityTypes) {
        super(Component.literal("Select Entity"), entityTypes);
    }

    @Override
    protected Component getItemName(String shape) {
        int columnIndex = shape.indexOf(":");
        if(columnIndex != -1 && columnIndex < shape.length() - 1) {
            return Component.literal(shape.substring(columnIndex + 1));
        }
        return Component.literal(shape);
    }

    @Override
    protected void onItemSelected(String entityType) {
        PacketDistributor.sendToServer(new ShapeShiftingSelectedPacket(entityType, true));
        minecraft.setScreen(null);
    }
}