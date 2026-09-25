package de.jakob.lotm.rendering.models.death;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.darkness.DarknessMythicalCreatureAnimations;
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

public class DeathMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "death_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Whole_model;
	private final ModelPart Body;
	private final ModelPart Body_neck;
	private final ModelPart Body_below_neck;
	private final ModelPart throat;
	private final ModelPart body_below_throat;
	private final ModelPart Wings_body;
	private final ModelPart wing_left;
	private final ModelPart wing_right;
	private final ModelPart body_below_wings;
	private final ModelPart chest;
	private final ModelPart body_below_chest;
	private final ModelPart abdomen;
	private final ModelPart body_below_abdomen;
	private final ModelPart waist;
	private final ModelPart legs_body;
	private final ModelPart legs;
	private final ModelPart body_below_legs;
	private final ModelPart below_legs;
	private final ModelPart tail_start;
	private final ModelPart tail1;
	private final ModelPart tail_middle;
	private final ModelPart tail2;
	private final ModelPart tail_tip;
	private final ModelPart tip;
	private final ModelPart bone6;
	private final ModelPart Skull;
	private final ModelPart mouth;
	private final ModelPart jaw;
	private final ModelPart Eye_Fire;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();


	public DeathMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Whole_model = root.getChild("Whole_model");
		this.Body = this.Whole_model.getChild("Body");
		this.Body_neck = this.Body.getChild("Body_neck");
		this.Body_below_neck = this.Body.getChild("Body_below_neck");
		this.throat = this.Body_below_neck.getChild("throat");
		this.body_below_throat = this.Body_below_neck.getChild("body_below_throat");
		this.Wings_body = this.body_below_throat.getChild("Wings_body");
		this.wing_left = this.Wings_body.getChild("wing_left");
		this.wing_right = this.Wings_body.getChild("wing_right");
		this.body_below_wings = this.body_below_throat.getChild("body_below_wings");
		this.chest = this.body_below_wings.getChild("chest");
		this.body_below_chest = this.body_below_wings.getChild("body_below_chest");
		this.abdomen = this.body_below_chest.getChild("abdomen");
		this.body_below_abdomen = this.body_below_chest.getChild("body_below_abdomen");
		this.waist = this.body_below_abdomen.getChild("waist");
		this.legs_body = this.body_below_abdomen.getChild("legs_body");
		this.legs = this.legs_body.getChild("legs");
		this.body_below_legs = this.legs_body.getChild("body_below_legs");
		this.below_legs = this.body_below_legs.getChild("below_legs");
		this.tail_start = this.body_below_legs.getChild("tail_start");
		this.tail1 = this.tail_start.getChild("tail1");
		this.tail_middle = this.tail_start.getChild("tail_middle");
		this.tail2 = this.tail_middle.getChild("tail2");
		this.tail_tip = this.tail_middle.getChild("tail_tip");
		this.tip = this.tail_tip.getChild("tip");
		this.bone6 = this.tail_tip.getChild("bone6");
		this.Skull = this.Whole_model.getChild("Skull");
		this.mouth = this.Skull.getChild("mouth");
		this.jaw = this.Skull.getChild("jaw");
		this.Eye_Fire = this.Skull.getChild("Eye_Fire");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Whole_model = partdefinition.addOrReplaceChild("Whole_model", CubeListBuilder.create(), PartPose.offset(-2.0436F, 23.2276F, 1.5169F));

		PartDefinition Body = Whole_model.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0436F, -8.8276F, -14.5169F, -0.1047F, 0.0F, 0.0F));

		PartDefinition Body_neck = Body.addOrReplaceChild("Body_neck", CubeListBuilder.create(), PartPose.offset(0.1379F, 0.0028F, -0.0335F));

		PartDefinition cube_r1 = Body_neck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0041F, -0.0058F, 0.0149F));

		PartDefinition cube_r2 = Body_neck.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1403F, 0.1874F, 0.4996F, -1.1269F, -1.1798F, -0.4272F));

		PartDefinition Body_below_neck = Body.addOrReplaceChild("Body_below_neck", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0661F, -0.1094F, 1.8343F, -0.192F, 0.0F, 0.0F));

		PartDefinition throat = Body_below_neck.addOrReplaceChild("throat", CubeListBuilder.create(), PartPose.offset(0.0245F, 0.1747F, 0.0442F));

		PartDefinition cube_r3 = throat.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4198F, 0.1249F, 2.0877F, -1.1269F, -1.1798F, -0.4272F));

		PartDefinition cube_r4 = throat.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1428F, -0.0596F, 0.9908F, -0.0171F, -0.0003F, 0.0033F));

		PartDefinition cube_r5 = throat.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0416F, 0.8347F, 2.9215F, 0.1222F, -0.0785F, -0.0043F));

		PartDefinition body_below_throat = Body_below_neck.addOrReplaceChild("body_below_throat", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0245F, -0.0747F, 3.5558F, 0.1745F, 0.0F, 0.0F));

		PartDefinition Wings_body = body_below_throat.addOrReplaceChild("Wings_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r6 = Wings_body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.1292F, -0.1848F, 6.6038F, -0.131F, 0.0012F, -0.0087F));

		PartDefinition cube_r7 = Wings_body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.0914F, -0.4238F, 4.7084F, -0.0457F, -0.0677F, 0.0293F));

		PartDefinition cube_r8 = Wings_body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, -0.4F, 2.8F, 0.11F, 0.006F, -0.0262F));

		PartDefinition cube_r9 = Wings_body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0862F, -0.1199F, 1.0669F, 0.1692F, 0.013F, -0.0203F));

		PartDefinition wing_left = Wings_body.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offsetAndRotation(0.6811F, -0.5F, 3.8F, 0.0F, -0.1222F, 0.0F));

		PartDefinition cube_r10 = wing_left.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(5, 13).mirror().addBox(-0.0415F, -0.4671F, -0.9919F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(2.9585F, -0.4671F, -0.9919F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(5.9585F, -0.4671F, -0.9919F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.3F, 3.0F, -0.1343F, -0.0013F, 0.0268F));

		PartDefinition cube_r11 = wing_left.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(5, 13).mirror().addBox(-0.0295F, -0.3868F, -0.9936F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(2.9705F, -0.3868F, -0.9936F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(5.9705F, -0.3868F, -0.9936F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(8.9705F, -0.3868F, -0.9936F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.082F, -0.0027F, 0.0267F));

		PartDefinition cube_r12 = wing_left.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(5, 13).mirror().addBox(-1.0F, 0.0F, 1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(2.0F, 0.0F, 1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(5.0F, 0.0F, 1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(8.0F, 0.0F, 1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(11.0F, 0.0F, 1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -0.3F, -3.0F, 0.0402F, -0.006F, 0.0262F));

		PartDefinition cube_r13 = wing_left.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(5, 13).mirror().addBox(8.9862F, -0.4715F, -1.458F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(5.9862F, -0.4715F, -1.458F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(2.9862F, -0.4715F, -1.458F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(5, 13).mirror().addBox(-0.0138F, -0.4715F, -1.458F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 0.1972F, -0.0113F, 0.0244F));

		PartDefinition cube_r14 = wing_left.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.7249F, 0.9655F, -0.9244F, 3.1387F, 0.0026F, -2.8998F));

		PartDefinition cube_r15 = wing_left.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.1296F, 0.5919F, -0.233F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.1109F, -0.024F, -2.9885F));

		PartDefinition cube_r16 = wing_left.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.1014F, 0.4387F, 0.1717F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.9842F, 0.0269F, -2.989F));

		PartDefinition cube_r17 = wing_left.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-4.0886F, -0.0875F, -0.7949F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 13).mirror().addBox(-8.0201F, -0.8508F, -0.7737F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.9526F, 0.0523F, -2.9054F));

		PartDefinition cube_r18 = wing_left.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0249F, 0.6655F, -0.1244F, 3.0514F, 0.0026F, -2.8998F));

		PartDefinition cube_r19 = wing_left.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0249F, 0.6655F, -1.1244F, 3.0518F, 0.0088F, -2.8303F));

		PartDefinition cube_r20 = wing_left.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-4.1138F, 0.0926F, -1.2759F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 13).mirror().addBox(-6.077F, -0.378F, -0.3093F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.1131F, -0.0266F, -2.9013F));

		PartDefinition cube_r21 = wing_left.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.077F, -0.3739F, -0.3013F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.3F, 3.0F, -3.0273F, -0.0391F, -2.9029F));

		PartDefinition cube_r22 = wing_left.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0249F, 0.7655F, -1.1244F, 3.0514F, 0.0026F, -2.8998F));

		PartDefinition cube_r23 = wing_left.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-6.0518F, -0.4207F, 0.2308F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 3.005F, 0.0523F, -2.9054F));

		PartDefinition cube_r24 = wing_left.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.0249F, 0.8655F, -0.1244F, -3.127F, 0.0026F, -2.8998F));

		PartDefinition cube_r25 = wing_left.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-8.0454F, -0.7156F, -1.306F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 13).mirror().addBox(-9.9852F, -1.2245F, -0.5197F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.0608F, -0.0266F, -2.9013F));

		PartDefinition cube_r26 = wing_left.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0249F, 0.8655F, -0.9244F, 3.1038F, 0.0026F, -2.8998F));

		PartDefinition cube_r27 = wing_left.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-9.9599F, -1.3096F, 0.0585F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.97F, 0.0523F, -2.9054F));

		PartDefinition cube_r28 = wing_left.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-11.637F, -1.6327F, -0.9789F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.9351F, 0.0523F, -2.9054F));

		PartDefinition cube_r29 = wing_left.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.7249F, 0.8655F, 0.0756F, 3.1387F, 0.0026F, -2.8998F));

		PartDefinition cube_r30 = wing_left.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.4751F, 0.8655F, 2.8756F, 3.0515F, -0.0053F, -2.9867F));

		PartDefinition cube_r31 = wing_left.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0482F, -0.2794F, -0.3112F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.1143F, 0.0286F, 2.8615F));

		PartDefinition cube_r32 = wing_left.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0249F, -0.1345F, -1.1244F, 3.0515F, -0.0053F, 2.8601F));

		PartDefinition cube_r33 = wing_left.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0786F, -0.3002F, 0.2191F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 3.042F, -0.063F, 2.867F));

		PartDefinition cube_r34 = wing_left.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-4.0192F, 0.0477F, -0.7782F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.8458F, -0.0813F, 2.9506F));

		PartDefinition cube_r35 = wing_left.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0249F, -0.1345F, -0.1244F, 3.0514F, 0.0026F, 2.947F));

		PartDefinition cube_r36 = wing_left.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-4.0012F, 0.1904F, -1.2673F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 13).mirror().addBox(-5.9415F, 0.5828F, -0.2228F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.1119F, 0.0261F, 2.9487F));

		PartDefinition cube_r37 = wing_left.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-5.9609F, 0.4851F, -0.2316F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.3F, 3.0F, -3.0604F, 0.0361F, 2.9503F));

		PartDefinition cube_r38 = wing_left.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0249F, -0.0345F, -1.1244F, 3.0514F, 0.0026F, 2.947F));

		PartDefinition cube_r39 = wing_left.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-5.9596F, 0.5408F, 0.1662F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 3.0029F, -0.0377F, 2.9506F));

		PartDefinition cube_r40 = wing_left.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-7.9051F, 0.9793F, -0.8006F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.9506F, -0.0377F, 2.9506F));

		PartDefinition cube_r41 = wing_left.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.0249F, -0.0345F, -0.1244F, -3.127F, 0.0026F, 2.947F));

		PartDefinition cube_r42 = wing_left.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-7.9064F, 1.0163F, -1.2408F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 13).mirror().addBox(-9.8472F, 1.4704F, -0.4181F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -3.0595F, 0.0261F, 2.9487F));

		PartDefinition cube_r43 = wing_left.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(8.0249F, 0.0655F, -0.9244F, 3.1038F, 0.0026F, 2.947F));

		PartDefinition cube_r44 = wing_left.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-9.8653F, 1.3848F, -0.028F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.968F, -0.0377F, 2.9506F));

		PartDefinition cube_r45 = wing_left.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-11.5164F, 1.7997F, -0.9693F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.2F, -2.5F, 2.9331F, -0.0377F, 2.9506F));

		PartDefinition cube_r46 = wing_left.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.7249F, 0.0655F, 0.0756F, 3.1387F, 0.0026F, 2.947F));

		PartDefinition cube_r47 = wing_left.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.9863F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(11.7249F, 0.1655F, -0.9244F, 3.1387F, 0.0026F, 2.947F));

		PartDefinition wing_right = Wings_body.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.4F, -0.5F, 3.8F, 0.0F, 0.1222F, 0.0F));

		PartDefinition cube_r48 = wing_right.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(5, 13).addBox(-13.9824F, -0.3065F, -0.9952F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-11.9824F, -0.3065F, -0.9952F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-8.9824F, -0.3065F, -0.9952F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-5.9824F, -0.3065F, -0.9952F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-2.9824F, -0.3065F, -0.9952F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 0.0402F, 0.006F, -0.0262F));

		PartDefinition cube_r49 = wing_right.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(0, 13).addBox(12.4022F, -2.1104F, -0.4732F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(10.439F, -1.7257F, -1.4771F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.1387F, -0.0026F, 2.8998F));

		PartDefinition cube_r50 = wing_right.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(0, 13).addBox(10.5108F, -1.4344F, -1.4761F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.9521F, -0.0482F, 2.9046F));

		PartDefinition cube_r51 = wing_right.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(0, 13).addBox(8.8337F, -1.1288F, -0.4453F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.987F, -0.0482F, 2.9046F));

		PartDefinition cube_r52 = wing_right.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(0, 13).addBox(8.7858F, -1.3189F, -0.5212F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.1038F, -0.0026F, 2.8998F));

		PartDefinition cube_r53 = wing_right.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.8249F, 0.8655F, 1.0756F, -3.1286F, 0.01F, 2.9F));

		PartDefinition cube_r54 = wing_right.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.8249F, 0.8655F, 1.8756F, -3.1286F, 0.01F, 2.9F));

		PartDefinition cube_r55 = wing_right.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(0, 13).addBox(6.846F, -0.8553F, -1.2716F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, -3.127F, -0.0026F, 2.8998F));

		PartDefinition cube_r56 = wing_right.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(0, 13).addBox(6.8939F, -0.6612F, -1.2743F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(2.9624F, 0.102F, -1.2955F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.9696F, -0.0482F, 2.9046F));

		PartDefinition cube_r57 = wing_right.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(0, 13).addBox(4.9256F, -0.2576F, -0.279F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 3.0219F, -0.0482F, 2.9046F));

		PartDefinition cube_r58 = wing_right.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(0, 13).addBox(4.8777F, -0.4723F, -0.3158F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(2.9145F, -0.0016F, -1.2823F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.0514F, -0.0026F, 2.8998F));

		PartDefinition cube_r59 = wing_right.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8249F, 0.8655F, 0.8756F, 3.1023F, 0.01F, 2.9F));

		PartDefinition cube_r60 = wing_right.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(0, 13).addBox(4.1249F, -0.18F, -0.2904F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 3.0F, -3.0782F, 0.0266F, 2.9013F));

		PartDefinition cube_r61 = wing_right.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8249F, 0.7655F, 1.6756F, 3.1023F, 0.01F, 2.9F));

		PartDefinition cube_r62 = wing_right.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(0, 13).addBox(0.9508F, 0.6852F, -0.3456F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 3.0014F, -0.0242F, 2.9885F));

		PartDefinition cube_r63 = wing_right.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(0, 13).addBox(1.0048F, 0.3158F, -0.2483F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.0518F, -0.0088F, 2.8303F));

		PartDefinition cube_r64 = wing_right.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8249F, 0.7655F, 0.8756F, 3.1033F, 0.0134F, 2.9872F));

		PartDefinition cube_r65 = wing_right.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(0, 13).addBox(-0.349F, 0.7669F, -0.2147F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 3.0F, -3.1109F, 0.024F, 2.9885F));

		PartDefinition cube_r66 = wing_right.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(0, 13).addBox(12.2236F, 2.5359F, -0.4602F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(10.2833F, 2.054F, -1.4666F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.1387F, -0.0026F, -2.947F));

		PartDefinition cube_r67 = wing_right.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(0, 13).addBox(10.2253F, 2.3483F, -1.4659F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.9503F, 0.0344F, -2.95F));

		PartDefinition cube_r68 = wing_right.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(0, 13).addBox(8.5741F, 1.9157F, -0.5434F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.9852F, 0.0344F, -2.95F));

		PartDefinition cube_r69 = wing_right.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(0, 13).addBox(8.6128F, 1.7229F, -0.4066F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.1038F, -0.0026F, -2.947F));

		PartDefinition cube_r70 = wing_right.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.8249F, 0.0655F, 1.0756F, -3.128F, -0.0127F, -2.9474F));

		PartDefinition cube_r71 = wing_right.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.8249F, 0.0655F, 1.8756F, -3.128F, -0.0127F, -2.9474F));

		PartDefinition cube_r72 = wing_right.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(0, 13).addBox(6.6719F, 1.2248F, -1.3023F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, -3.127F, -0.0026F, -2.947F));

		PartDefinition cube_r73 = wing_right.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(0, 13).addBox(6.6139F, 1.5191F, -1.3067F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.9677F, 0.0344F, -2.95F));

		PartDefinition cube_r74 = wing_right.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(0, 13).addBox(4.6684F, 1.0534F, -0.3675F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 3.0201F, 0.0344F, -2.95F));

		PartDefinition cube_r75 = wing_right.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(0, 13).addBox(4.7071F, 0.8344F, -0.198F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(2.7668F, 0.442F, -1.2426F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.0514F, -0.0026F, -2.947F));

		PartDefinition cube_r76 = wing_right.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8249F, -0.0345F, 0.8756F, 3.1028F, -0.0127F, -2.9474F));

		PartDefinition cube_r77 = wing_right.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(0, 13).addBox(3.9222F, 0.6805F, -0.214F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 3.0F, -3.1119F, -0.0261F, -2.9487F));

		PartDefinition cube_r78 = wing_right.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8249F, -0.1345F, 1.8756F, 3.1028F, -0.0127F, -2.9474F));

		PartDefinition cube_r79 = wing_right.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(0, 13).addBox(2.7281F, 0.6375F, -1.225F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 2.863F, 0.0344F, -2.95F));

		PartDefinition cube_r80 = wing_right.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(0, 13).addBox(0.7334F, 0.2519F, -0.3435F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 3.0589F, 0.0582F, -2.866F));

		PartDefinition cube_r81 = wing_right.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(0, 13).addBox(0.7889F, 0.038F, -0.2865F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -1.0F, 3.0515F, 0.0053F, -2.8601F));

		PartDefinition cube_r82 = wing_right.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8249F, -0.1345F, 0.8756F, 3.1018F, -0.0093F, -2.8602F));

		PartDefinition cube_r83 = wing_right.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(0, 13).addBox(-0.4599F, -0.3223F, -0.3124F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 3.0F, -3.1143F, -0.0286F, -2.8615F));

		PartDefinition cube_r84 = wing_right.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(5, 13).addBox(-11.0F, 0.0F, 3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-8.0F, 0.0F, 3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-5.0F, 0.0F, 3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-2.0F, 0.0F, 3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -0.4F, -3.0F, -0.0122F, 0.0046F, -0.0265F));

		PartDefinition cube_r85 = wing_right.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(5, 13).addBox(-11.9996F, -0.1126F, -1.0098F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-8.9996F, -0.1126F, -1.0098F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-5.9996F, -0.1126F, -1.0098F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-2.9996F, -0.1126F, -1.0098F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, -3.0F, 0.1448F, 0.0109F, -0.0246F));

		PartDefinition cube_r86 = wing_right.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(5, 13).addBox(-8.164F, -0.2462F, -0.996F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-5.164F, -0.2462F, -0.996F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 13).addBox(-2.164F, -0.2462F, -0.996F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 3.0F, -0.082F, 0.0027F, -0.0267F));

		PartDefinition body_below_wings = body_below_throat.addOrReplaceChild("body_below_wings", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2249F, -0.2345F, 7.4756F, -0.27F, -0.2443F, 0.0668F));

		PartDefinition chest = body_below_wings.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0657F, 0.0664F, -0.099F));

		PartDefinition cube_r87 = chest.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -0.1F, 4.9F, 0.0239F, 0.019F, 0.0828F));

		PartDefinition cube_r88 = chest.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0693F, -0.0417F, 3.0103F, 0.069F, 0.0136F, 0.043F));

		PartDefinition cube_r89 = chest.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0826F, 0.0609F, 1.0191F, -0.0218F, 0.009F, 0.0269F));

		PartDefinition body_below_chest = body_below_wings.addOrReplaceChild("body_below_chest", CubeListBuilder.create(), PartPose.offsetAndRotation(0.6263F, -0.2383F, 5.6397F, -0.2618F, -0.0169F, 0.0045F));

		PartDefinition abdomen = body_below_chest.addOrReplaceChild("abdomen", CubeListBuilder.create(), PartPose.offset(-0.4327F, 0.1247F, -0.2291F));

		PartDefinition cube_r90 = abdomen.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.223F, 0.0706F, 2.9415F, 0.0043F, 0.0474F, 0.0808F));

		PartDefinition cube_r91 = abdomen.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(0, 13).addBox(-0.0137F, -0.1464F, -0.4238F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4937F, 0.2912F, 2.0996F, 1.14F, -0.8534F, -2.5781F));

		PartDefinition cube_r92 = abdomen.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.4F, 0.5F, -1.7566F, -0.8376F, 0.4466F));

		PartDefinition cube_r93 = abdomen.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1138F, 0.045F, 1.1112F, -0.003F, 0.0435F, 0.061F));

		PartDefinition body_below_abdomen = body_below_chest.addOrReplaceChild("body_below_abdomen", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1916F, 0.1379F, 3.6269F, 0.1372F, 0.3027F, 0.0411F));

		PartDefinition waist = body_below_abdomen.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(0.1882F, -0.0445F, 0.0508F));

		PartDefinition cube_r94 = waist.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.8F, 0.0083F, 0.0061F, -0.0086F));

		PartDefinition cube_r95 = waist.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1316F, 0.0221F, 0.9268F, 0.0097F, 0.0581F, 0.0235F));

		PartDefinition legs_body = body_below_abdomen.addOrReplaceChild("legs_body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1249F, -0.1818F, 3.6484F, 0.3193F, 0.2909F, 0.0945F));

		PartDefinition legs = legs_body.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.1249F, 0.1182F, 0.0484F));

		PartDefinition cube_r96 = legs.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, -0.0043F, -0.0055F, -0.0183F));

		PartDefinition cube_r97 = legs.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4499F, 0.0624F, 2.9293F, -1.692F, -0.997F, 2.9913F));

		PartDefinition cube_r98 = legs.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0155F, 0.0368F, 1.0098F, -0.0071F, 0.0384F, -0.03F));

		PartDefinition body_below_legs = legs_body.addOrReplaceChild("body_below_legs", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3751F, 0.1818F, 3.8516F, 0.2185F, 0.0511F, 0.0113F));

		PartDefinition below_legs = body_below_legs.addOrReplaceChild("below_legs", CubeListBuilder.create(), PartPose.offset(-0.2341F, -0.0955F, 0.0694F));

		PartDefinition cube_r99 = below_legs.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.022F, 0.0F, -0.0161F));

		PartDefinition cube_r100 = below_legs.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0397F, 0.0503F, 1.0069F, -0.0035F, 0.0165F, 0.0036F));

		PartDefinition cube_r101 = below_legs.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.993F, 0.4823F, 3.0747F, -1.1492F, -0.7658F, -0.3755F));

		PartDefinition tail_start = body_below_legs.addOrReplaceChild("tail_start", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3F, -0.1F, 4.0F, 0.0447F, -0.218F, -0.0097F));

		PartDefinition tail1 = tail_start.addOrReplaceChild("tail1", CubeListBuilder.create(), PartPose.offset(0.0375F, -0.022F, 0.0263F));

		PartDefinition cube_r102 = tail1.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0085F, -0.016F, -0.0171F));

		PartDefinition cube_r103 = tail1.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0616F, -0.0137F, 3.0F, -0.0028F, -0.0316F, -0.0126F));

		PartDefinition tail_middle = tail_start.addOrReplaceChild("tail_middle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.1F, 4.1F, 0.0894F, -0.2173F, -0.0193F));

		PartDefinition tail2 = tail_middle.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.offset(-0.0638F, 0.0458F, -0.1207F));

		PartDefinition cube_r104 = tail2.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0313F, -0.0256F, 0.0093F));

		PartDefinition cube_r105 = tail2.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0153F, -0.0227F, 2.8209F, 0.0152F, 0.0011F, 0.0161F));

		PartDefinition cube_r106 = tail2.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3578F, 0.1443F, 3.0496F, -2.2417F, -0.9488F, -2.6118F));

		PartDefinition tail_tip = tail_middle.addOrReplaceChild("tail_tip", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0701F, 0.1534F, 3.1759F, 0.1415F, -0.3892F, -0.054F));

		PartDefinition tip = tail_tip.addOrReplaceChild("tip", CubeListBuilder.create(), PartPose.offset(-0.086F, -0.0848F, 1.449F));

		PartDefinition cube_r107 = tip.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(1, 0).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.1F, 2.7F, 3.1381F, -0.0208F, -3.1368F));

		PartDefinition cube_r108 = tip.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0188F, 0.0439F, 0.9407F, -3.1067F, 0.0026F, -3.1415F));

		PartDefinition bone6 = tail_tip.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1F, 0.0F, -0.2F, -0.0524F, -0.0174F, -0.034F));

		PartDefinition cube_r109 = bone6.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0606F, -0.0997F, 0.9801F, 0.0698F, -0.0009F, 0.0349F));

		PartDefinition cube_r110 = bone6.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.381F, 0.081F, 1.4131F, -2.1236F, 0.9642F, 2.5261F));

		PartDefinition Skull = Whole_model.addOrReplaceChild("Skull", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0437F, -8.7724F, -14.4831F, 0.2269F, 0.0873F, 0.0F));

		PartDefinition cube_r111 = Skull.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(0, 3).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.3873F, 1.5449F, -1.9337F, -0.0436F, -0.0873F, 0.0F));

		PartDefinition mouth = Skull.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3873F, -0.2551F, -2.9337F, 0.0F, -0.1047F, 0.0F));

		PartDefinition cube_r112 = mouth.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(0, 9).addBox(-0.9F, -1.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0261F, 0.8F, -1.0011F, -0.192F, 0.0F, 0.0F));

		PartDefinition jaw = Skull.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3873F, 0.6422F, -2.7617F, 0.0175F, -0.1047F, 0.0F));

		PartDefinition cube_r113 = jaw.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(0, 9).addBox(-1.4F, -0.5973F, -0.8721F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.1F, -0.9F, 3.1067F, 0.0F, 0.0F));

		PartDefinition Eye_Fire = Skull.addOrReplaceChild("Eye_Fire", CubeListBuilder.create(), PartPose.offset(0.4873F, -1.5551F, -1.3337F));

		PartDefinition cube_r114 = Eye_Fire.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(0, 10).addBox(1.2F, -0.7F, -0.8F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.48F));

		PartDefinition cube_r115 = Eye_Fire.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3734F, 0.323F, -0.5724F, 0.2618F, -0.1309F, -0.3054F));

		return LayerDefinition.create(meshdefinition, 16, 16);
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
			this.animate(this.idleAnimationState, DeathMythicalCreatureAnimations.idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, DeathMythicalCreatureAnimations.walk, ageInTicks, 1.0F);
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