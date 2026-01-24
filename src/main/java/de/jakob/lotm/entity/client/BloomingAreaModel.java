package de.jakob.lotm.entity.client;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BloomingAreaModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "blooming_area"), "main");
	private final ModelPart rune_1;
	private final ModelPart rune_8;
	private final ModelPart rune_2;
	private final ModelPart rune_7;
	private final ModelPart rune_3;
	private final ModelPart rune_6;
	private final ModelPart rune_4;
	private final ModelPart rune_5;

	public BloomingAreaModel(ModelPart root) {
		this.rune_1 = root.getChild("rune_1");
		this.rune_8 = root.getChild("rune_8");
		this.rune_2 = root.getChild("rune_2");
		this.rune_7 = root.getChild("rune_7");
		this.rune_3 = root.getChild("rune_3");
		this.rune_6 = root.getChild("rune_6");
		this.rune_4 = root.getChild("rune_4");
		this.rune_5 = root.getChild("rune_5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rune_1 = partdefinition.addOrReplaceChild("rune_1", CubeListBuilder.create().texOffs(8, 9).addBox(-1.0F, -3.425F, -0.6F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.425F, -9.4F, 0.244F, 0.4994F, 0.1577F));

		PartDefinition cube_r1 = rune_1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition cube_r2 = rune_1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 18).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition rune_8 = partdefinition.addOrReplaceChild("rune_8", CubeListBuilder.create().texOffs(8, 9).addBox(-1.0F, -3.425F, -0.6F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, 16.425F, -0.4F, 0.244F, 0.4994F, 0.1577F));

		PartDefinition cube_r3 = rune_8.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition cube_r4 = rune_8.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(8, 18).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition rune_2 = partdefinition.addOrReplaceChild("rune_2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 23.0F, 5.0F, -0.2571F, -0.9821F, 0.3218F));

		PartDefinition cube_r5 = rune_2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(12, 12).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r6 = rune_2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(12, 6).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.375F, 0.3F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r7 = rune_2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r8 = rune_2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_7 = partdefinition.addOrReplaceChild("rune_7", CubeListBuilder.create(), PartPose.offsetAndRotation(15.0F, 28.0F, -7.0F, 2.309F, -0.9829F, -2.0549F));

		PartDefinition cube_r9 = rune_7.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(12, 12).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r10 = rune_7.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(12, 6).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.375F, 0.3F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r11 = rune_7.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r12 = rune_7.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_3 = partdefinition.addOrReplaceChild("rune_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, 18.0F, -1.0F, -0.1343F, -0.1119F, 0.8802F));

		PartDefinition cube_r13 = rune_3.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 16).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r14 = rune_3.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r15 = rune_3.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.4F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r16 = rune_3.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_6 = partdefinition.addOrReplaceChild("rune_6", CubeListBuilder.create(), PartPose.offsetAndRotation(6.0F, 11.0F, -19.0F, -0.9477F, 0.2315F, 0.4256F));

		PartDefinition cube_r17 = rune_6.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(4, 16).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r18 = rune_6.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r19 = rune_6.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.4F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r20 = rune_6.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_4 = partdefinition.addOrReplaceChild("rune_4", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0F, -8.0F, -1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 24.0F, -2.0F, 0.1745F, -0.3491F, 0.0F));

		PartDefinition cube_r21 = rune_4.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(16, 11).addBox(-1.0F, -4.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, 0.7418F, 0.0F, 0.0F));

		PartDefinition cube_r22 = rune_4.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(16, 6).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

		PartDefinition rune_5 = partdefinition.addOrReplaceChild("rune_5", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0F, -8.0F, -1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 17.0F, 13.0F, 0.2525F, 0.1726F, -0.1487F));

		PartDefinition cube_r23 = rune_5.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(16, 11).addBox(-1.0F, -4.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, 0.7418F, 0.0F, 0.0F));

		PartDefinition cube_r24 = rune_5.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(16, 6).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		rune_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}