package de.jakob.lotm.util.mixin;

import de.jakob.lotm.beyonders.abilities.twilight_giant.MercuryArmoryAbility;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MercuryPushMixin {

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void lotm$skipMercuryPush(Entity other, CallbackInfo ci) {
        if (MercuryArmoryAbility.ignoresCollision((Entity) (Object) this, other)) ci.cancel();
    }
}
