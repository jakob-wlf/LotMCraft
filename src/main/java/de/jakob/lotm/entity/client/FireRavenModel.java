package de.jakob.lotm.entity.client;// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class FireRavenModel<T extends FireRavenEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fire_raven"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart foot1;
	private final ModelPart foot2;

	public FireRavenModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.wing1 = this.root.getChild("wing1");
		this.wing2 = this.root.getChild("wing2");
		this.head = this.root.getChild("head");
		this.tail = this.root.getChild("tail");
		this.foot1 = this.root.getChild("foot1");
		this.foot2 = this.root.getChild("foot2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.1F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -4.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition wing1 = root.addOrReplaceChild("wing1", CubeListBuilder.create(), PartPose.offsetAndRotation(4.4F, 0.6F, 0.5F, 0.1289F, 0.0227F, -0.1731F));

		PartDefinition cube_r1 = wing1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 0).addBox(0.0F, -7.0F, -4.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 11).addBox(-1.0F, -4.0F, -4.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(24, 14).addBox(0.0F, -4.0F, 1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition wing2 = root.addOrReplaceChild("wing2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.4F, 0.6F, 0.5F, 0.1289F, -0.0227F, 0.1731F));

		PartDefinition cube_r2 = wing2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 0).addBox(0.0F, -7.0F, -4.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 11).addBox(0.0F, -4.0F, -4.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(24, 14).addBox(0.0F, -4.0F, 1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -4.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(24, 11).addBox(-1.0F, -4.0F, -3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 26).addBox(0.0F, -3.0F, -3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 20).addBox(0.0F, -3.0F, -5.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -3.0F, -1.4F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 26).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, -4.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(1.0F, 1.7F, 4.1F));

		PartDefinition cube_r4 = tail.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, -2.0F, 3.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(22, 7).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.0F, 0.0F));

		PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(12, 19).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, 2.2F, 0.7243F, 0.0F, 0.0F));

		PartDefinition foot1 = root.addOrReplaceChild("foot1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, 2.7F, -0.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r6 = foot1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(3, 36).addBox(1.0F, -2.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, -0.9599F, 1.5708F));

		PartDefinition cube_r7 = foot1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(3, 36).addBox(1.0F, -1.0F, 0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.4F, 0.3F, 1.5708F, -0.6109F, -1.5708F));

		PartDefinition foot2 = root.addOrReplaceChild("foot2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.7F, 2.7F, -0.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r8 = foot2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(3, 36).addBox(1.0F, -2.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, -0.9599F, 1.5708F));

		PartDefinition cube_r9 = foot2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(3, 36).addBox(1.0F, -1.0F, 0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.4F, 0.3F, 1.5708F, -0.6109F, -1.5708F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}


	@Override
	public void setupAnim(FireRavenEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		if (entity.isFlying()) {
			this.animate(entity.FLY_ANIMATION, FireRavenAnimations.FLY, ageInTicks, 1f);

			float flyingBob = Mth.sin(ageInTicks * 0.1f) * 0.05f;
			this.body.y += flyingBob;
		} else {
			this.animate(entity.IDLE_ANIMATION, FireRavenAnimations.IDLE, ageInTicks, 1f);

			if (limbSwingAmount > 0.01f) {
				this.animateWalk(FireRavenAnimations.FLY, limbSwing, limbSwingAmount, 2f, 2.5f);
			}
		}
	}

	private void applyHeadRotation(float headYaw, float headPitch) {
		headYaw = Mth.clamp(headYaw, -30f, 30f);
		headPitch = Mth.clamp(headPitch, -25f, 45);

		this.head.yRot = headYaw * ((float)Math.PI / 180f);
		this.head.xRot = headPitch *  ((float)Math.PI / 180f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		wing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		wing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}