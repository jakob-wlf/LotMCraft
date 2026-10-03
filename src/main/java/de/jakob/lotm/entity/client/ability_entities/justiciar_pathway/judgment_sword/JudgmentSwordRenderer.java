package de.jakob.lotm.entity.client.ability_entities.justiciar_pathway.judgment_sword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.JudgmentSwordEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class JudgmentSwordRenderer extends EntityRenderer<JudgmentSwordEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/entity/judgment_sword/judgment_sword.png");

    private final JudgmentSwordModel<JudgmentSwordEntity> model;

    public JudgmentSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new JudgmentSwordModel<>(context.bakeLayer(JudgmentSwordModel.LAYER_LOCATION));
    }

    @Override
    public void render(JudgmentSwordEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Random random = new Random(entity.getId());

        poseStack.scale(2, -2, 2);
        poseStack.translate(0, -1, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextInt(360)));

        var vertexConsumer = buffer.getBuffer(this.model.renderType(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(JudgmentSwordEntity entity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(JudgmentSwordEntity entity) {
        return TEXTURE;
    }
}