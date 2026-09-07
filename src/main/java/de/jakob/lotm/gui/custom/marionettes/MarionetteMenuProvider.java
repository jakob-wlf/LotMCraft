package de.jakob.lotm.gui.custom.marionettes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MarionetteMenuProvider implements MenuProvider {
    private List<LivingEntity> marionettes;

    public MarionetteMenuProvider(List<LivingEntity> marionettes) {
        this.marionettes = marionettes;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("gui.lotm.marionette_control.title");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MarionetteMenu(containerId, playerInventory, marionettes.stream().map(LivingEntity::getId).toList());
    }
}
