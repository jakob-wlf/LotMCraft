package de.jakob.lotm.entity.client;// Made with Blockbench 4.12.6
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

public class ExileDoorsModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "exile_doors"), "main");
	private final ModelPart door_1;
	private final ModelPart door_10;
	private final ModelPart door_7;
	private final ModelPart door_11;
	private final ModelPart door_2;
	private final ModelPart door_12;
	private final ModelPart door_6;
	private final ModelPart door_13;
	private final ModelPart door_8;
	private final ModelPart door_14;
	private final ModelPart door_9;
	private final ModelPart door_15;
	private final ModelPart door_3;
	private final ModelPart door_4;
	private final ModelPart door_17;
	private final ModelPart door_5;
	private final ModelPart door_18;

	public ExileDoorsModel(ModelPart root) {
		this.door_1 = root.getChild("door_1");
		this.door_10 = root.getChild("door_10");
		this.door_7 = root.getChild("door_7");
		this.door_11 = root.getChild("door_11");
		this.door_2 = root.getChild("door_2");
		this.door_12 = root.getChild("door_12");
		this.door_6 = root.getChild("door_6");
		this.door_13 = root.getChild("door_13");
		this.door_8 = root.getChild("door_8");
		this.door_14 = root.getChild("door_14");
		this.door_9 = root.getChild("door_9");
		this.door_15 = root.getChild("door_15");
		this.door_3 = root.getChild("door_3");
		this.door_4 = root.getChild("door_4");
		this.door_17 = root.getChild("door_17");
		this.door_5 = root.getChild("door_5");
		this.door_18 = root.getChild("door_18");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition door_1 = partdefinition.addOrReplaceChild("door_1", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(27.0F, 9.875F, 0.0F));

		PartDefinition door_10 = partdefinition.addOrReplaceChild("door_10", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.0F, 11.875F, -26.0F, 0.0F, 0.5236F, 0.0F));

		PartDefinition door_7 = partdefinition.addOrReplaceChild("door_7", CubeListBuilder.create().texOffs(36, 76).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 48).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 64).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 9.875F, -4.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition door_11 = partdefinition.addOrReplaceChild("door_11", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -2.125F, 12.0F, 2.8495F, 0.7401F, 2.7222F));

		PartDefinition door_2 = partdefinition.addOrReplaceChild("door_2", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 9.875F, -22.0F, 0.0F, 1.3963F, 0.0F));

		PartDefinition door_12 = partdefinition.addOrReplaceChild("door_12", CubeListBuilder.create().texOffs(36, 76).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 48).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 64).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.875F, -28.0F, 0.0F, 1.3963F, 0.0F));

		PartDefinition door_6 = partdefinition.addOrReplaceChild("door_6", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-36.0F, 9.875F, -19.0F, 2.5563F, 0.9275F, 2.4498F));

		PartDefinition door_13 = partdefinition.addOrReplaceChild("door_13", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-31.0F, -3.125F, -32.0F, 2.5563F, 0.9275F, 2.4498F));

		PartDefinition door_8 = partdefinition.addOrReplaceChild("door_8", CubeListBuilder.create().texOffs(36, 76).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 48).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 64).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-25.0F, 9.875F, 2.0F, 0.4411F, 0.547F, 0.0638F));

		PartDefinition door_14 = partdefinition.addOrReplaceChild("door_14", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-25.0F, -7.125F, 49.0F, 0.4411F, 0.547F, 0.0638F));

		PartDefinition door_9 = partdefinition.addOrReplaceChild("door_9", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, 3.875F, 27.0F, 0.1894F, 0.4899F, -0.538F));

		PartDefinition door_15 = partdefinition.addOrReplaceChild("door_15", CubeListBuilder.create().texOffs(36, 76).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 48).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 64).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -33.125F, 29.0F, 0.1894F, 0.4899F, -0.538F));

		PartDefinition door_3 = partdefinition.addOrReplaceChild("door_3", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -14.125F, -16.0F, -1.2725F, 0.9386F, -1.3277F));

		PartDefinition door_4 = partdefinition.addOrReplaceChild("door_4", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 9.875F, 17.0F, 0.0F, -2.0071F, 0.0F));

		PartDefinition door_17 = partdefinition.addOrReplaceChild("door_17", CubeListBuilder.create().texOffs(36, 76).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 48).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 64).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -27.125F, 19.0F, 2.1176F, -0.6219F, -1.9116F));

		PartDefinition door_5 = partdefinition.addOrReplaceChild("door_5", CubeListBuilder.create().texOffs(95, 41).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(59, 13).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(95, 13).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(95, 29).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 9.875F, 17.0F, 2.6349F, -1.0663F, -2.5765F));

		PartDefinition door_18 = partdefinition.addOrReplaceChild("door_18", CubeListBuilder.create().texOffs(36, 28).addBox(-1.0F, -22.875F, -3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -17.875F, -8.0F, 2.0F, 32.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.0F, -19.875F, -7.0F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-1.0F, -21.875F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -27.125F, 19.0F, 2.6349F, -1.0663F, -2.5765F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		door_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		door_18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}