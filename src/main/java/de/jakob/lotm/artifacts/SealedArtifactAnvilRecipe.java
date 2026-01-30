package de.jakob.lotm.artifacts;

import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Handles anvil recipes for creating sealed artifacts
 */
@EventBusSubscriber
public class SealedArtifactAnvilRecipe {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // Check if right slot has a beyonder characteristic
        if (!(right.getItem() instanceof BeyonderCharacteristicItem characteristic)) {
            return;
        }

        // Check if left slot has a valid base item
        if (!SealedArtifactHandler.isValidBaseItem(left.getItem())) {
            return;
        }

        // Create the sealed artifact
        ItemStack result = switch (SealedArtifactHandler.getBaseTypeName(left.getItem())) {
            case "bell" -> new ItemStack(ModItems.SEALED_ARTIFACT_BELL);
            case "chain" -> new ItemStack(ModItems.SEALED_ARTIFACT_CHAIN);
            case "gem" -> new ItemStack(ModItems.SEALED_ARTIFACT_GEM);
            case "star" -> new ItemStack(ModItems.SEALED_ARTIFACT_STAR);
            default -> new ItemStack(ModItems.SEALED_ARTIFACT);
        };

        if (left.getCount() > 1) {
            event.setCanceled(true); // Prevent the recipe from working
            return;
        }

        // Generate sealed artifact data
        SealedArtifactData data = SealedArtifactHandler.createSealedArtifactData(characteristic);
        result.set(ModDataComponents.SEALED_ARTIFACT_DATA, data);
        
        // Store the base type for display
        String baseType = SealedArtifactHandler.getBaseTypeName(left.getItem());
        result.set(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, baseType);
        
        // Initialize selected ability index
        result.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);

        // Set the result
        event.setOutput(result);
        
        // Set the cost (XP levels)
        int sequence = characteristic.getSequence();
        int cost = 10 - sequence; // Higher cost for lower sequence (stronger artifacts)
        event.setCost(Math.max(1, cost));

        // Material cost is handled automatically by the anvil
        event.setMaterialCost(1);
    }
}