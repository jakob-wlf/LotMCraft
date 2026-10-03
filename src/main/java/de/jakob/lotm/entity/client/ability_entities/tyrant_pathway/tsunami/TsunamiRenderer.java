package de.jakob.lotm.entity.client.ability_entities.tyrant_pathway.tsunami;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.TsunamiEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class TsunamiRenderer extends EntityRenderer<TsunamiEntity> {

    public static final float scale = 3.75F;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/tsunami/tsunami.png");
    private final TsunamiModel<TsunamiEntity> model;

    public TsunamiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TsunamiModel<>(context.bakeLayer(TsunamiModel.LAYER_LOCATION));
    }

    @Override
    public void render(TsunamiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");
        boolean frozen = entity.getTags().contains("frozen");

        poseStack.pushPose();

        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        Vec3 direction = entity.getDirectionFacing();
        if (direction.lengthSqr() > 0) {
            float yaw = (float) Math.atan2(-direction.x, direction.z) + (float) (Math.PI / 2);
            poseStack.mulPose(Axis.YP.rotation(yaw));
        }

        poseStack.translate(0, -3.0f * scale, 0);

        int color = petrified ? 0xFF808080 : 0xFFFFFFFF;
        RenderType renderType =
                petrified ? this.model.renderType(LOTMCraft.STONE_TEXTURE) :
                frozen    ? this.model.renderType(LOTMCraft.ICE_TEXTURE) :
                            this.model.renderType(this.getTextureLocation(entity));

        var vertexConsumer = buffer.getBuffer(renderType);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);


        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(TsunamiEntity entity, BlockPos pos) { return 15; }

    @Override
    protected int getSkyLightLevel(TsunamiEntity entity, BlockPos pos) { return 15; }

    @Override
    public ResourceLocation getTextureLocation(TsunamiEntity entity) { return TEXTURE; }
}