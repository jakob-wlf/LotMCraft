package de.jakob.lotm.beyonders.potions;

import net.minecraft.world.item.ItemStack;

public record PotionRecipe(
        BeyonderPotion potion,
        ItemStack supplementaryIngredient1,
        ItemStack supplementaryIngredient2,
        ItemStack mainIngredient
) {
}
