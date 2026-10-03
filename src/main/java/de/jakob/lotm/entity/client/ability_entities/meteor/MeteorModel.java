package de.jakob.lotm.entity.client.ability_entities.meteor;// Made with Blockbench 4.12.6
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

public class MeteorModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "meteor"), "main");
	private final ModelPart meteor_body;
	private final ModelPart meteor_tail;
	private final ModelPart flames;
	private final ModelPart out_smoke;
	private final ModelPart in_smoke;

	public MeteorModel(ModelPart root) {
		this.meteor_body = root.getChild("meteor_body");
		this.meteor_tail = root.getChild("meteor_tail");
		this.flames = root.getChild("flames");
		this.out_smoke = root.getChild("out_smoke");
		this.in_smoke = root.getChild("in_smoke");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition meteor_body = partdefinition.addOrReplaceChild("meteor_body", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 8).addBox(-2.5F, -1.5F, -0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 18.0F, -1.0F));

		PartDefinition meteor_tail = partdefinition.addOrReplaceChild("meteor_tail", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.1568F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.1568F, 0.0371F));

		PartDefinition flames = partdefinition.addOrReplaceChild("flames", CubeListBuilder.create(), PartPose.offset(1.0F, 18.0F, -1.0F));

		PartDefinition cube_r1 = flames.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.5F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 2.5F, 1.0F, 0.0F, -1.5708F, 0.4363F));

		PartDefinition cube_r2 = flames.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.5F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 2.5F, 1.0F, 0.0F, 1.5708F, -0.4363F));

		PartDefinition cube_r3 = flames.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.5F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.5F, 5.0F, -2.7053F, 0.0F, 3.1416F));

		PartDefinition cube_r4 = flames.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.1568F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.1568F, -2.9629F, 0.4363F, 0.0F, 0.0F));

		PartDefinition out_smoke = partdefinition.addOrReplaceChild("out_smoke", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r5 = out_smoke.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.5F, 3.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r6 = out_smoke.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(1, 8).addBox(-1.5F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.5F, 0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r7 = out_smoke.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.5F, -3.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r8 = out_smoke.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -4.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r9 = out_smoke.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, -3.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r10 = out_smoke.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(1, 8).addBox(-1.5F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r11 = out_smoke.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, 3.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition in_smoke = partdefinition.addOrReplaceChild("in_smoke", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r12 = in_smoke.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.5F, 3.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r13 = in_smoke.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(1, 8).addBox(-1.5F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.5F, 0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r14 = in_smoke.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -0.5F, -3.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r15 = in_smoke.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -4.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r16 = in_smoke.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, -3.0F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r17 = in_smoke.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(1, 8).addBox(-1.5F, -1.0F, 1.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, -0.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r18 = in_smoke.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(1, 8).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.5F, 3.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		meteor_body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		//meteor_tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		//flames.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		//out_smoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		//in_smoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}