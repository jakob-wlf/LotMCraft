package de.jakob.lotm.entity.client.knowledge_rabbit;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.RabbitOfKnowledgeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RabbitOfKnowledgeModel<T extends RabbitOfKnowledgeEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "rabbit_of_knowledge"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart Haunches;
	private final ModelPart Haunch_left;
	private final ModelPart foot_left;
	private final ModelPart Haunch_right;
	private final ModelPart foot_right;
	private final ModelPart Waist;
	private final ModelPart body;
	private final ModelPart Head;
	private final ModelPart Hat;
	private final ModelPart Ear_left;
	private final ModelPart Ear_right;
	private final ModelPart Front_legs;
	private final ModelPart front_leg_left;
	private final ModelPart front_leg_right;
	private final ModelPart tail;

	public RabbitOfKnowledgeModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.Haunches = this.All.getChild("Haunches");
		this.Haunch_left = this.Haunches.getChild("Haunch_left");
		this.foot_left = this.Haunch_left.getChild("foot_left");
		this.Haunch_right = this.Haunches.getChild("Haunch_right");
		this.foot_right = this.Haunch_right.getChild("foot_right");
		this.Waist = this.All.getChild("Waist");
		this.body = this.Waist.getChild("body");
		this.Head = this.Waist.getChild("Head");
		this.Hat = this.Head.getChild("Hat");
		this.Ear_left = this.Head.getChild("Ear_left");
		this.Ear_right = this.Head.getChild("Ear_right");
		this.Front_legs = this.Waist.getChild("Front_legs");
		this.front_leg_left = this.Front_legs.getChild("front_leg_left");
		this.front_leg_right = this.Front_legs.getChild("front_leg_right");
		this.tail = this.Waist.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 11.5F, 1.1F));

		PartDefinition Haunches = All.addOrReplaceChild("Haunches", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 2.0F));

		PartDefinition Haunch_left = Haunches.addOrReplaceChild("Haunch_left", CubeListBuilder.create().texOffs(8, 41).addBox(-1.0F, -0.5F, -1.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

		PartDefinition foot_left = Haunch_left.addOrReplaceChild("foot_left", CubeListBuilder.create().texOffs(16, 34).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, 0.9F));

		PartDefinition Haunch_right = Haunches.addOrReplaceChild("Haunch_right", CubeListBuilder.create().texOffs(8, 41).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -0.5F, -0.1F, 0.0F, 0.5236F, 0.0F));

		PartDefinition foot_right = Haunch_right.addOrReplaceChild("foot_right", CubeListBuilder.create().texOffs(16, 34).mirror().addBox(-1.0F, 0.0F, -5.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 3.0F, 1.0F));

		PartDefinition Waist = All.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 1.4F, 1.3F));

		PartDefinition body = Waist.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 1.1F, -3.4F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -5.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6981F, 0.0F, 0.0F));

		PartDefinition Head = Waist.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(24, 24).addBox(-2.74F, -4.62F, -4.53F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-3.24F, -4.32F, -4.93F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.24F, -6.28F, -2.37F));

		PartDefinition Hat = Head.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(28, 16).addBox(-2.5F, -3.5F, -2.55F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(-3.5F, -0.5F, -3.45F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.24F, -4.82F, -1.98F));

		PartDefinition Ear_left = Head.addOrReplaceChild("Ear_left", CubeListBuilder.create(), PartPose.offset(1.76F, -5.12F, -1.33F));

		PartDefinition Ear_left_r1 = Ear_left.addOrReplaceChild("Ear_left_r1", CubeListBuilder.create().texOffs(22, 41).mirror().addBox(-1.5F, -6.0503F, -0.0054F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.6F, 0.5F, -0.7F, -0.5236F, 0.0F, 0.0F));

		PartDefinition Ear_right = Head.addOrReplaceChild("Ear_right", CubeListBuilder.create(), PartPose.offset(-2.44F, -5.12F, -1.43F));

		PartDefinition Ear_right_r1 = Ear_right.addOrReplaceChild("Ear_right_r1", CubeListBuilder.create().texOffs(22, 41).addBox(-1.1F, -6.1503F, -0.0054F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, 0.4866F, -0.65F, -0.5236F, 0.0F, 0.0F));

		PartDefinition Front_legs = Waist.addOrReplaceChild("Front_legs", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, -5.1F));

		PartDefinition front_leg_left = Front_legs.addOrReplaceChild("front_leg_left", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition left_front_leg_r1 = front_leg_left.addOrReplaceChild("left_front_leg_r1", CubeListBuilder.create().texOffs(36, 0).addBox(1.0F, -1.611F, -2.8281F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 0.7F, 1.6F, -0.1309F, 0.0F, 0.3927F));

		PartDefinition front_leg_right = Front_legs.addOrReplaceChild("front_leg_right", CubeListBuilder.create(), PartPose.offset(-2.9F, -0.311F, -0.1281F));

		PartDefinition right_front_leg_r1 = front_leg_right.addOrReplaceChild("right_front_leg_r1", CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, -0.3927F));

		PartDefinition tail = Waist.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.4F, 3.6F));

		PartDefinition tail_r1 = tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0572F, 0.8489F, 0.1745F, 0.0F, 0.0F));

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
	public void setupAnim(RabbitOfKnowledgeEntity entity, float limbSwing, float limbSwingAmount,
	                      float ageInTicks, float netHeadYaw, float headPitch) {

		// Lazy-init buffers once we know part count
		if (this.idlePose == null) {
			int partCount = (int) this.root().getAllParts().count();
			this.idlePose = new float[partCount * FLOATS_PER_PART];
			this.walkPose = new float[partCount * FLOATS_PER_PART];
		}

		boolean isWalking = limbSwingAmount > 0.01F;
		this.walkBlend = Mth.lerp(BLEND_SPEED, this.walkBlend, isWalking ? 1.0F : 0.0F);

		// Keep both states running so their internal timers don't reset
		if (!entity.IDLE_ANIMATION.isStarted()) entity.IDLE_ANIMATION.start((int) ageInTicks);
		if (!entity.HOP_ANIMATION.isStarted()) entity.HOP_ANIMATION.start((int) ageInTicks);

		// Sample idle into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.IDLE_ANIMATION, RabbitOfKnowledgeAnimations.Idle, ageInTicks, 1.0F);
		capturePoseInto(this.idlePose);

		// Sample walk into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.HOP_ANIMATION, RabbitOfKnowledgeAnimations.walk, ageInTicks, 1.0F);
		capturePoseInto(this.walkPose);

		// Write the lerped result
		applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}