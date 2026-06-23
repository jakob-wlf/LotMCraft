package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.BiomeDivinationSelectedC2SPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class BiomeSelectionGui extends ButtonListGui<String> {
    public BiomeSelectionGui(List<String> biomeIds) {
        super(Component.literal("Select a Biome"), biomeIds);
    }

    @Override
    protected Component getItemName(String biome) {
        int columnIndex = biome.indexOf(":");
        if(columnIndex != -1 && columnIndex < biome.length() - 1) {
            return Component.literal(biome.substring(columnIndex + 1));
        }
        return Component.literal(biome);
    }

    @Override
    protected void onItemSelected(String biomeId) {
        PacketDistributor.sendToServer(new BiomeDivinationSelectedC2SPacket(biomeId));
        minecraft.setScreen(null);
    }
}