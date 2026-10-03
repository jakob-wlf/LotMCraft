package de.jakob.lotm.entity.client.spirits.knowledge_demon;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandAnimations;
import de.jakob.lotm.entity.client.spirits.asmann.AsmannAnimations;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import de.jakob.lotm.entity.custom.spirits.AsmannEntity;
import de.jakob.lotm.entity.custom.spirits.KnowledgeDemonEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class KnowledgeDemonModel<T extends KnowledgeDemonEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "knowledge_demon"), "main");
	private final ModelPart root;
	private final ModelPart Knowledge_demon;
	private final ModelPart Enchanting_particles;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone;
	private final ModelPart body;
	private final ModelPart Lightball2;
	private final ModelPart body7;
	private final ModelPart body8;
	private final ModelPart body9;
	private final ModelPart body10;
	private final ModelPart body11;
	private final ModelPart Lightball;
	private final ModelPart body4;
	private final ModelPart body6;
	private final ModelPart body5;
	private final ModelPart body3;
	private final ModelPart body2;
	private final ModelPart Lightball3;
	private final ModelPart body12;
	private final ModelPart body13;
	private final ModelPart body14;
	private final ModelPart body15;
	private final ModelPart body16;
	private final ModelPart Lightball4;
	private final ModelPart body17;
	private final ModelPart body18;
	private final ModelPart body19;
	private final ModelPart body20;
	private final ModelPart body21;

	public KnowledgeDemonModel(ModelPart root) {
		this.root = root;
		this.Knowledge_demon = root.getChild("Knowledge_demon");
		this.Enchanting_particles = this.Knowledge_demon.getChild("Enchanting_particles");
		this.bone2 = this.Enchanting_particles.getChild("bone2");
		this.bone3 = this.Enchanting_particles.getChild("bone3");
		this.bone = this.Enchanting_particles.getChild("bone");
		this.body = this.Knowledge_demon.getChild("body");
		this.Lightball2 = this.body.getChild("Lightball2");
		this.body7 = this.Lightball2.getChild("body7");
		this.body8 = this.body7.getChild("body8");
		this.body9 = this.body7.getChild("body9");
		this.body10 = this.Lightball2.getChild("body10");
		this.body11 = this.Lightball2.getChild("body11");
		this.Lightball = this.body.getChild("Lightball");
		this.body4 = this.Lightball.getChild("body4");
		this.body6 = this.body4.getChild("body6");
		this.body5 = this.body4.getChild("body5");
		this.body3 = this.Lightball.getChild("body3");
		this.body2 = this.Lightball.getChild("body2");
		this.Lightball3 = this.body.getChild("Lightball3");
		this.body12 = this.Lightball3.getChild("body12");
		this.body13 = this.body12.getChild("body13");
		this.body14 = this.body12.getChild("body14");
		this.body15 = this.Lightball3.getChild("body15");
		this.body16 = this.Lightball3.getChild("body16");
		this.Lightball4 = this.body.getChild("Lightball4");
		this.body17 = this.Lightball4.getChild("body17");
		this.body18 = this.body17.getChild("body18");
		this.body19 = this.body17.getChild("body19");
		this.body20 = this.Lightball4.getChild("body20");
		this.body21 = this.Lightball4.getChild("body21");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Knowledge_demon = partdefinition.addOrReplaceChild("Knowledge_demon", CubeListBuilder.create(), PartPose.offset(0.0F, -9.65F, -4.8F));

		PartDefinition Enchanting_particles = Knowledge_demon.addOrReplaceChild("Enchanting_particles", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 3.0F, 23.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition bone2 = Enchanting_particles.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.5F, -1.15F, -0.1F));

		PartDefinition cube_r1 = bone2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.9F, -23.9F, 12.1F, 0.4179F, 0.3026F, 0.1869F));

		PartDefinition cube_r2 = bone2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.9F, -5.9F, 12.1F, 0.4179F, 0.3026F, 0.1869F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(8, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -10.0F, -11.9F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 12.1F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r5 = bone2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.2F, 2.0F, -3.9F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition cube_r6 = bone2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.1F, 14.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition cube_r7 = bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 10.0F, -11.3F, 2.9899F, -1.0256F, -2.7875F));

		PartDefinition cube_r8 = bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -22.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition cube_r9 = bone2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, 14.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition bone3 = Enchanting_particles.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.6F, -1.25F, -0.9F));

		PartDefinition cube_r10 = bone3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, 5.9F, 10.8F, -3.0258F, -0.7773F, 2.9971F));

		PartDefinition cube_r11 = bone3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, -22.1F, -5.2F, 0.948F, -1.151F, -1.1872F));

		PartDefinition cube_r12 = bone3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, -3.1F, -5.2F, 0.948F, -1.151F, -1.1872F));

		PartDefinition cube_r13 = bone3.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.1F, -3.1F, -10.0F, 0.013F, 0.3678F, -1.3893F));

		PartDefinition cube_r14 = bone3.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -3.1F, -10.0F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r15 = bone3.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(8, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, 6.1F, -9.3F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r16 = bone3.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(4, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7F, -6.9F, 12.9F, -3.0131F, 0.6717F, -2.8267F));

		PartDefinition cube_r17 = bone3.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.3F, -15.9F, -3.1F, -1.6141F, 1.3457F, -2.1348F));

		PartDefinition cube_r18 = bone3.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7F, -6.9F, -3.1F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition bone = Enchanting_particles.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.2333F, 1.171F, 0.87F));

		PartDefinition cube_r19 = bone.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(4, 2).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.1F, 11.0F, 3.0288F, 2.7245F, -0.4597F));

		PartDefinition cube_r20 = bone.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7667F, -19.321F, 13.13F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r21 = bone.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7667F, 0.679F, -0.87F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r22 = bone.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(8, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.4333F, -18.321F, -1.87F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r23 = bone.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(8, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.5667F, -11.321F, -1.87F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r24 = bone.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.2333F, -23.321F, -5.17F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r25 = bone.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.7667F, 6.679F, -5.17F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r26 = bone.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2333F, 6.679F, 10.83F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition body = Knowledge_demon.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-1.0005F, 1.6813F, 6.8787F));

		PartDefinition Lightball2 = body.addOrReplaceChild("Lightball2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -6.3F));

		PartDefinition body7 = Lightball2.addOrReplaceChild("body7", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, 4.4925F));

		PartDefinition body_r1 = body7.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(10, 0).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body8 = body7.addOrReplaceChild("body8", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r2 = body8.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r3 = body8.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body9 = body7.addOrReplaceChild("body9", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r4 = body9.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r5 = body9.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r6 = body9.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r7 = body9.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body10 = Lightball2.addOrReplaceChild("body10", CubeListBuilder.create(), PartPose.offset(0.937F, -0.3976F, 4.3778F));

		PartDefinition body_r8 = body10.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r9 = body10.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body11 = Lightball2.addOrReplaceChild("body11", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, 4.325F));

		PartDefinition body_r10 = body11.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r11 = body11.addOrReplaceChild("body_r11", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r12 = body11.addOrReplaceChild("body_r12", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r13 = body11.addOrReplaceChild("body_r13", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition Lightball = body.addOrReplaceChild("Lightball", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body4 = Lightball.addOrReplaceChild("body4", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, -1.8075F));

		PartDefinition body_r14 = body4.addOrReplaceChild("body_r14", CubeListBuilder.create().texOffs(10, 0).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body6 = body4.addOrReplaceChild("body6", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r15 = body6.addOrReplaceChild("body_r15", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r16 = body6.addOrReplaceChild("body_r16", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body5 = body4.addOrReplaceChild("body5", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r17 = body5.addOrReplaceChild("body_r17", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r18 = body5.addOrReplaceChild("body_r18", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r19 = body5.addOrReplaceChild("body_r19", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r20 = body5.addOrReplaceChild("body_r20", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body3 = Lightball.addOrReplaceChild("body3", CubeListBuilder.create(), PartPose.offset(0.937F, -0.3976F, -1.9222F));

		PartDefinition body_r21 = body3.addOrReplaceChild("body_r21", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r22 = body3.addOrReplaceChild("body_r22", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body2 = Lightball.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, -1.975F));

		PartDefinition body_r23 = body2.addOrReplaceChild("body_r23", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r24 = body2.addOrReplaceChild("body_r24", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r25 = body2.addOrReplaceChild("body_r25", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r26 = body2.addOrReplaceChild("body_r26", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition Lightball3 = body.addOrReplaceChild("Lightball3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -7.0F));

		PartDefinition body12 = Lightball3.addOrReplaceChild("body12", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, 5.1925F));

		PartDefinition body_r27 = body12.addOrReplaceChild("body_r27", CubeListBuilder.create().texOffs(10, 0).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body13 = body12.addOrReplaceChild("body13", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r28 = body13.addOrReplaceChild("body_r28", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r29 = body13.addOrReplaceChild("body_r29", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body14 = body12.addOrReplaceChild("body14", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r30 = body14.addOrReplaceChild("body_r30", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r31 = body14.addOrReplaceChild("body_r31", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r32 = body14.addOrReplaceChild("body_r32", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r33 = body14.addOrReplaceChild("body_r33", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body15 = Lightball3.addOrReplaceChild("body15", CubeListBuilder.create(), PartPose.offset(0.937F, -0.3976F, 5.0778F));

		PartDefinition body_r34 = body15.addOrReplaceChild("body_r34", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r35 = body15.addOrReplaceChild("body_r35", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body16 = Lightball3.addOrReplaceChild("body16", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, 5.025F));

		PartDefinition body_r36 = body16.addOrReplaceChild("body_r36", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r37 = body16.addOrReplaceChild("body_r37", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r38 = body16.addOrReplaceChild("body_r38", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r39 = body16.addOrReplaceChild("body_r39", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition Lightball4 = body.addOrReplaceChild("Lightball4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -7.0F));

		PartDefinition body17 = Lightball4.addOrReplaceChild("body17", CubeListBuilder.create(), PartPose.offset(1.0234F, -0.509F, 5.1925F));

		PartDefinition body_r40 = body17.addOrReplaceChild("body_r40", CubeListBuilder.create().texOffs(10, 0).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body18 = body17.addOrReplaceChild("body18", CubeListBuilder.create(), PartPose.offset(-0.2229F, -0.4634F, 0.1421F));

		PartDefinition body_r41 = body18.addOrReplaceChild("body_r41", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -3.3F, -0.8F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r42 = body18.addOrReplaceChild("body_r42", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.5F, 0.5F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body19 = body17.addOrReplaceChild("body19", CubeListBuilder.create(), PartPose.offset(0.1671F, 0.1724F, -0.0484F));

		PartDefinition body_r43 = body19.addOrReplaceChild("body_r43", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7464F, -0.2611F, -2.0163F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r44 = body19.addOrReplaceChild("body_r44", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5536F, -0.7611F, -1.0163F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body_r45 = body19.addOrReplaceChild("body_r45", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8536F, 0.7389F, 2.2837F, 2.6659F, -0.3347F, 0.5749F));

		PartDefinition body_r46 = body19.addOrReplaceChild("body_r46", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, -1.5F, -1.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3464F, 1.7389F, 0.7837F, 0.9212F, -0.9966F, 2.7134F));

		PartDefinition body20 = Lightball4.addOrReplaceChild("body20", CubeListBuilder.create(), PartPose.offset(0.937F, -0.3976F, 5.0778F));

		PartDefinition body_r47 = body20.addOrReplaceChild("body_r47", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9635F, -9.2747F, 2.0568F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r48 = body20.addOrReplaceChild("body_r48", CubeListBuilder.create().texOffs(18, 18).mirror().addBox(-1.0F, -0.5F, -6.5F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.4365F, -2.7747F, -2.3432F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body21 = Lightball4.addOrReplaceChild("body21", CubeListBuilder.create(), PartPose.offset(0.9F, -0.65F, 5.025F));

		PartDefinition body_r49 = body21.addOrReplaceChild("body_r49", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.4F, 0.85F, 3.875F, 1.0951F, -0.3347F, 0.5749F));

		PartDefinition body_r50 = body21.addOrReplaceChild("body_r50", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.9F, 2.45F, 1.175F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r51 = body21.addOrReplaceChild("body_r51", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.3F, -1.95F, -1.225F, 0.6496F, 0.9966F, -0.4282F));

		PartDefinition body_r52 = body21.addOrReplaceChild("body_r52", CubeListBuilder.create().texOffs(14, 25).mirror().addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.2F, -0.75F, -3.125F, 1.0951F, -0.3347F, 0.5749F));

		return LayerDefinition.create(meshdefinition, 32, 32);
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
	public void setupAnim(KnowledgeDemonEntity entity, float limbSwing, float limbSwingAmount,
	                      float ageInTicks, float netHeadYaw, float headPitch) {

		// Lazy-init buffers once we know part count
		if (this.idlePose == null) {
			int partCount = (int) this.root().getAllParts().count();
			this.idlePose = new float[partCount * FLOATS_PER_PART];
			this.walkPose = new float[partCount * FLOATS_PER_PART];
		}

		boolean isWalking = limbSwingAmount > 0.01F;
		this.walkBlend = Mth.lerp(BLEND_SPEED, this.walkBlend, isWalking ? 1.0F : 0.0F);

		// Keep both states running so their internal timers don't reset
		if (!entity.IDLE_ANIMATION.isStarted()) entity.IDLE_ANIMATION.start((int) ageInTicks);
		if (!entity.FLY_ANIMATION.isStarted()) entity.FLY_ANIMATION.start((int) ageInTicks);

		// Sample idle into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.IDLE_ANIMATION, KnowledgeDemonAnimations.Idle, ageInTicks, 1.0F);
		capturePoseInto(this.idlePose);

		// Sample walk into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.FLY_ANIMATION, KnowledgeDemonAnimations.walk, ageInTicks, 1.0F);
		capturePoseInto(this.walkPose);

		// Write the lerped result
		applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Knowledge_demon.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}