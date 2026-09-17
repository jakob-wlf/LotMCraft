package de.jakob.lotm.entity.client.murloc;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.abscessed_hand.AbscessedHandAnimations;
import de.jakob.lotm.entity.custom.MurlocEntity;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class MurlocModel<T extends MurlocEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "murloc"), "main");
	private final ModelPart root;
	private final ModelPart murlock;
	private final ModelPart tail;
	private final ModelPart fins;
	private final ModelPart waist;
	private final ModelPart torso;
	private final ModelPart mouth;
	private final ModelPart leg_back_left;
	private final ModelPart foot4;
	private final ModelPart leg_back_right;
	private final ModelPart foot3;
	private final ModelPart leg_front_right;
	private final ModelPart foot2;
	private final ModelPart leg_front_left;
	private final ModelPart foot;

	public MurlocModel(ModelPart root) {
		this.root = root;
		this.murlock = root.getChild("murlock");
		this.tail = this.murlock.getChild("tail");
		this.fins = this.tail.getChild("fins");
		this.waist = this.murlock.getChild("waist");
		this.torso = this.waist.getChild("torso");
		this.mouth = this.torso.getChild("mouth");
		this.leg_back_left = this.waist.getChild("leg_back_left");
		this.foot4 = this.leg_back_left.getChild("foot4");
		this.leg_back_right = this.waist.getChild("leg_back_right");
		this.foot3 = this.leg_back_right.getChild("foot3");
		this.leg_front_right = this.waist.getChild("leg_front_right");
		this.foot2 = this.leg_front_right.getChild("foot2");
		this.leg_front_left = this.waist.getChild("leg_front_left");
		this.foot = this.leg_front_left.getChild("foot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition murlock = partdefinition.addOrReplaceChild("murlock", CubeListBuilder.create(), PartPose.offset(-0.1667F, 14.8667F, -2.4F));

		PartDefinition tail = murlock.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8333F, 0.1333F, 8.4F));

		PartDefinition fins = tail.addOrReplaceChild("fins", CubeListBuilder.create().texOffs(44, 46).addBox(1.0F, 0.0F, 0.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(36, 46).addBox(1.0F, -4.0F, 0.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 4.0F));

		PartDefinition waist = murlock.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(-1.8333F, 0.1333F, 8.4F));

		PartDefinition torso = waist.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(0, 29).addBox(-1.0F, 4.0F, -1.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(13, 41).addBox(-1.0F, 4.0F, -8.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(0, 3).addBox(-1.0F, 0.0F, -8.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(18, 27).addBox(-1.0F, 0.0F, -12.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(29, 38).addBox(-1.0F, 0.0F, -15.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 55).addBox(1.0F, -3.0F, -9.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 55).addBox(1.0F, -3.0F, -1.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -6.0F));

		PartDefinition mouth = torso.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, -2.0F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(41, 44).addBox(-3.0F, -2.0F, -7.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 6.0F, -8.0F));

		PartDefinition leg_back_left = waist.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(18, 53).addBox(-3.0F, -2.3F, -1.9F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 2.3F, -4.1F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r1 = leg_back_left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(44, 57).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.3F, -0.9F, -0.1309F, 0.0F, 0.0F));

		PartDefinition foot4 = leg_back_left.addOrReplaceChild("foot4", CubeListBuilder.create(), PartPose.offset(-1.5F, 5.0F, -1.0F));

		PartDefinition cube_r2 = foot4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(18, 62).mirror().addBox(-2.0F, 1.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(31, 60).mirror().addBox(-3.0F, 1.0F, 1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, -1.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r3 = foot4.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 0.4872F, -0.9958F, -0.1431F, 0.4151F, -0.058F));

		PartDefinition cube_r4 = foot4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.4872F, -1.1958F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r5 = foot4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, 0.4872F, -0.8958F, -0.154F, -0.5532F, 0.0814F));

		PartDefinition cube_r6 = foot4.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(22, 62).mirror().addBox(-2.5F, 0.6305F, -1.9914F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.1F, -0.0128F, -0.7958F, -0.1311F, -0.0519F, 0.0068F));

		PartDefinition leg_back_right = waist.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(18, 53).mirror().addBox(-3.0F, -2.3F, -1.9F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 2.3F, -4.1F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r7 = leg_back_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(44, 57).mirror().addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 3.3F, -0.9F, -0.1309F, 0.0F, 0.0F));

		PartDefinition foot3 = leg_back_right.addOrReplaceChild("foot3", CubeListBuilder.create(), PartPose.offset(-1.5F, 5.0F, -1.0F));

		PartDefinition cube_r8 = foot3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(18, 62).addBox(-2.0F, 1.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(31, 60).addBox(-3.0F, 1.0F, 1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -1.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r9 = foot3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.4872F, -0.9958F, -0.1431F, 0.4151F, -0.058F));

		PartDefinition cube_r10 = foot3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4872F, -1.1958F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r11 = foot3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.4872F, -0.8958F, -0.154F, -0.5532F, 0.0814F));

		PartDefinition cube_r12 = foot3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(22, 62).addBox(-2.5F, 0.6305F, -1.9914F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -0.0128F, -0.7958F, -0.1311F, -0.0519F, 0.0068F));

		PartDefinition leg_front_right = waist.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(18, 53).mirror().addBox(-3.0F, -2.3F, -1.9F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 2.3F, -12.1F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r13 = leg_front_right.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(44, 57).mirror().addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 3.3F, -0.9F, -0.1309F, 0.0F, 0.0F));

		PartDefinition foot2 = leg_front_right.addOrReplaceChild("foot2", CubeListBuilder.create(), PartPose.offset(-1.5F, 5.0F, -1.0F));

		PartDefinition cube_r14 = foot2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(18, 62).addBox(-2.0F, 1.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(31, 60).addBox(-3.0F, 1.0F, 1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -1.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r15 = foot2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.4872F, -0.9958F, -0.1431F, 0.4151F, -0.058F));

		PartDefinition cube_r16 = foot2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4872F, -1.1958F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r17 = foot2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(12, 61).addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.4872F, -0.8958F, -0.154F, -0.5532F, 0.0814F));

		PartDefinition cube_r18 = foot2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(22, 62).addBox(-2.5F, 0.6305F, -1.9914F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1F, -0.0128F, -0.7958F, -0.1311F, -0.0519F, 0.0068F));

		PartDefinition leg_front_left = waist.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(18, 53).addBox(-3.0F, -2.3F, -1.9F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 2.3F, -12.1F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r19 = leg_front_left.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(44, 57).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 3.3F, 0.1F, -0.1309F, 0.0F, 0.0F));

		PartDefinition foot = leg_front_left.addOrReplaceChild("foot", CubeListBuilder.create(), PartPose.offset(-1.4F, 5.0F, 0.0F));

		PartDefinition cube_r20 = foot.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(18, 62).mirror().addBox(-2.0F, 1.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(31, 60).mirror().addBox(-3.0F, 1.0F, 1.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.4F, -1.0F, -2.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r21 = foot.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.1F, 0.4872F, -0.9958F, -0.1431F, 0.4151F, -0.058F));

		PartDefinition cube_r22 = foot.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.1F, 0.4872F, -1.1958F, -0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r23 = foot.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(12, 61).mirror().addBox(-0.5F, -0.3695F, -1.9914F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.9F, 0.4872F, -0.8958F, -0.154F, -0.5532F, 0.0814F));

		PartDefinition cube_r24 = foot.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(22, 62).mirror().addBox(-2.5F, 0.6305F, -1.9914F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -0.0128F, -0.7958F, -0.1311F, -0.0519F, 0.0068F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private float walkBlend = 0.0F;
	private static final float BLEND_SPEED = 0.1F;

	// Pre-allocate to avoid per-frame garbage
	private float[] idlePose;
	private float[] walkPose;
	private static final int FLOATS_PER_PART = 6; // x, y, z, xRot, yRot, zRot

	private void capturePoseInto(float[] buffer) {
		int i = 0;
		for (var part : (Iterable<ModelPart>) this.root().getAllParts()::iterator) {
			buffer[i++] = part.x;    buffer[i++] = part.y;    buffer[i++] = part.z;
			buffer[i++] = part.xRot; buffer[i++] = part.yRot; buffer[i++] = part.zRot;
		}
	}

	private void applyBlendedPose(float[] from, float[] to, float blend) {
		int i = 0;
		for (var part : (Iterable<ModelPart>) this.root().getAllParts()::iterator) {
			part.x    = Mth.lerp(blend, from[i], to[i]); i++;
			part.y    = Mth.lerp(blend, from[i], to[i]); i++;
			part.z    = Mth.lerp(blend, from[i], to[i]); i++;
			part.xRot = Mth.lerp(blend, from[i], to[i]); i++;
			part.yRot = Mth.lerp(blend, from[i], to[i]); i++;
			part.zRot = Mth.lerp(blend, from[i], to[i]); i++;
		}
	}

	@Override
	public void setupAnim(MurlocEntity entity, float limbSwing, float limbSwingAmount,
	                      float ageInTicks, float netHeadYaw, float headPitch) {

		// Lazy-init buffers once we know part count
		if (this.idlePose == null) {
			int partCount = (int) this.root().getAllParts().count();
			this.idlePose = new float[partCount * FLOATS_PER_PART];
			this.walkPose = new float[partCount * FLOATS_PER_PART];
		}

		boolean isWalking = limbSwingAmount > 0.01F;
		this.walkBlend = Mth.lerp(BLEND_SPEED, this.walkBlend, isWalking ? 1.0F : 0.0F);

		// Keep both states running so their internal timers don't reset
		if (!entity.IDLE_ANIMATION.isStarted()) entity.IDLE_ANIMATION.start((int) ageInTicks);
		if (!entity.SWIM_ANIMATION.isStarted()) entity.SWIM_ANIMATION.start((int) ageInTicks);

		// Sample idle into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.IDLE_ANIMATION, MurlocAnimations.idle, ageInTicks, 1.0F);
		capturePoseInto(this.idlePose);

		// Sample walk into snapshot
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.SWIM_ANIMATION, MurlocAnimations.swim, ageInTicks, 1.0F);
		capturePoseInto(this.walkPose);

		// Write the lerped result
		applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		murlock.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}