package de.jakob.lotm.entity.client.spirits.blue_wizard;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritBlueWizardModel<T extends SpiritBlueWizardEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_blue_wizard"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart hat;

	public SpiritBlueWizardModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.hat = root.getChild("hat");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.5F, 0.0F));

		PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, 1.3647F, -4.0245F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(-2.5F, 0.3701F, -3.0245F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.2353F, 1.1604F));

		PartDefinition brim_r1 = hat.addOrReplaceChild("brim_r1", CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, -1.0F, -1.2F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.1299F, 1.4755F, -0.8727F, 0.0F, 0.0F));

		PartDefinition brim_r2 = hat.addOrReplaceChild("brim_r2", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2517F, 0.3168F, -0.5672F, 0.0F, 0.0F));

		PartDefinition brim_r3 = hat.addOrReplaceChild("brim_r3", CubeListBuilder.create().texOffs(0, 28).addBox(-0.5F, 0.5F, -0.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.1299F, -0.5245F, -0.3927F, 0.0F, 0.0F));

		PartDefinition brim_r4 = hat.addOrReplaceChild("brim_r4", CubeListBuilder.create().texOffs(0, 27).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8701F, -0.5245F, -0.0873F, 0.0F, 0.0F));

		PartDefinition brim_r5 = hat.addOrReplaceChild("brim_r5", CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.3701F, -0.5245F, 0.0F, 0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(SpiritBlueWizardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		boolean isWalking = limbSwingAmount > 0.01F;

		if (isWalking) {
			// Stop idle animation and start/continue walk animation
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, SpiritBlueWizardAnimations.WALK, ageInTicks, 1.0F);
		} else {
			// Stop walk animation and start/continue idle animation
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, SpiritBlueWizardAnimations.IDLE, ageInTicks, 1.0F);
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
		hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}