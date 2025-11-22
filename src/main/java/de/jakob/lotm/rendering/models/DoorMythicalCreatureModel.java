package de.jakob.lotm.rendering.models;// Made with Blockbench 5.0.3
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

public class DoorMythicalCreatureModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "door_mythical_creature"), "main");
	private final ModelPart Waist;
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart RightArm;
	private final ModelPart bone2;
	private final ModelPart LeftArm;
	private final ModelPart bone;
	private final ModelPart RightLeg;
	private final ModelPart lightballrightleg;
	private final ModelPart LeftLeg;
	private final ModelPart lightballleftleg;

	public DoorMythicalCreatureModel(ModelPart root) {
		this.Waist = root.getChild("Waist");
		this.Head = this.Waist.getChild("Head");
		this.Body = this.Waist.getChild("Body");
		this.RightArm = this.Waist.getChild("RightArm");
		this.bone2 = this.RightArm.getChild("bone2");
		this.LeftArm = this.Waist.getChild("LeftArm");
		this.bone = this.LeftArm.getChild("bone");
		this.RightLeg = root.getChild("RightLeg");
		this.lightballrightleg = this.RightLeg.getChild("lightballrightleg");
		this.LeftLeg = root.getChild("LeftLeg");
		this.lightballleftleg = this.LeftLeg.getChild("lightballleftleg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition Head = Waist.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition cube_r1 = Head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-2.3513F, -3.0987F, 1.3453F, 0.929F, -0.9453F, -0.0773F));

		PartDefinition cube_r2 = Head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(-0.7513F, -2.6987F, 1.3453F, -2.3124F, -1.1737F, 0.4805F));

		PartDefinition cube_r3 = Head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-2.9732F, -2.2972F, 2.4187F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.3F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-2.9732F, -2.2972F, 2.4187F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-1.5946F, -0.2943F, -3.8333F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.5946F, -0.2943F, -3.8333F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.7513F, -2.6987F, 1.3453F, 1.9336F, -0.7925F, 0.3924F));

		PartDefinition cube_r4 = Head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-5.2494F, -2.0827F, -3.4593F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.5F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-5.2494F, -2.0827F, -3.4593F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.3F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(1.512F, -0.266F, 0.4232F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(1.512F, -0.266F, 0.4232F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offsetAndRotation(-0.7513F, -2.6987F, 1.3453F, 1.8763F, -1.1737F, 0.4805F));

		PartDefinition Body = Waist.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition cube_r5 = Body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-5.7513F, 3.4013F, 1.3453F, 0.929F, -0.9453F, -0.0773F));

		PartDefinition cube_r6 = Body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-1.2513F, 4.6013F, 1.3453F, 0.929F, -0.9453F, -0.0773F));

		PartDefinition cube_r7 = Body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.8811F, -4.1271F, -5.7123F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(-5.2513F, 10.6013F, 1.3453F, 0.929F, -0.9453F, -0.0773F));

		PartDefinition cube_r8 = Body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 3.2F, -2.7F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r9 = Body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F))
		.texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(5.5F, -0.8F, -2.7F, 1.3603F, 0.4981F, -0.2426F));

		PartDefinition cube_r10 = Body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F))
		.texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(2.5F, 3.2F, -2.7F, 1.3603F, 0.4981F, -0.2426F));

		PartDefinition cube_r11 = Body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(0.0639F, -1.1488F, -2.1247F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 8).addBox(0.0639F, -1.1488F, -2.1247F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-0.0387F, 1.7795F, -1.1688F, -1.0464F, -0.0164F, 0.344F));

		PartDefinition cube_r12 = Body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-1.183F, 0.2373F, -0.6897F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 8).addBox(-1.183F, 0.2373F, -0.6897F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.2074F, 9.5795F, -0.009F, -1.1861F, -0.3848F, 2.554F));

		PartDefinition cube_r13 = Body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 8).addBox(-0.6837F, 0.1308F, -2.9601F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F))
		.texOffs(0, 0).addBox(-0.6837F, 0.1308F, -2.9601F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.2074F, 10.5795F, -0.009F, 0.9748F, 0.0482F, -0.6043F));

		PartDefinition cube_r14 = Body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 7.2F, -2.7F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r15 = Body.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F))
		.texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(2.5F, 3.2F, -3.9F, 1.6888F, 0.5253F, 0.4101F));

		PartDefinition cube_r16 = Body.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-3.3035F, -0.3296F, -2.9652F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 8).addBox(-3.3035F, -0.3296F, -2.9652F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.2074F, 10.5795F, -0.009F, -2.2333F, -0.3848F, 2.554F));

		PartDefinition RightArm = Waist.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, -10.0F, 0.0F));

		PartDefinition bone2 = RightArm.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.5F, 4.5F, -0.35F));

		PartDefinition cube_r17 = bone2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.0364F, -8.4547F, -4.3439F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-1.0364F, -8.4547F, -4.3439F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.3013F, 1.1665F, -0.6565F, 1.0574F, -1.142F));

		PartDefinition cube_r18 = bone2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(0.2534F, -2.8618F, 6.2784F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(0.2534F, -2.8618F, 6.2784F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.3013F, 1.1665F, 2.4851F, -1.0574F, -1.9996F));

		PartDefinition cube_r19 = bone2.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-10.5042F, -0.8283F, -2.377F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-10.5042F, -0.8283F, -2.377F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.3013F, 1.1665F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r20 = bone2.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-2.0976F, -3.3399F, -5.538F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-2.0976F, -3.3399F, -5.538F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offsetAndRotation(-4.8F, -1.1F, 0.0F, -1.0072F, 0.8049F, 2.8808F));

		PartDefinition cube_r21 = bone2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.3F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(-3.5F, 3.7F, 0.35F, 2.3827F, -0.5517F, -0.1288F));

		PartDefinition cube_r22 = bone2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-9.4469F, -0.4862F, -5.5599F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-9.4469F, -0.4862F, -5.5599F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.3F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.3013F, 1.1665F, 2.3827F, -0.5517F, -0.1288F));

		PartDefinition cube_r23 = bone2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.5426F, -5.233F, 1.2399F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.5426F, -5.233F, 1.2399F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, 1.3013F, 1.1665F, -1.0072F, 0.8049F, 2.8808F));

		PartDefinition cube_r24 = bone2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false), PartPose.offsetAndRotation(-8.0F, -3.3F, -6.35F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r25 = bone2.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -3.5F, -0.35F, 2.4851F, -1.0574F, -1.9996F));

		PartDefinition cube_r26 = bone2.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.7146F, -5.4994F, -0.5876F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-1.7146F, -5.4994F, -0.5876F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6565F, 1.0574F, -1.142F));

		PartDefinition LeftArm = Waist.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, -10.0F, 0.0F));

		PartDefinition bone = LeftArm.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(2.7291F, 5.7005F, -0.2227F));

		PartDefinition cube_r27 = bone.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F))
		.texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(4.7709F, -4.5005F, -6.4773F, 1.3603F, 0.4981F, -0.2426F));

		PartDefinition cube_r28 = bone.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F))
		.texOffs(0, 8).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.2709F, 2.4995F, 0.2227F, 2.3827F, 0.5517F, 0.1288F));

		PartDefinition cube_r29 = bone.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 0).addBox(-1.9024F, -3.3399F, -5.538F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F))
		.texOffs(0, 8).addBox(-1.9024F, -3.3399F, -5.538F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5709F, -2.3005F, -0.1273F, -1.0072F, -0.8049F, -2.8808F));

		PartDefinition cube_r30 = bone.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 8).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.7291F, -4.7005F, -0.4773F, 2.4851F, 1.0574F, 1.9996F));

		PartDefinition cube_r31 = bone.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F))
		.texOffs(0, 8).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2709F, -0.7005F, 0.9227F, 2.3827F, 0.5517F, 0.1288F));

		PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition lightballrightleg = RightLeg.addOrReplaceChild("lightballrightleg", CubeListBuilder.create(), PartPose.offset(-5.1F, -9.0F, 0.0F));

		PartDefinition cube_r32 = lightballrightleg.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F)).mirror(false), PartPose.offsetAndRotation(0.5F, 11.2F, -7.7F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r33 = lightballrightleg.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.4F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)).mirror(false), PartPose.offsetAndRotation(0.0F, 9.5F, -5.0F, 1.3603F, -0.4981F, 0.2426F));

		PartDefinition cube_r34 = lightballrightleg.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.9904F, 0.4734F, -7.8467F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(4.9904F, 0.4734F, -7.8467F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(4.6972F, 11.3897F, 4.0F, -2.3062F, 1.225F, 2.4694F));

		PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition lightballleftleg = LeftLeg.addOrReplaceChild("lightballleftleg", CubeListBuilder.create(), PartPose.offset(5.1F, -9.0F, 0.0F));

		PartDefinition cube_r35 = lightballleftleg.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.6F))
		.texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(-0.5F, 11.2F, -7.7F, 1.3603F, 0.4981F, -0.2426F));

		PartDefinition cube_r36 = lightballleftleg.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 0).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.4F))
		.texOffs(0, 8).addBox(-8.7175F, -0.25F, -3.75F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 9.5F, -5.0F, 1.3603F, 0.4981F, -0.2426F));

		PartDefinition cube_r37 = lightballleftleg.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(0, 0).addBox(-8.9904F, 0.4734F, -7.8467F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.1F))
		.texOffs(0, 8).addBox(-8.9904F, 0.4734F, -7.8467F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-4.6972F, 11.3897F, 4.0F, -2.3062F, -1.225F, -2.4694F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		Waist.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}