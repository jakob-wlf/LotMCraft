package de.jakob.lotm.gui.custom.selection_gui;

import de.jakob.lotm.network.packets.toServer.HistoricalVoidBorrowingSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class HistoricalVoidBorrowingSelectionGui  extends ButtonListGui<String> {
    public HistoricalVoidBorrowingSelectionGui(List<String> options) {
        super(Component.literal("Select something to borrow from history"), options);
    }

    @Override
    protected Component getItemName(String option) {
        return Component.literal(option);
    }

    @Override
    protected void onItemSelected(String option) {
        PacketDistributor.sendToServer(new HistoricalVoidBorrowingSelectedPacket(option));
        minecraft.setScreen(null);
    }
}