package de.jakob.lotm.rendering.models;// Made with Blockbench 5.0.3
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
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class TyrantMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "tyrant_mythical_creature"), "main");

	private final AnimationState idleAnimationState = new AnimationState();
	private final AnimationState walkAnimationState = new AnimationState();

	private final ModelPart root;
	private final ModelPart Body;
	private final ModelPart Topsection1;
	private final ModelPart Topmiddlesection1;
	private final ModelPart Lowermiddlesection1;
	private final ModelPart Bottomsegment1;
	private final ModelPart Topsection2;
	private final ModelPart Topmiddlesection2;
	private final ModelPart Lowermiddlesection2;
	private final ModelPart Bottomsegment2;
	private final ModelPart Topsection13;
	private final ModelPart Topmiddlesection13;
	private final ModelPart Lowermiddlesection13;
	private final ModelPart Bottomsegment13;
	private final ModelPart Topsection14;
	private final ModelPart Topmiddlesection14;
	private final ModelPart Lowermiddlesection14;
	private final ModelPart Bottomsegment14;
	private final ModelPart Topsection15;
	private final ModelPart Topmiddlesection15;
	private final ModelPart Lowermiddlesection15;
	private final ModelPart Bottomsegment15;
	private final ModelPart Topsection16;
	private final ModelPart Topmiddlesection16;
	private final ModelPart Lowermiddlesection16;
	private final ModelPart Bottomsegment16;
	private final ModelPart Topsection3;
	private final ModelPart Topmiddlesection3;
	private final ModelPart Lowermiddlesection3;
	private final ModelPart Bottomsegment3;
	private final ModelPart Topsection4;
	private final ModelPart Topmiddlesection4;
	private final ModelPart Lowermiddlesection4;
	private final ModelPart Bottomsegment4;
	private final ModelPart Topsection5;
	private final ModelPart Topmiddlesection5;
	private final ModelPart Lowermiddlesection5;
	private final ModelPart Bottomsegment5;
	private final ModelPart Topsection6;
	private final ModelPart Topmiddlesection6;
	private final ModelPart Lowermiddlesection6;
	private final ModelPart Bottomsegment6;
	private final ModelPart Topsection7;
	private final ModelPart Topmiddlesection7;
	private final ModelPart Lowermiddlesection7;
	private final ModelPart Bottomsegment7;
	private final ModelPart Topsection8;
	private final ModelPart Topmiddlesection8;
	private final ModelPart Lowermiddlesection8;
	private final ModelPart Bottomsegment8;

	public TyrantMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Body = root.getChild("Body");
		this.Topsection1 = this.Body.getChild("Topsection1");
		this.Topmiddlesection1 = this.Topsection1.getChild("Topmiddlesection1");
		this.Lowermiddlesection1 = this.Topmiddlesection1.getChild("Lowermiddlesection1");
		this.Bottomsegment1 = this.Lowermiddlesection1.getChild("Bottomsegment1");
		this.Topsection2 = this.Body.getChild("Topsection2");
		this.Topmiddlesection2 = this.Topsection2.getChild("Topmiddlesection2");
		this.Lowermiddlesection2 = this.Topmiddlesection2.getChild("Lowermiddlesection2");
		this.Bottomsegment2 = this.Lowermiddlesection2.getChild("Bottomsegment2");
		this.Topsection13 = this.Body.getChild("Topsection13");
		this.Topmiddlesection13 = this.Topsection13.getChild("Topmiddlesection13");
		this.Lowermiddlesection13 = this.Topmiddlesection13.getChild("Lowermiddlesection13");
		this.Bottomsegment13 = this.Lowermiddlesection13.getChild("Bottomsegment13");
		this.Topsection14 = this.Body.getChild("Topsection14");
		this.Topmiddlesection14 = this.Topsection14.getChild("Topmiddlesection14");
		this.Lowermiddlesection14 = this.Topmiddlesection14.getChild("Lowermiddlesection14");
		this.Bottomsegment14 = this.Lowermiddlesection14.getChild("Bottomsegment14");
		this.Topsection15 = this.Body.getChild("Topsection15");
		this.Topmiddlesection15 = this.Topsection15.getChild("Topmiddlesection15");
		this.Lowermiddlesection15 = this.Topmiddlesection15.getChild("Lowermiddlesection15");
		this.Bottomsegment15 = this.Lowermiddlesection15.getChild("Bottomsegment15");
		this.Topsection16 = this.Body.getChild("Topsection16");
		this.Topmiddlesection16 = this.Topsection16.getChild("Topmiddlesection16");
		this.Lowermiddlesection16 = this.Topmiddlesection16.getChild("Lowermiddlesection16");
		this.Bottomsegment16 = this.Lowermiddlesection16.getChild("Bottomsegment16");
		this.Topsection3 = this.Body.getChild("Topsection3");
		this.Topmiddlesection3 = this.Topsection3.getChild("Topmiddlesection3");
		this.Lowermiddlesection3 = this.Topmiddlesection3.getChild("Lowermiddlesection3");
		this.Bottomsegment3 = this.Lowermiddlesection3.getChild("Bottomsegment3");
		this.Topsection4 = this.Body.getChild("Topsection4");
		this.Topmiddlesection4 = this.Topsection4.getChild("Topmiddlesection4");
		this.Lowermiddlesection4 = this.Topmiddlesection4.getChild("Lowermiddlesection4");
		this.Bottomsegment4 = this.Lowermiddlesection4.getChild("Bottomsegment4");
		this.Topsection5 = this.Body.getChild("Topsection5");
		this.Topmiddlesection5 = this.Topsection5.getChild("Topmiddlesection5");
		this.Lowermiddlesection5 = this.Topmiddlesection5.getChild("Lowermiddlesection5");
		this.Bottomsegment5 = this.Lowermiddlesection5.getChild("Bottomsegment5");
		this.Topsection6 = this.Body.getChild("Topsection6");
		this.Topmiddlesection6 = this.Topsection6.getChild("Topmiddlesection6");
		this.Lowermiddlesection6 = this.Topmiddlesection6.getChild("Lowermiddlesection6");
		this.Bottomsegment6 = this.Lowermiddlesection6.getChild("Bottomsegment6");
		this.Topsection7 = this.Body.getChild("Topsection7");
		this.Topmiddlesection7 = this.Topsection7.getChild("Topmiddlesection7");
		this.Lowermiddlesection7 = this.Topmiddlesection7.getChild("Lowermiddlesection7");
		this.Bottomsegment7 = this.Lowermiddlesection7.getChild("Bottomsegment7");
		this.Topsection8 = this.Body.getChild("Topsection8");
		this.Topmiddlesection8 = this.Topsection8.getChild("Topmiddlesection8");
		this.Lowermiddlesection8 = this.Topmiddlesection8.getChild("Lowermiddlesection8");
		this.Bottomsegment8 = this.Lowermiddlesection8.getChild("Bottomsegment8");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(142, 89).addBox(-17.0F, -39.0F, -20.0F, 34.0F, 11.0F, 37.0F, new CubeDeformation(0.01F))
				.texOffs(0, 89).addBox(-17.0F, -61.0F, -20.0F, 34.0F, 22.0F, 37.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-18.0F, -79.0F, 5.0F, 36.0F, 40.0F, 49.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = Body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(156, 260).mirror().addBox(10.2652F, -11.749F, -24.5128F, 15.0F, 17.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -56.7122F, 11.2475F, 0.7459F, 0.274F, -0.2849F));

		PartDefinition cube_r2 = Body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 148).addBox(-18.0F, -18.5F, -24.5F, 34.0F, 14.0F, 31.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.99F, -37.3548F, 40.0779F, 0.5672F, 0.0F, 0.0F));

		PartDefinition cube_r3 = Body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(142, 137).addBox(-17.0F, -20.4056F, -24.05F, 34.0F, 14.0F, 31.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, -56.7122F, 11.2475F, 0.5672F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(156, 260).addBox(-25.2652F, -11.749F, -24.5128F, 15.0F, 17.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -56.7122F, 11.2475F, 0.7459F, -0.274F, 0.2849F));

		PartDefinition Topsection1 = Body.addOrReplaceChild("Topsection1", CubeListBuilder.create(), PartPose.offset(9.1217F, -16.2374F, -14.6024F));

		PartDefinition cube_r5 = Topsection1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1217F, -10.0626F, 2.6024F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection1 = Topsection1.addOrReplaceChild("Topmiddlesection1", CubeListBuilder.create(), PartPose.offset(-1.1017F, 8.9872F, 0.2041F));

		PartDefinition cube_r6 = Topmiddlesection1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(3.4283F, -12.7404F, -5.9264F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection1 = Topmiddlesection1.addOrReplaceChild("Lowermiddlesection1", CubeListBuilder.create(), PartPose.offset(3.7804F, 4.3467F, -9.1267F));

		PartDefinition cube_r7 = Lowermiddlesection1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(6.6839F, -4.3081F, -13.786F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment1 = Lowermiddlesection1.addOrReplaceChild("Bottomsegment1", CubeListBuilder.create(), PartPose.offset(22.993F, -0.33F, -45.6889F));

		PartDefinition cube_r8 = Bottomsegment1.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-8.116F, 1.0F, 12.0574F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection2 = Body.addOrReplaceChild("Topsection2", CubeListBuilder.create(), PartPose.offset(6.6829F, -11.9841F, -5.8537F));

		PartDefinition cube_r9 = Topsection2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6829F, -2.1884F, 4.8487F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection2 = Topsection2.addOrReplaceChild("Topmiddlesection2", CubeListBuilder.create(), PartPose.offset(-6.6829F, 11.9841F, 5.8537F));

		PartDefinition cube_r10 = Topmiddlesection2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection2 = Topmiddlesection2.addOrReplaceChild("Lowermiddlesection2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r11 = Lowermiddlesection2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment2 = Lowermiddlesection2.addOrReplaceChild("Bottomsegment2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r12 = Bottomsegment2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, -1.0472F, 0.0F));

		PartDefinition Topsection13 = Body.addOrReplaceChild("Topsection13", CubeListBuilder.create(), PartPose.offsetAndRotation(6.8109F, -12.0606F, 9.4213F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r13 = Topsection13.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.9396F, -2.112F, 4.5332F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection13 = Topsection13.addOrReplaceChild("Topmiddlesection13", CubeListBuilder.create(), PartPose.offset(-6.9396F, 12.0606F, 5.5382F));

		PartDefinition cube_r14 = Topmiddlesection13.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection13 = Topmiddlesection13.addOrReplaceChild("Lowermiddlesection13", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r15 = Lowermiddlesection13.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment13 = Lowermiddlesection13.addOrReplaceChild("Bottomsegment13", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r16 = Bottomsegment13.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, -1.0472F, 0.0F));

		PartDefinition Topsection14 = Body.addOrReplaceChild("Topsection14", CubeListBuilder.create(), PartPose.offsetAndRotation(5.1279F, -12.6429F, 7.5137F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r17 = Topsection14.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.968F, -13.6571F, -4.5944F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection14 = Topsection14.addOrReplaceChild("Topmiddlesection14", CubeListBuilder.create(), PartPose.offset(-5.032F, 12.6429F, 7.4056F));

		PartDefinition cube_r18 = Topmiddlesection14.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(11.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection14 = Topmiddlesection14.addOrReplaceChild("Lowermiddlesection14", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r19 = Lowermiddlesection14.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(18.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment14 = Lowermiddlesection14.addOrReplaceChild("Bottomsegment14", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r20 = Bottomsegment14.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(26.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection15 = Body.addOrReplaceChild("Topsection15", CubeListBuilder.create(), PartPose.offsetAndRotation(6.7777F, -11.8146F, 2.17F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r21 = Topsection15.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.3121F, -2.358F, 5.0345F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection15 = Topsection15.addOrReplaceChild("Topmiddlesection15", CubeListBuilder.create(), PartPose.offset(-6.3121F, 11.8146F, 6.0395F));

		PartDefinition cube_r22 = Topmiddlesection15.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection15 = Topmiddlesection15.addOrReplaceChild("Lowermiddlesection15", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r23 = Lowermiddlesection15.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment15 = Lowermiddlesection15.addOrReplaceChild("Bottomsegment15", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r24 = Bottomsegment15.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, -1.0472F, 0.0F));

		PartDefinition Topsection16 = Body.addOrReplaceChild("Topsection16", CubeListBuilder.create(), PartPose.offsetAndRotation(5.1053F, -12.6225F, 1.0665F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r25 = Topsection16.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9155F, -13.6775F, -4.5878F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection16 = Topsection16.addOrReplaceChild("Topmiddlesection16", CubeListBuilder.create(), PartPose.offset(-5.0845F, 12.6225F, 7.4122F));

		PartDefinition cube_r26 = Topmiddlesection16.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(11.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection16 = Topmiddlesection16.addOrReplaceChild("Lowermiddlesection16", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r27 = Lowermiddlesection16.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(18.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment16 = Lowermiddlesection16.addOrReplaceChild("Bottomsegment16", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r28 = Bottomsegment16.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(26.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection3 = Body.addOrReplaceChild("Topsection3", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.8797F, -12.5558F, 7.602F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r29 = Topsection3.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.8797F, -13.7442F, -4.602F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection3 = Topsection3.addOrReplaceChild("Topmiddlesection3", CubeListBuilder.create(), PartPose.offset(-5.1203F, 12.5558F, 7.398F));

		PartDefinition cube_r30 = Topmiddlesection3.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(27.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection3 = Topmiddlesection3.addOrReplaceChild("Lowermiddlesection3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r31 = Lowermiddlesection3.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(34.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment3 = Lowermiddlesection3.addOrReplaceChild("Bottomsegment3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r32 = Bottomsegment3.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(42.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection4 = Body.addOrReplaceChild("Topsection4", CubeListBuilder.create(), PartPose.offsetAndRotation(-13.2262F, -15.6853F, -9.6363F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r33 = Topsection4.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.9091F, 1.5127F, 8.7395F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection4 = Topsection4.addOrReplaceChild("Topmiddlesection4", CubeListBuilder.create(), PartPose.offset(-3.9693F, 8.4454F, 1.3336F));

		PartDefinition cube_r34 = Topmiddlesection4.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-5.9398F, -6.9327F, 7.406F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection4 = Topmiddlesection4.addOrReplaceChild("Lowermiddlesection4", CubeListBuilder.create(), PartPose.offset(9.098F, 4.1662F, -3.8819F));

		PartDefinition cube_r35 = Lowermiddlesection4.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-15.0378F, -11.0989F, 11.2878F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment4 = Lowermiddlesection4.addOrReplaceChild("Bottomsegment4", CubeListBuilder.create(), PartPose.offset(53.1712F, -1.1451F, -23.5055F));

		PartDefinition cube_r36 = Bottomsegment4.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-68.209F, -9.9538F, 34.7933F, 0.0F, -1.0472F, 0.0F));

		PartDefinition Topsection5 = Body.addOrReplaceChild("Topsection5", CubeListBuilder.create(), PartPose.offsetAndRotation(-11.8251F, -12.6053F, -2.977F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r37 = Topsection5.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8625F, -13.6947F, -4.6965F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection5 = Topsection5.addOrReplaceChild("Topmiddlesection5", CubeListBuilder.create(), PartPose.offset(-5.1375F, 12.6053F, 7.3035F));

		PartDefinition cube_r38 = Topmiddlesection5.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(11.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection5 = Topmiddlesection5.addOrReplaceChild("Lowermiddlesection5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r39 = Lowermiddlesection5.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(18.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment5 = Lowermiddlesection5.addOrReplaceChild("Bottomsegment5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r40 = Bottomsegment5.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(26.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection6 = Body.addOrReplaceChild("Topsection6", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.9762F, -12.0562F, -1.4205F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r41 = Topsection6.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0153F, -2.1164F, 4.3165F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection6 = Topsection6.addOrReplaceChild("Topmiddlesection6", CubeListBuilder.create(), PartPose.offset(-7.0153F, 12.0562F, 5.3215F));

		PartDefinition cube_r42 = Topmiddlesection6.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection6 = Topmiddlesection6.addOrReplaceChild("Lowermiddlesection6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r43 = Lowermiddlesection6.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment6 = Lowermiddlesection6.addOrReplaceChild("Bottomsegment6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r44 = Bottomsegment6.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, -1.0472F, 0.0F));

		PartDefinition Topsection7 = Body.addOrReplaceChild("Topsection7", CubeListBuilder.create(), PartPose.offsetAndRotation(-9.8712F, -12.6123F, -0.3928F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r45 = Topsection7.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8883F, -13.6877F, -4.5968F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Topmiddlesection7 = Topsection7.addOrReplaceChild("Topmiddlesection7", CubeListBuilder.create(), PartPose.offset(-5.1117F, 12.6123F, 7.4032F));

		PartDefinition cube_r46 = Topmiddlesection7.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(11.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition Lowermiddlesection7 = Topmiddlesection7.addOrReplaceChild("Lowermiddlesection7", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r47 = Lowermiddlesection7.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(18.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition Bottomsegment7 = Lowermiddlesection7.addOrReplaceChild("Bottomsegment7", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r48 = Bottomsegment7.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(26.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition Topsection8 = Body.addOrReplaceChild("Topsection8", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.2472F, -11.9719F, 4.1313F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r49 = Topsection8.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0382F, -2.2007F, 7.3718F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Topmiddlesection8 = Topsection8.addOrReplaceChild("Topmiddlesection8", CubeListBuilder.create(), PartPose.offset(-7.0382F, 11.9719F, 1.3768F));

		PartDefinition cube_r50 = Topmiddlesection8.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 5.995F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition Lowermiddlesection8 = Topmiddlesection8.addOrReplaceChild("Lowermiddlesection8", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r51 = Lowermiddlesection8.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 5.995F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition Bottomsegment8 = Lowermiddlesection8.addOrReplaceChild("Bottomsegment8", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r52 = Bottomsegment8.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 5.995F, 0.0F, -1.0472F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Reset all model parts to their default pose
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof LivingEntity living) {
			// Calculate movement speed
			boolean isWalking = limbSwingAmount > 0.01F;

			if (isWalking) {
				// Stop idle animation and start/continue walk animation
				this.idleAnimationState.stop();
				if (!this.walkAnimationState.isStarted()) {
					this.walkAnimationState.start((int) ageInTicks);
				}
				this.animate(this.walkAnimationState, TyrantMythicalCreatureAnimations.walk, ageInTicks, 1.0F);
			} else {
				// Stop walk animation and start/continue idle animation
				this.walkAnimationState.stop();
				if (!this.idleAnimationState.isStarted()) {
					this.idleAnimationState.start((int) ageInTicks);
				}
				this.animate(this.idleAnimationState, TyrantMythicalCreatureAnimations.idle, ageInTicks, 1.0F);
			}
		}
	}
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public @NotNull ModelPart root() {
		return this.root;
	}
}