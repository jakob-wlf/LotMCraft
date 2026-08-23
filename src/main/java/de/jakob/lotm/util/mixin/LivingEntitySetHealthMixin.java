package de.jakob.lotm.util.mixin;

import de.jakob.lotm.attachments.EndpointComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Endpoint blocks every heal source, including direct setHealth() calls that bypass
 * LivingHealEvent entirely (self-heals, revival abilities, etc.). Only increases are
 * blocked so damage and death (setHealth(0)) still go through untouched.
 *
 * Checks EndpointComponent (an attachment) rather than the cosmetic MobEffect, since
 * the effect can be stripped by milk/effect-clearing but the attachment cannot.
 */
@Mixin(LivingEntity.class)
public class LivingEntitySetHealthMixin {

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void lotmcraft$blockEndpointHealing(float health, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        EndpointComponent endpointComponent = self.getData(ModAttachments.ENDPOINT_COMPONENT);
        if (health > self.getHealth() && endpointComponent.isActive()) {
            ci.cancel();
        }
    }
}
