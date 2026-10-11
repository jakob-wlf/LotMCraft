package de.jakob.lotm.rendering.models.wheel_of_fortune;// Made with Blockbench 5.0.7
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
import net.minecraft.world.entity.Entity;

public class WheelOfFortuneMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "wof_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart head_top4;
	private final ModelPart head_top3;
	private final ModelPart head_top2;
	private final ModelPart head_top;
	private final ModelPart body;
	private final ModelPart segment12;
	private final ModelPart segment11;
	private final ModelPart segment10;
	private final ModelPart segment9;
	private final ModelPart segment8;
	private final ModelPart segment7;
	private final ModelPart segment6;
	private final ModelPart segment5;
	private final ModelPart segment4;
	private final ModelPart segment3;
	private final ModelPart segment2;
	private final ModelPart segment1;

	public WheelOfFortuneMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.head_top4 = this.head.getChild("head_top4");
		this.head_top3 = this.head.getChild("head_top3");
		this.head_top2 = this.head.getChild("head_top2");
		this.head_top = this.head.getChild("head_top");
		this.body = root.getChild("body");
		this.segment12 = this.body.getChild("segment12");
		this.segment11 = this.body.getChild("segment11");
		this.segment10 = this.body.getChild("segment10");
		this.segment9 = this.body.getChild("segment9");
		this.segment8 = this.body.getChild("segment8");
		this.segment7 = this.body.getChild("segment7");
		this.segment6 = this.body.getChild("segment6");
		this.segment5 = this.body.getChild("segment5");
		this.segment4 = this.body.getChild("segment4");
		this.segment3 = this.body.getChild("segment3");
		this.segment2 = this.body.getChild("segment2");
		this.segment1 = this.body.getChild("segment1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2318F, 11.3532F, -4.4552F, 0.1427F, 0.0475F, -0.009F));

		PartDefinition head_top4 = head.addOrReplaceChild("head_top4", CubeListBuilder.create().texOffs(35, 13).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.375F, -3.2681F, -8.1492F, 0.0873F, 0.0F, 0.0F));

		PartDefinition head_top3 = head.addOrReplaceChild("head_top3", CubeListBuilder.create().texOffs(34, 13).addBox(-3.5F, -7.0F, -11.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, 1.4227F, 2.3831F, 0.0436F, 0.0F, 0.0F));

		PartDefinition head_top2 = head.addOrReplaceChild("head_top2", CubeListBuilder.create().texOffs(-2, 10).addBox(-2.5F, -5.0F, -11.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, 1.4227F, 2.3831F, -0.0436F, 0.0F, 0.0F));

		PartDefinition head_top = head.addOrReplaceChild("head_top", CubeListBuilder.create().texOffs(-2, -1).addBox(-4.0F, -7.0F, -11.0F, 7.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.125F, 0.4227F, 3.3831F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(1.8081F, 4.6467F, 16.0F, 0.0F, 0.0F, 0.0436F));

		PartDefinition segment12 = body.addOrReplaceChild("segment12", CubeListBuilder.create().texOffs(41, 8).addBox(8.0F, -20.0F, 5.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-26.9167F, 5.5F, 11.0F, 2.117F, -1.3231F, 0.4109F));

		PartDefinition segment11 = body.addOrReplaceChild("segment11", CubeListBuilder.create(), PartPose.offsetAndRotation(-18.9167F, -6.5F, 10.0F, 1.324F, -1.2893F, 1.99F));

		PartDefinition body_r1 = segment11.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(37, 7).addBox(-1.5F, -2.0F, -4.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -21.0F, 10.0F, 0.0F, 0.0F, -2.8362F));

		PartDefinition segment10 = body.addOrReplaceChild("segment10", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.9167F, -11.5F, 3.0F, 0.5874F, -0.842F, -3.04F));

		PartDefinition body_r2 = segment10.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(24, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -19.3207F, 13.7337F, -0.0465F, 0.2577F, -1.7514F));

		PartDefinition segment9 = body.addOrReplaceChild("segment9", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0833F, -6.5F, 7.0F, 0.972F, -0.476F, -2.4207F));

		PartDefinition body_r3 = segment9.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(25, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, -18.5F, 14.5F, 0.0F, 0.0F, -0.829F));

		PartDefinition segment8 = body.addOrReplaceChild("segment8", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0833F, -11.5F, 3.0F, 0.4366F, 0.4561F, -2.3184F));

		PartDefinition body_r4 = segment8.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(28, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3029F, -18.8997F, 6.5F, 0.0F, 0.0F, 0.5672F));

		PartDefinition segment7 = body.addOrReplaceChild("segment7", CubeListBuilder.create(), PartPose.offsetAndRotation(13.0833F, 2.5F, -2.0F, 0.438F, 0.9263F, -1.6899F));

		PartDefinition body_r5 = segment7.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(26, 0).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, -19.5F, 6.5F, 0.0F, 0.0F, 1.309F));

		PartDefinition segment6 = body.addOrReplaceChild("segment6", CubeListBuilder.create(), PartPose.offsetAndRotation(17.0833F, -1.5F, 4.0F, 1.8171F, 0.932F, -0.6633F));

		PartDefinition body_r6 = segment6.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(28, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -24.5F, 6.5F, 0.0F, 0.0F, 1.6581F));

		PartDefinition segment5 = body.addOrReplaceChild("segment5", CubeListBuilder.create(), PartPose.offsetAndRotation(15.0833F, -1.5F, 6.0F, 2.7013F, 0.9136F, -0.4199F));

		PartDefinition body_r7 = segment5.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(25, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -19.5F, 9.5F, 0.0F, 0.0F, 2.8362F));

		PartDefinition segment4 = body.addOrReplaceChild("segment4", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0833F, -7.5F, -4.0F, -2.074F, 1.0066F, -0.0805F));

		PartDefinition body_r8 = segment4.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(25, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -19.5F, 9.5F, 0.0F, 0.0F, 2.3998F));

		PartDefinition segment3 = body.addOrReplaceChild("segment3", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.9167F, 6.5F, -13.0F, -0.7859F, 0.8229F, 0.0274F));

		PartDefinition body_r9 = segment3.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -19.9355F, 9.1437F, 0.0172F, -0.1298F, 1.4388F));

		PartDefinition segment2 = body.addOrReplaceChild("segment2", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.9167F, 13.5F, -15.0F, -0.2107F, 0.8635F, -0.1518F));

		PartDefinition body_r10 = segment2.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(23, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -18.0698F, 10.0127F, 0.1585F, -0.0735F, 0.4305F));

		PartDefinition segment1 = body.addOrReplaceChild("segment1", CubeListBuilder.create().texOffs(25, 0).addBox(-1.0F, -21.0F, 0.0F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0833F, 18.5F, -10.0F, 0.6031F, 0.6309F, 0.0198F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}