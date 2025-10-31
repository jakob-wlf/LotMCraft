package de.jakob.lotm.datagen;

import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;


public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Shaped recipe example
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BREWING_CAULDRON.asItem())
            .pattern("I I")
            .pattern("IBI")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('B', Items.BLAZE_POWDER)
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(recipeOutput);
    }
}