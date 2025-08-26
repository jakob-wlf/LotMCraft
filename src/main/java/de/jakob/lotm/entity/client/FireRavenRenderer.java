package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FireRavenRenderer extends MobRenderer<FireRavenEntity, FireRavenModel<FireRavenEntity>> {
    public FireRavenRenderer(EntityRendererProvider.Context context) {
        super(context, new FireRavenModel<>(context.bakeLayer(FireRavenModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(FireRavenEntity fireRavenEntity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_raven/fire_raven.png");
    }

    @Override
    public void render(FireRavenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // This translates the *visual model* upwards — try adjusting the Y value
        poseStack.translate(0.0D, -1.8D, 0.0D); // ← Try 0.3, 0.5, 0.6, etc. as needed

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
