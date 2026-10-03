package de.jakob.lotm.entity.client.spirits.headless_bride;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.stone_golem.StoneGolemAnimations;
import de.jakob.lotm.entity.custom.StoneGolemEntity;
import de.jakob.lotm.entity.custom.spirits.HeadlessBrideEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HeadlessBrideModel<T extends HeadlessBrideEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "headless_bride"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart dress;
	private final ModelPart jacket;
	private final ModelPart jacket3;
	private final ModelPart jacket2;
	private final ModelPart jacket4;
	private final ModelPart arm_right;
	private final ModelPart arm_left;
	private final ModelPart body;
	private final ModelPart body2;
	private final ModelPart body3;

	public HeadlessBrideModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.dress = this.All.getChild("dress");
		this.jacket = this.dress.getChild("jacket");
		this.jacket3 = this.dress.getChild("jacket3");
		this.jacket2 = this.dress.getChild("jacket2");
		this.jacket4 = this.dress.getChild("jacket4");
		this.arm_right = this.All.getChild("arm_right");
		this.arm_left = this.All.getChild("arm_left");
		this.body = this.All.getChild("body");
		this.body2 = this.body.getChild("body2");
		this.body3 = this.body.getChild("body3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 12.2F, 0.0F));

		PartDefinition dress = All.addOrReplaceChild("dress", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition jacket_r1 = dress.addOrReplaceChild("jacket_r1", CubeListBuilder.create().texOffs(54, 8).addBox(-2.0F, -0.2F, -0.5F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.3F, 0.0F, 0.0F, -1.5708F, -0.1745F));

		PartDefinition jacket_r2 = dress.addOrReplaceChild("jacket_r2", CubeListBuilder.create().texOffs(54, 8).mirror().addBox(-2.0F, -0.05F, -0.4F, 4.0F, 5.75F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.4F, -0.2F, 0.0F, 0.0F, -1.5708F, 0.1571F));

		PartDefinition jacket_r3 = dress.addOrReplaceChild("jacket_r3", CubeListBuilder.create().texOffs(46, 0).addBox(-4.0F, 0.05F, -0.65F, 8.0F, 5.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, 1.9F, 0.2094F, 0.0F, 0.0F));

		PartDefinition jacket_r4 = dress.addOrReplaceChild("jacket_r4", CubeListBuilder.create().texOffs(46, 0).mirror().addBox(-4.0F, 0.05F, -0.25F, 8.0F, 5.75F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.3F, -2.0F, -0.1222F, 0.0F, 0.0F));

		PartDefinition jacket = dress.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.offset(0.0F, 5.5F, -2.8F));

		PartDefinition jacket_r5 = jacket.addOrReplaceChild("jacket_r5", CubeListBuilder.create().texOffs(35, 9).addBox(-4.0F, 0.0F, -0.25F, 8.0F, 4.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition jacket3 = dress.addOrReplaceChild("jacket3", CubeListBuilder.create(), PartPose.offset(0.0F, 5.4F, 3.3F));

		PartDefinition jacket_r6 = jacket3.addOrReplaceChild("jacket_r6", CubeListBuilder.create().texOffs(35, 9).addBox(-4.0F, 0.0F, -0.65F, 8.0F, 4.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition jacket2 = dress.addOrReplaceChild("jacket2", CubeListBuilder.create(), PartPose.offset(-5.3F, 5.3F, 0.0F));

		PartDefinition jacket_r7 = jacket2.addOrReplaceChild("jacket_r7", CubeListBuilder.create().texOffs(54, 17).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 4.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.3927F));

		PartDefinition jacket4 = dress.addOrReplaceChild("jacket4", CubeListBuilder.create(), PartPose.offset(5.5F, 5.5F, 0.0F));

		PartDefinition jacket_r8 = jacket4.addOrReplaceChild("jacket_r8", CubeListBuilder.create().texOffs(54, 17).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 4.75F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, -0.48F));

		PartDefinition arm_right = All.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(25, 41).mirror().addBox(-3.0F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(25, 24).addBox(-3.0F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-4.0F, -11.2F, 0.0F, -0.9599F, 0.0F, 0.0F));

		PartDefinition arm_left = All.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(25, 41).addBox(0.0F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(25, 24).mirror().addBox(0.0F, -1.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(4.0F, -11.2F, 0.0F, -0.9599F, 0.0F, 0.0F));

		PartDefinition body = All.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 41).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(40, 24).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -0.2F, 0.0F));

		PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(43, 17).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, -12.0F, 0.0F));

		PartDefinition body3 = body.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(43, 17).mirror().addBox(-1.5F, -2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.1F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(HeadlessBrideEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity.isCursing()) {
			entity.ATTACK_ANIMATION.stop();
			entity.WALK_ANIMATION.stop();
			entity.IDLE_ANIMATION.stop();
			if (!entity.CURSE_ANIMATION.isStarted()) {
				entity.CURSE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.CURSE_ANIMATION, HeadlessBrideAnimations.Use_beyonder_power, ageInTicks, 1.0F);
			return;
		}

		if (entity.isMeleeAttacking()) {
			entity.CURSE_ANIMATION.stop();
			entity.WALK_ANIMATION.stop();
			entity.IDLE_ANIMATION.stop();
			if (!entity.ATTACK_ANIMATION.isStarted()) {
				entity.ATTACK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.ATTACK_ANIMATION, HeadlessBrideAnimations.attack, ageInTicks, 1.0F);
			return;
		}

		boolean isWalking = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && entity.onGround();

		if (isWalking) {
			entity.IDLE_ANIMATION.stop();
			if (!entity.WALK_ANIMATION.isStarted()) {
				entity.WALK_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.WALK_ANIMATION, HeadlessBrideAnimations.walk, ageInTicks, 1.0F);
		} else {
			entity.WALK_ANIMATION.stop();
			if (!entity.IDLE_ANIMATION.isStarted()) {
				entity.IDLE_ANIMATION.start((int) ageInTicks);
			}
			this.animate(entity.IDLE_ANIMATION, HeadlessBrideAnimations.Idle, ageInTicks, 1.0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		All.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}