package de.jakob.lotm.entity.client.ability_entities.door_pathway.exile_doors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ExileDoorsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ExileDoorsRenderer extends EntityRenderer<ExileDoorsEntity> {

    private ExileDoorsModel model;

    public ExileDoorsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExileDoorsModel(context.bakeLayer(ExileDoorsModel.LAYER_LOCATION));
    }

    @Override
    public void render(ExileDoorsEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();

        poseStack.scale(1.5F, -1.5F, 1.5F);


        RenderType renderType = petrified ? RenderType.entityTranslucent(LOTMCraft.STONE_TEXTURE) : RenderType.entityTranslucent(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);


        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    protected int getBlockLightLevel(ExileDoorsEntity entity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ExileDoorsEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/exile_doors/exile_doors.png");
    }
}