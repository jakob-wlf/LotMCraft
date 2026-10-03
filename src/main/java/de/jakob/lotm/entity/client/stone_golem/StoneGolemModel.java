package de.jakob.lotm.entity.client.stone_golem;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.StoneGolemEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class StoneGolemModel<T extends StoneGolemEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "stone_golem"), "main");
	private final ModelPart root;
	private final ModelPart Golem;
	private final ModelPart leg_left;
	private final ModelPart foot_left;
	private final ModelPart leg_right;
	private final ModelPart foot_right;
	private final ModelPart waist;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart arm_right;
	private final ModelPart forearm_right;
	private final ModelPart fingers_right;
	private final ModelPart arm_left;
	private final ModelPart forearm_left;
	private final ModelPart fingers_left;

	public StoneGolemModel(ModelPart root) {
		this.root = root;
		this.Golem = root.getChild("Golem");
		this.leg_left = this.Golem.getChild("leg_left");
		this.foot_left = this.leg_left.getChild("foot_left");
		this.leg_right = this.Golem.getChild("leg_right");
		this.foot_right = this.leg_right.getChild("foot_right");
		this.waist = this.Golem.getChild("waist");
		this.body = this.waist.getChild("body");
		this.head = this.waist.getChild("head");
		this.arm_right = this.waist.getChild("arm_right");
		this.forearm_right = this.arm_right.getChild("forearm_right");
		this.fingers_right = this.forearm_right.getChild("fingers_right");
		this.arm_left = this.waist.getChild("arm_left");
		this.forearm_left = this.arm_left.getChild("forearm_left");
		this.fingers_left = this.forearm_left.getChild("fingers_left");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Golem = partdefinition.addOrReplaceChild("Golem", CubeListBuilder.create(), PartPose.offset(0.0F, -7.9333F, 0.0F));

		PartDefinition leg_left = Golem.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(0, 81).mirror().addBox(-1.5F, 0.0F, -3.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, 10.3333F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition left_leg_r1 = leg_left.addOrReplaceChild("left_leg_r1", CubeListBuilder.create().texOffs(0, 65).mirror().addBox(-2.5F, -2.0F, -2.0F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 9.8F, -2.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition foot_left = leg_left.addOrReplaceChild("foot_left", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 17.8F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition left_leg_r2 = foot_left.addOrReplaceChild("left_leg_r2", CubeListBuilder.create().texOffs(48, 0).addBox(-2.5F, 0.7576F, -4.5528F, 6.0F, 2.0F, 8.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition leg_right = Golem.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(0, 81).addBox(-4.5F, 0.0F, -3.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(-3.0F, 10.3333F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition right_leg_r1 = leg_right.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-3.5F, -2.0F, -2.0F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 9.8F, -2.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition foot_right = leg_right.addOrReplaceChild("foot_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 17.8F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition right_leg_r2 = foot_right.addOrReplaceChild("right_leg_r2", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-3.5F, 0.7576F, -4.5528F, 6.0F, 2.0F, 8.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition waist = Golem.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 9.3333F, 1.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition body = waist.addOrReplaceChild("body", CubeListBuilder.create().texOffs(59, 12).addBox(-4.5F, -21.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F))
		.texOffs(64, 26).mirror().addBox(-10.5F, -34.0F, -9.5F, 12.0F, 11.0F, 7.0F, new CubeDeformation(-2.0F)).mirror(false)
		.texOffs(64, 26).addBox(-1.6F, -34.0F, -9.5F, 12.0F, 11.0F, 7.0F, new CubeDeformation(-2.0F))
		.texOffs(0, 0).addBox(-9.0F, -33.0F, -6.0F, 18.0F, 12.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 23).addBox(-11.0F, -6.0F, -5.5F, 20.0F, 12.0F, 11.0F, new CubeDeformation(-3.0F)), PartPose.offsetAndRotation(0.0F, -23.6F, -4.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.8F, -43.4783F, 4.9022F, -0.6478F, 0.768F, -0.8282F));

		PartDefinition body_r3 = body.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.8F, -43.4783F, 4.9022F, -0.5585F, -0.6109F, 0.0F));

		PartDefinition body_r4 = body.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1F, -35.9783F, 9.9022F, -1.5708F, 0.6981F, -1.5708F));

		PartDefinition body_r5 = body.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1F, -35.9783F, 9.9022F, -0.8727F, 0.0F, 0.0F));

		PartDefinition body_r6 = body.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9F, -41.9783F, 6.9022F, -1.5708F, 0.6981F, -1.5708F));

		PartDefinition body_r7 = body.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(23, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9F, -41.9783F, 6.9022F, -0.8727F, 0.0F, 0.0F));

		PartDefinition body_r8 = body.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(51, 61).addBox(-9.0F, -6.0F, -5.5F, 18.0F, 12.0F, 11.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(0.0F, -34.0F, 2.5F, 0.6109F, 0.0F, 0.0F));

		PartDefinition head = waist.addOrReplaceChild("head", CubeListBuilder.create().texOffs(26, 49).addBox(-3.0F, -4.0F, -5.2F, 6.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(75, 0).addBox(-1.0F, -4.9F, -6.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.5F, -4.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(10, 99).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4611F, -10.4829F, -1.9F, 0.0F, -1.5708F, -0.1309F));

		PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(10, 99).addBox(-2.0F, -4.5F, -4.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2F, -8.5F, 2.1F, 0.0F, 0.0F, -0.1309F));

		PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(0, 49).addBox(-3.0F, -5.5F, -4.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.5F, -1.5F, 0.0F, 0.0F, -0.1309F));

		PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(0, 49).mirror().addBox(-2.0F, -5.5F, -4.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -3.5F, -1.5F, 0.0F, 0.0F, 0.1309F));

		PartDefinition arm_right = waist.addOrReplaceChild("arm_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.0F, -11.9F, -2.0F, -0.4737F, -0.0803F, -0.1551F));

		PartDefinition cube_r1 = arm_right.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(86, 0).addBox(-5.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-4.0F, 12.0F, -1.0F, -0.1309F, 0.0F, 0.2182F));

		PartDefinition cube_r2 = arm_right.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 100).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.4352F, -1.9572F, 0.0F, -1.5708F, 0.0F, 0.2182F));

		PartDefinition cube_r3 = arm_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 100).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.4352F, -1.9572F, 0.0F, 0.0F, 0.0F, 0.2182F));

		PartDefinition cube_r4 = arm_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(25, 64).addBox(-5.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

		PartDefinition forearm_right = arm_right.addOrReplaceChild("forearm_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.4957F, 11.3828F, -0.4661F, 0.4114F, 0.382F, 0.0657F));

		PartDefinition cube_r5 = forearm_right.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(59, 46).addBox(-5.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3957F, 6.5172F, -2.4339F, -0.3054F, 0.0F, 0.2182F));

		PartDefinition fingers_right = forearm_right.addOrReplaceChild("fingers_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 6.0F, -2.0F, 0.6149F, 0.1071F, 0.0754F));

		PartDefinition cube_r6 = fingers_right.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(85, 52).addBox(-1.114F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.2F, 1.4F, -2.3F, 0.0189F, 0.0129F, 0.1115F));

		PartDefinition cube_r7 = fingers_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(85, 52).addBox(-1.114F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(2.7F, 2.0F, -2.2F, 0.0891F, -0.1965F, 0.1075F));

		PartDefinition cube_r8 = fingers_right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(85, 52).addBox(-1.114F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.2F, 1.4F, -2.3F, 0.0189F, 0.0129F, 0.1115F));

		PartDefinition cube_r9 = fingers_right.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(85, 52).addBox(-1.114F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-2.3F, 0.8F, -2.4F, -0.2371F, 0.2515F, 0.2657F));

		PartDefinition cube_r10 = fingers_right.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(104, 50).addBox(-0.6982F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-1.5042F, -1.3828F, -0.1339F, -0.7258F, 0.2515F, 0.2657F));

		PartDefinition cube_r11 = fingers_right.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(94, 50).addBox(-0.6982F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(1.9958F, -0.6828F, -0.2339F, -0.584F, -0.2096F, 0.0645F));

		PartDefinition cube_r12 = fingers_right.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(94, 50).addBox(-0.6982F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.3957F, -1.1828F, -0.2339F, -0.6127F, 0.0715F, 0.168F));

		PartDefinition arm_left = waist.addOrReplaceChild("arm_left", CubeListBuilder.create(), PartPose.offsetAndRotation(9.0F, -11.9F, -2.0F, -0.4765F, 0.0603F, 0.1163F));

		PartDefinition cube_r13 = arm_left.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(86, 0).mirror().addBox(-1.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(4.0F, 12.0F, -1.0F, -0.1309F, 0.0F, -0.2182F));

		PartDefinition cube_r14 = arm_left.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(25, 64).mirror().addBox(-1.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

		PartDefinition forearm_left = arm_left.addOrReplaceChild("forearm_left", CubeListBuilder.create(), PartPose.offsetAndRotation(6.7957F, 11.3828F, 0.3339F, 0.4114F, -0.382F, -0.0657F));

		PartDefinition cube_r15 = forearm_left.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(59, 46).mirror().addBox(-1.0F, -7.0F, -1.0F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.6958F, 6.5172F, -3.2339F, -0.3054F, 0.0F, -0.2182F));

		PartDefinition fingers_left = forearm_left.addOrReplaceChild("fingers_left", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.0F, -3.0F, 0.7537F, -0.1603F, -0.1487F));

		PartDefinition cube_r16 = fingers_left.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(85, 52).mirror().addBox(-0.886F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 2.0F, -2.0F, 0.0891F, 0.1965F, -0.1075F));

		PartDefinition cube_r17 = fingers_left.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(85, 52).mirror().addBox(-0.886F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.5F, 1.4F, -2.1F, 0.0189F, -0.0129F, -0.1115F));

		PartDefinition cube_r18 = fingers_left.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(85, 52).mirror().addBox(-0.886F, 0.1098F, -0.7445F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(3.0F, 0.8F, -2.2F, -0.2371F, -0.2515F, -0.2657F));

		PartDefinition cube_r19 = fingers_left.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(94, 50).mirror().addBox(-1.3018F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(2.2043F, -1.3828F, 0.0661F, -0.7258F, -0.2515F, -0.2657F));

		PartDefinition cube_r20 = fingers_left.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(94, 50).mirror().addBox(-1.3018F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(-1.2957F, -0.6828F, -0.0339F, -0.584F, 0.2096F, -0.0645F));

		PartDefinition cube_r21 = fingers_left.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(94, 50).mirror().addBox(-1.3018F, 0.0349F, -0.6412F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.3043F, -1.1828F, -0.0339F, -0.6127F, -0.0715F, -0.168F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(StoneGolemEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		if (entity.isStomping()) {
			entity.ATTACK_ANIMATION.stop();
			entity.WALK_ANIMATION.stop();
			entity.IDLE_ANIMATION.stop();
			if (!entity.STOMP_ANIMATION.isStarted()) {
				entity.STOMP_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.STOMP_ANIMATION, StoneGolemAnimations.use_beyonder_power, ageInTicks, 1.0F);
			return;
		}

		if (entity.isMeleeAttacking()) {
			entity.STOMP_ANIMATION.stop();
			entity.WALK_ANIMATION.stop();
			entity.IDLE_ANIMATION.stop();
			if (!entity.ATTACK_ANIMATION.isStarted()) {
				entity.ATTACK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.ATTACK_ANIMATION, StoneGolemAnimations.attack, ageInTicks, 1.0F);
			return;
		}

		boolean isWalking = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && entity.onGround();

		if (isWalking) {
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, StoneGolemAnimations.walk, ageInTicks, 1.0F);
		} else {
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, StoneGolemAnimations.idle, ageInTicks, 1.0F);
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
		Golem.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}