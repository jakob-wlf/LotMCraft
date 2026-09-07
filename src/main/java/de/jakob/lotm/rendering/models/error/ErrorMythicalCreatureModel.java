package de.jakob.lotm.rendering.models.error;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ErrorMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "error_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Clock_Head;
	private final ModelPart Clock_body;
	private final ModelPart pendulum;

	public ErrorMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Clock_Head = root.getChild("Clock_Head");
		this.Clock_body = root.getChild("Clock_body");
		this.pendulum = this.Clock_body.getChild("pendulum");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Clock_Head = partdefinition.addOrReplaceChild("Clock_Head", CubeListBuilder.create().texOffs(4, 11).addBox(-2.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-4.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-2.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-4.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-5.8F, 0.7F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-5.8F, -1.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-5.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-4.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-2.8F, -3.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-2.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-4.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-5.8F, -5.3F, 5.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -10.8F, -4.1F));

		PartDefinition cube_r1 = Clock_Head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, 2.7F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r2 = Clock_Head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.7F, -3.3F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition Clock_body = partdefinition.addOrReplaceChild("Clock_body", CubeListBuilder.create().texOffs(4, 11).addBox(3.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 8.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 6.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 4.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 2.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 10.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 12.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 14.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 16.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 22.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 20.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 18.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(0.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(1.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 24.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 26.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 28.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(3.0F, 30.7F, 4.6F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, -9.8F, -3.7F));

		PartDefinition cube_r3 = Clock_body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 26.7F, 3.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r4 = Clock_body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(4, 11).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 26.7F, 3.6F, 0.0F, -1.5708F, 0.0F));

		PartDefinition pendulum = Clock_body.addOrReplaceChild("pendulum", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
		Clock_Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		Clock_body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		pendulum.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}