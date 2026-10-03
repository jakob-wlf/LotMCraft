package de.jakob.lotm.entity.client.spirits.bubbles;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.dervish.SpiritDervishAnimations;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpiritBubblesModel<T extends SpiritBubblesEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_bubbles"), "main");
	private final ModelPart root;
	private final ModelPart bubbles;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;

	public SpiritBubblesModel(ModelPart root) {
		this.root = root;
		this.bubbles = root.getChild("bubbles");
		this.bone = this.bubbles.getChild("bone");
		this.bone2 = this.bubbles.getChild("bone2");
		this.bone3 = this.bubbles.getChild("bone3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bubbles = partdefinition.addOrReplaceChild("bubbles", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

		PartDefinition bone = bubbles.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -3.5F, 0.0F));

		PartDefinition bone2 = bubbles.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0846F, 2.1088F, 2.2588F));

		PartDefinition head_r1 = bone2.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 10).addBox(0.5F, -8.0F, -3.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0846F, 6.8912F, -1.2588F, -0.2572F, -0.8124F, -0.0723F));

		PartDefinition bone3 = bubbles.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-1.5F, 5.0F, -0.5F));

		PartDefinition head_r2 = bone3.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4013F, 0.2013F, 0.4773F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(SpiritBubblesEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		this.animate(entity.IDLE_ANIMATION, SpiritBubblesAnimations.IDLE, ageInTicks, 1f);
	}

	private void applyHeadRotation(float headYaw, float headPitch) {
		headYaw = Mth.clamp(headYaw, -30f, 30f);
		headPitch = Mth.clamp(headPitch, -25f, 45);

		this.bubbles.yRot = headYaw * ((float)Math.PI / 180f);
		this.bubbles.xRot = headPitch *  ((float)Math.PI / 180f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bubbles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}



	@Override
	public ModelPart root() {
		return this.root;
	}
}