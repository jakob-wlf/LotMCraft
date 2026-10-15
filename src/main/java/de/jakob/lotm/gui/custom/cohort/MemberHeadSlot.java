package de.jakob.lotm.gui.custom.cohort;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MemberHeadSlot extends SlotItemHandler {

    public MemberHeadSlot(ItemStackHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return false; // head never leaves the slot — drag, shift-click, and Q are all blocked
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}
