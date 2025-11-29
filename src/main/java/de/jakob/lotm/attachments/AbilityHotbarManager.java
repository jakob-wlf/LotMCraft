package de.jakob.lotm.attachments;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.artifacts.SealedArtifactItem;
import de.jakob.lotm.item.custom.ExcavatedAreaItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityHotbarPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class AbilityHotbarManager implements INBTSerializable<CompoundTag> {
    private static final int HOTBAR_SIZE = 9;
    private static final int MAX_ABILITY_HOTBARS = 3; // Number of ability hotbars

    private final ItemStack[][] abilityHotbars;
    private final ItemStack[] regularHotbar; // Store regular hotbar items
    private int currentHotbarIndex = -1; // -1 means regular hotbar, 0+ means ability hotbar

    private int lastUsedAbilityHotbar = 0;

    public AbilityHotbarManager() {
        abilityHotbars = new ItemStack[MAX_ABILITY_HOTBARS][HOTBAR_SIZE];
        regularHotbar = new ItemStack[HOTBAR_SIZE];

        for (int i = 0; i < MAX_ABILITY_HOTBARS; i++) {
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                abilityHotbars[i][j] = ItemStack.EMPTY;
            }
        }

        for (int i = 0; i < HOTBAR_SIZE; i++) {
            regularHotbar[i] = ItemStack.EMPTY;
        }
    }

    public boolean isAbilityHotbarActive() {
        return currentHotbarIndex >= 0;
    }

    public int getCurrentHotbarIndex() {
        return currentHotbarIndex;
    }

    public void setCurrentHotbarIndex(int currentHotbarIndex) {
        this.currentHotbarIndex = currentHotbarIndex;
    }

    public int getMaxHotbars() {
        return MAX_ABILITY_HOTBARS;
    }

    // Toggle between regular hotbar and ability hotbar
    public void toggleAbilityHotbar(Player player) {
        if (currentHotbarIndex == -1) {
            // Switch back to the last used ability hotbar instead of always hotbar 0
            int target = Math.max(0, lastUsedAbilityHotbar);
            swapToAbilityHotbar(player, target);
        } else {
            // Save current for next time, then go back to regular hotbar
            lastUsedAbilityHotbar = currentHotbarIndex;
            swapToRegularHotbar(player);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new SyncAbilityHotbarPacket(getCurrentHotbarIndex()));
        }
    }


    // Cycle to next ability hotbar
    public void cycleAbilityHotbar(Player player) {
        if (currentHotbarIndex == -1) {
            // If on regular hotbar, act like toggle
            swapToAbilityHotbar(player, 0);
        } else {
            // Cycle to next ability hotbar
            int nextIndex = (currentHotbarIndex + 1) % MAX_ABILITY_HOTBARS;
            swapToAbilityHotbar(player, nextIndex);
            lastUsedAbilityHotbar = nextIndex;
        }

        if(player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(serverPlayer, new SyncAbilityHotbarPacket(getCurrentHotbarIndex()));
        }
    }

    private void swapToAbilityHotbar(Player player, int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex >= MAX_ABILITY_HOTBARS) return;

        // If coming from regular hotbar, save it
        if (currentHotbarIndex == -1) {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                regularHotbar[i] = player.getInventory().getItem(i).copy();
            }
        }
        // If coming from another ability hotbar, save that one
        else if (currentHotbarIndex != hotbarIndex) {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                abilityHotbars[currentHotbarIndex][i] = player.getInventory().getItem(i).copy();
            }
        }

        // Clear the hotbar slots first
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        // Load new ability hotbar
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            ItemStack stackToPlace = abilityHotbars[hotbarIndex][i].copy();
            player.getInventory().setItem(i, stackToPlace);
        }

        currentHotbarIndex = hotbarIndex;
        player.containerMenu.broadcastChanges();
    }

    public void saveCurrentAbilityHotbar(Player player) {
        if (currentHotbarIndex >= 0 && currentHotbarIndex < MAX_ABILITY_HOTBARS) {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                abilityHotbars[currentHotbarIndex][i] = player.getInventory().getItem(i).copy();
            }
        }
    }

    private void swapToRegularHotbar(Player player) {
        if (currentHotbarIndex == -1) return;

        // Save current ability hotbar before switching
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            abilityHotbars[currentHotbarIndex][i] = player.getInventory().getItem(i).copy();
        }

        // Clear hotbar slots
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        // Load regular hotbar
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            player.getInventory().setItem(i, regularHotbar[i].copy());
        }

        currentHotbarIndex = -1;
        player.containerMenu.broadcastChanges();
    }

    public void resetToRegularHotbar(Player player) {
        if (currentHotbarIndex >= 0) {
            // Save current ability hotbar
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                abilityHotbars[currentHotbarIndex][i] = player.getInventory().getItem(i).copy();
            }

            // Clear hotbar slots
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }

            // Load regular hotbar
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                player.getInventory().setItem(i, regularHotbar[i].copy());
            }

            currentHotbarIndex = -1;
            player.containerMenu.broadcastChanges();
        }
    }

    // Save the current regular hotbar without switching
    public void saveCurrentRegularHotbar(Player player) {
        if (currentHotbarIndex == -1) {
            for (int i = 0; i < HOTBAR_SIZE; i++) {
                regularHotbar[i] = player.getInventory().getItem(i).copy();
            }
        }
    }

    // Validate that only ability items can be placed in ability hotbars
    public boolean canPlaceInAbilityHotbar(ItemStack stack) {
        return stack.getItem() instanceof AbilityItem || stack.getItem() instanceof ToggleAbilityItem || stack.getItem() instanceof ExcavatedAreaItem || stack.getItem() instanceof SealedArtifactItem;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CurrentHotbar", currentHotbarIndex);

        // Save regular hotbar
        ListTag regularHotbarTag = new ListTag();
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            CompoundTag itemTag = (CompoundTag) regularHotbar[i].saveOptional(provider);
            regularHotbarTag.add(itemTag);
        }
        tag.put("RegularHotbar", regularHotbarTag);

        // Save ability hotbars
        ListTag hotbarsTag = new ListTag();
        for (int i = 0; i < MAX_ABILITY_HOTBARS; i++) {
            CompoundTag hotbarTag = new CompoundTag();
            ListTag itemsTag = new ListTag();

            for (int j = 0; j < HOTBAR_SIZE; j++) {
                CompoundTag itemTag = (CompoundTag) abilityHotbars[i][j].saveOptional(provider);
                itemsTag.add(itemTag);
            }

            hotbarTag.put("Items", itemsTag);
            hotbarsTag.add(hotbarTag);
        }

        tag.put("AbilityHotbars", hotbarsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        currentHotbarIndex = tag.getInt("CurrentHotbar");

        // Load regular hotbar
        ListTag regularHotbarTag = tag.getList("RegularHotbar", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(regularHotbarTag.size(), HOTBAR_SIZE); i++) {
            regularHotbar[i] = ItemStack.parseOptional(provider, regularHotbarTag.getCompound(i));
        }
        // Fill remaining slots with empty stacks if data is missing
        for (int i = regularHotbarTag.size(); i < HOTBAR_SIZE; i++) {
            regularHotbar[i] = ItemStack.EMPTY;
        }

        // Load ability hotbars
        ListTag hotbarsTag = tag.getList("AbilityHotbars", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(hotbarsTag.size(), MAX_ABILITY_HOTBARS); i++) {
            CompoundTag hotbarTag = hotbarsTag.getCompound(i);
            ListTag itemsTag = hotbarTag.getList("Items", Tag.TAG_COMPOUND);

            for (int j = 0; j < Math.min(itemsTag.size(), HOTBAR_SIZE); j++) {
                CompoundTag itemTag = itemsTag.getCompound(j);
                abilityHotbars[i][j] = ItemStack.parseOptional(provider, itemTag);
            }
            // Fill remaining slots with empty stacks if data is missing
            for (int j = itemsTag.size(); j < HOTBAR_SIZE; j++) {
                abilityHotbars[i][j] = ItemStack.EMPTY;
            }
        }
        // Fill remaining ability hotbars if data is missing
        for (int i = hotbarsTag.size(); i < MAX_ABILITY_HOTBARS; i++) {
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                abilityHotbars[i][j] = ItemStack.EMPTY;
            }
        }
    }
}