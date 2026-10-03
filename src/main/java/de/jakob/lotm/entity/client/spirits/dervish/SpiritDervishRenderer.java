package de.jakob.lotm.entity.client.spirits.dervish;

import com.mojang.blaze3d.vertex.PoseStack;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class SpiritDervishRenderer extends MobRenderer<SpiritDervishEntity, SpiritDervishModel<SpiritDervishEntity>> {
    public SpiritDervishRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritDervishModel<>(context.bakeLayer(SpiritDervishModel.LAYER_LOCATION)), .3f);
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritDervishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirits/spirit_dervish/spirit_dervish.png");
    }

    @Override
    public void render(SpiritDervishEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0, -.32, 0);

        Random random = new Random(entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits());
        float scale = 1f + random.nextFloat();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
