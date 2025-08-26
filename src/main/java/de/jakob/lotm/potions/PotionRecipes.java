package de.jakob.lotm.potions;

import de.jakob.lotm.item.ModIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class PotionRecipes {

    public static final Set<PotionRecipe> RECIPES = new HashSet<>();

    public static void initPotionRecipes() {
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SEER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.FERMENTED_SPIDER_EYE, 1),
                new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.CLOWN_POTION.get(),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(Items.APPLE, 1),
                new ItemStack(ModIngredients.HORNACIS_GRAY_MOUNTAIN_GOAT_HORN.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.MAGICIAN_POTION.get(),
                new ItemStack(Items.PRISMARINE_CRYSTALS, 1),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(ModIngredients.ROOT_OF_MIST_TREANT.get())
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.APPRENTICE_POTION.get(),
                new ItemStack(Items.ROSE_BUSH, 1),
                new ItemStack(Items.OAK_DOOR, 1),
                new ItemStack(ModIngredients.ILLUSION_CRYSTAL.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.TRICKMASTER_POTION.get(),
                new ItemStack(Items.SHORT_GRASS, 1),
                new ItemStack(Items.POPPY, 1),
                new ItemStack(ModIngredients.SPIRIT_EATER_STOMACH_POUCH.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.ASTROLOGER_POTION.get(),
                new ItemStack(Items.AMETHYST_SHARD, 1),
                new ItemStack(Items.SPIDER_EYE, 1),
                new ItemStack(ModIngredients.METEORITE_CRYSTAL.get())
        ));


        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.BARD_POTION.get(),
                new ItemStack(Items.SWEET_BERRIES, 1),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(ModIngredients.CRYSTAL_SUNFLOWER.get())
        ));

        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.LIGHT_SUPPLICANT_POTION.get(),
                new ItemStack(Items.SUNFLOWER, 1),
                new ItemStack(Items.GLOW_BERRIES, 1),
                new ItemStack(ModIngredients.POWDER_OF_DAZZLING_SOUL.get())
        ));
        RECIPES.add(new PotionRecipe(
                (BeyonderPotion) PotionItemHandler.SOLAR_HIGH_PRIEST_POTION.get(),
                new ItemStack(Items.MAGMA_BLOCK, 1),
                new ItemStack(Items.LAVA_BUCKET, 1),
                new ItemStack(ModIngredients.SPIRIT_PACT_TREE_FRUIT.get())
        ));
    }

    @Nullable
    public static BeyonderPotion getByIngredients(ItemStack supp1, ItemStack supp2, ItemStack main) {
        PotionRecipe recipe = RECIPES.stream().filter(r -> {
            if(!areSimilar(r.mainIngredient(), main))
                return false;

            if(!areSimilar(r.supplementaryIngredient1(), supp1)) {
                return areSimilar(r.supplementaryIngredient1(), supp2) && areSimilar(r.supplementaryIngredient2(), supp1);
            }
            return areSimilar(r.supplementaryIngredient2(), supp2);
        }).findFirst().orElse(null);

        return recipe == null ? null : recipe.potion();
    }

    public static boolean areSimilar(ItemStack i1, ItemStack i2) {
        return i1.is(i2.getItem()) && i1.getCount() == i2.getCount();
    }
}
