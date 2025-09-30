package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class TornadoModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "tornado"), "main");

	private final ModelPart bottom2;
	private final ModelPart bottom;
	private final ModelPart bottom3;
	private final ModelPart bottom5;
	private final ModelPart top3;
	private final ModelPart top2;
	private final ModelPart top;

	private final ModelPart root;

	public TornadoModel(ModelPart root) {
		this.root = root;
		this.bottom2 = root.getChild("bottom2");
		this.bottom = root.getChild("bottom");
		this.bottom3 = root.getChild("bottom3");
		this.bottom5 = root.getChild("bottom5");
		this.top3 = root.getChild("top3");
		this.top2 = root.getChild("top2");
		this.top = root.getChild("top");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bottom2 = partdefinition.addOrReplaceChild("bottom2",
				CubeListBuilder.create().texOffs(496, 458).addBox(-9.0F, -17.0F, -9.0F, 18.0F, 10.0F, 17.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = partdefinition.addOrReplaceChild("bottom",
				CubeListBuilder.create().texOffs(496, 389).addBox(-12.0F, 70.25F, -9.65F, 24.0F, 9.0F, 23.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -53.25F, -2.35F));

		PartDefinition bottom3 = partdefinition.addOrReplaceChild("bottom3",
				CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bottom3.addOrReplaceChild("cube_r1",
				CubeListBuilder.create().texOffs(536, 204).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 15.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition bottom5 = partdefinition.addOrReplaceChild("bottom5",
				CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r2 = bottom5.addOrReplaceChild("cube_r2",
				CubeListBuilder.create().texOffs(496, 421).addBox(-10.0F, -18.0F, -11.0F, 20.0F, 18.0F, 19.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -28.25F, -1.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition top3 = partdefinition.addOrReplaceChild("top3",
				CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r3 = top3.addOrReplaceChild("cube_r3",
				CubeListBuilder.create().texOffs(496, 338).addBox(-17.0F, -17.0F, -18.0F, 34.0F, 17.0F, 34.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -44.25F, -1.0F, 0.0436F, 0.0F, 0.0F));

		PartDefinition top2 = partdefinition.addOrReplaceChild("top2",
				CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r4 = top2.addOrReplaceChild("cube_r4",
				CubeListBuilder.create().texOffs(0, 491).addBox(-37.0F, -16.0F, -38.0F, 74.0F, 16.0F, 74.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -61.25F, -1.75F, 0.0436F, 0.0F, 0.0F));

		PartDefinition top = partdefinition.addOrReplaceChild("top",
				CubeListBuilder.create().texOffs(0, 338).addBox(-62.0F, -105.25F, -65.35F, 124.0F, 29.0F, 124.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 1024, 1024);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Animate using the idle animation from TornadoAnimations
		animateRotation(bottom, ageInTicks, -810.0f, 3.0f); // Full rotation
		animateRotation(bottom3, ageInTicks, -360.0f, 3.0f);
		animateRotation(bottom2, ageInTicks, -360.0f, 3.0f);
		animateRotation(bottom5, ageInTicks, -360.0f, 3.0f);
		animateRotation(top3, ageInTicks, -360.0f, 3.0f);
		animateRotation(top2, ageInTicks, -540.0f, 3.0f);
		animateRotation(top, ageInTicks, -630.0f, 3.0f);

		// Position animations for bottom5 and top3 (bobbing effect)
		// Reset Y position first, then add animation
		bottom5.y = 24.0f; // Original offset
		top3.y = 24.0f; // Original offset
		animatePosition(bottom5, ageInTicks, 1.5f, 3.0f, 6); // 6 bobs in 3 seconds
		animatePosition(top3, ageInTicks, 1.5f, 3.0f, 4); // 4 bobs in 3 seconds
	}

	private void animateRotation(ModelPart part, float ageInTicks, float totalDegrees, float duration) {
		float progress = (ageInTicks % (duration * 20.0f)) / (duration * 20.0f);
		float radians = (float) Math.toRadians(totalDegrees * progress);
		part.yRot = radians;
	}

	private void animatePosition(ModelPart part, float ageInTicks, float height, float duration, int cycles) {
		float cycleTime = duration / cycles;
		float progress = (ageInTicks % (cycleTime * 20.0f)) / (cycleTime * 20.0f);
		float wave = Mth.sin(progress * (float) Math.PI * 2.0f);
		// Add to existing y position instead of replacing it
		part.y += wave * height;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bottom2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bottom3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bottom5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		top3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		top2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		top.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	public ModelPart root() {
		return this.root;
	}
}