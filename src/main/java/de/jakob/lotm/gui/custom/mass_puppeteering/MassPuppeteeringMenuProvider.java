package de.jakob.lotm.gui.custom.mass_puppeteering;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MassPuppeteeringMenuProvider implements MenuProvider {
    private final Map<LivingEntity, Integer> validTargets;

    public MassPuppeteeringMenuProvider(Map<LivingEntity, Integer> validTargets) {
        this.validTargets = validTargets;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.lotm.mass_puppeteering.title");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MassPuppeteeringMenu(containerId, playerInventory, validTargets);
    }
}