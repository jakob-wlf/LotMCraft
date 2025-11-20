package de.jakob.lotm.rendering.models;// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TyrantMythicalCreatureModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "tyrant_mythical_creature"), "main");
	private final ModelPart bone;
	private final ModelPart bb_main;

	public TyrantMythicalCreatureModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -47.0F, -1.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(142, 89).addBox(-17.0F, -39.0F, -20.0F, 34.0F, 11.0F, 37.0F, new CubeDeformation(0.01F))
		.texOffs(0, 89).addBox(-17.0F, -61.0F, -20.0F, 34.0F, 22.0F, 37.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-18.0F, -79.0F, 5.0F, 36.0F, 40.0F, 49.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(156, 260).mirror().addBox(-9.5F, -9.5F, -7.5F, 15.0F, 17.0F, 17.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(17.4F, -51.5F, -7.6F, 0.7459F, 0.274F, -0.2849F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(-10.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.2248F, -14.1726F, -0.6708F, -2.5307F, -1.1781F, -3.1416F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(-23.8734F, -0.7901F, -6.8441F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.5307F, -0.3927F, 3.1416F));

		PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(2.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.2248F, -14.1726F, -0.6708F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(15.8735F, -0.7901F, -6.8441F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.5307F, 0.3927F, 3.1416F));

		PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(284, 78).addBox(-23.8734F, -0.7901F, -6.8441F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(-23.8734F, -0.7901F, 16.1559F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.5307F, -0.3927F, -3.1416F));

		PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(2.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.2248F, -14.1726F, -0.6708F, -2.5307F, 1.1781F, -3.1416F));

		PartDefinition cube_r8 = bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(284, 78).mirror().addBox(-10.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)).mirror(false), PartPose.offsetAndRotation(0.2248F, -14.1726F, -0.6708F, 0.6109F, 1.1781F, 0.0F));

		PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(284, 78).addBox(0.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(0.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, -2.5307F, -1.1781F, 3.1416F));

		PartDefinition cube_r10 = bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 148).addBox(-18.0F, -18.5F, -24.5F, 34.0F, 14.0F, 31.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.99F, -37.3548F, 40.0779F, 0.5672F, 0.0F, 0.0F));

		PartDefinition cube_r11 = bb_main.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(142, 137).addBox(-18.0F, -18.5F, -24.5F, 34.0F, 14.0F, 31.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(1.0F, -58.5611F, 10.6031F, 0.5672F, 0.0F, 0.0F));

		PartDefinition cube_r12 = bb_main.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(156, 260).addBox(-5.5F, -9.5F, -7.5F, 15.0F, 17.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.4F, -51.5F, -7.6F, 0.7459F, -0.274F, 0.2849F));

		PartDefinition cube_r13 = bb_main.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(248, 39).addBox(-1.6028F, 6.9391F, -97.8467F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.0F, 1.309F, 0.0F));

		PartDefinition cube_r14 = bb_main.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(248, 39).addBox(-6.3972F, 6.9391F, -97.8467F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -3.1416F, 1.309F, -3.1416F));

		PartDefinition cube_r15 = bb_main.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(284, 78).addBox(2.5127F, -8.2121F, -69.65F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -2.9234F, 1.1781F, 3.1416F));

		PartDefinition cube_r16 = bb_main.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(284, 78).addBox(-10.5127F, -8.2121F, -69.65F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.2182F, 1.1781F, 0.0F));

		PartDefinition cube_r17 = bb_main.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(284, 78).addBox(-10.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(-10.5127F, -25.7421F, -19.4793F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.6109F, 1.1781F, 0.0F));

		PartDefinition cube_r18 = bb_main.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(284, 78).addBox(2.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(2.5127F, -25.7421F, -19.4793F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -2.5307F, 1.1781F, 3.1416F));

		PartDefinition cube_r19 = bb_main.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(78, 193).addBox(2.5127F, -25.7421F, -19.4793F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(284, 78).addBox(2.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition cube_r20 = bb_main.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(78, 193).addBox(-10.5127F, -25.7421F, -19.4793F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F))
		.texOffs(284, 78).addBox(-10.5127F, -25.7421F, -42.4793F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -2.5307F, -1.1781F, 3.1416F));

		PartDefinition cube_r21 = bb_main.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(248, 39).addBox(-6.3972F, 6.9391F, -97.8467F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.0F, -1.309F, 0.0F));

		PartDefinition cube_r22 = bb_main.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(284, 78).addBox(2.5127F, -8.2121F, -69.65F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition cube_r23 = bb_main.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(284, 78).addBox(-10.5127F, -8.2121F, -69.65F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -2.9234F, -1.1781F, 3.1416F));

		PartDefinition cube_r24 = bb_main.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(248, 39).addBox(-1.6028F, 6.9391F, -97.8467F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-0.2248F, -14.1726F, -0.6708F, -3.1416F, -1.309F, 3.1416F));

		PartDefinition cube_r25 = bb_main.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(284, 78).addBox(0.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, 1.1781F, 0.0F));

		PartDefinition cube_r26 = bb_main.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 3.1416F, 1.0472F, 3.1416F));

		PartDefinition cube_r27 = bb_main.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(284, 78).addBox(0.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(0.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, 1.1781F, 0.0F));

		PartDefinition cube_r28 = bb_main.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, -2.9234F, 1.1781F, -3.1416F));

		PartDefinition cube_r29 = bb_main.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, -2.5307F, 1.1781F, 3.1416F));

		PartDefinition cube_r30 = bb_main.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(248, 39).addBox(-7.9726F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, 1.0472F, 0.0F));

		PartDefinition cube_r31 = bb_main.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(284, 78).addBox(0.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, -2.9234F, -1.1781F, 3.1416F));

		PartDefinition cube_r32 = bb_main.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(248, 39).addBox(-0.0274F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.0F, -1.0472F, 0.0F));

		PartDefinition cube_r33 = bb_main.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -6.995F, -64.1599F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.2182F, -1.1781F, 0.0F));

		PartDefinition cube_r34 = bb_main.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(284, 78).addBox(-8.1834F, -22.5166F, -37.8728F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F))
		.texOffs(78, 193).addBox(-8.1834F, -22.5166F, -14.8728F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 0.6109F, -1.1781F, 0.0F));

		PartDefinition cube_r35 = bb_main.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(248, 39).addBox(-7.9726F, 6.9391F, -91.9673F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, -1.005F, 3.1416F, -1.0472F, -3.1416F));

		PartDefinition cube_r36 = bb_main.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(284, 78).addBox(15.8735F, 1.2036F, -27.1787F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.9234F, 0.3927F, -3.1416F));

		PartDefinition cube_r37 = bb_main.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(248, 39).addBox(-20.5274F, 6.9391F, -56.4603F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, 3.1416F, -0.5236F, 3.1416F));

		PartDefinition cube_r38 = bb_main.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(284, 78).addBox(-23.8734F, 1.2036F, -27.1787F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.9234F, -0.3927F, -3.1416F));

		PartDefinition cube_r39 = bb_main.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(78, 193).addBox(15.8735F, -0.7901F, 16.1559F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -2.5307F, 0.3927F, -3.1416F));

		PartDefinition cube_r40 = bb_main.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(248, 39).addBox(12.5274F, 6.9391F, -56.4603F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(0.0F, -14.1726F, 39.995F, -3.1416F, 0.5236F, -3.1416F));

		PartDefinition cube_r41 = bb_main.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(18.4842F, -7.2116F, -37.311F, 0.2182F, -0.3927F, 0.0F));

		PartDefinition cube_r42 = bb_main.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(248, 39).addBox(-5.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(-26.6773F, -2.2335F, -57.1566F, 0.0F, 0.5236F, 0.0F));

		PartDefinition cube_r43 = bb_main.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(284, 78).addBox(-3.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(11.4482F, -19.9907F, -20.3248F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition cube_r44 = bb_main.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(284, 78).addBox(-5.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-18.4842F, -7.2116F, -37.311F, 0.2182F, 0.3927F, 0.0F));

		PartDefinition cube_r45 = bb_main.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(78, 193).addBox(-3.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -26.3F, -12.0F, 0.6109F, -0.3927F, 0.0F));

		PartDefinition cube_r46 = bb_main.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(284, 78).addBox(-5.0F, -5.0F, -23.0F, 8.0F, 8.0F, 24.0F, new CubeDeformation(-0.25F)), PartPose.offsetAndRotation(-11.4482F, -19.9907F, -20.3248F, 0.6109F, 0.3927F, 0.0F));

		PartDefinition cube_r47 = bb_main.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(248, 39).addBox(-3.0F, -5.0F, -30.0F, 8.0F, 8.0F, 31.0F, new CubeDeformation(-0.75F)), PartPose.offsetAndRotation(26.6773F, -2.2335F, -57.1566F, 0.0F, -0.5236F, 0.0F));

		PartDefinition cube_r48 = bb_main.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(78, 193).addBox(-5.0F, -5.0F, -11.0F, 8.0F, 8.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -26.3F, -12.0F, 0.6109F, 0.3927F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}