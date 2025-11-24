package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
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
        basicItem(ModItems.MARIONETTE_CONTROLLER.get());
        basicItem(ModItems.SUBORDINATE_CONTROLLER.get());
        basicItem(ModItems.EXCAVATED_AREA_ITEM.get());
        basicItem(ModItems.GUIDING_BOOK.get());

        PotionItemHandler.ITEMS.getEntries().forEach(i -> {
            basicItem(i.get());
        });

        PotionRecipeItemHandler.ITEMS.getEntries().forEach(i -> {
            potionRecipeItem(i.get());
        });

        BeyonderCharacteristicItemHandler.ITEMS.getEntries().forEach(i -> {
            if(!(i.get() instanceof BeyonderCharacteristicItem characteristicItem)) {
                return;
            }
            characteristicItem(characteristicItem);
        });

        ModIngredients.ITEMS.getEntries().forEach(i -> {
            basicItem(i.get());
        });

        AbilityItemHandler.ITEMS.getEntries().forEach(i -> {
            itemWithCustomDisplay(i.get());
        });
        PassiveAbilityHandler.ITEMS.getEntries().forEach(i -> {
            itemWithCustomDisplay(i.get());
        });

        itemWithCustomDisplay(ModItems.FOOL_Card.get());
        basicItem(ModItems.MOD_ICON.get());
    }

    // Helper method for items that need custom display properties (auto-detects texture name)
    private void itemWithCustomDisplay(Item item) {
        String itemName = getItemName(item);
        itemWithCustomDisplay(item, itemName);
    }

    // Helper method for items that need custom display properties with custom texture name
    private void itemWithCustomDisplay(Item item, String textureName) {
        String itemName = getItemName(item);
        getBuilder(itemName)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + textureName))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .scale(0, 0, 0)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .scale(0, 0, 0)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .translation(1.25f, 4.25f, 0.75f)
                .scale(0.39f, 0.39f, 0.39f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .translation(1.25f, 4.25f, 0.75f)
                .scale(0.39f, 0.39f, 0.39f)
                .end()
                .end();
    }

    // Helper method to get the item's registry name
    private String getItemName(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return itemId.getPath();
    }

    private void potionRecipeItem(Item item) {
        String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
        withExistingParent(itemName, "item/generated")
                .texture("layer0", modLoc("item/potion_recipe")); // All items use the same texture
    }

    private void characteristicItem(BeyonderCharacteristicItem characteristicItem) {
        if(!BeyonderData.implementedPathways.contains(characteristicItem.getPathway())) {
            return;
        }
        String itemName = BuiltInRegistries.ITEM.getKey(characteristicItem).getPath();
        if(characteristicItem.getSequence() > 4) {
            withExistingParent(itemName, "item/generated")
                    .texture("layer0", modLoc("item/beyonder_characteristic_" + characteristicItem.getPathway()));
        }
        else {
            withExistingParent(itemName, "item/generated")
                    .texture("layer0", modLoc("item/beyonder_characteristic_high_" + characteristicItem.getPathway()));
        }
    }
}
