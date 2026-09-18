package de.jakob.lotm.rendering.models.death;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.models.darkness.DarknessMythicalCreatureAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DeathMythicalCreatureModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "death_mythical_creature"), "main");
	private final ModelPart root;
	private final ModelPart Whole_model;
	private final ModelPart Wings;
	private final ModelPart Right_Wing;
	private final ModelPart Feather_Right_Wing;
	private final ModelPart Left_Wing;
	private final ModelPart Feathers_Left_Wing;
	private final ModelPart Body;
	private final ModelPart Body_Part1;
	private final ModelPart Body_Part2;
	private final ModelPart bone4;
	private final ModelPart bone3;
	private final ModelPart Body_Part3;
	private final ModelPart bone2;
	private final ModelPart bone;
	private final ModelPart Body_Part4;
	private final ModelPart bone6;
	private final ModelPart bone5;
	private final ModelPart Body_Neck;
	private final ModelPart Body_Wings;
	private final ModelPart Skull;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart Fire;
	private final ModelPart Eye_Fire;

	private AnimationState idleAnimationState = new AnimationState();
	private AnimationState walkAnimationState = new AnimationState();


	public DeathMythicalCreatureModel(ModelPart root) {
		this.root = root;
		this.Whole_model = root.getChild("Whole_model");
		this.Wings = this.Whole_model.getChild("Wings");
		this.Right_Wing = this.Wings.getChild("Right_Wing");
		this.Feather_Right_Wing = this.Right_Wing.getChild("Feather_Right_Wing");
		this.Left_Wing = this.Wings.getChild("Left_Wing");
		this.Feathers_Left_Wing = this.Left_Wing.getChild("Feathers_Left_Wing");
		this.Body = this.Whole_model.getChild("Body");
		this.Body_Part1 = this.Body.getChild("Body_Part1");
		this.Body_Part2 = this.Body.getChild("Body_Part2");
		this.bone4 = this.Body_Part2.getChild("bone4");
		this.bone3 = this.Body_Part2.getChild("bone3");
		this.Body_Part3 = this.Body.getChild("Body_Part3");
		this.bone2 = this.Body_Part3.getChild("bone2");
		this.bone = this.Body_Part3.getChild("bone");
		this.Body_Part4 = this.Body.getChild("Body_Part4");
		this.bone6 = this.Body_Part4.getChild("bone6");
		this.bone5 = this.Body_Part4.getChild("bone5");
		this.Body_Neck = this.Body.getChild("Body_Neck");
		this.Body_Wings = this.Body.getChild("Body_Wings");
		this.Skull = this.Whole_model.getChild("Skull");
		this.bone7 = this.Skull.getChild("bone7");
		this.bone8 = this.Skull.getChild("bone8");
		this.Fire = this.Skull.getChild("Fire");
		this.Eye_Fire = this.Fire.getChild("Eye_Fire");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Whole_model = partdefinition.addOrReplaceChild("Whole_model", CubeListBuilder.create(), PartPose.offset(-2.431F, 15.6305F, -4.556F));

		PartDefinition Wings = Whole_model.addOrReplaceChild("Wings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Right_Wing = Wings.addOrReplaceChild("Right_Wing", CubeListBuilder.create(), PartPose.offset(-1.4F, -1.1F, 0.0F));

		PartDefinition cube_r1 = Right_Wing.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(5, 13).addBox(0.5572F, -0.8238F, -2.0716F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7262F, 1.0933F, 5.1275F, -0.3035F, -0.0558F, 0.2145F));

		PartDefinition cube_r2 = Right_Wing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, -0.0783F, -1.0807F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4041F, -0.0522F, 4.2366F, -0.3471F, -0.0558F, 0.2145F));

		PartDefinition cube_r3 = Right_Wing.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(5, 13).addBox(-3.0F, -0.5F, -2.7F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.169F, 0.4695F, 5.556F, -0.3828F, -0.1658F, 0.1431F));

		PartDefinition cube_r4 = Right_Wing.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(5, 13).addBox(-2.0F, -1.3F, -2.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.169F, 1.4695F, 3.556F, -0.3907F, -0.0558F, 0.2145F));

		PartDefinition cube_r5 = Right_Wing.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(5, 13).addBox(-2.8F, -1.0F, -2.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.169F, 0.4695F, 3.556F, -0.3035F, -0.0558F, 0.2145F));

		PartDefinition cube_r6 = Right_Wing.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(5, 13).addBox(0.5F, -0.3F, -1.7F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.169F, -1.5305F, 2.556F, -0.3471F, -0.0558F, 0.1709F));

		PartDefinition cube_r7 = Right_Wing.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -1.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.169F, -0.5305F, 1.556F, -0.3392F, 0.0088F, 0.1868F));

		PartDefinition cube_r8 = Right_Wing.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(5, 13).addBox(-0.7F, -1.3F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.169F, 0.4695F, 1.556F, -0.1708F, -0.0925F, 0.2345F));

		PartDefinition cube_r9 = Right_Wing.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(5, 13).addBox(-1.0F, -1.0233F, -0.7112F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.1453F, -1.5072F, -0.5328F, -0.2182F, -0.0925F, 0.1909F));

		PartDefinition cube_r10 = Right_Wing.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, 0.1289F, -0.6496F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7282F, -2.1595F, 0.0055F, -0.2581F, -0.0925F, 0.1909F));

		PartDefinition cube_r11 = Right_Wing.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -0.3F, -3.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.169F, -0.5305F, 2.556F, -0.1708F, -0.0925F, 0.1909F));

		PartDefinition cube_r12 = Right_Wing.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8764F, -1.2702F, -1.0275F, -0.6072F, -0.0925F, 0.1909F));

		PartDefinition cube_r13 = Right_Wing.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(5, 13).addBox(-5.0641F, 0.7241F, -3.8611F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.769F, -0.6305F, 2.156F, -0.6072F, -0.0925F, 0.1909F));

		PartDefinition cube_r14 = Right_Wing.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, -0.0669F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.6583F, -2.7636F, -1.2844F, -0.6072F, -0.0925F, 0.1909F));

		PartDefinition cube_r15 = Right_Wing.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(5, 13).addBox(-0.7186F, 0.0294F, -1.3572F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.4504F, -3.2599F, -1.3868F, -0.6508F, -0.0925F, 0.1909F));

		PartDefinition cube_r16 = Right_Wing.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(5, 13).addBox(-3.0F, -1.0F, -1.7F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.169F, -1.5305F, 0.556F, -0.1682F, -0.1292F, 0.1671F));

		PartDefinition Feather_Right_Wing = Right_Wing.addOrReplaceChild("Feather_Right_Wing", CubeListBuilder.create(), PartPose.offset(-10.1502F, -2.4762F, -2.7016F));

		PartDefinition cube_r17 = Feather_Right_Wing.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 14).addBox(6.3F, 4.6F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.0F, 5.3F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.8F, 5.6F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.5F, 4.7F, -0.9F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(6.5F, 4.5F, -0.9F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.7F, 4.1F, -0.9F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.8F, 3.7F, -0.9F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.5F, 3.3F, -0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.2F, 3.2F, -0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(8.0F, 5.2F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(8.0F, 5.2F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.5F, 3.9F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.6F, 3.3F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.7F, 2.7F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(2.5F, 2.0F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.6F, 1.5F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.8F, 1.0F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.0F, 0.6F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, 0.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4287F, -0.4085F, -0.2943F));

		PartDefinition cube_r18 = Feather_Right_Wing.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 14).addBox(2.8F, 1.2F, -0.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.9F, 1.1F, -0.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.9F, 0.9F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.1F, 0.6F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.4F, 0.4F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.3F, 0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.5F, -0.7F, -1.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, -0.4F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.1F, -0.1F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.5F, 0.3F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.3F, 0.6F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.9F, 0.9F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(2.6F, 1.2F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.4F, 1.5F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.4F, 1.5F, -0.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.4F, 1.5F, -0.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5358F, 3.4036F, 6.7323F, -0.3152F, -0.138F, -0.1546F));

		PartDefinition cube_r19 = Feather_Right_Wing.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 14).addBox(1.5F, 1.4F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.8F, 1.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.2F, 0.6F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.5F, 0.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7535F, 2.7445F, 6.5466F, -0.2895F, -0.232F, -0.2559F));

		PartDefinition cube_r20 = Feather_Right_Wing.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 14).addBox(-6.0F, 1.7F, 0.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.0F, 1.4F, 0.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-4.2F, 1.1F, 0.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.3F, 0.9F, 0.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.3F, 0.6F, 0.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.1F, 0.2F, 0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.3F, -0.1F, 0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.1F, -0.5F, 0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.1F, -0.5F, 0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.1F, -0.5F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3072F, 2.1487F, 6.7475F, -0.3405F, 0.0211F, 0.4502F));

		PartDefinition cube_r21 = Feather_Right_Wing.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -0.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.1398F, 1.6531F, 6.3335F, -0.4543F, -0.1127F, 0.2975F));

		PartDefinition cube_r22 = Feather_Right_Wing.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 14).addBox(1.5F, -0.7F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.4F, -0.4F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3236F, 1.0528F, 5.762F, -0.4122F, -0.0557F, 0.3711F));

		PartDefinition cube_r23 = Feather_Right_Wing.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7933F, 0.2282F, 5.2191F, -0.3719F, -0.0142F, 0.3987F));

		PartDefinition cube_r24 = Feather_Right_Wing.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 14).addBox(-11.3F, 2.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-10.1F, 1.8F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-8.9F, 1.5F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-8.9F, 1.5F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-7.8F, 1.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.6F, 0.8F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-6.7F, 1.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5962F, 1.826F, 4.9265F, -0.2921F, 0.0193F, 0.3554F));

		PartDefinition cube_r25 = Feather_Right_Wing.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 14).addBox(-2.2309F, 1.5618F, -2.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.7309F, 1.9618F, -2.7F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.1309F, 2.1618F, -3.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.1309F, 2.1618F, -5.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-4.1309F, 1.9618F, -5.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.4309F, 1.4618F, -5.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8806F, 1.8072F, 8.3265F, -0.2682F, 0.0091F, 0.4015F));

		PartDefinition cube_r26 = Feather_Right_Wing.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(0, 14).addBox(-3.1309F, 1.6618F, -5.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.1309F, 1.6618F, -4.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8806F, 1.8072F, 8.3265F, -0.2936F, -0.0284F, 0.4087F));

		PartDefinition cube_r27 = Feather_Right_Wing.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 1.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7409F, 1.5031F, 7.5464F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r28 = Feather_Right_Wing.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 1.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6013F, 1.1989F, 6.7662F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r29 = Feather_Right_Wing.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 1.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4616F, 0.8948F, 5.986F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r30 = Feather_Right_Wing.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 1.1618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.8309F, 1.1618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5228F, 1.2365F, 5.9423F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r31 = Feather_Right_Wing.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 0.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3832F, 0.9323F, 5.1621F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r32 = Feather_Right_Wing.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 0.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4444F, 1.274F, 5.1185F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r33 = Feather_Right_Wing.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 0.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2257F, 0.933F, 5.7499F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r34 = Feather_Right_Wing.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 0.1618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2869F, 1.2747F, 5.7062F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r35 = Feather_Right_Wing.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, -0.3382F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.8309F, -0.5382F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2085F, 1.3122F, 4.8824F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition cube_r36 = Feather_Right_Wing.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 14).addBox(-0.8309F, 0.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5949F, -1.2712F, 1.9361F, -0.7473F, -0.0284F, 0.4087F));

		PartDefinition cube_r37 = Feather_Right_Wing.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(0, 14).addBox(-0.8309F, 0.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.394F, -0.6253F, 2.6727F, -0.4901F, 0.0516F, 0.449F));

		PartDefinition cube_r38 = Feather_Right_Wing.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 14).addBox(-0.8309F, -0.0382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8246F, -0.2843F, 2.0414F, -0.4901F, 0.0516F, 0.449F));

		PartDefinition cube_r39 = Feather_Right_Wing.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(0, 14).addBox(-0.8309F, 0.3618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7634F, -0.626F, 2.085F, -0.5289F, 0.0072F, 0.3835F));

		PartDefinition cube_r40 = Feather_Right_Wing.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 14).addBox(-0.8309F, 0.5618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7022F, -0.9677F, 2.1286F, -0.5289F, 0.0072F, 0.3835F));

		PartDefinition cube_r41 = Feather_Right_Wing.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8419F, -0.6635F, 2.9088F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r42 = Feather_Right_Wing.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.3618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0605F, -0.3225F, 2.2775F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r43 = Feather_Right_Wing.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -0.0382F, -0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2792F, 0.0185F, 1.6461F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r44 = Feather_Right_Wing.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -0.0382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.48F, 0.6644F, 2.3827F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r45 = Feather_Right_Wing.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4188F, 0.3227F, 2.4263F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r46 = Feather_Right_Wing.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3576F, -0.019F, 2.4699F, -0.361F, 0.0934F, 0.5163F));

		PartDefinition cube_r47 = Feather_Right_Wing.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5585F, 0.6269F, 3.2065F, -0.4608F, -0.0511F, 0.3774F));

		PartDefinition cube_r48 = Feather_Right_Wing.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7771F, 0.9679F, 2.5752F, -0.4608F, -0.0511F, 0.3774F));

		PartDefinition cube_r49 = Feather_Right_Wing.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(0, 14).addBox(9.1F, 3.8F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(9.1F, 3.5F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.3F, 1.3F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.0F, 1.7F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.6F, 2.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.2F, 2.7F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(6.6F, 2.4F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.9F, 3.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(8.4F, 3.5F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(8.5F, 3.3F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.7F, 3.2F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(7.0F, 3.0F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(6.4F, 2.8F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.9F, 2.6F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(5.3F, 2.3F, -1.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3806F, 0.7039F, 1.9369F, -0.1756F, -0.2776F, -0.1546F));

		PartDefinition cube_r50 = Feather_Right_Wing.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(0, 14).addBox(-4.9F, -1.1F, -0.1F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.7F, -0.8F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.7F, -0.8F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.8F, -0.5F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3937F, 0.7457F, 1.2773F, -0.4287F, -0.4085F, -0.0674F));

		PartDefinition cube_r51 = Feather_Right_Wing.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(0, 14).addBox(4.8F, 2.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(4.2F, 2.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.6F, 1.9F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(3.0F, 1.6F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(2.5F, 1.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.8F, 1.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.2F, 1.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.6F, 0.8F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.3F, 0.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3806F, 0.7039F, 1.9369F, -0.1756F, -0.2776F, -0.1895F));

		PartDefinition cube_r52 = Feather_Right_Wing.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(0, 14).addBox(0.1F, -0.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, -0.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3013F, 0.9002F, 2.252F, -0.1407F, -0.2078F, -0.0674F));

		PartDefinition cube_r53 = Feather_Right_Wing.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(0, 14).addBox(-6.2F, -1.3F, -0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.5F, -1.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.5F, -1.1F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.0F, -0.9F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-4.2F, -0.7F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.2F, -0.5F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.3F, -0.4F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.6F, -0.3F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.685F, 1.9418F, 3.9389F, -0.4025F, -0.2078F, -0.0674F));

		PartDefinition cube_r54 = Feather_Right_Wing.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(0, 14).addBox(-5.9F, -1.4F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.1F, -1.2F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4339F, 3.4921F, 5.4169F, -0.4723F, -0.2078F, -0.0325F));

		PartDefinition cube_r55 = Feather_Right_Wing.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(0, 14).addBox(-4.3F, -1.0F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.3F, -0.7F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.6F, -0.4F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.7F, -0.2F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, 0.0F, -1.4F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4339F, 3.4921F, 5.4169F, -0.4723F, -0.2602F, -0.0325F));

		PartDefinition cube_r56 = Feather_Right_Wing.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4339F, 3.4921F, 5.4169F, -0.2803F, -0.2427F, -0.0325F));

		PartDefinition cube_r57 = Feather_Right_Wing.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(0, 14).addBox(2.9F, 1.5F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(2.3F, 1.2F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.0F, 0.4F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(1.7F, 0.8F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.5F, -0.4F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(0.2F, 0.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.5F, -0.7F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.1F, -1.0F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.7F, -1.3F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.5F, -1.6F, -2.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-4.4F, -1.8F, -2.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-5.2F, -2.1F, -2.2F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-6.1F, -2.4F, -2.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-6.9F, -2.8F, -2.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.4F, -1.3F, -1.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.4F, -1.3F, -1.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-2.7F, -1.1F, -1.3F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5358F, 3.4036F, 6.7323F, -0.2803F, -0.2427F, -0.1895F));

		PartDefinition cube_r58 = Feather_Right_Wing.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(0, 14).addBox(-2.2F, -0.9F, -1.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5358F, 3.4036F, 6.7323F, -0.3152F, -0.138F, -0.1895F));

		PartDefinition cube_r59 = Feather_Right_Wing.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(0, 14).addBox(-3.8309F, 2.0618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-3.8309F, 1.6618F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8806F, 1.8072F, 8.3265F, -0.311F, -0.0284F, 0.4087F));

		PartDefinition Left_Wing = Wings.addOrReplaceChild("Left_Wing", CubeListBuilder.create(), PartPose.offsetAndRotation(1.5F, -1.6F, 1.6F, 0.0F, -0.1571F, 0.0F));

		PartDefinition cube_r60 = Left_Wing.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -1.3F, -1.3F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2976F, 0.9611F, -0.9453F, -0.5236F, 0.0F, -0.1745F));

		PartDefinition cube_r61 = Left_Wing.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2976F, 0.9611F, 1.0547F, -0.3927F, -0.0436F, -0.1745F));

		PartDefinition cube_r62 = Left_Wing.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(5, 13).addBox(-3.0F, -0.5F, -1.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8976F, 1.6611F, 3.0547F, -0.3927F, -0.0436F, -0.1745F));

		PartDefinition cube_r63 = Left_Wing.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3404F, 1.2849F, 2.6263F, -0.3491F, 0.0873F, -0.1745F));

		PartDefinition cube_r64 = Left_Wing.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(5, 13).addBox(-1.4428F, -0.8238F, -1.5716F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3404F, 1.2849F, 2.6263F, -0.3491F, 0.0873F, -0.1745F));

		PartDefinition cube_r65 = Left_Wing.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -0.5F, -1.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2976F, 0.7611F, 1.0547F, -0.3927F, 0.0873F, -0.1745F));

		PartDefinition cube_r66 = Left_Wing.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(5, 13).addBox(-2.5F, -1.0F, -2.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2976F, 0.9611F, 1.0547F, -0.3927F, 0.0873F, -0.1745F));

		PartDefinition cube_r67 = Left_Wing.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(5, 13).addBox(-2.0F, -1.3F, -2.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2976F, 0.9611F, 1.0547F, -0.3054F, 0.0873F, -0.1745F));

		PartDefinition cube_r68 = Left_Wing.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(5, 13).addBox(-2.0F, -1.0F, -2.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5976F, 0.4611F, -1.9453F, -0.7418F, 0.0436F, -0.1745F));

		PartDefinition cube_r69 = Left_Wing.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(5, 13).addBox(-2.1F, -0.5F, -1.8F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5976F, -0.5389F, -1.9453F, -0.7418F, 0.0436F, -0.1745F));

		PartDefinition cube_r70 = Left_Wing.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(5, 13).addBox(-2.05F, -0.7F, -2.3F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.5976F, -0.5389F, -1.9453F, -0.7418F, 0.0436F, -0.1745F));

		PartDefinition cube_r71 = Left_Wing.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(5, 13).addBox(-2.05F, -1.0F, -2.7F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.5976F, -0.5389F, -1.9453F, -0.7418F, 0.0436F, -0.1745F));

		PartDefinition cube_r72 = Left_Wing.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(5, 13).addBox(-2.05F, -0.8F, -2.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2976F, -0.0389F, -0.9453F, -0.7418F, 0.0436F, -0.1745F));

		PartDefinition cube_r73 = Left_Wing.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(5, 13).addBox(-1.05F, -1.3F, -2.7F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.2976F, -0.0389F, -0.9453F, -0.6109F, 0.0436F, -0.1745F));

		PartDefinition cube_r74 = Left_Wing.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(5, 13).addBox(-1.5F, -0.5F, -0.8496F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.7384F, -0.6679F, -1.4957F, -0.5672F, 0.0436F, -0.1745F));

		PartDefinition cube_r75 = Left_Wing.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(5, 13).addBox(-1.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3213F, -0.0156F, -1.0341F, -0.5236F, 0.0436F, -0.1745F));

		PartDefinition Feathers_Left_Wing = Left_Wing.addOrReplaceChild("Feathers_Left_Wing", CubeListBuilder.create(), PartPose.offset(1.9063F, 4.6572F, 2.4475F));

		PartDefinition cube_r76 = Feathers_Left_Wing.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.9417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -0.8F, -0.1F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r77 = Feathers_Left_Wing.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.8721F, -3.3141F, -0.733F, -0.4633F, 0.3181F, 0.2749F));

		PartDefinition cube_r78 = Feathers_Left_Wing.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.0418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7336F, -2.0043F, -0.7017F, -0.5157F, 0.2483F, 0.1004F));

		PartDefinition cube_r79 = Feathers_Left_Wing.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.4417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7948F, -1.5626F, -1.1453F, -0.5265F, 0.215F, 0.0072F));

		PartDefinition cube_r80 = Feathers_Left_Wing.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.4418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6552F, -1.5668F, -1.0255F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r81 = Feathers_Left_Wing.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.4417F, -0.8903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7164F, -1.6251F, -1.1691F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r82 = Feathers_Left_Wing.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -1.4417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.062F, -1.2875F, -1.8929F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r83 = Feathers_Left_Wing.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.9417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4233F, -1.9292F, -1.9493F, -0.5162F, 0.148F, 0.0641F));

		PartDefinition cube_r84 = Feathers_Left_Wing.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.9417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5156F, -2.2709F, -1.9057F, -0.5162F, 0.148F, 0.0641F));

		PartDefinition cube_r85 = Feathers_Left_Wing.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.6417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4544F, -2.6126F, -1.8621F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r86 = Feathers_Left_Wing.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.0418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.332F, -3.296F, -1.7749F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r87 = Feathers_Left_Wing.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.3418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3932F, -2.9543F, -1.8185F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r88 = Feathers_Left_Wing.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.0417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2707F, -3.6377F, -1.7312F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r89 = Feathers_Left_Wing.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, 0.2583F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2095F, -3.9794F, -1.6876F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r90 = Feathers_Left_Wing.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, 0.4582F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.1483F, -4.3211F, -1.644F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r91 = Feathers_Left_Wing.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, 0.4582F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0871F, -4.6628F, -1.6004F, -0.3853F, 0.148F, 0.0641F));

		PartDefinition cube_r92 = Feathers_Left_Wing.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.7583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7639F, -5.992F, -2.2497F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r93 = Feathers_Left_Wing.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.4583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8251F, -5.6503F, -2.2933F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r94 = Feathers_Left_Wing.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.2582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8863F, -5.3086F, -2.3369F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r95 = Feathers_Left_Wing.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9475F, -4.9669F, -2.3806F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r96 = Feathers_Left_Wing.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.6583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0087F, -4.6253F, -2.4242F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r97 = Feathers_Left_Wing.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.4582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0699F, -4.2836F, -2.4678F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r98 = Feathers_Left_Wing.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1311F, -3.9419F, -2.5114F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r99 = Feathers_Left_Wing.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.0417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1923F, -3.6002F, -2.5551F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r100 = Feathers_Left_Wing.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2535F, -3.2585F, -2.5987F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r101 = Feathers_Left_Wing.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6241F, -2.5751F, -2.6859F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r102 = Feathers_Left_Wing.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.5417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2249F, -2.9209F, -3.3225F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r103 = Feathers_Left_Wing.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2861F, -3.2626F, -3.2789F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r104 = Feathers_Left_Wing.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.0418F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6527F, -3.6043F, -3.2352F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r105 = Feathers_Left_Wing.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.3582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5915F, -3.946F, -3.1916F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r106 = Feathers_Left_Wing.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.6582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5303F, -4.2877F, -3.148F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r107 = Feathers_Left_Wing.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.8583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.469F, -4.6294F, -3.1044F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r108 = Feathers_Left_Wing.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4078F, -4.9711F, -3.0608F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r109 = Feathers_Left_Wing.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.3582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3466F, -5.3128F, -3.0171F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r110 = Feathers_Left_Wing.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.5582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2854F, -5.6545F, -2.9735F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r111 = Feathers_Left_Wing.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.0583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1631F, -6.3379F, -2.8863F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r112 = Feathers_Left_Wing.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.1018F, -6.6796F, -2.8427F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r113 = Feathers_Left_Wing.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0406F, -7.0213F, -2.799F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r114 = Feathers_Left_Wing.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.9794F, -7.363F, -2.7554F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r115 = Feathers_Left_Wing.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.0417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.7406F, -5.2772F, -5.6041F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r116 = Feathers_Left_Wing.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.7786F, -8.0088F, -3.492F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r117 = Feathers_Left_Wing.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.8398F, -7.6671F, -3.5356F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r118 = Feathers_Left_Wing.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 2.0582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.901F, -7.3254F, -3.5792F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r119 = Feathers_Left_Wing.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.7583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9622F, -6.9837F, -3.6229F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r120 = Feathers_Left_Wing.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.5583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0234F, -6.642F, -3.6665F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r121 = Feathers_Left_Wing.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.7582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2243F, -5.9962F, -2.9299F, -0.8216F, 0.148F, 0.0641F));

		PartDefinition cube_r122 = Feathers_Left_Wing.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.5582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0846F, -6.3003F, -3.7101F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r123 = Feathers_Left_Wing.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.2583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1458F, -5.9586F, -3.7537F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r124 = Feathers_Left_Wing.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.207F, -5.6169F, -3.7973F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r125 = Feathers_Left_Wing.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.7583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2682F, -5.2753F, -3.841F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r126 = Feathers_Left_Wing.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.5583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3294F, -4.9336F, -3.8846F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r127 = Feathers_Left_Wing.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3906F, -4.5919F, -3.9282F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r128 = Feathers_Left_Wing.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4518F, -4.2502F, -3.9718F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r129 = Feathers_Left_Wing.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.487F, -3.9085F, -4.0154F, -0.6907F, 0.148F, 0.0641F));

		PartDefinition cube_r130 = Feathers_Left_Wing.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.3417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4258F, -3.5668F, -4.0591F, -0.5671F, 0.0869F, 0.0588F));

		PartDefinition cube_r131 = Feathers_Left_Wing.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.9582F, 0.0098F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2381F, -8.6588F, -5.0088F, -0.7592F, 0.2198F, -0.0558F));

		PartDefinition cube_r132 = Feathers_Left_Wing.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4009F, -5.2814F, -6.3843F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r133 = Feathers_Left_Wing.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.9582F, -0.0903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2993F, -8.3171F, -5.0524F, -0.7741F, 0.2852F, -0.1154F));

		PartDefinition cube_r134 = Feathers_Left_Wing.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.7583F, -0.2903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3605F, -7.9754F, -5.096F, -0.7741F, 0.2852F, -0.1154F));

		PartDefinition cube_r135 = Feathers_Left_Wing.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4621F, -4.9397F, -6.4279F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r136 = Feathers_Left_Wing.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.5583F, -0.2903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4217F, -7.6337F, -5.1396F, -0.7741F, 0.2852F, -0.1154F));

		PartDefinition cube_r137 = Feathers_Left_Wing.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.3582F, -0.3902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4829F, -7.292F, -5.1833F, -0.7661F, 0.2526F, -0.0853F));

		PartDefinition cube_r138 = Feathers_Left_Wing.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.7417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5845F, -4.2563F, -6.5151F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r139 = Feathers_Left_Wing.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1582F, -0.3902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5441F, -6.9503F, -5.2269F, -0.7661F, 0.2526F, -0.0853F));

		PartDefinition cube_r140 = Feathers_Left_Wing.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9582F, -0.3902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6053F, -6.6086F, -5.2705F, -0.7533F, 0.1867F, -0.0267F));

		PartDefinition cube_r141 = Feathers_Left_Wing.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.0418F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3659F, -4.5973F, -5.8838F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r142 = Feathers_Left_Wing.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.3417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1472F, -4.9384F, -5.2525F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r143 = Feathers_Left_Wing.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.7583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.6665F, -6.2669F, -5.3141F, -0.7592F, 0.2198F, -0.0558F));

		PartDefinition cube_r144 = Feathers_Left_Wing.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.4583F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7277F, -5.9253F, -5.3577F, -0.7533F, 0.1867F, -0.0267F));

		PartDefinition cube_r145 = Feathers_Left_Wing.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8696F, -4.855F, -5.4397F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r146 = Feathers_Left_Wing.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.3417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2084F, -4.5967F, -5.2961F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r147 = Feathers_Left_Wing.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2582F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7889F, -5.5835F, -5.4013F, -0.7484F, 0.1536F, 0.0021F));

		PartDefinition cube_r148 = Feathers_Left_Wing.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1499F, -5.2419F, -5.445F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r149 = Feathers_Left_Wing.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.051F, -4.596F, -4.7084F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r150 = Feathers_Left_Wing.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8878F, -4.2543F, -4.752F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r151 = Feathers_Left_Wing.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0887F, -4.9002F, -5.4886F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r152 = Feathers_Left_Wing.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.7418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2224F, -1.2834F, -1.2128F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r153 = Feathers_Left_Wing.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.9417F, -0.8903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1612F, -0.9417F, -1.2564F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r154 = Feathers_Left_Wing.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.9418F, -0.8903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, -0.4F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r155 = Feathers_Left_Wing.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.9417F, -0.8903F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, -0.2F, -0.4F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r156 = Feathers_Left_Wing.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.9417F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1F, -0.6F, -1.3F, -0.5098F, 0.0806F, 0.1198F));

		PartDefinition cube_r157 = Feathers_Left_Wing.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(0, 13).addBox(0.0691F, -0.4403F, 0.6058F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2695F, -4.6498F, 0.8222F, -2.0749F, -0.2465F, 0.0778F));

		PartDefinition cube_r158 = Feathers_Left_Wing.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2597F, 0.1058F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5316F, -3.4622F, 1.7151F, -2.1491F, -0.1832F, 0.0164F));

		PartDefinition cube_r159 = Feathers_Left_Wing.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2597F, 0.1058F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5928F, -3.3205F, 1.7715F, -2.1491F, -0.1832F, 0.0164F));

		PartDefinition cube_r160 = Feathers_Left_Wing.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2597F, -0.1942F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.654F, -3.0789F, 1.6279F, -2.1491F, -0.1832F, 0.0164F));

		PartDefinition cube_r161 = Feathers_Left_Wing.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.5597F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9811F, -2.0948F, 1.6284F, -2.0934F, -0.087F, -0.0733F));

		PartDefinition cube_r162 = Feathers_Left_Wing.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2597F, 0.1058F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5035F, -2.6782F, 1.7156F, -2.1491F, -0.1832F, 0.0164F));

		PartDefinition cube_r163 = Feathers_Left_Wing.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8641F, -2.6455F, -0.4761F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r164 = Feathers_Left_Wing.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0597F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3919F, -3.3664F, 0.4349F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r165 = Feathers_Left_Wing.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.5597F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2695F, -4.0498F, 0.5222F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r166 = Feathers_Left_Wing.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.7597F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2083F, -4.3915F, 0.5658F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r167 = Feathers_Left_Wing.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.2403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.15F, -4.9669F, -2.1744F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r168 = Feathers_Left_Wing.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2725F, -4.2835F, -2.2616F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r169 = Feathers_Left_Wing.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4733F, -3.6377F, -1.525F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r170 = Feathers_Left_Wing.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6284F, -3.3302F, -1.5643F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r171 = Feathers_Left_Wing.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3336F, -3.9418F, -2.3052F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r172 = Feathers_Left_Wing.addOrReplaceChild("cube_r172", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5499F, -3.2926F, -2.3881F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r173 = Feathers_Left_Wing.addOrReplaceChild("cube_r173", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5933F, -2.6461F, -1.0638F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r174 = Feathers_Left_Wing.addOrReplaceChild("cube_r174", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5463F, -2.9503F, -1.844F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r175 = Feathers_Left_Wing.addOrReplaceChild("cube_r175", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7472F, -3.5961F, -2.5806F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r176 = Feathers_Left_Wing.addOrReplaceChild("cube_r176", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.948F, -4.242F, -3.3172F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r177 = Feathers_Left_Wing.addOrReplaceChild("cube_r177", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0092F, -4.5837F, -3.2736F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r178 = Feathers_Left_Wing.addOrReplaceChild("cube_r178", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1916F, -3.9378F, -2.537F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r179 = Feathers_Left_Wing.addOrReplaceChild("cube_r179", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9629F, -4.6222F, -1.5729F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r180 = Feathers_Left_Wing.addOrReplaceChild("cube_r180", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.8618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7621F, -5.2681F, -2.3095F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r181 = Feathers_Left_Wing.addOrReplaceChild("cube_r181", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.2403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0888F, -5.3086F, -2.1308F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r182 = Feathers_Left_Wing.addOrReplaceChild("cube_r182", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1204F, -4.6229F, -2.1606F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r183 = Feathers_Left_Wing.addOrReplaceChild("cube_r183", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0592F, -4.9646F, -2.117F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r184 = Feathers_Left_Wing.addOrReplaceChild("cube_r184", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8583F, -5.6104F, -2.8536F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r185 = Feathers_Left_Wing.addOrReplaceChild("cube_r185", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9195F, -5.2687F, -2.8972F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r186 = Feathers_Left_Wing.addOrReplaceChild("cube_r186", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7359F, -6.2938F, -2.7664F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r187 = Feathers_Left_Wing.addOrReplaceChild("cube_r187", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9368F, -5.648F, -2.0298F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r188 = Feathers_Left_Wing.addOrReplaceChild("cube_r188", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8756F, -5.9897F, -1.9862F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r189 = Feathers_Left_Wing.addOrReplaceChild("cube_r189", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.6747F, -6.6355F, -2.7227F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r190 = Feathers_Left_Wing.addOrReplaceChild("cube_r190", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.6135F, -6.9772F, -2.6791F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r191 = Feathers_Left_Wing.addOrReplaceChild("cube_r191", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8144F, -6.3313F, -1.9425F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r192 = Feathers_Left_Wing.addOrReplaceChild("cube_r192", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.7532F, -6.673F, -1.8989F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r193 = Feathers_Left_Wing.addOrReplaceChild("cube_r193", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5523F, -7.3189F, -2.6355F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r194 = Feathers_Left_Wing.addOrReplaceChild("cube_r194", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.1506F, -8.6106F, -4.1086F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r195 = Feathers_Left_Wing.addOrReplaceChild("cube_r195", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.6618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.3515F, -7.9647F, -3.3721F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r196 = Feathers_Left_Wing.addOrReplaceChild("cube_r196", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.4127F, -7.623F, -3.4157F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r197 = Feathers_Left_Wing.addOrReplaceChild("cube_r197", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.4618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2118F, -8.2689F, -4.1523F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r198 = Feathers_Left_Wing.addOrReplaceChild("cube_r198", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4739F, -7.2814F, -3.4593F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r199 = Feathers_Left_Wing.addOrReplaceChild("cube_r199", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 1.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.273F, -7.9272F, -4.1959F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r200 = Feathers_Left_Wing.addOrReplaceChild("cube_r200", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.3342F, -7.5855F, -4.2395F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r201 = Feathers_Left_Wing.addOrReplaceChild("cube_r201", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5351F, -6.9397F, -3.5029F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r202 = Feathers_Left_Wing.addOrReplaceChild("cube_r202", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.7618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5963F, -6.598F, -3.5465F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r203 = Feathers_Left_Wing.addOrReplaceChild("cube_r203", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.7618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3954F, -7.2438F, -4.2831F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r204 = Feathers_Left_Wing.addOrReplaceChild("cube_r204", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.5618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4567F, -6.9021F, -4.3268F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r205 = Feathers_Left_Wing.addOrReplaceChild("cube_r205", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.5618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6575F, -6.2563F, -3.5902F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r206 = Feathers_Left_Wing.addOrReplaceChild("cube_r206", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.3618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7187F, -5.9146F, -3.6338F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r207 = Feathers_Left_Wing.addOrReplaceChild("cube_r207", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.3618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5179F, -6.5604F, -4.3704F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r208 = Feathers_Left_Wing.addOrReplaceChild("cube_r208", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5791F, -6.2187F, -4.414F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r209 = Feathers_Left_Wing.addOrReplaceChild("cube_r209", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.1618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7799F, -5.5729F, -3.6774F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r210 = Feathers_Left_Wing.addOrReplaceChild("cube_r210", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.1382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8411F, -5.2312F, -3.721F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r211 = Feathers_Left_Wing.addOrReplaceChild("cube_r211", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.1382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6403F, -5.877F, -4.4576F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r212 = Feathers_Left_Wing.addOrReplaceChild("cube_r212", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.3382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2985F, -5.5353F, -4.5012F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r213 = Feathers_Left_Wing.addOrReplaceChild("cube_r213", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.3382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0977F, -4.8895F, -3.7647F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r214 = Feathers_Left_Wing.addOrReplaceChild("cube_r214", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.5382F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0365F, -4.5478F, -3.8083F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r215 = Feathers_Left_Wing.addOrReplaceChild("cube_r215", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2373F, -5.1936F, -4.5448F, -0.7418F, -0.0436F, -0.3491F));

		PartDefinition cube_r216 = Feathers_Left_Wing.addOrReplaceChild("cube_r216", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.7418F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6853F, -2.1168F, -2.5423F, -0.5598F, 0.148F, 0.0641F));

		PartDefinition cube_r217 = Feathers_Left_Wing.addOrReplaceChild("cube_r217", CubeListBuilder.create().texOffs(0, 13).addBox(-0.1852F, -1.0418F, -0.8902F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6724F, -2.346F, -0.2581F, -0.5356F, 0.0922F, -0.0025F));

		PartDefinition cube_r218 = Feathers_Left_Wing.addOrReplaceChild("cube_r218", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.7417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.1233F, -5.198F, -6.5715F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r219 = Feathers_Left_Wing.addOrReplaceChild("cube_r219", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.8018F, -4.9355F, -5.6477F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r220 = Feathers_Left_Wing.addOrReplaceChild("cube_r220", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7971F, -5.9521F, -2.81F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r221 = Feathers_Left_Wing.addOrReplaceChild("cube_r221", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9807F, -4.927F, -2.9408F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r222 = Feathers_Left_Wing.addOrReplaceChild("cube_r222", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.2417F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2018F, -4.4355F, -5.6477F, -0.7416F, 0.0869F, 0.0588F));

		PartDefinition cube_r223 = Feathers_Left_Wing.addOrReplaceChild("cube_r223", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.9618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.998F, -5.3063F, -2.0734F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r224 = Feathers_Left_Wing.addOrReplaceChild("cube_r224", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.2618F, -1.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1816F, -4.2812F, -2.2043F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r225 = Feathers_Left_Wing.addOrReplaceChild("cube_r225", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7633F, -1.9996F, 0.2605F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r226 = Feathers_Left_Wing.addOrReplaceChild("cube_r226", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.2403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4245F, -2.3413F, 0.3041F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r227 = Feathers_Left_Wing.addOrReplaceChild("cube_r227", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0597F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5143F, -2.683F, 0.3477F, -0.4363F, -0.0436F, -0.3491F));

		PartDefinition cube_r228 = Feathers_Left_Wing.addOrReplaceChild("cube_r228", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3455F, -2.3044F, -1.1075F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r229 = Feathers_Left_Wing.addOrReplaceChild("cube_r229", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6723F, -2.6092F, -2.4754F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r230 = Feathers_Left_Wing.addOrReplaceChild("cube_r230", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.9403F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7508F, -2.6468F, -1.6515F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition cube_r231 = Feathers_Left_Wing.addOrReplaceChild("cube_r231", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -0.4403F, -0.5F, 2.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.3509F, -4.3211F, -1.4378F, -0.6545F, -0.0436F, -0.3491F));

		PartDefinition Body = Whole_model.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.431F, 7.3695F, 2.556F));

		PartDefinition Body_Part1 = Body.addOrReplaceChild("Body_Part1", CubeListBuilder.create(), PartPose.offset(-0.3F, -2.0F, -3.3F));

		PartDefinition cube_r232 = Body_Part1.addOrReplaceChild("cube_r232", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -0.4F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3766F, -0.4648F, 13.0072F, -1.0908F, -1.405F, -0.3491F));

		PartDefinition cube_r233 = Body_Part1.addOrReplaceChild("cube_r233", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -0.2F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1782F, -0.9099F, 9.7661F, -1.2305F, -0.5498F, -0.3491F));

		PartDefinition cube_r234 = Body_Part1.addOrReplaceChild("cube_r234", CubeListBuilder.create().texOffs(7, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0759F, 0.6809F, 12.8997F, -0.3927F, 0.3491F, 0.1309F));

		PartDefinition cube_r235 = Body_Part1.addOrReplaceChild("cube_r235", CubeListBuilder.create().texOffs(8, 7).addBox(-1.6F, -0.6F, -2.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, 0.0F, 12.3F, -0.4363F, 0.7854F, 0.1309F));

		PartDefinition cube_r236 = Body_Part1.addOrReplaceChild("cube_r236", CubeListBuilder.create().texOffs(7, 0).addBox(-1.5F, -0.5F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, 0.0F, 12.3F, -0.5672F, 0.6981F, 0.1309F));

		PartDefinition cube_r237 = Body_Part1.addOrReplaceChild("cube_r237", CubeListBuilder.create().texOffs(8, 7).addBox(-0.5F, -1.6F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, -1.0F, 10.3F, -0.5672F, 0.6981F, 0.1309F));

		PartDefinition cube_r238 = Body_Part1.addOrReplaceChild("cube_r238", CubeListBuilder.create().texOffs(7, 0).addBox(0.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -2.0F, 8.3F, -0.5672F, 0.48F, 0.1309F));

		PartDefinition Body_Part2 = Body.addOrReplaceChild("Body_Part2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone4 = Body_Part2.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(4.0F, 2.0F, 16.0F));

		PartDefinition cube_r239 = bone4.addOrReplaceChild("cube_r239", CubeListBuilder.create().texOffs(7, 0).addBox(-2.0F, -1.3F, -4.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, -0.2618F, 0.1309F));

		PartDefinition cube_r240 = bone4.addOrReplaceChild("cube_r240", CubeListBuilder.create().texOffs(8, 7).addBox(-2.0F, -1.0F, -4.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.0F, -2.0F, -0.48F, 0.0F, 0.1309F));

		PartDefinition bone3 = Body_Part2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(3.0F, 1.8F, 17.0F));

		PartDefinition cube_r241 = bone3.addOrReplaceChild("cube_r241", CubeListBuilder.create().texOffs(8, 7).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, -0.4363F, 0.1309F));

		PartDefinition cube_r242 = bone3.addOrReplaceChild("cube_r242", CubeListBuilder.create().texOffs(7, 0).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -0.1F, 0.7F, -0.0262F, -1.0647F, 0.1309F));

		PartDefinition Body_Part3 = Body.addOrReplaceChild("Body_Part3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone2 = Body_Part3.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(-3.4F, 1.0F, 16.8F));

		PartDefinition cube_r243 = bone2.addOrReplaceChild("cube_r243", CubeListBuilder.create().texOffs(8, 7).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0262F, -1.7628F, 0.2182F));

		PartDefinition cube_r244 = bone2.addOrReplaceChild("cube_r244", CubeListBuilder.create().texOffs(7, 0).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9F, 0.4F, 0.9F, -0.0262F, -1.4312F, 0.1658F));

		PartDefinition bone = Body_Part3.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-5.8F, -0.1F, 13.6F));

		PartDefinition cube_r245 = bone.addOrReplaceChild("cube_r245", CubeListBuilder.create().texOffs(8, 7).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0262F, -2.4784F, 0.3752F));

		PartDefinition cube_r246 = bone.addOrReplaceChild("cube_r246", CubeListBuilder.create().texOffs(7, 0).addBox(-1.8F, -2.0F, -3.3F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, 0.7F, 1.7F, -0.0262F, -2.1817F, 0.2182F));

		PartDefinition Body_Part4 = Body.addOrReplaceChild("Body_Part4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone6 = Body_Part4.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(-4.1F, -0.3F, 9.1F));

		PartDefinition cube_r247 = bone6.addOrReplaceChild("cube_r247", CubeListBuilder.create().texOffs(6, 0).addBox(-1.0F, -2.2F, -3.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.0436F, 0.0873F));

		PartDefinition cube_r248 = bone6.addOrReplaceChild("cube_r248", CubeListBuilder.create().texOffs(5, 0).addBox(-2.2F, -1.8F, -2.6F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, -0.1F, 0.9F, -0.1309F, 0.0F, 0.0873F));

		PartDefinition cube_r249 = bone6.addOrReplaceChild("cube_r249", CubeListBuilder.create().texOffs(5, 0).addBox(-2.2F, -1.5F, -2.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 0.0F, 1.9F, -0.2182F, 0.0F, 0.0873F));

		PartDefinition cube_r250 = bone6.addOrReplaceChild("cube_r250", CubeListBuilder.create().texOffs(5, 0).addBox(-2.2F, -1.0F, -2.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 0.0F, 2.9F, -0.3491F, 0.0F, 0.0873F));

		PartDefinition bone5 = Body_Part4.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(-3.6808F, 0.8839F, 12.1019F));

		PartDefinition cube_r251 = bone5.addOrReplaceChild("cube_r251", CubeListBuilder.create().texOffs(0, 13).addBox(-1.6F, -0.3F, 0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.45F, -0.4F, 2.2F, 1.7443F, -1.9706F, -0.1197F));

		PartDefinition cube_r252 = bone5.addOrReplaceChild("cube_r252", CubeListBuilder.create().texOffs(0, 13).addBox(-1.6F, -0.3F, 0.8F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.6395F, 2.0262F, 0.1072F));

		PartDefinition cube_r253 = bone5.addOrReplaceChild("cube_r253", CubeListBuilder.create().texOffs(7, 0).addBox(-2.2F, -2.2F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2808F, -0.3839F, 0.8981F, -0.2618F, 0.0F, 0.0873F));

		PartDefinition cube_r254 = bone5.addOrReplaceChild("cube_r254", CubeListBuilder.create().texOffs(8, 7).addBox(-1.8F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2808F, -0.0839F, 2.7981F, -0.2007F, 0.2269F, 0.2007F));

		PartDefinition Body_Neck = Body.addOrReplaceChild("Body_Neck", CubeListBuilder.create(), PartPose.offset(0.0F, -8.8F, 0.0F));

		PartDefinition cube_r255 = Body_Neck.addOrReplaceChild("cube_r255", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4782F, 1.3901F, -6.8338F, -1.213F, -1.0908F, -0.3491F));

		PartDefinition cube_r256 = Body_Neck.addOrReplaceChild("cube_r256", CubeListBuilder.create().texOffs(8, 7).addBox(-1.5F, -2.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.8F, -4.0F, 0.2182F, -0.2618F, 0.1309F));

		PartDefinition cube_r257 = Body_Neck.addOrReplaceChild("cube_r257", CubeListBuilder.create().texOffs(7, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.8F, -6.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition cube_r258 = Body_Neck.addOrReplaceChild("cube_r258", CubeListBuilder.create().texOffs(7, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.8F, -8.0F, 0.0873F, 0.1309F, 0.0F));

		PartDefinition cube_r259 = Body_Neck.addOrReplaceChild("cube_r259", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.8F, -10.0F, -0.0445F, -0.084F, -0.0377F));

		PartDefinition Body_Wings = Body.addOrReplaceChild("Body_Wings", CubeListBuilder.create(), PartPose.offset(0.0F, -7.8F, 0.0F));

		PartDefinition cube_r260 = Body_Wings.addOrReplaceChild("cube_r260", CubeListBuilder.create().texOffs(7, 0).addBox(-0.7F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.8F, 3.0F, -0.6109F, 0.3054F, 0.0873F));

		PartDefinition cube_r261 = Body_Wings.addOrReplaceChild("cube_r261", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 1.8F, 1.0F, -0.5672F, 0.0F, 0.1309F));

		PartDefinition cube_r262 = Body_Wings.addOrReplaceChild("cube_r262", CubeListBuilder.create().texOffs(8, 7).addBox(-0.6F, -2.0F, -1.7F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.8F, -1.0F, -0.3491F, -0.2618F, 0.1309F));

		PartDefinition cube_r263 = Body_Wings.addOrReplaceChild("cube_r263", CubeListBuilder.create().texOffs(7, 0).addBox(-0.5F, -2.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.8F, -3.0F, -0.1745F, -0.0436F, 0.1309F));

		PartDefinition Skull = Whole_model.addOrReplaceChild("Skull", CubeListBuilder.create(), PartPose.offset(0.931F, -1.0332F, 0.628F));

		PartDefinition cube_r264 = Skull.addOrReplaceChild("cube_r264", CubeListBuilder.create().texOffs(0, 3).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 2.4027F, -11.0721F, -0.0436F, -0.0873F, 0.0F));

		PartDefinition bone7 = Skull.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(-0.5F, 1.1027F, -13.1721F));

		PartDefinition cube_r265 = bone7.addOrReplaceChild("cube_r265", CubeListBuilder.create().texOffs(0, 9).addBox(-0.9F, -1.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.192F, 0.0F, 0.0F));

		PartDefinition bone8 = Skull.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(0.0F, 1.4F, -12.8F));

		PartDefinition cube_r266 = bone8.addOrReplaceChild("cube_r266", CubeListBuilder.create().texOffs(0, 9).addBox(-1.4F, -0.5973F, -0.8721F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition Fire = Skull.addOrReplaceChild("Fire", CubeListBuilder.create(), PartPose.offset(1.5F, 6.4027F, -2.0721F));

		PartDefinition cube_r267 = Fire.addOrReplaceChild("cube_r267", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -0.1208F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7022F, -5.3792F, -10.4764F, 2.975F, -0.5638F, -0.4443F));

		PartDefinition cube_r268 = Fire.addOrReplaceChild("cube_r268", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -0.1208F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7022F, -5.3792F, -10.4764F, 2.975F, -0.5638F, -0.7061F));

		PartDefinition cube_r269 = Fire.addOrReplaceChild("cube_r269", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -0.1208F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7022F, -5.3792F, -10.4764F, -2.8719F, -0.5638F, -0.7061F));

		PartDefinition cube_r270 = Fire.addOrReplaceChild("cube_r270", CubeListBuilder.create().texOffs(0, 10).addBox(1.2F, -0.8F, -0.8F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.2F, -10.2F, -0.0873F, 0.0436F, 0.7854F));

		PartDefinition cube_r271 = Fire.addOrReplaceChild("cube_r271", CubeListBuilder.create().texOffs(0, 10).addBox(1.2F, -0.8F, -0.8F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.2F, -10.2F, -0.0873F, 0.0F, 0.6109F));

		PartDefinition cube_r272 = Fire.addOrReplaceChild("cube_r272", CubeListBuilder.create().texOffs(0, 10).addBox(1.2F, -0.8F, -0.8F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -6.2F, -10.2F, 0.1745F, 0.0F, 0.6109F));

		PartDefinition Eye_Fire = Fire.addOrReplaceChild("Eye_Fire", CubeListBuilder.create(), PartPose.offset(-1.9F, -7.1F, -8.4F));

		PartDefinition cube_r273 = Eye_Fire.addOrReplaceChild("cube_r273", CubeListBuilder.create().texOffs(0, 10).addBox(1.2F, -0.7F, -0.8F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.48F));

		PartDefinition cube_r274 = Eye_Fire.addOrReplaceChild("cube_r274", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2734F, 0.323F, -0.5724F, 0.2618F, -0.1309F, -0.3054F));

		return LayerDefinition.create(meshdefinition, 16, 16);
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
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
	                      float ageInTicks, float netHeadYaw, float headPitch) {

		// Lazy-init buffers once we know part count
		if (this.idlePose == null) {
			int partCount = (int) this.root().getAllParts().count();
			this.idlePose = new float[partCount * FLOATS_PER_PART];
			this.walkPose = new float[partCount * FLOATS_PER_PART];
		}

		if (entity instanceof LivingEntity living) {
			boolean isWalking = limbSwingAmount > 0.01F;
			this.walkBlend = Mth.lerp(BLEND_SPEED, this.walkBlend, isWalking ? 1.0F : 0.0F);

			// Keep both states running so their internal timers don't reset
			if (!this.idleAnimationState.isStarted()) this.idleAnimationState.start((int) ageInTicks);
			if (!this.walkAnimationState.isStarted()) this.walkAnimationState.start((int) ageInTicks);

			// Sample idle into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.idleAnimationState, DeathMythicalCreatureAnimations.idle, ageInTicks, 1.0F);
			capturePoseInto(this.idlePose);

			// Sample walk into snapshot
			this.root().getAllParts().forEach(ModelPart::resetPose);
			this.animate(this.walkAnimationState, DeathMythicalCreatureAnimations.Walking, ageInTicks, 1.0F);
			capturePoseInto(this.walkPose);

			// Write the lerped result
			applyBlendedPose(this.idlePose, this.walkPose, this.walkBlend);
		}
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}