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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MYSTICAL_RING.asItem())
                .pattern("GDG")
                .pattern("DND")
                .pattern("GDG")
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DIAMOND)
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GUIDING_BOOK.get())
                .requires(Items.BOOK)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CRYSTAL_BALL.asItem())
                .pattern("GGG")
                .pattern("GAG")
                .pattern("CNC")
                .define('G', Items.GLASS)
                .define('A', Items.AMETHYST_SHARD)
                .define('C', Items.COPPER_INGOT)
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CANE.asItem())
                .pattern("  I")
                .pattern(" S ")
                .pattern("S  ")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
    }
}