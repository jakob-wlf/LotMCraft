package de.jakob.lotm.entity.client.spirits.ghost;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.translucent_wizard.SpiritTranslucentWizardAnimations;
import de.jakob.lotm.entity.custom.spirits.SpiritGhostEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritGhostModel<T extends SpiritGhostEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_ghost"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart l_arm;
	private final ModelPart r_arm;

	public SpiritGhostModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.l_arm = root.getChild("l_arm");
		this.r_arm = root.getChild("r_arm");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -2.75F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.5F))
		.texOffs(0, 0).addBox(-4.0F, -4.25F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -1.75F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -7.5F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.5F))
		.texOffs(38, 42).addBox(-3.0F, -6.5F, -3.5F, 6.0F, 14.0F, 7.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 10.5F, 0.0F));

		PartDefinition l_arm = partdefinition.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(45, 46).addBox(-1.37F, -6.25F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.5F))
		.texOffs(48, 22).addBox(-2.13F, -6.75F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(6.62F, 9.75F, 0.0F));

		PartDefinition r_arm = partdefinition.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(45, 46).addBox(-1.38F, -6.5F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.5F))
		.texOffs(40, 22).addBox(-2.12F, -7.0F, -2.0F, 4.0F, 15.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(-6.87F, 10.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(SpiritGhostEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		boolean isFlying = entity.isFlying();

		if (isFlying) {
			// Stop idle animation and start/continue walk animation
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, SpiritGhostAnimations.WALK, ageInTicks, 1.0F);
		} else {
			// Stop walk animation and start/continue idle animation
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, SpiritGhostAnimations.IDLE, ageInTicks, 1.0F);
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
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		l_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		r_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}