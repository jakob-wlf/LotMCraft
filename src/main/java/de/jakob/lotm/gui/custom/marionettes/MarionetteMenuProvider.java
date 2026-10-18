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
    private final List<LivingEntity> marionettes;
    private final boolean servantMenu;

    public MarionetteMenuProvider(List<LivingEntity> marionettes) {
        this.marionettes = marionettes;
        this.servantMenu = false;
    }

    public static MarionetteMenuProvider servants() {
        return new MarionetteMenuProvider(true);
    }

    private MarionetteMenuProvider(boolean servantMenu) {
        this.marionettes = List.of();
        this.servantMenu = servantMenu;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(servantMenu ? "gui.lotm.servants.title" : "gui.lotm.marionette_control.title");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MarionetteMenu(containerId, playerInventory, marionettes.stream().map(LivingEntity::getId).toList());
    }
}
