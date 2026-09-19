package de.jakob.lotm.util.mixin;

import de.jakob.lotm.events.custom.ContainerCraftingEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ContainerCraftingListenerMixin {

    @Final
    @Shadow
    private Item item;



    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onTakeResultItem(Level level, Player player, int amount, CallbackInfo ci) {
        if (!player.level().isClientSide()) {
            ContainerCraftingEvent event = new ContainerCraftingEvent(player, player.containerMenu, item);
            NeoForge.EVENT_BUS.post(event);


        }
    }
}

