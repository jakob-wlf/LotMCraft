package de.jakob.lotm.entity.client.ability_entities.death_pathway.underworld_gate;// Made with Blockbench 5.1.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.spirits.bizarro_bane.SpiritBizarroBaneAnimations;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.UnderworldGateEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class UnderworldGateModel<T extends UnderworldGateEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "underworld_gate"), "main");
	private final ModelPart root;
	private final ModelPart full_gate;
	private final ModelPart door;
	private final ModelPart door_2;
	private final ModelPart arch_3;
	private final ModelPart arch_5;
	private final ModelPart arch_13;
	private final ModelPart arch_14;
	private final ModelPart arch_8;
	private final ModelPart arch_9;
	private final ModelPart arch_6;
	private final ModelPart arch7;
	private final ModelPart arch_10;
	private final ModelPart arch_12;
	private final ModelPart door_3;
	private final ModelPart door_right;
	private final ModelPart arch_right;
	private final ModelPart arch16;
	private final ModelPart arch_right_1;
	private final ModelPart arch_22;
	private final ModelPart arch_left_2;
	private final ModelPart arch15;
	private final ModelPart arch_right_2;
	private final ModelPart arch20;
	private final ModelPart arch_left_3_2;
	private final ModelPart arch_left_3_1;
	private final ModelPart arch_right_3_2;
	private final ModelPart arch_right_3_1;
	private final ModelPart door5;
	private final ModelPart door4;
	private final ModelPart door_left;
	private final ModelPart start_of_left_door;
	private final ModelPart arch24;
	private final ModelPart bone2;
	private final ModelPart bone;
	private final ModelPart arch17;
	private final ModelPart arch18;
	private final ModelPart arch25;
	private final ModelPart arch26;
	private final ModelPart left;
	private final ModelPart arch19;
	private final ModelPart tentacles;
	private final ModelPart tentacle_1;
	private final ModelPart tentacle_5;
	private final ModelPart tentacle_6;
	private final ModelPart tentacle_7;
	private final ModelPart tentacle_4;
	private final ModelPart tentacle_2;
	private final ModelPart tentacle_3;

	public UnderworldGateModel(ModelPart root) {
		this.root = root;
		this.full_gate = root.getChild("full_gate");
		this.door = this.full_gate.getChild("door");
		this.door_2 = this.door.getChild("door_2");
		this.arch_3 = this.door.getChild("arch_3");
		this.arch_5 = this.arch_3.getChild("arch_5");
		this.arch_13 = this.door.getChild("arch_13");
		this.arch_14 = this.arch_13.getChild("arch_14");
		this.arch_8 = this.door.getChild("arch_8");
		this.arch_9 = this.arch_8.getChild("arch_9");
		this.arch_6 = this.door.getChild("arch_6");
		this.arch7 = this.arch_6.getChild("arch7");
		this.arch_10 = this.door.getChild("arch_10");
		this.arch_12 = this.arch_10.getChild("arch_12");
		this.door_3 = this.full_gate.getChild("door_3");
		this.door_right = this.door_3.getChild("door_right");
		this.arch_right = this.door_right.getChild("arch_right");
		this.arch16 = this.arch_right.getChild("arch16");
		this.arch_right_1 = this.door_right.getChild("arch_right_1");
		this.arch_22 = this.arch_right_1.getChild("arch_22");
		this.arch_left_2 = this.door_right.getChild("arch_left_2");
		this.arch15 = this.arch_left_2.getChild("arch15");
		this.arch_right_2 = this.door_right.getChild("arch_right_2");
		this.arch20 = this.arch_right_2.getChild("arch20");
		this.arch_left_3_2 = this.door_right.getChild("arch_left_3_2");
		this.arch_left_3_1 = this.arch_left_3_2.getChild("arch_left_3_1");
		this.arch_right_3_2 = this.door_right.getChild("arch_right_3_2");
		this.arch_right_3_1 = this.arch_right_3_2.getChild("arch_right_3_1");
		this.door5 = this.door_right.getChild("door5");
		this.door4 = this.door_right.getChild("door4");
		this.door_left = this.door_3.getChild("door_left");
		this.start_of_left_door = this.door_left.getChild("start_of_left_door");
		this.arch24 = this.start_of_left_door.getChild("arch24");
		this.bone2 = this.door_left.getChild("bone2");
		this.bone = this.door_left.getChild("bone");
		this.arch17 = this.door_left.getChild("arch17");
		this.arch18 = this.arch17.getChild("arch18");
		this.arch25 = this.door_left.getChild("arch25");
		this.arch26 = this.arch25.getChild("arch26");
		this.left = this.door_left.getChild("left");
		this.arch19 = this.left.getChild("arch19");
		this.tentacles = root.getChild("tentacles");
		this.tentacle_1 = this.tentacles.getChild("tentacle_1");
		this.tentacle_5 = this.tentacles.getChild("tentacle_5");
		this.tentacle_6 = this.tentacles.getChild("tentacle_6");
		this.tentacle_7 = this.tentacles.getChild("tentacle_7");
		this.tentacle_4 = this.tentacles.getChild("tentacle_4");
		this.tentacle_2 = this.tentacles.getChild("tentacle_2");
		this.tentacle_3 = this.tentacles.getChild("tentacle_3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition full_gate = partdefinition.addOrReplaceChild("full_gate", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition door = full_gate.addOrReplaceChild("door", CubeListBuilder.create().texOffs(0, 54).addBox(0.5F, 8.0F, -4.0F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(18, 27).addBox(0.5F, -9.0F, -5.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(12, -10).addBox(0.5F, -17.0F, -4.0F, 0.0F, 25.0F, 18.0F, new CubeDeformation(0.1F))
				.texOffs(0, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));

		PartDefinition door_2 = door.addOrReplaceChild("door_2", CubeListBuilder.create().texOffs(22, 40).addBox(0.5F, -9.0F, 4.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 54).addBox(0.5F, 8.0F, -5.0F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));

		PartDefinition cube_r1 = door_2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -18.0F, -5.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -21.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r2 = door_2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -21.0F, -5.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -20.0F, 9.4F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r3 = door_2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -21.0F, -5.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -20.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r4 = door_2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -22.0F, -5.0F, 1.0F, 17.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -19.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r5 = door_2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(7, 7).addBox(-0.5F, -22.0F, -5.0F, 1.0F, 18.0F, 3.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -16.0F, 8.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r6 = door_2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(7, 7).addBox(-0.5F, -22.0F, -5.0F, 1.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 7.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition arch_3 = door.addOrReplaceChild("arch_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -16.1F, 13.1F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r7 = arch_3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r8 = arch_3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r9 = arch_3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r10 = arch_3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r11 = arch_3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_5 = arch_3.addOrReplaceChild("arch_5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r12 = arch_5.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r13 = arch_5.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r14 = arch_5.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r15 = arch_5.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r16 = arch_5.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r17 = arch_5.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r18 = arch_5.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r19 = arch_5.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r20 = arch_5.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r21 = arch_5.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r22 = arch_5.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_13 = door.addOrReplaceChild("arch_13", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -16.1F, 13.1F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r23 = arch_13.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r24 = arch_13.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r25 = arch_13.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r26 = arch_13.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r27 = arch_13.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_14 = arch_13.addOrReplaceChild("arch_14", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r28 = arch_14.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r29 = arch_14.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r30 = arch_14.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r31 = arch_14.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r32 = arch_14.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r33 = arch_14.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r34 = arch_14.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r35 = arch_14.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r36 = arch_14.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r37 = arch_14.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r38 = arch_14.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_8 = door.addOrReplaceChild("arch_8", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2F, -16.1F, 13.1F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r39 = arch_8.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r40 = arch_8.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r41 = arch_8.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r42 = arch_8.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r43 = arch_8.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_9 = arch_8.addOrReplaceChild("arch_9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r44 = arch_9.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r45 = arch_9.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r46 = arch_9.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r47 = arch_9.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r48 = arch_9.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r49 = arch_9.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r50 = arch_9.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r51 = arch_9.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r52 = arch_9.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r53 = arch_9.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r54 = arch_9.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_6 = door.addOrReplaceChild("arch_6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2F, -15.9F, -3.2F, -0.1053F, -0.0262F, 0.0399F));

		PartDefinition cube_r55 = arch_6.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r56 = arch_6.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r57 = arch_6.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r58 = arch_6.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r59 = arch_6.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch7 = arch_6.addOrReplaceChild("arch7", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, 0.014F, -0.0104F));

		PartDefinition cube_r60 = arch7.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r61 = arch7.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r62 = arch7.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r63 = arch7.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r64 = arch7.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r65 = arch7.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r66 = arch7.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r67 = arch7.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r68 = arch7.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r69 = arch7.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r70 = arch7.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_10 = door.addOrReplaceChild("arch_10", CubeListBuilder.create(), PartPose.offsetAndRotation(1.2F, -15.9F, -3.2F, -0.1053F, -0.0262F, 0.0399F));

		PartDefinition cube_r71 = arch_10.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r72 = arch_10.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r73 = arch_10.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r74 = arch_10.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.5F, -2.6F, 1.6754F, 0.004F, -0.0347F));

		PartDefinition cube_r75 = arch_10.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r76 = arch_10.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_12 = arch_10.addOrReplaceChild("arch_12", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, 0.014F, -0.0104F));

		PartDefinition cube_r77 = arch_12.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r78 = arch_12.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r79 = arch_12.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r80 = arch_12.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r81 = arch_12.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4935F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r82 = arch_12.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r83 = arch_12.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r84 = arch_12.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r85 = arch_12.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r86 = arch_12.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r87 = arch_12.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition door_3 = full_gate.addOrReplaceChild("door_3", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, -5.0F));

		PartDefinition door_right = door_3.addOrReplaceChild("door_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, -6.1F, 14.35F, 0.0F, -2.3562F, 0.0F));

		PartDefinition arch_right = door_right.addOrReplaceChild("arch_right", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -10.0F, -2.0F, 3.0117F, 0.0173F, 3.1166F));

		PartDefinition cube_r88 = arch_right.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, -0.0158F, 0.0074F));

		PartDefinition cube_r89 = arch_right.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r90 = arch_right.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r91 = arch_right.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(6, 33).mirror().addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(7, 34).mirror().addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r92 = arch_right.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5988F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, -0.0183F, 0.0165F));

		PartDefinition arch16 = arch_right.addOrReplaceChild("arch16", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r93 = arch16.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r94 = arch16.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r95 = arch16.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r96 = arch16.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r97 = arch16.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r98 = arch16.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5026F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, 0.0098F, -0.0025F));

		PartDefinition cube_r99 = arch16.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r100 = arch16.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r101 = arch16.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r102 = arch16.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r103 = arch16.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_right_1 = door_right.addOrReplaceChild("arch_right_1", CubeListBuilder.create(), PartPose.offsetAndRotation(2.4F, -10.0F, -2.0F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r104 = arch_right_1.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r105 = arch_right_1.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r106 = arch_right_1.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r107 = arch_right_1.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r108 = arch_right_1.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_22 = arch_right_1.addOrReplaceChild("arch_22", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r109 = arch_22.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r110 = arch_22.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r111 = arch_22.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r112 = arch_22.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r113 = arch_22.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r114 = arch_22.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r115 = arch_22.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r116 = arch_22.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r117 = arch_22.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r118 = arch_22.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r119 = arch_22.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_left_2 = door_right.addOrReplaceChild("arch_left_2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -10.0F, -2.0F, 3.0117F, 0.0173F, 3.1166F));

		PartDefinition cube_r120 = arch_left_2.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, -0.0158F, 0.0074F));

		PartDefinition cube_r121 = arch_left_2.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r122 = arch_left_2.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r123 = arch_left_2.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(6, 33).mirror().addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(7, 34).mirror().addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r124 = arch_left_2.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5988F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, -0.0183F, 0.0165F));

		PartDefinition arch15 = arch_left_2.addOrReplaceChild("arch15", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r125 = arch15.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r126 = arch15.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r127 = arch15.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r128 = arch15.addOrReplaceChild("cube_r128", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r129 = arch15.addOrReplaceChild("cube_r129", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r130 = arch15.addOrReplaceChild("cube_r130", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5026F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, 0.0098F, -0.0025F));

		PartDefinition cube_r131 = arch15.addOrReplaceChild("cube_r131", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r132 = arch15.addOrReplaceChild("cube_r132", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r133 = arch15.addOrReplaceChild("cube_r133", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r134 = arch15.addOrReplaceChild("cube_r134", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r135 = arch15.addOrReplaceChild("cube_r135", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_right_2 = door_right.addOrReplaceChild("arch_right_2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.4F, -10.0F, -2.0F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r136 = arch_right_2.addOrReplaceChild("cube_r136", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r137 = arch_right_2.addOrReplaceChild("cube_r137", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r138 = arch_right_2.addOrReplaceChild("cube_r138", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r139 = arch_right_2.addOrReplaceChild("cube_r139", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r140 = arch_right_2.addOrReplaceChild("cube_r140", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch20 = arch_right_2.addOrReplaceChild("arch20", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r141 = arch20.addOrReplaceChild("cube_r141", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r142 = arch20.addOrReplaceChild("cube_r142", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r143 = arch20.addOrReplaceChild("cube_r143", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r144 = arch20.addOrReplaceChild("cube_r144", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r145 = arch20.addOrReplaceChild("cube_r145", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r146 = arch20.addOrReplaceChild("cube_r146", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r147 = arch20.addOrReplaceChild("cube_r147", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r148 = arch20.addOrReplaceChild("cube_r148", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r149 = arch20.addOrReplaceChild("cube_r149", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r150 = arch20.addOrReplaceChild("cube_r150", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r151 = arch20.addOrReplaceChild("cube_r151", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_left_3_2 = door_right.addOrReplaceChild("arch_left_3_2", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -10.0F, -2.0F, 3.0117F, 0.0173F, 3.1166F));

		PartDefinition cube_r152 = arch_left_3_2.addOrReplaceChild("cube_r152", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, -0.0158F, 0.0074F));

		PartDefinition cube_r153 = arch_left_3_2.addOrReplaceChild("cube_r153", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r154 = arch_left_3_2.addOrReplaceChild("cube_r154", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r155 = arch_left_3_2.addOrReplaceChild("cube_r155", CubeListBuilder.create().texOffs(6, 33).mirror().addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(7, 34).mirror().addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r156 = arch_left_3_2.addOrReplaceChild("cube_r156", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5988F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, -0.0183F, 0.0165F));

		PartDefinition arch_left_3_1 = arch_left_3_2.addOrReplaceChild("arch_left_3_1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r157 = arch_left_3_1.addOrReplaceChild("cube_r157", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r158 = arch_left_3_1.addOrReplaceChild("cube_r158", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r159 = arch_left_3_1.addOrReplaceChild("cube_r159", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r160 = arch_left_3_1.addOrReplaceChild("cube_r160", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r161 = arch_left_3_1.addOrReplaceChild("cube_r161", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r162 = arch_left_3_1.addOrReplaceChild("cube_r162", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5026F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, 0.0098F, -0.0025F));

		PartDefinition cube_r163 = arch_left_3_1.addOrReplaceChild("cube_r163", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r164 = arch_left_3_1.addOrReplaceChild("cube_r164", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r165 = arch_left_3_1.addOrReplaceChild("cube_r165", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r166 = arch_left_3_1.addOrReplaceChild("cube_r166", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r167 = arch_left_3_1.addOrReplaceChild("cube_r167", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch_right_3_2 = door_right.addOrReplaceChild("arch_right_3_2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.4F, -10.0F, -2.0F, 3.0117F, -0.0173F, -3.1166F));

		PartDefinition cube_r168 = arch_right_3_2.addOrReplaceChild("cube_r168", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r169 = arch_right_3_2.addOrReplaceChild("cube_r169", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r170 = arch_right_3_2.addOrReplaceChild("cube_r170", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r171 = arch_right_3_2.addOrReplaceChild("cube_r171", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r172 = arch_right_3_2.addOrReplaceChild("cube_r172", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch_right_3_1 = arch_right_3_2.addOrReplaceChild("arch_right_3_1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.637F, 0.0F, 0.0F));

		PartDefinition cube_r173 = arch_right_3_1.addOrReplaceChild("cube_r173", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r174 = arch_right_3_1.addOrReplaceChild("cube_r174", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r175 = arch_right_3_1.addOrReplaceChild("cube_r175", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r176 = arch_right_3_1.addOrReplaceChild("cube_r176", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r177 = arch_right_3_1.addOrReplaceChild("cube_r177", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r178 = arch_right_3_1.addOrReplaceChild("cube_r178", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1613F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r179 = arch_right_3_1.addOrReplaceChild("cube_r179", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r180 = arch_right_3_1.addOrReplaceChild("cube_r180", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r181 = arch_right_3_1.addOrReplaceChild("cube_r181", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r182 = arch_right_3_1.addOrReplaceChild("cube_r182", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r183 = arch_right_3_1.addOrReplaceChild("cube_r183", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition door5 = door_right.addOrReplaceChild("door5", CubeListBuilder.create().texOffs(22, 40).mirror().addBox(-1.5F, -9.0F, 4.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 54).mirror().addBox(-1.5F, 8.0F, -5.0F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).mirror().addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).mirror().addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.2F, 6.1F, -5.1F));

		PartDefinition cube_r184 = door5.addOrReplaceChild("cube_r184", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -13.7F, -5.0F, 1.0F, 8.7F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -19.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r185 = door5.addOrReplaceChild("cube_r185", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -13.7F, -5.0F, 1.0F, 4.7F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -21.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r186 = door5.addOrReplaceChild("cube_r186", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -14.399F, -5.0F, 1.0F, 7.399F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -20.0F, 9.4F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r187 = door5.addOrReplaceChild("cube_r187", CubeListBuilder.create().texOffs(7, 7).mirror().addBox(-0.5F, -13.499F, -5.0F, 1.0F, 9.499F, 3.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.0F, 8.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition door4 = door_right.addOrReplaceChild("door4", CubeListBuilder.create().texOffs(22, 40).addBox(0.5F, -9.0F, 4.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 54).addBox(0.5F, 8.0F, -5.0F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, -11.0F, -5.0F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, 6.1F, -5.1F));

		PartDefinition cube_r188 = door4.addOrReplaceChild("cube_r188", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -13.7F, -5.0F, 1.0F, 8.7F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -19.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r189 = door4.addOrReplaceChild("cube_r189", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -13.7F, -5.0F, 1.0F, 4.7F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -21.0F, 8.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r190 = door4.addOrReplaceChild("cube_r190", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -14.399F, -5.0F, 1.0F, 7.399F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -20.0F, 9.4F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r191 = door4.addOrReplaceChild("cube_r191", CubeListBuilder.create().texOffs(7, 7).addBox(-0.5F, -13.499F, -5.0F, 1.0F, 9.499F, 3.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -16.0F, 8.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition door_left = door_3.addOrReplaceChild("door_left", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.55F, -5.9F, -4.6F, 0.0F, 2.3562F, 0.0F));

		PartDefinition start_of_left_door = door_left.addOrReplaceChild("start_of_left_door", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -10.0F, 2.0F, -0.1053F, -0.0262F, 0.0399F));

		PartDefinition cube_r192 = start_of_left_door.addOrReplaceChild("cube_r192", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r193 = start_of_left_door.addOrReplaceChild("cube_r193", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r194 = start_of_left_door.addOrReplaceChild("cube_r194", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r195 = start_of_left_door.addOrReplaceChild("cube_r195", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r196 = start_of_left_door.addOrReplaceChild("cube_r196", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch24 = start_of_left_door.addOrReplaceChild("arch24", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, 0.014F, -0.0104F));

		PartDefinition cube_r197 = arch24.addOrReplaceChild("cube_r197", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r198 = arch24.addOrReplaceChild("cube_r198", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r199 = arch24.addOrReplaceChild("cube_r199", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r200 = arch24.addOrReplaceChild("cube_r200", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r201 = arch24.addOrReplaceChild("cube_r201", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r202 = arch24.addOrReplaceChild("cube_r202", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r203 = arch24.addOrReplaceChild("cube_r203", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r204 = arch24.addOrReplaceChild("cube_r204", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r205 = arch24.addOrReplaceChild("cube_r205", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r206 = arch24.addOrReplaceChild("cube_r206", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r207 = arch24.addOrReplaceChild("cube_r207", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition bone2 = door_left.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.5F, 28.0F, -23.4F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(18, 27).mirror().addBox(-1.5F, 11.0F, -24.4F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).mirror().addBox(-0.5F, 9.0F, -24.4F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.8F, -14.1F, 24.6F));

		PartDefinition cube_r208 = bone2.addOrReplaceChild("cube_r208", CubeListBuilder.create().texOffs(7, 7).mirror().addBox(-0.5F, -22.501F, -5.0F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 4.0F, -0.9F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r209 = bone2.addOrReplaceChild("cube_r209", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -18.0F, -5.0F, 1.0F, 4.3F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0F, -0.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r210 = bone2.addOrReplaceChild("cube_r210", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -22.0F, -5.0F, 1.0F, 8.3F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0F, -0.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r211 = bone2.addOrReplaceChild("cube_r211", CubeListBuilder.create().texOffs(9, 9).mirror().addBox(-0.5F, -21.401F, -5.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition bone = door_left.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 54).addBox(0.5F, 28.0F, -23.4F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(18, 27).addBox(0.5F, 11.0F, -24.4F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, 9.0F, -24.4F, 1.0F, 20.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.8F, -14.1F, 24.6F));

		PartDefinition cube_r212 = bone.addOrReplaceChild("cube_r212", CubeListBuilder.create().texOffs(7, 7).addBox(-0.5F, -22.501F, -5.0F, 1.0F, 9.0F, 3.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 4.0F, -0.9F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r213 = bone.addOrReplaceChild("cube_r213", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -18.0F, -5.0F, 1.0F, 4.3F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -0.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r214 = bone.addOrReplaceChild("cube_r214", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -22.0F, -5.0F, 1.0F, 8.3F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.7F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r215 = bone.addOrReplaceChild("cube_r215", CubeListBuilder.create().texOffs(9, 9).addBox(-0.5F, -21.401F, -5.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition arch17 = door_left.addOrReplaceChild("arch17", CubeListBuilder.create(), PartPose.offsetAndRotation(0.6F, -10.0F, 2.0F, -0.1053F, 0.0262F, -0.0399F));

		PartDefinition cube_r216 = arch17.addOrReplaceChild("cube_r216", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, -0.0158F, 0.0074F));

		PartDefinition cube_r217 = arch17.addOrReplaceChild("cube_r217", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r218 = arch17.addOrReplaceChild("cube_r218", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r219 = arch17.addOrReplaceChild("cube_r219", CubeListBuilder.create().texOffs(6, 33).mirror().addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.2F, 0.5F, -2.6F, 1.6754F, -0.004F, 0.0347F));

		PartDefinition cube_r220 = arch17.addOrReplaceChild("cube_r220", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r221 = arch17.addOrReplaceChild("cube_r221", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5988F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, -0.0183F, 0.0165F));

		PartDefinition arch18 = arch17.addOrReplaceChild("arch18", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, -0.014F, 0.0104F));

		PartDefinition cube_r222 = arch18.addOrReplaceChild("cube_r222", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r223 = arch18.addOrReplaceChild("cube_r223", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r224 = arch18.addOrReplaceChild("cube_r224", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r225 = arch18.addOrReplaceChild("cube_r225", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r226 = arch18.addOrReplaceChild("cube_r226", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r227 = arch18.addOrReplaceChild("cube_r227", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5026F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, 0.0098F, -0.0025F));

		PartDefinition cube_r228 = arch18.addOrReplaceChild("cube_r228", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r229 = arch18.addOrReplaceChild("cube_r229", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r230 = arch18.addOrReplaceChild("cube_r230", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r231 = arch18.addOrReplaceChild("cube_r231", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r232 = arch18.addOrReplaceChild("cube_r232", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition arch25 = door_left.addOrReplaceChild("arch25", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -10.0F, 2.0F, -0.1053F, -0.0262F, 0.0399F));

		PartDefinition cube_r233 = arch25.addOrReplaceChild("cube_r233", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, 0.0158F, -0.0074F));

		PartDefinition cube_r234 = arch25.addOrReplaceChild("cube_r234", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r235 = arch25.addOrReplaceChild("cube_r235", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r236 = arch25.addOrReplaceChild("cube_r236", CubeListBuilder.create().texOffs(6, 33).addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 0.5F, -2.6F, 1.6754F, 0.004F, -0.0347F));

		PartDefinition cube_r237 = arch25.addOrReplaceChild("cube_r237", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r238 = arch25.addOrReplaceChild("cube_r238", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4012F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, 0.0183F, -0.0165F));

		PartDefinition arch26 = arch25.addOrReplaceChild("arch26", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, 0.014F, -0.0104F));

		PartDefinition cube_r239 = arch26.addOrReplaceChild("cube_r239", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r240 = arch26.addOrReplaceChild("cube_r240", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, -0.0098F, 0.0025F));

		PartDefinition cube_r241 = arch26.addOrReplaceChild("cube_r241", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r242 = arch26.addOrReplaceChild("cube_r242", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r243 = arch26.addOrReplaceChild("cube_r243", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4936F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, -0.0098F, 0.0025F));

		PartDefinition cube_r244 = arch26.addOrReplaceChild("cube_r244", CubeListBuilder.create().texOffs(7, 34).addBox(-0.4974F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, -0.0098F, 0.0025F));

		PartDefinition cube_r245 = arch26.addOrReplaceChild("cube_r245", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r246 = arch26.addOrReplaceChild("cube_r246", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r247 = arch26.addOrReplaceChild("cube_r247", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r248 = arch26.addOrReplaceChild("cube_r248", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r249 = arch26.addOrReplaceChild("cube_r249", CubeListBuilder.create().texOffs(7, 34).addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition left = door_left.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offsetAndRotation(1.6F, -10.0F, 2.0F, -0.1053F, 0.0262F, -0.0399F));

		PartDefinition cube_r250 = left.addOrReplaceChild("cube_r250", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.7237F, -4.0611F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1344F, -0.0158F, 0.0074F));

		PartDefinition cube_r251 = left.addOrReplaceChild("cube_r251", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.9076F, -3.9617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r252 = left.addOrReplaceChild("cube_r252", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0225F, -3.9139F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.4661F, 0.0F, 0.0F));

		PartDefinition cube_r253 = left.addOrReplaceChild("cube_r253", CubeListBuilder.create().texOffs(6, 33).mirror().addBox(-0.5F, 0.828F, -6.9523F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(7, 34).mirror().addBox(-0.5F, 0.828F, -4.9523F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -2.7F, 1.7017F, 0.0F, 0.0F));

		PartDefinition cube_r254 = left.addOrReplaceChild("cube_r254", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5988F, -8.8316F, -3.5734F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, 7.3F, 1.6229F, -0.0183F, 0.0165F));

		PartDefinition arch19 = left.addOrReplaceChild("arch19", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.1F, 3.2F, -0.6371F, -0.014F, 0.0104F));

		PartDefinition cube_r255 = arch19.addOrReplaceChild("cube_r255", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2617F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8F, 3.6F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r256 = arch19.addOrReplaceChild("cube_r256", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.2F, 3.0F, 0.8112F, 0.0098F, -0.0025F));

		PartDefinition cube_r257 = arch19.addOrReplaceChild("cube_r257", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 2.3F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r258 = arch19.addOrReplaceChild("cube_r258", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.8F, 1.7F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r259 = arch19.addOrReplaceChild("cube_r259", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5064F, -0.3884F, -1.2616F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8461F, 0.0098F, -0.0025F));

		PartDefinition cube_r260 = arch19.addOrReplaceChild("cube_r260", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5026F, -0.411F, -2.1612F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 0.8984F, 0.0098F, -0.0025F));

		PartDefinition cube_r261 = arch19.addOrReplaceChild("cube_r261", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -0.6146F, -3.0625F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.0123F, 0.0F, 0.0F));

		PartDefinition cube_r262 = arch19.addOrReplaceChild("cube_r262", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.084F, -3.9454F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.2F, 1.2F, 1.1781F, 0.0F, 0.0F));

		PartDefinition cube_r263 = arch19.addOrReplaceChild("cube_r263", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0814F, -3.9419F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 1.309F, 0.0F, 0.0F));

		PartDefinition cube_r264 = arch19.addOrReplaceChild("cube_r264", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, -1.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.3963F, 0.0F, 0.0F));

		PartDefinition cube_r265 = arch19.addOrReplaceChild("cube_r265", CubeListBuilder.create().texOffs(7, 34).mirror().addBox(-0.5F, 0.0707F, -4.0707F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.5F, -1.7F, 1.5533F, 0.0F, 0.0F));

		PartDefinition tentacles = partdefinition.addOrReplaceChild("tentacles", CubeListBuilder.create(), PartPose.offset(-0.4954F, 8.9344F, -7.4028F));

		PartDefinition tentacle_1 = tentacles.addOrReplaceChild("tentacle_1", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9286F, 0.5F, 0.0F, -0.3927F, 0.0F, -0.48F));

		PartDefinition cube_r266 = tentacle_1.addOrReplaceChild("cube_r266", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.201F, 8.4906F, -12.9472F, 0.0382F, 0.6645F, -0.0495F));

		PartDefinition cube_r267 = tentacle_1.addOrReplaceChild("cube_r267", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r268 = tentacle_1.addOrReplaceChild("cube_r268", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r269 = tentacle_1.addOrReplaceChild("cube_r269", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r270 = tentacle_1.addOrReplaceChild("cube_r270", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r271 = tentacle_1.addOrReplaceChild("cube_r271", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_5 = tentacles.addOrReplaceChild("tentacle_5", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9286F, -4.5F, 0.0F, -0.9163F, 0.0F, -0.48F));

		PartDefinition cube_r272 = tentacle_5.addOrReplaceChild("cube_r272", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.201F, 8.4906F, -12.9472F, 0.0382F, 0.6645F, -0.0495F));

		PartDefinition cube_r273 = tentacle_5.addOrReplaceChild("cube_r273", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r274 = tentacle_5.addOrReplaceChild("cube_r274", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r275 = tentacle_5.addOrReplaceChild("cube_r275", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r276 = tentacle_5.addOrReplaceChild("cube_r276", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r277 = tentacle_5.addOrReplaceChild("cube_r277", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_6 = tentacles.addOrReplaceChild("tentacle_6", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0714F, -4.5F, 0.0F, -1.1562F, -0.5221F, 0.3677F));

		PartDefinition cube_r278 = tentacle_6.addOrReplaceChild("cube_r278", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.201F, 8.4906F, -12.9472F, 0.0382F, 0.6645F, -0.0495F));

		PartDefinition cube_r279 = tentacle_6.addOrReplaceChild("cube_r279", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r280 = tentacle_6.addOrReplaceChild("cube_r280", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r281 = tentacle_6.addOrReplaceChild("cube_r281", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r282 = tentacle_6.addOrReplaceChild("cube_r282", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r283 = tentacle_6.addOrReplaceChild("cube_r283", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_7 = tentacles.addOrReplaceChild("tentacle_7", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4286F, 3.5F, 0.0F, -0.1309F, 0.0F, -0.48F));

		PartDefinition cube_r284 = tentacle_7.addOrReplaceChild("cube_r284", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.201F, 8.4906F, -12.9472F, 0.0382F, 0.6645F, -0.0495F));

		PartDefinition cube_r285 = tentacle_7.addOrReplaceChild("cube_r285", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r286 = tentacle_7.addOrReplaceChild("cube_r286", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r287 = tentacle_7.addOrReplaceChild("cube_r287", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r288 = tentacle_7.addOrReplaceChild("cube_r288", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r289 = tentacle_7.addOrReplaceChild("cube_r289", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_4 = tentacles.addOrReplaceChild("tentacle_4", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4286F, 3.5F, 0.0F, -0.5236F, 0.0F, 2.8798F));

		PartDefinition cube_r290 = tentacle_4.addOrReplaceChild("cube_r290", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.201F, 8.4906F, -12.9472F, 0.0382F, 0.6645F, -0.0495F));

		PartDefinition cube_r291 = tentacle_4.addOrReplaceChild("cube_r291", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r292 = tentacle_4.addOrReplaceChild("cube_r292", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r293 = tentacle_4.addOrReplaceChild("cube_r293", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r294 = tentacle_4.addOrReplaceChild("cube_r294", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r295 = tentacle_4.addOrReplaceChild("cube_r295", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_2 = tentacles.addOrReplaceChild("tentacle_2", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5714F, -0.75F, 0.0F, -0.3927F, -0.3927F, -0.48F));

		PartDefinition cube_r296 = tentacle_2.addOrReplaceChild("cube_r296", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r297 = tentacle_2.addOrReplaceChild("cube_r297", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r298 = tentacle_2.addOrReplaceChild("cube_r298", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r299 = tentacle_2.addOrReplaceChild("cube_r299", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r300 = tentacle_2.addOrReplaceChild("cube_r300", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		PartDefinition tentacle_3 = tentacles.addOrReplaceChild("tentacle_3", CubeListBuilder.create().texOffs(33, 37).addBox(0.674F, -2.9344F, 3.9028F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0714F, 2.25F, 0.0F, -0.4342F, 0.3829F, 2.8971F));

		PartDefinition cube_r301 = tentacle_3.addOrReplaceChild("cube_r301", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.276F, 6.8156F, -8.1972F, 0.4239F, 0.272F, -0.0647F));

		PartDefinition cube_r302 = tentacle_3.addOrReplaceChild("cube_r302", CubeListBuilder.create().texOffs(31, 35).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.576F, 3.0656F, -4.9972F, 1.0784F, 0.272F, -0.0647F));

		PartDefinition cube_r303 = tentacle_3.addOrReplaceChild("cube_r303", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.776F, 0.8156F, -1.7472F, 0.3366F, 0.272F, -0.0647F));

		PartDefinition cube_r304 = tentacle_3.addOrReplaceChild("cube_r304", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.174F, -1.5094F, 0.6528F, 0.7854F, 0.3927F, 0.0F));

		PartDefinition cube_r305 = tentacle_3.addOrReplaceChild("cube_r305", CubeListBuilder.create().texOffs(33, 37).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.424F, -1.9344F, 3.4028F, 0.0F, 0.3927F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(UnderworldGateEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.openAnimationState, UnderworldGateAnimations.open, ageInTicks, 1.0F);
		this.animate(entity.tentacleAnimationState, UnderworldGateAnimations.tentacles, ageInTicks, 1.0F);
	}

	@Override
	public @NotNull ModelPart root() {
		return root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		full_gate.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		tentacles.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}