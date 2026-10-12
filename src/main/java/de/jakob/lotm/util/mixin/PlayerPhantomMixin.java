package de.jakob.lotm.util.mixin;

import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom.PhantomFormAttachment;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerPhantomMixin {

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;tick()V")
    )
    private void lotm$phantomNoClip(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (PhantomFormAttachment.isGhost(self)) {
            if (self.tickCount % 20 == 0) System.out.println("PHANTOM TICK-MIXIN client=" + self.level().isClientSide);
            self.noPhysics = true;
        }
    }
}