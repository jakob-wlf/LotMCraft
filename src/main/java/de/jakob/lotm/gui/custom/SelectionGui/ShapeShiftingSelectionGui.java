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
        String[] parts = shape.split(":");
        if (parts.length == 0) return Component.literal(shape);

        // to remove the uuid from player names, im not responsible for cracked minecraft players tho.. (they can have the same name)
        if (shape.contains("player")) {
            return Component.literal("Player : " + parts[1]);
        }

        // to remove the "mod_id:" from other names
        return Component.literal(parts[1]);
    }

    @Override
    protected void onItemSelected(String entityType) {
        PacketDistributor.sendToServer(new ShapeShiftingSelectedPacket(entityType, true));
        minecraft.setScreen(null);
    }
}