package de.jakob.lotm.entity.client.ability_entities.red_priest_pathway.fire_plate;
// Made with Blockbench 5.2.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FirePlateModel<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fire_armor_plate"), "main");
    private final ModelPart outer;
    private final ModelPart inner;

    public FirePlateModel(ModelPart root) {
        this.outer = root.getChild("outer");
        this.inner = root.getChild("inner");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition outer = partdefinition.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(1, 1).addBox(-6.0F, -2.0F, -1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 17).addBox(-5.0F, 2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = outer.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 11).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r2 = outer.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(2, 14).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition inner = partdefinition.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(15, 10).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 17).addBox(-2.0F, 0.0F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 23.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        outer.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        inner.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}