package de.jakob.lotm.beyonders.potions;

import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PotionRecipePlaceholders {

    public static final Set<PotionRecipePlaceholder> RECIPES = new HashSet<>();

    public static boolean initialized = false;

    public static void initPotionRecipes() {
        initialized = true;
        RECIPES.add(new PotionRecipePlaceholder(
                (BeyonderPotion) PotionItemHandler.SEER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.FERMENTED_SPIDER_EYE, 1),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get())
        ));
    }
}
