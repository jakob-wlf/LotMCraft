package de.jakob.lotm.entity.client.spirits.asmann;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.bubbles.SpiritBubblesAnimations;
import de.jakob.lotm.entity.client.spirits.spirit_bane.SpiritBaneAnimations;
import de.jakob.lotm.entity.custom.spirits.AsmannEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class AsmannModel<T extends AsmannEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "asmann"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart Asmann;
	private final ModelPart Spine;
	private final ModelPart hypophysis;
	private final ModelPart parietal_lobe;
	private final ModelPart temporal_lobe;
	private final ModelPart frontal_lobe;
	private final ModelPart cerebellum;
	private final ModelPart chains;
	private final ModelPart chain5;
	private final ModelPart chain2;
	private final ModelPart chain43;
	private final ModelPart chain44;
	private final ModelPart Enchanting_particles;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone;

	public AsmannModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.Asmann = this.All.getChild("Asmann");
		this.Spine = this.Asmann.getChild("Spine");
		this.hypophysis = this.Asmann.getChild("hypophysis");
		this.parietal_lobe = this.Asmann.getChild("parietal_lobe");
		this.temporal_lobe = this.Asmann.getChild("temporal_lobe");
		this.frontal_lobe = this.Asmann.getChild("frontal_lobe");
		this.cerebellum = this.Asmann.getChild("cerebellum");
		this.chains = this.All.getChild("chains");
		this.chain5 = this.chains.getChild("chain5");
		this.chain2 = this.chains.getChild("chain2");
		this.chain43 = this.chain2.getChild("chain43");
		this.chain44 = this.chain2.getChild("chain44");
		this.Enchanting_particles = this.All.getChild("Enchanting_particles");
		this.bone2 = this.Enchanting_particles.getChild("bone2");
		this.bone3 = this.Enchanting_particles.getChild("bone3");
		this.bone = this.Enchanting_particles.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.5F, 20.5F, 4.0F));

		PartDefinition Asmann = All.addOrReplaceChild("Asmann", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Spine = Asmann.addOrReplaceChild("Spine", CubeListBuilder.create().texOffs(23, 8).addBox(-1.0F, 2.5F, -0.6F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(21, 2).addBox(-0.5F, 6.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(21, 0).addBox(-0.5F, 7.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition cube_r1 = Spine.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.7F, 1.3F, 1.5708F, 0.2793F, -1.5708F));

		PartDefinition cube_r2 = Spine.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.5F, 1.5708F, 0.0524F, -1.5708F));

		PartDefinition cube_r3 = Spine.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 1.5F, 1.5708F, 0.0524F, -1.5708F));

		PartDefinition cube_r4 = Spine.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.2F, 1.4F, 1.5708F, -0.1571F, -1.5708F));

		PartDefinition cube_r5 = Spine.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.1F, 1.3F, 1.5708F, -0.1571F, -1.5708F));

		PartDefinition cube_r6 = Spine.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.1F, 0.5F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r7 = Spine.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(21, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r8 = Spine.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(23, 8).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.1745F, 0.0F, 0.0F));

		PartDefinition cube_r9 = Spine.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(23, 8).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 0.4F, 0.2618F, 0.0F, 0.0F));

		PartDefinition hypophysis = Asmann.addOrReplaceChild("hypophysis", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -5.0F, -5.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.5F, -5.0F, -9.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 15).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, -1.0F));

		PartDefinition parietal_lobe = Asmann.addOrReplaceChild("parietal_lobe", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, 0.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -2.0F, -2.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(-0.5F, -12.5F, -6.0F));

		PartDefinition temporal_lobe = Asmann.addOrReplaceChild("temporal_lobe", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -1.0F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(1.0F))
		.texOffs(0, 8).addBox(-3.0F, 0.5F, -5.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, -9.0F));

		PartDefinition frontal_lobe = Asmann.addOrReplaceChild("frontal_lobe", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -6.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(14, 26).addBox(-3.0F, -1.0F, -9.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.5F, -8.0F));

		PartDefinition cerebellum = Asmann.addOrReplaceChild("cerebellum", CubeListBuilder.create().texOffs(0, 26).addBox(-2.0F, -1.5F, 2.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(-3.0F, -3.5F, -1.5F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 4).addBox(-1.5F, -1.0F, 5.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 0).addBox(2.0F, -1.0F, 3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 0).addBox(-3.0F, -1.0F, 3.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, -0.5F));

		PartDefinition chains = All.addOrReplaceChild("chains", CubeListBuilder.create(), PartPose.offset(-2.5F, -8.0F, -1.0F));

		PartDefinition cube_r10 = chains.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8F, 3.7F, 1.6F, -2.577F, 0.6956F, 0.7097F));

		PartDefinition cube_r11 = chains.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1478F, 3.7947F, 0.4295F, -2.5716F, -0.7056F, -0.0698F));

		PartDefinition cube_r12 = chains.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2F, 1.3F, 0.0F, -2.1581F, 0.6956F, 0.7097F));

		PartDefinition cube_r13 = chains.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3546F, 1.6163F, -1.173F, -2.3576F, -0.4394F, -0.4579F));

		PartDefinition cube_r14 = chains.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.1F, -1.5F, -1.4508F, 1.0789F, 0.4157F));

		PartDefinition cube_r15 = chains.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8555F, 0.4443F, -2.5876F, -2.6527F, 0.0566F, -1.261F));

		PartDefinition cube_r16 = chains.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -0.1F, -3.5F, -1.4061F, 0.5078F, 0.2761F));

		PartDefinition cube_r17 = chains.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5916F, 0.6371F, -4.3456F, -2.0844F, 0.1438F, -1.3754F));

		PartDefinition cube_r18 = chains.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.1F, -5.9F, -1.8669F, 0.5953F, -0.5414F));

		PartDefinition cube_r19 = chains.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0809F, -0.0615F, -6.9775F, -2.1869F, -0.2441F, -1.9428F));

		PartDefinition cube_r20 = chains.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, -0.7F, -8.1F, 0.693F, 0.3597F, -0.6686F));

		PartDefinition cube_r21 = chains.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3306F, -1.3827F, -7.7929F, 2.1028F, 0.8039F, 1.3031F));

		PartDefinition cube_r22 = chains.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, -2.7F, -8.9F, 0.0255F, -0.3798F, 2.753F));

		PartDefinition cube_r23 = chains.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5682F, -2.8709F, -8.0532F, 0.0637F, 1.1902F, 2.8217F));

		PartDefinition cube_r24 = chains.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, -5.4F, -8.9F, 0.0178F, -0.5037F, -2.8056F));

		PartDefinition cube_r25 = chains.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1331F, -6.0714F, -7.9717F, 0.0323F, 1.0669F, -2.7688F));

		PartDefinition cube_r26 = chains.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -7.3F, -9.3F, -0.3785F, -0.3411F, -1.8973F));

		PartDefinition cube_r27 = chains.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7572F, -8.3122F, -8.7018F, -0.8055F, 1.0669F, -2.7688F));

		PartDefinition cube_r28 = chains.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -7.9F, -10.3F, -0.4282F, -0.1803F, -1.6389F));

		PartDefinition cube_r29 = chains.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.897F, -8.8824F, -9.8775F, -1.1573F, 1.1084F, -2.8354F));

		PartDefinition cube_r30 = chains.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1F, -8.0F, -10.6F, 0.1849F, -0.136F, -1.5428F));

		PartDefinition cube_r31 = chains.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5249F, -8.9001F, -9.8864F, 0.931F, 1.3417F, -0.5991F));

		PartDefinition cube_r32 = chains.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.1F, -8.0F, -9.1F, 1.1228F, -0.0024F, -1.7267F));

		PartDefinition cube_r33 = chains.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7138F, -8.95F, -8.4304F, 1.5681F, 0.448F, -0.1571F));

		PartDefinition cube_r34 = chains.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.6F, -8.0F, -6.3F, 1.5765F, -0.0024F, -1.7267F));

		PartDefinition cube_r35 = chains.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9481F, -8.9086F, -5.8005F, 1.5684F, -0.0057F, -0.1559F));

		PartDefinition cube_r36 = chains.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.6F, -8.0F, -3.3F, 1.5765F, -0.0024F, -1.7267F));

		PartDefinition cube_r37 = chains.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9481F, -8.9086F, -2.8005F, 1.5684F, -0.0057F, -0.1559F));

		PartDefinition cube_r38 = chains.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.8F, -7.6F, -0.7F, 2.1875F, -0.1944F, -1.7278F));

		PartDefinition cube_r39 = chains.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.9617F, -8.4374F, -0.3904F, 1.334F, -0.6034F, -0.0209F));

		PartDefinition cube_r40 = chains.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -7.0F, 1.5F, 2.315F, 0.0634F, -1.6862F));

		PartDefinition cube_r41 = chains.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1831F, -7.9118F, 1.4657F, 1.6569F, -0.7424F, -0.1737F));

		PartDefinition cube_r42 = chains.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, -6.2F, 3.9F, 2.099F, -0.4616F, -2.0634F));

		PartDefinition cube_r43 = chains.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4119F, -6.5949F, 4.5064F, 1.0483F, -0.4681F, -0.2383F));

		PartDefinition cube_r44 = chains.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -4.7F, 6.2F, 2.0761F, -0.3705F, -2.0068F));

		PartDefinition cube_r45 = chains.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5203F, -5.1938F, 6.7443F, 1.153F, -0.4681F, -0.2383F));

		PartDefinition cube_r46 = chains.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.2F, 7.3F, 2.6158F, -1.022F, -2.7826F));

		PartDefinition cube_r47 = chains.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1265F, -2.7978F, 8.0584F, 0.2978F, -0.4681F, -0.2383F));

		PartDefinition cube_r48 = chains.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -1.2F, 6.6F, -2.2005F, -0.6979F, 1.7704F));

		PartDefinition cube_r49 = chains.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6516F, -0.0308F, 6.7074F, -0.7669F, -0.4681F, -0.2383F));

		PartDefinition cube_r50 = chains.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3F, 0.2F, 4.7F, -2.1188F, -0.5234F, 1.6286F));

		PartDefinition cube_r51 = chains.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0641F, 1.398F, 4.6045F, -0.9763F, -0.4681F, -0.2383F));

		PartDefinition cube_r52 = chains.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, 1.2F, 2.5F, -1.5783F, -0.3526F, 1.5888F));

		PartDefinition cube_r53 = chains.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2838F, 2.3033F, 2.3726F, -1.2182F, -0.007F, 0.0155F));

		PartDefinition cube_r54 = chains.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 1.2F, -0.2F, -1.3223F, 0.3006F, 1.6708F));

		PartDefinition cube_r55 = chains.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8107F, 1.9832F, -0.8415F, -1.8803F, 0.2372F, 0.025F));

		PartDefinition cube_r56 = chains.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, 0.3F, -2.9F, -1.5666F, 0.3006F, 1.6708F));

		PartDefinition cube_r57 = chains.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6198F, 1.0536F, -3.6717F, -1.8714F, 0.004F, 0.0988F));

		PartDefinition cube_r58 = chains.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, -0.1F, -5.9F, -1.5668F, 0.0213F, 1.6697F));

		PartDefinition cube_r59 = chains.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6028F, 0.8348F, -6.4192F, -1.5921F, 0.004F, 0.0988F));

		PartDefinition cube_r60 = chains.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, -0.3F, -8.9F, -1.5667F, 0.2657F, 1.6707F));

		PartDefinition cube_r61 = chains.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6173F, 0.48F, -9.643F, -1.8365F, 0.004F, 0.0988F));

		PartDefinition cube_r62 = chains.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -0.9F, -11.6F, -1.7875F, 0.1294F, 1.7017F));

		PartDefinition cube_r63 = chains.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0082F, -0.0532F, -12.3355F, -1.7556F, -0.2148F, 0.1593F));

		PartDefinition cube_r64 = chains.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, -1.2F, -14.3F, -1.5668F, 0.161F, 1.6702F));

		PartDefinition cube_r65 = chains.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2173F, -0.42F, -15.043F, -1.8365F, 0.004F, 0.0988F));

		PartDefinition cube_r66 = chains.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8F, -2.8F, -16.4F, -1.6636F, 0.992F, 1.3896F));

		PartDefinition cube_r67 = chains.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3724F, -2.6287F, -17.5348F, -2.5648F, -0.0507F, -0.1035F));

		PartDefinition cube_r68 = chains.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1F, -5.2F, -16.9F, 2.435F, 0.997F, -0.983F));

		PartDefinition cube_r69 = chains.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7885F, -6.0038F, -17.77F, 2.7442F, -0.4256F, -0.1894F));

		PartDefinition cube_r70 = chains.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, -6.9F, -15.2F, 2.0423F, 0.4299F, -1.5508F));

		PartDefinition cube_r71 = chains.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4475F, -7.9132F, -15.4183F, 2.0461F, -0.4256F, -0.1894F));

		PartDefinition cube_r72 = chains.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -7.9F, -12.8F, 1.7422F, 0.3178F, -1.3312F));

		PartDefinition cube_r73 = chains.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9942F, -9.0836F, -12.7255F, 1.893F, -0.1628F, 0.1856F));

		PartDefinition cube_r74 = chains.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -8.9F, -10.0F, 1.7422F, 0.3178F, -1.3312F));

		PartDefinition cube_r75 = chains.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7942F, -10.0836F, -9.9254F, 1.893F, -0.1628F, 0.1856F));

		PartDefinition cube_r76 = chains.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, -9.0F, -7.0F, 1.7346F, -0.1127F, -1.4038F));

		PartDefinition cube_r77 = chains.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4906F, -10.0297F, -6.4784F, 1.4566F, -0.1628F, 0.1856F));

		PartDefinition cube_r78 = chains.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -8.8F, -4.7F, 2.3505F, -0.2676F, -1.4303F));

		PartDefinition cube_r79 = chains.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1652F, -9.8529F, -4.4317F, 1.2028F, -0.7451F, 0.3962F));

		PartDefinition cube_r80 = chains.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -7.6F, -2.8F, 1.7392F, -0.8382F, -1.5251F));

		PartDefinition cube_r81 = chains.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5595F, -7.9906F, -1.7829F, 0.7255F, -0.1123F, 0.1714F));

		PartDefinition cube_r82 = chains.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7F, -5.1F, -1.5F, 1.2392F, -1.2915F, -1.5708F));

		PartDefinition cube_r83 = chains.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.01F, -4.7647F, -0.3636F, 0.2648F, 0.0899F, -0.3196F));

		PartDefinition cube_r84 = chains.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -2.6F, -0.8F, 1.2392F, -1.2915F, -1.5708F));

		PartDefinition cube_r85 = chains.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.21F, -2.2647F, 0.3364F, 0.2648F, 0.0899F, -0.3196F));

		PartDefinition cube_r86 = chains.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4F, 5.3F, 2.0F, 0.8904F, -1.1678F, -1.4199F));

		PartDefinition cube_r87 = chains.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2885F, 5.5407F, 3.1957F, 0.32F, 0.2493F, -0.4891F));

		PartDefinition cube_r88 = chains.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 2.8F, 0.9F, 1.3267F, -1.1678F, -1.4199F));

		PartDefinition cube_r89 = chains.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3149F, 2.9087F, 2.0575F, 0.3922F, 0.0949F, -0.0743F));

		PartDefinition cube_r90 = chains.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.2392F, -1.2915F, -1.5708F));

		PartDefinition cube_r91 = chains.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(19, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.31F, 0.3353F, 1.1364F, 0.2648F, 0.0899F, -0.3196F));

		PartDefinition chain5 = chains.addOrReplaceChild("chain5", CubeListBuilder.create().texOffs(19, 0).addBox(-0.2892F, 0.2335F, 0.0236F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9F, 6.0F, 2.3F, 0.4029F, 0.0067F, 0.1351F));

		PartDefinition cube_r92 = chain5.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2108F, 1.7335F, 0.0236F, 0.0F, -1.5708F, 0.0F));

		PartDefinition chain2 = chains.addOrReplaceChild("chain2", CubeListBuilder.create(), PartPose.offset(4.3F, 4.6F, 2.7F));

		PartDefinition chain43 = chain2.addOrReplaceChild("chain43", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5809F, -2.943F, 0.1059F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 0.3F, -0.5F, -2.4388F, -0.8521F, 0.4062F));

		PartDefinition cube_r93 = chain43.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0809F, -1.443F, 0.1059F, 0.0F, -1.5708F, 0.0F));

		PartDefinition chain44 = chain2.addOrReplaceChild("chain44", CubeListBuilder.create().texOffs(19, 0).addBox(-0.4147F, -2.9946F, 0.0941F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, 1.7F, 0.6F, 3.1185F, -0.985F, 0.7466F));

		PartDefinition cube_r94 = chain44.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(19, 0).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0853F, -1.4946F, 0.0941F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Enchanting_particles = All.addOrReplaceChild("Enchanting_particles", CubeListBuilder.create(), PartPose.offset(-0.5F, -12.15F, -4.0F));

		PartDefinition bone2 = Enchanting_particles.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.5F, 1.85F, -0.1F));

		PartDefinition cube_r95 = bone2.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.9F, -5.9F, 12.1F, 0.4179F, 0.3026F, 0.1869F));

		PartDefinition cube_r96 = bone2.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -10.0F, -11.9F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r97 = bone2.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(28, 23).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 12.1F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r98 = bone2.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.2F, 2.0F, -3.9F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition cube_r99 = bone2.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.1F, 14.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition cube_r100 = bone2.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(27, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 10.0F, -11.3F, 2.9899F, -1.0256F, -2.7875F));

		PartDefinition cube_r101 = bone2.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(28, 23).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, 14.0F, 2.1F, 0.4631F, -1.3945F, -0.2328F));

		PartDefinition bone3 = Enchanting_particles.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.6F, 1.75F, -0.9F));

		PartDefinition cube_r102 = bone3.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(27, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, 5.9F, 10.8F, -3.0258F, -0.7773F, 2.9971F));

		PartDefinition cube_r103 = bone3.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(27, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1F, -3.1F, -5.2F, 0.948F, -1.151F, -1.1872F));

		PartDefinition cube_r104 = bone3.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.1F, -3.1F, -10.0F, 0.013F, 0.3678F, -1.3893F));

		PartDefinition cube_r105 = bone3.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, -3.1F, -10.0F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r106 = bone3.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(27, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4F, 6.1F, -9.3F, 0.3409F, 0.1414F, -0.2328F));

		PartDefinition cube_r107 = bone3.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7F, -6.9F, 12.9F, -3.0131F, 0.6717F, -2.8267F));

		PartDefinition cube_r108 = bone3.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.3F, -15.9F, -3.1F, -1.6141F, 1.3457F, -2.1348F));

		PartDefinition cube_r109 = bone3.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7F, -6.9F, -3.1F, -0.9157F, 1.0141F, -0.8961F));

		PartDefinition bone = Enchanting_particles.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.2333F, 4.171F, 0.87F));

		PartDefinition cube_r110 = bone.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.1F, 11.0F, 3.0288F, 2.7245F, -0.4597F));

		PartDefinition cube_r111 = bone.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(28, 23).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7667F, 0.679F, -0.87F, -0.4363F, -0.1309F, 0.0F));

		PartDefinition cube_r112 = bone.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(22, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.5667F, -11.321F, -1.87F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r113 = bone.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.7667F, 6.679F, -5.17F, -0.7571F, -0.9146F, 0.5818F));

		PartDefinition cube_r114 = bone.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(28, 23).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2333F, 6.679F, 10.83F, -0.7571F, -0.9146F, 0.5818F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(AsmannEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		boolean isWalking = limbSwingAmount > 0.01F;

		if (isWalking) {
			// Stop idle animation and start/continue walk animation
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, AsmannAnimations.walk, ageInTicks, 1.0F);
		} else {
			// Stop walk animation and start/continue idle animation
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, AsmannAnimations.Idle, ageInTicks, 1.0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}