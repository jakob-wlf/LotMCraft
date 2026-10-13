package de.jakob.lotm.entity.client.ability_entities.red_priest_pathway.fire_plate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.FirePlateEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import static de.jakob.lotm.util.data.PriestFlameVariants.*;

public class FirePlateRenderer extends EntityRenderer<FirePlateEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate.png");
    private final FirePlateModel<FirePlateEntity> model;

    public FirePlateRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FirePlateModel<>(context.bakeLayer(FirePlateModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(FirePlateEntity firePlateEntity) {
        return switch (firePlateEntity.getVariant()) {
            case DEFAULT -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate.png");
            case CRIMSON -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate7.png");
            case WHITISH_RED -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate6.png");
            case WHITE -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate5.png");
            case WHITISH_BLUE -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate4.png");
            case AZURE -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate3.png");
            case AZURE_VIOLENT -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate2.png");
            case VIOLENT -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate1.png");
            case DARK -> ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/fire_armor_plate/fire_armor_plate0.png");
        };
    }

    @Override
    public void render(FirePlateEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float rotation = (entity.tickCount + partialTicks) * 2.5f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(1.5f, -2.75f, 1.5f);
        poseStack.translate(0, -1, 0);

        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(
                buffer, this.model.renderType(this.getTextureLocation(entity)), false, false
        );
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

//    @Override
//    protected int getBlockLightLevel(WarBannerEntity entity, BlockPos pos) {
//        return 15;
//    }
//
//    @Override
//    protected int getSkyLightLevel(WarBannerEntity entity, BlockPos pos) {
//        return 15;
//    }

//    @Override
//    public ResourceLocation getTextureLocation(WarBannerEntity entity) {
//        return TEXTURE;
//    }
}