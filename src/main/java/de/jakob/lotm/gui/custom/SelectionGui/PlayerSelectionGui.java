package de.jakob.lotm.gui.custom.SelectionGui;

import de.jakob.lotm.network.packets.toServer.PlayerDivinationSelectedPacket;
import de.jakob.lotm.util.data.PlayerInfo;
import de.jakob.lotm.util.data.PlayerSelectionWorkType;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PlayerSelectionGui extends ButtonListGui<PlayerInfo> {
    private final PlayerSelectionWorkType type;

    public PlayerSelectionGui(List<PlayerInfo> players, PlayerSelectionWorkType type) {
        super(Component.literal("Select a Player"), players);
        this.type = type;
    }

    @Override
    protected Component getItemName(PlayerInfo player) {
        return Component.literal(player.name());
    }

    @Override
    protected void onItemSelected(PlayerInfo player) {
        PacketDistributor.sendToServer(new PlayerDivinationSelectedPacket(player.uuid(), type));
        minecraft.setScreen(null);
    }
}