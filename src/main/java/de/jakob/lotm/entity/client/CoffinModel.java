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

public class CoffinModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "coffin"), "main");
	private final ModelPart main_body;
	private final ModelPart lid;

	public CoffinModel(ModelPart root) {
		this.main_body = root.getChild("main_body");
		this.lid = root.getChild("lid");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main_body = partdefinition.addOrReplaceChild("main_body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.175F, 1.95F, -3.325F, 10.0F, 0.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.825F, 22.0F, -7.675F));

		PartDefinition cube_r1 = main_body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 44).addBox(-0.5F, -2.0F, -13.0F, 1.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.45F, 0.0F, 6.6F, 0.0F, 3.0107F, 0.0F));

		PartDefinition cube_r2 = main_body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 44).addBox(-0.5F, -2.0F, -13.0F, 1.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, 0.0F, 6.6F, 0.0F, -3.0107F, 0.0F));

		PartDefinition cube_r3 = main_body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(68, 51).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, 0.0F, 19.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r4 = main_body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(68, 41).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, 0.0F, -3.4F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r5 = main_body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(64, 17).addBox(-0.5F, -2.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.65F, 0.0F, 0.0F, 0.0F, 0.3054F, 0.0F));

		PartDefinition cube_r6 = main_body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(64, 29).addBox(-0.5F, -2.0F, -4.0F, 1.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3054F, 0.0F));

		PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 22).addBox(-4.9786F, -0.3786F, -9.9521F, 10.0F, 0.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3214F, 15.2286F, -1.0479F, -0.1391F, -0.0876F, -1.183F));

		PartDefinition cube_r7 = lid.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(64, 0).addBox(-0.5F, 1.0F, -13.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6464F, -1.4286F, -0.0271F, 0.0F, 3.0107F, 0.0F));

		PartDefinition cube_r8 = lid.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 64).addBox(-0.5F, 1.0F, -13.0F, 1.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6036F, -1.4286F, -0.0271F, 0.0F, -3.0107F, 0.0F));

		PartDefinition cube_r9 = lid.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(70, 68).addBox(-0.5F, 1.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0536F, -1.4286F, 12.3729F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r10 = lid.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(70, 61).addBox(-0.5F, 1.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0536F, -1.4286F, -10.0271F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r11 = lid.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(52, 64).addBox(-0.5F, 1.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8464F, -1.4286F, -6.6271F, 0.0F, 0.3054F, 0.0F));

		PartDefinition cube_r12 = lid.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(34, 64).addBox(-0.5F, 1.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8036F, -1.4286F, -6.6271F, 0.0F, -0.3054F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		main_body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}