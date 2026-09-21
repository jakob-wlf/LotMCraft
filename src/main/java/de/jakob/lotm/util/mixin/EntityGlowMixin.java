package de.jakob.lotm.util.mixin;

import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void setAllyGlowColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            var allyComponent = localPlayer.getData(ModAttachments.ALLY_COMPONENT);
            if (allyComponent.isAlly(entity.getUUID())) {
                cir.setReturnValue(0x55FF55);
            }
        }
    }
}