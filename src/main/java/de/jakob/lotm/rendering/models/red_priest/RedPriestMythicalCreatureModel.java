package de.jakob.lotm.rendering.models.red_priest;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.visionary.VisionaryMythicalCreatureAnimations;
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

public class RedPriestMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "red_priest_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Calamity_giant;
	private final ModelPart leg_right;
	private final ModelPart foot_right;
	private final ModelPart right_leg_fire;
	private final ModelPart leg_left;
	private final ModelPart left_leg_fire;
	private final ModelPart foot_left;
	private final ModelPart Waist;
	private final ModelPart arm_right;
	private final ModelPart right_arm_fire;
	private final ModelPart Forearm_right;
	private final ModelPart Cape_forearm;
	private final ModelPart tassel;
	private final ModelPart tassel2;
	private final ModelPart cape;
	private final ModelPart tassel3;
	private final ModelPart arm_left;
	private final ModelPart forearm_left;
	private final ModelPart left_forearm_fire;
	private final ModelPart fireball;
	private final ModelPart left_arm_fire;
	private final ModelPart body;
	private final ModelPart body_fire;
	private final ModelPart tassels;
	private final ModelPart l_tassels;
	private final ModelPart r_tassels;
	private final ModelPart head;
	private final ModelPart headflames;
	private final ModelPart skull;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();

	public RedPriestMythicalCreatureModel (ModelPart root) {
		this.root = root;
		this.Calamity_giant = root.getChild("Calamity_giant");
		this.leg_right = this.Calamity_giant.getChild("leg_right");
		this.foot_right = this.leg_right.getChild("foot_right");
		this.right_leg_fire = this.leg_right.getChild("right_leg_fire");
		this.leg_left = this.Calamity_giant.getChild("leg_left");
		this.left_leg_fire = this.leg_left.getChild("left_leg_fire");
		this.foot_left = this.leg_left.getChild("foot_left");
		this.Waist = this.Calamity_giant.getChild("Waist");
		this.arm_right = this.Waist.getChild("arm_right");
		this.right_arm_fire = this.arm_right.getChild("right_arm_fire");
		this.Forearm_right = this.arm_right.getChild("Forearm_right");
		this.Cape_forearm = this.Forearm_right.getChild("Cape_forearm");
		this.tassel = this.Cape_forearm.getChild("tassel");
		this.tassel2 = this.Cape_forearm.getChild("tassel2");
		this.cape = this.arm_right.getChild("cape");
		this.tassel3 = this.cape.getChild("tassel3");
		this.arm_left = this.Waist.getChild("arm_left");
		this.forearm_left = this.arm_left.getChild("forearm_left");
		this.left_forearm_fire = this.forearm_left.getChild("left_forearm_fire");
		this.fireball = this.left_forearm_fire.getChild("fireball");
		this.left_arm_fire = this.arm_left.getChild("left_arm_fire");
		this.body = this.Waist.getChild("body");
		this.body_fire = this.body.getChild("body_fire");
		this.tassels = this.body.getChild("tassels");
		this.l_tassels = this.tassels.getChild("l_tassels");
		this.r_tassels = this.tassels.getChild("r_tassels");
		this.head = this.Waist.getChild("head");
		this.headflames = this.head.getChild("headflames");
		this.skull = this.head.getChild("skull");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Calamity_giant = partdefinition.addOrReplaceChild("Calamity_giant", CubeListBuilder.create(), PartPose.offset(-4.5411F, 16.2077F, 4.5646F));

		PartDefinition leg_right = Calamity_giant.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, -9.5F, -1.3F, -0.187F, 0.2124F, 0.0916F));

		PartDefinition tassel_r1 = leg_right.addOrReplaceChild("tassel_r1", CubeListBuilder.create().texOffs(48, 18).addBox(1.4293F, -6.7321F, 0.9341F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8908F, 11.1258F, 2.0412F, -1.0446F, 1.3825F, -1.5501F));

		PartDefinition tassel_r2 = leg_right.addOrReplaceChild("tassel_r2", CubeListBuilder.create().texOffs(48, 18).addBox(-4.5821F, -2.8994F, 0.4422F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8908F, 11.1258F, 2.0412F, -1.1321F, 0.752F, -1.4555F));

		PartDefinition right_leg_r1 = leg_right.addOrReplaceChild("right_leg_r1", CubeListBuilder.create().texOffs(38, 16).addBox(-4.4878F, -5.81F, -0.9452F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1613F, 11.7825F, -0.3014F, 0.3845F, -0.0044F, 0.0522F));

		PartDefinition right_leg_r2 = leg_right.addOrReplaceChild("right_leg_r2", CubeListBuilder.create().texOffs(25, 14).addBox(-2.0F, -9.5F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0303F, 9.3418F, -0.6871F, -0.1309F, 0.0F, 0.0F));

		PartDefinition foot_right = leg_right.addOrReplaceChild("foot_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5788F, 14.4833F, 1.3031F, -0.12F, -0.0313F, -0.1361F));

		PartDefinition right_leg_r3 = foot_right.addOrReplaceChild("right_leg_r3", CubeListBuilder.create().texOffs(35, 13).addBox(-4.4878F, 3.19F, -3.9452F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7401F, -2.7008F, -1.6046F, 0.3845F, -0.0044F, 0.0522F));

		PartDefinition right_leg_fire = leg_right.addOrReplaceChild("right_leg_fire", CubeListBuilder.create(), PartPose.offset(0.1779F, 12.7138F, 0.1751F));

		PartDefinition right_leg_r4 = right_leg_fire.addOrReplaceChild("right_leg_r4", CubeListBuilder.create().texOffs(28, 115).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.757F, -0.0044F, 0.0522F));

		PartDefinition right_leg_r5 = right_leg_fire.addOrReplaceChild("right_leg_r5", CubeListBuilder.create().texOffs(6, 115).addBox(-0.5F, -8.0F, -0.5F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2083F, -3.372F, -0.8622F, -0.1309F, 0.0F, 0.0F));

		PartDefinition leg_left = Calamity_giant.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offsetAndRotation(6.7519F, -8.7833F, -1.1075F, -0.3385F, -0.5306F, -0.0073F));

		PartDefinition tassel_r3 = leg_left.addOrReplaceChild("tassel_r3", CubeListBuilder.create().texOffs(48, 18).addBox(-3.5256F, -6.9405F, 1.148F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5041F, 9.2942F, 0.2504F, -0.2553F, -1.2797F, 0.7635F));

		PartDefinition tassel_r4 = leg_left.addOrReplaceChild("tassel_r4", CubeListBuilder.create().texOffs(48, 18).addBox(2.9244F, -3.1265F, -0.4234F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5041F, 9.2942F, 0.2504F, -0.8884F, -0.7228F, 1.4975F));

		PartDefinition left_leg_r1 = leg_left.addOrReplaceChild("left_leg_r1", CubeListBuilder.create().texOffs(38, 16).mirror().addBox(1.4864F, -5.8398F, -1.0797F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0999F, 11.045F, -0.9765F, 0.3821F, 0.0049F, -0.0594F));

		PartDefinition left_leg_r2 = leg_left.addOrReplaceChild("left_leg_r2", CubeListBuilder.create().texOffs(25, 14).mirror().addBox(-2.0F, -8.5F, -3.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0497F, 8.6043F, -0.4861F, -0.1309F, 0.0F, 0.0F));

		PartDefinition left_leg_fire = leg_left.addOrReplaceChild("left_leg_fire", CubeListBuilder.create(), PartPose.offset(0.0497F, 8.6043F, -0.4861F));

		PartDefinition left_leg_r3 = left_leg_fire.addOrReplaceChild("left_leg_r3", CubeListBuilder.create().texOffs(6, 115).mirror().addBox(-1.5F, -8.0F, -1.5F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition left_leg_r4 = left_leg_fire.addOrReplaceChild("left_leg_r4", CubeListBuilder.create().texOffs(17, 114).mirror().addBox(-1.0F, -5.5F, -0.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.3407F, 2.3981F, -0.4385F, -2.7595F, 0.0049F, -0.0594F));

		PartDefinition foot_left = leg_left.addOrReplaceChild("foot_left", CubeListBuilder.create(), PartPose.offsetAndRotation(0.6001F, 14.145F, 0.1235F, -0.2618F, 0.0F, 0.0F));

		PartDefinition left_leg_r5 = foot_left.addOrReplaceChild("left_leg_r5", CubeListBuilder.create().texOffs(36, 13).mirror().addBox(2.4864F, 3.1602F, -4.0797F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.7F, -3.0F, -1.1F, 0.6875F, 0.0049F, -0.0594F));

		PartDefinition Waist = Calamity_giant.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offsetAndRotation(4.5027F, -8.6231F, -3.0464F, 0.0873F, 0.0F, 0.0F));

		PartDefinition arm_right = Waist.addOrReplaceChild("arm_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -12.7F, 4.0F, -0.5561F, -0.6194F, 1.5067F));

		PartDefinition right_arm_r1 = arm_right.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(10, 14).addBox(-13.2763F, -5.2725F, -5.7291F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7278F, 9.9075F, 2.5777F, -0.4297F, -0.002F, 0.3388F));

		PartDefinition right_arm_fire = arm_right.addOrReplaceChild("right_arm_fire", CubeListBuilder.create(), PartPose.offset(-0.2623F, 4.4778F, -0.7214F));

		PartDefinition right_arm_r2 = right_arm_fire.addOrReplaceChild("right_arm_r2", CubeListBuilder.create().texOffs(34, 114).addBox(-12.7332F, -4.2725F, -5.2291F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.99F, 5.4297F, 3.2991F, -0.4297F, -0.002F, 0.3388F));

		PartDefinition Forearm_right = arm_right.addOrReplaceChild("Forearm_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.4879F, 6.7804F, -2.8981F, 0.9599F, 0.0F, 0.0F));

		PartDefinition right_arm_r3 = Forearm_right.addOrReplaceChild("right_arm_r3", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -5.0F, -1.0F, 3.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8649F, -2.3372F, -3.2965F, -2.5297F, 0.3117F, 0.0557F));

		PartDefinition right_arm_r4 = Forearm_right.addOrReplaceChild("right_arm_r4", CubeListBuilder.create().texOffs(17, 116).addBox(-1.0F, -4.5F, -0.5F, 2.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1437F, -2.3515F, -3.3413F, 0.5394F, 0.29F, 0.1056F));

		PartDefinition right_arm_r5 = Forearm_right.addOrReplaceChild("right_arm_r5", CubeListBuilder.create().texOffs(0, 111).addBox(-13.3494F, 12.1515F, 0.4234F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.6F, 7.734F, 0.5234F, -2.5185F, 0.2341F, 0.3642F));

		PartDefinition Cape_forearm = Forearm_right.addOrReplaceChild("Cape_forearm", CubeListBuilder.create(), PartPose.offset(-0.0472F, -2.7896F, -2.9263F));

		PartDefinition tassel_r5 = Cape_forearm.addOrReplaceChild("tassel_r5", CubeListBuilder.create().texOffs(106, 88).addBox(-5.882F, -1.2301F, 0.9125F, 11.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 1.0F, -9.0F, 2.6847F, 0.125F, -1.4251F));

		PartDefinition tassel_r6 = Cape_forearm.addOrReplaceChild("tassel_r6", CubeListBuilder.create().texOffs(106, 88).addBox(-5.882F, -1.2301F, 0.9125F, 11.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, 10.0F, -5.0F, 2.5499F, 1.0377F, -1.6284F));

		PartDefinition tassel = Cape_forearm.addOrReplaceChild("tassel", CubeListBuilder.create(), PartPose.offsetAndRotation(11.6F, 1.5F, -9.9F, 0.0F, 0.5236F, 0.0F));

		PartDefinition tassel_r7 = tassel.addOrReplaceChild("tassel_r7", CubeListBuilder.create().texOffs(40, 112).addBox(-5.0509F, 0.7573F, 0.999F, 11.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -0.5F, 0.9F, -2.8926F, -0.0887F, 1.629F));

		PartDefinition tassel2 = Cape_forearm.addOrReplaceChild("tassel2", CubeListBuilder.create(), PartPose.offsetAndRotation(11.7F, 11.3F, -5.5F, 0.0F, 0.1745F, 0.0F));

		PartDefinition tassel_r8 = tassel2.addOrReplaceChild("tassel_r8", CubeListBuilder.create().texOffs(40, 112).addBox(-5.0509F, 0.7572F, 0.999F, 11.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -1.3F, 0.5F, -2.6417F, -0.9853F, 1.3653F));

		PartDefinition cape = arm_right.addOrReplaceChild("cape", CubeListBuilder.create(), PartPose.offset(-0.5958F, 5.307F, -0.8255F));

		PartDefinition tassel_r9 = cape.addOrReplaceChild("tassel_r9", CubeListBuilder.create().texOffs(106, 108).addBox(-2.645F, -2.586F, -0.9839F, 11.0F, 20.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3236F, 4.6005F, 3.4032F, -0.0368F, 0.4456F, 1.9149F));

		PartDefinition tassel3 = cape.addOrReplaceChild("tassel3", CubeListBuilder.create(), PartPose.offset(11.8236F, 7.9005F, 1.5032F));

		PartDefinition tassel_r10 = tassel3.addOrReplaceChild("tassel_r10", CubeListBuilder.create().texOffs(40, 112).addBox(-8.528F, 2.4166F, -0.3848F, 11.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -3.3F, 1.9F, -0.1814F, -0.4456F, -1.2267F));

		PartDefinition arm_left = Waist.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(10, 14).mirror().addBox(-1.502F, -0.3335F, -1.6153F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.315F, -11.7463F, 4.0068F, 0.1027F, -0.4598F, -0.4379F));

		PartDefinition forearm_left = arm_left.addOrReplaceChild("forearm_left", CubeListBuilder.create(), PartPose.offset(-0.3233F, 8.3623F, 0.4631F));

		PartDefinition left_arm_r1 = forearm_left.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(0, 111).mirror().addBox(-1.6787F, -1.5F, -0.9783F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 6.7F, -6.0F, -0.6939F, 0.084F, 0.1005F));

		PartDefinition left_arm_r2 = forearm_left.addOrReplaceChild("left_arm_r2", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.5F, -4.5F, -1.0F, 3.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.3213F, 2.8042F, -2.0783F, -0.6939F, 0.084F, 0.1005F));

		PartDefinition tassel_r11 = forearm_left.addOrReplaceChild("tassel_r11", CubeListBuilder.create().texOffs(48, 18).addBox(0.7377F, -5.2167F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0787F, 4.7268F, 1.828F, -1.5708F, 1.3963F, 1.6144F));

		PartDefinition tassel_r12 = forearm_left.addOrReplaceChild("tassel_r12", CubeListBuilder.create().texOffs(48, 18).addBox(-2.5569F, -5.4319F, -0.1602F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0787F, 4.7268F, 1.828F, -1.6007F, 0.0419F, 1.5361F));

		PartDefinition tassel_r13 = forearm_left.addOrReplaceChild("tassel_r13", CubeListBuilder.create().texOffs(48, 18).addBox(-0.9384F, -4.7044F, 0.0027F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0787F, 4.7268F, 1.828F, -1.6074F, 0.6973F, 1.5575F));

		PartDefinition left_forearm_fire = forearm_left.addOrReplaceChild("left_forearm_fire", CubeListBuilder.create(), PartPose.offset(0.2235F, 3.242F, -2.4296F));

		PartDefinition left_arm_r3 = left_forearm_fire.addOrReplaceChild("left_arm_r3", CubeListBuilder.create().texOffs(10, 116).mirror().addBox(-1.0F, -4.5F, -0.5F, 2.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.4477F, 0.084F, 0.1005F));

		PartDefinition fireball = left_forearm_fire.addOrReplaceChild("fireball", CubeListBuilder.create(), PartPose.offset(-0.2235F, 4.458F, -3.5704F));

		PartDefinition left_arm_r4 = fireball.addOrReplaceChild("left_arm_r4", CubeListBuilder.create().texOffs(28, 112).mirror().addBox(-2.6787F, -2.5F, -1.9783F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6939F, 0.084F, 0.1005F));

		PartDefinition left_arm_fire = arm_left.addOrReplaceChild("left_arm_fire", CubeListBuilder.create().texOffs(20, 114).mirror().addBox(-2.0F, -4.0F, -1.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.998F, 4.6665F, -0.1153F));

		PartDefinition body = Waist.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0384F, -0.6089F, 2.0731F, -0.1745F, 0.0F, 0.0F));

		PartDefinition tassel_r14 = body.addOrReplaceChild("tassel_r14", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, -15.9468F, -3.4005F, -2.6345F, -0.3567F, 2.1319F));

		PartDefinition tassel_r15 = body.addOrReplaceChild("tassel_r15", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -15.9468F, -3.4005F, -0.5071F, -0.3567F, 1.0096F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(90, 2).addBox(-4.0F, -5.5F, -2.0F, 8.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.3694F, -1.8068F, 0.1745F, 0.0F, 0.0F));

		PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(56, 0).addBox(-5.0F, -8.5F, -3.0F, 10.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.3694F, -2.8068F, -0.2182F, 0.0F, 0.0F));

		PartDefinition body_fire = body.addOrReplaceChild("body_fire", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0514F, -1.4012F));

		PartDefinition body_r3 = body_fire.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(7, 114).addBox(-3.5F, -4.4208F, -1.9056F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -2.9671F, 0.0F, 0.0F));

		PartDefinition body_r4 = body_fire.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(0, 112).addBox(-4.0F, -4.5747F, -2.0694F, 9.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -9.946F, -0.1362F, -0.2182F, 0.0F, 0.0F));

		PartDefinition tassels = body.addOrReplaceChild("tassels", CubeListBuilder.create(), PartPose.offset(0.0F, 17.1306F, -2.8068F));

		PartDefinition l_tassels = tassels.addOrReplaceChild("l_tassels", CubeListBuilder.create(), PartPose.offsetAndRotation(4.1F, -24.4107F, 1.4063F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tassel_r16 = l_tassels.addOrReplaceChild("tassel_r16", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6997F, -1.6667F, 5.537F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition tassel_r17 = l_tassels.addOrReplaceChild("tassel_r17", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6997F, 1.3333F, 3.537F, -1.5708F, 0.48F, 1.5708F));

		PartDefinition tassel_r18 = l_tassels.addOrReplaceChild("tassel_r18", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6997F, 3.3333F, 1.537F, -1.5708F, 0.7854F, 1.5708F));

		PartDefinition r_tassels = tassels.addOrReplaceChild("r_tassels", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.1F, -23.5F, -0.5F, 0.0F, -0.3927F, 0.0F));

		PartDefinition tassel_r19 = r_tassels.addOrReplaceChild("tassel_r19", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5774F, 6.9063F, -1.5708F, 0.0F, -1.5708F));

		PartDefinition tassel_r20 = r_tassels.addOrReplaceChild("tassel_r20", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4226F, 4.9063F, -1.5708F, -0.48F, -1.5708F));

		PartDefinition tassel_r21 = r_tassels.addOrReplaceChild("tassel_r21", CubeListBuilder.create().texOffs(48, 18).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.4226F, 2.9063F, -1.5708F, -0.7854F, -1.5708F));

		PartDefinition head = Waist.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0293F, -16.3711F, 3.268F, 1.0155F, 0.133F, -0.1243F));

		PartDefinition headflames = head.addOrReplaceChild("headflames", CubeListBuilder.create().texOffs(12, 111).addBox(-2.3646F, -8.7203F, -2.4813F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.1885F, -2.5197F, 1.0202F, -1.0908F, 0.0F, 0.0F));

		PartDefinition headwear_r1 = headflames.addOrReplaceChild("headwear_r1", CubeListBuilder.create().texOffs(27, 111).addBox(-3.5F, -4.5F, -3.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.6354F, -6.2203F, -1.4813F, 0.1745F, 0.0F, 0.0F));

		PartDefinition headwear_r2 = headflames.addOrReplaceChild("headwear_r2", CubeListBuilder.create().texOffs(25, 111).addBox(-2.5F, -2.5F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-3.3287F, -4.3804F, -0.3955F, 0.0877F, -0.2022F, -0.5855F));

		PartDefinition headwear_r3 = headflames.addOrReplaceChild("headwear_r3", CubeListBuilder.create().texOffs(25, 111).addBox(-2.5F, -1.5F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.5938F, -4.4986F, 0.1681F, -0.0883F, 0.2325F, 0.5475F));

		PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3239F, -0.1776F, -0.2558F, -0.1309F, 0.0F, 0.0F));

		PartDefinition tassel_r22 = skull.addOrReplaceChild("tassel_r22", CubeListBuilder.create().texOffs(48, 18).addBox(0.0472F, -5.3647F, 1.2045F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.7615F, -0.658F, -2.7402F, -0.5025F, 2.1761F));

		PartDefinition tassel_r23 = skull.addOrReplaceChild("tassel_r23", CubeListBuilder.create().texOffs(48, 18).addBox(0.0472F, -5.3647F, -1.2045F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.7615F, -0.658F, -0.4014F, -0.5025F, 0.9655F));

		PartDefinition head_r1 = skull.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -2.0F, -3.0F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -3.6842F, 2.9357F, 0.2618F, 0.0F, 0.0F));

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
			this.animate(this.idleAnimationState, RedPriestMythicalCreatureAnimations.Idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, RedPriestMythicalCreatureAnimations.walk, ageInTicks, 1.0F);
			capturePoseInto(this.walkPose);

			// Write the lerped result
			applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Calamity_giant.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}