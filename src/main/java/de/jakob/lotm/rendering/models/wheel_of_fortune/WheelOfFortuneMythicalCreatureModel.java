package de.jakob.lotm.rendering.models.wheel_of_fortune;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.visionary.VisionaryMythicalCreatureAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

public class WheelOfFortuneMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "wof_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart serpent_of_merkury;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart segment1;
	private final ModelPart body2;
	private final ModelPart segment2;
	private final ModelPart body3;
	private final ModelPart segment3;
	private final ModelPart body4;
	private final ModelPart segment4;
	private final ModelPart body5;
	private final ModelPart segment5;
	private final ModelPart body6;
	private final ModelPart segment6;
	private final ModelPart body7;
	private final ModelPart segment7;
	private final ModelPart body8;
	private final ModelPart segment8;
	private final ModelPart body9;
	private final ModelPart segment9;
	private final ModelPart body10;
	private final ModelPart segment10;
	private final ModelPart body11;
	private final ModelPart segment12;
	private final ModelPart segment11;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();

	public WheelOfFortuneMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.serpent_of_merkury = root.getChild("serpent_of_merkury");
		this.head = this.serpent_of_merkury.getChild("head");
		this.body = this.serpent_of_merkury.getChild("body");
		this.segment1 = this.body.getChild("segment1");
		this.body2 = this.body.getChild("body2");
		this.segment2 = this.body2.getChild("segment2");
		this.body3 = this.body2.getChild("body3");
		this.segment3 = this.body3.getChild("segment3");
		this.body4 = this.body3.getChild("body4");
		this.segment4 = this.body4.getChild("segment4");
		this.body5 = this.body4.getChild("body5");
		this.segment5 = this.body5.getChild("segment5");
		this.body6 = this.body5.getChild("body6");
		this.segment6 = this.body6.getChild("segment6");
		this.body7 = this.body6.getChild("body7");
		this.segment7 = this.body7.getChild("segment7");
		this.body8 = this.body7.getChild("body8");
		this.segment8 = this.body8.getChild("segment8");
		this.body9 = this.body8.getChild("body9");
		this.segment9 = this.body9.getChild("segment9");
		this.body10 = this.body9.getChild("body10");
		this.segment10 = this.body10.getChild("segment10");
		this.body11 = this.body10.getChild("body11");
		this.segment12 = this.body11.getChild("segment12");
		this.segment11 = this.body11.getChild("segment11");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition serpent_of_merkury = partdefinition.addOrReplaceChild("serpent_of_merkury", CubeListBuilder.create(), PartPose.offset(0.7882F, 6.0F, 5.7724F));

		PartDefinition head = serpent_of_merkury.addOrReplaceChild("head", CubeListBuilder.create().texOffs(-2, -1).addBox(-3.9112F, -2.6183F, -8.1874F, 7.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0199F, 1.3532F, -10.2276F, 0.1427F, 0.0475F, -0.009F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(-2, 10).addBox(-2.5F, -5.0F, -11.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0888F, 5.3817F, 1.8126F, -0.0436F, 0.0F, 0.0F));

		PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(34, 13).addBox(-3.5F, -7.0F, -11.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0888F, 5.3817F, 1.8126F, 0.0436F, 0.0F, 0.0F));

		PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(35, 13).addBox(-2.5F, -7.0F, -11.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0888F, 5.3855F, 0.7255F, 0.0873F, 0.0F, 0.0F));

		PartDefinition body = serpent_of_merkury.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.1801F, 1.7468F, -10.7724F, 0.0F, 0.0F, 0.0436F));

		PartDefinition segment1 = body.addOrReplaceChild("segment1", CubeListBuilder.create().texOffs(25, 0).addBox(-2.852F, -2.5394F, -0.4697F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6003F, -0.292F, 0.9645F, 0.2678F, 0.4249F, -0.0908F));

		PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(4.734F, -4.0376F, 11.3572F));

		PartDefinition segment2 = body2.addOrReplaceChild("segment2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1623F, 0.5718F, -0.0791F));

		PartDefinition body_r1 = segment2.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(23, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9311F, -0.4539F, 5.7382F, 0.1585F, -0.0735F, 0.4305F));

		PartDefinition body3 = body2.addOrReplaceChild("body3", CubeListBuilder.create(), PartPose.offset(7.3708F, -1.6377F, 9.2645F));

		PartDefinition segment3 = body3.addOrReplaceChild("segment3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5881F, 0.6651F, 0.2155F));

		PartDefinition body_r2 = segment3.addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(26, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0505F, -0.9166F, 6.7028F, 0.0172F, -0.1298F, 1.4388F));

		PartDefinition body4 = body3.addOrReplaceChild("body4", CubeListBuilder.create(), PartPose.offset(5.962F, 5.7064F, 9.0059F));

		PartDefinition segment4 = body4.addOrReplaceChild("segment4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4383F, 0.684F, 1.5281F));

		PartDefinition body_r3 = segment4.addOrReplaceChild("body_r3", CubeListBuilder.create().texOffs(25, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3661F, -0.0363F, 6.623F, 0.0F, 0.0F, 2.3998F));

		PartDefinition body5 = body4.addOrReplaceChild("body5", CubeListBuilder.create(), PartPose.offset(-4.8319F, 8.5912F, 8.5748F));

		PartDefinition segment5 = body5.addOrReplaceChild("segment5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1795F, 0.7736F, -2.8933F));

		PartDefinition body_r4 = segment5.addOrReplaceChild("body_r4", CubeListBuilder.create().texOffs(25, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1091F, -0.1843F, 6.2221F, 0.0F, 0.0F, 2.8362F));

		PartDefinition body6 = body5.addOrReplaceChild("body6", CubeListBuilder.create(), PartPose.offset(-8.8675F, 1.3911F, 8.9853F));

		PartDefinition segment6 = body6.addOrReplaceChild("segment6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8371F, 0.5594F, -1.2603F));

		PartDefinition body_r5 = segment6.addOrReplaceChild("body_r5", CubeListBuilder.create().texOffs(28, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7318F, 0.09F, 6.6352F, 0.0F, 0.0F, 1.6581F));

		PartDefinition body7 = body6.addOrReplaceChild("body7", CubeListBuilder.create(), PartPose.offset(-7.0492F, -7.6356F, 8.1844F));

		PartDefinition segment7 = body7.addOrReplaceChild("segment7", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2974F, 0.7817F, -1.7836F));

		PartDefinition body_r6 = segment7.addOrReplaceChild("body_r6", CubeListBuilder.create().texOffs(26, 0).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1837F, -0.7196F, 6.3041F, 0.0F, 0.0F, 1.309F));

		PartDefinition body8 = body7.addOrReplaceChild("body8", CubeListBuilder.create(), PartPose.offset(-6.2617F, -7.5232F, 8.2839F));

		PartDefinition segment8 = body8.addOrReplaceChild("segment8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2823F, 0.339F, -2.3563F));

		PartDefinition body_r7 = segment8.addOrReplaceChild("body_r7", CubeListBuilder.create().texOffs(28, 1).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5176F, 0.1705F, 5.9777F, 0.0F, 0.0F, 0.5672F));

		PartDefinition body9 = body8.addOrReplaceChild("body9", CubeListBuilder.create(), PartPose.offset(-5.6649F, -1.2181F, 9.7785F));

		PartDefinition segment9 = body9.addOrReplaceChild("segment9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6144F, -0.1461F, -2.0166F));

		PartDefinition body_r8 = segment9.addOrReplaceChild("body_r8", CubeListBuilder.create().texOffs(25, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0268F, 0.2499F, 6.3958F, 0.0F, 0.0F, -0.829F));

		PartDefinition body10 = body9.addOrReplaceChild("body10", CubeListBuilder.create(), PartPose.offset(-6.6817F, 4.0763F, 10.5346F));

		PartDefinition segment10 = body10.addOrReplaceChild("segment10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4007F, -0.3296F, -2.7153F));

		PartDefinition body_r9 = segment10.addOrReplaceChild("body_r9", CubeListBuilder.create().texOffs(24, 2).addBox(-2.5F, -2.5F, -6.5F, 5.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1142F, -1.6038F, 5.9114F, -0.0465F, 0.2577F, -1.7514F));

		PartDefinition body11 = body10.addOrReplaceChild("body11", CubeListBuilder.create(), PartPose.offset(-0.7646F, 8.8589F, 10.336F));

		PartDefinition segment12 = body11.addOrReplaceChild("segment12", CubeListBuilder.create().texOffs(41, 8).addBox(-0.251F, -1.1016F, -0.7715F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 1.8F, 6.1F, 0.3456F, -0.9041F, 2.266F));

		PartDefinition segment11 = body11.addOrReplaceChild("segment11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.4404F, -0.0298F, -0.3571F, 0.3598F, -0.6996F, 3.0667F));

		PartDefinition body_r10 = segment11.addOrReplaceChild("body_r10", CubeListBuilder.create().texOffs(37, 7).addBox(-1.5F, -2.0F, -4.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3978F, 0.1476F, 4.0988F, 0.0F, 0.0F, -2.8362F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		boolean isWalking = limbSwingAmount > 0.01F;

		if (isWalking) {
			this.idleAnimationState.stop();
			this.walkAnimationState.startIfStopped((int) ageInTicks);
			this.animate(this.walkAnimationState, WheelOfFortuneMythicalCreatureAnimations.walk, ageInTicks, 1.0F);
		} else {
			this.walkAnimationState.stop();
			this.idleAnimationState.startIfStopped((int) ageInTicks);
			this.animate(this.idleAnimationState, WheelOfFortuneMythicalCreatureAnimations.idle, ageInTicks, 1.0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}