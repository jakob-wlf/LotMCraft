package de.jakob.lotm.rendering.models.mother;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.door.DoorHighMythicalCreatureAnimations;
import de.jakob.lotm.rendering.models.fool.FoolMythicalCreatureAnimations;
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

public class MotherMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	private final AnimationState idleAnimationState = new AnimationState();
	private final AnimationState walkAnimationState = new AnimationState();

	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mother_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart Creeper;
	private final ModelPart head;
	private final ModelPart body2;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;
	private final ModelPart Pillager2;
	private final ModelPart right_leg2;
	private final ModelPart left_leg2;
	private final ModelPart right_arm2;
	private final ModelPart left_arm2;
	private final ModelPart body3;
	private final ModelPart nose2;
	private final ModelPart Pillager;
	private final ModelPart right_leg;
	private final ModelPart left_leg;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart body;
	private final ModelPart nose;
	private final ModelPart Main_Body;
	private final ModelPart mushrooms_top;
	private final ModelPart Crops;
	private final ModelPart Carrots;
	private final ModelPart Wheat;
	private final ModelPart Beetroot;
	private final ModelPart Birth_Gate4;
	private final ModelPart Door1Gate7;
	private final ModelPart Door1Gate8;
	private final ModelPart Birth_Gate3;
	private final ModelPart Door1Gate5;
	private final ModelPart Door1Gate6;
	private final ModelPart Birth_Gate2;
	private final ModelPart Door1Gate3;
	private final ModelPart Door1Gate4;
	private final ModelPart Birth_Gate1;
	private final ModelPart Door1Gate2;
	private final ModelPart Door1Gate1;
	private final ModelPart Body4;

	public MotherMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.Creeper = this.All.getChild("Creeper");
		this.head = this.Creeper.getChild("head");
		this.body2 = this.Creeper.getChild("body2");
		this.leg1 = this.Creeper.getChild("leg1");
		this.leg2 = this.Creeper.getChild("leg2");
		this.leg3 = this.Creeper.getChild("leg3");
		this.leg4 = this.Creeper.getChild("leg4");
		this.Pillager2 = this.All.getChild("Pillager2");
		this.right_leg2 = this.Pillager2.getChild("right_leg2");
		this.left_leg2 = this.Pillager2.getChild("left_leg2");
		this.right_arm2 = this.Pillager2.getChild("right_arm2");
		this.left_arm2 = this.Pillager2.getChild("left_arm2");
		this.body3 = this.Pillager2.getChild("body3");
		this.nose2 = this.Pillager2.getChild("nose2");
		this.Pillager = this.All.getChild("Pillager");
		this.right_leg = this.Pillager.getChild("right_leg");
		this.left_leg = this.Pillager.getChild("left_leg");
		this.right_arm = this.Pillager.getChild("right_arm");
		this.left_arm = this.Pillager.getChild("left_arm");
		this.body = this.Pillager.getChild("body");
		this.nose = this.Pillager.getChild("nose");
		this.Main_Body = this.All.getChild("Main_Body");
		this.mushrooms_top = this.Main_Body.getChild("mushrooms_top");
		this.Crops = this.Main_Body.getChild("Crops");
		this.Carrots = this.Crops.getChild("Carrots");
		this.Wheat = this.Crops.getChild("Wheat");
		this.Beetroot = this.Crops.getChild("Beetroot");
		this.Birth_Gate4 = this.Main_Body.getChild("Birth_Gate4");
		this.Door1Gate7 = this.Birth_Gate4.getChild("Door1Gate7");
		this.Door1Gate8 = this.Birth_Gate4.getChild("Door1Gate8");
		this.Birth_Gate3 = this.Main_Body.getChild("Birth_Gate3");
		this.Door1Gate5 = this.Birth_Gate3.getChild("Door1Gate5");
		this.Door1Gate6 = this.Birth_Gate3.getChild("Door1Gate6");
		this.Birth_Gate2 = this.Main_Body.getChild("Birth_Gate2");
		this.Door1Gate3 = this.Birth_Gate2.getChild("Door1Gate3");
		this.Door1Gate4 = this.Birth_Gate2.getChild("Door1Gate4");
		this.Birth_Gate1 = this.Main_Body.getChild("Birth_Gate1");
		this.Door1Gate2 = this.Birth_Gate1.getChild("Door1Gate2");
		this.Door1Gate1 = this.Birth_Gate1.getChild("Door1Gate1");
		this.Body4 = this.Main_Body.getChild("Body4");
	}

	public static LayerDefinition createBodyLayer() {

		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(-0.8375F, 15.725F, 0.4625F));

		PartDefinition Creeper = All.addOrReplaceChild("Creeper", CubeListBuilder.create(), PartPose.offset(-3.2625F, -2.625F, 1.4375F));

		PartDefinition head = Creeper.addOrReplaceChild("head", CubeListBuilder.create().texOffs(59, 6).addBox(0.0F, 2.9F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition body2 = Creeper.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(71, 0).addBox(0.0F, 6.9F, -2.0F, 4.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition leg1 = Creeper.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(63, 1).addBox(2.0F, 2.9F, -4.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 4.0F, 4.0F));

		PartDefinition leg2 = Creeper.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(63, 1).addBox(0.0F, 2.9F, -4.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 4.0F, 4.0F));

		PartDefinition leg3 = Creeper.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(63, 1).addBox(2.0F, 2.9F, 0.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 4.0F, -4.0F));

		PartDefinition leg4 = Creeper.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(63, 1).addBox(0.0F, 2.9F, 0.1F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 4.0F, -4.0F));

		PartDefinition Pillager2 = All.addOrReplaceChild("Pillager2", CubeListBuilder.create(), PartPose.offset(0.5375F, -5.325F, 5.5375F));

		PartDefinition right_leg2 = Pillager2.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(39, 11).addBox(-1.4F, -0.1F, -1.1F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.6F, 7.1F, -2.0F));

		PartDefinition left_leg2 = Pillager2.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(39, 11).mirror().addBox(-1.0F, -0.1F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.2F, 7.1F, -2.1F));

		PartDefinition right_arm2 = Pillager2.addOrReplaceChild("right_arm2", CubeListBuilder.create().texOffs(48, 0).addBox(-2.1F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.3F, 2.5F, -2.1F));

		PartDefinition left_arm2 = Pillager2.addOrReplaceChild("left_arm2", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-0.2F, -0.4F, -1.4F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.4F, 2.4F, -1.7F));

		PartDefinition body3 = Pillager2.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(47, 11).addBox(0.0F, -17.0F, -3.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 19.0F, -0.1F));

		PartDefinition nose2 = Pillager2.addOrReplaceChild("nose2", CubeListBuilder.create().texOffs(39, 4).addBox(-1.4F, -3.6F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(42, 2).addBox(-0.3F, -1.5F, -2.1F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.1F, 1.6F, -2.2F));

		PartDefinition Pillager = All.addOrReplaceChild("Pillager", CubeListBuilder.create(), PartPose.offset(0.9375F, -4.925F, -0.4625F));

		PartDefinition right_leg = Pillager.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(39, 11).addBox(-1.2F, -0.2F, -1.2F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.4F, 7.2F, -1.9F));

		PartDefinition left_leg = Pillager.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(39, 11).mirror().addBox(-1.2F, -0.1F, -1.1F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.4F, 7.1F, -2.0F));

		PartDefinition right_arm = Pillager.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-1.8F, -0.4F, -1.1F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.4F, -2.0F));

		PartDefinition left_arm = Pillager.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(0.0F, -0.2F, -1.1F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.2F, 2.2F, -2.0F));

		PartDefinition body = Pillager.addOrReplaceChild("body", CubeListBuilder.create().texOffs(47, 11).addBox(0.0F, -17.0F, -3.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2F, 19.0F, -0.1F));

		PartDefinition nose = Pillager.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(39, 4).addBox(-1.4F, -3.6F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(42, 2).addBox(-0.3F, -1.5F, -2.1F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.1F, 1.6F, -2.2F));

		PartDefinition Main_Body = All.addOrReplaceChild("Main_Body", CubeListBuilder.create(), PartPose.offset(5.7875F, 8.075F, 4.2875F));

		PartDefinition mushrooms_top = Main_Body.addOrReplaceChild("mushrooms_top", CubeListBuilder.create().texOffs(46, 17).addBox(1.6F, -33.6F, 3.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(0.6F, -33.6F, 4.5F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(46, 17).addBox(0.4F, -33.6F, -2.6F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(-0.6F, -33.6F, -1.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.45F, 14.7F, -4.75F));

		PartDefinition mushrooms_top_r1 = mushrooms_top.addOrReplaceChild("mushrooms_top_r1", CubeListBuilder.create().texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -23.2F, 0.6F, 0.0F, 1.5708F, 1.5882F));

		PartDefinition mushrooms_top_r2 = mushrooms_top.addOrReplaceChild("mushrooms_top_r2", CubeListBuilder.create().texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.5F, -21.5F, -2.5F, 0.0F, 1.5708F, -1.5708F));

		PartDefinition mushrooms_top_r3 = mushrooms_top.addOrReplaceChild("mushrooms_top_r3", CubeListBuilder.create().texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.5F, -17.4F, -0.3F, 0.0F, 1.5708F, -1.5708F));

		PartDefinition mushrooms_top_r4 = mushrooms_top.addOrReplaceChild("mushrooms_top_r4", CubeListBuilder.create().texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.4F, -17.8F, 10.7F, -1.5708F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r5 = mushrooms_top.addOrReplaceChild("mushrooms_top_r5", CubeListBuilder.create().texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4F, -22.3F, 10.7F, -1.5708F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r6 = mushrooms_top.addOrReplaceChild("mushrooms_top_r6", CubeListBuilder.create().texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, -32.5F, 1.0F, 3.1416F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r7 = mushrooms_top.addOrReplaceChild("mushrooms_top_r7", CubeListBuilder.create().texOffs(50, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(50, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, -23.5F, -9.6F, 1.5708F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r8 = mushrooms_top.addOrReplaceChild("mushrooms_top_r8", CubeListBuilder.create().texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -26.5F, -9.6F, 1.5708F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r9 = mushrooms_top.addOrReplaceChild("mushrooms_top_r9", CubeListBuilder.create().texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -20.5F, -9.6F, 1.5708F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r10 = mushrooms_top.addOrReplaceChild("mushrooms_top_r10", CubeListBuilder.create().texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6F, -13.4F, -2.4F, 0.0F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r11 = mushrooms_top.addOrReplaceChild("mushrooms_top_r11", CubeListBuilder.create().texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -13.4F, 3.4F, 0.0F, 0.0F, -3.1416F));

		PartDefinition mushrooms_top_r12 = mushrooms_top.addOrReplaceChild("mushrooms_top_r12", CubeListBuilder.create().texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -20.5F, 2.9F, 0.0F, 0.0F, 1.5708F));

		PartDefinition mushrooms_top_r13 = mushrooms_top.addOrReplaceChild("mushrooms_top_r13", CubeListBuilder.create().texOffs(46, 17).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 19).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -26.6F, 4.5F, 0.0F, 0.0F, 1.5708F));

		PartDefinition Crops = Main_Body.addOrReplaceChild("Crops", CubeListBuilder.create(), PartPose.offset(0.0F, -1.8F, 2.1F));

		PartDefinition Carrots = Crops.addOrReplaceChild("Carrots", CubeListBuilder.create().texOffs(45, 22).addBox(-4.6F, -12.9F, 15.25F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-4.6F, -12.9F, 14.25F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-4.6F, -12.9F, 13.15F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.3F, -4.3F, -15.65F));

		PartDefinition cube_r1 = Carrots.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Carrots.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Carrots.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.2F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Carrots.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, 1.3F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Carrots.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, 0.3F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r6 = Carrots.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, 2.5F, 0.0F, 1.5882F, 0.0F, 0.0F));

		PartDefinition cube_r7 = Carrots.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-2.0F, -2.0F, 0.1F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-2.0F, -2.0F, 1.1F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.6F, -0.2F, 10.05F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r8 = Carrots.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(45, 22).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 22).addBox(-2.0F, -2.0F, -3.1F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.6F, -3.4F, 12.55F, 0.0F, 0.0F, -1.5708F));

		PartDefinition Wheat = Crops.addOrReplaceChild("Wheat", CubeListBuilder.create().texOffs(45, 21).addBox(-3.0F, -5.7F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-4.8F, -5.0F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-6.1F, -5.6F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-6.1F, -9.4F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-5.1F, -8.5F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-3.3F, -9.3F, 3.1F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r9 = Wheat.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -13.3F, -9.8F, 1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r10 = Wheat.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -11.3F, -9.9F, 1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r11 = Wheat.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7F, -12.3F, -9.4F, 1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r12 = Wheat.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(-1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(2.9F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.7F, -16.2F, -10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r13 = Wheat.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.7F, -16.2F, -9.2F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r14 = Wheat.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(45, 21).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(45, 21).addBox(3.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.8F, -16.2F, -9.3F, 1.5708F, 0.0F, 0.0F));

		PartDefinition Beetroot = Crops.addOrReplaceChild("Beetroot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r15 = Beetroot.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(45, 27).addBox(-1.8F, -1.5F, 0.5F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.2F, -1.5F, 1.6F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-1.6F, -1.5F, 2.5F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.9F, -9.7F, -16.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r16 = Beetroot.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, -4.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -1.0F, -6.4F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r17 = Beetroot.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, -4.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4F, -1.0F, -7.3F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r18 = Beetroot.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, -4.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, -1.0F, -8.4F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r19 = Beetroot.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, 2.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 2.2F, -6.7F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r20 = Beetroot.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, 2.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1F, 2.2F, -7.6F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r21 = Beetroot.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(45, 27).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(45, 27).addBox(-2.0F, -2.0F, 2.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7F, 2.2F, -8.7F, 0.0F, 0.0F, -3.1416F));

		PartDefinition Birth_Gate4 = Main_Body.addOrReplaceChild("Birth_Gate4", CubeListBuilder.create().texOffs(0, 49).addBox(-2.0F, -4.6F, 10.0F, 4.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.45F, -8.8F, -2.15F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Door1Gate7 = Birth_Gate4.addOrReplaceChild("Door1Gate7", CubeListBuilder.create(), PartPose.offset(2.5F, 0.0F, 11.3F));

		PartDefinition cube_r22 = Door1Gate7.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(13, 50).addBox(6.1637F, -4.6F, 0.6498F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4F, 0.0F, -10.3F, 0.0F, -1.6057F, 0.0F));

		PartDefinition Door1Gate8 = Birth_Gate4.addOrReplaceChild("Door1Gate8", CubeListBuilder.create(), PartPose.offset(-2.5F, 0.0F, 11.2F));

		PartDefinition cube_r23 = Door1Gate8.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(13, 50).addBox(-11.0F, -4.6F, -3.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 0.0F, -11.2F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Birth_Gate3 = Main_Body.addOrReplaceChild("Birth_Gate3", CubeListBuilder.create().texOffs(0, 49).addBox(0.0F, -4.6F, 0.0F, 4.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.15F, -9.1F, 2.05F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Door1Gate5 = Birth_Gate3.addOrReplaceChild("Door1Gate5", CubeListBuilder.create(), PartPose.offset(3.9F, 0.0F, 1.0F));

		PartDefinition cube_r24 = Door1Gate5.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(13, 50).addBox(-3.9F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.6057F, 0.0F));

		PartDefinition Door1Gate6 = Birth_Gate3.addOrReplaceChild("Door1Gate6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r25 = Door1Gate6.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(13, 50).addBox(-1.0F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Birth_Gate2 = Main_Body.addOrReplaceChild("Birth_Gate2", CubeListBuilder.create().texOffs(0, 49).addBox(0.0F, -4.6F, 0.0F, 4.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.45F, -8.5F, -6.55F, 0.0F, -1.5533F, 0.0F));

		PartDefinition Door1Gate3 = Birth_Gate2.addOrReplaceChild("Door1Gate3", CubeListBuilder.create(), PartPose.offset(3.9F, 0.0F, 1.0F));

		PartDefinition cube_r26 = Door1Gate3.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(13, 50).addBox(-3.9F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.6057F, 0.0F));

		PartDefinition Door1Gate4 = Birth_Gate2.addOrReplaceChild("Door1Gate4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r27 = Door1Gate4.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(13, 50).addBox(-1.0F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Birth_Gate1 = Main_Body.addOrReplaceChild("Birth_Gate1", CubeListBuilder.create().texOffs(0, 49).addBox(0.0F, -4.6F, 0.0F, 4.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.95F, -8.5F, -9.65F));

		PartDefinition Door1Gate2 = Birth_Gate1.addOrReplaceChild("Door1Gate2", CubeListBuilder.create(), PartPose.offset(3.9F, 0.0F, 1.0F));

		PartDefinition cube_r28 = Door1Gate2.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(13, 50).addBox(-3.9F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.6057F, 0.0F));

		PartDefinition Door1Gate1 = Birth_Gate1.addOrReplaceChild("Door1Gate1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r29 = Door1Gate1.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(13, 50).addBox(-1.0F, -4.6F, -1.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Body4 = Main_Body.addOrReplaceChild("Body4", CubeListBuilder.create().texOffs(0, 19).addBox(-13.05F, -15.8F, -11.55F, 15.0F, 15.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(2, 2).addBox(-12.0F, -14.7F, -13.5F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(2, 2).addBox(-12.0F, -14.8F, 1.1F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r30 = Body4.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(2, 2).addBox(-12.0F, -13.0F, -1.0F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -1.8F, 1.3F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r31 = Body4.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(2, 2).addBox(-12.0F, -13.0F, -1.0F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6F, -1.7F, 1.3F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r32 = Body4.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(2, 2).addBox(-12.0F, -13.0F, -1.0F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.6F, -10.6F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r33 = Body4.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(2, 2).addBox(-12.0F, -13.0F, -1.0F, 13.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -16.0F, -10.6F, -1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
	                      float ageInTicks, float netHeadYaw, float headPitch) {

		this.root().getAllParts().forEach(ModelPart::resetPose);

		boolean isWalking = limbSwingAmount > 0.01F;

		if (isWalking) {
			this.idleAnimationState.stop();
			this.walkAnimationState.startIfStopped((int) ageInTicks);
			this.animate(this.walkAnimationState, MotherMythicalCreatureAnimations.WALK, ageInTicks, 1.0F);
		} else {
			this.walkAnimationState.stop();
			this.idleAnimationState.startIfStopped((int) ageInTicks);
			this.animate(this.idleAnimationState, MotherMythicalCreatureAnimations.IDLE, ageInTicks, 1.0F);
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