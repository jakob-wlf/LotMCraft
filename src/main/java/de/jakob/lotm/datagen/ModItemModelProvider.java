package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LOTMCraft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.PAPER_FIGURINE_SUBSTITUTE.get());
        basicItem(ModItems.MIRROR.get());

        PotionItemHandler.ITEMS.getEntries().forEach(i -> {
            basicItem(i.get());
        });

        PotionRecipeItemHandler.ITEMS.getEntries().forEach(i -> {
            potionRecipeItem(i.get());
        });

        ModIngredients.ITEMS.getEntries().forEach(i -> {
            basicItem(i.get());
        });
    }

    private void potionRecipeItem(Item item) {
        String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
        withExistingParent(itemName, "item/generated")
                .texture("layer0", modLoc("item/potion_recipe")); // All items use the same texture
    }
}
