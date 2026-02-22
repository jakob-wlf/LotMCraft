package de.jakob.lotm.util.shapeShifting.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void onRenderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                   float equippedProgress, float swingProgress, HumanoidArm side,
                                   CallbackInfo ci) {

        AbstractClientPlayer player = this.minecraft.player;
        if (player == null) return;

        TransformData data = (TransformData) player;
        String shapeKey = data.getCurrentShape();

        // skip for non transformed or player transformed
        if (shapeKey == null || shapeKey.startsWith("player:")) {
            return;
        }
        //  change hand texture for other humanoid entities (Not Implemented)

        // hide hand for any other "non humanoid" models
        ci.cancel();
    }
}