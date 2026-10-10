package de.jakob.lotm.beyonders.potions;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record PotionRecipe(
        BeyonderPotion potion,
        ItemStack supplementaryIngredient1,
        ItemStack supplementaryIngredient2,
        ItemStack supplementaryIngredient3,
        ItemStack supplementaryIngredient4,
        ItemStack mainIngredient
) {
    public List<ItemStack> supplementaryIngredients() {
        List<ItemStack> supplementaryIngredients = new ArrayList<>();
        supplementaryIngredients.add(supplementaryIngredient1);
        supplementaryIngredients.add(supplementaryIngredient2);
        supplementaryIngredients.add(supplementaryIngredient3);
        supplementaryIngredients.add(supplementaryIngredient4);
        return supplementaryIngredients;
    }
}
