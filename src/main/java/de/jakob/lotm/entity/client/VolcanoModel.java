package de.jakob.lotm.entity.client;// Made with Blockbench 5.0.3
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

public class VolcanoModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "volcano"), "main");
	private final ModelPart bone;
	private final ModelPart bone9;
	private final ModelPart bone10;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;

	public VolcanoModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bone9 = root.getChild("bone9");
		this.bone10 = root.getChild("bone10");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.bone5 = root.getChild("bone5");
		this.bone6 = root.getChild("bone6");
		this.bone7 = root.getChild("bone7");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(80, 157).addBox(-10.0F, -3.0F, -10.0F, 20.0F, 5.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(120, 130).addBox(-9.0F, -3.0F, -11.0F, 18.0F, 5.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 156).addBox(-9.0F, -3.0F, -11.0F, 18.0F, 5.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone9 = partdefinition.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(120, 101).addBox(-12.0F, -3.0F, -12.0F, 24.0F, 5.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(120, 68).addBox(-11.0F, -3.0F, -14.0F, 22.0F, 5.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition cube_r2 = bone9.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(120, 35).addBox(-11.0F, -3.0F, -14.0F, 22.0F, 5.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone10 = partdefinition.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(120, 0).addBox(-15.0F, -3.0F, -15.0F, 30.0F, 5.0F, 30.0F, new CubeDeformation(0.0F))
		.texOffs(0, 119).addBox(-14.0F, -3.0F, -16.0F, 28.0F, 5.0F, 32.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-12.0F, -3.0F, -18.0F, 24.0F, 5.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition cube_r3 = bone10.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 41).addBox(-12.0F, -3.0F, -18.0F, 24.0F, 5.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r4 = bone10.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 82).addBox(-14.0F, -3.0F, -16.0F, 28.0F, 5.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(80, 182).addBox(-8.0F, -3.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(160, 178).addBox(-7.0F, -3.0F, -9.0F, 14.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

		PartDefinition cube_r5 = bone2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(160, 157).addBox(-7.0F, -3.0F, -9.0F, 14.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(200, 130).addBox(-7.0F, -4.0F, -7.0F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(144, 199).addBox(-6.0F, -4.0F, -8.0F, 12.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition cube_r6 = bone3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 183).addBox(-6.0F, -4.0F, -8.0F, 12.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 203).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(56, 201).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition cube_r7 = bone4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(200, 199).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(104, 217).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(216, 101).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r8 = bone5.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(104, 201).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone6 = partdefinition.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(216, 117).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 219).addBox(-3.0F, -4.0F, -5.0F, 6.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition cube_r9 = bone6.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(200, 217).addBox(-3.0F, -4.0F, -5.0F, 6.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone7 = partdefinition.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(56, 219).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(32, 219).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

		PartDefinition cube_r10 = bone7.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(56, 183).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bone7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}