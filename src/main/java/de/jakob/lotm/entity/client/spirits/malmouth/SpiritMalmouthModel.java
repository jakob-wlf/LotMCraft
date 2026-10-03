package de.jakob.lotm.entity.client.spirits.malmouth;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.translucent_wizard.SpiritTranslucentWizardAnimations;
import de.jakob.lotm.entity.custom.spirits.SpiritMalmouthEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritMalmouthModel<T extends SpiritMalmouthEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_malmouth"), "main");
	private final ModelPart root;
	private final ModelPart head;

	public SpiritMalmouthModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(SpiritMalmouthEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		boolean isFlying = entity.isFlying();

		if (isFlying) {
			// Stop idle animation and start/continue walk animation
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, SpiritMalmouthAnimations.WALK, ageInTicks, 1.0F);
		} else {
			// Stop walk animation and start/continue idle animation
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, SpiritMalmouthAnimations.IDLE, ageInTicks, 1.0F);
		}
	}

	private void applyHeadRotation(float headYaw, float headPitch) {
		headYaw = Mth.clamp(headYaw, -30f, 30f);
		headPitch = Mth.clamp(headPitch, -25f, 45);

		this.head.yRot = headYaw * ((float)Math.PI / 180f);
		this.head.xRot = headPitch *  ((float)Math.PI / 180f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}