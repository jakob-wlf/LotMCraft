package de.jakob.lotm.gui.custom.Envisioning;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class EnvisioningScreen extends CreativeModeInventoryScreen {

    public EnvisioningScreen(Player player) {
        super((LocalPlayer) player, Minecraft.getInstance().level.enabledFeatures(), true);
    }

    @Override
    public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int mouseButton) {
        return false;
    }
}
