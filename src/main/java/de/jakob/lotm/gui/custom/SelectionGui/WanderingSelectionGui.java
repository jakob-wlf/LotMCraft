package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.WanderingSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class WanderingSelectionGui extends ButtonListGui<String> {
    public WanderingSelectionGui(List<String> dimensionIds) {
        super(Component.literal("Select Dimension"), dimensionIds);
    }

    @Override
    protected Component getItemName(String dimensionId) {
        int colonIndex = dimensionId.indexOf(":");
        if (colonIndex != -1 && colonIndex < dimensionId.length() - 1) {
            return Component.literal(dimensionId.substring(colonIndex + 1).replace("_", " "));
        }
        return Component.literal(dimensionId);
    }

    @Override
    protected void onItemSelected(String dimensionId) {
        PacketDistributor.sendToServer(new WanderingSelectedPacket(dimensionId));
        minecraft.setScreen(null);
    }
}
