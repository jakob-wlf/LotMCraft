package de.jakob.lotm.entity.client.spirits.dervish;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritDervishModel<T extends SpiritDervishEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_dervish"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart ring;
	private final ModelPart bone;
	private final ModelPart bone2;

	public SpiritDervishModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.ring = root.getChild("ring");
		this.bone = this.ring.getChild("bone");
		this.bone2 = this.ring.getChild("bone2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.5F, 0.0F));

		PartDefinition ring = partdefinition.addOrReplaceChild("ring", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone = ring.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, 0.0F));

		PartDefinition ring_r1 = bone.addOrReplaceChild("ring_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.9355F, 0.5747F, 0.6353F));

		PartDefinition bone2 = ring.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, 0.0F));

		PartDefinition ring_r2 = bone2.addOrReplaceChild("ring_r2", CubeListBuilder.create().texOffs(0, 10).addBox(-4.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.6662F, 0.7268F, -2.812F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(SpiritDervishEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		this.animate(entity.IDLE_ANIMATION, SpiritDervishAnimations.IDLE, ageInTicks, 1f);
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
		ring.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}