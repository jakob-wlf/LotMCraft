package de.jakob.lotm.datagen;

import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;


public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BREWING_CAULDRON.asItem())
            .pattern("I I")
            .pattern("IBI")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('B', Items.BLAZE_POWDER)
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
            .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GUIDING_BOOK.get())
                .requires(Items.BOOK)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipeOutput);
    }
}