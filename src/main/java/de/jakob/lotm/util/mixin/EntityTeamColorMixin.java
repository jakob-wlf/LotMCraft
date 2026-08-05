package de.jakob.lotm.util.mixin;

import de.jakob.lotm.rendering.AbsolutePerceptionOutlineColors;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityTeamColorMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void lotmcraft$useAbsolutePerceptionColor(CallbackInfoReturnable<Integer> callback) {
        Integer color = AbsolutePerceptionOutlineColors.get(((Entity) (Object) this).getId());
        if (color != null) {
            callback.setReturnValue(color);
        }
    }
}