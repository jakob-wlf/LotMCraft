package de.jakob.lotm.entity.client.ability_entities.door_pathway.exile_doors;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ExileDoorsEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ExileDoorsModel<T extends ExileDoorsEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "exile_doors"), "main");
	private final ModelPart root;
	private final ModelPart bone1;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;

	public ExileDoorsModel(ModelPart root) {
		this.root = root;
		this.bone1 = root.getChild("bone1");
		this.bone2 = this.bone1.getChild("bone2");
		this.bone3 = this.bone1.getChild("bone3");
		this.bone4 = this.bone1.getChild("bone4");
		this.bone5 = root.getChild("bone5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone1 = partdefinition.addOrReplaceChild("bone1", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone2 = bone1.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -41.0F, -24.0F, 48.0F, 0.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone3 = bone1.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 48).addBox(-16.4396F, -29.0F, -16.0927F, 32.0F, 0.0F, 32.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-16.4396F, -51.0F, -16.0927F, 32.0F, 0.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone4 = bone1.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 80).addBox(-8.0F, -13.0F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bone5.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-1, 220).addBox(-8.5F, -17.0F, -1.0F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.1901F, -67.3293F, 19.103F, 3.0839F, 0.8253F, -0.7539F));

		PartDefinition cube_r2 = bone5.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-1, 220).addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -62.0F, 4.0F, 2.2771F, 0.5943F, -0.2601F));

		PartDefinition cube_r3 = bone5.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-1, 220).addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -57.0F, 0.0F, 0.0F, 0.829F, -2.618F));

		PartDefinition cube_r4 = bone5.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(-1, 220).mirror().addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -57.0F, -4.0F, -3.1416F, -0.5672F, -0.5236F));

		PartDefinition cube_r5 = bone5.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(-1, 220).mirror().addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(14.0F, -63.0F, 11.0F, 0.5387F, -0.2261F, 3.0084F));

		PartDefinition cube_r6 = bone5.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(-1, 220).addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.0F, 0.0F, -0.3645F, -0.28F, 0.1212F));

		PartDefinition cube_r7 = bone5.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(-1, 220).mirror().addBox(-6.9621F, -30.5F, 22.2937F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 2.7909F, 0.0756F, 3.0979F));

		PartDefinition cube_r8 = bone5.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(-1, 220).mirror().addBox(-9.0F, -17.0F, -1.0F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-27.0F, -26.0F, -8.0F, -3.0975F, -1.3095F, 2.7058F));

		PartDefinition cube_r9 = bone5.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(-1, 220).addBox(-9.0F, -17.0F, -1.0F, 17.0F, 34.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(27.0F, -17.0F, 0.0F, 0.0F, 1.4835F, 0.5236F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(ExileDoorsEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (!entity.IDLE.isStarted()) {
			entity.IDLE.start(0);
		}

		this.animate(entity.IDLE, ExileDoorsAnimation.idle, ageInTicks, 1f);
	}



	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bone1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}