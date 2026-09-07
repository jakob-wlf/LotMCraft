package de.jakob.lotm.rendering.models.visionary;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
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

public class VisionaryMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "visionary_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Mind_Dragon;
	private final ModelPart Head;
	private final ModelPart Upper_Jaw;
	private final ModelPart Horns;
	private final ModelPart Side_horn;
	private final ModelPart Side_horn2;
	private final ModelPart Horn2;
	private final ModelPart Horn;
	private final ModelPart Jaw;
	private final ModelPart Lightball;
	private final ModelPart body4;
	private final ModelPart body6;
	private final ModelPart body5;
	private final ModelPart body3;
	private final ModelPart body2;
	private final ModelPart Lightball2;
	private final ModelPart body7;
	private final ModelPart body8;
	private final ModelPart body9;
	private final ModelPart body10;
	private final ModelPart body11;
	private final ModelPart neck5;
	private final ModelPart neck1;
	private final ModelPart neck6;
	private final ModelPart neck7;
	private final ModelPart body;
	private final ModelPart neck14;
	private final ModelPart Legs;
	private final ModelPart Leg_Right;
	private final ModelPart Foot;
	private final ModelPart Finger5;
	private final ModelPart Finger6;
	private final ModelPart Finger7;
	private final ModelPart Finger8;
	private final ModelPart Leg_Left;
	private final ModelPart Foot1;
	private final ModelPart Finger3;
	private final ModelPart Finger4;
	private final ModelPart Finger2;
	private final ModelPart Finger;
	private final ModelPart tail12;
	private final ModelPart tail3;
	private final ModelPart tail2;
	private final ModelPart neck15;
	private final ModelPart neck19;
	private final ModelPart neck21;
	private final ModelPart neck23;
	private final ModelPart Left_Wing;
	private final ModelPart Wing_Left_Arm;
	private final ModelPart Wing_Left_Forearm;
	private final ModelPart Left_Wing_Skeleton_Far;
	private final ModelPart Left_Wing_Skeleton_far_Middle;
	private final ModelPart Left_Wing_Skeleton_Middle;
	private final ModelPart Wing_left_Claws;
	private final ModelPart Wing_left_Claw2;
	private final ModelPart Wing_left_Claw;
	private final ModelPart Left_Wing_Near_Membrane;
	private final ModelPart bone14;
	private final ModelPart bone13;
	private final ModelPart bone12;
	private final ModelPart bone11;
	private final ModelPart Left_Wing_Middle_Membrane2;
	private final ModelPart bone17;
	private final ModelPart bone16;
	private final ModelPart bone15;
	private final ModelPart Left_Wing_Far_Membrane;
	private final ModelPart bone18;
	private final ModelPart bone19;
	private final ModelPart bone20;
	private final ModelPart bone21;
	private final ModelPart Right_Wing;
	private final ModelPart Wing_Right_Arm;
	private final ModelPart Wing_Right_Forearm;
	private final ModelPart Right_Wing_Skeleton_Far2;
	private final ModelPart Right_Wing_Skeleton_far_Middle;
	private final ModelPart Right_Wing_Skeleton_Middle;
	private final ModelPart Wing_Right_Claws;
	private final ModelPart Wing_Right_Claw2;
	private final ModelPart Wing_Right_Claw;
	private final ModelPart Right_Wing_Near_Membrane2;
	private final ModelPart bone3;
	private final ModelPart bone2;
	private final ModelPart bone;
	private final ModelPart Right_Wing_Middle_Membrane3;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone4;
	private final ModelPart Right_Wing_Far_Membrane2;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart bone9;
	private final ModelPart bone10;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();


	public VisionaryMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Mind_Dragon = root.getChild("Mind_Dragon");
		this.Head = this.Mind_Dragon.getChild("Head");
		this.Upper_Jaw = this.Head.getChild("Upper_Jaw");
		this.Horns = this.Head.getChild("Horns");
		this.Side_horn = this.Horns.getChild("Side_horn");
		this.Side_horn2 = this.Horns.getChild("Side_horn2");
		this.Horn2 = this.Horns.getChild("Horn2");
		this.Horn = this.Horns.getChild("Horn");
		this.Jaw = this.Head.getChild("Jaw");
		this.Lightball = this.Head.getChild("Lightball");
		this.body4 = this.Lightball.getChild("body4");
		this.body6 = this.body4.getChild("body6");
		this.body5 = this.body4.getChild("body5");
		this.body3 = this.Lightball.getChild("body3");
		this.body2 = this.Lightball.getChild("body2");
		this.Lightball2 = this.Head.getChild("Lightball2");
		this.body7 = this.Lightball2.getChild("body7");
		this.body8 = this.body7.getChild("body8");
		this.body9 = this.body7.getChild("body9");
		this.body10 = this.Lightball2.getChild("body10");
		this.body11 = this.Lightball2.getChild("body11");
		this.neck5 = this.Mind_Dragon.getChild("neck5");
		this.neck1 = this.neck5.getChild("neck1");
		this.neck6 = this.neck5.getChild("neck6");
		this.neck7 = this.neck5.getChild("neck7");
		this.body = this.Mind_Dragon.getChild("body");
		this.neck14 = this.body.getChild("neck14");
		this.Legs = this.Mind_Dragon.getChild("Legs");
		this.Leg_Right = this.Legs.getChild("Leg_Right");
		this.Foot = this.Leg_Right.getChild("Foot");
		this.Finger5 = this.Foot.getChild("Finger5");
		this.Finger6 = this.Foot.getChild("Finger6");
		this.Finger7 = this.Foot.getChild("Finger7");
		this.Finger8 = this.Foot.getChild("Finger8");
		this.Leg_Left = this.Legs.getChild("Leg_Left");
		this.Foot1 = this.Leg_Left.getChild("Foot1");
		this.Finger3 = this.Foot1.getChild("Finger3");
		this.Finger4 = this.Foot1.getChild("Finger4");
		this.Finger2 = this.Foot1.getChild("Finger2");
		this.Finger = this.Foot1.getChild("Finger");
		this.tail12 = this.Mind_Dragon.getChild("tail12");
		this.tail3 = this.tail12.getChild("tail3");
		this.tail2 = this.tail12.getChild("tail2");
		this.neck15 = this.tail12.getChild("neck15");
		this.neck19 = this.tail12.getChild("neck19");
		this.neck21 = this.tail12.getChild("neck21");
		this.neck23 = this.tail12.getChild("neck23");
		this.Left_Wing = this.Mind_Dragon.getChild("Left_Wing");
		this.Wing_Left_Arm = this.Left_Wing.getChild("Wing_Left_Arm");
		this.Wing_Left_Forearm = this.Left_Wing.getChild("Wing_Left_Forearm");
		this.Left_Wing_Skeleton_Far = this.Left_Wing.getChild("Left_Wing_Skeleton_Far");
		this.Left_Wing_Skeleton_far_Middle = this.Left_Wing.getChild("Left_Wing_Skeleton_far_Middle");
		this.Left_Wing_Skeleton_Middle = this.Left_Wing.getChild("Left_Wing_Skeleton_Middle");
		this.Wing_left_Claws = this.Left_Wing.getChild("Wing_left_Claws");
		this.Wing_left_Claw2 = this.Wing_left_Claws.getChild("Wing_left_Claw2");
		this.Wing_left_Claw = this.Wing_left_Claws.getChild("Wing_left_Claw");
		this.Left_Wing_Near_Membrane = this.Left_Wing.getChild("Left_Wing_Near_Membrane");
		this.bone14 = this.Left_Wing_Near_Membrane.getChild("bone14");
		this.bone13 = this.Left_Wing_Near_Membrane.getChild("bone13");
		this.bone12 = this.Left_Wing_Near_Membrane.getChild("bone12");
		this.bone11 = this.Left_Wing_Near_Membrane.getChild("bone11");
		this.Left_Wing_Middle_Membrane2 = this.Left_Wing.getChild("Left_Wing_Middle_Membrane2");
		this.bone17 = this.Left_Wing_Middle_Membrane2.getChild("bone17");
		this.bone16 = this.Left_Wing_Middle_Membrane2.getChild("bone16");
		this.bone15 = this.Left_Wing_Middle_Membrane2.getChild("bone15");
		this.Left_Wing_Far_Membrane = this.Left_Wing.getChild("Left_Wing_Far_Membrane");
		this.bone18 = this.Left_Wing_Far_Membrane.getChild("bone18");
		this.bone19 = this.Left_Wing_Far_Membrane.getChild("bone19");
		this.bone20 = this.Left_Wing_Far_Membrane.getChild("bone20");
		this.bone21 = this.Left_Wing_Far_Membrane.getChild("bone21");
		this.Right_Wing = this.Mind_Dragon.getChild("Right_Wing");
		this.Wing_Right_Arm = this.Right_Wing.getChild("Wing_Right_Arm");
		this.Wing_Right_Forearm = this.Right_Wing.getChild("Wing_Right_Forearm");
		this.Right_Wing_Skeleton_Far2 = this.Right_Wing.getChild("Right_Wing_Skeleton_Far2");
		this.Right_Wing_Skeleton_far_Middle = this.Right_Wing.getChild("Right_Wing_Skeleton_far_Middle");
		this.Right_Wing_Skeleton_Middle = this.Right_Wing.getChild("Right_Wing_Skeleton_Middle");
		this.Wing_Right_Claws = this.Right_Wing.getChild("Wing_Right_Claws");
		this.Wing_Right_Claw2 = this.Wing_Right_Claws.getChild("Wing_Right_Claw2");
		this.Wing_Right_Claw = this.Wing_Right_Claws.getChild("Wing_Right_Claw");
		this.Right_Wing_Near_Membrane2 = this.Right_Wing.getChild("Right_Wing_Near_Membrane2");
		this.bone3 = this.Right_Wing_Near_Membrane2.getChild("bone3");
		this.bone2 = this.Right_Wing_Near_Membrane2.getChild("bone2");
		this.bone = this.Right_Wing_Near_Membrane2.getChild("bone");
		this.Right_Wing_Middle_Membrane3 = this.Right_Wing.getChild("Right_Wing_Middle_Membrane3");
		this.bone5 = this.Right_Wing_Middle_Membrane3.getChild("bone5");
		this.bone6 = this.Right_Wing_Middle_Membrane3.getChild("bone6");
		this.bone4 = this.Right_Wing_Middle_Membrane3.getChild("bone4");
		this.Right_Wing_Far_Membrane2 = this.Right_Wing.getChild("Right_Wing_Far_Membrane2");
		this.bone7 = this.Right_Wing_Far_Membrane2.getChild("bone7");
		this.bone8 = this.Right_Wing_Far_Membrane2.getChild("bone8");
		this.bone9 = this.Right_Wing_Far_Membrane2.getChild("bone9");
		this.bone10 = this.Right_Wing_Far_Membrane2.getChild("bone10");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Mind_Dragon = partdefinition.addOrReplaceChild("Mind_Dragon", CubeListBuilder.create(), PartPose.offset(0.0F, -44.6F, -31.0F));

		PartDefinition Head = Mind_Dragon.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(-2.5F, -18.7F, -66.8F));

		PartDefinition body_r1 = Head.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(122, 95).addBox(-8.0F, -13.0684F, -0.5884F, 16.0F, 18.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8F, -0.1281F, 14.7635F, 0.2793F, 0.0F, 0.0F));

		PartDefinition body_r2 = Head.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(121, 88).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.9499F, -11.6205F, 14.7807F, -0.239F, -0.4054F, 1.7461F));

		PartDefinition body_r3 = Head.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(121, 88).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2501F, -11.3205F, 13.9807F, 0.2965F, -0.4808F, 1.2129F));

		PartDefinition Upper_Jaw = Head.addOrReplaceChild("Upper_Jaw", CubeListBuilder.create(), PartPose.offset(0.0F, -5.4F, 26.4F));

		PartDefinition body_r4 = Upper_Jaw.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(189, 75).addBox(-2.0F, -4.9251F, -38.771F, 8.0F, 7.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2793F, 0.0F, 0.0F));

		PartDefinition body_r5 = Upper_Jaw.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(184, 99).addBox(-4.0F, -8.9484F, -25.8637F, 12.0F, 10.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.2793F, 0.0F, 0.0F));

		PartDefinition body_r6 = Upper_Jaw.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, 7.5087F, -15.6327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r7 = Upper_Jaw.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, 8.8087F, -20.0327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r8 = Upper_Jaw.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, 9.8087F, -23.3327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r9 = Upper_Jaw.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2438F, 10.4087F, -25.8327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r10 = Upper_Jaw.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2438F, 11.7087F, -30.1327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r11 = Upper_Jaw.addOrReplaceChild("body_r11", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2438F, 12.6087F, -33.6327F, 1.3108F, 0.0879F, -2.8711F));

		PartDefinition body_r12 = Upper_Jaw.addOrReplaceChild("body_r12", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4438F, 13.6087F, -36.4327F, 0.0348F, 1.5323F, 1.6159F));

		PartDefinition body_r13 = Upper_Jaw.addOrReplaceChild("body_r13", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.3F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4562F, 14.0087F, -36.6327F, 0.0348F, 1.5323F, 1.6159F));

		PartDefinition body_r14 = Upper_Jaw.addOrReplaceChild("body_r14", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.3F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4562F, 13.9087F, -36.6327F, 0.0348F, 1.5323F, 1.6159F));

		PartDefinition body_r15 = Upper_Jaw.addOrReplaceChild("body_r15", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 12.8087F, -34.0327F, 1.3126F, -0.093F, 2.755F));

		PartDefinition body_r16 = Upper_Jaw.addOrReplaceChild("body_r16", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 11.8087F, -30.6327F, 1.2602F, -0.093F, 2.755F));

		PartDefinition body_r17 = Upper_Jaw.addOrReplaceChild("body_r17", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 11.0087F, -27.7327F, 1.2602F, -0.093F, 2.755F));

		PartDefinition body_r18 = Upper_Jaw.addOrReplaceChild("body_r18", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 10.2087F, -24.9327F, 1.2602F, -0.093F, 2.755F));

		PartDefinition body_r19 = Upper_Jaw.addOrReplaceChild("body_r19", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3562F, 9.7087F, -23.3327F, 1.2602F, -0.093F, 2.755F));

		PartDefinition body_r20 = Upper_Jaw.addOrReplaceChild("body_r20", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3562F, 9.0087F, -20.9327F, 1.3292F, -0.1272F, 2.7622F));

		PartDefinition body_r21 = Upper_Jaw.addOrReplaceChild("body_r21", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3562F, 8.0087F, -17.6327F, 1.3297F, -0.1103F, 2.758F));

		PartDefinition body_r22 = Upper_Jaw.addOrReplaceChild("body_r22", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3562F, 6.9087F, -13.7327F, 1.3297F, -0.1103F, 2.758F));

		PartDefinition Horns = Head.addOrReplaceChild("Horns", CubeListBuilder.create(), PartPose.offset(9.6501F, -5.0205F, 26.0807F));

		PartDefinition Side_horn = Horns.addOrReplaceChild("Side_horn", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4014F, 0.0F));

		PartDefinition body_r23 = Side_horn.addOrReplaceChild("body_r23", CubeListBuilder.create().texOffs(112, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.956F, 2.063F, 4.4693F, 0.4001F, -0.4365F, 1.2515F));

		PartDefinition body_r24 = Side_horn.addOrReplaceChild("body_r24", CubeListBuilder.create().texOffs(108, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6011F, 0.7993F, 3.9034F, 0.4544F, -0.5492F, 1.2516F));

		PartDefinition body_r25 = Side_horn.addOrReplaceChild("body_r25", CubeListBuilder.create().texOffs(120, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6011F, -2.8007F, 7.4034F, 0.3936F, -0.6465F, 1.2046F));

		PartDefinition body_r26 = Side_horn.addOrReplaceChild("body_r26", CubeListBuilder.create().texOffs(116, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1011F, -0.7007F, 5.6034F, 0.4369F, -0.5492F, 1.2516F));

		PartDefinition body_r27 = Side_horn.addOrReplaceChild("body_r27", CubeListBuilder.create().texOffs(108, 84).addBox(0.0F, 1.5F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7F, 1.0F, 2.9F, 0.2699F, 0.1386F, 1.4581F));

		PartDefinition body_r28 = Side_horn.addOrReplaceChild("body_r28", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, 3.0F, 1.4F, 0.2699F, 0.1386F, 1.4581F));

		PartDefinition body_r29 = Side_horn.addOrReplaceChild("body_r29", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, 0.0F, 0.0F, 0.2965F, -0.4808F, 1.2129F));

		PartDefinition body_r30 = Side_horn.addOrReplaceChild("body_r30", CubeListBuilder.create().texOffs(99, 81).addBox(1.0F, 0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.6F, 4.3F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition body_r31 = Side_horn.addOrReplaceChild("body_r31", CubeListBuilder.create().texOffs(99, 81).addBox(1.0F, 0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8F, -5.7F, 6.2F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition body_r32 = Side_horn.addOrReplaceChild("body_r32", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, -2.5F, 3.4F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition Side_horn2 = Horns.addOrReplaceChild("Side_horn2", CubeListBuilder.create(), PartPose.offsetAndRotation(-14.0F, 3.2F, -1.1F, 0.0F, -1.0123F, 0.0F));

		PartDefinition body_r33 = Side_horn2.addOrReplaceChild("body_r33", CubeListBuilder.create().texOffs(112, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.956F, -1.1371F, 5.5693F, 0.4001F, -0.4365F, 1.2515F));

		PartDefinition body_r34 = Side_horn2.addOrReplaceChild("body_r34", CubeListBuilder.create().texOffs(108, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6011F, -2.4007F, 5.0034F, 0.4544F, -0.5492F, 1.2516F));

		PartDefinition body_r35 = Side_horn2.addOrReplaceChild("body_r35", CubeListBuilder.create().texOffs(120, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6011F, -6.0007F, 8.5034F, 0.3936F, -0.6465F, 1.2046F));

		PartDefinition body_r36 = Side_horn2.addOrReplaceChild("body_r36", CubeListBuilder.create().texOffs(116, 84).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1011F, -3.9007F, 6.7034F, 0.4369F, -0.5492F, 1.2516F));

		PartDefinition body_r37 = Side_horn2.addOrReplaceChild("body_r37", CubeListBuilder.create().texOffs(108, 84).addBox(0.0F, 1.5F, -2.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7F, -2.2F, 4.0F, 0.2699F, 0.1386F, 1.4581F));

		PartDefinition body_r38 = Side_horn2.addOrReplaceChild("body_r38", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, -0.2F, 2.5F, 0.2699F, 0.1386F, 1.4581F));

		PartDefinition body_r39 = Side_horn2.addOrReplaceChild("body_r39", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -3.2F, 1.1F, 0.2965F, -0.4808F, 1.2129F));

		PartDefinition body_r40 = Side_horn2.addOrReplaceChild("body_r40", CubeListBuilder.create().texOffs(99, 81).addBox(1.0F, 0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -1.6F, 5.4F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition body_r41 = Side_horn2.addOrReplaceChild("body_r41", CubeListBuilder.create().texOffs(99, 81).addBox(1.0F, 0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8F, -8.9F, 7.3F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition body_r42 = Side_horn2.addOrReplaceChild("body_r42", CubeListBuilder.create().texOffs(97, 80).addBox(0.0F, -0.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, -5.7F, 4.5F, 0.4528F, -0.5946F, 1.1865F));

		PartDefinition Horn2 = Horns.addOrReplaceChild("Horn2", CubeListBuilder.create(), PartPose.offsetAndRotation(-17.7768F, -20.6874F, 3.8531F, -0.0037F, 0.2093F, 0.0168F));

		PartDefinition body_r43 = Horn2.addOrReplaceChild("body_r43", CubeListBuilder.create().texOffs(97, 117).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5042F, -10.4202F, 7.4984F, 1.1946F, 0.2995F, 0.173F));

		PartDefinition body_r44 = Horn2.addOrReplaceChild("body_r44", CubeListBuilder.create().texOffs(70, 116).addBox(1.0F, 1.5F, -6.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7733F, -8.0831F, 3.3794F, 1.2626F, -0.7322F, -1.4452F));

		PartDefinition body_r45 = Horn2.addOrReplaceChild("body_r45", CubeListBuilder.create().texOffs(70, 116).addBox(1.0F, 1.5F, -6.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.9733F, -6.3831F, 3.6794F, 0.9644F, -0.572F, -1.1723F));

		PartDefinition body_r46 = Horn2.addOrReplaceChild("body_r46", CubeListBuilder.create().texOffs(79, 118).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4079F, -4.4583F, 3.3389F, 0.7196F, -0.4964F, -0.8261F));

		PartDefinition body_r47 = Horn2.addOrReplaceChild("body_r47", CubeListBuilder.create().texOffs(79, 118).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3858F, -2.234F, 1.3379F, 0.9281F, -0.6571F, -0.9921F));

		PartDefinition body_r48 = Horn2.addOrReplaceChild("body_r48", CubeListBuilder.create().texOffs(97, 117).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.9733F, -3.7831F, 0.1794F, 0.899F, 0.1329F, -0.0945F));

		PartDefinition body_r49 = Horn2.addOrReplaceChild("body_r49", CubeListBuilder.create().texOffs(97, 117).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0733F, -0.6831F, -3.4206F, 1.1497F, -0.1688F, -0.127F));

		PartDefinition body_r50 = Horn2.addOrReplaceChild("body_r50", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5733F, 3.5169F, -4.4206F, 1.1361F, -0.251F, -0.5277F));

		PartDefinition body_r51 = Horn2.addOrReplaceChild("body_r51", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4733F, 6.8169F, -6.2206F, 1.0611F, -0.2769F, -0.6878F));

		PartDefinition body_r52 = Horn2.addOrReplaceChild("body_r52", CubeListBuilder.create().texOffs(97, 117).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0733F, 9.6169F, -8.7206F, 1.3515F, -0.1706F, -0.873F));

		PartDefinition Horn = Horns.addOrReplaceChild("Horn", CubeListBuilder.create(), PartPose.offset(-3.1501F, -38.6705F, 11.6325F));

		PartDefinition body_r53 = Horn.addOrReplaceChild("body_r53", CubeListBuilder.create().texOffs(110, 73).addBox(2.0F, 2.5F, -6.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8806F, -0.6091F, 0.1365F));

		PartDefinition body_r54 = Horn.addOrReplaceChild("body_r54", CubeListBuilder.create().texOffs(110, 73).addBox(1.0F, 1.5F, -6.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, 2.7F, 0.1F, 0.7235F, -0.6091F, 0.1365F));

		PartDefinition body_r55 = Horn.addOrReplaceChild("body_r55", CubeListBuilder.create().texOffs(110, 73).addBox(1.0F, 1.5F, -6.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 4.7F, -2.1F, 0.7042F, -0.7186F, 0.0265F));

		PartDefinition body_r56 = Horn.addOrReplaceChild("body_r56", CubeListBuilder.create().texOffs(110, 73).addBox(0.0F, 0.5F, -6.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8F, 8.3F, -3.5F, 0.6647F, -0.8567F, -0.1633F));

		PartDefinition body_r57 = Horn.addOrReplaceChild("body_r57", CubeListBuilder.create().texOffs(110, 73).addBox(0.0F, 0.5F, -6.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 9.7F, -5.6F, 0.6871F, -0.6568F, 0.1069F));

		PartDefinition body_r58 = Horn.addOrReplaceChild("body_r58", CubeListBuilder.create().texOffs(110, 73).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4F, 12.9F, -7.6F, 0.7676F, -0.5248F, 0.275F));

		PartDefinition body_r59 = Horn.addOrReplaceChild("body_r59", CubeListBuilder.create().texOffs(110, 73).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, 15.6F, -10.5F, 0.9538F, -0.429F, 0.4394F));

		PartDefinition body_r60 = Horn.addOrReplaceChild("body_r60", CubeListBuilder.create().texOffs(110, 73).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3F, 19.1F, -12.2F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r61 = Horn.addOrReplaceChild("body_r61", CubeListBuilder.create().texOffs(110, 73).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9F, 23.1F, -14.0F, 1.0611F, -0.2769F, 0.6878F));

		PartDefinition body_r62 = Horn.addOrReplaceChild("body_r62", CubeListBuilder.create().texOffs(110, 73).addBox(-2.0F, -1.5F, -6.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4F, 25.9F, -16.5F, 1.3515F, -0.1706F, 0.873F));

		PartDefinition Jaw = Head.addOrReplaceChild("Jaw", CubeListBuilder.create(), PartPose.offset(0.0F, 1.6F, 14.7F));

		PartDefinition body_r63 = Jaw.addOrReplaceChild("body_r63", CubeListBuilder.create().texOffs(129, 130).addBox(-4.0F, -1.9008F, -26.2538F, 8.0F, 4.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(188, 54).addBox(-6.0F, -1.9629F, -13.6222F, 12.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2719F, 0.7635F, 0.2793F, 0.0F, 0.0F));

		PartDefinition body_r64 = Jaw.addOrReplaceChild("body_r64", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9727F, 4.9112F, -25.4097F, -3.1412F, -1.0555F, -1.5528F));

		PartDefinition body_r65 = Jaw.addOrReplaceChild("body_r65", CubeListBuilder.create().texOffs(174, 100).addBox(-0.0227F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5273F, 4.9112F, -25.4097F, -3.1104F, -1.0552F, -1.5882F));

		PartDefinition body_r66 = Jaw.addOrReplaceChild("body_r66", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2562F, -1.0913F, -4.4327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r67 = Jaw.addOrReplaceChild("body_r67", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2562F, -0.0913F, -7.8327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r68 = Jaw.addOrReplaceChild("body_r68", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2562F, 0.9087F, -11.1327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r69 = Jaw.addOrReplaceChild("body_r69", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 1.7087F, -14.6327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r70 = Jaw.addOrReplaceChild("body_r70", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 2.6087F, -17.6327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r71 = Jaw.addOrReplaceChild("body_r71", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 3.5087F, -20.9327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r72 = Jaw.addOrReplaceChild("body_r72", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3562F, 4.4087F, -23.8327F, 1.8242F, -0.1057F, 0.3383F));

		PartDefinition body_r73 = Jaw.addOrReplaceChild("body_r73", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2438F, 4.4087F, -23.3327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition body_r74 = Jaw.addOrReplaceChild("body_r74", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3438F, 3.4087F, -20.5327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition body_r75 = Jaw.addOrReplaceChild("body_r75", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3438F, 2.4087F, -17.1327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition body_r76 = Jaw.addOrReplaceChild("body_r76", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, 0.4087F, -10.2327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition body_r77 = Jaw.addOrReplaceChild("body_r77", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, -0.5913F, -6.9327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition body_r78 = Jaw.addOrReplaceChild("body_r78", CubeListBuilder.create().texOffs(174, 100).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3438F, -1.7913F, -2.5327F, 1.8359F, 0.0704F, -0.3021F));

		PartDefinition Lightball = Head.addOrReplaceChild("Lightball", CubeListBuilder.create(), PartPose.offset(1.4995F, -25.3187F, 28.6787F));

		PartDefinition body4 = Lightball.addOrReplaceChild("body4", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, -1.8075F));

		PartDefinition body_r79 = body4.addOrReplaceChild("body_r79", CubeListBuilder.create().texOffs(226, 55).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body6 = body4.addOrReplaceChild("body6", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r80 = body6.addOrReplaceChild("body_r80", CubeListBuilder.create().texOffs(229, 57).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r81 = body6.addOrReplaceChild("body_r81", CubeListBuilder.create().texOffs(229, 57).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body5 = body4.addOrReplaceChild("body5", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r82 = body5.addOrReplaceChild("body_r82", CubeListBuilder.create().texOffs(229, 57).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r83 = body5.addOrReplaceChild("body_r83", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r84 = body5.addOrReplaceChild("body_r84", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r85 = body5.addOrReplaceChild("body_r85", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body3 = Lightball.addOrReplaceChild("body3", CubeListBuilder.create(), PartPose.offset(0.937F, -0.3976F, -1.9222F));

		PartDefinition body_r86 = body3.addOrReplaceChild("body_r86", CubeListBuilder.create().texOffs(231, 46).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r87 = body3.addOrReplaceChild("body_r87", CubeListBuilder.create().texOffs(230, 46).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body2 = Lightball.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, -1.975F));

		PartDefinition body_r88 = body2.addOrReplaceChild("body_r88", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r89 = body2.addOrReplaceChild("body_r89", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r90 = body2.addOrReplaceChild("body_r90", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r91 = body2.addOrReplaceChild("body_r91", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition Lightball2 = Head.addOrReplaceChild("Lightball2", CubeListBuilder.create(), PartPose.offset(0.8996F, -1.3187F, 19.3787F));

		PartDefinition body7 = Lightball2.addOrReplaceChild("body7", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, -1.8075F));

		PartDefinition body_r92 = body7.addOrReplaceChild("body_r92", CubeListBuilder.create().texOffs(226, 55).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body8 = body7.addOrReplaceChild("body8", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r93 = body8.addOrReplaceChild("body_r93", CubeListBuilder.create().texOffs(229, 57).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r94 = body8.addOrReplaceChild("body_r94", CubeListBuilder.create().texOffs(229, 57).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body9 = body7.addOrReplaceChild("body9", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r95 = body9.addOrReplaceChild("body_r95", CubeListBuilder.create().texOffs(229, 57).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r96 = body9.addOrReplaceChild("body_r96", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r97 = body9.addOrReplaceChild("body_r97", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r98 = body9.addOrReplaceChild("body_r98", CubeListBuilder.create().texOffs(230, 58).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body10 = Lightball2.addOrReplaceChild("body10", CubeListBuilder.create(), PartPose.offset(0.9369F, -0.3976F, -1.9222F));

		PartDefinition body_r99 = body10.addOrReplaceChild("body_r99", CubeListBuilder.create().texOffs(231, 46).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r100 = body10.addOrReplaceChild("body_r100", CubeListBuilder.create().texOffs(230, 46).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body11 = Lightball2.addOrReplaceChild("body11", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, -1.975F));

		PartDefinition body_r101 = body11.addOrReplaceChild("body_r101", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r102 = body11.addOrReplaceChild("body_r102", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r103 = body11.addOrReplaceChild("body_r103", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r104 = body11.addOrReplaceChild("body_r104", CubeListBuilder.create().texOffs(226, 73).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition neck5 = Mind_Dragon.addOrReplaceChild("neck5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7505F, 0.0F, 0.0F));

		PartDefinition neck1 = neck5.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(30, 158).addBox(-6.05F, -1.549F, -10.2107F, 12.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(28, 157).addBox(-6.85F, -2.149F, -0.2107F, 14.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.05F, -4.551F, -9.7893F));

		PartDefinition neck10_r1 = neck1.addOrReplaceChild("neck10_r1", CubeListBuilder.create().texOffs(98, -9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.05F, -6.149F, 5.3893F, -0.0524F, 0.0F, 0.0F));

		PartDefinition neck9_r1 = neck1.addOrReplaceChild("neck9_r1", CubeListBuilder.create().texOffs(98, -9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.05F, -6.149F, -4.8107F, -0.0524F, 0.0F, 0.0F));

		PartDefinition neck6 = neck5.addOrReplaceChild("neck6", CubeListBuilder.create(), PartPose.offset(-5.1F, 1.4F, -48.3F));

		PartDefinition neck6_r1 = neck6.addOrReplaceChild("neck6_r1", CubeListBuilder.create().texOffs(98, -9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition neck5_r1 = neck6.addOrReplaceChild("neck5_r1", CubeListBuilder.create().texOffs(65, 1).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1F, 6.7F, 6.9F, 0.7854F, 0.0F, 0.0F));

		PartDefinition neck7 = neck5.addOrReplaceChild("neck7", CubeListBuilder.create(), PartPose.offset(-0.05F, -2.3491F, -30.6949F));

		PartDefinition neck7_r1 = neck7.addOrReplaceChild("neck7_r1", CubeListBuilder.create().texOffs(98, -9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.05F, -3.3509F, -7.1051F, 0.4538F, 0.0F, 0.0F));

		PartDefinition neck8_r1 = neck7.addOrReplaceChild("neck8_r1", CubeListBuilder.create().texOffs(98, -9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.05F, -6.6509F, 3.4949F, 0.2443F, 0.0F, 0.0F));

		PartDefinition neck3_r1 = neck7.addOrReplaceChild("neck3_r1", CubeListBuilder.create().texOffs(65, 1).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 2.0491F, 5.8949F, 0.2443F, 0.0F, 0.0F));

		PartDefinition neck4_r1 = neck7.addOrReplaceChild("neck4_r1", CubeListBuilder.create().texOffs(65, 1).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 4.8491F, -3.1051F, 0.4538F, 0.0F, 0.0F));

		PartDefinition body = Mind_Dragon.addOrReplaceChild("body", CubeListBuilder.create().texOffs(98, 2).addBox(-10.3F, -12.0F, -35.2F, 20.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(98, 2).addBox(-10.3F, -12.0F, -11.2F, 20.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(98, 2).addBox(-10.3F, -12.0F, 10.8F, 20.0F, 24.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(9.2F, -8.7708F, 11.0062F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(9.2F, -8.7708F, -10.9938F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(9.2F, -8.7708F, -34.9938F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(-14.4F, -8.7708F, 11.0062F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(-14.4F, -8.7708F, -10.9938F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(62, 71).addBox(-14.4F, -8.7708F, -34.9938F, 5.0F, 18.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 16.7F, 31.2F, -0.4538F, 0.0F, 0.0F));

		PartDefinition neck14 = body.addOrReplaceChild("neck14", CubeListBuilder.create(), PartPose.offset(-5.1F, -16.2F, 27.5F));

		PartDefinition neck14_r1 = neck14.addOrReplaceChild("neck14_r1", CubeListBuilder.create().texOffs(72, 15).addBox(5.0F, -5.0937F, -9.4276F, 0.0F, 9.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0349F, 0.0F, 0.0F));

		PartDefinition neck13_r1 = neck14.addOrReplaceChild("neck13_r1", CubeListBuilder.create().texOffs(72, 15).addBox(5.0F, -4.6382F, -7.2545F, 0.0F, 9.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -17.8F, 0.0349F, 0.0F, 0.0F));

		PartDefinition neck12_r1 = neck14.addOrReplaceChild("neck12_r1", CubeListBuilder.create().texOffs(70, 25).addBox(5.2056F, -5.2578F, -3.4939F, 0.0F, 10.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.2F, -37.9F, 0.0379F, 0.0151F, 0.038F));

		PartDefinition neck11_r1 = neck14.addOrReplaceChild("neck11_r1", CubeListBuilder.create().texOffs(72, 15).addBox(5.0F, -3.6765F, -2.7762F, 0.0F, 9.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, -54.9F, 0.0349F, 0.0F, 0.0F));

		PartDefinition Legs = Mind_Dragon.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(-10.0F, 37.1846F, 50.135F));

		PartDefinition Leg_Right = Legs.addOrReplaceChild("Leg_Right", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3665F, 0.0F, 0.0F));

		PartDefinition Leg3_r1 = Leg_Right.addOrReplaceChild("Leg3_r1", CubeListBuilder.create().texOffs(173, 130).addBox(-1.3F, -4.5F, -0.1F, 7.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 16.7154F, -2.935F, -1.9548F, 0.0F, 0.0F));

		PartDefinition Leg4_r1 = Leg_Right.addOrReplaceChild("Leg4_r1", CubeListBuilder.create().texOffs(164, 0).addBox(-3.3F, -3.5F, -0.1F, 9.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 11.4154F, -9.835F, -0.3665F, 0.0F, 0.0F));

		PartDefinition Leg2_r1 = Leg_Right.addOrReplaceChild("Leg2_r1", CubeListBuilder.create().texOffs(212, 130).addBox(-4.3F, -7.5F, -0.1F, 10.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.9846F, -1.735F, -1.9548F, 0.0F, 0.0F));

		PartDefinition Foot = Leg_Right.addOrReplaceChild("Foot", CubeListBuilder.create(), PartPose.offsetAndRotation(0.8F, 27.6154F, -6.635F, 0.0349F, 0.0F, 0.0F));

		PartDefinition Leg4_r2 = Foot.addOrReplaceChild("Leg4_r2", CubeListBuilder.create().texOffs(191, 30).addBox(-1.3F, -0.5F, -0.1F, 7.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3F, 4.2F, 4.9F, 2.7576F, 0.0F, 0.0F));

		PartDefinition Finger5 = Foot.addOrReplaceChild("Finger5", CubeListBuilder.create(), PartPose.offsetAndRotation(2.3F, -1.4154F, -4.165F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg8_r1 = Finger5.addOrReplaceChild("Leg8_r1", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg7_r1 = Finger5.addOrReplaceChild("Leg7_r1", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg6_r1 = Finger5.addOrReplaceChild("Leg6_r1", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg5_r1 = Finger5.addOrReplaceChild("Leg5_r1", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition Finger6 = Foot.addOrReplaceChild("Finger6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, -1.4154F, -4.165F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg9_r1 = Finger6.addOrReplaceChild("Leg9_r1", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg8_r2 = Finger6.addOrReplaceChild("Leg8_r2", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg7_r2 = Finger6.addOrReplaceChild("Leg7_r2", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg6_r2 = Finger6.addOrReplaceChild("Leg6_r2", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition Finger7 = Foot.addOrReplaceChild("Finger7", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, 2.3846F, 5.135F, -2.3911F, 0.0F, 3.1416F));

		PartDefinition Leg10_r1 = Finger7.addOrReplaceChild("Leg10_r1", CubeListBuilder.create().texOffs(194, 37).addBox(39.6681F, -14.8483F, 19.1586F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg9_r2 = Finger7.addOrReplaceChild("Leg9_r2", CubeListBuilder.create().texOffs(191, 30).addBox(39.1681F, -21.242F, 10.2756F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg8_r3 = Finger7.addOrReplaceChild("Leg8_r3", CubeListBuilder.create().texOffs(197, 46).addBox(38.5681F, -24.1048F, -0.169F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Finger8 = Foot.addOrReplaceChild("Finger8", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.6F, -1.4154F, -4.165F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg10_r2 = Finger8.addOrReplaceChild("Leg10_r2", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg9_r3 = Finger8.addOrReplaceChild("Leg9_r3", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg8_r4 = Finger8.addOrReplaceChild("Leg8_r4", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg7_r3 = Finger8.addOrReplaceChild("Leg7_r3", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition Leg_Left = Legs.addOrReplaceChild("Leg_Left", CubeListBuilder.create(), PartPose.offsetAndRotation(19.4F, 0.2F, 0.5F, 0.3665F, 0.0F, 0.0F));

		PartDefinition Leg2_r2 = Leg_Left.addOrReplaceChild("Leg2_r2", CubeListBuilder.create().texOffs(173, 130).addBox(-1.3F, -4.5F, -0.1F, 7.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4F, 16.5154F, -3.435F, -1.9548F, 0.0F, 0.0F));

		PartDefinition Leg3_r2 = Leg_Left.addOrReplaceChild("Leg3_r2", CubeListBuilder.create().texOffs(164, 0).addBox(-3.3F, -3.5F, -0.1F, 9.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4F, 11.2154F, -10.335F, -0.3665F, 0.0F, 0.0F));

		PartDefinition Leg1_r1 = Leg_Left.addOrReplaceChild("Leg1_r1", CubeListBuilder.create().texOffs(212, 130).addBox(-4.3F, -7.5F, -0.1F, 10.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -2.1846F, -2.235F, -1.9548F, 0.0F, 0.0F));

		PartDefinition Foot1 = Leg_Left.addOrReplaceChild("Foot1", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, 27.6154F, -7.035F, 0.0175F, 0.0F, 0.0F));

		PartDefinition Leg3_r3 = Foot1.addOrReplaceChild("Leg3_r3", CubeListBuilder.create().texOffs(191, 30).addBox(-1.3F, -0.5F, -0.1F, 7.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9F, 4.0F, 4.8F, 2.7576F, 0.0F, 0.0F));

		PartDefinition Finger3 = Foot1.addOrReplaceChild("Finger3", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.2F, -1.6154F, -4.265F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg9_r4 = Finger3.addOrReplaceChild("Leg9_r4", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg8_r5 = Finger3.addOrReplaceChild("Leg8_r5", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg7_r4 = Finger3.addOrReplaceChild("Leg7_r4", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg6_r3 = Finger3.addOrReplaceChild("Leg6_r3", CubeListBuilder.create().texOffs(191, 30).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition Finger4 = Foot1.addOrReplaceChild("Finger4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, 2.1846F, 5.035F, -2.3911F, 0.0F, 3.1416F));

		PartDefinition Leg9_r5 = Finger4.addOrReplaceChild("Leg9_r5", CubeListBuilder.create().texOffs(194, 37).addBox(39.6681F, -14.8483F, 19.1586F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg8_r6 = Finger4.addOrReplaceChild("Leg8_r6", CubeListBuilder.create().texOffs(191, 30).addBox(39.1681F, -21.242F, 10.2756F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg7_r5 = Finger4.addOrReplaceChild("Leg7_r5", CubeListBuilder.create().texOffs(197, 46).addBox(38.5681F, -24.1048F, -0.169F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.8681F, -22.6505F, 3.992F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Finger2 = Foot1.addOrReplaceChild("Finger2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, -1.6154F, -4.265F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg8_r7 = Finger2.addOrReplaceChild("Leg8_r7", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg7_r6 = Finger2.addOrReplaceChild("Leg7_r6", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg6_r4 = Finger2.addOrReplaceChild("Leg6_r4", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg5_r2 = Finger2.addOrReplaceChild("Leg5_r2", CubeListBuilder.create().texOffs(191, 30).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition Finger = Foot1.addOrReplaceChild("Finger", CubeListBuilder.create(), PartPose.offsetAndRotation(2.7F, -1.6154F, -4.265F, 0.2094F, 0.0F, 0.0F));

		PartDefinition Leg7_r7 = Finger.addOrReplaceChild("Leg7_r7", CubeListBuilder.create().texOffs(194, 37).addBox(0.5F, -0.5F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.5F, -7.0F, -2.5831F, 0.0F, 0.0F));

		PartDefinition Leg6_r5 = Finger.addOrReplaceChild("Leg6_r5", CubeListBuilder.create().texOffs(196, 38).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.1F, -5.4F, -2.9845F, 0.0F, 0.0F));

		PartDefinition Leg5_r3 = Finger.addOrReplaceChild("Leg5_r3", CubeListBuilder.create().texOffs(197, 46).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -0.1846F, -2.135F, 2.9496F, 0.0F, 0.0F));

		PartDefinition Leg4_r3 = Finger.addOrReplaceChild("Leg4_r3", CubeListBuilder.create().texOffs(191, 30).addBox(3.7F, 0.5F, -0.1F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 1.1154F, -1.035F, 2.4086F, 0.0F, 0.0F));

		PartDefinition tail12 = Mind_Dragon.addOrReplaceChild("tail12", CubeListBuilder.create(), PartPose.offset(0.6833F, 33.1912F, 61.4817F));

		PartDefinition tail3 = tail12.addOrReplaceChild("tail3", CubeListBuilder.create(), PartPose.offset(-0.6833F, 7.3741F, 19.4425F));

		PartDefinition tail3_r1 = tail3.addOrReplaceChild("tail3_r1", CubeListBuilder.create().texOffs(135, 68).addBox(-5.0F, -6.4622F, 1.3334F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2269F, 0.0F, 0.0F));

		PartDefinition tail4_r1 = tail3.addOrReplaceChild("tail4_r1", CubeListBuilder.create().texOffs(136, 68).addBox(-5.0F, -4.2383F, 0.3582F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8161F, 10.3841F, -0.1396F, 0.0F, 0.0F));

		PartDefinition neck18_r1 = tail3.addOrReplaceChild("neck18_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1F, -6.5654F, 16.7758F, -0.1222F, 0.0F, 0.0F));

		PartDefinition neck17_r1 = tail3.addOrReplaceChild("neck17_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1F, -8.9654F, 8.2758F, -0.2094F, 0.0F, 0.0F));

		PartDefinition tail2 = tail12.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.offset(-0.1833F, 27.8908F, 95.7843F));

		PartDefinition tail12_r1 = tail2.addOrReplaceChild("tail12_r1", CubeListBuilder.create().texOffs(155, 73).addBox(-2.0F, -3.5483F, 1.2099F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 7.0F, -0.2443F, 0.0F, 0.0F));

		PartDefinition tail11_r1 = tail2.addOrReplaceChild("tail11_r1", CubeListBuilder.create().texOffs(151, 71).addBox(-3.0F, -2.5092F, -0.137F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.8025F, -0.083F, -0.4363F, 0.0F, 0.0F));

		PartDefinition neck15 = tail12.addOrReplaceChild("neck15", CubeListBuilder.create(), PartPose.offset(-5.7833F, 0.6088F, 2.9183F));

		PartDefinition neck15_r1 = neck15.addOrReplaceChild("neck15_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -2.0F, -5.0F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.0F, 8.0F, -0.3316F, 0.0F, 0.0F));

		PartDefinition neck16_r1 = neck15.addOrReplaceChild("neck16_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.2F, 17.0F, -0.3316F, 0.0F, 0.0F));

		PartDefinition tail2_r1 = neck15.addOrReplaceChild("tail2_r1", CubeListBuilder.create().texOffs(135, 68).addBox(-6.3F, -7.3589F, 1.2244F, 12.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6F, 3.2F, 6.1F, -0.3316F, 0.0F, 0.0F));

		PartDefinition tail1_r1 = neck15.addOrReplaceChild("tail1_r1", CubeListBuilder.create().texOffs(135, 68).addBox(-8.3F, -11.5F, -0.1F, 14.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, 3.4F, -2.8F, -0.3316F, 0.0F, 0.0F));

		PartDefinition neck19 = tail12.addOrReplaceChild("neck19", CubeListBuilder.create().texOffs(137, 67).addBox(0.1F, -3.9F, 0.7F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.7833F, 8.8088F, 38.9183F));

		PartDefinition neck19_r1 = neck19.addOrReplaceChild("neck19_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 6.0F, 0.0349F, 0.0F, 0.0F));

		PartDefinition neck20_r1 = neck19.addOrReplaceChild("neck20_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.4F, 18.2F, -0.2618F, 0.0F, 0.0F));

		PartDefinition tail6_r1 = neck19.addOrReplaceChild("tail6_r1", CubeListBuilder.create().texOffs(137, 68).addBox(-5.0F, -4.7606F, 0.1868F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1F, 0.9139F, 10.6553F, -0.2269F, 0.0F, 0.0F));

		PartDefinition neck21 = tail12.addOrReplaceChild("neck21", CubeListBuilder.create(), PartPose.offset(-5.7833F, 5.0088F, 66.8183F));

		PartDefinition neck21_r1 = neck21.addOrReplaceChild("neck21_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition neck22_r1 = neck21.addOrReplaceChild("neck22_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.6F, 9.8F, -0.3665F, 0.0F, 0.0F));

		PartDefinition tail8_r1 = neck21.addOrReplaceChild("tail8_r1", CubeListBuilder.create().texOffs(137, 68).addBox(-5.0F, -4.3778F, 0.0153F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1F, 10.0681F, 1.538F, -0.3491F, 0.0F, 0.0F));

		PartDefinition tail7_r1 = neck21.addOrReplaceChild("tail7_r1", CubeListBuilder.create().texOffs(137, 69).addBox(-5.0F, -3.7284F, 1.0863F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1F, 6.1857F, -8.725F, -0.2793F, 0.0F, 0.0F));

		PartDefinition neck23 = tail12.addOrReplaceChild("neck23", CubeListBuilder.create(), PartPose.offset(-5.7833F, 18.3088F, 77.1183F));

		PartDefinition neck23_r1 = neck23.addOrReplaceChild("neck23_r1", CubeListBuilder.create().texOffs(212, 9).addBox(5.0F, -1.0F, -5.0F, 0.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 9.0F, -0.3665F, 0.0F, 0.0F));

		PartDefinition tail10_r1 = neck23.addOrReplaceChild("tail10_r1", CubeListBuilder.create().texOffs(147, 71).addBox(-4.0F, -6.0318F, -0.2134F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6F, 6.3996F, 8.442F, -0.4014F, 0.0F, 0.0F));

		PartDefinition tail9_r1 = neck23.addOrReplaceChild("tail9_r1", CubeListBuilder.create().texOffs(136, 68).addBox(-5.0F, -5.4504F, 1.4356F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1F, 0.7718F, -1.2723F, -0.4014F, 0.0F, 0.0F));

		PartDefinition Left_Wing = Mind_Dragon.addOrReplaceChild("Left_Wing", CubeListBuilder.create(), PartPose.offsetAndRotation(14.4F, 4.8F, 10.2F, -0.1762F, -0.1375F, 0.0244F));

		PartDefinition Wing_Left_Arm = Left_Wing.addOrReplaceChild("Wing_Left_Arm", CubeListBuilder.create(), PartPose.offset(0.2F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Wing_Left_Arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(85, 53).addBox(19.5F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));

		PartDefinition Wing_Left_Forearm = Left_Wing.addOrReplaceChild("Wing_Left_Forearm", CubeListBuilder.create(), PartPose.offset(40.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Wing_Left_Forearm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4257F, 0.4345F, -0.1886F));

		PartDefinition cube_r3 = Wing_Left_Forearm.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -3.5F, -8.3F, -0.4257F, 0.4345F, -0.1886F));

		PartDefinition Left_Wing_Skeleton_Far = Left_Wing.addOrReplaceChild("Left_Wing_Skeleton_Far", CubeListBuilder.create(), PartPose.offset(75.7F, -9.3F, -18.7F));

		PartDefinition cube_r4 = Left_Wing_Skeleton_Far.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(143, 53).addBox(-21.0019F, -1.6704F, -3.7652F, 23.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(28.0878F, 11.3769F, 27.7537F, -2.3184F, 1.0346F, -2.394F));

		PartDefinition cube_r5 = Left_Wing_Skeleton_Far.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(143, 53).addBox(-20.7644F, -1.5299F, -3.8625F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r6 = Left_Wing_Skeleton_Far.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(143, 53).addBox(-18.0019F, -2.6704F, -3.7652F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.1878F, 6.6769F, 13.2537F, -2.5833F, 0.8528F, -2.7243F));

		PartDefinition Left_Wing_Skeleton_far_Middle = Left_Wing.addOrReplaceChild("Left_Wing_Skeleton_far_Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(72.4F, -7.7F, -17.5F, 0.0219F, -0.7006F, 0.0715F));

		PartDefinition cube_r7 = Left_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(143, 53).addBox(20.1943F, -12.4342F, 22.3814F, 20.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.7544F, 38.745F, 64.8839F, -2.3184F, 1.0346F, -2.394F));

		PartDefinition cube_r8 = Left_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(143, 53).addBox(-20.7644F, -1.5299F, -3.8625F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.2F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r9 = Left_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(143, 53).addBox(-18.0019F, -2.6704F, -3.7652F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.1878F, 6.477F, 12.0537F, -2.5504F, 0.8338F, -2.6803F));

		PartDefinition Left_Wing_Skeleton_Middle = Left_Wing.addOrReplaceChild("Left_Wing_Skeleton_Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(43.1F, -0.1F, 1.5F, 0.0785F, -0.7327F, 0.1174F));

		PartDefinition cube_r10 = Left_Wing_Skeleton_Middle.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(143, 53).addBox(-15.0019F, -1.6704F, -3.7652F, 17.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.6878F, 7.1769F, 13.8537F, -2.5159F, 0.8768F, -2.6346F));

		PartDefinition cube_r11 = Left_Wing_Skeleton_Middle.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(143, 53).addBox(-10.7644F, -1.5299F, -3.8625F, 10.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.2F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r12 = Left_Wing_Skeleton_Middle.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(143, 53).addBox(-8.0019F, -2.6704F, -3.7652F, 10.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3878F, 4.377F, 6.7537F, -2.5504F, 0.8338F, -2.6803F));

		PartDefinition Wing_left_Claws = Left_Wing.addOrReplaceChild("Wing_left_Claws", CubeListBuilder.create(), PartPose.offset(73.1416F, -14.4F, -30.335F));

		PartDefinition Wing_left_Claw2 = Wing_left_Claws.addOrReplaceChild("Wing_left_Claw2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r13 = Wing_left_Claw2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(108, 14).addBox(-1.0108F, -0.8471F, -0.3264F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4166F, 1.2376F, -4.0F, -0.416F, 1.1141F, 2.5222F));

		PartDefinition cube_r14 = Wing_left_Claw2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(108, 9).addBox(-0.3817F, -0.9935F, -1.4134F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3636F, 1.1141F, 2.5222F));

		PartDefinition cube_r15 = Wing_left_Claw2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(143, 53).addBox(3.403F, -1.9968F, -1.8588F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3F, 7.5F, 12.8F, -2.1742F, 0.7723F, -2.1998F));

		PartDefinition cube_r16 = Wing_left_Claw2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(143, 53).addBox(0.1116F, -0.5146F, -1.3113F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 0.4F, 6.3F, -2.6672F, 1.0963F, -2.7988F));

		PartDefinition Wing_left_Claw = Wing_left_Claws.addOrReplaceChild("Wing_left_Claw", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.2192F, 0.0F, 1.1551F, 0.0F, 0.2094F, 0.0F));

		PartDefinition cube_r17 = Wing_left_Claw.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(108, 14).addBox(2.8907F, -1.1475F, -0.2628F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5381F, 1.1141F, 2.5222F));

		PartDefinition cube_r18 = Wing_left_Claw.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(108, 9).addBox(-1.0445F, -1.298F, -1.2745F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3636F, 1.1141F, 2.5222F));

		PartDefinition cube_r19 = Wing_left_Claw.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(143, 53).addBox(3.403F, -1.9968F, -1.8588F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3F, 7.5F, 12.8F, -2.1742F, 0.7723F, -2.1998F));

		PartDefinition cube_r20 = Wing_left_Claw.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(143, 53).addBox(0.1116F, -0.5146F, -1.3113F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 0.4F, 6.3F, -2.6672F, 1.0963F, -2.7988F));

		PartDefinition Left_Wing_Near_Membrane = Left_Wing.addOrReplaceChild("Left_Wing_Near_Membrane", CubeListBuilder.create(), PartPose.offsetAndRotation(20.5F, 1.3F, 4.3F, 0.0873F, 0.0F, 0.0F));

		PartDefinition bone14 = Left_Wing_Near_Membrane.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offset(18.6F, 0.6F, -2.5F));

		PartDefinition cube_r21 = bone14.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(19, 132).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r22 = bone14.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(19, 132).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, 6.2F, 16.7F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone13 = Left_Wing_Near_Membrane.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offset(8.5F, 0.0F, -2.6F));

		PartDefinition cube_r23 = bone13.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r24 = bone13.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(8, 105).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 6.8F, 18.4F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone12 = Left_Wing_Near_Membrane.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(0.1F, 6.8F, 14.7F));

		PartDefinition cube_r25 = bone12.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r26 = bone12.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4F, -6.2F, -16.7F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone11 = Left_Wing_Near_Membrane.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(-10.9F, 1.8F, 0.4F));

		PartDefinition cube_r27 = bone11.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(4, 102).addBox(-9.0893F, -1.1177F, -2.9036F, 10.0F, 2.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r28 = bone11.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(4, 102).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 6.8F, 18.4F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition Left_Wing_Middle_Membrane2 = Left_Wing.addOrReplaceChild("Left_Wing_Middle_Membrane2", CubeListBuilder.create(), PartPose.offsetAndRotation(54.9F, 0.2F, 4.3F, 0.0871F, 0.0061F, -0.0695F));

		PartDefinition bone17 = Left_Wing_Middle_Membrane2.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offset(17.3F, -3.2F, -12.9F));

		PartDefinition cube_r29 = bone17.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(19, 132).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r30 = bone17.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(19, 132).addBox(-9.3134F, -2.0764F, -2.4953F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 7.1F, 19.1F, -0.4238F, -0.1639F, 0.0602F));

		PartDefinition cube_r31 = bone17.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(25, 139).addBox(17.5454F, -11.4865F, -31.7858F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-33.2865F, 32.7915F, 50.257F, -0.4587F, -0.1639F, 0.0602F));

		PartDefinition bone16 = Left_Wing_Middle_Membrane2.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offset(7.7F, -1.4F, -8.2F));

		PartDefinition cube_r32 = bone16.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(27, 33).addBox(-8.9965F, -2.1579F, -2.8289F, 10.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r33 = bone16.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 7.0F, 18.8F, -0.4587F, -0.1639F, 0.0602F));

		PartDefinition bone15 = Left_Wing_Middle_Membrane2.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offset(-2.1F, -0.5F, -5.5F));

		PartDefinition cube_r34 = bone15.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(5, 104).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r35 = bone15.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(5, 104).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, 7.0F, 18.6F, -0.39F, -0.1803F, 0.0664F));

		PartDefinition Left_Wing_Far_Membrane = Left_Wing.addOrReplaceChild("Left_Wing_Far_Membrane", CubeListBuilder.create(), PartPose.offsetAndRotation(95.8F, -4.7F, -3.9F, 0.0522F, 0.0037F, -0.0697F));

		PartDefinition bone18 = Left_Wing_Far_Membrane.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offset(-13.0F, -1.2F, -8.5F));

		PartDefinition cube_r36 = bone18.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(5, 104).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3371F, 0.1396F, 0.0799F));

		PartDefinition cube_r37 = bone18.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(5, 104).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9F, 5.7F, 16.1F, -0.364F, 0.0462F, 0.0761F));

		PartDefinition cube_r38 = bone18.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(5, 104).addBox(25.0738F, -13.8672F, -40.3406F, 10.0F, 2.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-44.0659F, 31.9288F, 54.8357F, -0.4339F, -0.2481F, 0.1736F));

		PartDefinition bone19 = Left_Wing_Far_Membrane.addOrReplaceChild("bone19", CubeListBuilder.create(), PartPose.offset(-3.3F, 1.4F, -3.2F));

		PartDefinition cube_r39 = bone19.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3455F, 0.1163F, 0.0136F));

		PartDefinition cube_r40 = bone19.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0042F, -1.8302F, -0.8945F, 10.0F, 2.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, 6.6F, 19.3F, -0.4353F, -0.0098F, 0.0233F));

		PartDefinition bone20 = Left_Wing_Far_Membrane.addOrReplaceChild("bone20", CubeListBuilder.create(), PartPose.offset(2.8F, 5.0F, 3.4F));

		PartDefinition cube_r41 = bone20.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(19, 132).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3436F, 0.1221F, 0.0301F));

		PartDefinition cube_r42 = bone20.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(19, 132).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, 5.6F, 15.4F, -0.4483F, 0.1221F, 0.0301F));

		PartDefinition bone21 = Left_Wing_Far_Membrane.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offset(6.4F, 8.7F, 12.1F));

		PartDefinition cube_r43 = bone21.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(4, 91).addBox(-5.0F, -2.0F, -1.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2912F, 0.1221F, 0.0301F));

		PartDefinition cube_r44 = bone21.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(1, 66).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, 2.0F, 4.1F, -0.4657F, 0.1221F, 0.0301F));

		PartDefinition Right_Wing = Mind_Dragon.addOrReplaceChild("Right_Wing", CubeListBuilder.create(), PartPose.offsetAndRotation(-12.3F, 4.8F, 10.2F, 0.7434F, -0.1375F, 3.0791F));

		PartDefinition Wing_Right_Arm = Right_Wing.addOrReplaceChild("Wing_Right_Arm", CubeListBuilder.create(), PartPose.offset(0.2F, 0.0F, 0.0F));

		PartDefinition cube_r45 = Wing_Right_Arm.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(85, 53).addBox(19.5F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.384F, 0.0F, 0.0F));

		PartDefinition Wing_Right_Forearm = Right_Wing.addOrReplaceChild("Wing_Right_Forearm", CubeListBuilder.create(), PartPose.offset(40.0F, 0.0F, 0.0F));

		PartDefinition cube_r46 = Wing_Right_Forearm.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4257F, 0.4345F, -0.1886F));

		PartDefinition cube_r47 = Wing_Right_Forearm.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(85, 53).addBox(-0.3F, -4.0F, -4.0F, 20.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -3.5F, -8.3F, -0.4257F, 0.4345F, -0.1886F));

		PartDefinition Right_Wing_Skeleton_Far2 = Right_Wing.addOrReplaceChild("Right_Wing_Skeleton_Far2", CubeListBuilder.create(), PartPose.offset(75.7F, -9.3F, -18.7F));

		PartDefinition cube_r48 = Right_Wing_Skeleton_Far2.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(143, 53).addBox(-21.0019F, -1.6704F, -3.7652F, 23.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(28.0878F, 11.3769F, 27.7537F, -2.3184F, 1.0346F, -2.394F));

		PartDefinition cube_r49 = Right_Wing_Skeleton_Far2.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(143, 53).addBox(-20.7644F, -1.5299F, -3.8625F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r50 = Right_Wing_Skeleton_Far2.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(143, 53).addBox(-18.0019F, -2.6704F, -3.7652F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.1878F, 6.6769F, 13.2537F, -2.5833F, 0.8528F, -2.7243F));

		PartDefinition Right_Wing_Skeleton_far_Middle = Right_Wing.addOrReplaceChild("Right_Wing_Skeleton_far_Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(72.4F, -7.7F, -17.5F, 0.0219F, -0.7006F, 0.0715F));

		PartDefinition cube_r51 = Right_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(143, 53).addBox(-18.0019F, -1.6704F, -3.7652F, 20.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(28.0878F, 11.7769F, 26.5537F, -2.3184F, 1.0346F, -2.394F));

		PartDefinition cube_r52 = Right_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(143, 53).addBox(-20.7644F, -1.5299F, -3.8625F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.2F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r53 = Right_Wing_Skeleton_far_Middle.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(143, 53).addBox(-18.0019F, -2.6704F, -3.7652F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.1878F, 6.477F, 12.0537F, -2.5504F, 0.8338F, -2.6803F));

		PartDefinition Right_Wing_Skeleton_Middle = Right_Wing.addOrReplaceChild("Right_Wing_Skeleton_Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(43.1F, -0.1F, 1.5F, 0.0785F, -0.7327F, 0.1174F));

		PartDefinition cube_r54 = Right_Wing_Skeleton_Middle.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(143, 53).addBox(-15.0019F, -1.6704F, -3.7652F, 17.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.6878F, 7.1769F, 13.8537F, -2.5159F, 0.8768F, -2.6346F));

		PartDefinition cube_r55 = Right_Wing_Skeleton_Middle.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(143, 53).addBox(-10.7644F, -1.5299F, -3.8625F, 10.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.2F, -2.6436F, 0.6693F, -2.8163F));

		PartDefinition cube_r56 = Right_Wing_Skeleton_Middle.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(143, 53).addBox(-8.0019F, -2.6704F, -3.7652F, 10.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.3878F, 4.377F, 6.7537F, -2.5504F, 0.8338F, -2.6803F));

		PartDefinition Wing_Right_Claws = Right_Wing.addOrReplaceChild("Wing_Right_Claws", CubeListBuilder.create(), PartPose.offset(74.3541F, -6.695F, -18.2115F));

		PartDefinition Wing_Right_Claw2 = Wing_Right_Claws.addOrReplaceChild("Wing_Right_Claw2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7505F, 0.0F));

		PartDefinition cube_r57 = Wing_Right_Claw2.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(108, 14).addBox(0.6296F, 4.5237F, -0.0684F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(108, 9).addBox(-3.1787F, 4.5528F, -1.0829F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -8.4F, -10.1F, -2.0835F, 0.5943F, -2.0569F));

		PartDefinition cube_r58 = Wing_Right_Claw2.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(143, 53).addBox(3.3228F, -1.018F, -1.6221F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, -0.9F, 2.7F, -3.0166F, 1.1487F, 3.0978F));

		PartDefinition cube_r59 = Wing_Right_Claw2.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(143, 53).addBox(-0.3654F, 6.6661F, -1.6943F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, -8.0F, -3.8F, -2.3969F, 0.9851F, -2.4866F));

		PartDefinition Wing_Right_Claw = Wing_Right_Claws.addOrReplaceChild("Wing_Right_Claw", CubeListBuilder.create(), PartPose.offset(-7.6484F, -7.6817F, -11.217F));

		PartDefinition cube_r60 = Wing_Right_Claw.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(108, 14).addBox(0.2255F, 4.5677F, -0.1619F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(108, 9).addBox(-3.1787F, 4.5528F, -1.0829F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.0835F, 0.5943F, -2.0569F));

		PartDefinition cube_r61 = Wing_Right_Claw.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(143, 53).addBox(3.3228F, -1.018F, -1.6221F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3F, 7.5F, 12.8F, -3.0166F, 1.1487F, 3.0978F));

		PartDefinition cube_r62 = Wing_Right_Claw.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(143, 53).addBox(-0.3654F, 6.6661F, -1.6943F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 0.4F, 6.3F, -2.3969F, 0.9851F, -2.4866F));

		PartDefinition Right_Wing_Near_Membrane2 = Right_Wing.addOrReplaceChild("Right_Wing_Near_Membrane2", CubeListBuilder.create(), PartPose.offsetAndRotation(20.5F, 1.3F, 4.3F, 0.0873F, 0.0F, 0.0F));

		PartDefinition bone3 = Right_Wing_Near_Membrane2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(19.8F, 6.8F, 14.2F));

		PartDefinition cube_r63 = bone3.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(81, 146).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r64 = bone3.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(81, 146).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, -6.2F, -16.7F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r65 = bone3.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.7F, 0.0F, 0.5F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r66 = bone3.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.1F, -6.2F, -16.2F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone2 = Right_Wing_Near_Membrane2.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(10.5F, -0.3F, -4.1F));

		PartDefinition cube_r67 = bone2.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(1, 66).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.1F, 19.9F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r68 = bone2.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(1, 66).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.3F, 1.5F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone = Right_Wing_Near_Membrane2.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-8.9F, 8.6F, 18.8F));

		PartDefinition cube_r69 = bone.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r70 = bone.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.8F, -18.4F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition Right_Wing_Middle_Membrane3 = Right_Wing.addOrReplaceChild("Right_Wing_Middle_Membrane3", CubeListBuilder.create(), PartPose.offsetAndRotation(54.9F, 0.2F, 4.3F, 0.0871F, 0.0061F, -0.0695F));

		PartDefinition bone5 = Right_Wing_Middle_Membrane3.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(15.3F, 2.0F, -15.5F));

		PartDefinition cube_r71 = bone5.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(86, 154).addBox(-9.0152F, -10.0643F, -1.4195F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.142F, 14.6962F, 31.6041F, -0.4587F, -0.1639F, 0.0602F));

		PartDefinition cube_r72 = bone5.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(81, 146).addBox(-8.9911F, -9.8568F, -3.0567F, 10.0F, 2.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.642F, 9.2962F, 19.2041F, -0.4238F, -0.1639F, 0.0602F));

		PartDefinition cube_r73 = bone5.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(81, 146).addBox(-10.4734F, -10.0894F, -2.4005F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.142F, 2.1962F, 0.1041F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition bone6 = Right_Wing_Middle_Membrane3.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(7.7F, -1.4F, -8.2F));

		PartDefinition cube_r74 = bone6.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition cube_r75 = bone6.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 7.0F, 18.8F, -0.4587F, -0.1639F, 0.0602F));

		PartDefinition bone4 = Right_Wing_Middle_Membrane3.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-0.8F, 6.5F, 13.1F));

		PartDefinition cube_r76 = bone4.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.39F, -0.1803F, 0.0664F));

		PartDefinition cube_r77 = bone4.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, -7.0F, -18.6F, -0.3508F, 0.0984F, -0.0359F));

		PartDefinition Right_Wing_Far_Membrane2 = Right_Wing.addOrReplaceChild("Right_Wing_Far_Membrane2", CubeListBuilder.create(), PartPose.offsetAndRotation(95.8F, -4.7F, -3.9F, 0.0522F, 0.0037F, -0.0697F));

		PartDefinition bone7 = Right_Wing_Far_Membrane2.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(-11.4F, 10.9F, 24.8F));

		PartDefinition cube_r78 = bone7.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4339F, -0.2481F, 0.1736F));

		PartDefinition cube_r79 = bone7.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -6.4F, -17.2F, -0.364F, 0.0462F, 0.0761F));

		PartDefinition cube_r80 = bone7.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(6, 103).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -12.1F, -33.3F, -0.3371F, 0.1396F, 0.0799F));

		PartDefinition bone8 = Right_Wing_Far_Membrane2.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(-4.0F, 8.0F, 16.1F));

		PartDefinition cube_r81 = bone8.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(27, 33).addBox(-9.0F, -2.0F, -1.0F, 10.0F, 2.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4353F, -0.0098F, 0.0233F));

		PartDefinition cube_r82 = bone8.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(31, 33).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, -6.6F, -19.3F, -0.3455F, 0.1163F, 0.0136F));

		PartDefinition bone9 = Right_Wing_Far_Membrane2.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offset(2.8F, 5.0F, 3.4F));

		PartDefinition cube_r83 = bone9.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(1, 66).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3436F, 0.1221F, 0.0301F));

		PartDefinition cube_r84 = bone9.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(1, 66).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, 5.6F, 15.4F, -0.4483F, 0.1221F, 0.0301F));

		PartDefinition bone10 = Right_Wing_Far_Membrane2.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(6.4F, 8.7F, 12.1F));

		PartDefinition cube_r85 = bone10.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(75, 138).addBox(-5.0F, -2.0F, -1.0F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2912F, 0.1221F, 0.0301F));

		PartDefinition cube_r86 = bone10.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(81, 146).addBox(-9.0F, -2.0F, -1.0F, 7.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, 2.0F, 4.1F, -0.4657F, 0.1221F, 0.0301F));

		return LayerDefinition.create(meshdefinition, 256, 256);
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
			this.animate(this.idleAnimationState, VisionaryMythicalCreatureAnimations.idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, VisionaryMythicalCreatureAnimations.Walk, ageInTicks, 1.0F);
			capturePoseInto(this.walkPose);

			// Write the lerped result
			applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
		}
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