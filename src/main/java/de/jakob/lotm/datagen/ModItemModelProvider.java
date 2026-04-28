package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
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
        basicItem(ModItems.SUN_ITEM.get());
        basicItem(ModItems.MOON_ITEM.get());
        basicItem(ModItems.GUIDING_BOOK.get());
        basicItem(ModItems.CRYSTAL_BALL.get());
        basicItem(ModItems.CANE.get());

        tintableItem(ModItems.SEALED_ARTIFACT.get());
        tintableItem(ModItems.SEALED_ARTIFACT_BELL.get());
        tintableItem(ModItems.SEALED_ARTIFACT_CHAIN.get());
        tintableItem(ModItems.SEALED_ARTIFACT_GEM.get());
        tintableItem(ModItems.SEALED_ARTIFACT_STAR.get());
        basicItem(ModItems.BLOOD.get());

        uniqueness(ModItems.RED_PRIEST_UNIQUENESS.get());
        uniqueness(ModItems.FOOL_UNIQUENESS.get());
        uniqueness(ModItems.TYRANT_UNIQUENESS.get());
        uniqueness(ModItems.SUN_UNIQUENESS.get());
        uniqueness(ModItems.ERROR_UNIQUENESS.get());
        uniqueness(ModItems.DARKNESS_UNIQUENESS.get());
        uniqueness(ModItems.DOOR_UNIQUENESS.get());
        uniqueness(ModItems.VISIONARY_UNIQUENESS.get());
        uniqueness(ModItems.WHEEL_OF_FORTUNE_UNIQUENESS.get());
        uniqueness(ModItems.ABYSS_UNIQUENESS.get());
        uniqueness(ModItems.MOTHER_UNIQUENESS.get());
        uniqueness(ModItems.DEMONESS_UNIQUENESS.get());
        uniqueness(ModItems.JUSTICIAR_UNIQUENESS.get());


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

        PassiveAbilityHandler.ITEMS.getEntries().forEach(i -> {
            itemWithCustomDisplay(i.get());
        });

        itemWithCustomDisplay(ModItems.FOOL_Card.get());
        basicItem(ModItems.MOD_ICON.get());
    }

    private void uniqueness(Item item) {
        String itemName = getItemName(item);
        getBuilder(itemName)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + itemName))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 0)
                .translation(0, 8, 2)
                .scale(1f, 1f, 1f)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(0, 90, 0)
                .translation(0, 8, 2)
                .scale(1f, 1f, 1f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 0)
                .translation(1.13f, 4f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 90, 0)
                .translation(1.13f, 4f, 1.13f)
                .scale(0.68f, 0.68f, 0.68f)
                .end()
                .transform(ItemDisplayContext.GUI)
                .rotation(0, 0, 0)
                .translation(0, 0, 0)
                .scale(1.25f, 1.25f, 1.25f)
                .end()
                .transform(ItemDisplayContext.GROUND)
                .translation(0, 5, 0)
                .scale(2.5f, 2.5f, 2.5f)
                .end()
                .transform(ItemDisplayContext.FIXED)
                .scale(1, 1, 1)
                .end()
                .end();
    }

    // Helper method for items that need custom display properties (auto-detects texture name)
    private void itemWithCustomDisplay(Item item) {
        String itemName = getItemName(item);
        itemWithCustomDisplay(item, itemName);
    }

    private void tintableItem(Item item) {
        String itemName = getItemName(item);
        getBuilder(itemName)
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/" + itemName))
                .texture("layer1", modLoc("item/" + itemName + "_tint"));
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
