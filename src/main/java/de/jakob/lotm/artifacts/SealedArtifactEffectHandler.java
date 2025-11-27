package de.jakob.lotm.artifacts;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.data.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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
            applyNegativeEffect(player, mainHand, true);
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof SealedArtifactItem) {
            applyNegativeEffect(player, offHand, false);
        }

        // Check inventory for some passive effects (optional)
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SealedArtifactItem) {
                SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
                if (data != null) {
                    // Apply weaker version of effect if in inventory but not held
                    // You can customize this behavior
                    if (!stack.equals(mainHand) && !stack.equals(offHand)) {
                        applyInventoryEffect(player, data);
                    }
                }
            }
        }
    }

    private static void applyNegativeEffect(Player player, ItemStack stack, boolean inMainHand) {
        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
        if (data == null) {
            return;
        }

        NegativeEffect effect = data.negativeEffect();
        if (effect != null) {
            effect.apply(player, inMainHand);
        }
    }

    private static void applyInventoryEffect(Player player, SealedArtifactData data) {
        // Apply a weaker version of the negative effect
        // For example, only apply every 5 seconds instead of constantly
        if (player.tickCount % 100 != 0) {
            return;
        }

        NegativeEffect effect = data.negativeEffect();
        if (effect != null && effect.getType() == NegativeEffect.NegativeEffectType.HEARING_WHISPERS) {
            // Some effects like whispers can still occur even when in inventory
            effect.apply(player, false);
        }
    }
}