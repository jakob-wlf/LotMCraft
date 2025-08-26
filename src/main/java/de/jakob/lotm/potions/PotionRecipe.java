package de.jakob.lotm.potions;

import net.minecraft.world.item.ItemStack;

public record PotionRecipe(
        BeyonderPotion potion,
        ItemStack supplementaryIngredient1,
        ItemStack supplementaryIngredient2,
        ItemStack mainIngredient
) {
}
