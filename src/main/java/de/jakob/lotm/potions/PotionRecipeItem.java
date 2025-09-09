package de.jakob.lotm.potions;

import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PotionRecipeItem extends Item {
    private PotionRecipe recipe = null;

    public PotionRecipeItem(Properties properties) {
        super(properties);
    }

    public PotionRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(PotionRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.literal(PathwayInfos.getSequenceName(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) + " ").append(Component.translatable("lotm.potion_recipe"));
    }
}
