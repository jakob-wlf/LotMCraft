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

public class HighSequenceDoorsModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "high_sequence_doors"), "main");
	private final ModelPart bb_main;

	public HighSequenceDoorsModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 43).addBox(12.0F, -30.0F, -2.0F, 2.0F, 30.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 43).addBox(-14.0F, -30.0F, -2.0F, 2.0F, 30.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-13.0F, -41.0F, -0.95F, 25.0F, 41.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 43).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 17.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-1.5424F, -41.9343F, -1.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 43).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3359F, -43.4755F, -1.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, 43).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.3359F, -43.4755F, -1.0F, 0.0F, 0.0F, 0.3927F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}