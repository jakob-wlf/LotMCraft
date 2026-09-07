package de.jakob.lotm.rendering.models.darkness;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.fool.FoolMythicalCreatureAnimations;
import de.jakob.lotm.rendering.models.mother.MotherMythicalCreatureAnimations;
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

public class DarknessMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "darkness_demonic_wolf"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart Main_Body;
	private final ModelPart Head;
	private final ModelPart Higher_Jaw;
	private final ModelPart Lower_Jaw;
	private final ModelPart Tentacle;
	private final ModelPart Tentacle_Start;
	private final ModelPart Tentacle_Middle;
	private final ModelPart Tentacle_End;
	private final ModelPart Tentacle3;
	private final ModelPart Tentacle_Start3;
	private final ModelPart Tentacle_Middle3;
	private final ModelPart Tentacle_End3;
	private final ModelPart Tentacle2;
	private final ModelPart Tentacle_Start2;
	private final ModelPart Tentacle_Middle2;
	private final ModelPart Tentacle_End2;
	private final ModelPart Torso;
	private final ModelPart Fur;
	private final ModelPart Tail;
	private final ModelPart Torso_Front;
	private final ModelPart Torso_Back;
	private final ModelPart Legs;
	private final ModelPart Front_pair_of_legs;
	private final ModelPart Leg;
	private final ModelPart Foreleg;
	private final ModelPart Paw;
	private final ModelPart Tight;
	private final ModelPart Leg2;
	private final ModelPart Foreleg2;
	private final ModelPart Paw2;
	private final ModelPart Tight2;
	private final ModelPart Second_Front_Pair_Of_Legs;
	private final ModelPart Leg4;
	private final ModelPart Foreleg4;
	private final ModelPart Paw4;
	private final ModelPart Tight4;
	private final ModelPart Leg3;
	private final ModelPart Foreleg3;
	private final ModelPart Paw3;
	private final ModelPart Tight3;
	private final ModelPart Secon_Back_Pair_of_Legs;
	private final ModelPart Leg5;
	private final ModelPart Foreleg5;
	private final ModelPart Paw5;
	private final ModelPart Tight5;
	private final ModelPart Leg8;
	private final ModelPart Foreleg8;
	private final ModelPart Paw8;
	private final ModelPart Tight8;
	private final ModelPart Back_Pair_Of_Legs;
	private final ModelPart Leg6;
	private final ModelPart Foreleg6;
	private final ModelPart Paw6;
	private final ModelPart Tight6;
	private final ModelPart Leg7;
	private final ModelPart Foreleg7;
	private final ModelPart Paw7;
	private final ModelPart Tight7;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();

	public DarknessMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.Main_Body = this.All.getChild("Main_Body");
		this.Head = this.Main_Body.getChild("Head");
		this.Higher_Jaw = this.Head.getChild("Higher_Jaw");
		this.Lower_Jaw = this.Head.getChild("Lower_Jaw");
		this.Tentacle = this.Main_Body.getChild("Tentacle");
		this.Tentacle_Start = this.Tentacle.getChild("Tentacle_Start");
		this.Tentacle_Middle = this.Tentacle.getChild("Tentacle_Middle");
		this.Tentacle_End = this.Tentacle.getChild("Tentacle_End");
		this.Tentacle3 = this.Main_Body.getChild("Tentacle3");
		this.Tentacle_Start3 = this.Tentacle3.getChild("Tentacle_Start3");
		this.Tentacle_Middle3 = this.Tentacle3.getChild("Tentacle_Middle3");
		this.Tentacle_End3 = this.Tentacle3.getChild("Tentacle_End3");
		this.Tentacle2 = this.Main_Body.getChild("Tentacle2");
		this.Tentacle_Start2 = this.Tentacle2.getChild("Tentacle_Start2");
		this.Tentacle_Middle2 = this.Tentacle2.getChild("Tentacle_Middle2");
		this.Tentacle_End2 = this.Tentacle2.getChild("Tentacle_End2");
		this.Torso = this.Main_Body.getChild("Torso");
		this.Fur = this.Torso.getChild("Fur");
		this.Tail = this.Torso.getChild("Tail");
		this.Torso_Front = this.Torso.getChild("Torso_Front");
		this.Torso_Back = this.Torso.getChild("Torso_Back");
		this.Legs = this.All.getChild("Legs");
		this.Front_pair_of_legs = this.Legs.getChild("Front_pair_of_legs");
		this.Leg = this.Front_pair_of_legs.getChild("Leg");
		this.Foreleg = this.Leg.getChild("Foreleg");
		this.Paw = this.Foreleg.getChild("Paw");
		this.Tight = this.Leg.getChild("Tight");
		this.Leg2 = this.Front_pair_of_legs.getChild("Leg2");
		this.Foreleg2 = this.Leg2.getChild("Foreleg2");
		this.Paw2 = this.Foreleg2.getChild("Paw2");
		this.Tight2 = this.Leg2.getChild("Tight2");
		this.Second_Front_Pair_Of_Legs = this.Legs.getChild("Second_Front_Pair_Of_Legs");
		this.Leg4 = this.Second_Front_Pair_Of_Legs.getChild("Leg4");
		this.Foreleg4 = this.Leg4.getChild("Foreleg4");
		this.Paw4 = this.Foreleg4.getChild("Paw4");
		this.Tight4 = this.Leg4.getChild("Tight4");
		this.Leg3 = this.Second_Front_Pair_Of_Legs.getChild("Leg3");
		this.Foreleg3 = this.Leg3.getChild("Foreleg3");
		this.Paw3 = this.Foreleg3.getChild("Paw3");
		this.Tight3 = this.Leg3.getChild("Tight3");
		this.Secon_Back_Pair_of_Legs = this.Legs.getChild("Secon_Back_Pair_of_Legs");
		this.Leg5 = this.Secon_Back_Pair_of_Legs.getChild("Leg5");
		this.Foreleg5 = this.Leg5.getChild("Foreleg5");
		this.Paw5 = this.Foreleg5.getChild("Paw5");
		this.Tight5 = this.Leg5.getChild("Tight5");
		this.Leg8 = this.Secon_Back_Pair_of_Legs.getChild("Leg8");
		this.Foreleg8 = this.Leg8.getChild("Foreleg8");
		this.Paw8 = this.Foreleg8.getChild("Paw8");
		this.Tight8 = this.Leg8.getChild("Tight8");
		this.Back_Pair_Of_Legs = this.Legs.getChild("Back_Pair_Of_Legs");
		this.Leg6 = this.Back_Pair_Of_Legs.getChild("Leg6");
		this.Foreleg6 = this.Leg6.getChild("Foreleg6");
		this.Paw6 = this.Foreleg6.getChild("Paw6");
		this.Tight6 = this.Leg6.getChild("Tight6");
		this.Leg7 = this.Back_Pair_Of_Legs.getChild("Leg7");
		this.Foreleg7 = this.Leg7.getChild("Foreleg7");
		this.Paw7 = this.Foreleg7.getChild("Paw7");
		this.Tight7 = this.Leg7.getChild("Tight7");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Main_Body = All.addOrReplaceChild("Main_Body", CubeListBuilder.create(), PartPose.offset(14.6F, -15.0F, -5.2F));

		PartDefinition Head = Main_Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(54, 30).addBox(7.1F, -5.7F, -2.7F, 6.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(62, 1).addBox(10.9F, -7.6F, -1.9F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(62, 1).addBox(10.9F, -7.6F, 2.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(63, 8).addBox(10.9F, -8.6F, 3.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(63, 8).addBox(10.9F, -8.6F, -1.7F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(49, 0).addBox(7.35F, -4.8F, 4.15F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(49, 0).addBox(7.45F, -4.8F, -2.55F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-42.3F, -3.5F, 3.3F));

		PartDefinition Higher_Jaw = Head.addOrReplaceChild("Higher_Jaw", CubeListBuilder.create(), PartPose.offset(6.1F, -0.9F, 3.2F));

		PartDefinition cube_r1 = Higher_Jaw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(63, 55).addBox(-1.0F, -0.5F, 3.8F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.85F, 0.0F, -3.1F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r2 = Higher_Jaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.15F, 0.5F, -3.4F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r3 = Higher_Jaw.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, 0.6F, -4.0F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r4 = Higher_Jaw.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.15F, 0.9F, -4.4F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r5 = Higher_Jaw.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(63, 55).addBox(-1.0F, -0.5F, 7.5F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 1.0F, -4.9F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r6 = Higher_Jaw.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.15F, 1.1F, 2.1F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r7 = Higher_Jaw.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.05F, 0.7F, 1.6F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r8 = Higher_Jaw.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.05F, 0.5F, 1.0F, 0.0F, 0.0F, 0.2443F));

		PartDefinition cube_r9 = Higher_Jaw.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.65F, -0.2F, -0.4F, 3.1416F, -1.5708F, -2.8972F));

		PartDefinition cube_r10 = Higher_Jaw.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.65F, -0.1F, -2.3F, 3.1416F, -1.5708F, -2.8972F));

		PartDefinition cube_r11 = Higher_Jaw.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(28, 10).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8F, -0.4F, -1.2F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r12 = Higher_Jaw.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(30, 1).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9F, 0.4F, -2.2F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r13 = Higher_Jaw.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(40, 5).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, -3.2F, 0.0F, 0.0F, 0.2618F));

		PartDefinition Lower_Jaw = Head.addOrReplaceChild("Lower_Jaw", CubeListBuilder.create(), PartPose.offset(7.8F, 2.2F, 2.2F));

		PartDefinition cube_r14 = Lower_Jaw.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.55F, -1.8F, -3.9F, 0.0F, 0.0F, 3.0194F));

		PartDefinition cube_r15 = Lower_Jaw.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.65F, -1.6F, -3.3F, 0.0F, 0.0F, 3.0194F));

		PartDefinition cube_r16 = Lower_Jaw.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.35F, -1.8F, 3.6F, -3.1416F, 0.0F, -0.1222F));

		PartDefinition cube_r17 = Lower_Jaw.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, -1.7F, 3.0F, -3.1416F, 0.0F, -0.1222F));

		PartDefinition cube_r18 = Lower_Jaw.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.85F, -1.4F, 2.7F, -3.1416F, 0.0F, -0.1222F));

		PartDefinition cube_r19 = Lower_Jaw.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(63, 55).addBox(-0.468F, 0.1399F, -1.8F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.4F, -0.5F, 0.3F, -3.1416F, 0.0F, -0.1222F));

		PartDefinition cube_r20 = Lower_Jaw.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(63, 55).addBox(-0.4681F, 0.1399F, -1.8F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.1F, -0.4F, -0.2F, -3.1416F, 0.0F, -0.1222F));

		PartDefinition cube_r21 = Lower_Jaw.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.65F, -0.9F, 0.7F, 0.0F, -1.5708F, 3.0194F));

		PartDefinition cube_r22 = Lower_Jaw.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.65F, -0.9F, -1.2F, 0.0F, -1.5708F, 3.0194F));

		PartDefinition cube_r23 = Lower_Jaw.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.65F, -1.0F, -2.1F, 0.0F, 0.0F, 3.0194F));

		PartDefinition cube_r24 = Lower_Jaw.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.65F, -1.3F, -3.1F, 0.0F, 0.0F, 3.0194F));

		PartDefinition cube_r25 = Lower_Jaw.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(63, 55).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.65F, -1.2F, -2.4F, 0.0F, 0.0F, 3.0194F));

		PartDefinition cube_r26 = Lower_Jaw.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(26, 28).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.7F, -0.6F, -0.2F, 0.0F, 0.0F, 3.002F));

		PartDefinition cube_r27 = Lower_Jaw.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(21, 18).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -0.9F, -1.2F, 0.0F, 0.0F, 3.002F));

		PartDefinition cube_r28 = Lower_Jaw.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(40, 17).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, -1.3F, -2.2F, 0.0F, 0.0F, 3.002F));

		PartDefinition Tentacle = Main_Body.addOrReplaceChild("Tentacle", CubeListBuilder.create(), PartPose.offsetAndRotation(-19.15F, -8.9F, 1.6F, 0.2793F, 0.0F, 0.0F));

		PartDefinition Tentacle_Start = Tentacle.addOrReplaceChild("Tentacle_Start", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, -4.2F, -1.0F, 0.0F, 0.0F, 0.1047F));

		PartDefinition cube_r29 = Tentacle_Start.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(73, 53).addBox(-1.2585F, -3.1164F, -1.3308F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5934F));

		PartDefinition cube_r30 = Tentacle_Start.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(73, 53).addBox(-1.5679F, -2.9305F, -1.3308F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 3.7F, 0.0F, 0.0F, 0.0F, 0.2793F));

		PartDefinition Tentacle_Middle = Tentacle.addOrReplaceChild("Tentacle_Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(2.5F, -7.7F, -0.5F, 0.0F, 0.0F, 0.4189F));

		PartDefinition cube_r31 = Tentacle_Middle.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(77, 60).addBox(0.1472F, -2.8794F, -1.3308F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0472F));

		PartDefinition cube_r32 = Tentacle_Middle.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(77, 60).addBox(0.1193F, -3.2791F, -1.3308F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -1.3F, 0.0F, 0.0F, 0.0F, 1.3963F));

		PartDefinition Tentacle_End = Tentacle.addOrReplaceChild("Tentacle_End", CubeListBuilder.create(), PartPose.offsetAndRotation(8.3862F, -6.75F, -0.5F, 0.0F, 0.0F, 0.3142F));

		PartDefinition cube_r33 = Tentacle_End.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(72, 61).addBox(1.0839F, -2.3945F, -1.3308F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5183F, -1.0352F, 0.5F, 0.0F, 0.0F, 1.6057F));

		PartDefinition cube_r34 = Tentacle_End.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(72, 61).addBox(1.1142F, -2.2986F, -1.3308F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4817F, -0.9352F, 0.5F, 0.0F, 0.0F, 1.5184F));

		PartDefinition Tentacle3 = Main_Body.addOrReplaceChild("Tentacle3", CubeListBuilder.create(), PartPose.offsetAndRotation(-19.75F, -9.7F, 9.6F, -0.2269F, 0.0F, 0.0F));

		PartDefinition Tentacle_Start3 = Tentacle3.addOrReplaceChild("Tentacle_Start3", CubeListBuilder.create(), PartPose.offset(0.5F, 0.0F, -1.0F));

		PartDefinition cube_r35 = Tentacle_Start3.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(73, 53).addBox(-1.5244F, -2.9318F, -0.7301F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, -3.4F, 0.0F, 0.0F, 0.0F, 0.4189F));

		PartDefinition cube_r36 = Tentacle_Start3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(73, 53).addBox(-1.6777F, -2.8761F, -0.7301F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, 0.3F, 0.0F, 0.0F, 0.0F, 0.2793F));

		PartDefinition Tentacle_Middle3 = Tentacle3.addOrReplaceChild("Tentacle_Middle3", CubeListBuilder.create(), PartPose.offset(2.5F, -6.9F, -0.5F));

		PartDefinition cube_r37 = Tentacle_Middle3.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(77, 60).addBox(-0.2963F, -2.0662F, -0.7301F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.2F, 0.0F, 0.0F, 0.0F, 0.6458F));

		PartDefinition cube_r38 = Tentacle_Middle3.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(77, 60).addBox(0.0509F, -2.4874F, -0.7301F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, -2.4F, 0.0F, 0.0F, 0.0F, 1.117F));

		PartDefinition Tentacle_End3 = Tentacle3.addOrReplaceChild("Tentacle_End3", CubeListBuilder.create(), PartPose.offset(5.8F, -10.5F, -0.5F));

		PartDefinition cube_r39 = Tentacle_End3.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(72, 61).addBox(0.5489F, -0.9676F, -0.7301F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -1.1F, 0.5F, 0.0F, 0.0F, 0.4887F));

		PartDefinition cube_r40 = Tentacle_End3.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(72, 61).addBox(1.0418F, -1.4692F, -0.7301F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -0.5F, 0.5F, 0.0F, 0.0F, 1.0996F));

		PartDefinition Tentacle2 = Main_Body.addOrReplaceChild("Tentacle2", CubeListBuilder.create(), PartPose.offset(-20.15F, -9.9F, 5.6F));

		PartDefinition Tentacle_Start2 = Tentacle2.addOrReplaceChild("Tentacle_Start2", CubeListBuilder.create(), PartPose.offset(0.5F, -0.9F, -0.5F));

		PartDefinition cube_r41 = Tentacle_Start2.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(73, 53).addBox(-2.0F, -4.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.1F, -0.5F, 0.0F, 0.0F, 0.5934F));

		PartDefinition cube_r42 = Tentacle_Start2.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(73, 53).addBox(-2.0F, -4.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 2.6F, -0.5F, 0.0F, 0.0F, 0.2793F));

		PartDefinition Tentacle_Middle2 = Tentacle2.addOrReplaceChild("Tentacle_Middle2", CubeListBuilder.create(), PartPose.offset(3.1F, -5.9F, -0.7F));

		PartDefinition cube_r43 = Tentacle_Middle2.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(77, 60).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.4F, 0.2F, 0.0F, 0.0F, 1.0472F));

		PartDefinition cube_r44 = Tentacle_Middle2.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(77, 60).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -0.9F, 0.2F, 0.0F, 0.0F, 1.3963F));

		PartDefinition Tentacle_End2 = Tentacle2.addOrReplaceChild("Tentacle_End2", CubeListBuilder.create(), PartPose.offset(7.9F, -7.3F, -0.7F));

		PartDefinition cube_r45 = Tentacle_End2.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(72, 61).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -0.6F, 0.7F, 0.0F, 0.0F, 1.3614F));

		PartDefinition cube_r46 = Tentacle_End2.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(72, 61).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -0.5F, 0.7F, 0.0F, 0.0F, 1.5184F));

		PartDefinition Torso = Main_Body.addOrReplaceChild("Torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Fur = Torso.addOrReplaceChild("Fur", CubeListBuilder.create().texOffs(84, 48).addBox(-0.7F, -0.8F, -5.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(-0.7F, -0.8F, -2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-5.3F, -0.8F, -5.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-5.3F, -0.8F, -1.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-5.3F, -0.8F, 3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-11.9F, -3.1F, 3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-11.9F, -3.1F, -5.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(12.8F, -0.8F, 1.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(8.8F, -0.8F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(81, 45).addBox(-1.2F, -0.8F, 1.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(4.0F, -0.8F, -0.2F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(6.0F, -0.8F, 3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(2.5F, -0.8F, 5.9F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(13.0F, -0.8F, 6.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(8.0F, -0.8F, 6.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(5.7F, -0.8F, -5.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(13.0F, -0.8F, -5.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(13.0F, -0.8F, -2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.05F, -9.9F, 4.0F));

		PartDefinition Tail = Torso.addOrReplaceChild("Tail", CubeListBuilder.create(), PartPose.offset(2.3F, -4.7F, 4.0F));

		PartDefinition cube_r47 = Tail.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(71, 1).addBox(-6.0F, -3.0F, -1.0F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 5.5F, 0.0F, 0.0F, 0.0F, 1.2043F));

		PartDefinition cube_r48 = Tail.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(75, 1).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, 0.3F, 0.0F, 0.0F, 0.0F, 0.4887F));

		PartDefinition cube_r49 = Tail.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(73, 0).addBox(-3.0F, -4.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 0.3F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition Torso_Front = Torso.addOrReplaceChild("Torso_Front", CubeListBuilder.create().texOffs(86, 39).addBox(-15.4F, -12.6F, -1.5F, 8.0F, 12.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(76, 15).addBox(-13.0F, -10.0F, -1.0F, 14.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-14.0F, 1.2F, 0.0F));

		PartDefinition Torso_Back = Torso.addOrReplaceChild("Torso_Back", CubeListBuilder.create().texOffs(76, 15).addBox(-1.0895F, -8.5376F, -6.0F, 14.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, -0.3F, 5.0F));

		PartDefinition Legs = All.addOrReplaceChild("Legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Front_pair_of_legs = Legs.addOrReplaceChild("Front_pair_of_legs", CubeListBuilder.create(), PartPose.offset(-8.7F, -14.4F, -1.0F));

		PartDefinition Leg = Front_pair_of_legs.addOrReplaceChild("Leg", CubeListBuilder.create(), PartPose.offset(-0.9F, -0.1F, -4.0F));

		PartDefinition Foreleg = Leg.addOrReplaceChild("Foreleg", CubeListBuilder.create(), PartPose.offset(-0.6F, 6.9F, 0.2F));

		PartDefinition cube_r50 = Foreleg.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, 4.8F, 0.2F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw = Foreleg.addOrReplaceChild("Paw", CubeListBuilder.create().texOffs(122, 7).addBox(-4.8F, 0.0F, 2.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.8F, 0.0F, 0.9F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.8F, 0.0F, -0.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-3.6F, -1.0F, -0.6F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 6.6F, -1.2F));

		PartDefinition Tight = Leg.addOrReplaceChild("Tight", CubeListBuilder.create().texOffs(109, 7).addBox(-1.8F, 0.9F, -2.1F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.3F, 1.2F, -1.6F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8F, -1.0F, 0.5F));

		PartDefinition Leg2 = Front_pair_of_legs.addOrReplaceChild("Leg2", CubeListBuilder.create(), PartPose.offset(-1.5F, -0.1F, 5.9F));

		PartDefinition Foreleg2 = Leg2.addOrReplaceChild("Foreleg2", CubeListBuilder.create(), PartPose.offset(-0.6F, 6.7F, -0.5F));

		PartDefinition cube_r51 = Foreleg2.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 5.0F, 0.1F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw2 = Foreleg2.addOrReplaceChild("Paw2", CubeListBuilder.create().texOffs(122, 7).addBox(-3.7F, 0.7F, 1.1F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.7F, 0.7F, -0.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.7F, 0.7F, -1.9F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-2.5F, -0.3F, -1.9F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.3F, 6.1F, 0.0F));

		PartDefinition Tight2 = Leg2.addOrReplaceChild("Tight2", CubeListBuilder.create().texOffs(109, 7).addBox(-1.7F, -0.2F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.2F, 0.1F, -1.8F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.3F, 0.1F, -0.1F));

		PartDefinition Second_Front_Pair_Of_Legs = Legs.addOrReplaceChild("Second_Front_Pair_Of_Legs", CubeListBuilder.create(), PartPose.offset(-2.5F, -13.7F, -0.3F));

		PartDefinition Leg4 = Second_Front_Pair_Of_Legs.addOrReplaceChild("Leg4", CubeListBuilder.create(), PartPose.offset(-0.4F, -0.6F, 5.2F));

		PartDefinition Foreleg4 = Leg4.addOrReplaceChild("Foreleg4", CubeListBuilder.create(), PartPose.offset(-0.8F, 6.9F, -0.2F));

		PartDefinition cube_r52 = Foreleg4.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 4.6F, -0.2F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw4 = Foreleg4.addOrReplaceChild("Paw4", CubeListBuilder.create().texOffs(122, 7).addBox(-4.15F, 0.725F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.15F, 0.725F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.15F, 0.725F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-2.95F, -0.275F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 5.675F, -0.2F));

		PartDefinition Tight4 = Leg4.addOrReplaceChild("Tight4", CubeListBuilder.create().texOffs(109, 7).addBox(-2.2F, -0.7F, -1.8F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.7F, -0.4F, -1.6F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.4F, 0.4F, -0.3F));

		PartDefinition Leg3 = Second_Front_Pair_Of_Legs.addOrReplaceChild("Leg3", CubeListBuilder.create(), PartPose.offset(-0.7F, -1.1F, -5.2F));

		PartDefinition Foreleg3 = Leg3.addOrReplaceChild("Foreleg3", CubeListBuilder.create(), PartPose.offset(-0.5F, 7.4F, 1.0F));

		PartDefinition cube_r53 = Foreleg3.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 4.6F, -0.1F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw3 = Foreleg3.addOrReplaceChild("Paw3", CubeListBuilder.create().texOffs(122, 7).addBox(-4.05F, 0.625F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.05F, 0.625F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.05F, 0.625F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-2.85F, -0.375F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.35F, 5.775F, -0.1F));

		PartDefinition Tight3 = Leg3.addOrReplaceChild("Tight3", CubeListBuilder.create().texOffs(109, 7).addBox(-1.1F, 0.3F, -1.9F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-0.6F, 0.6F, -1.4F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.2F, -0.1F, 0.8F));

		PartDefinition Secon_Back_Pair_of_Legs = Legs.addOrReplaceChild("Secon_Back_Pair_of_Legs", CubeListBuilder.create(), PartPose.offset(6.5F, -13.4F, -0.3F));

		PartDefinition Leg5 = Secon_Back_Pair_of_Legs.addOrReplaceChild("Leg5", CubeListBuilder.create(), PartPose.offset(0.7F, -0.9F, 4.8F));

		PartDefinition Foreleg5 = Leg5.addOrReplaceChild("Foreleg5", CubeListBuilder.create(), PartPose.offset(0.0F, 6.6F, 1.4F));

		PartDefinition cube_r54 = Foreleg5.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 4.9F, -1.4F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw5 = Foreleg5.addOrReplaceChild("Paw5", CubeListBuilder.create().texOffs(122, 7).addBox(-3.9F, 1.3F, 0.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.9F, 1.3F, -1.2F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.9F, 1.3F, -2.7F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-2.7F, 0.3F, -2.7F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1F, 5.4F, -0.7F));

		PartDefinition Tight5 = Leg5.addOrReplaceChild("Tight5", CubeListBuilder.create().texOffs(109, 7).addBox(-2.0F, -0.4F, -2.2F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.5F, -0.1F, -1.7F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.4F, 0.1F, 0.2F));

		PartDefinition Leg8 = Secon_Back_Pair_of_Legs.addOrReplaceChild("Leg8", CubeListBuilder.create(), PartPose.offset(0.0F, -1.1F, -4.7F));

		PartDefinition Foreleg8 = Leg8.addOrReplaceChild("Foreleg8", CubeListBuilder.create(), PartPose.offset(0.2F, 7.1F, 0.6F));

		PartDefinition cube_r55 = Foreleg8.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, 4.6F, -0.2F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw8 = Foreleg8.addOrReplaceChild("Paw8", CubeListBuilder.create().texOffs(122, 7).addBox(-4.5F, 0.3F, 1.9F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.5F, 0.3F, 0.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.5F, 0.3F, -1.1F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-3.3F, -0.7F, -1.1F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.1F, -1.1F));

		PartDefinition Tight8 = Leg8.addOrReplaceChild("Tight8", CubeListBuilder.create().texOffs(109, 7).addBox(-1.9F, -0.5F, -2.3F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.4F, -0.2F, -1.8F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 0.4F, 0.7F));

		PartDefinition Back_Pair_Of_Legs = Legs.addOrReplaceChild("Back_Pair_Of_Legs", CubeListBuilder.create(), PartPose.offset(13.9F, -14.2F, -0.2F));

		PartDefinition Leg6 = Back_Pair_Of_Legs.addOrReplaceChild("Leg6", CubeListBuilder.create(), PartPose.offset(-0.2F, -0.3F, 5.1F));

		PartDefinition Foreleg6 = Leg6.addOrReplaceChild("Foreleg6", CubeListBuilder.create(), PartPose.offset(-0.5F, 6.8F, 1.0F));

		PartDefinition cube_r56 = Foreleg6.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 4.9F, -1.4F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw6 = Foreleg6.addOrReplaceChild("Paw6", CubeListBuilder.create().texOffs(122, 7).addBox(-3.9F, 1.0F, 1.1F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.9F, 1.0F, -0.4F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-3.9F, 1.0F, -1.9F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-2.7F, 0.0F, -1.9F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.2F, 5.7F, -1.5F));

		PartDefinition Tight6 = Leg6.addOrReplaceChild("Tight6", CubeListBuilder.create().texOffs(109, 7).addBox(-2.2F, -0.4F, -2.2F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.7F, -0.1F, -1.7F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 0.3F, -0.2F));

		PartDefinition Leg7 = Back_Pair_Of_Legs.addOrReplaceChild("Leg7", CubeListBuilder.create(), PartPose.offset(-0.2F, -0.3F, -4.6F));

		PartDefinition Foreleg7 = Leg7.addOrReplaceChild("Foreleg7", CubeListBuilder.create(), PartPose.offset(-0.2F, 7.0F, 0.7F));

		PartDefinition cube_r57 = Foreleg7.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(102, 0).addBox(-1.0F, -5.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 4.7F, -0.5F, 0.0F, 0.0F, 0.1571F));

		PartDefinition Paw7 = Foreleg7.addOrReplaceChild("Paw7", CubeListBuilder.create().texOffs(122, 7).addBox(-4.9F, 1.0F, 0.9F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.9F, 1.0F, -0.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 7).addBox(-4.9F, 1.0F, -2.1F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 0).addBox(-3.7F, 0.0F, -2.1F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 5.5F, -0.4F));

		PartDefinition Tight7 = Leg7.addOrReplaceChild("Tight7", CubeListBuilder.create().texOffs(109, 7).addBox(-1.9F, -0.1F, -1.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 0).addBox(-1.4F, 0.2F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, 0.0F, -0.3F));

		return LayerDefinition.create(meshdefinition, 128, 128);
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
			this.animate(this.idleAnimationState, DarknessMythicalCreatureAnimations.Idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, DarknessMythicalCreatureAnimations.walk, ageInTicks, 1.0F);
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