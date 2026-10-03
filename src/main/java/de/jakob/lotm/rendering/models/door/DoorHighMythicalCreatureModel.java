package de.jakob.lotm.rendering.models.door;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class DoorHighMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "door_high_mythical_creature"), "main");

	private final AnimationState animationState = new AnimationState();

	private final ModelPart root;
	private final ModelPart doors;
	private final ModelPart door1;
	private final ModelPart door1a;
	private final ModelPart frame1a;
	private final ModelPart door2;
	private final ModelPart frame2a;
	private final ModelPart door2a;
	private final ModelPart door3;
	private final ModelPart door3a;
	private final ModelPart frame3a;
	private final ModelPart door4;
	private final ModelPart door4a;
	private final ModelPart door4b;
	private final ModelPart door4c;
	private final ModelPart star4;
	private final ModelPart tentacles;
	private final ModelPart rods;
	private final ModelPart tentacle1;
	private final ModelPart tentacle1_top;
	private final ModelPart tentacle1_bot;
	private final ModelPart tentacle1_bot5;
	private final ModelPart tentacle1_bot6;
	private final ModelPart tentacle2;
	private final ModelPart tentacle1_top2;
	private final ModelPart tentacle1_bot2;
	private final ModelPart tentacle1_bot4;
	private final ModelPart tentacle3;
	private final ModelPart tentacle1_bot3;
	private final ModelPart tentacle1_top3;
	private final ModelPart tentacle1_top4;
	private final ModelPart tentacle1_top5;

	public DoorHighMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.doors = root.getChild("doors");
		this.door1 = this.doors.getChild("door1");
		this.door1a = this.door1.getChild("door1a");
		this.frame1a = this.door1.getChild("frame1a");
		this.door2 = this.doors.getChild("door2");
		this.frame2a = this.door2.getChild("frame2a");
		this.door2a = this.door2.getChild("door2a");
		this.door3 = this.doors.getChild("door3");
		this.door3a = this.door3.getChild("door3a");
		this.frame3a = this.door3.getChild("frame3a");
		this.door4 = this.doors.getChild("door4");
		this.door4a = this.door4.getChild("door4a");
		this.door4b = this.door4.getChild("door4b");
		this.door4c = this.door4.getChild("door4c");
		this.star4 = this.door4.getChild("star4");
		this.tentacles = root.getChild("tentacles");
		this.rods = this.tentacles.getChild("rods");
		this.tentacle1 = this.rods.getChild("tentacle1");
		this.tentacle1_top = this.tentacle1.getChild("tentacle1_top");
		this.tentacle1_bot = this.tentacle1.getChild("tentacle1_bot");
		this.tentacle1_bot5 = this.tentacle1.getChild("tentacle1_bot5");
		this.tentacle1_bot6 = this.tentacle1.getChild("tentacle1_bot6");
		this.tentacle2 = this.rods.getChild("tentacle2");
		this.tentacle1_top2 = this.tentacle2.getChild("tentacle1_top2");
		this.tentacle1_bot2 = this.tentacle2.getChild("tentacle1_bot2");
		this.tentacle1_bot4 = this.tentacle2.getChild("tentacle1_bot4");
		this.tentacle3 = this.rods.getChild("tentacle3");
		this.tentacle1_bot3 = this.tentacle3.getChild("tentacle1_bot3");
		this.tentacle1_top3 = this.tentacle3.getChild("tentacle1_top3");
		this.tentacle1_top4 = this.tentacle3.getChild("tentacle1_top4");
		this.tentacle1_top5 = this.tentacle3.getChild("tentacle1_top5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition doors = partdefinition.addOrReplaceChild("doors", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));

		PartDefinition door1 = doors.addOrReplaceChild("door1", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, -1.0F));

		PartDefinition door1a = door1.addOrReplaceChild("door1a", CubeListBuilder.create().texOffs(1, 5).addBox(-6.0F, -11.0F, -1.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2233F, -0.2129F, -0.1702F));

		PartDefinition frame1a = door1.addOrReplaceChild("frame1a", CubeListBuilder.create().texOffs(0, 32).addBox(-7.0F, -12.0F, -2.0F, 14.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2233F, -0.2129F, -0.1702F));

		PartDefinition door2 = doors.addOrReplaceChild("door2", CubeListBuilder.create(), PartPose.offset(8.0F, -4.0F, 6.0F));

		PartDefinition frame2a = door2.addOrReplaceChild("frame2a", CubeListBuilder.create().texOffs(0, 32).addBox(-7.0F, -14.0F, 2.0F, 14.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, -0.8727F, 0.0F));

		PartDefinition door2a = door2.addOrReplaceChild("door2a", CubeListBuilder.create().texOffs(29, 5).addBox(-6.0F, -13.0F, 3.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, -0.8727F, 0.0F));

		PartDefinition door3 = doors.addOrReplaceChild("door3", CubeListBuilder.create(), PartPose.offset(-7.0F, -13.0F, 2.0F));

		PartDefinition door3a = door3.addOrReplaceChild("door3a", CubeListBuilder.create().texOffs(57, 5).addBox(-7.6905F, -11.0F, 3.6252F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4363F, 0.4363F));

		PartDefinition frame3a = door3.addOrReplaceChild("frame3a", CubeListBuilder.create().texOffs(0, 32).addBox(-8.6905F, -12.0F, 2.6252F, 13.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.4363F, 0.4363F));

		PartDefinition door4 = doors.addOrReplaceChild("door4", CubeListBuilder.create(), PartPose.offset(0.0F, -19.0F, 4.0F));

		PartDefinition door4a = door4.addOrReplaceChild("door4a", CubeListBuilder.create().texOffs(85, 4).addBox(-6.0F, -11.0F, 6.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition door4b = door4.addOrReplaceChild("door4b", CubeListBuilder.create().texOffs(85, 4).addBox(-6.0F, -11.0F, 6.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.2182F));

		PartDefinition door4c = door4.addOrReplaceChild("door4c", CubeListBuilder.create().texOffs(85, 4).addBox(-6.0F, -11.0F, 7.0F, 12.0F, 22.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition star4 = door4.addOrReplaceChild("star4", CubeListBuilder.create().texOffs(11, 90).addBox(-2.0F, -3.0F, 5.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -3.0F, 2.0F));

		PartDefinition tentacles = partdefinition.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(-3.0F, 8.0F, -19.0F));

		PartDefinition rods = tentacles.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3054F, 0.0F));

		PartDefinition tentacle1 = rods.addOrReplaceChild("tentacle1", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, 14.0F, 11.0F, -0.6622F, 0.4241F, -0.9373F));

		PartDefinition tentacle1_top = tentacle1.addOrReplaceChild("tentacle1_top", CubeListBuilder.create().texOffs(1, 73).addBox(0.0F, -14.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5155F, 0.0319F, -0.1768F));

		PartDefinition tentacle1_bot = tentacle1.addOrReplaceChild("tentacle1_bot", CubeListBuilder.create().texOffs(2, 74).addBox(3.15F, -9.5F, 0.2F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -3.5F, 4.6F, 1.4797F, -0.3606F, -0.106F));

		PartDefinition tentacle1_bot5 = tentacle1.addOrReplaceChild("tentacle1_bot5", CubeListBuilder.create().texOffs(2, 74).addBox(1.15F, -1.5F, -1.8F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -6.5F, 1.6F, 2.396F, -0.3606F, -0.106F));

		PartDefinition tentacle1_bot6 = tentacle1.addOrReplaceChild("tentacle1_bot6", CubeListBuilder.create().texOffs(2, 74).addBox(0.15F, -0.5F, -1.8F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -8.5F, 4.6F, -2.7895F, -0.4526F, -0.8548F));

		PartDefinition tentacle2 = rods.addOrReplaceChild("tentacle2", CubeListBuilder.create(), PartPose.offsetAndRotation(-15.0F, 9.0F, 15.0F, -1.3314F, 0.2746F, -0.794F));

		PartDefinition tentacle1_top2 = tentacle2.addOrReplaceChild("tentacle1_top2", CubeListBuilder.create().texOffs(1, 73).addBox(0.0F, -14.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3431F, 3.3826F, 1.7742F, 0.5155F, 0.0319F, -0.1768F));

		PartDefinition tentacle1_bot2 = tentacle2.addOrReplaceChild("tentacle1_bot2", CubeListBuilder.create().texOffs(2, 74).addBox(9.15F, 0.5F, 0.2F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6569F, -0.1174F, 6.3742F, 1.4406F, 0.8556F, -0.2367F));

		PartDefinition tentacle1_bot4 = tentacle2.addOrReplaceChild("tentacle1_bot4", CubeListBuilder.create().texOffs(2, 74).addBox(9.15F, -1.5F, 1.2F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3431F, -4.1174F, 14.3742F, 2.647F, 1.0869F, 0.1841F));

		PartDefinition tentacle3 = rods.addOrReplaceChild("tentacle3", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.0F, 0.0F, 9.0F, -0.616F, 0.1684F, -0.7156F));

		PartDefinition tentacle1_bot3 = tentacle3.addOrReplaceChild("tentacle1_bot3", CubeListBuilder.create().texOffs(2, 74).addBox(7.15F, -5.5F, 0.2F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6569F, 2.8826F, 5.3742F, 1.7442F, -0.0538F, -0.1899F));

		PartDefinition tentacle1_top3 = tentacle3.addOrReplaceChild("tentacle1_top3", CubeListBuilder.create().texOffs(1, 73).addBox(0.0F, -10.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3431F, 3.3826F, 1.7742F, 0.4282F, 0.0319F, -0.1768F));

		PartDefinition tentacle1_top4 = tentacle3.addOrReplaceChild("tentacle1_top4", CubeListBuilder.create().texOffs(1, 73).addBox(0.0F, -9.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3431F, -4.6175F, -3.2258F, -0.6417F, 0.1425F, 0.0615F));

		PartDefinition tentacle1_top5 = tentacle3.addOrReplaceChild("tentacle1_top5", CubeListBuilder.create().texOffs(1, 73).addBox(0.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3431F, -11.6175F, 1.7742F, -1.2962F, 0.1425F, 0.0615F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof LivingEntity living) {
			if (!this.animationState.isStarted()) {
				this.animationState.start((int) ageInTicks);
			}
			this.animate(this.animationState, DoorHighMythicalCreatureAnimations.IDLE, ageInTicks, 1.0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		doors.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		tentacles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}