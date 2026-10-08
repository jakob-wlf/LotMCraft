package de.jakob.lotm.beyonders.potions;

import net.minecraft.world.item.ItemStack;

public record PotionRecipePlaceholder(
        BeyonderPotion potion,
        ItemStack supplementaryIngredient1,
        ItemStack supplementaryIngredient2,
        ItemStack supplementaryIngredient3,
        ItemStack supplementaryIngredient4,
        ItemStack mainIngredient
) {
}
