package de.jakob.lotm.abilities;

import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAbilityWheelHelper {
    
    public static void openWheel(SelectableAbilityItem abilityItem, ItemStack itemStack, Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new AbilityWheelScreen(abilityItem, itemStack, player));
    }
}