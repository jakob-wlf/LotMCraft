package de.jakob.lotm.entity.client.spirits.grim_reaper;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandAnimations;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritGrimReaperEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GrimReaperModel<T extends SpiritGrimReaperEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "grim_reaper"), "main");
	private final ModelPart root;
	private final ModelPart reaper;
	private final ModelPart robe;
	private final ModelPart robe_side_front_right;
	private final ModelPart bone11;
	private final ModelPart bone12;
	private final ModelPart bone13;
	private final ModelPart robe_side_middle_right;
	private final ModelPart bone9;
	private final ModelPart bone10;
	private final ModelPart robe_side_back_right;
	private final ModelPart bone;
	private final ModelPart bone8;
	private final ModelPart back_robe;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart robe_side_back_left;
	private final ModelPart bone14;
	private final ModelPart robe_side_middle_left;
	private final ModelPart bone15;
	private final ModelPart robe_side_front_left;
	private final ModelPart bone17;
	private final ModelPart bone16;
	private final ModelPart reaper_body;
	private final ModelPart head;
	private final ModelPart jacket;
	private final ModelPart arm_left;
	private final ModelPart forearm_left;
	private final ModelPart chain;
	private final ModelPart chain_forearm;
	private final ModelPart lantern;
	private final ModelPart arm_right;
	private final ModelPart forearm_right;
	private final ModelPart scythe;
	private final ModelPart legs;

	public GrimReaperModel(ModelPart root) {
		this.root = root;
		this.reaper = root.getChild("reaper");
		this.robe = this.reaper.getChild("robe");
		this.robe_side_front_right = this.robe.getChild("robe_side_front_right");
		this.bone11 = this.robe_side_front_right.getChild("bone11");
		this.bone12 = this.bone11.getChild("bone12");
		this.bone13 = this.bone12.getChild("bone13");
		this.robe_side_middle_right = this.robe.getChild("robe_side_middle_right");
		this.bone9 = this.robe_side_middle_right.getChild("bone9");
		this.bone10 = this.bone9.getChild("bone10");
		this.robe_side_back_right = this.robe.getChild("robe_side_back_right");
		this.bone = this.robe_side_back_right.getChild("bone");
		this.bone8 = this.bone.getChild("bone8");
		this.back_robe = this.robe.getChild("back_robe");
		this.bone4 = this.back_robe.getChild("bone4");
		this.bone5 = this.back_robe.getChild("bone5");
		this.bone2 = this.back_robe.getChild("bone2");
		this.bone3 = this.bone2.getChild("bone3");
		this.bone6 = this.back_robe.getChild("bone6");
		this.bone7 = this.bone6.getChild("bone7");
		this.robe_side_back_left = this.robe.getChild("robe_side_back_left");
		this.bone14 = this.robe_side_back_left.getChild("bone14");
		this.robe_side_middle_left = this.robe.getChild("robe_side_middle_left");
		this.bone15 = this.robe_side_middle_left.getChild("bone15");
		this.robe_side_front_left = this.robe.getChild("robe_side_front_left");
		this.bone17 = this.robe_side_front_left.getChild("bone17");
		this.bone16 = this.bone17.getChild("bone16");
		this.reaper_body = this.reaper.getChild("reaper_body");
		this.head = this.reaper_body.getChild("head");
		this.jacket = this.reaper_body.getChild("jacket");
		this.arm_left = this.reaper_body.getChild("arm_left");
		this.forearm_left = this.arm_left.getChild("forearm_left");
		this.chain = this.arm_left.getChild("chain");
		this.chain_forearm = this.chain.getChild("chain_forearm");
		this.lantern = this.chain_forearm.getChild("lantern");
		this.arm_right = this.reaper_body.getChild("arm_right");
		this.forearm_right = this.arm_right.getChild("forearm_right");
		this.scythe = this.forearm_right.getChild("scythe");
		this.legs = this.reaper_body.getChild("legs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition reaper = partdefinition.addOrReplaceChild("reaper", CubeListBuilder.create(), PartPose.offset(0.2822F, 1.5508F, 4.4749F));

		PartDefinition robe = reaper.addOrReplaceChild("robe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition robe_side_front_right = robe.addOrReplaceChild("robe_side_front_right", CubeListBuilder.create(), PartPose.offset(-5.3535F, -13.8297F, -4.9546F));

		PartDefinition cube_r1 = robe_side_front_right.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 47).addBox(-2.9845F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.5F, -0.6F, 0.1064F, -0.2722F, -0.3619F));

		PartDefinition cube_r2 = robe_side_front_right.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 47).addBox(-2.9845F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, 8.7F, 0.2F, 0.0925F, -0.332F, -0.9533F));

		PartDefinition cube_r3 = robe_side_front_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(41, 13).addBox(-4.9845F, -0.1682F, -2.7194F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 4.6F, 0.5F, 0.3384F, -0.0644F, -2.0658F));

		PartDefinition cube_r4 = robe_side_front_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(41, 13).addBox(-4.9845F, -0.1682F, -2.7194F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.215F, -0.0949F, -1.348F));

		PartDefinition bone11 = robe_side_front_right.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(-2.7F, 11.5F, -2.2F));

		PartDefinition cube_r5 = bone11.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(22, 47).addBox(-2.9845F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2F, 1.7F, 0.2F, 0.2463F, -0.1581F, -0.9952F));

		PartDefinition cube_r6 = bone11.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(22, 47).addBox(-2.9845F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.8F, 0.1821F, -0.2292F, -0.665F));

		PartDefinition bone12 = bone11.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(-3.6F, 4.0F, -0.7F));

		PartDefinition cube_r7 = bone12.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(12, 42).addBox(-5.9845F, -0.1682F, -2.7194F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.9F, 0.1968F, -0.1423F, -1.3037F));

		PartDefinition bone13 = bone12.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offset(-1.4F, 5.6F, -0.7F));

		PartDefinition cube_r8 = bone13.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(12, 42).addBox(-5.9845F, -0.1682F, -2.7194F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.8F, 0.2319F, -0.0711F, -1.6295F));

		PartDefinition robe_side_middle_right = robe.addOrReplaceChild("robe_side_middle_right", CubeListBuilder.create(), PartPose.offset(-4.7535F, -14.3297F, -1.1546F));

		PartDefinition cube_r9 = robe_side_middle_right.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(39, 19).addBox(-6.9845F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -0.8F, 0.1F, 0.18F, -0.05F, -1.2167F));

		PartDefinition cube_r10 = robe_side_middle_right.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(39, 19).addBox(-6.9845F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, 5.7F, -0.4F, 0.1316F, -0.1343F, -1.1439F));

		PartDefinition cube_r11 = robe_side_middle_right.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(39, 19).addBox(-6.9845F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8F, 12.1F, -1.1F, 0.0053F, -0.0751F, -1.4363F));

		PartDefinition bone9 = robe_side_middle_right.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offset(-6.9F, 19.1F, -1.6F));

		PartDefinition cube_r12 = bone9.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(39, 19).addBox(-6.9845F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0366F, -0.0658F, -1.8736F));

		PartDefinition bone10 = bone9.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(2.3F, 6.2F, -0.5F));

		PartDefinition cube_r13 = bone10.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(39, 19).addBox(-6.9845F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0013F, -0.0753F, -1.3488F));

		PartDefinition robe_side_back_right = robe.addOrReplaceChild("robe_side_back_right", CubeListBuilder.create(), PartPose.offset(-5.1822F, -15.4508F, 4.1251F));

		PartDefinition cube_r14 = robe_side_back_right.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 27).addBox(-6.571F, -0.5168F, -2.1887F, 7.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.3F, -0.9F, -0.1052F, -0.1913F, -1.2714F));

		PartDefinition cube_r15 = robe_side_back_right.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 27).addBox(-6.571F, -0.5168F, -2.1887F, 7.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 6.4F, -2.2F, -0.1321F, 0.0524F, -1.1429F));

		PartDefinition cube_r16 = robe_side_back_right.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(34, 34).addBox(-8.571F, -0.5168F, -2.1887F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, 12.6F, -1.8F, -0.1396F, -0.0265F, -1.7048F));

		PartDefinition bone = robe_side_back_right.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-3.4F, 21.5F, -2.0F));

		PartDefinition cube_r17 = bone.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(34, 34).addBox(-8.571F, -0.5168F, -2.1887F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1418F, -0.0082F, -1.5752F));

		PartDefinition bone8 = bone.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));

		PartDefinition cube_r18 = bone8.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(18, 35).addBox(-4.571F, -0.5168F, -2.1887F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1321F, 0.0524F, -1.1429F));

		PartDefinition back_robe = robe.addOrReplaceChild("back_robe", CubeListBuilder.create(), PartPose.offset(-1.0822F, -5.3508F, 2.9251F));

		PartDefinition cube_r19 = back_robe.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(44, 42).addBox(-0.6934F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.6F, 5.7F, 3.9F, -0.9343F, -0.0144F, 1.5416F));

		PartDefinition cube_r20 = back_robe.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(44, 42).addBox(-0.6934F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4F, -1.3F, 3.8F, -0.9343F, -0.0144F, 1.5416F));

		PartDefinition cube_r21 = back_robe.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(1, 34).addBox(-0.8093F, -0.723F, -0.236F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6F, -7.7F, 2.3F, -0.746F, -0.1731F, 1.246F));

		PartDefinition cube_r22 = back_robe.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(44, 42).addBox(-6.3066F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 5.7F, 3.9F, -0.9343F, 0.0144F, -1.5416F));

		PartDefinition cube_r23 = back_robe.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(44, 42).addBox(-6.3066F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.8F, -1.3F, 3.8F, -0.9343F, 0.0144F, -1.5416F));

		PartDefinition cube_r24 = back_robe.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(1, 34).addBox(-6.1907F, -0.723F, -0.236F, 7.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -7.7F, 2.3F, -0.746F, 0.1731F, -1.246F));

		PartDefinition cube_r25 = back_robe.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(2, 0).addBox(-5.5F, -0.5F, -5.5F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0498F, -12.9006F, -4.6931F, 0.2982F, 0.1774F, -0.7332F));

		PartDefinition cube_r26 = back_robe.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(2, 0).addBox(-0.5F, -0.5F, -5.5F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2728F, -13.1919F, -4.7313F, 0.2161F, -0.149F, 0.5947F));

		PartDefinition cube_r27 = back_robe.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(30, 27).addBox(-3.0F, -0.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6526F, -11.7125F, 0.7064F, -0.15F, 0.0042F, -0.6737F));

		PartDefinition cube_r28 = back_robe.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(29, 27).addBox(-1.5F, -0.5F, -5.5F, 7.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3704F, -12.7378F, 3.2969F, -0.1584F, -0.0529F, 0.5905F));

		PartDefinition cube_r29 = back_robe.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(26, 23).addBox(-5.5F, -0.5F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 22).addBox(-4.5F, -0.5F, 0.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -11.3002F, 3.748F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r30 = back_robe.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(28, 15).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -11.7002F, 3.6436F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r31 = back_robe.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(28, 15).addBox(-2.5F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -11.1002F, 3.748F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r32 = back_robe.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(49, 36).addBox(-2.5F, -0.5F, 0.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -11.3002F, 3.648F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r33 = back_robe.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(50, 36).addBox(-1.5F, -0.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -10.9002F, 3.748F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r34 = back_robe.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(24, 22).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -10.2003F, 3.6567F, -1.5626F, -0.003F, 0.3491F));

		PartDefinition cube_r35 = back_robe.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(28, 15).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8F, -9.8003F, 3.6611F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r36 = back_robe.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 13).addBox(-5.5F, -0.5F, -5.5F, 11.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -3.8F, 3.9F, -1.5621F, 0.0F, 0.0F));

		PartDefinition cube_r37 = back_robe.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(0, 13).addBox(-5.5F, -0.5F, -5.5F, 11.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, 9.2F, 4.2F, -1.5272F, 0.0F, 0.0F));

		PartDefinition bone4 = back_robe.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-2.2F, 17.0F, 4.8F));

		PartDefinition cube_r38 = bone4.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(31, 0).addBox(-2.0F, -0.2819F, -0.5048F, 5.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.2397F, -0.2592F, 0.0592F));

		PartDefinition bone5 = back_robe.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(4.3F, 17.2F, 5.2F));

		PartDefinition cube_r39 = bone5.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(31, 0).addBox(0.5F, -0.2819F, -0.5048F, 5.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, -1.1632F, 0.0414F, -0.1242F));

		PartDefinition bone2 = back_robe.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(-6.2F, 12.7F, 3.9F));

		PartDefinition cube_r40 = bone2.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(44, 42).addBox(-6.3066F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9343F, 0.0144F, -1.5416F));

		PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-0.2F, 6.3F, 0.1F));

		PartDefinition cube_r41 = bone3.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(44, 42).addBox(-7.0062F, -0.3836F, -1.2908F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.8737F, 0.3875F, -1.2458F));

		PartDefinition bone6 = back_robe.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(7.8F, 12.7F, 3.9F));

		PartDefinition cube_r42 = bone6.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(44, 42).addBox(0.0062F, -0.3836F, -1.2908F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 6.3F, 0.1F, -0.8737F, -0.3875F, 1.2458F));

		PartDefinition cube_r43 = bone6.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(44, 42).addBox(-0.6934F, -0.4038F, -1.3013F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9343F, -0.0144F, 1.5416F));

		PartDefinition bone7 = bone6.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(0.2F, 6.3F, 0.1F));

		PartDefinition robe_side_back_left = robe.addOrReplaceChild("robe_side_back_left", CubeListBuilder.create(), PartPose.offset(5.3178F, -15.1508F, 3.6251F));

		PartDefinition cube_r44 = robe_side_back_left.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(0, 27).addBox(-0.429F, -0.5168F, -2.1887F, 7.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.4F, -0.1052F, 0.1913F, 1.2714F));

		PartDefinition cube_r45 = robe_side_back_left.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(0, 27).addBox(-0.429F, -0.5168F, -2.1887F, 7.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, 6.1F, -1.7F, -0.2433F, -0.0745F, 0.9694F));

		PartDefinition cube_r46 = robe_side_back_left.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(34, 34).addBox(-0.429F, -0.5168F, -2.1887F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3F, 11.7F, -1.3F, -0.2764F, 0.0386F, 1.7913F));

		PartDefinition cube_r47 = robe_side_back_left.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(34, 34).addBox(-0.429F, -0.5168F, -2.1887F, 9.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, 20.3F, -1.5F, -0.2256F, -0.0412F, 1.3626F));

		PartDefinition bone14 = robe_side_back_left.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offset(5.0F, 28.9F, -0.1F));

		PartDefinition cube_r48 = bone14.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(18, 35).addBox(-0.429F, -0.5168F, -2.1887F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.1421F, -0.0017F, 1.5061F));

		PartDefinition robe_side_middle_left = robe.addOrReplaceChild("robe_side_middle_left", CubeListBuilder.create(), PartPose.offset(5.089F, -15.5297F, -1.8546F));

		PartDefinition cube_r49 = robe_side_middle_left.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(39, 19).addBox(-0.0155F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.005F, -0.0481F, 1.2175F));

		PartDefinition cube_r50 = robe_side_middle_left.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(39, 19).addBox(-0.0155F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, 6.3F, 0.3F, -0.125F, 0.1507F, 0.7234F));

		PartDefinition cube_r51 = robe_side_middle_left.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(39, 19).addBox(-0.0155F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5F, 11.2F, -0.9F, 0.0053F, 0.0751F, 1.4363F));

		PartDefinition cube_r52 = robe_side_middle_left.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(39, 19).addBox(-0.0155F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, 18.3F, -0.9F, 0.0366F, 0.0658F, 1.8736F));

		PartDefinition bone15 = robe_side_middle_left.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offset(6.2F, 24.6F, -1.9F));

		PartDefinition cube_r53 = bone15.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(39, 19).addBox(-0.0155F, -0.1682F, -2.7194F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.5F, -0.0013F, 0.0753F, 1.3488F));

		PartDefinition robe_side_front_left = robe.addOrReplaceChild("robe_side_front_left", CubeListBuilder.create(), PartPose.offset(5.0515F, -14.3368F, -5.5272F));

		PartDefinition cube_r54 = robe_side_front_left.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(22, 47).addBox(-0.0155F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5376F, 12.5071F, -0.8274F, -0.199F, 0.2148F, -0.7738F));

		PartDefinition cube_r55 = robe_side_front_left.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(22, 47).addBox(-0.0155F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5624F, 11.0071F, -0.0274F, 0.1571F, 0.2469F, 0.5588F));

		PartDefinition cube_r56 = robe_side_front_left.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(22, 47).addBox(-0.0155F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4624F, 9.2071F, 0.7726F, 0.2441F, 0.2452F, 1.4741F));

		PartDefinition cube_r57 = robe_side_front_left.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(41, 13).addBox(-0.0155F, -0.1682F, -2.7194F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8376F, 5.1071F, 1.0726F, 0.3384F, 0.0644F, 2.0658F));

		PartDefinition cube_r58 = robe_side_front_left.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(0, 37).addBox(-0.0155F, -0.1682F, -2.7194F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4376F, 0.5071F, 1.5726F, 0.2283F, 0.1873F, 1.4952F));

		PartDefinition bone17 = robe_side_front_left.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offset(2.8376F, 11.0071F, -1.9274F));

		PartDefinition cube_r59 = bone17.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(41, 13).addBox(-0.9917F, -0.4743F, -2.6892F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, -3.0F, -0.2F, 0.0553F, -0.0067F, -0.1542F));

		PartDefinition cube_r60 = bone17.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(22, 47).addBox(-0.0155F, -0.1682F, -2.7194F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -0.5F, 0.3F, -0.0382F, 0.1692F, -0.9821F));

		PartDefinition bone16 = bone17.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offset(6.0F, -4.0F, -1.2F));

		PartDefinition cube_r61 = bone16.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(12, 42).addBox(-0.0155F, -0.1682F, -2.7194F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0028F, -0.0605F, 1.1578F));

		PartDefinition reaper_body = reaper.addOrReplaceChild("reaper_body", CubeListBuilder.create(), PartPose.offset(0.0928F, -2.8008F, 1.5251F));

		PartDefinition head = reaper_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -9.0F, -1.7F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -1.75F, 0.0F));

		PartDefinition jacket = reaper_body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, -6.0F, 1.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.025F, 3.25F, 0.0F));

		PartDefinition arm_left = reaper_body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(1, 48).addBox(-0.5F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(4.525F, -0.75F, 0.0F, -1.3865F, -0.4802F, -0.2174F));

		PartDefinition forearm_left = arm_left.addOrReplaceChild("forearm_left", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -0.6F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 10.0F, 0.0F, -0.3491F, 0.0F, 0.1745F));

		PartDefinition chain = arm_left.addOrReplaceChild("chain", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3F, -1.0F, -3.6F, 0.4604F, 0.2338F, 0.5609F));

		PartDefinition cube_r62 = chain.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(6, 44).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.875F, 7.2796F, -2.4721F, 2.6565F, -0.4622F, -0.4135F));

		PartDefinition cube_r63 = chain.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(6, 44).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5833F, 4.853F, -1.8147F, 3.0374F, -0.3572F, -0.302F));

		PartDefinition cube_r64 = chain.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(6, 44).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.875F, 7.2796F, -2.4721F, 2.3893F, 0.9138F, -1.2822F));

		PartDefinition cube_r65 = chain.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(6, 44).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5833F, 4.853F, -1.8147F, 2.8697F, 1.1994F, -0.5928F));

		PartDefinition cube_r66 = chain.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9569F, 2.8643F, -1.3336F, 2.276F, 0.8712F, -0.7272F));

		PartDefinition cube_r67 = chain.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(8, 44).addBox(-0.5F, -2.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.435F, 0.997F, -1.1052F));

		PartDefinition cube_r68 = chain.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9569F, 2.8643F, -1.3336F, 2.5716F, -0.4306F, 0.2663F));

		PartDefinition cube_r69 = chain.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(8, 44).addBox(-0.5F, -2.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.7442F, -0.4256F, -0.3116F));

		PartDefinition chain_forearm = chain.addOrReplaceChild("chain_forearm", CubeListBuilder.create(), PartPose.offsetAndRotation(3.706F, 8.3903F, -3.1252F, -0.0012F, -0.0698F, 0.0175F));

		PartDefinition cube_r70 = chain_forearm.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5954F, 0.678F, -0.4281F, 2.5256F, -0.8847F, -0.2695F));

		PartDefinition cube_r71 = chain_forearm.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4954F, 0.578F, -0.3281F, 2.6997F, 0.5434F, -1.0105F));

		PartDefinition cube_r72 = chain_forearm.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6954F, 1.778F, -1.2281F, 2.1377F, -0.7398F, -0.067F));

		PartDefinition cube_r73 = chain_forearm.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6954F, 1.778F, -1.2281F, 2.3955F, 0.4079F, -1.2324F));

		PartDefinition cube_r74 = chain_forearm.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(0, 45).addBox(-0.487F, -1.9847F, -0.0121F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0954F, 2.178F, -1.7281F, -2.6089F, -1.4831F, -1.4714F));

		PartDefinition cube_r75 = chain_forearm.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5121F, -1.9847F, -0.013F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0954F, 2.178F, -1.7281F, -3.097F, 0.0755F, -0.937F));

		PartDefinition cube_r76 = chain_forearm.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(0, 45).addBox(-0.487F, -1.9847F, -0.0121F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3954F, 3.178F, -1.7281F, 2.075F, -1.4166F, -0.3447F));

		PartDefinition cube_r77 = chain_forearm.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5121F, -1.9847F, -0.013F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3954F, 3.178F, -1.7281F, 3.0064F, 0.0742F, -1.4163F));

		PartDefinition cube_r78 = chain_forearm.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(0, 45).addBox(-0.487F, -1.9847F, -0.0121F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1954F, 3.478F, -1.9281F, 1.9411F, -1.2641F, 0.1534F));

		PartDefinition cube_r79 = chain_forearm.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5121F, -1.9847F, -0.013F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1954F, 3.478F, -1.9281F, 2.8545F, 0.1095F, -1.0629F));

		PartDefinition cube_r80 = chain_forearm.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(0, 45).addBox(-0.487F, -1.9847F, -0.0121F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5954F, 4.378F, -2.3281F, 1.8469F, -0.7596F, 0.2167F));

		PartDefinition cube_r81 = chain_forearm.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(0, 45).addBox(-0.5121F, -1.9847F, -0.013F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5954F, 4.378F, -2.3281F, 2.3506F, 0.1561F, -1.2056F));

		PartDefinition lantern = chain_forearm.addOrReplaceChild("lantern", CubeListBuilder.create(), PartPose.offsetAndRotation(7.3954F, 5.078F, -4.0281F, -0.2518F, -0.1737F, 0.1381F));

		PartDefinition cube_r82 = lantern.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(37, 57).addBox(-1.6741F, 0.8722F, -1.4228F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2222F, 2.168F, -0.8254F, 0.6227F, -0.2769F, -0.1843F));

		PartDefinition cube_r83 = lantern.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(0, 45).addBox(-1.1002F, 0.0447F, -0.9526F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2222F, 2.168F, -0.8254F, 0.6397F, -0.2673F, -0.1728F));

		PartDefinition cube_r84 = lantern.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(6, 44).addBox(-0.487F, -2.9847F, -0.0121F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.372F, -0.1941F, 0.2443F, 2.8438F, -0.0194F, 0.1822F));

		PartDefinition cube_r85 = lantern.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(6, 44).addBox(-0.5121F, -2.9847F, -0.013F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.372F, -0.1941F, 0.2443F, 1.637F, 1.2724F, -1.3254F));

		PartDefinition arm_right = reaper_body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(1, 48).addBox(-3.4F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-4.575F, -0.75F, 0.0F, -1.0631F, 0.4149F, -0.2582F));

		PartDefinition forearm_right = arm_right.addOrReplaceChild("forearm_right", CubeListBuilder.create(), PartPose.offset(-1.4F, 9.0F, 0.0F));

		PartDefinition right_arm_r1 = forearm_right.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(1, 48).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, 0.0F, -0.1309F));

		PartDefinition scythe = forearm_right.addOrReplaceChild("scythe", CubeListBuilder.create(), PartPose.offsetAndRotation(1.1752F, 8.9701F, -1.9119F, 0.6615F, 0.4825F, 2.1684F));

		PartDefinition cube_r86 = scythe.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(28, 56).addBox(-1.0F, 4.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(56, 47).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(49, 54).addBox(-1.0F, -10.5F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 56).addBox(-1.0F, -22.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 56).addBox(-1.0F, -16.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.9F, 2.7F, 1.0721F, -0.3994F, -0.2459F));

		PartDefinition cube_r87 = scythe.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(58, 58).addBox(0.0F, -1.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.7978F, -13.7762F, -9.9275F, -0.9348F, -0.7349F, 2.2273F));

		PartDefinition cube_r88 = scythe.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(38, 45).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(15.4122F, -13.1499F, -10.9459F, -0.9348F, -0.7349F, 2.2273F));

		PartDefinition cube_r89 = scythe.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(58, 61).addBox(-0.4264F, -2.8584F, -0.6968F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.4897F, -12.9463F, -11.6069F, -0.763F, -0.8566F, 2.085F));

		PartDefinition cube_r90 = scythe.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(48, 46).addBox(-1.4264F, -3.8585F, -0.6968F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(11.9492F, -12.2766F, -13.0542F, -0.8941F, -0.7903F, 2.1684F));

		PartDefinition cube_r91 = scythe.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(60, 60).addBox(-0.8429F, -4.0506F, -0.7571F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5752F, -12.219F, -12.8197F, -0.6704F, -0.919F, 1.9881F));

		PartDefinition cube_r92 = scythe.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(40, 49).addBox(-0.8429F, -4.0506F, -0.7571F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7441F, -11.8857F, -14.9736F, -0.7577F, -0.919F, 1.9881F));

		PartDefinition cube_r93 = scythe.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(35, 52).addBox(-0.8429F, -6.0506F, -0.7571F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.344F, -10.0035F, -13.4439F, -0.2655F, -1.0966F, 1.4129F));

		PartDefinition cube_r94 = scythe.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(40, 49).addBox(-0.8429F, -6.0506F, -0.7571F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(3.3891F, -9.6675F, -15.4768F, -0.2655F, -1.0966F, 1.4129F));

		PartDefinition legs = reaper_body.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 8.5F, 3.4F));

		PartDefinition cube_r95 = legs.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(2, 51).addBox(-4.0F, -0.0114F, -2.2615F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r96 = legs.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(2, 51).addBox(-4.0F, -0.0114F, -2.2615F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, 6.6F, 0.7F, 0.2182F, 0.0F, 0.0F));

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
	public void setupAnim(SpiritGrimReaperEntity entity, float limbSwing, float limbSwingAmount,
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
		if (!entity.WALK_ANIMATION.isStarted()) entity.WALK_ANIMATION.start((int) ageInTicks);

		// Sample idle into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.IDLE_ANIMATION, GrimReaperAnimations.Idle, ageInTicks, 1.0F);
		capturePoseInto(this.idlePose);

		// Sample walk into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.WALK_ANIMATION, GrimReaperAnimations.walk, ageInTicks, 1.0F);
		capturePoseInto(this.walkPose);

		// Write the lerped result
		applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		reaper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

}