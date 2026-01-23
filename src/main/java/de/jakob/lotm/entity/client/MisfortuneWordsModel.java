package de.jakob.lotm.entity.client;// Made with Blockbench 5.0.7
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

public class MisfortuneWordsModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "misfortune_words"), "main");
	private final ModelPart rune_1;
	private final ModelPart rune_2;
	private final ModelPart rune_3;
	private final ModelPart rune_4;

	public MisfortuneWordsModel(ModelPart root) {
		this.rune_1 = root.getChild("rune_1");
		this.rune_2 = root.getChild("rune_2");
		this.rune_3 = root.getChild("rune_3");
		this.rune_4 = root.getChild("rune_4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rune_1 = partdefinition.addOrReplaceChild("rune_1", CubeListBuilder.create().texOffs(8, 9).addBox(-1.0F, -3.425F, -0.6F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.425F, -4.4F, 0.1745F, 0.2182F, 0.0F));

		PartDefinition cube_r1 = rune_1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition cube_r2 = rune_1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 18).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.0F));

		PartDefinition rune_2 = partdefinition.addOrReplaceChild("rune_2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 23.0F, 4.0F, 0.1614F, -1.3055F, -0.1671F));

		PartDefinition cube_r3 = rune_2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 12).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r4 = rune_2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 6).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.375F, 0.3F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r5 = rune_2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = rune_2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_3 = partdefinition.addOrReplaceChild("rune_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 20.0F, -1.0F, 0.0381F, -0.1704F, -0.2214F));

		PartDefinition cube_r7 = rune_3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(4, 16).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, -2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r8 = rune_3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.375F, 0.7F, 2.3562F, 0.0F, 0.0F));

		PartDefinition cube_r9 = rune_3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -5.0F, -1.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.4F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r10 = rune_3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -5.0F, 0.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition rune_4 = partdefinition.addOrReplaceChild("rune_4", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0F, -8.0F, -1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 24.0F, -2.0F, 0.0F, 0.2182F, 0.0F));

		PartDefinition cube_r11 = rune_4.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(16, 11).addBox(-1.0F, -4.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, 0.7418F, 0.0F, 0.0F));

		PartDefinition cube_r12 = rune_4.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(16, 6).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		rune_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		rune_4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}