package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientLeftClickHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(InputEvent.InteractionKeyMappingTriggered event) {
        // This event fires when left-click happens in air (no block targeted)
        if (event.isAttack()) { // isAttack() returns true for left-click
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            
            if (player != null && event.getHand() == InteractionHand.MAIN_HAND) {
                ItemStack stack = player.getMainHandItem();
                
                if (stack.getItem() instanceof SelectableAbilityItem selectableItem) {
                    if (selectableItem.getAbilityNamesCopy().length > 0) {
                        selectableItem.handleLeftClickInAir(player, stack);
                        event.setCanceled(true); // Prevent the left-click from doing anything else
                        event.setSwingHand(false); // Don't swing arm
                    }
                }
            }
        }
    }
}