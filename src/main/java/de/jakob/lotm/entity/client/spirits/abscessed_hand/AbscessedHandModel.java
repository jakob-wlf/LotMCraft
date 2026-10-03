package de.jakob.lotm.entity.client.spirits.abscessed_hand;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.spirits.AbscessedHandEntity;
import de.jakob.lotm.rendering.models.fool.FoolMythicalCreatureAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AbscessedHandModel<T extends AbscessedHandEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "abscessed_hand"), "main");
	private final ModelPart root;
	private final ModelPart Hand;
	private final ModelPart Thumb;
	private final ModelPart Thumb_front;
	private final ModelPart Fingers;
	private final ModelPart Middle_finger;
	private final ModelPart bone3;
	private final ModelPart small_finger;
	private final ModelPart bone;
	private final ModelPart Heartfinger;
	private final ModelPart bone2;
	private final ModelPart Pointing_finger;
	private final ModelPart bone5;
	private final ModelPart abscess3;
	private final ModelPart Wrist;
	private final ModelPart Meat;
	private final ModelPart abscess;
	private final ModelPart abscess2;

	public AbscessedHandModel(ModelPart root) {
		this.root = root;
		this.Hand = root.getChild("Hand");
		this.Thumb = this.Hand.getChild("Thumb");
		this.Thumb_front = this.Thumb.getChild("Thumb_front");
		this.Fingers = this.Hand.getChild("Fingers");
		this.Middle_finger = this.Fingers.getChild("Middle_finger");
		this.bone3 = this.Middle_finger.getChild("bone3");
		this.small_finger = this.Fingers.getChild("small_finger");
		this.bone = this.small_finger.getChild("bone");
		this.Heartfinger = this.Fingers.getChild("Heartfinger");
		this.bone2 = this.Heartfinger.getChild("bone2");
		this.Pointing_finger = this.Fingers.getChild("Pointing_finger");
		this.bone5 = this.Pointing_finger.getChild("bone5");
		this.abscess3 = this.Pointing_finger.getChild("abscess3");
		this.Wrist = this.Hand.getChild("Wrist");
		this.Meat = this.Hand.getChild("Meat");
		this.abscess = this.Meat.getChild("abscess");
		this.abscess2 = this.Meat.getChild("abscess2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Hand = partdefinition.addOrReplaceChild("Hand", CubeListBuilder.create(), PartPose.offsetAndRotation(0.58F, 22.0712F, -0.0295F, 0.2793F, 0.0F, 0.0F));

		PartDefinition Thumb = Hand.addOrReplaceChild("Thumb", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3127F, 0.4117F, -0.5902F, 0.2194F, 0.4034F, -0.5103F));

		PartDefinition cube_r1 = Thumb.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 3).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -0.6F, 0.5F, 1.3196F, 0.7262F, -0.1508F));

		PartDefinition Thumb_front = Thumb.addOrReplaceChild("Thumb_front", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.9F, -0.5F, -1.0F, -0.5685F, 0.2388F, 0.3087F));

		PartDefinition cube_r2 = Thumb_front.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(8, 1).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -1.4F, 0.0F, 1.7059F, 0.6564F, -0.1471F));

		PartDefinition cube_r3 = Thumb_front.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -0.4F, 0.2F, 1.7059F, 0.6564F, -0.1471F));

		PartDefinition Fingers = Hand.addOrReplaceChild("Fingers", CubeListBuilder.create(), PartPose.offset(0.4873F, -0.4884F, -2.0902F));

		PartDefinition Middle_finger = Fingers.addOrReplaceChild("Middle_finger", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.8F, -0.5F, 0.0F, 0.8976F, 0.0483F, 0.1321F));

		PartDefinition cube_r4 = Middle_finger.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.4F, 0.0F, 1.2344F, -0.2136F, 0.0704F));

		PartDefinition bone3 = Middle_finger.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5338F, -0.5406F, -1.8072F, -0.7156F, -0.0698F, 0.0F));

		PartDefinition cube_r5 = bone3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 1).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -1.5F, -0.5F, 1.4431F, -0.1657F, 0.1031F));

		PartDefinition cube_r6 = bone3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(16, 2).addBox(-0.6F, -1.7F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0004F, 0.1164F, -0.0697F, 1.4381F, -0.15F, 0.0969F));

		PartDefinition small_finger = Fingers.addOrReplaceChild("small_finger", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -0.3F, 0.3F, 1.1084F, -0.0945F, -0.1872F));

		PartDefinition cube_r7 = small_finger.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -0.5F, 0.1F, 1.2274F, -0.2019F, 0.104F));

		PartDefinition bone = small_finger.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -0.6F, -1.6F, -1.0647F, 0.0F, 0.0F));

		PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 2).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, 0.0F, 1.4369F, -0.2019F, 0.104F));

		PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(8, 1).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -1.6F, -0.7F, 1.4431F, -0.1657F, 0.1031F));

		PartDefinition Heartfinger = Fingers.addOrReplaceChild("Heartfinger", CubeListBuilder.create(), PartPose.offsetAndRotation(0.4F, -0.1F, -0.2F, 0.9425F, 0.0F, 0.0F));

		PartDefinition cube_r10 = Heartfinger.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -0.3F, 0.7F, 1.2289F, -0.1704F, -0.0143F));

		PartDefinition bone2 = Heartfinger.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.4F, -0.4F, -1.2F, -0.6632F, 0.0F, 0.0F));

		PartDefinition cube_r11 = bone2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(8, 1).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -1.6F, -0.3F, 1.4438F, -0.2822F, 0.0057F));

		PartDefinition cube_r12 = bone2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(16, 2).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -0.4F, 0.4F, 1.4043F, -0.2665F, -0.0003F));

		PartDefinition Pointing_finger = Fingers.addOrReplaceChild("Pointing_finger", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0172F, -0.5373F, -0.2833F, 0.9611F, 0.1627F, 0.2279F));

		PartDefinition cube_r13 = Pointing_finger.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4828F, -0.1627F, 0.0833F, 1.2274F, -0.2019F, 0.104F));

		PartDefinition bone5 = Pointing_finger.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.481F, -0.1678F, -1.7584F, -0.7156F, 0.0F, 0.0F));

		PartDefinition cube_r14 = bone5.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(8, 1).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, -1.6F, -0.3F, 1.4431F, -0.1657F, 0.1031F));

		PartDefinition cube_r15 = bone5.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(16, 2).addBox(0.0F, -1.5F, -0.6F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5247F, -0.1607F, -0.1378F, 1.4369F, -0.2019F, 0.104F));

		PartDefinition abscess3 = Pointing_finger.addOrReplaceChild("abscess3", CubeListBuilder.create(), PartPose.offset(0.1F, 0.0F, -0.6F));

		PartDefinition cube_r16 = abscess3.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(11, 1).addBox(-0.6F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1988F, 0.0F, 0.0156F, 1.0982F, -0.1726F, 0.008F));

		PartDefinition Wrist = Hand.addOrReplaceChild("Wrist", CubeListBuilder.create(), PartPose.offset(0.1554F, 0.5024F, 2.554F));

		PartDefinition cube_r17 = Wrist.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(14, 13).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1207F, -0.2361F, -0.2379F, 2.2602F, -0.1571F, 0.0F));

		PartDefinition cube_r18 = Wrist.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(5, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5426F, -0.2599F, -0.3728F, 2.2602F, -0.1571F, 0.0F));

		PartDefinition Meat = Hand.addOrReplaceChild("Meat", CubeListBuilder.create(), PartPose.offset(1.67F, -0.4257F, 0.1265F));

		PartDefinition cube_r19 = Meat.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(20, 3).addBox(1.0861F, -0.5448F, -0.4147F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.002F, -0.2648F, -2.152F, 0.8695F, -0.2094F, 0.0607F));

		PartDefinition cube_r20 = Meat.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(20, 3).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2828F, -0.4627F, -1.5167F, 0.8695F, -0.2094F, 0.0607F));

		PartDefinition cube_r21 = Meat.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(20, 3).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5828F, -0.4627F, -1.7167F, 0.8695F, -0.2094F, 0.0607F));

		PartDefinition cube_r22 = Meat.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(20, 3).addBox(-2.0861F, -0.4552F, -0.5853F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.702F, -0.2648F, -2.052F, 0.8695F, -0.2094F, 0.0607F));

		PartDefinition cube_r23 = Meat.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(16, 9).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5828F, -0.2627F, 2.6833F, 1.501F, -0.1571F, 0.0F));

		PartDefinition cube_r24 = Meat.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(12, 5).addBox(-4.0F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0828F, 0.5373F, -0.2167F, 1.501F, -0.1571F, 0.0F));

		PartDefinition cube_r25 = Meat.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 9).addBox(-4.0F, -5.0F, -1.0F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5828F, 1.1373F, 2.7833F, 1.501F, -0.1571F, 0.0F));

		PartDefinition abscess = Meat.addOrReplaceChild("abscess", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r26 = abscess.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(11, 1).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.501F, -0.1571F, 0.0F));

		PartDefinition abscess2 = Meat.addOrReplaceChild("abscess2", CubeListBuilder.create(), PartPose.offset(-3.1F, 0.0F, 1.3F));

		PartDefinition cube_r27 = abscess2.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(11, 1).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1F, 0.0F, 1.501F, -0.1571F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
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
	public void setupAnim(AbscessedHandEntity entity, float limbSwing, float limbSwingAmount,
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
        if (!entity.WALK_ANIMATION.isStarted()) entity.WALK_ANIMATION.start((int) ageInTicks);

        // Sample idle into snapshot
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.IDLE_ANIMATION, AbscessedHandAnimations.Idle, ageInTicks, 1.0F);
        capturePoseInto(this.idlePose);

        // Sample walk into snapshot
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.WALK_ANIMATION, AbscessedHandAnimations.Walk, ageInTicks, 1.0F);
        capturePoseInto(this.walkPose);

        // Write the lerped result
        applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
    }

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Hand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}