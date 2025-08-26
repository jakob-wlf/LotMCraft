package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.FlamingSpearProjectileEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class FlamingSpearProjectileModel extends EntityModel<FlamingSpearProjectileEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "flaming_spear"), "main");
    private final ModelPart flamingSpear;

    public FlamingSpearProjectileModel(ModelPart root) {
        super();
        this.flamingSpear = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(-42, -41).addBox(-1.5F, -2.25F, 1.5F, 3.0F, 3.0F, 85.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -7.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(-1, 1).addBox(-1.875F, -3.0F, 4.875F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-2, 0).addBox(-2.925F, -3.0F, 1.875F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.525F, -1.75F, -4.875F, 0.0F, 0.0F, 1.5708F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone7 = bone6.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.475F, -3.0F, -1.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-3, -1).addBox(-1.4F, -3.0F, -3.925F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-2, 0).addBox(-0.925F, -3.0F, -5.65F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 0).addBox(-0.375F, -3.0F, -7.65F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, -1).addBox(0.1F, -2.5F, -10.65F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(3, 1).addBox(0.6F, -2.5F, -12.55F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -0.125F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(-1, 1).addBox(-1.875F, -3.0F, 4.875F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-2, 0).addBox(-2.925F, -3.0F, 1.875F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.075F, -2.05F, -4.875F, 0.0F, 0.0F, -3.1416F));

        PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone4 = bone3.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(-2, 0).addBox(-2.475F, -3.0F, -1.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-3, -1).addBox(-1.4F, -3.0F, -3.925F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-2, 0).addBox(-0.925F, -3.0F, -5.65F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 0).addBox(-0.375F, -3.0F, -7.65F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, -1).addBox(0.1F, -2.5F, -10.65F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(3, 1).addBox(0.6F, -2.5F, -12.55F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -0.125F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(FlamingSpearProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        flamingSpear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
