package de.jakob.lotm.rendering.models.fool;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FoolMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "fool_mythical_creature"), "main");

	private final AnimationState idleAnimationState = new AnimationState();
	private final AnimationState walkAnimationState = new AnimationState();

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart left_arm;
	private final ModelPart right_arm;
	private final ModelPart tentacles;
	private final ModelPart rods;
	private final ModelPart tentacle1;
	private final ModelPart tentacle1_top;
	private final ModelPart tentacle1_bot;
	private final ModelPart tentacle2;
	private final ModelPart tentacle2_top;
	private final ModelPart tentacle2_bot;
	private final ModelPart tentacle3;
	private final ModelPart tentacle3_top;
	private final ModelPart tentacle3_bot;
	private final ModelPart tentacle4;
	private final ModelPart tentacle4_top;
	private final ModelPart tentacle4_bot;
	private final ModelPart rods2;
	private final ModelPart tentacle5;
	private final ModelPart tentacle5_top;
	private final ModelPart tentacle5_bot;
	private final ModelPart tentacle6;
	private final ModelPart tentacle6_top;
	private final ModelPart tentacle6_bot;
	private final ModelPart rods3;
	private final ModelPart tentacle7;
	private final ModelPart tentacle7_top;
	private final ModelPart tentacle7_bot;
	private final ModelPart tentacle8;
	private final ModelPart tentacle8_top;
	private final ModelPart tentacle8_bot;

	public FoolMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.left_arm = root.getChild("left_arm");
		this.right_arm = root.getChild("right_arm");
		this.tentacles = root.getChild("tentacles");
		this.rods = this.tentacles.getChild("rods");
		this.tentacle1 = this.rods.getChild("tentacle1");
		this.tentacle1_top = this.tentacle1.getChild("tentacle1_top");
		this.tentacle1_bot = this.tentacle1.getChild("tentacle1_bot");
		this.tentacle2 = this.rods.getChild("tentacle2");
		this.tentacle2_top = this.tentacle2.getChild("tentacle2_top");
		this.tentacle2_bot = this.tentacle2.getChild("tentacle2_bot");
		this.tentacle3 = this.rods.getChild("tentacle3");
		this.tentacle3_top = this.tentacle3.getChild("tentacle3_top");
		this.tentacle3_bot = this.tentacle3.getChild("tentacle3_bot");
		this.tentacle4 = this.rods.getChild("tentacle4");
		this.tentacle4_top = this.tentacle4.getChild("tentacle4_top");
		this.tentacle4_bot = this.tentacle4.getChild("tentacle4_bot");
		this.rods2 = this.tentacles.getChild("rods2");
		this.tentacle5 = this.rods2.getChild("tentacle5");
		this.tentacle5_top = this.tentacle5.getChild("tentacle5_top");
		this.tentacle5_bot = this.tentacle5.getChild("tentacle5_bot");
		this.tentacle6 = this.rods2.getChild("tentacle6");
		this.tentacle6_top = this.tentacle6.getChild("tentacle6_top");
		this.tentacle6_bot = this.tentacle6.getChild("tentacle6_bot");
		this.rods3 = this.tentacles.getChild("rods3");
		this.tentacle7 = this.rods3.getChild("tentacle7");
		this.tentacle7_top = this.tentacle7.getChild("tentacle7_top");
		this.tentacle7_bot = this.tentacle7.getChild("tentacle7_bot");
		this.tentacle8 = this.rods3.getChild("tentacle8");
		this.tentacle8_top = this.tentacle8.getChild("tentacle8_top");
		this.tentacle8_bot = this.tentacle8.getChild("tentacle8_bot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, -0.4868F, 0.1586F, 0.4549F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, -0.4868F, -0.1586F, -0.4549F));

		PartDefinition tentacles = partdefinition.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rods = tentacles.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3054F, 0.0F));

		PartDefinition tentacle1 = rods.addOrReplaceChild("tentacle1", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, 15.0F, 6.0F, 0.0436F, 0.5236F, 0.0F));

		PartDefinition tentacle1_top = tentacle1.addOrReplaceChild("tentacle1_top", CubeListBuilder.create().texOffs(1, 21).addBox(-1.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5155F, 0.0319F, -0.1768F));

		PartDefinition tentacle1_bot = tentacle1.addOrReplaceChild("tentacle1_bot", CubeListBuilder.create().texOffs(1, 21).addBox(2.15F, -9.5F, -0.8F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.5F, 2.6F, 0.9562F, -0.317F, -0.1062F));

		PartDefinition tentacle2 = rods.addOrReplaceChild("tentacle2", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 17.0F, 5.0F, -0.0507F, 0.3474F, -0.0357F));

		PartDefinition tentacle2_top = tentacle2.addOrReplaceChild("tentacle2_top", CubeListBuilder.create().texOffs(1, 21).addBox(-2.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition tentacle2_bot = tentacle2.addOrReplaceChild("tentacle2_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-2.2F, -6.7F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.0F, 3.0F, 0.8676F, -0.3464F, 0.2707F));

		PartDefinition tentacle3 = rods.addOrReplaceChild("tentacle3", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 5.0F));

		PartDefinition tentacle3_top = tentacle3.addOrReplaceChild("tentacle3_top", CubeListBuilder.create().texOffs(1, 21).addBox(-2.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition tentacle3_bot = tentacle3.addOrReplaceChild("tentacle3_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-2.2F, -6.7F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, 3.0F, 0.9091F, -0.1427F, 0.1008F));

		PartDefinition tentacle4 = rods.addOrReplaceChild("tentacle4", CubeListBuilder.create(), PartPose.offset(-1.0F, 15.0F, 8.0F));

		PartDefinition tentacle4_top = tentacle4.addOrReplaceChild("tentacle4_top", CubeListBuilder.create().texOffs(1, 21).addBox(-5.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.606F, -0.0594F, 0.1642F));

		PartDefinition tentacle4_bot = tentacle4.addOrReplaceChild("tentacle4_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-6.6F, -8.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -4.0F, 1.0F, 1.0662F, 0.3994F, -0.3446F));

		PartDefinition rods2 = tentacles.addOrReplaceChild("rods2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

		PartDefinition tentacle5 = rods2.addOrReplaceChild("tentacle5", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 5.0F));

		PartDefinition tentacle5_top = tentacle5.addOrReplaceChild("tentacle5_top", CubeListBuilder.create().texOffs(1, 21).addBox(-2.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition tentacle5_bot = tentacle5.addOrReplaceChild("tentacle5_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-2.2F, -6.7F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, 3.0F, 0.9091F, -0.1427F, 0.1008F));

		PartDefinition tentacle6 = rods2.addOrReplaceChild("tentacle6", CubeListBuilder.create(), PartPose.offset(-3.0F, 15.0F, 8.0F));

		PartDefinition tentacle6_top = tentacle6.addOrReplaceChild("tentacle6_top", CubeListBuilder.create().texOffs(1, 21).addBox(-3.0F, -14.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.606F, -0.0594F, 0.1642F));

		PartDefinition tentacle6_bot = tentacle6.addOrReplaceChild("tentacle6_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-6.6F, -8.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -4.0F, 1.0F, 1.0662F, 0.3994F, -0.3446F));

		PartDefinition rods3 = tentacles.addOrReplaceChild("rods3", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -2.0F, 0.0F, -0.3927F, -0.1309F, 0.0F));

		PartDefinition tentacle7 = rods3.addOrReplaceChild("tentacle7", CubeListBuilder.create(), PartPose.offset(-1.0F, 15.0F, 8.0F));

		PartDefinition tentacle7_top = tentacle7.addOrReplaceChild("tentacle7_top", CubeListBuilder.create().texOffs(1, 21).addBox(-5.0F, -11.0F, -1.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.606F, -0.0594F, 0.1642F));

		PartDefinition tentacle7_bot = tentacle7.addOrReplaceChild("tentacle7_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-6.6F, -8.0F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -4.0F, 1.0F, 1.0662F, 0.3994F, -0.3446F));

		PartDefinition tentacle8 = rods3.addOrReplaceChild("tentacle8", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 5.0F));

		PartDefinition tentacle8_top = tentacle8.addOrReplaceChild("tentacle8_top", CubeListBuilder.create().texOffs(1, 21).addBox(-2.0F, -11.0F, -1.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition tentacle8_bot = tentacle8.addOrReplaceChild("tentacle8_bot", CubeListBuilder.create().texOffs(1, 21).addBox(-2.2F, -6.7F, -1.0F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, 3.0F, 0.9091F, -0.1427F, 0.1008F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private float walkBlend = 0.0F;
	private static final float BLEND_SPEED = 0.1F;

	// Pre-allocate to avoid per-frame garbage
	private float[] idlePose;
	private float[] walkPose;
	private static final int FLOATS_PER_PART = 6; // x, y, z, xRot, yRot, zRot

	private void capturePoseInto(float[] buffer) {
		int i = 0;
		for (var part : (Iterable<ModelPart>) this.root().getAllParts()::iterator) {
			buffer[i++] = part.x;    buffer[i++] = part.y;    buffer[i++] = part.z;
			buffer[i++] = part.xRot; buffer[i++] = part.yRot; buffer[i++] = part.zRot;
		}
	}

	private void applyBlendedPose(float[] from, float[] to, float blend) {
		int i = 0;
		for (var part : (Iterable<ModelPart>) this.root().getAllParts()::iterator) {
			part.x    = Mth.lerp(blend, from[i], to[i]); i++;
			part.y    = Mth.lerp(blend, from[i], to[i]); i++;
			part.z    = Mth.lerp(blend, from[i], to[i]); i++;
			part.xRot = Mth.lerp(blend, from[i], to[i]); i++;
			part.yRot = Mth.lerp(blend, from[i], to[i]); i++;
			part.zRot = Mth.lerp(blend, from[i], to[i]); i++;
		}
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {

		// Lazy-init buffers once we know part count
		if (this.idlePose == null) {
			int partCount = (int) this.root().getAllParts().count();
			this.idlePose = new float[partCount * FLOATS_PER_PART];
			this.walkPose = new float[partCount * FLOATS_PER_PART];
		}

		if (entity instanceof LivingEntity living) {
			boolean isWalking = limbSwingAmount > 0.01F;
			this.walkBlend = Mth.lerp(BLEND_SPEED, this.walkBlend, isWalking ? 1.0F : 0.0F);

			// Keep both states running so their internal timers don't reset
			if (!this.idleAnimationState.isStarted()) this.idleAnimationState.start((int) ageInTicks);
			if (!this.walkAnimationState.isStarted()) this.walkAnimationState.start((int) ageInTicks);

			// Sample idle into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.idleAnimationState, FoolMythicalCreatureAnimations.IDLE, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, FoolMythicalCreatureAnimations.WALK, ageInTicks, 1.0F);
			capturePoseInto(this.walkPose);

			// Write the lerped result
			applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		tentacles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}