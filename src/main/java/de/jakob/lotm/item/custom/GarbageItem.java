package de.jakob.lotm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Garbage item. Once placed in a player's inventory it is locked to that
 * slot and cannot be moved or removed until a Garbage Collector event cleans it.
 *
 * The locked slot is stored in the item's CustomData NBT under "GarbageSlot".
 * A value of -1 means the slot has not yet been assigned.
 */
public class GarbageItem extends Item {

    public static final String NBT_SLOT = "GarbageSlot";

    public GarbageItem(Properties properties) {
        super(properties);
    }

    // ── Slot locking helpers ─────────────────────────────────────────────────

    /** Returns the slot this Garbage item is locked to, or -1 if not yet locked. */
    public static int getLockedSlot(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return -1;
        CompoundTag tag = data.copyTag();
        return tag.contains(NBT_SLOT) ? tag.getInt(NBT_SLOT) : -1;
    }

    /** Locks this Garbage item to the given inventory slot index. */
    public static void lockToSlot(ItemStack stack, int slot) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> {
            CompoundTag tag = existing.copyTag();
            tag.putInt(NBT_SLOT, slot);
            return CustomData.of(tag);
        });
    }

    // ── Drop prevention ───────────────────────────────────────────────────────

    @Override
    public boolean onDroppedByPlayer(@NotNull ItemStack stack, @NotNull Player player) {
        return false; // cannot be dropped
    }

    // ── Tooltip ──────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("A relic of a failed transaction.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lines.add(Component.literal("Cannot be moved or removed.").withStyle(ChatFormatting.RED));
    }
}
