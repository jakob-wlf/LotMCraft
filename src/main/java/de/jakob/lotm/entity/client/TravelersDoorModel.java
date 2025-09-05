package de.jakob.lotm.entity.client;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.TravelersDoorEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class TravelersDoorModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "travelers_door"), "main");
	private final ModelPart door;
	private final ModelPart bb_main;

	public TravelersDoorModel(ModelPart root) {
		this.door = root.getChild("door");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition door = partdefinition.addOrReplaceChild("door", CubeListBuilder.create(), PartPose.offset(-0.5F, 0, -0.5F));

		PartDefinition cube_r1 = door.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -15.0F, 0.0F, 8.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.1135F, 0.0F, -2.703F, 0.0F, 2.1817F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -15.0F, -0.5F, 9.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Reset all transformations to default
		this.door.resetPose();

		// Apply spawn animation if entity is TravelersDoorEntity
		if (entity instanceof TravelersDoorEntity doorEntity) {
			if (doorEntity.isPlayingSpawnAnimation()) {
				// Calculate animation progress (0.0 to 1.0)
				float animationProgress = doorEntity.getSpawnAnimationProgress();

				// Apply the door opening animation manually based on keyframes
				applySpawnAnimation(animationProgress);
			}
		}
	}

	private void applySpawnAnimation(float progress) {
		// Clamp progress to animation duration (0.6667 seconds in your animation)
		float animTime = Math.min(progress * 1.5f, 1.0f); // Scale to match 0.6667s duration

		// Rotation animation: -125 degrees to 0 degrees
		if (animTime <= 0.6667f) {
			float rotationProgress = animTime / 0.6667f;
			float yRotation = Mth.lerp(rotationProgress, -125.0f, 0.0f);
			this.door.yRot = (float) Math.toRadians(yRotation);
		} else {
			this.door.yRot = 0.0f;
		}

		// Position animation with keyframes
		float xPos = 0.0f;
		float zPos = 0.0f;

		if (animTime <= 0.0833f) {
			float t = animTime / 0.0833f;
			xPos = Mth.lerp(t, -6.3f, -5.31f);
			zPos = Mth.lerp(t, 3.3f, 3.74f);
		} else if (animTime <= 0.1667f) {
			float t = (animTime - 0.0833f) / (0.1667f - 0.0833f);
			xPos = Mth.lerp(t, -5.31f, -4.32f);
			zPos = Mth.lerp(t, 3.74f, 3.97f);
		} else if (animTime <= 0.25f) {
			float t = (animTime - 0.1667f) / (0.25f - 0.1667f);
			xPos = Mth.lerp(t, -4.32f, -3.2f);
			zPos = Mth.lerp(t, 3.97f, 3.96f);
		} else if (animTime <= 0.3333f) {
			float t = (animTime - 0.25f) / (0.3333f - 0.25f);
			xPos = Mth.lerp(t, -3.2f, -2.08f);
			zPos = Mth.lerp(t, 3.96f, 3.55f);
		} else if (animTime <= 0.4167f) {
			float t = (animTime - 0.3333f) / (0.4167f - 0.3333f);
			xPos = Mth.lerp(t, -2.08f, -1.31f);
			zPos = Mth.lerp(t, 3.55f, 2.92f);
		} else if (animTime <= 0.5f) {
			float t = (animTime - 0.4167f) / (0.5f - 0.4167f);
			xPos = Mth.lerp(t, -1.31f, -0.54f);
			zPos = Mth.lerp(t, 2.92f, 2.08f);
		} else if (animTime <= 0.5833f) {
			float t = (animTime - 0.5f) / (0.5833f - 0.5f);
			xPos = Mth.lerp(t, -0.54f, -0.17f);
			zPos = Mth.lerp(t, 2.08f, 1.04f);
		} else if (animTime <= 0.6667f) {
			float t = (animTime - 0.5833f) / (0.6667f - 0.5833f);
			xPos = Mth.lerp(t, -0.17f, 0.0f);
			zPos = Mth.lerp(t, 1.04f, 0.0f);
		} else {
			xPos = 0.0f;
			zPos = 0.0f;
		}

		// Apply position offsets (convert from blocks to model units)
		this.door.x = xPos;
		this.door.z = zPos;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		door.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}