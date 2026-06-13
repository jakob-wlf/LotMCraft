package de.jakob.lotm.artifacts;

import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

        if (!(right.getItem() instanceof BeyonderCharacteristicItem characteristic)) {
            return;
        }

        if (right.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("VoidSummonTime")) {
            return;
        }

        if (!SealedArtifactHandler.isValidBaseItem(left.getItem())) {
            return;
        }

        ItemStack result = switch (SealedArtifactHandler.getBaseTypeName(left.getItem())) {
            case "gem" -> new ItemStack(ModItems.SEALED_ARTIFACT_GEM);
            case "star" -> new ItemStack(ModItems.SEALED_ARTIFACT_STAR);
            default -> new ItemStack(ModItems.SEALED_ARTIFACT);
        };

        if (left.getCount() > 1) {
            event.setCanceled(true);
            return;
        }

        // Store the base type for display
        String baseType = SealedArtifactHandler.getBaseTypeName(left.getItem());
        result.set(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, baseType);

        // Generate sealed artifact data
        //SealedArtifactData data = SealedArtifactHandler.createSealedArtifactData(characteristic, baseType);
        //result.set(ModDataComponents.SEALED_ARTIFACT_DATA, data);

        // Initialize selected ability index
        result.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, 0);

        result.set(ModDataComponents.SEALED_ARTIFACT_BASE_TYPE, baseType);

        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_SEQ, characteristic.getSequence());
        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_PATH, characteristic.getPathway());

        result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED, false);

        var level = event.getPlayer().level();
        if(!level.isClientSide)
            result.set(ModDataComponents.SEALED_ARTIFACT_GENERATED_FAILED,
                isFailed((ServerLevel) level, characteristic.getSequence()));

        event.setOutput(result);

        int sequence = characteristic.getSequence();
        int cost = (sequence <= 1) ? 65 :
                   (sequence == 2) ? 50 :
                   (sequence <= 4) ? 30 :
                   (sequence <= 7) ? 20 : 10;

        event.setCost(cost);

        event.setMaterialCost(1);
    }

    public static boolean isFailed(ServerLevel level, int seq){
        float additional = 0.0f;

        additional += seq < 7 && seq > 0 ? Math.min((7 - seq) * 0.15f, .5f) : 0;

        return level.random.nextFloat() < 0.2f + additional;
    }

}