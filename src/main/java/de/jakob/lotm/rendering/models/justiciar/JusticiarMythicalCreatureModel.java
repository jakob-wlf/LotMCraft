package de.jakob.lotm.rendering.models.justiciar;// Made with Blockbench 5.1.4
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

public class JusticiarMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "justiciar_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Entire_body;
	private final ModelPart Pillar;
	private final ModelPart Pillar_body;
	private final ModelPart Foundations;
	private final ModelPart Enchanting_particles;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone;
	private final ModelPart Earths;
	private final ModelPart Earth2;
	private final ModelPart Earth;
	private final ModelPart Heaven;
	private final ModelPart Free_clouds;
	private final ModelPart Main_Cloud;
	private final ModelPart back_clouds;
	private final ModelPart Second_middle_clouds;
	private final ModelPart Middle_clouds;
	private final ModelPart front_clouds;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();


	public JusticiarMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Entire_body = root.getChild("Entire_body");
		this.Pillar = this.Entire_body.getChild("Pillar");
		this.Pillar_body = this.Pillar.getChild("Pillar_body");
		this.Foundations = this.Pillar.getChild("Foundations");
		this.Enchanting_particles = this.Entire_body.getChild("Enchanting_particles");
		this.bone2 = this.Enchanting_particles.getChild("bone2");
		this.bone3 = this.Enchanting_particles.getChild("bone3");
		this.bone = this.Enchanting_particles.getChild("bone");
		this.Earths = this.Entire_body.getChild("Earths");
		this.Earth2 = this.Earths.getChild("Earth2");
		this.Earth = this.Earths.getChild("Earth");
		this.Heaven = this.Entire_body.getChild("Heaven");
		this.Free_clouds = this.Heaven.getChild("Free_clouds");
		this.Main_Cloud = this.Heaven.getChild("Main_Cloud");
		this.back_clouds = this.Main_Cloud.getChild("back_clouds");
		this.Second_middle_clouds = this.Main_Cloud.getChild("Second_middle_clouds");
		this.Middle_clouds = this.Main_Cloud.getChild("Middle_clouds");
		this.front_clouds = this.Main_Cloud.getChild("front_clouds");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Entire_body = partdefinition.addOrReplaceChild("Entire_body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Pillar = Entire_body.addOrReplaceChild("Pillar", CubeListBuilder.create(), PartPose.offset(0.0F, -17.0F, 0.0F));

		PartDefinition Pillar_body = Pillar.addOrReplaceChild("Pillar_body", CubeListBuilder.create().texOffs(0, 11).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition cube_r1 = Pillar_body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 11).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r2 = Pillar_body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -28.5F, 0.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r3 = Pillar_body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 11).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.5F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r4 = Pillar_body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 11).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.5F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r5 = Pillar_body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 1.5708F, 3.1416F));

		PartDefinition Foundations = Pillar.addOrReplaceChild("Foundations", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -2.0F, -4.1F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 34).addBox(-4.0F, -32.0F, -4.1F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition cube_r6 = Foundations.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -30.0F, -3.0F, 0.0F, -2.3387F, -3.1416F));

		PartDefinition cube_r7 = Foundations.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -30.0F, 3.0F, 0.0F, 2.4435F, -3.1416F));

		PartDefinition cube_r8 = Foundations.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -30.0F, 3.0F, 0.0F, 0.7156F, -3.1416F));

		PartDefinition cube_r9 = Foundations.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -30.0F, -3.0F, 0.0F, -0.6981F, -3.1416F));

		PartDefinition cube_r10 = Foundations.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.0F, -3.0F, 0.0F, -2.2689F, 0.0F));

		PartDefinition cube_r11 = Foundations.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.0F, 3.0F, 0.0F, 0.7505F, 0.0F));

		PartDefinition cube_r12 = Foundations.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.0F, 3.0F, 0.0F, 2.4435F, 0.0F));

		PartDefinition cube_r13 = Foundations.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.0F, -3.0F, 0.0F, -0.7505F, 0.0F));

		PartDefinition Enchanting_particles = Entire_body.addOrReplaceChild("Enchanting_particles", CubeListBuilder.create(), PartPose.offset(0.0F, -16.85F, 0.0F));

		PartDefinition bone2 = Enchanting_particles.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.5F, -1.15F, -0.1F));

		PartDefinition cube_r14 = bone2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 6.1F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r15 = bone2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 2.0F, -3.9F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition cube_r16 = bone2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 14.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition bone3 = Enchanting_particles.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.6F, -1.25F, -0.9F));

		PartDefinition cube_r17 = bone3.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -3.1F, -5.2F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r18 = bone3.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, 6.1F, -3.1F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r19 = bone3.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -6.9F, -3.1F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition bone = Enchanting_particles.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.2333F, 1.171F, 0.87F));

		PartDefinition cube_r20 = bone.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.1F, 6.1F, 3.0288F, 2.7245F, -0.4597F));

		PartDefinition cube_r21 = bone.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7667F, 0.679F, -0.87F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r22 = bone.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2333F, 6.679F, 5.13F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition Earths = Entire_body.addOrReplaceChild("Earths", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Earth2 = Earths.addOrReplaceChild("Earth2", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, 0.0F, -8.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 6).addBox(-8.0F, 0.0F, -8.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r23 = Earth2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, -0.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r24 = Earth2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(-5, 23).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, -0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r25 = Earth2.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, 4.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r26 = Earth2.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, -5.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r27 = Earth2.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.5F, -0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r28 = Earth2.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, 4.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r29 = Earth2.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.5F, 4.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Earth = Earths.addOrReplaceChild("Earth", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, 0.0F, -8.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 6).addBox(-8.0F, 0.0F, -8.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r30 = Earth.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, -0.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r31 = Earth.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, 4.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r32 = Earth.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, -5.5F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r33 = Earth.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.5F, -0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r34 = Earth.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(24, 0).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, 4.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r35 = Earth.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(-5, 23).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, -0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r36 = Earth.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(24, 6).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.5F, 4.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Heaven = Entire_body.addOrReplaceChild("Heaven", CubeListBuilder.create(), PartPose.offset(0.0F, -33.0F, 0.0F));

		PartDefinition Free_clouds = Heaven.addOrReplaceChild("Free_clouds", CubeListBuilder.create().texOffs(23, 16).addBox(-9.1F, -1.0F, -6.5F, 4.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(23, 18).addBox(-2.2F, 1.0F, 5.5F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(2.8F, -3.0F, -7.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(1.1F, 5.0F, -0.3F));

		PartDefinition Main_Cloud = Heaven.addOrReplaceChild("Main_Cloud", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition back_clouds = Main_Cloud.addOrReplaceChild("back_clouds", CubeListBuilder.create().texOffs(20, 16).addBox(7.5667F, -1.0F, -2.9F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(5.6667F, -1.0F, -8.6F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(5.6667F, -1.0F, 1.0F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.7333F, 1.0F, -1.2F));

		PartDefinition Second_middle_clouds = Main_Cloud.addOrReplaceChild("Second_middle_clouds", CubeListBuilder.create().texOffs(20, 16).addBox(-8.72F, -0.58F, -0.24F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-1.72F, -0.58F, -0.24F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-1.72F, -0.58F, -7.24F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-1.62F, -0.58F, -2.54F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.72F, -2.68F, -7.24F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-1.72F, -0.58F, -14.14F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(5.62F, 0.58F, 3.14F));

		PartDefinition Middle_clouds = Main_Cloud.addOrReplaceChild("Middle_clouds", CubeListBuilder.create().texOffs(20, 16).addBox(-10.1462F, -0.6077F, 0.2308F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.2462F, -0.6077F, -2.8692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.2462F, -2.6077F, -2.8692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.1462F, -0.6077F, -9.7692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-5.0462F, -0.6077F, 2.1308F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-5.0462F, -0.6077F, -8.5692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(1.9539F, -0.6077F, -8.5692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-6.6462F, -2.7077F, -2.8692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-10.1462F, -0.6077F, -2.8692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-10.1462F, -0.6077F, 0.2308F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-8.3461F, -0.6077F, -5.9692F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1462F, 0.6077F, -1.2308F));

		PartDefinition front_clouds = Main_Cloud.addOrReplaceChild("front_clouds", CubeListBuilder.create().texOffs(20, 16).addBox(-4.925F, -1.0F, -2.425F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.025F, -1.0F, -0.525F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.025F, -1.0F, -5.525F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(20, 16).addBox(-3.025F, -1.0F, -5.525F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.175F, 1.0F, -1.675F));

		return LayerDefinition.create(meshdefinition, 48, 48);
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
			this.animate(this.idleAnimationState, JusticiarMythicalCreatureAnimations.Idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, JusticiarMythicalCreatureAnimations.Walk, ageInTicks, 1.0F);
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