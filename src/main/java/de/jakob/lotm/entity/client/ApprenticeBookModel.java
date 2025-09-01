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

public class ApprenticeBookModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "apprentice_book"), "main");
	private final ModelPart main;
	private final ModelPart Cover;
	private final ModelPart pages;
	private final ModelPart bone;

	public ApprenticeBookModel(ModelPart root) {
		this.main = root.getChild("main");
		this.Cover = this.main.getChild("Cover");
		this.pages = this.main.getChild("pages");
		this.bone = this.main.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(-2, 0, 0.0F));

		PartDefinition Cover = main.addOrReplaceChild("Cover", CubeListBuilder.create().texOffs(46, 15).addBox(0.9659F, 0.2588F, -8.0F, 2.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Cover.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-2, 0).addBox(-2.0F, 0.0F, -8.0F, 8.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9319F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r2 = Cover.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-1, 0).addBox(-6.0F, 0.0F, -8.0F, 8.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition pages = main.addOrReplaceChild("pages", CubeListBuilder.create(), PartPose.offset(1.2159F, -0.7412F, -7.0F));

		PartDefinition cube_r3 = pages.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-2, 32).addBox(-1.6512F, -0.4253F, 0.0F, 8.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5672F));

		PartDefinition cube_r4 = pages.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 32).addBox(-6.3488F, -0.4253F, 0.0F, 8.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5672F));

		PartDefinition bone = main.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(38, 46).addBox(-2.9659F, -0.7412F, -7.0F, 2.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(3.9319F, 0.0F, 0.0F));

		PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(-2, 46).addBox(-5.0F, -1.0F, -7.0F, 7.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9319F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(44, 0).addBox(-2.0F, -1.0F, -7.0F, 7.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}



	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}