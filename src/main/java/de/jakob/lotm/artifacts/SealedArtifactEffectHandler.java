package de.jakob.lotm.artifacts;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.data.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Handles the negative effects of sealed artifacts on players
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SealedArtifactEffectHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // Only process on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof SealedArtifactItem) {
            applyHandNegativeEffect(player, mainHand, true);
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof SealedArtifactItem) {
            applyHandNegativeEffect(player, offHand, false);
        }

        // check hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof SealedArtifactItem) {
                applyHotBarNegativeEffect(player, stack);
            }
        }

        // Check inventory for some passive effects (optional)
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SealedArtifactItem) {
                SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
                if (data != null) {
                    applyInventoryEffect(player, data);
                }
            }
        }
    }


    private static void applyHandNegativeEffect(Player player, ItemStack stack, boolean inMainHand) {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.negativeEffect() == null) {
            return;
        }

        // loop through every effect, if it's a hand only effect, apply it
        for (NegativeEffect effect : data.negativeEffect()) {
            if (NegativeEffect.handOnlyTick.contains(effect.getType())) {
                effect.apply(player, inMainHand, List.of(data.pathway()));
            }
        }
    }

    private static void applyHotBarNegativeEffect(Player player, ItemStack stack) {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null || data.negativeEffect() == null) {
            return;
        }

        // apply to hotbar only effects
        for (NegativeEffect effect : data.negativeEffect()) {
            if (NegativeEffect.hotBarOnlyTick.contains(effect.getType())) {
                effect.apply(player, true, List.of(data.pathway()));
            }
        }
    }

    private static void applyInventoryEffect(Player player, SealedArtifactData data) {
        // Apply a weaker version of the negative effect
        // For example, only apply every 5 seconds instead of constantly
//        if (player.tickCount % 100 != 0) {
//            return;
//        }

        for (NegativeEffect effect : data.negativeEffect()) {
            if (!NegativeEffect.useOnlyTick.contains(effect.getType()) && !NegativeEffect.handOnlyTick.contains(effect.getType()) && !NegativeEffect.hotBarOnlyTick.contains(effect.getType())) {
                effect.apply(player, true, List.of(data.pathway()));
            }
        }
    }
}