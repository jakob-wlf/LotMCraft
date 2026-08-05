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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SEALED_BOTTLE.get())
                .pattern("NDN")
                .pattern("DBD")
                .pattern("NDN")
                .define('N', Items.NETHERITE_INGOT)
                .define('D', Items.DIAMOND)
                .define('B', de.jakob.lotm.beyonders.potions.PotionItemHandler.EMPTY_BOTTLE.get())
                .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MYSTERIOUS_TABLET.get())
            .requires(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get())
            .requires(ModItems.RIGHT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get())
            .requires(ModItems.LEFT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get())
            .requires(ModItems.LOWER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get())
            .unlockedBy("has_upper_fragment", has(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get()))
            .save(recipeOutput);

        // ─── Blasphemy Slate — Left Half ───
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLASPHEMY_SLATE_LEFT_HALF.get())
                .requires(ModItems.SUN_BLASPHEMY_CARD.get())
                .requires(ModItems.TYRANT_BLASPHEMY_CARD.get())
                .requires(ModItems.VISIONARY_BLASPHEMY_CARD.get())
                .requires(ModItems.JUSTICIAR_BLASPHEMY_CARD.get())
                .requires(ModItems.TWILIGHT_GIANT_BLASPHEMY_CARD.get())
                .requires(ModItems.DEATH_BLASPHEMY_CARD.get())
                .requires(ModItems.ABYSS_BLASPHEMY_CARD.get())
                .requires(ModItems.WHEEL_OF_FORTUNE_BLASPHEMY_CARD.get())
                .unlockedBy("has_sun_blasphemy_card", has(ModItems.SUN_BLASPHEMY_CARD.get()))
                .save(recipeOutput);

        // ─── Blasphemy Slate — Right Half ───
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLASPHEMY_SLATE_RIGHT_HALF.get())
                .requires(ModItems.RED_PRIEST_BLASPHEMY_CARD.get())
                .requires(ModItems.DEMONESS_BLASPHEMY_CARD.get())
                .requires(ModItems.ERROR_BLASPHEMY_CARD.get())
                .requires(ModItems.DOOR_BLASPHEMY_CARD.get())
                .requires(ModItems.FOOL_BLASPHEMY_CARD.get())
                .requires(ModItems.MOTHER_BLASPHEMY_CARD.get())
                .requires(ModItems.BLACK_EMPEROR_BLASPHEMY_CARD.get())
                .requires(ModItems.DARKNESS_BLASPHEMY_CARD.get())
                .unlockedBy("has_red_priest_blasphemy_card", has(ModItems.RED_PRIEST_BLASPHEMY_CARD.get()))
                .save(recipeOutput);

        // ─── Blasphemy Slate (full) ──────────────────────────────────────────
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLASPHEMY_SLATE.get())
                .requires(ModItems.BLASPHEMY_SLATE_LEFT_HALF.get())
                .requires(ModItems.BLASPHEMY_SLATE_RIGHT_HALF.get())
                .unlockedBy("has_blasphemy_slate_left_half", has(ModItems.BLASPHEMY_SLATE_LEFT_HALF.get()))
                .save(recipeOutput);
    }
}