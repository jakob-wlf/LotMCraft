package de.jakob.lotm.util.shapeShifting.mixin;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderRenderer.class)
public class WanderingTraderRendererMixin {

    private static final ResourceLocation ANCIENT_TRADER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/wandering_trader/ancient_trader.png");

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void overrideAncientTraderTexture(WanderingTrader entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity.hasCustomName() && "Ancient Trader".equals(entity.getCustomName().getString())) {
            cir.setReturnValue(ANCIENT_TRADER_TEXTURE);
        }
    }
}
