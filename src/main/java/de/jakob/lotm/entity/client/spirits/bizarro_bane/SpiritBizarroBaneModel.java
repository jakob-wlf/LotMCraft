package de.jakob.lotm.entity.client.spirits.bizarro_bane;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.client.spirits.blue_wizard.SpiritBlueWizardAnimations;
import de.jakob.lotm.entity.custom.spirits.SpiritBizarroBaneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritBizarroBaneModel<T extends SpiritBizarroBaneEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "bizarro_bane"), "main");
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart extra_eyes;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart leg;

	public SpiritBizarroBaneModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.extra_eyes = root.getChild("extra_eyes");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.leg = root.getChild("leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 10).addBox(-4.0F, -12.375F, -2.0F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(69, 36).addBox(-5.0F, -12.375F, -3.0F, 10.0F, 16.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(36, 12).addBox(-3.0F, -3.375F, -1.0F, 6.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(72, 38).addBox(-4.0F, -3.375F, -2.0F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.375F, 1.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(75, 13).addBox(-7.0F, -5.5F, -4.0F, 14.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.5F, 0.0F));

		PartDefinition extra_eyes = partdefinition.addOrReplaceChild("extra_eyes", CubeListBuilder.create().texOffs(12, 40).addBox(-10.0F, -10.3333F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F))
		.texOffs(43, 40).addBox(-11.0F, -11.3333F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F))
		.texOffs(12, 40).addBox(5.0F, -6.3333F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F))
		.texOffs(43, 40).addBox(4.0F, -7.3333F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F))
		.texOffs(12, 40).addBox(-7.0F, 4.6667F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F))
		.texOffs(46, 42).addBox(-8.0F, 3.6667F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F)), PartPose.offset(-1.0F, -6.6667F, -1.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 18.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(49, 36).addBox(-2.0F, -9.0F, -2.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0836F, 8.9958F, 0.9686F, -0.0005F, -0.0022F, 0.4363F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -9.0F, -1.0F, 2.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(49, 36).mirror().addBox(-2.0F, -9.0F, -2.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.271F, 9.841F, 0.973F, -0.0005F, 0.0022F, -0.4363F));

		PartDefinition leg = partdefinition.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, -7.5F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(46, 38).mirror().addBox(-2.0F, -8.5F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 15.6291F, 1.524F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(SpiritBizarroBaneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		boolean isWalking = limbSwingAmount > 0.01F;

		if (isWalking) {
			// Stop idle animation and start/continue walk animation
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, SpiritBizarroBaneAnimations.WALK, ageInTicks, 1.0F);
		} else {
			// Stop walk animation and start/continue idle animation
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, SpiritBizarroBaneAnimations.IDLE, ageInTicks, 1.0F);
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
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		extra_eyes.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}