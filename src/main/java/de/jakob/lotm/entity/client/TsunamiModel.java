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

public class TsunamiModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "model"), "main");
	private final ModelPart lower_appendage_left;
	private final ModelPart lower_appendage_right;
	private final ModelPart bb_main;

	public TsunamiModel(ModelPart root) {
		this.lower_appendage_left = root.getChild("lower_appendage_left");
		this.lower_appendage_right = root.getChild("lower_appendage_right");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition lower_appendage_left = partdefinition.addOrReplaceChild("lower_appendage_left", CubeListBuilder.create().texOffs(730, 273).addBox(-2.0F, -65.0F, -10.0F, 2.0F, 18.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(748, 273).addBox(0.0F, -59.0F, -10.0F, 2.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(748, 311).addBox(2.0F, -56.0F, -10.0F, 2.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(488, 330).addBox(4.0F, -53.0F, -10.0F, 2.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(524, 330).addBox(6.0F, -51.0F, -10.0F, 2.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(560, 330).addBox(8.0F, -50.0F, -10.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 229.0F, -50.0F));

		PartDefinition lower_appendage_right = partdefinition.addOrReplaceChild("lower_appendage_right", CubeListBuilder.create().texOffs(730, 298).addBox(-2.0F, -65.0F, -14.0F, 2.0F, 18.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(748, 292).addBox(0.0F, -59.0F, -14.0F, 2.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(766, 273).addBox(2.0F, -56.0F, -14.0F, 2.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(506, 330).addBox(4.0F, -53.0F, -14.0F, 2.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(542, 330).addBox(6.0F, -51.0F, -14.0F, 2.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(578, 330).addBox(8.0F, -50.0F, -14.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 229.0F, 67.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 345).addBox(-1.0F, -46.0F, -60.0F, 2.0F, 46.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(484, 668).addBox(-1.0F, -54.0F, -60.0F, 9.0F, 8.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(728, 330).addBox(8.0F, -52.0F, -59.0F, 11.0F, 6.0F, 118.0F, new CubeDeformation(0.0F))
		.texOffs(728, 454).addBox(8.0F, -54.0F, -60.0F, 8.0F, 6.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 172).addBox(-3.0F, -53.0F, -60.0F, 2.0F, 53.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.0F, -52.0F, -60.0F, 3.0F, 52.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(244, 172).addBox(-8.0F, -51.0F, -60.0F, 2.0F, 51.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(246, 0).addBox(-9.0F, -50.0F, -60.0F, 1.0F, 50.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(244, 343).addBox(-10.0F, -49.0F, -60.0F, 1.0F, 49.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(486, 343).addBox(-11.0F, -48.0F, -60.0F, 1.0F, 48.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(488, 0).addBox(-12.0F, -46.0F, -60.0F, 1.0F, 46.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(488, 166).addBox(-13.0F, -44.0F, -60.0F, 1.0F, 44.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 511).addBox(-14.0F, -41.0F, -60.0F, 1.0F, 41.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(486, 511).addBox(-15.0F, -37.0F, -60.0F, 1.0F, 37.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(242, 512).addBox(-16.0F, -32.0F, -60.0F, 1.0F, 32.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(242, 664).addBox(-17.0F, -27.0F, -60.0F, 1.0F, 27.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 672).addBox(-18.0F, -22.0F, -60.0F, 1.0F, 22.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(730, 0).addBox(-19.0F, -18.0F, -60.0F, 1.0F, 18.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(730, 138).addBox(-20.0F, -15.0F, -60.0F, 1.0F, 15.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(742, 702).addBox(-21.0F, -12.0F, -60.0F, 1.0F, 12.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(484, 796).addBox(-22.0F, -10.0F, -60.0F, 1.0F, 10.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(242, 811).addBox(-23.0F, -8.0F, -60.0F, 1.0F, 8.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 814).addBox(-24.0F, -6.0F, -60.0F, 1.0F, 6.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(726, 834).addBox(-26.0F, -4.0F, -60.0F, 2.0F, 4.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(488, 958).addBox(-28.0F, -3.0F, -60.0F, 2.0F, 3.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(970, 834).addBox(-30.0F, -2.0F, -60.0F, 2.0F, 2.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(742, 580).addBox(1.0F, -46.0F, -60.0F, 7.0F, 2.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(242, 939).addBox(1.0F, -44.0F, -60.0F, 3.0F, 2.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(0, 940).addBox(1.0F, -42.0F, -60.0F, 1.0F, 5.0F, 120.0F, new CubeDeformation(0.0F))
		.texOffs(972, 0).addBox(1.0F, -14.0F, -53.0F, 1.0F, 14.0F, 106.0F, new CubeDeformation(0.0F))
		.texOffs(974, 956).addBox(2.0F, -8.0F, -53.0F, 1.0F, 8.0F, 106.0F, new CubeDeformation(0.0F))
		.texOffs(972, 120).addBox(3.0F, -3.0F, -53.0F, 5.0F, 3.0F, 106.0F, new CubeDeformation(0.0F))
		.texOffs(984, 454).addBox(3.0F, -5.0F, -53.0F, 2.0F, 2.0F, 106.0F, new CubeDeformation(0.0F))
		.texOffs(732, 958).addBox(19.0F, -50.0F, -59.0F, 3.0F, 7.0F, 118.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 182.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 2048, 2048);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		lower_appendage_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		lower_appendage_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}