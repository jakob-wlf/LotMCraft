package de.jakob.lotm.util.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class MixinIngredient {
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void checkPersistentData(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty()) {
            CustomData Data = stack.get(DataComponents.CUSTOM_DATA);

            if (Data != null) {
                if (Data.contains("VoidSummonTime")) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}