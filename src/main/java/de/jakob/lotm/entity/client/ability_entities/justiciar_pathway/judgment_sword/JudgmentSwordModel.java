package de.jakob.lotm.entity.client.ability_entities.justiciar_pathway.judgment_sword;

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

public class JudgmentSwordModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "judgment_sword"), "main");

    private final ModelPart bb_main;

    public JudgmentSwordModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        // ── Core sword (same proportions as JusticeSword) ────────────────────
        PartDefinition bb_main = part.addOrReplaceChild("bb_main", CubeListBuilder.create()
                // Pommel cap (bottom nub)
                .texOffs(34, 36).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                // Grip adapter
                .texOffs(30, 14).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                // Main blade body
                .texOffs(0, 0).addBox(-1.0F, -49.0F, -3.0F, 2.0F, 43.0F, 6.0F, new CubeDeformation(0.0F))
                // Handle / grip
                .texOffs(1, 49).addBox(-2.0F, -63.0F, -2.5F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
                // Pommel block
                .texOffs(34, 50).addBox(-4.0F, -70.0F, -3.5F, 8.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))

                // ── Scales of Justice ─────────────────────────────────────────
                // Balance beam: wide horizontal bar extending left-right at guard level
                .texOffs(64, 0).addBox(-10.0F, -55.0F, -1.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                // Left pan chain (thin vertical connector)
                .texOffs(64, 4).addBox(-10.5F, -53.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                // Right pan chain
                .texOffs(64, 4).addBox(9.5F, -53.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                // Left scale pan (flat plate)
                .texOffs(64, 6).addBox(-13.0F, -48.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                // Right scale pan (flat plate)
                .texOffs(64, 6).addBox(7.0F, -48.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),

                PartPose.offset(0.0F, 24.0F, 0.0F));

        // ── Traditional cross-guard (same as JusticeSword, extends in Z) ─────
        bb_main.addOrReplaceChild("guard_end",
                CubeListBuilder.create()
                        .texOffs(48, 26).addBox(-2.0F, 8.5F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -52.0F, -21.0F, 1.5708F, 0.0F, 0.0F));

        bb_main.addOrReplaceChild("guard_bar",
                CubeListBuilder.create()
                        .texOffs(48, 36).addBox(-2.0F, 9.5F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(46, 1).addBox(-2.0F, -9.5F, -3.0F, 4.0F, 19.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -52.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
