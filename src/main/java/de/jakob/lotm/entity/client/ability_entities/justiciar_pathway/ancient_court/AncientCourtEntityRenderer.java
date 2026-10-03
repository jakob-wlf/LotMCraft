package de.jakob.lotm.entity.client.ability_entities.justiciar_pathway.ancient_court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class AncientCourtEntityRenderer extends EntityRenderer<AncientCourtEntity> {

    // Visual court is 25 blocks — large enough to feel grand, small enough to see fully
    private static final float COURT_RADIUS = 25f;
    private static final float PILLAR_HEIGHT = 18f;
    private static final float PILLAR_W = 0.8f;  // half-width — thick visible pillars

    private static final float GOLD_R  = 1.00f, GOLD_G  = 0.80f, GOLD_B  = 0.10f;
    private static final float GOLD2_R = 1.00f, GOLD2_G = 0.92f, GOLD2_B = 0.45f;
    private static final float WHITE_R = 1.00f, WHITE_G = 1.00f, WHITE_B = 0.85f;

    public AncientCourtEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AncientCourtEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);

        float tick = entity.tickCount + partialTicks;
        float alpha = Mth.clamp(tick / 40f, 0f, 1f);
        if (alpha >= 1f) alpha = 0.88f + 0.12f * Mth.sin(tick * 0.04f);
        if (alpha < 0.01f) return;

        poseStack.pushPose();

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f m = poseStack.last().pose();

        renderFloor(consumer, m, alpha);
        renderPillars(consumer, m, tick, alpha);
        renderWallSegments(consumer, m, alpha);
        renderTopBeams(consumer, m, alpha);
        renderCentralAltar(consumer, m, tick, alpha);
        renderDomeRings(consumer, m, tick, alpha);
        renderRuneOrbit(consumer, m, tick, alpha);

        poseStack.popPose();
    }

    // ── Floor: concentric rings + radial lines ────────────────────────────────

    private void renderFloor(VertexConsumer consumer, Matrix4f m, float alpha) {
        float floorAlpha = alpha * 0.55f;

        // Three concentric rings
        float[] radii = { COURT_RADIUS * 0.35f, COURT_RADIUS * 0.70f, COURT_RADIUS };
        for (float r : radii) {
            int seg = 64;
            float inner = r - 0.4f, outer = r + 0.4f;
            for (int i = 0; i < seg; i++) {
                float a1 = (i / (float) seg) * Mth.TWO_PI;
                float a2 = ((i + 1) / (float) seg) * Mth.TWO_PI;
                addVertex(consumer, m, Mth.cos(a1) * inner, 0.02f, Mth.sin(a1) * inner, GOLD_R, GOLD_G, GOLD_B, floorAlpha);
                addVertex(consumer, m, Mth.cos(a2) * inner, 0.02f, Mth.sin(a2) * inner, GOLD_R, GOLD_G, GOLD_B, floorAlpha);
                addVertex(consumer, m, Mth.cos(a2) * outer, 0.02f, Mth.sin(a2) * outer, GOLD2_R, GOLD2_G, GOLD2_B, floorAlpha * 0.3f);
                addVertex(consumer, m, Mth.cos(a1) * outer, 0.02f, Mth.sin(a1) * outer, GOLD2_R, GOLD2_G, GOLD2_B, floorAlpha * 0.3f);
            }
        }

        // 8 radial lines from inner ring to outer edge
        float lineW = 0.15f;
        for (int i = 0; i < 8; i++) {
            float angle = (i / 8f) * Mth.TWO_PI;
            float ex = Mth.cos(angle) * COURT_RADIUS;
            float ez = Mth.sin(angle) * COURT_RADIUS;
            float px = -Mth.sin(angle) * lineW;
            float pz =  Mth.cos(angle) * lineW;
            addVertex(consumer, m,  px, 0.03f,  pz, GOLD_R, GOLD_G, GOLD_B, floorAlpha * 0.8f);
            addVertex(consumer, m, -px, 0.03f, -pz, GOLD_R, GOLD_G, GOLD_B, floorAlpha * 0.8f);
            addVertex(consumer, m, ex - px, 0.03f, ez - pz, GOLD_R, GOLD_G, GOLD_B, 0f);
            addVertex(consumer, m, ex + px, 0.03f, ez + pz, GOLD_R, GOLD_G, GOLD_B, 0f);
        }
    }

    // ── 8 cross-shaped pillars at the court boundary ─────────────────────────

    private void renderPillars(VertexConsumer consumer, Matrix4f m, float tick, float alpha) {
        for (int i = 0; i < 8; i++) {
            float angle = (i / 8f) * Mth.TWO_PI;
            float cx = Mth.cos(angle) * COURT_RADIUS;
            float cz = Mth.sin(angle) * COURT_RADIUS;

            // Radial face
            float rx = Mth.cos(angle) * PILLAR_W;
            float rz = Mth.sin(angle) * PILLAR_W;
            // Tangential face
            float tx = -Mth.sin(angle) * PILLAR_W;
            float tz =  Mth.cos(angle) * PILLAR_W;

            // Radial quad (faces left/right along ring)
            renderPillarFace(consumer, m, cx - tx, cz - tz, cx + tx, cz + tz, PILLAR_HEIGHT, alpha);
            // Tangential quad (faces inward/outward)
            renderPillarFace(consumer, m, cx - rx, cz - rz, cx + rx, cz + rz, PILLAR_HEIGHT, alpha);

            // Pillar cap rings at 3 heights
            renderRing(consumer, m, cx, cz, 3f * PILLAR_H(0.20f), 1.3f, alpha * 0.9f, WHITE_R, WHITE_G, WHITE_B);
            renderRing(consumer, m, cx, cz, 3f * PILLAR_H(0.50f), 1.3f, alpha * 0.7f, WHITE_R, WHITE_G, WHITE_B);
            renderRing(consumer, m, cx, cz, 3f * PILLAR_H(0.90f), 1.1f, alpha * 0.5f, GOLD_R, GOLD_G, GOLD_B);

            // Animated glow pulse at pillar base
            float pulse = 0.4f + 0.6f * (Mth.sin(tick * 0.06f + i * 0.78f) * 0.5f + 0.5f);
            renderRing(consumer, m, cx, cz, 0.3f, 1.8f, alpha * pulse * 0.6f, GOLD2_R, GOLD2_G, GOLD2_B);
        }
    }

    private float PILLAR_H(float fraction) { return PILLAR_HEIGHT * fraction; }

    private void renderPillarFace(VertexConsumer consumer, Matrix4f m,
                                   float x1, float z1, float x2, float z2,
                                   float height, float alpha) {
        addVertex(consumer, m, x1, 0f,      z1, GOLD_R,  GOLD_G,  GOLD_B,  alpha);
        addVertex(consumer, m, x2, 0f,      z2, GOLD_R,  GOLD_G,  GOLD_B,  alpha);
        addVertex(consumer, m, x2, height,  z2, GOLD2_R, GOLD2_G, GOLD2_B, alpha * 0.1f);
        addVertex(consumer, m, x1, height,  z1, GOLD2_R, GOLD2_G, GOLD2_B, alpha * 0.1f);
    }

    // ── Low wall segments between pillars ────────────────────────────────────

    private void renderWallSegments(VertexConsumer consumer, Matrix4f m, float alpha) {
        float wallH = 3.5f;
        float wallAlpha = alpha * 0.35f;
        int seg = 12; // geometry per span

        for (int i = 0; i < 8; i++) {
            float a1 = (i / 8f) * Mth.TWO_PI;
            float a2 = ((i + 1) / 8f) * Mth.TWO_PI;

            for (int j = 0; j < seg; j++) {
                float t1 = a1 + (a2 - a1) * (j / (float) seg);
                float t2 = a1 + (a2 - a1) * ((j + 1) / (float) seg);
                float inner = COURT_RADIUS - 0.4f;
                float outer = COURT_RADIUS + 0.4f;

                float ix1 = Mth.cos(t1) * inner, iz1 = Mth.sin(t1) * inner;
                float ix2 = Mth.cos(t2) * inner, iz2 = Mth.sin(t2) * inner;
                float ox1 = Mth.cos(t1) * outer, oz1 = Mth.sin(t1) * outer;
                float ox2 = Mth.cos(t2) * outer, oz2 = Mth.sin(t2) * outer;

                // Inner face of wall
                addVertex(consumer, m, ix1, 0f,    iz1, GOLD_R, GOLD_G, GOLD_B, wallAlpha);
                addVertex(consumer, m, ix2, 0f,    iz2, GOLD_R, GOLD_G, GOLD_B, wallAlpha);
                addVertex(consumer, m, ix2, wallH, iz2, GOLD_R, GOLD_G, GOLD_B, 0f);
                addVertex(consumer, m, ix1, wallH, iz1, GOLD_R, GOLD_G, GOLD_B, 0f);

                // Outer face
                addVertex(consumer, m, ox2, 0f,    oz2, GOLD_R, GOLD_G, GOLD_B, wallAlpha * 0.5f);
                addVertex(consumer, m, ox1, 0f,    oz1, GOLD_R, GOLD_G, GOLD_B, wallAlpha * 0.5f);
                addVertex(consumer, m, ox1, wallH, oz1, GOLD_R, GOLD_G, GOLD_B, 0f);
                addVertex(consumer, m, ox2, wallH, oz2, GOLD_R, GOLD_G, GOLD_B, 0f);
            }
        }
    }

    // ── Horizontal beams connecting pillar tops ───────────────────────────────

    private void renderTopBeams(VertexConsumer consumer, Matrix4f m, float alpha) {
        float beamH = PILLAR_HEIGHT * 0.88f;
        float beamThick = 0.3f;
        float beamAlpha = alpha * 0.6f;

        for (int i = 0; i < 8; i++) {
            float a1 = (i / 8f) * Mth.TWO_PI;
            float a2 = ((i + 1) / 8f) * Mth.TWO_PI;
            float x1 = Mth.cos(a1) * COURT_RADIUS;
            float z1 = Mth.sin(a1) * COURT_RADIUS;
            float x2 = Mth.cos(a2) * COURT_RADIUS;
            float z2 = Mth.sin(a2) * COURT_RADIUS;

            // Vertical thickness of beam
            addVertex(consumer, m, x1, beamH,             z1, GOLD2_R, GOLD2_G, GOLD2_B, beamAlpha);
            addVertex(consumer, m, x2, beamH,             z2, GOLD2_R, GOLD2_G, GOLD2_B, beamAlpha);
            addVertex(consumer, m, x2, beamH + beamThick, z2, GOLD_R,  GOLD_G,  GOLD_B,  beamAlpha * 0.4f);
            addVertex(consumer, m, x1, beamH + beamThick, z1, GOLD_R,  GOLD_G,  GOLD_B,  beamAlpha * 0.4f);
        }
    }

    // ── Central altar / throne ────────────────────────────────────────────────

    private void renderCentralAltar(VertexConsumer consumer, Matrix4f m, float tick, float alpha) {
        float pulse = 0.75f + 0.25f * Mth.sin(tick * 0.07f);

        // Altar base — wide flat platform
        renderRing(consumer, m, 0f, 0f, 0.05f, 4.5f, alpha * 0.65f, GOLD_R, GOLD_G, GOLD_B);
        renderRing(consumer, m, 0f, 0f, 0.10f, 3.0f, alpha * 0.50f, WHITE_R, WHITE_G, WHITE_B);
        renderRing(consumer, m, 0f, 0f, 0.15f, 1.5f, alpha * 0.40f, GOLD2_R, GOLD2_G, GOLD2_B);

        // Central beacon column — 4-sided, tall
        float w = 0.22f;
        float colH = 14f;
        for (int side = 0; side < 4; side++) {
            float a1 = (side / 4f) * Mth.TWO_PI;
            float a2 = ((side + 1) / 4f) * Mth.TWO_PI;
            float x1 = Mth.cos(a1) * w, z1 = Mth.sin(a1) * w;
            float x2 = Mth.cos(a2) * w, z2 = Mth.sin(a2) * w;
            addVertex(consumer, m, x1, 0f,   z1, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, m, x2, 0f,   z2, WHITE_R, WHITE_G, WHITE_B, alpha);
            addVertex(consumer, m, x2, colH, z2, GOLD_R,  GOLD_G,  GOLD_B,  0f);
            addVertex(consumer, m, x1, colH, z1, GOLD_R,  GOLD_G,  GOLD_B,  0f);
        }

        // Pulsing crown ring at top of column
        renderRing(consumer, m, 0f, 0f, colH, 1.8f, alpha * pulse * 0.9f, GOLD_R, GOLD_G, GOLD_B);
        renderRing(consumer, m, 0f, 0f, colH * 0.6f, 1.2f, alpha * 0.5f, WHITE_R, WHITE_G, WHITE_B);
    }

    // ── Slowly rotating dome rings overhead ───────────────────────────────────

    private void renderDomeRings(VertexConsumer consumer, Matrix4f m, float tick, float alpha) {
        float[] heights   = { 8f,  14f, 20f };
        float[] radii_    = { COURT_RADIUS * 0.85f, COURT_RADIUS * 0.60f, COURT_RADIUS * 0.30f };
        float[] rotSpeeds = { 0.003f, -0.005f, 0.008f };

        for (int r = 0; r < 3; r++) {
            float offset = tick * rotSpeeds[r];
            int seg = 48;
            float radius = radii_[r];
            float thick = 0.5f;
            float domeAlpha = alpha * (0.5f - r * 0.1f);

            for (int i = 0; i < seg; i++) {
                float a1 = offset + (i / (float) seg) * Mth.TWO_PI;
                float a2 = offset + ((i + 1) / (float) seg) * Mth.TWO_PI;
                float inner = radius - thick;
                float outer = radius + thick;

                addVertex(consumer, m, Mth.cos(a1) * inner, heights[r], Mth.sin(a1) * inner, GOLD_R, GOLD_G, GOLD_B, domeAlpha);
                addVertex(consumer, m, Mth.cos(a2) * inner, heights[r], Mth.sin(a2) * inner, GOLD_R, GOLD_G, GOLD_B, domeAlpha);
                addVertex(consumer, m, Mth.cos(a2) * outer, heights[r], Mth.sin(a2) * outer, GOLD2_R, GOLD2_G, GOLD2_B, domeAlpha * 0.2f);
                addVertex(consumer, m, Mth.cos(a1) * outer, heights[r], Mth.sin(a1) * outer, GOLD2_R, GOLD2_G, GOLD2_B, domeAlpha * 0.2f);
            }
        }
    }

    // ── 24 runes orbiting low around the perimeter ───────────────────────────

    private void renderRuneOrbit(VertexConsumer consumer, Matrix4f m, float tick, float alpha) {
        int count = 24;
        float size = 0.9f;
        float rot = tick * 0.005f;
        for (int i = 0; i < count; i++) {
            float angle = (i / (float) count) * Mth.TWO_PI + rot;
            float rx = Mth.cos(angle) * (COURT_RADIUS - 2f);
            float rz = Mth.sin(angle) * (COURT_RADIUS - 2f);
            float ry = 2.5f + Mth.sin(angle * 3f + tick * 0.03f) * 1.5f;
            float a = alpha * (0.35f + 0.65f * (Mth.sin(i * 1.3f + tick * 0.05f) * 0.5f + 0.5f));

            float px = -Mth.sin(angle) * size;
            float pz =  Mth.cos(angle) * size;

            addVertex(consumer, m, rx - px, ry,          rz - pz, GOLD_R,  GOLD_G,  GOLD_B,  a);
            addVertex(consumer, m, rx + px, ry,          rz + pz, GOLD_R,  GOLD_G,  GOLD_B,  a);
            addVertex(consumer, m, rx + px, ry + size*2, rz + pz, GOLD2_R, GOLD2_G, GOLD2_B, a * 0.15f);
            addVertex(consumer, m, rx - px, ry + size*2, rz - pz, GOLD2_R, GOLD2_G, GOLD2_B, a * 0.15f);
        }
    }

    // ── Shared ring helper ────────────────────────────────────────────────────

    private void renderRing(VertexConsumer consumer, Matrix4f m,
                             float cx, float cz, float y, float radius, float alpha,
                             float r, float g, float b) {
        int seg = 32;
        float inner = radius - 0.3f, outer = radius + 0.3f;
        for (int i = 0; i < seg; i++) {
            float a1 = (i / (float) seg) * Mth.TWO_PI;
            float a2 = ((i + 1) / (float) seg) * Mth.TWO_PI;
            addVertex(consumer, m, cx + Mth.cos(a1) * inner, y, cz + Mth.sin(a1) * inner, r, g, b, alpha);
            addVertex(consumer, m, cx + Mth.cos(a2) * inner, y, cz + Mth.sin(a2) * inner, r, g, b, alpha);
            addVertex(consumer, m, cx + Mth.cos(a2) * outer, y, cz + Mth.sin(a2) * outer, r, g, b, alpha * 0.1f);
            addVertex(consumer, m, cx + Mth.cos(a1) * outer, y, cz + Mth.sin(a1) * outer, r, g, b, alpha * 0.1f);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void addVertex(VertexConsumer consumer, Matrix4f m,
                           float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(m, x, y, z).setColor(r, g, b, a);
    }

    @Override
    public boolean shouldRender(AncientCourtEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(AncientCourtEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
