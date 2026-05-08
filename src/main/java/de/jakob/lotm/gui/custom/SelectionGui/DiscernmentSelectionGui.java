package de.jakob.lotm.gui.custom.SelectionGui;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.network.packets.toServer.DiscernmentSelectedPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DiscernmentSelectionGui extends ButtonListGui<Pair<String, Integer>>{
    public DiscernmentSelectionGui(List<Pair<String, Integer>> items) {
        super(Component.literal("Select Pathway and Sequence"), items);
    }

    @Override
    protected Component getItemName(Pair<String, Integer> item) {
        return Component.literal("Path: " + item.getFirst() + " Seq: " + item.getSecond());
    }

    @Override
    protected void onItemSelected(Pair<String, Integer> item) {
        PacketDistributor.sendToServer(new DiscernmentSelectedPacket(item));
        minecraft.setScreen(null);
    }
}
