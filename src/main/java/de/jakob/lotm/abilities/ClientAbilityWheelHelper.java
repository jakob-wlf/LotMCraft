package de.jakob.lotm.abilities;

import de.jakob.lotm.rendering.AbilityWheelOverlay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAbilityWheelHelper {

    public static void openWheel(SelectableAbilityItem abilityItem, Player player) {
        AbilityWheelOverlay.getInstance().open(abilityItem, player);
    }

    public static void closeWheel() {
        AbilityWheelOverlay.getInstance().close();
    }

    public static boolean isWheelOpen() {
        return AbilityWheelOverlay.getInstance().isOpen();
    }

    public static SelectableAbilityItem getCurrentAbilityItem() {
        return AbilityWheelOverlay.getInstance().getAbilityItem();
    }
}