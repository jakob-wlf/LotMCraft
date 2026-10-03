package de.jakob.lotm.entity.client.spirits.asmann;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.spirit_bane.SpiritBaneModel;
import de.jakob.lotm.entity.custom.spirits.AsmannEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBaneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AsmannRenderer extends MobRenderer<AsmannEntity, AsmannModel<AsmannEntity>> {
    public AsmannRenderer(EntityRendererProvider.Context context) {
        super(context, new AsmannModel<>(context.bakeLayer(AsmannModel.LAYER_LOCATION)), .3f);
    }
    @Override
    public void render(AsmannEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1, 1, 1);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AsmannEntity spirit) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/asmann/asmann.png");
    }
}
