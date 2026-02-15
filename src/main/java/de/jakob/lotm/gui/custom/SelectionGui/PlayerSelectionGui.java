package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.PlayerDivinationSelectedPacket;
import de.jakob.lotm.util.data.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PlayerSelectionGui extends ButtonListGui<PlayerInfo> {
    public PlayerSelectionGui(List<PlayerInfo> players) {
        super(Component.literal("Select a Player"), players);
    }

    @Override
    protected Component getItemName(PlayerInfo player) {
        return Component.literal(player.name());
    }

    @Override
    protected void onItemSelected(PlayerInfo player) {
        PacketDistributor.sendToServer(new PlayerDivinationSelectedPacket(player.uuid()));
        minecraft.setScreen(null);
    }
}