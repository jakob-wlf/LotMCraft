package de.jakob.lotm.rendering.models.demoness;// Made with Blockbench 5.1.4
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

public class DemonessMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "demoness_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Whole;
	private final ModelPart Body;
	private final ModelPart right_sleeve;
	private final ModelPart left_sleeve;
	private final ModelPart left_pants;
	private final ModelPart right_pants;
	private final ModelPart Head;
	private final ModelPart Snake;
	private final ModelPart Long_Snake_Front2;
	private final ModelPart Long_Snake_Front2_Head;
	private final ModelPart Long_Snake_Front2_Neck;
	private final ModelPart Long_Snake_Front2_body;
	private final ModelPart Long_Snek_Back2;
	private final ModelPart Long_Snek_Back2_Heak;
	private final ModelPart Long_Snek_Back2_Neck;
	private final ModelPart Long_Snek_Back2_Back;
	private final ModelPart Long_Snake_Back1;
	private final ModelPart Long_Snake_Back1_Back;
	private final ModelPart Long_Snake_Back1_Neck;
	private final ModelPart Long_Snake_Back1_Head;
	private final ModelPart Snake_Hair1;
	private final ModelPart Snake_Hair2;
	private final ModelPart Long_Snake_front1;
	private final ModelPart Long_Snake_Front_back;
	private final ModelPart Long_Snake_Front_neck;
	private final ModelPart _Long_Snake_Front_Head;
	private final ModelPart Snake_Hair3;
	private final ModelPart Hair_Clouds;
	private final ModelPart Clouds;
	private final ModelPart Sneks_Cloud;
	private final ModelPart Big_snake1;
	private final ModelPart bone5;
	private final ModelPart bone4;
	private final ModelPart Big_snake2;
	private final ModelPart Big_Snake3;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();


	public DemonessMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Whole = root.getChild("Whole");
		this.Body = this.Whole.getChild("Body");
		this.right_sleeve = this.Body.getChild("right_sleeve");
		this.left_sleeve = this.Body.getChild("left_sleeve");
		this.left_pants = this.Body.getChild("left_pants");
		this.right_pants = this.Body.getChild("right_pants");
		this.Head = this.Body.getChild("Head");
		this.Snake = this.Head.getChild("Snake");
		this.Long_Snake_Front2 = this.Snake.getChild("Long_Snake_Front2");
		this.Long_Snake_Front2_Head = this.Long_Snake_Front2.getChild("Long_Snake_Front2_Head");
		this.Long_Snake_Front2_Neck = this.Long_Snake_Front2.getChild("Long_Snake_Front2_Neck");
		this.Long_Snake_Front2_body = this.Long_Snake_Front2.getChild("Long_Snake_Front2_body");
		this.Long_Snek_Back2 = this.Snake.getChild("Long_Snek_Back2");
		this.Long_Snek_Back2_Heak = this.Long_Snek_Back2.getChild("Long_Snek_Back2_Heak");
		this.Long_Snek_Back2_Neck = this.Long_Snek_Back2.getChild("Long_Snek_Back2_Neck");
		this.Long_Snek_Back2_Back = this.Long_Snek_Back2.getChild("Long_Snek_Back2_Back");
		this.Long_Snake_Back1 = this.Snake.getChild("Long_Snake_Back1");
		this.Long_Snake_Back1_Back = this.Long_Snake_Back1.getChild("Long_Snake_Back1_Back");
		this.Long_Snake_Back1_Neck = this.Long_Snake_Back1.getChild("Long_Snake_Back1_Neck");
		this.Long_Snake_Back1_Head = this.Long_Snake_Back1.getChild("Long_Snake_Back1_Head");
		this.Snake_Hair1 = this.Snake.getChild("Snake_Hair1");
		this.Snake_Hair2 = this.Snake.getChild("Snake_Hair2");
		this.Long_Snake_front1 = this.Snake.getChild("Long_Snake_front1");
		this.Long_Snake_Front_back = this.Long_Snake_front1.getChild("Long_Snake_Front_back");
		this.Long_Snake_Front_neck = this.Long_Snake_front1.getChild("Long_Snake_Front_neck");
		this._Long_Snake_Front_Head = this.Long_Snake_front1.getChild("_Long_Snake_Front_Head");
		this.Snake_Hair3 = this.Snake.getChild("Snake_Hair3");
		this.Hair_Clouds = this.Snake.getChild("Hair_Clouds");
		this.Clouds = this.Whole.getChild("Clouds");
		this.Sneks_Cloud = this.Clouds.getChild("Sneks_Cloud");
		this.Big_snake1 = this.Clouds.getChild("Big_snake1");
		this.bone5 = this.Big_snake1.getChild("bone5");
		this.bone4 = this.Big_snake1.getChild("bone4");
		this.Big_snake2 = this.Clouds.getChild("Big_snake2");
		this.Big_Snake3 = this.Clouds.getChild("Big_Snake3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Whole = partdefinition.addOrReplaceChild("Whole", CubeListBuilder.create(), PartPose.offset(1.0F, 1.6333F, 10.35F));

		PartDefinition Body = Whole.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(64, 16).addBox(-4.5F, -0.2333F, -12.25F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(34, 0).addBox(-5.0F, -1.6333F, -11.35F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
		.texOffs(0, 55).addBox(-5.0F, -1.6333F, -11.35F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(64, 16).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 2.7667F, -11.35F, 0.0F, 3.1416F, 0.0F));

		PartDefinition right_sleeve = Body.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(18, 36).addBox(-2.3F, -0.9F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(34, 19).addBox(-2.3F, -0.9F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-6.7F, -0.7333F, -9.35F));

		PartDefinition left_sleeve = Body.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(60, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(52, 19).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 0.3667F, -9.35F));

		PartDefinition left_pants = Body.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 73).addBox(-3.1F, -0.8F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 37).addBox(-3.1F, -0.8F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(2.1F, 11.1667F, -10.35F));

		PartDefinition right_pants = Body.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(20, 73).addBox(-1.7F, -0.8F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(29, 54).addBox(-1.7F, -0.8F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-3.3F, 11.1667F, -10.35F));

		PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -9.6333F, -13.35F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
		.texOffs(0, 0).addBox(-5.0F, -9.6333F, -13.35F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Snake = Head.addOrReplaceChild("Snake", CubeListBuilder.create(), PartPose.offset(-1.0F, 22.3667F, -10.35F));

		PartDefinition Long_Snake_Front2 = Snake.addOrReplaceChild("Long_Snake_Front2", CubeListBuilder.create(), PartPose.offset(6.0F, -31.0F, 3.0F));

		PartDefinition cube_r2 = Long_Snake_Front2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Long_Snake_Front2_Head = Long_Snake_Front2.addOrReplaceChild("Long_Snake_Front2_Head", CubeListBuilder.create(), PartPose.offset(1.1287F, 5.3F, -4.6777F));

		PartDefinition cube_r3 = Long_Snake_Front2_Head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.7751F, 0.2269F, -1.5359F));

		PartDefinition Long_Snake_Front2_Neck = Long_Snake_Front2.addOrReplaceChild("Long_Snake_Front2_Neck", CubeListBuilder.create(), PartPose.offset(1.3787F, 2.7F, -1.9777F));

		PartDefinition cube_r4 = Long_Snake_Front2_Neck.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3038F, -2.1642F, -0.5236F));

		PartDefinition cube_r5 = Long_Snake_Front2_Neck.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.6F, -0.9F, -0.8378F, -0.4887F, 2.4958F));

		PartDefinition Long_Snake_Front2_body = Long_Snake_Front2.addOrReplaceChild("Long_Snake_Front2_body", CubeListBuilder.create(), PartPose.offset(0.8787F, -0.2F, -0.5777F));

		PartDefinition cube_r6 = Long_Snake_Front2_body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0996F, -0.6981F, -0.6109F));

		PartDefinition cube_r7 = Long_Snake_Front2_body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 1.3F, -0.7F, 2.3038F, -2.1642F, -0.5236F));

		PartDefinition Long_Snek_Back2 = Snake.addOrReplaceChild("Long_Snek_Back2", CubeListBuilder.create(), PartPose.offset(0.6787F, -25.3F, 6.5223F));

		PartDefinition cube_r8 = Long_Snek_Back2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition Long_Snek_Back2_Heak = Long_Snek_Back2.addOrReplaceChild("Long_Snek_Back2_Heak", CubeListBuilder.create(), PartPose.offset(-0.759F, 8.2279F, 2.9858F));

		PartDefinition cube_r9 = Long_Snek_Back2_Heak.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(43, 40).addBox(0.3785F, 0.4949F, -1.3858F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2478F, -0.6861F, -1.4205F));

		PartDefinition cube_r10 = Long_Snek_Back2_Heak.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(48, 39).addBox(-0.7691F, -0.3836F, 0.2624F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.219F, -0.3197F, 0.1727F, 0.368F, 0.8558F, -0.8133F));

		PartDefinition cube_r11 = Long_Snek_Back2_Heak.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(36, 40).addBox(0.4464F, -1.3749F, -1.3225F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0714F, -0.0881F, 0.2171F, 0.6883F, -0.6993F, -1.3594F));

		PartDefinition Long_Snek_Back2_Neck = Long_Snek_Back2.addOrReplaceChild("Long_Snek_Back2_Neck", CubeListBuilder.create(), PartPose.offset(0.3F, 5.1F, 1.2F));

		PartDefinition cube_r12 = Long_Snek_Back2_Neck.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.4F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0908F, 0.0785F, -0.0436F));

		PartDefinition cube_r13 = Long_Snek_Back2_Neck.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.4F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, 1.2F, 1.4F, -0.7069F, -0.1484F, -0.0436F));

		PartDefinition Long_Snek_Back2_Back = Long_Snek_Back2.addOrReplaceChild("Long_Snek_Back2_Back", CubeListBuilder.create(), PartPose.offset(0.3F, 1.5F, 0.7F));

		PartDefinition cube_r14 = Long_Snek_Back2_Back.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1257F, 0.4102F, -0.0436F));

		PartDefinition cube_r15 = Long_Snek_Back2_Back.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.9F, 0.0F, -1.6144F, 0.4102F, -0.0436F));

		PartDefinition Long_Snake_Back1 = Snake.addOrReplaceChild("Long_Snake_Back1", CubeListBuilder.create(), PartPose.offset(0.9559F, -20.2439F, 9.2345F));

		PartDefinition cube_r16 = Long_Snake_Back1.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7772F, -7.1561F, -2.7123F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition Long_Snake_Back1_Back = Long_Snake_Back1.addOrReplaceChild("Long_Snake_Back1_Back", CubeListBuilder.create(), PartPose.offset(-2.2772F, -5.6561F, -2.0123F));

		PartDefinition cube_r17 = Long_Snake_Back1_Back.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1257F, 0.4102F, -0.0436F));

		PartDefinition cube_r18 = Long_Snake_Back1_Back.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 1.8F, 0.9F, 0.48F, 1.1257F, 1.946F));

		PartDefinition Long_Snake_Back1_Neck = Long_Snake_Back1.addOrReplaceChild("Long_Snake_Back1_Neck", CubeListBuilder.create(), PartPose.offset(-1.9773F, -2.0561F, -0.3123F));

		PartDefinition cube_r19 = Long_Snake_Back1_Neck.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3054F, 1.1257F, 1.946F));

		PartDefinition cube_r20 = Long_Snake_Back1_Neck.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, 1.5F, 0.2F, 0.5149F, 1.4748F, 1.4923F));

		PartDefinition Long_Snake_Back1_Head = Long_Snake_Back1.addOrReplaceChild("Long_Snake_Back1_Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r21 = Long_Snake_Back1_Head.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.2305F, -0.2443F, -1.3875F));

		PartDefinition Snake_Hair1 = Snake.addOrReplaceChild("Snake_Hair1", CubeListBuilder.create().texOffs(33, 36).addBox(1.1713F, 3.5F, 7.4777F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1713F, -35.5F, -2.4777F));

		PartDefinition cube_r22 = Snake_Hair1.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.672F, -2.6878F, 1.1781F));

		PartDefinition cube_r23 = Snake_Hair1.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 0.7F, 1.4F, 0.4189F, -2.2689F, 0.576F));

		PartDefinition cube_r24 = Snake_Hair1.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1713F, 1.5F, 2.4777F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r25 = Snake_Hair1.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(43, 40).addBox(-0.6123F, 0.0029F, 0.2285F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.3974F, 5.0432F, 2.5893F, -2.4784F, -2.6005F, -1.7802F));

		PartDefinition cube_r26 = Snake_Hair1.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(48, 39).addBox(-2.7643F, -0.3683F, -0.4056F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0974F, 4.5432F, 1.7893F, -1.9897F, 0.7854F, 3.0543F));

		PartDefinition cube_r27 = Snake_Hair1.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(36, 40).addBox(-0.6936F, -0.866F, 0.2191F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.3974F, 5.0432F, 2.5893F, -1.9199F, -2.5831F, -1.5882F));

		PartDefinition cube_r28 = Snake_Hair1.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.05F, 4.4F, 1.5F, 0.3491F, -2.2689F, 0.0873F));

		PartDefinition cube_r29 = Snake_Hair1.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1713F, 4.5F, 1.4777F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r30 = Snake_Hair1.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3F, -0.7F, 3.3F, -2.9758F, 2.6704F, 1.1781F));

		PartDefinition cube_r31 = Snake_Hair1.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1713F, -0.2F, 1.4777F, 2.3038F, 3.1067F, 0.0F));

		PartDefinition cube_r32 = Snake_Hair1.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1713F, 1.5F, 1.4777F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r33 = Snake_Hair1.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9F, -0.4F, 3.2F, 0.1484F, -2.8623F, 1.1781F));

		PartDefinition cube_r34 = Snake_Hair1.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5713F, 0.4F, 4.7777F, 0.576F, 2.7402F, -0.4189F));

		PartDefinition cube_r35 = Snake_Hair1.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1713F, 1.6F, 6.4777F, 0.0F, 1.5708F, -0.8727F));

		PartDefinition cube_r36 = Snake_Hair1.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.65F, 3.1F, 6.0F, 1.5708F, 1.5708F, 0.4363F));

		PartDefinition cube_r37 = Snake_Hair1.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(43, 40).addBox(-0.3862F, -0.4953F, -1.551F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.727F, 8.3263F, 11.8796F, -0.8378F, 0.2967F, -0.925F));

		PartDefinition cube_r38 = Snake_Hair1.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(48, 39).addBox(-0.7663F, -1.2606F, -0.1974F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.427F, 8.0263F, 11.9796F, -1.5533F, 0.9948F, -2.3038F));

		PartDefinition cube_r39 = Snake_Hair1.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(36, 40).addBox(-0.3166F, -2.332F, -1.0395F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.627F, 8.3263F, 11.4796F, -0.384F, 0.2793F, -0.8727F));

		PartDefinition cube_r40 = Snake_Hair1.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.25F, 7.0F, 10.7F, 0.2182F, 0.4102F, -0.0436F));

		PartDefinition cube_r41 = Snake_Hair1.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.65F, 7.1F, 9.0F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition cube_r42 = Snake_Hair1.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(43, 40).addBox(0.7179F, 0.1307F, -2.2878F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.173F, 5.7263F, 9.8796F, 0.2269F, -1.3963F, -0.7679F));

		PartDefinition cube_r43 = Snake_Hair1.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(48, 39).addBox(0.3307F, -0.8242F, 0.5668F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.173F, 5.6263F, 9.8796F, -0.0698F, -0.0175F, -0.576F));

		PartDefinition cube_r44 = Snake_Hair1.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(36, 40).addBox(0.7797F, -2.0693F, -1.8683F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.173F, 5.4263F, 9.8796F, 0.9425F, -1.3788F, -0.576F));

		PartDefinition cube_r45 = Snake_Hair1.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.35F, 4.3F, 10.5F, -0.3054F, -0.5498F, -0.1134F));

		PartDefinition cube_r46 = Snake_Hair1.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.35F, 5.1F, 3.0F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r47 = Snake_Hair1.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6287F, 5.3F, 2.4777F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r48 = Snake_Hair1.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9F, 5.6F, 2.7F, -1.3352F, 2.8623F, 0.8639F));

		PartDefinition cube_r49 = Snake_Hair1.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 5.2F, -5.4F, -0.1309F, 2.8623F, 1.1781F));

		PartDefinition cube_r50 = Snake_Hair1.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 4.7F, -3.8F, -0.3316F, -2.5656F, 0.0262F));

		PartDefinition cube_r51 = Snake_Hair1.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1713F, 4.8F, -2.5223F, 0.0F, -3.1241F, 0.0F));

		PartDefinition Snake_Hair2 = Snake.addOrReplaceChild("Snake_Hair2", CubeListBuilder.create(), PartPose.offset(-0.5182F, -37.5771F, -5.4348F));

		PartDefinition cube_r52 = Snake_Hair2.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(36, 40).addBox(0.0587F, -2.7342F, -2.3283F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.0142F, -0.2588F, -2.8463F));

		PartDefinition cube_r53 = Snake_Hair2.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(43, 40).addBox(-0.048F, -0.1002F, -3.0264F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0968F, 0.2781F, -0.0572F, -2.7144F, -0.2085F, -3.0354F));

		PartDefinition cube_r54 = Snake_Hair2.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3531F, 7.9771F, 14.0571F, 2.7838F, -2.9671F, 1.3003F));

		PartDefinition cube_r55 = Snake_Hair2.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0031F, 8.1771F, 11.9571F, 0.3491F, 0.0349F, -0.0175F));

		PartDefinition cube_r56 = Snake_Hair2.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(48, 39).addBox(0.9716F, -0.7831F, -0.3056F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0645F, 0.1854F, -0.0382F, -1.3098F, -1.4006F, 1.5916F));

		PartDefinition cube_r57 = Snake_Hair2.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5182F, 1.5771F, 3.4348F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r58 = Snake_Hair2.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5182F, 3.5771F, 3.4348F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r59 = Snake_Hair2.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5531F, 3.6771F, 2.5571F, -1.3352F, -2.81F, 1.7366F));

		PartDefinition cube_r60 = Snake_Hair2.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(43, 40).addBox(0.4309F, 0.3472F, -1.3838F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.8787F, 4.8477F, 9.464F, 0.2269F, -1.5359F, -0.192F));

		PartDefinition cube_r61 = Snake_Hair2.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(48, 39).addBox(-0.6284F, -0.7157F, 0.4366F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.8787F, 4.8477F, 9.464F, -0.0349F, 0.0F, 0.0F));

		PartDefinition cube_r62 = Snake_Hair2.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(36, 40).addBox(0.4442F, -1.4718F, -1.2721F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.8787F, 4.8477F, 9.464F, 0.6632F, -1.5184F, 0.0F));

		PartDefinition cube_r63 = Snake_Hair2.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.2031F, 5.2771F, 10.4571F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r64 = Snake_Hair2.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.3031F, 5.3771F, 10.3571F, -0.3491F, -1.2741F, 0.0262F));

		PartDefinition cube_r65 = Snake_Hair2.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0031F, 5.1771F, 9.9571F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r66 = Snake_Hair2.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5031F, 4.4771F, 3.5571F, 0.3491F, -2.2689F, 0.0873F));

		PartDefinition cube_r67 = Snake_Hair2.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0031F, 5.1771F, 3.9571F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r68 = Snake_Hair2.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3469F, 4.2771F, 3.0571F, 1.5446F, -2.5656F, 1.3526F));

		PartDefinition cube_r69 = Snake_Hair2.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.6182F, 5.0771F, 3.5348F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r70 = Snake_Hair2.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.9969F, 5.1771F, 2.9571F, 1.5708F, 1.5708F, 0.4363F));

		PartDefinition cube_r71 = Snake_Hair2.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(48, 39).addBox(-0.1367F, -1.0696F, -0.5004F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2089F, 5.8881F, -3.4696F, -1.6132F, -1.0479F, -0.7935F));

		PartDefinition cube_r72 = Snake_Hair2.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(43, 40).addBox(-0.4114F, 1.3089F, -1.5752F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2915F, 5.9662F, -3.2106F, 2.1324F, -0.0911F, -2.2537F));

		PartDefinition cube_r73 = Snake_Hair2.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(36, 40).addBox(-0.3363F, -0.8026F, -1.9964F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2915F, 5.9662F, -3.2106F, 2.7471F, -0.1588F, -2.089F));

		PartDefinition cube_r74 = Snake_Hair2.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2969F, 5.9771F, -0.3429F, 0.384F, 2.7053F, 0.2443F));

		PartDefinition cube_r75 = Snake_Hair2.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5182F, 6.5771F, 0.4348F, 0.0F, -3.1241F, 0.0F));

		PartDefinition cube_r76 = Snake_Hair2.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1469F, 4.4771F, 14.9571F, 2.33F, -2.9671F, 0.9861F));

		PartDefinition cube_r77 = Snake_Hair2.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1678F, 4.8313F, 12.9348F, -0.0349F, -0.1396F, -1.3963F));

		PartDefinition cube_r78 = Snake_Hair2.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9969F, 5.1771F, 10.9571F, 0.3491F, 0.0349F, -0.0175F));

		PartDefinition cube_r79 = Snake_Hair2.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.9969F, 5.1771F, 5.9571F, 1.5708F, 1.5708F, 0.4363F));

		PartDefinition cube_r80 = Snake_Hair2.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(43, 40).addBox(-0.4327F, 0.0338F, 0.0702F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5589F, 4.124F, 6.9352F, 0.5978F, -0.7009F, 1.1868F));

		PartDefinition cube_r81 = Snake_Hair2.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9969F, 4.3771F, 6.3571F, 0.2094F, -2.1642F, -0.5236F));

		PartDefinition cube_r82 = Snake_Hair2.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(48, 39).addBox(-2.6581F, -0.3442F, -0.6714F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3F, 3.5926F, 6.5603F, -2.2293F, 0.7007F, 2.6154F));

		PartDefinition cube_r83 = Snake_Hair2.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(36, 40).addBox(-0.5017F, -0.9421F, 0.1184F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5589F, 4.124F, 6.9352F, 1.1048F, -0.6513F, 1.4009F));

		PartDefinition Long_Snake_front1 = Snake.addOrReplaceChild("Long_Snake_front1", CubeListBuilder.create(), PartPose.offset(-5.9818F, -24.5922F, -3.1799F));

		PartDefinition cube_r84 = Long_Snake_front1.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0182F, -6.4078F, 1.1799F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Long_Snake_Front_back = Long_Snake_front1.addOrReplaceChild("Long_Snake_Front_back", CubeListBuilder.create(), PartPose.offset(-1.3351F, -3.0627F, 1.6037F));

		PartDefinition cube_r85 = Long_Snake_Front_back.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.6529F, -2.618F, -0.7679F));

		PartDefinition cube_r86 = Long_Snake_Front_back.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2956F, -2.8451F, 0.4985F, -1.0472F, -1.2741F, 0.0262F));

		PartDefinition cube_r87 = Long_Snake_Front_back.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4169F, -0.9451F, -0.0238F, -1.501F, -1.4748F, 0.1745F));

		PartDefinition Long_Snake_Front_neck = Long_Snake_front1.addOrReplaceChild("Long_Snake_Front_neck", CubeListBuilder.create(), PartPose.offset(-1.0351F, -1.8627F, 0.4037F));

		PartDefinition cube_r88 = Long_Snake_Front_neck.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3387F, -2.618F, -0.7679F));

		PartDefinition cube_r89 = Long_Snake_Front_neck.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 1.3F, -1.1F, 1.8588F, -2.7053F, -1.0734F));

		PartDefinition _Long_Snake_Front_Head = Long_Snake_front1.addOrReplaceChild("_Long_Snake_Front_Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r90 = _Long_Snake_Front_Head.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(43, 40).addBox(-0.8676F, 0.3438F, 0.9137F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.9082F, 0.269F, -2.0926F));

		PartDefinition cube_r91 = _Long_Snake_Front_Head.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(48, 39).addBox(-2.9593F, -0.6292F, -0.8046F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1868F, -0.3758F, 0.0625F, -2.9634F, -1.2339F, -2.4295F));

		PartDefinition cube_r92 = _Long_Snake_Front_Head.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(36, 40).addBox(-0.9182F, -0.0368F, 0.4766F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2007F, -0.3575F, -0.3492F, -2.4172F, 0.2755F, -2.1901F));

		PartDefinition Snake_Hair3 = Snake.addOrReplaceChild("Snake_Hair3", CubeListBuilder.create().texOffs(33, 36).addBox(3.7639F, -1.5453F, 3.7232F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 36).addBox(-0.2361F, -1.5453F, 3.7232F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2361F, -30.4547F, 1.2768F));

		PartDefinition cube_r93 = Snake_Hair3.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(36, 40).addBox(-0.4713F, -1.7825F, -1.8842F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.4F, 1.2F, -1.9F, -2.4974F, -0.0231F, 1.746F));

		PartDefinition cube_r94 = Snake_Hair3.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(43, 40).addBox(-0.4976F, 0.45F, -2.0448F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.4F, 1.2F, -1.9F, -3.1256F, -0.0049F, 1.7524F));

		PartDefinition cube_r95 = Snake_Hair3.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(48, 39).addBox(-0.0037F, -0.5506F, -0.5078F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.4F, 1.2F, -1.9F, 2.5307F, -1.5359F, 2.3213F));

		PartDefinition cube_r96 = Snake_Hair3.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(48, 39).addBox(-0.0089F, -0.565F, -0.3714F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8938F, -4.478F, -4.4116F, 0.0522F, -1.4626F, 3.126F));

		PartDefinition cube_r97 = Snake_Hair3.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(36, 40).addBox(-0.9128F, -1.1871F, -1.9931F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8646F, -4.3668F, -3.9148F, -3.136F, 0.4316F, 3.0601F));

		PartDefinition cube_r98 = Snake_Hair3.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(48, 39).addBox(-0.0656F, -0.7683F, -0.3109F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2771F, -2.9609F, -9.3352F, -0.9345F, -1.2924F, 0.6543F));

		PartDefinition cube_r99 = Snake_Hair3.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(43, 40).addBox(-0.3079F, 0.3566F, -2.0078F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2771F, -2.9609F, -9.3352F, -3.0149F, -0.1981F, 2.8208F));

		PartDefinition cube_r100 = Snake_Hair3.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(36, 40).addBox(-0.2289F, -1.806F, -1.8108F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2771F, -2.9609F, -9.3352F, -2.4212F, -0.2519F, 3.0058F));

		PartDefinition cube_r101 = Snake_Hair3.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(36, 40).addBox(0.0742F, -2.493F, -2.6394F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6092F, -4.6621F, -11.9266F, -2.4252F, -0.275F, 3.1268F));

		PartDefinition cube_r102 = Snake_Hair3.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(43, 40).addBox(-0.0353F, 0.3003F, -3.1115F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5123F, -4.3839F, -11.9838F, -3.1366F, -0.2215F, 2.9406F));

		PartDefinition cube_r103 = Snake_Hair3.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(43, 40).addBox(-0.3524F, 0.5252F, -3.1066F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1634F, -3.933F, -5.2661F, 2.9186F, 0.0976F, -2.9619F));

		PartDefinition cube_r104 = Snake_Hair3.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(48, 39).addBox(0.9435F, -0.8396F, -0.163F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6092F, -4.6621F, -11.9266F, -0.8682F, -1.2783F, 0.7106F));

		PartDefinition cube_r105 = Snake_Hair3.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(36, 40).addBox(0.298F, -1.2751F, -1.2721F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5928F, -6.1219F, 2.0018F, 0.7402F, -0.5182F, 0.0241F));

		PartDefinition cube_r106 = Snake_Hair3.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(43, 40).addBox(0.2858F, 0.3175F, -1.2949F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6896F, -5.8438F, 1.9446F, 0.0148F, -0.5714F, -0.1559F));

		PartDefinition cube_r107 = Snake_Hair3.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(48, 39).addBox(-0.7403F, -0.8432F, 0.3007F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6574F, -5.9365F, 1.9636F, -0.3314F, 0.8129F, -0.2913F));

		PartDefinition cube_r108 = Snake_Hair3.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(48, 39).addBox(-0.9912F, -0.7971F, -0.4653F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0977F, -3.8601F, 7.1074F, -2.1495F, 1.357F, -2.0513F));

		PartDefinition cube_r109 = Snake_Hair3.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(43, 40).addBox(-0.5111F, 0.3495F, -1.1703F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0654F, -3.7674F, 7.0883F, 0.0459F, 0.259F, -0.1909F));

		PartDefinition cube_r110 = Snake_Hair3.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(36, 40).addBox(-0.5047F, -1.2013F, -1.2277F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1623F, -4.0455F, 7.1455F, 0.766F, 0.3125F, -0.0044F));

		PartDefinition cube_r111 = Snake_Hair3.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(36, 40).addBox(-0.0282F, -1.6429F, -0.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9724F, -1.0242F, 9.9732F, 0.2231F, -0.1699F, 0.9598F));

		PartDefinition cube_r112 = Snake_Hair3.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(43, 40).addBox(-0.0529F, -0.1868F, -1.2891F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1592F, -0.6484F, 9.9107F, -0.5263F, -0.2035F, 0.7791F));

		PartDefinition cube_r113 = Snake_Hair3.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(48, 39).addBox(-0.8004F, -1.2363F, -0.3151F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.007F, -0.8248F, 9.8351F, -1.5154F, 0.9049F, -0.6537F));

		PartDefinition cube_r114 = Snake_Hair3.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(48, 39).addBox(0.9855F, -0.587F, -0.3518F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0337F, 0.4485F, 1.7248F, 1.6481F, -1.5382F, -0.1344F));

		PartDefinition cube_r115 = Snake_Hair3.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(43, 40).addBox(0.2151F, 0.0877F, -2.9914F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0637F, 0.2305F, 1.7647F, -2.8926F, -0.4894F, -1.8361F));

		PartDefinition cube_r116 = Snake_Hair3.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(36, 40).addBox(0.2856F, -2.5592F, -2.4421F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3173F, 0.3508F, 1.73F, -2.2188F, -0.5039F, -1.6274F));

		PartDefinition cube_r117 = Snake_Hair3.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.3075F, -0.3453F, 5.5455F, -2.5569F, 2.3213F, 1.5097F));

		PartDefinition cube_r118 = Snake_Hair3.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2925F, -4.2454F, -7.6545F, 0.0785F, -2.3911F, 1.1781F));

		PartDefinition cube_r119 = Snake_Hair3.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.1075F, -1.5453F, 2.2455F, -1.8239F, 2.8449F, 1.5097F));

		PartDefinition cube_r120 = Snake_Hair3.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8075F, -6.0453F, 3.3455F, -1.8239F, 2.8449F, 1.5097F));

		PartDefinition cube_r121 = Snake_Hair3.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5075F, -3.4453F, 5.8455F, -1.946F, -2.426F, 2.1729F));

		PartDefinition cube_r122 = Snake_Hair3.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6925F, -0.2453F, 8.3455F, 2.7838F, -2.9671F, 1.3003F));

		PartDefinition cube_r123 = Snake_Hair3.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7575F, -1.9453F, 3.2455F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r124 = Snake_Hair3.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.1575F, -1.3453F, 1.8455F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r125 = Snake_Hair3.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7575F, -1.9453F, 0.2455F, 0.3491F, -1.5359F, -0.0175F));

		PartDefinition cube_r126 = Snake_Hair3.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2425F, -1.9453F, -4.7545F, 0.384F, 2.7053F, 0.2443F));

		PartDefinition cube_r127 = Snake_Hair3.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0425F, -2.6454F, -6.1545F, 0.8378F, 2.7053F, 0.2443F));

		PartDefinition cube_r128 = Snake_Hair3.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7575F, -1.9453F, -4.7545F, 0.384F, -2.7053F, -0.0175F));

		PartDefinition cube_r129 = Snake_Hair3.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1575F, -4.0453F, -7.6545F, 0.3491F, -3.1067F, -0.0175F));

		PartDefinition cube_r130 = Snake_Hair3.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0575F, -2.9453F, -6.1545F, 0.7854F, -3.1067F, -0.0175F));

		PartDefinition cube_r131 = Snake_Hair3.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1575F, -2.4453F, -6.1545F, 0.2793F, 2.9322F, -0.0175F));

		PartDefinition cube_r132 = Snake_Hair3.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7575F, -1.9453F, -4.7545F, 0.384F, -2.7053F, -0.0175F));

		PartDefinition cube_r133 = Snake_Hair3.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.5575F, 0.9547F, 1.1455F, 0.2443F, -2.4784F, -0.6807F));

		PartDefinition cube_r134 = Snake_Hair3.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.3575F, 0.9547F, 2.0455F, 0.3491F, -1.9373F, -0.1745F));

		PartDefinition cube_r135 = Snake_Hair3.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1575F, 0.2547F, 3.6455F, 0.3665F, -1.0821F, -0.0175F));

		PartDefinition cube_r136 = Snake_Hair3.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3425F, 3.0547F, 5.3455F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition cube_r137 = Snake_Hair3.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2425F, -0.8453F, 8.4455F, 0.3578F, 0.0436F, 0.096F));

		PartDefinition cube_r138 = Snake_Hair3.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1425F, -0.4453F, 6.7455F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition cube_r139 = Snake_Hair3.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1425F, -0.4453F, 6.7455F, -0.48F, -0.0087F, 0.0262F));

		PartDefinition cube_r140 = Snake_Hair3.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0575F, 0.3546F, 4.5455F, -0.3316F, -1.117F, 0.0262F));

		PartDefinition cube_r141 = Snake_Hair3.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.3575F, 1.0547F, 2.2455F, -0.3491F, -1.2741F, 0.0262F));

		PartDefinition cube_r142 = Snake_Hair3.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8575F, -2.9453F, 5.3455F, 0.4189F, -1.2043F, 0.0262F));

		PartDefinition cube_r143 = Snake_Hair3.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0575F, -5.8453F, 2.7455F, 0.4363F, -1.3265F, 0.0262F));

		PartDefinition cube_r144 = Snake_Hair3.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5425F, 1.4547F, 4.9455F, 2.2427F, 0.4102F, 0.9338F));

		PartDefinition cube_r145 = Snake_Hair3.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4425F, 2.5547F, 5.9455F, 1.0908F, 0.4102F, 0.9338F));

		PartDefinition cube_r146 = Snake_Hair3.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7575F, -4.5453F, 1.9455F, 0.9425F, -0.0349F, -0.0349F));

		PartDefinition cube_r147 = Snake_Hair3.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7575F, -1.9453F, 4.2455F, 0.3491F, 0.0349F, -0.0175F));

		PartDefinition cube_r148 = Snake_Hair3.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7575F, -1.9453F, 4.2455F, 0.3491F, 0.0349F, -0.0175F));

		PartDefinition cube_r149 = Snake_Hair3.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9425F, -2.2454F, 4.2455F, 0.5411F, 0.5411F, -0.0524F));

		PartDefinition cube_r150 = Snake_Hair3.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2361F, -3.5453F, 1.7232F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r151 = Snake_Hair3.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5135F, -4.6912F, -0.8768F, -0.0349F, 3.0194F, -1.4835F));

		PartDefinition cube_r152 = Snake_Hair3.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5865F, -2.8912F, 5.3232F, -0.0349F, 0.9425F, -1.3963F));

		PartDefinition cube_r153 = Snake_Hair3.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3135F, -4.1912F, 0.6232F, -0.0349F, 2.5307F, -1.3963F));

		PartDefinition cube_r154 = Snake_Hair3.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7639F, -3.5453F, 1.7232F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r155 = Snake_Hair3.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4639F, -5.5453F, 1.7232F, 1.7977F, 1.5708F, 0.0F));

		PartDefinition cube_r156 = Snake_Hair3.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7639F, -3.5453F, 1.7232F, 1.5708F, 1.5708F, 0.0F));

		PartDefinition cube_r157 = Snake_Hair3.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(33, 36).addBox(0.0F, -1.0F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2362F, -0.5453F, 1.3232F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Hair_Clouds = Snake.addOrReplaceChild("Hair_Clouds", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.7F, -31.8F, 1.4F));

		PartDefinition cube_r158 = Hair_Clouds.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.9F, -0.9F, 4.6F, 0.0873F, -0.8029F, -0.9425F));

		PartDefinition Clouds = Whole.addOrReplaceChild("Clouds", CubeListBuilder.create(), PartPose.offset(0.6419F, 20.0489F, -10.2461F));

		PartDefinition cube_r159 = Clouds.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(27, 19).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(2.0F)), PartPose.offsetAndRotation(-1.1419F, 3.7178F, 9.1961F, 2.9322F, 1.3788F, -1.8326F));

		PartDefinition cube_r160 = Clouds.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(3.8581F, 3.8178F, -6.3039F, 0.0F, 0.0524F, -1.6406F));

		PartDefinition cube_r161 = Clouds.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(2.0F)), PartPose.offsetAndRotation(-10.2419F, 3.8178F, 1.1961F, 0.0F, 0.384F, -1.6406F));

		PartDefinition Sneks_Cloud = Clouds.addOrReplaceChild("Sneks_Cloud", CubeListBuilder.create(), PartPose.offset(10.2581F, -2.5822F, 4.9961F));

		PartDefinition cube_r162 = Sneks_Cloud.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(2.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5061F, -0.9599F, 0.9948F));

		PartDefinition Big_snake1 = Clouds.addOrReplaceChild("Big_snake1", CubeListBuilder.create(), PartPose.offset(9.2143F, -6.9023F, -1.5638F));

		PartDefinition bone5 = Big_snake1.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.3224F, 1.5201F, 3.5822F));

		PartDefinition cube_r163 = bone5.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3316F, -2.5656F, 1.2654F));

		PartDefinition bone4 = Big_snake1.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-0.7562F, -1.58F, -1.8401F));

		PartDefinition cube_r164 = bone4.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(33, 39).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5411F, 0.0F, -0.0524F));

		PartDefinition cube_r165 = bone4.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(39, 36).addBox(-0.5F, -0.5F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(0.3787F, 1.1F, 2.1223F, 0.1134F, -2.5656F, 1.2654F));

		PartDefinition Big_snake2 = Clouds.addOrReplaceChild("Big_snake2", CubeListBuilder.create(), PartPose.offset(-11.5068F, 0.76F, -2.6577F));

		PartDefinition cube_r166 = Big_snake2.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(33, 36).addBox(-1.9965F, -2.2758F, -7.421F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5382F, -2.0107F, 0.7338F, 0.4887F, -2.1468F, 1.2654F));

		PartDefinition cube_r167 = Big_snake2.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(39, 36).addBox(-2.0284F, -2.2224F, -3.579F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9382F, -0.4107F, 1.3338F, 0.6021F, -2.2166F, 1.1432F));

		PartDefinition Big_Snake3 = Clouds.addOrReplaceChild("Big_Snake3", CubeListBuilder.create(), PartPose.offset(-1.145F, 3.5618F, 9.5167F));

		PartDefinition cube_r168 = Big_Snake3.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(33, 36).addBox(-0.5F, -0.4F, -1.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5818F, 1.6559F, -1.2983F, -2.0333F, -0.1484F, -0.0436F));

		PartDefinition cube_r169 = Big_Snake3.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(36, 40).addBox(-2.0227F, 0.4792F, -0.1582F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0588F, -0.9178F, -0.4187F, 1.2217F, -0.5934F, -0.4189F));

		PartDefinition cube_r170 = Big_Snake3.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(43, 40).addBox(-2.146F, 1.4809F, 0.4968F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8588F, -1.5178F, -0.2187F, 0.5585F, -0.576F, -0.4712F));

		PartDefinition cube_r171 = Big_Snake3.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(48, 39).addBox(-2.3409F, 1.0808F, -2.2425F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5588F, -2.3178F, -0.5187F, -2.4609F, 2.1817F, -2.81F));

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
			this.animate(this.idleAnimationState, DemonessMythicalCreatureAnimations.Idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, DemonessMythicalCreatureAnimations.Walk, ageInTicks, 1.0F);
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