package de.jakob.lotm.gui.custom.Recipe;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipeMenuProvider implements MenuProvider {
    private final List<ItemStack> ingredients;

    private final String pathway;
    private final int sequence;



    public RecipeMenuProvider(List<ItemStack> ingredients, String pathway, int sequence) {
        this.ingredients = ingredients;
        this.pathway = pathway;
        this.sequence = sequence;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal(BeyonderData.pathwayInfos.get(pathway).getSequenceName(sequence)).append(" ").append(Component.translatable("lotm.potion_recipe")).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RecipeMenu(ingredients, containerId, playerInventory);
    }
}