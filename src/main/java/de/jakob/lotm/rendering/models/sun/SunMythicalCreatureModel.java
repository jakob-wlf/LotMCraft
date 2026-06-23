package de.jakob.lotm.rendering.models.sun;// Made with Blockbench 5.0.7
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

public class SunMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sun_mythical_creature"), "main");

	private final AnimationState walkAnimationState = new AnimationState();
	private final AnimationState idleAnimationState = new AnimationState();

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart feather;
	private final ModelPart headfeather;
	private final ModelPart body;
	private final ModelPart wings;
	private final ModelPart l_wing;
	private final ModelPart r_wing;
	private final ModelPart wings2;
	private final ModelPart l_wing2;
	private final ModelPart r_wing2;
	private final ModelPart legs;
	private final ModelPart l_leg;
	private final ModelPart r_leg;
	private final ModelPart tailfeather;
	private final ModelPart l_tailfeather;
	private final ModelPart l_tailfeather2;
	private final ModelPart l_tailfeather3;
	private final ModelPart r_tailfeather;
	private final ModelPart r_tailfeather2;
	private final ModelPart r_tailfeather3;

	public SunMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.feather = this.head.getChild("feather");
		this.headfeather = this.feather.getChild("headfeather");
		this.body = root.getChild("body");
		this.wings = root.getChild("wings");
		this.l_wing = this.wings.getChild("l_wing");
		this.r_wing = this.wings.getChild("r_wing");
		this.wings2 = this.wings.getChild("wings2");
		this.l_wing2 = this.wings2.getChild("l_wing2");
		this.r_wing2 = this.wings2.getChild("r_wing2");
		this.legs = root.getChild("legs");
		this.l_leg = this.legs.getChild("l_leg");
		this.r_leg = this.legs.getChild("r_leg");
		this.tailfeather = root.getChild("tailfeather");
		this.l_tailfeather = this.tailfeather.getChild("l_tailfeather");
		this.l_tailfeather2 = this.l_tailfeather.getChild("l_tailfeather2");
		this.l_tailfeather3 = this.l_tailfeather2.getChild("l_tailfeather3");
		this.r_tailfeather = this.tailfeather.getChild("r_tailfeather");
		this.r_tailfeather2 = this.r_tailfeather.getChild("r_tailfeather2");
		this.r_tailfeather3 = this.r_tailfeather2.getChild("r_tailfeather3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 0).addBox(-0.4286F, -0.6806F, 0.383F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 0).addBox(-0.4286F, -0.9306F, -1.567F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-0.572F, 6.8381F, -6.807F, 0.2719F, -0.0171F, 0.0016F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-0.4286F, 0.3194F, -1.567F, -0.2618F, 0.0F, 0.0F));

		PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(23, 1).addBox(0.0F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-0.4286F, -0.1586F, -2.3113F, 0.3054F, 0.0F, 0.0F));

		PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4286F, -1.1806F, 1.383F, 0.2182F, 0.0F, 0.0F));

		PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(29, 0).addBox(-0.5F, -1.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4286F, 0.8194F, 1.883F, -0.1745F, 0.0F, 0.0F));

		PartDefinition feather = head.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(4, -2).addBox(0.5F, -4.861F, 0.1115F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4286F, -0.6806F, 0.383F, -0.2618F, 0.0F, 0.0F));

		PartDefinition feather_r1 = feather.addOrReplaceChild("feather_r1", CubeListBuilder.create().texOffs(4, -2).addBox(0.0F, -1.7982F, -0.7629F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -2.7627F, -0.6756F, 0.2618F, 0.0F, 0.0F));

		PartDefinition feather_r2 = feather.addOrReplaceChild("feather_r2", CubeListBuilder.create().texOffs(4, -2).addBox(0.0F, -2.5F, -1.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.7627F, -1.6756F, 0.48F, 0.0F, 0.0F));

		PartDefinition feather_r3 = feather.addOrReplaceChild("feather_r3", CubeListBuilder.create().texOffs(0, -2).addBox(0.5F, -3.161F, -2.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -3.0F, 0.829F, 0.0F, 0.0F));

		PartDefinition headfeather = feather.addOrReplaceChild("headfeather", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 2.0F));

		PartDefinition feather_r4 = headfeather.addOrReplaceChild("feather_r4", CubeListBuilder.create().texOffs(8, -2).addBox(1.6F, -4.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(8, -2).addBox(3.4F, -4.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -1.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition feather_r5 = headfeather.addOrReplaceChild("feather_r5", CubeListBuilder.create().texOffs(8, -2).addBox(1.6F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(8, -2).addBox(3.4F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 1.0F, -0.6545F, 0.0F, 0.0F));

		PartDefinition head_r5 = headfeather.addOrReplaceChild("head_r5", CubeListBuilder.create().texOffs(8, 2).addBox(1.41F, -0.5F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(8, 2).addBox(-0.41F, -0.5F, -2.0F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -1.0F, 1.1345F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2925F, 12.6601F, -0.0782F, 0.8727F, 0.0F, 0.0F));

		PartDefinition body_r1 = body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(22, 10).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0537F, 3.7755F, 1.5187F, 0.5083F, 0.2552F, -0.3621F));

		PartDefinition body_r2 = body.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(14, 10).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9463F, 3.7755F, 1.5187F, 0.5541F, -0.1096F, 0.2382F));

		PartDefinition body_r3 = body.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2925F, -0.1615F, 0.0817F, 0.2618F, 0.0F, 0.0F));

		PartDefinition body_r4 = body.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(0, 12).addBox(-2.474F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2665F, -2.6465F, -0.741F, 0.0466F, 0.0468F, -0.0225F));

		PartDefinition body_r5 = body.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(0, 7).addBox(-2.5F, -0.7F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2925F, -5.6615F, 0.0817F, -0.2182F, 0.0F, 0.0F));

		PartDefinition wings = partdefinition.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0077F, 11.2968F, 2.0211F, 0.8727F, 0.0F, 0.0F));

		PartDefinition l_wing = wings.addOrReplaceChild("l_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(9.3879F, -5.7968F, 0.2733F, 0.0F, -0.0873F, 0.0F));

		PartDefinition l_wing_r1 = l_wing.addOrReplaceChild("l_wing_r1", CubeListBuilder.create().texOffs(28, 26).addBox(-0.5F, -9.5F, -5.5F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8112F, -3.1F, 1.0F, -1.5708F, -1.1781F, 2.7053F));

		PartDefinition l_wing_r2 = l_wing.addOrReplaceChild("l_wing_r2", CubeListBuilder.create().texOffs(28, 11).addBox(-0.5F, -3.5F, -6.5F, 1.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1888F, -1.1F, 0.0F, 0.0F, -1.5708F, 0.6981F));

		PartDefinition l_wing_r3 = l_wing.addOrReplaceChild("l_wing_r3", CubeListBuilder.create().texOffs(36, 0).addBox(-0.5F, -2.5F, -4.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1888F, 1.9F, 0.0F, 1.5708F, -1.5272F, -0.4363F));

		PartDefinition r_wing = wings.addOrReplaceChild("r_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.8802F, -0.8064F, 1.5478F, 0.0F, 0.2182F, 0.0F));

		PartDefinition r_wing_r1 = r_wing.addOrReplaceChild("r_wing_r1", CubeListBuilder.create().texOffs(46, 26).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4195F, -8.0903F, 1.1274F, 1.5708F, -1.1345F, 0.4363F));

		PartDefinition r_wing_r2 = r_wing.addOrReplaceChild("r_wing_r2", CubeListBuilder.create().texOffs(46, 11).addBox(-0.5F, -3.5F, -6.5F, 1.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5222F, -6.0903F, -0.0637F, 0.0F, -1.5708F, 2.4435F));

		PartDefinition r_wing_r3 = r_wing.addOrReplaceChild("r_wing_r3", CubeListBuilder.create().texOffs(50, 0).addBox(-0.5F, -2.5F, -4.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5222F, -3.0903F, -0.0637F, -1.5708F, -1.5272F, -2.6616F));

		PartDefinition wings2 = wings.addOrReplaceChild("wings2", CubeListBuilder.create(), PartPose.offset(1.4923F, 11.6032F, -1.8211F));

		PartDefinition l_wing2 = wings2.addOrReplaceChild("l_wing2", CubeListBuilder.create(), PartPose.offsetAndRotation(6.4766F, -15.2931F, 3.796F, -0.0105F, -0.2981F, 0.2246F));

		PartDefinition l_wing_r4 = l_wing2.addOrReplaceChild("l_wing_r4", CubeListBuilder.create().texOffs(28, 26).addBox(-0.5F, -9.5F, -5.5F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9621F, -3.3198F, 0.2202F, -1.5708F, -1.1781F, 2.7053F));

		PartDefinition l_wing_r5 = l_wing2.addOrReplaceChild("l_wing_r5", CubeListBuilder.create().texOffs(28, 11).addBox(-0.5F, -3.5F, -6.5F, 1.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0379F, -1.3198F, -0.7798F, 0.0F, -1.5708F, 0.6981F));

		PartDefinition l_wing_r6 = l_wing2.addOrReplaceChild("l_wing_r6", CubeListBuilder.create().texOffs(36, 0).addBox(-0.5F, -2.5F, -4.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0379F, 1.6802F, -0.7798F, 1.5708F, -1.5272F, -0.4363F));

		PartDefinition r_wing2 = wings2.addOrReplaceChild("r_wing2", CubeListBuilder.create(), PartPose.offsetAndRotation(-11.3726F, -15.4097F, 4.3688F, -0.0424F, 0.4256F, -0.2359F));

		PartDefinition r_wing_r4 = r_wing2.addOrReplaceChild("r_wing_r4", CubeListBuilder.create().texOffs(46, 26).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1006F, -3.3539F, 0.8591F, 1.5708F, -1.1345F, 0.4363F));

		PartDefinition r_wing_r5 = r_wing2.addOrReplaceChild("r_wing_r5", CubeListBuilder.create().texOffs(46, 11).addBox(-0.5F, -3.5F, -6.5F, 1.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8411F, -1.3539F, -0.3319F, 0.0F, -1.5708F, 2.4435F));

		PartDefinition r_wing_r6 = r_wing2.addOrReplaceChild("r_wing_r6", CubeListBuilder.create().texOffs(50, 0).addBox(-0.5F, -2.5F, -4.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8411F, 1.6461F, -0.3319F, -1.5708F, -1.5272F, -2.6616F));

		PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1206F, 13.684F, 7.5F, 1.1345F, 0.0F, 0.0F));

		PartDefinition l_leg = legs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(18, 16).addBox(-0.1206F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -2.013F, -0.158F, -0.2747F));

		PartDefinition r_leg_r1 = l_leg.addOrReplaceChild("r_leg_r1", CubeListBuilder.create().texOffs(18, 16).addBox(-0.1206F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition r_leg = legs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(18, 16).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -2.168F, -0.0823F, 0.4758F));

		PartDefinition r_leg_r2 = r_leg.addOrReplaceChild("r_leg_r2", CubeListBuilder.create().texOffs(18, 16).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, -0.5F, -0.6109F, 0.0F, 0.0F));

		PartDefinition tailfeather = partdefinition.addOrReplaceChild("tailfeather", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.8628F, 11.3031F, 3.9107F, 1.6581F, 0.0F, 0.0F));

		PartDefinition l_tailfeather = tailfeather.addOrReplaceChild("l_tailfeather", CubeListBuilder.create(), PartPose.offsetAndRotation(3.8628F, -1.9088F, -0.2729F, -0.6863F, -0.1396F, -0.1682F));

		PartDefinition feather_r6 = l_tailfeather.addOrReplaceChild("feather_r6", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4242F, 7.6622F, 4.1955F, -3.0006F, 0.3193F, 2.8609F));

		PartDefinition feather_r7 = l_tailfeather.addOrReplaceChild("feather_r7", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -4.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4242F, 3.6622F, 4.1955F, 2.7285F, 0.4104F, 2.2016F));

		PartDefinition feather_r8 = l_tailfeather.addOrReplaceChild("feather_r8", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4242F, 0.6622F, 1.1955F, 2.2253F, 0.0F, 1.5708F));

		PartDefinition l_tailfeather2 = l_tailfeather.addOrReplaceChild("l_tailfeather2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.8239F, 8.4493F, 1.7272F, -0.5265F, -0.1679F, 0.3162F));

		PartDefinition feather_r9 = l_tailfeather2.addOrReplaceChild("feather_r9", CubeListBuilder.create().texOffs(10, 42).addBox(0.0F, -3.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9633F, 7.1475F, 0.7529F, 3.0694F, 0.2181F, -2.9253F));

		PartDefinition feather_r10 = l_tailfeather2.addOrReplaceChild("feather_r10", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9633F, 2.1475F, 0.7529F, -3.0006F, 0.3193F, 2.8609F));

		PartDefinition feather_r11 = l_tailfeather2.addOrReplaceChild("feather_r11", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -4.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9633F, -1.8525F, 0.7529F, 2.7285F, 0.4104F, 2.2016F));

		PartDefinition feather_r12 = l_tailfeather2.addOrReplaceChild("feather_r12", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0367F, -4.8525F, -2.2471F, 2.2253F, 0.0F, 1.5708F));

		PartDefinition l_tailfeather3 = l_tailfeather2.addOrReplaceChild("l_tailfeather3", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.7696F, 0.1429F, 1.7114F, -0.0388F, -0.2726F, 0.1699F));

		PartDefinition feather_r13 = l_tailfeather3.addOrReplaceChild("feather_r13", CubeListBuilder.create().texOffs(10, 42).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7043F, 7.1727F, 1.7672F, 3.0694F, 0.2181F, -2.9253F));

		PartDefinition feather_r14 = l_tailfeather3.addOrReplaceChild("feather_r14", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5208F, 2.9493F, 1.8235F, -3.0006F, 0.3193F, 2.8609F));

		PartDefinition feather_r15 = l_tailfeather3.addOrReplaceChild("feather_r15", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -4.8F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, -1.0507F, 1.8235F, 2.7285F, 0.4104F, 2.2016F));

		PartDefinition feather_r16 = l_tailfeather3.addOrReplaceChild("feather_r16", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4792F, -4.0507F, -1.1765F, 2.2253F, 0.0F, 1.5708F));

		PartDefinition r_tailfeather = tailfeather.addOrReplaceChild("r_tailfeather", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.8628F, 1.9088F, 0.2729F, 0.0873F, 0.0F, 0.1745F));

		PartDefinition feather_r17 = r_tailfeather.addOrReplaceChild("feather_r17", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4079F, 3.9767F, -0.3559F, -1.0183F, 0.329F, -0.1368F));

		PartDefinition feather_r18 = r_tailfeather.addOrReplaceChild("feather_r18", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4079F, -0.0233F, 1.6441F, 0.2294F, 0.5107F, 0.7246F));

		PartDefinition feather_r19 = r_tailfeather.addOrReplaceChild("feather_r19", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1497F, -3.1554F, -0.3504F, 1.105F, 0.7231F, 1.6167F));

		PartDefinition r_tailfeather2 = r_tailfeather.addOrReplaceChild("r_tailfeather2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.0F, -2.0F, -0.7037F, 0.13F, -0.0026F));

		PartDefinition feather_r20 = r_tailfeather2.addOrReplaceChild("feather_r20", CubeListBuilder.create().texOffs(10, 42).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5921F, 8.9766F, -1.3559F, -0.9162F, 0.3986F, -1.1068F));

		PartDefinition feather_r21 = r_tailfeather2.addOrReplaceChild("feather_r21", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4079F, 4.9766F, 1.6441F, -0.5755F, 0.8781F, -0.4094F));

		PartDefinition feather_r22 = r_tailfeather2.addOrReplaceChild("feather_r22", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5921F, -0.0233F, 1.6441F, 0.4421F, 0.7785F, 0.6635F));

		PartDefinition feather_r23 = r_tailfeather2.addOrReplaceChild("feather_r23", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1497F, -3.1554F, -0.3504F, 1.105F, 0.7231F, 1.6167F));

		PartDefinition r_tailfeather3 = r_tailfeather2.addOrReplaceChild("r_tailfeather3", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 1.0F, -1.0F, -0.3318F, 0.4212F, -0.1117F));

		PartDefinition feather_r24 = r_tailfeather3.addOrReplaceChild("feather_r24", CubeListBuilder.create().texOffs(10, 42).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5921F, 7.9766F, 1.6441F, -0.696F, 0.4538F, -1.5652F));

		PartDefinition feather_r25 = r_tailfeather3.addOrReplaceChild("feather_r25", CubeListBuilder.create().texOffs(10, 38).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5921F, 4.9766F, 2.6441F, -0.0443F, 0.8653F, -0.6004F));

		PartDefinition feather_r26 = r_tailfeather3.addOrReplaceChild("feather_r26", CubeListBuilder.create().texOffs(10, 32).addBox(0.0F, -3.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5921F, -0.0233F, 1.6441F, 0.36F, 0.8126F, 0.5487F));

		PartDefinition feather_r27 = r_tailfeather3.addOrReplaceChild("feather_r27", CubeListBuilder.create().texOffs(10, 26).addBox(0.0F, -4.4F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1497F, -3.1554F, -0.3504F, 1.105F, 0.7231F, 1.6167F));

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
			this.animate(this.idleAnimationState, SunMythicalCreatureAnimations.IDLE, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, SunMythicalCreatureAnimations.WALK, ageInTicks, 1.0F);
			capturePoseInto(this.walkPose);

			// Write the lerped result
			applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		wings.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		legs.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		tailfeather.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
	
}