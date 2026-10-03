package de.jakob.lotm.rendering.models.error;// Made with Blockbench 5.1.4
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
import net.minecraft.world.entity.Entity;

public class ErrorMythicalCreatureModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "error_mythical_creature"), "main");
	private final ModelPart Clock_Head;
	private final ModelPart Worm53;
	private final ModelPart Worm52;
	private final ModelPart Worm51;
	private final ModelPart Worm50;
	private final ModelPart Worm49;
	private final ModelPart Worm48;
	private final ModelPart Worm47;
	private final ModelPart Worm46;
	private final ModelPart Worm45;
	private final ModelPart Worm44;
	private final ModelPart Worm43;
	private final ModelPart Worm42;
	private final ModelPart Clock_body;
	private final ModelPart Worm40;
	private final ModelPart Worm39;
	private final ModelPart Worm37;
	private final ModelPart Worm35;
	private final ModelPart Worm33;
	private final ModelPart Worm31;
	private final ModelPart Worm29;
	private final ModelPart Worm27;
	private final ModelPart Worm25;
	private final ModelPart Worm23;
	private final ModelPart Worm21;
	private final ModelPart Worm19;
	private final ModelPart Worm17;
	private final ModelPart Worm15;
	private final ModelPart Worm13;
	private final ModelPart Worm11;
	private final ModelPart Worm9;
	private final ModelPart Worm7;
	private final ModelPart Worm5;
	private final ModelPart Worm3;
	private final ModelPart Worm2;
	private final ModelPart Worm38;
	private final ModelPart Worm36;
	private final ModelPart Worm34;
	private final ModelPart Worm32;
	private final ModelPart Worm30;
	private final ModelPart Worm28;
	private final ModelPart Worm26;
	private final ModelPart Worm24;
	private final ModelPart Worm22;
	private final ModelPart Worm20;
	private final ModelPart Worm18;
	private final ModelPart Worm16;
	private final ModelPart Worm14;
	private final ModelPart Worm12;
	private final ModelPart Worm10;
	private final ModelPart Worm8;
	private final ModelPart Worm6;
	private final ModelPart Worm4;
	private final ModelPart Worm;
	private final ModelPart pendulum;
	private final ModelPart Worm_Clock;
	private final ModelPart Worm_First;
	private final ModelPart Worm_Second;
	private final ModelPart Clockhands;

	public ErrorMythicalCreatureModel(ModelPart root) {
		this.Clock_Head = root.getChild("Clock_Head");
		this.Worm53 = this.Clock_Head.getChild("Worm53");
		this.Worm52 = this.Clock_Head.getChild("Worm52");
		this.Worm51 = this.Clock_Head.getChild("Worm51");
		this.Worm50 = this.Clock_Head.getChild("Worm50");
		this.Worm49 = this.Clock_Head.getChild("Worm49");
		this.Worm48 = this.Clock_Head.getChild("Worm48");
		this.Worm47 = this.Clock_Head.getChild("Worm47");
		this.Worm46 = this.Clock_Head.getChild("Worm46");
		this.Worm45 = this.Clock_Head.getChild("Worm45");
		this.Worm44 = this.Clock_Head.getChild("Worm44");
		this.Worm43 = this.Clock_Head.getChild("Worm43");
		this.Worm42 = this.Clock_Head.getChild("Worm42");
		this.Clock_body = root.getChild("Clock_body");
		this.Worm40 = this.Clock_body.getChild("Worm40");
		this.Worm39 = this.Clock_body.getChild("Worm39");
		this.Worm37 = this.Clock_body.getChild("Worm37");
		this.Worm35 = this.Clock_body.getChild("Worm35");
		this.Worm33 = this.Clock_body.getChild("Worm33");
		this.Worm31 = this.Clock_body.getChild("Worm31");
		this.Worm29 = this.Clock_body.getChild("Worm29");
		this.Worm27 = this.Clock_body.getChild("Worm27");
		this.Worm25 = this.Clock_body.getChild("Worm25");
		this.Worm23 = this.Clock_body.getChild("Worm23");
		this.Worm21 = this.Clock_body.getChild("Worm21");
		this.Worm19 = this.Clock_body.getChild("Worm19");
		this.Worm17 = this.Clock_body.getChild("Worm17");
		this.Worm15 = this.Clock_body.getChild("Worm15");
		this.Worm13 = this.Clock_body.getChild("Worm13");
		this.Worm11 = this.Clock_body.getChild("Worm11");
		this.Worm9 = this.Clock_body.getChild("Worm9");
		this.Worm7 = this.Clock_body.getChild("Worm7");
		this.Worm5 = this.Clock_body.getChild("Worm5");
		this.Worm3 = this.Clock_body.getChild("Worm3");
		this.Worm2 = this.Clock_body.getChild("Worm2");
		this.Worm38 = this.Clock_body.getChild("Worm38");
		this.Worm36 = this.Clock_body.getChild("Worm36");
		this.Worm34 = this.Clock_body.getChild("Worm34");
		this.Worm32 = this.Clock_body.getChild("Worm32");
		this.Worm30 = this.Clock_body.getChild("Worm30");
		this.Worm28 = this.Clock_body.getChild("Worm28");
		this.Worm26 = this.Clock_body.getChild("Worm26");
		this.Worm24 = this.Clock_body.getChild("Worm24");
		this.Worm22 = this.Clock_body.getChild("Worm22");
		this.Worm20 = this.Clock_body.getChild("Worm20");
		this.Worm18 = this.Clock_body.getChild("Worm18");
		this.Worm16 = this.Clock_body.getChild("Worm16");
		this.Worm14 = this.Clock_body.getChild("Worm14");
		this.Worm12 = this.Clock_body.getChild("Worm12");
		this.Worm10 = this.Clock_body.getChild("Worm10");
		this.Worm8 = this.Clock_body.getChild("Worm8");
		this.Worm6 = this.Clock_body.getChild("Worm6");
		this.Worm4 = this.Clock_body.getChild("Worm4");
		this.Worm = this.Clock_body.getChild("Worm");
		this.pendulum = this.Clock_body.getChild("pendulum");
		this.Worm_Clock = root.getChild("Worm_Clock");
		this.Worm_First = this.Worm_Clock.getChild("Worm_First");
		this.Worm_Second = this.Worm_Clock.getChild("Worm_Second");
		this.Clockhands = this.Worm_Clock.getChild("Clockhands");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Clock_Head = partdefinition.addOrReplaceChild("Clock_Head", CubeListBuilder.create().texOffs(4, 11).addBox(-2.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-4.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-2.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-4.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-5.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-5.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-5.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-4.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-2.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-2.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-4.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-5.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -10.8F, -4.1F));

		PartDefinition cube_r1 = Clock_Head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, 2.7F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r2 = Clock_Head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.7F, -3.3F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Worm53 = Clock_Head.addOrReplaceChild("Worm53", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r3 = Worm53.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(8, 8).addBox(-0.2F, -1.9F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r4 = Worm53.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -7.1F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.8F, -5.1F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.8F, -1.1F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.8F, -3.1F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition cube_r5 = Worm53.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 8).addBox(0.1444F, -1.8315F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.6F, 0.4F, -1.5708F, 1.1781F, -1.5708F));

		PartDefinition cube_r6 = Worm53.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 8).addBox(-0.4329F, -1.8693F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -3.4F, 0.0F, 1.5708F, 1.309F, 1.5708F));

		PartDefinition cube_r7 = Worm53.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(8, 8).addBox(-0.4631F, -1.8607F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -1.4F, 0.6F, 1.5708F, 1.2741F, 1.5708F));

		PartDefinition Worm52 = Clock_Head.addOrReplaceChild("Worm52", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r8 = Worm52.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(12, 0).addBox(-0.2F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.7F, 1.5708F, 1.2915F, 1.5708F));

		PartDefinition cube_r9 = Worm52.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -7.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.8F, -5.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition cube_r10 = Worm52.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(12, 4).addBox(-0.2F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.2F, -5.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.2F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.6F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r11 = Worm52.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(12, 12).addBox(-0.8F, -1.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, -0.5F, 1.5708F, -1.1868F, 1.5708F));

		PartDefinition cube_r12 = Worm52.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(12, 8).addBox(-0.8F, -3.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, -1.0F, -1.5708F, -1.2217F, -1.5708F));

		PartDefinition Worm51 = Clock_Head.addOrReplaceChild("Worm51", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r13 = Worm51.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(12, 0).addBox(-0.2F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.2F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.2F, -7.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.2F, -5.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r14 = Worm51.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -7.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -5.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -1.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -3.1F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition Worm50 = Clock_Head.addOrReplaceChild("Worm50", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, 0.0F, 6.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r15 = Worm50.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -1.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -3.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-1.0F, -5.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r16 = Worm50.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -7.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition cube_r17 = Worm50.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(12, 4).addBox(0.0F, -5.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 1.6F, 1.5708F, -1.2217F, 1.5708F));

		PartDefinition cube_r18 = Worm50.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(12, 12).addBox(0.0F, -1.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.6F, -1.5708F, -1.2392F, -1.5708F));

		PartDefinition cube_r19 = Worm50.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(12, 8).addBox(0.0F, -3.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.6F, 0.7F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition Worm49 = Clock_Head.addOrReplaceChild("Worm49", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.5F, 0.0F, 6.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r20 = Worm49.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -1.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -3.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-1.0F, -5.9F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r21 = Worm49.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -7.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, -1.6F, -1.5708F, -1.4137F, -1.5708F));

		PartDefinition cube_r22 = Worm49.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(12, 4).addBox(0.0F, -5.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 1.5708F, -1.3788F, 1.5708F));

		PartDefinition cube_r23 = Worm49.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(12, 12).addBox(0.0F, -1.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(0.0F, -3.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition Worm48 = Clock_Head.addOrReplaceChild("Worm48", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 6.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r24 = Worm48.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -7.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(0.0F, -5.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(0.0F, -1.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(0.0F, -3.1F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -4.4F, 0.0F, 0.0F, -1.5708F, -3.1416F));

		PartDefinition Worm47 = Clock_Head.addOrReplaceChild("Worm47", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 5.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r25 = Worm47.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r26 = Worm47.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -5.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(0.4F, -1.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r27 = Worm47.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -3.4F, -0.5F, -2.9147F, 0.0F, 2.9671F));

		PartDefinition cube_r28 = Worm47.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -1.6F, -0.3F, 2.9501F, 0.0133F, -3.0731F));

		PartDefinition Worm46 = Clock_Head.addOrReplaceChild("Worm46", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 4.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r29 = Worm46.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.8F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.8F, -7.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.8F, -5.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r30 = Worm46.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(12, 4).addBox(0.4F, -5.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -7.2F, 0.0F, 0.0F, 0.0F, -1.8151F));

		PartDefinition cube_r31 = Worm46.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(12, 12).addBox(0.4F, -1.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, -5.5F, 0.0F, 0.0F, 0.0F, -0.8203F));

		PartDefinition cube_r32 = Worm46.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(12, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -6.6F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition Worm45 = Clock_Head.addOrReplaceChild("Worm45", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 3.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r33 = Worm45.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.8F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.8F, -7.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.8F, -5.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r34 = Worm45.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -5.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.3F, 0.0F, 0.0F, 0.0F, -1.4486F));

		PartDefinition cube_r35 = Worm45.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -1.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9F, 0.0F, 0.0F, 0.0F, -1.7104F));

		PartDefinition cube_r36 = Worm45.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.7F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition Worm44 = Clock_Head.addOrReplaceChild("Worm44", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 2.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r37 = Worm44.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -3.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -7.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.8F, -5.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm43 = Clock_Head.addOrReplaceChild("Worm43", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 1.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r38 = Worm43.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(12, 0).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r39 = Worm43.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(8, 8).addBox(-0.4922F, -1.8457F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 0.6F, 0.0F, 3.1416F, 0.0F, -2.7925F));

		PartDefinition cube_r40 = Worm43.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(12, 12).addBox(0.4F, -1.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(0.4F, -1.7F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -5.3F, -1.0F, 0.0F, 0.0F, -1.2566F));

		PartDefinition cube_r41 = Worm43.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.9F, -1.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r42 = Worm43.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(12, 4).addBox(0.4F, -5.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(0.4F, -5.7F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.1F, -1.0F, 0.0F, 0.0F, -1.9199F));

		PartDefinition cube_r43 = Worm43.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(12, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.7F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r44 = Worm43.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(8, 8).addBox(-1.1945F, -1.8089F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.4F, 0.0F, 3.1416F, 0.0F, 2.6878F));

		PartDefinition cube_r45 = Worm43.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.4F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm42 = Clock_Head.addOrReplaceChild("Worm42", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, 0.0F, 0.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r46 = Worm42.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r47 = Worm42.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -9.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -4.9F, -13.7F, -1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r48 = Worm42.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -9.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -4.9F, -11.7F, -1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r49 = Worm42.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -9.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -4.9F, -9.7F, -1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r50 = Worm42.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(8, 8).addBox(-0.4196F, -1.8157F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.6F, 0.0F, 3.1416F, 0.0F, -2.7053F));

		PartDefinition cube_r51 = Worm42.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(8, 8).addBox(0.4F, -5.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(0.4F, -1.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(0.4F, -3.7F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r52 = Worm42.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(8, 8).addBox(-1.1804F, -1.8157F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -3.4F, 0.0F, 3.1416F, 0.0F, 2.7053F));

		PartDefinition cube_r53 = Worm42.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(8, 8).addBox(-0.8F, -1.9F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.4F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Clock_body = partdefinition.addOrReplaceChild("Clock_body", CubeListBuilder.create().texOffs(4, 11).addBox(3.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(0.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(1.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(3.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, -9.8F, -3.7F));

		PartDefinition cube_r54 = Clock_body.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 26.7F, 3.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r55 = Clock_body.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(4, 11).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 26.7F, 3.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Worm40 = Clock_body.addOrReplaceChild("Worm40", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r56 = Worm40.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9F, 4.2F, -0.2F, -3.1416F, 0.0F, -0.8465F));

		PartDefinition cube_r57 = Worm40.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -4.6F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r58 = Worm40.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -2.6F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r59 = Worm40.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(12, 4).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4F, 3.4F, -0.3F, -3.1416F, 0.0F, -1.1868F));

		PartDefinition cube_r60 = Worm40.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(12, 12).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -3.1F, 0.0F, 3.1416F, 1.5708F));

		PartDefinition cube_r61 = Worm40.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(8, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.4F, -1.0F, 1.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-1.4F, -1.0F, 2.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.1F, -5.1F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r62 = Worm40.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(8, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.4F, -1.0F, 2.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -5.1F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r63 = Worm40.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(12, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7F, 3.4F, -4.1F, 0.0F, 0.0F, 0.9076F));

		PartDefinition cube_r64 = Worm40.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(12, 4).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 4.6F, -4.1F, 0.0F, 0.0F, 0.6632F));

		PartDefinition cube_r65 = Worm40.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(8, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4F, 2.4F, -1.1F, 0.0F, 0.0F, 1.8675F));

		PartDefinition cube_r66 = Worm40.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(8, 8).addBox(1.0395F, -1.2847F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -1.1F, 3.1416F, 0.0F, -1.9897F));

		PartDefinition cube_r67 = Worm40.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(12, 8).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -0.8F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition Worm39 = Clock_body.addOrReplaceChild("Worm39", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 31.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r68 = Worm39.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, 2.4F, -0.1F, 3.1416F, 0.0F, -2.0246F));

		PartDefinition cube_r69 = Worm39.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -4.6F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r70 = Worm39.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(12, 0).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -2.6F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r71 = Worm39.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(12, 4).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -0.1F, 0.0F, 3.1416F, 1.5708F));

		PartDefinition cube_r72 = Worm39.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(12, 12).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -3.1F, 0.0F, 3.1416F, 1.5708F));

		PartDefinition cube_r73 = Worm39.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(12, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-1.4F, -1.0F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.4F, -1.0F, 1.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.4F, -1.0F, 3.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.1F, -5.1F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r74 = Worm39.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(12, 4).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.4F, -1.0F, 0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -5.1F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r75 = Worm39.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(8, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 2.8F, -2.1F, 0.0F, 0.0F, 1.2741F));

		PartDefinition cube_r76 = Worm39.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(8, 8).addBox(-1.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, 2.8F, -2.1F, 0.0F, 0.0F, 1.7802F));

		PartDefinition cube_r77 = Worm39.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(12, 12).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.1F, -1.1F, 0.0F, 3.1416F, 1.5708F));

		PartDefinition cube_r78 = Worm39.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(12, 8).addBox(0.4F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 3.1F, -0.6F, 1.5708F, 0.0F, -1.5708F));

		PartDefinition Worm37 = Clock_body.addOrReplaceChild("Worm37", CubeListBuilder.create().texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 0.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r79 = Worm37.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r80 = Worm37.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.4F, 0.0F, 0.0F, 0.0F, -0.2182F));

		PartDefinition cube_r81 = Worm37.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.4F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition Worm35 = Clock_body.addOrReplaceChild("Worm35", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 1.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r82 = Worm35.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm33 = Clock_body.addOrReplaceChild("Worm33", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 2.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r83 = Worm33.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm31 = Clock_body.addOrReplaceChild("Worm31", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 3.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r84 = Worm31.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 1.3F, 0.0F, 3.1416F, 0.0F, -2.8274F));

		PartDefinition cube_r85 = Worm31.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, -0.6F, 0.0F, 3.1416F, 0.0F, -2.8362F));

		PartDefinition cube_r86 = Worm31.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -9.4F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r87 = Worm31.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -6.4F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r88 = Worm31.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, -8.2F, 0.0F, 0.0F, 0.0F, -0.829F));

		PartDefinition cube_r89 = Worm31.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, -4.5F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r90 = Worm31.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4F, -2.6F, 0.0F, 3.1416F, 0.0F, -2.7489F));

		PartDefinition Worm29 = Clock_body.addOrReplaceChild("Worm29", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 4.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r91 = Worm29.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm27 = Clock_body.addOrReplaceChild("Worm27", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 5.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r92 = Worm27.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r93 = Worm27.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -11.4F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition cube_r94 = Worm27.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -9.4F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Worm25 = Clock_body.addOrReplaceChild("Worm25", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 15.0F, 6.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r95 = Worm25.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm23 = Clock_body.addOrReplaceChild("Worm23", CubeListBuilder.create().texOffs(12, 12).addBox(0.7F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r96 = Worm23.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r97 = Worm23.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, -9.4F, 0.0F, 0.0F, 0.0F, -0.3316F));

		PartDefinition cube_r98 = Worm23.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -7.4F, 0.0F, 0.0F, 0.0F, -0.3403F));

		PartDefinition cube_r99 = Worm23.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, -3.4F, 0.0F, 3.1416F, 0.0F, -2.9845F));

		PartDefinition cube_r100 = Worm23.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -1.5F, 0.0F, 3.1416F, 0.0F, -2.7925F));

		PartDefinition Worm21 = Clock_body.addOrReplaceChild("Worm21", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r101 = Worm21.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm19 = Clock_body.addOrReplaceChild("Worm19", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r102 = Worm19.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm17 = Clock_body.addOrReplaceChild("Worm17", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r103 = Worm17.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, 2.6F, 0.0F, 3.1416F, 0.0F, 2.81F));

		PartDefinition cube_r104 = Worm17.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, 0.7F, 0.0F, 3.1416F, 0.0F, 2.8798F));

		PartDefinition cube_r105 = Worm17.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -3.4F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r106 = Worm17.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.3F, 0.0F, 3.1416F, 0.0F, 2.9496F));

		PartDefinition Worm15 = Clock_body.addOrReplaceChild("Worm15", CubeListBuilder.create().texOffs(12, 8).addBox(-1.5F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r107 = Worm15.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r108 = Worm15.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -9.4F, 0.0F, 0.0F, 0.0F, 0.192F));

		PartDefinition cube_r109 = Worm15.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.4F, 0.0F, 0.0F, 0.0F, -0.2793F));

		PartDefinition Worm13 = Clock_body.addOrReplaceChild("Worm13", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 15.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r110 = Worm13.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm11 = Clock_body.addOrReplaceChild("Worm11", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 5.1F));

		PartDefinition cube_r111 = Worm11.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r112 = Worm11.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -12.3F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r113 = Worm11.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.3F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Worm9 = Clock_body.addOrReplaceChild("Worm9", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 4.2F));

		PartDefinition cube_r114 = Worm9.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r115 = Worm9.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -4.3F, 0.0F, 3.1416F, 0.0F, -2.8274F));

		PartDefinition cube_r116 = Worm9.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -2.3F, 0.0F, 3.1416F, 0.0F, 2.7489F));

		PartDefinition Worm7 = Clock_body.addOrReplaceChild("Worm7", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 3.2F));

		PartDefinition cube_r117 = Worm7.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm5 = Clock_body.addOrReplaceChild("Worm5", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 2.2F));

		PartDefinition cube_r118 = Worm5.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm3 = Clock_body.addOrReplaceChild("Worm3", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 1.2F));

		PartDefinition cube_r119 = Worm3.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm2 = Clock_body.addOrReplaceChild("Worm2", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 15.0F, 0.3F));

		PartDefinition cube_r120 = Worm2.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r121 = Worm2.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -6.3F, 0.0F, 0.0F, 0.0F, 0.4189F));

		PartDefinition cube_r122 = Worm2.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, -4.3F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r123 = Worm2.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -2.3F, 0.0F, 3.1416F, 0.0F, 2.8798F));

		PartDefinition Worm38 = Clock_body.addOrReplaceChild("Worm38", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 0.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r124 = Worm38.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm36 = Clock_body.addOrReplaceChild("Worm36", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 1.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r125 = Worm36.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4F, 0.5F, 0.0F, 3.1416F, 0.0F, -2.4871F));

		PartDefinition cube_r126 = Worm36.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, -1.1F, 0.0F, 3.1416F, 0.0F, -2.618F));

		PartDefinition cube_r127 = Worm36.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -5.4F, 0.0F, 0.0F, 0.0F, -0.2182F));

		PartDefinition cube_r128 = Worm36.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8F, -4.0F, 0.0F, 3.1416F, 0.0F, 2.8362F));

		PartDefinition cube_r129 = Worm36.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -2.4F, 0.0F, 3.1416F, 0.0F, -2.6616F));

		PartDefinition Worm34 = Clock_body.addOrReplaceChild("Worm34", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 2.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r130 = Worm34.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm32 = Clock_body.addOrReplaceChild("Worm32", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -10.1F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -12.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -14.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 3.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r131 = Worm32.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -8.1F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.3F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm30 = Clock_body.addOrReplaceChild("Worm30", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 4.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r132 = Worm30.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm28 = Clock_body.addOrReplaceChild("Worm28", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 5.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r133 = Worm28.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r134 = Worm28.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, -11.4F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r135 = Worm28.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -9.6F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition Worm26 = Clock_body.addOrReplaceChild("Worm26", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 31.0F, 6.1F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r136 = Worm26.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm24 = Clock_body.addOrReplaceChild("Worm24", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r137 = Worm24.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm22 = Clock_body.addOrReplaceChild("Worm22", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r138 = Worm22.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7F, 0.9F, 0.0F, 3.1416F, 0.0F, 1.9722F));

		PartDefinition cube_r139 = Worm22.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, -11.4F, 0.0F, 0.0F, 0.0F, -0.4712F));

		PartDefinition cube_r140 = Worm22.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1F, 0.2F, 0.0F, 3.1416F, 0.0F, 2.2689F));

		PartDefinition cube_r141 = Worm22.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -9.4F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition cube_r142 = Worm22.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -3.4F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r143 = Worm22.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.2F, 0.0F, 3.1416F, 0.0F, 2.6704F));

		PartDefinition Worm20 = Clock_body.addOrReplaceChild("Worm20", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r144 = Worm20.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm18 = Clock_body.addOrReplaceChild("Worm18", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r145 = Worm18.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm16 = Clock_body.addOrReplaceChild("Worm16", CubeListBuilder.create().texOffs(12, 8).addBox(-0.4F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r146 = Worm16.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r147 = Worm16.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -9.4F, 0.0F, 0.0F, 0.0F, -0.4014F));

		PartDefinition cube_r148 = Worm16.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -5.4F, 0.0F, 0.0F, 0.0F, 0.2269F));

		PartDefinition Worm14 = Clock_body.addOrReplaceChild("Worm14", CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 31.0F, 6.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r149 = Worm14.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -3.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -7.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.9F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.6F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm12 = Clock_body.addOrReplaceChild("Worm12", CubeListBuilder.create().texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 5.1F));

		PartDefinition cube_r150 = Worm12.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r151 = Worm12.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3F, -12.3F, 0.0F, 0.0F, 0.0F, 0.6109F));

		PartDefinition cube_r152 = Worm12.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, -10.3F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cube_r153 = Worm12.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.3F, 0.0F, 0.0F, 0.0F, 0.4189F));

		PartDefinition Worm10 = Clock_body.addOrReplaceChild("Worm10", CubeListBuilder.create().texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 4.2F));

		PartDefinition cube_r154 = Worm10.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r155 = Worm10.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -6.3F, 0.0F, 0.0F, 0.0F, 0.192F));

		PartDefinition cube_r156 = Worm10.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -8.3F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r157 = Worm10.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, -4.3F, 0.0F, 3.1416F, 0.0F, 2.7751F));

		PartDefinition Worm8 = Clock_body.addOrReplaceChild("Worm8", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 3.2F));

		PartDefinition cube_r158 = Worm8.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm6 = Clock_body.addOrReplaceChild("Worm6", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 2.2F));

		PartDefinition cube_r159 = Worm6.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r160 = Worm6.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -0.3F, 0.0F, 3.1416F, 0.0F, -2.8798F));

		PartDefinition cube_r161 = Worm6.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(8, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.3F, 0.0F, 3.1416F, 0.0F, 2.8623F));

		PartDefinition Worm4 = Clock_body.addOrReplaceChild("Worm4", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 1.2F));

		PartDefinition cube_r162 = Worm4.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Worm = Clock_body.addOrReplaceChild("Worm", CubeListBuilder.create().texOffs(12, 8).addBox(-1.0F, -9.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-1.0F, -7.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-1.0F, -11.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(-1.0F, -13.3F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 31.0F, 0.3F));

		PartDefinition cube_r163 = Worm.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 4).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 12).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, 8).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 1.7F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition pendulum = Clock_body.addOrReplaceChild("pendulum", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Worm_Clock = partdefinition.addOrReplaceChild("Worm_Clock", CubeListBuilder.create(), PartPose.offsetAndRotation(5.2F, -4.3F, -2.5F, 0.0F, -0.2618F, 0.0F));

		PartDefinition Worm_First = Worm_Clock.addOrReplaceChild("Worm_First", CubeListBuilder.create(), PartPose.offset(0.5F, -5.0F, -0.5F));

		PartDefinition cube_r164 = Worm_First.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5433F, -0.9282F, 0.3961F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.7F, 0.6F, -1.6906F, 1.3327F, 2.9362F));

		PartDefinition cube_r165 = Worm_First.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(12, 0).addBox(-0.0312F, -1.6447F, -0.0822F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -4.4F, 0.0F, 0.5313F, 0.0714F, -0.5495F));

		PartDefinition cube_r166 = Worm_First.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(12, 4).addBox(-0.4308F, -1.673F, 0.0935F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, 1.1F, 0.8568F, -1.0479F, -0.1545F));

		PartDefinition cube_r167 = Worm_First.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(12, 4).addBox(-0.2221F, -1.8512F, -0.5904F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -2.8F, 0.3F, -0.1047F, -0.0036F, -0.314F));

		PartDefinition cube_r168 = Worm_First.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(12, 12).addBox(-1.278F, -1.2761F, -0.8583F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 0.5F, -0.1F, -0.4302F, 0.2643F, 1.1097F));

		PartDefinition cube_r169 = Worm_First.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(12, 8).addBox(-0.9192F, -1.7912F, -0.5904F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, -0.9F, 0.1F, -0.0755F, 0.0727F, 0.4859F));

		PartDefinition cube_r170 = Worm_First.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(12, 12).addBox(0.339F, -0.7551F, -0.2852F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, 0.4F, -0.2F, 2.8266F, -0.1519F, -1.2317F));

		PartDefinition cube_r171 = Worm_First.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(12, 8).addBox(-0.0037F, -0.3245F, -0.1723F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9F, -0.5F, 0.6F, 2.4952F, 0.2848F, -0.6121F));

		PartDefinition Worm_Second = Worm_Clock.addOrReplaceChild("Worm_Second", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r172 = Worm_Second.addOrReplaceChild("cube_r172", CubeListBuilder.create().texOffs(12, 12).addBox(-0.9677F, -1.2588F, -1.2241F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -6.7F, 1.3F, -2.0216F, -0.8647F, 0.9287F));

		PartDefinition cube_r173 = Worm_Second.addOrReplaceChild("cube_r173", CubeListBuilder.create().texOffs(12, 4).addBox(-0.5853F, -0.1555F, -0.2009F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8F, -4.1F, -1.9F, 2.5425F, -1.2146F, 0.2752F));

		PartDefinition cube_r174 = Worm_Second.addOrReplaceChild("cube_r174", CubeListBuilder.create().texOffs(12, 0).addBox(0.393F, -0.8916F, -0.5278F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, -11.9F, 0.4F, 3.0822F, -0.0229F, 1.7106F));

		PartDefinition cube_r175 = Worm_Second.addOrReplaceChild("cube_r175", CubeListBuilder.create().texOffs(12, 4).addBox(0.0922F, -1.673F, -0.5795F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, -9.3F, 0.8F, 2.7331F, -0.3172F, -2.3766F));

		PartDefinition cube_r176 = Worm_Second.addOrReplaceChild("cube_r176", CubeListBuilder.create().texOffs(12, 12).addBox(-0.0413F, -1.0756F, 0.2706F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -6.3F, 0.0F, -1.0714F, 0.7594F, -2.3624F));

		PartDefinition cube_r177 = Worm_Second.addOrReplaceChild("cube_r177", CubeListBuilder.create().texOffs(12, 0).addBox(-0.4982F, 0.6F, -0.5084F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -1.6F, -2.1F, -3.1324F, -1.2915F, -0.0041F));

		PartDefinition cube_r178 = Worm_Second.addOrReplaceChild("cube_r178", CubeListBuilder.create().texOffs(12, 8).addBox(-0.2069F, -1.5547F, 0.1453F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -5.6F, -1.2F, -1.6636F, 1.034F, -2.4512F));

		PartDefinition cube_r179 = Worm_Second.addOrReplaceChild("cube_r179", CubeListBuilder.create().texOffs(12, 8).addBox(-0.4355F, -1.8332F, -0.8342F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7F, -8.0F, 1.6F, 2.6904F, -0.7697F, -3.0416F));

		PartDefinition Clockhands = Worm_Clock.addOrReplaceChild("Clockhands", CubeListBuilder.create(), PartPose.offset(-1.85F, -8.7F, -0.5F));

		PartDefinition cube_r180 = Clockhands.addOrReplaceChild("cube_r180", CubeListBuilder.create().texOffs(10, 0).addBox(-0.8505F, -2.2244F, -0.2787F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.1F, 0.1F, 0.4356F, -0.1857F, 2.6989F));

		PartDefinition cube_r181 = Clockhands.addOrReplaceChild("cube_r181", CubeListBuilder.create().texOffs(10, 0).addBox(0.2998F, -1.5282F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 1.2F, 0.0F, 0.0F, 0.0F, -2.1118F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Clock_Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		Clock_body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		Worm_Clock.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

}