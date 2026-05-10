package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class HangedBurstEffect extends ActiveEffect {
    public enum Profile {
        SHADOW_SUMMON,
        FLESH_CURSE,
        SEA_OF_DARKNESS
    }

    private final Profile profile;

    public HangedBurstEffect(double x, double y, double z, Profile profile) {
        super(x, y, z, switch (profile) {
            case SHADOW_SUMMON, FLESH_CURSE -> 70;
            case SEA_OF_DARKNESS -> 160;
        });
        this.profile = profile;
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        float progress = Mth.clamp(getProgress(), 0.0f, 1.0f);
        float fade = progress < 0.16f ? progress / 0.16f : (progress > 0.84f ? 1.0f - ((progress - 0.84f) / 0.16f) : 1.0f);

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        switch (profile) {
            case SHADOW_SUMMON -> renderShadowSummon(consumer, matrix, tick, fade);
            case FLESH_CURSE -> renderFleshCurse(consumer, matrix, tick, fade);
            case SEA_OF_DARKNESS -> renderSeaOfDarkness(consumer, matrix, tick, fade);
        }

        poseStack.popPose();
    }

    private void renderShadowSummon(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        float radius = 2.3f + 0.25f * Mth.sin(tick * 0.1f);
        renderDisc(consumer, matrix, radius, 0.05f, 0.03f, 0.03f, 0.05f, alpha * 0.55f, 28);
        renderRing(consumer, matrix, radius * 0.78f, radius + 0.2f, 0.12f, 28, 0.15f, 0.15f, 0.22f, alpha * 0.88f);
        for (int i = 0; i < 10; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / 10.0) + tick * 0.015f);
            float inner = radius * 0.42f;
            float outer = radius * 0.98f;
            renderQuad(consumer, matrix,
                    Mth.cos(angle) * inner, 0.08f, Mth.sin(angle) * inner,
                    Mth.cos(angle + 0.08f) * inner, 0.08f, Mth.sin(angle + 0.08f) * inner,
                    Mth.cos(angle) * outer, 2.4f, Mth.sin(angle) * outer,
                    Mth.cos(angle) * outer, 2.4f, Mth.sin(angle) * outer,
                    0.18f, 0.18f, 0.28f, alpha * 0.72f);
        }
    }

    private void renderFleshCurse(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        float radius = 1.6f;
        renderDisc(consumer, matrix, radius, 0.05f, 0.24f, 0.02f, 0.03f, alpha * 0.5f, 24);
        renderRing(consumer, matrix, radius * 0.68f, radius + 0.12f, 0.07f, 24, 0.72f, 0.14f, 0.16f, alpha * 0.92f);
        renderCross(consumer, matrix, radius * 1.1f, 0.09f, 0.82f, 0.36f, 0.3f, alpha * 0.68f);
        for (int i = 0; i < 6; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / 6.0) + tick * 0.01f);
            float px = Mth.cos(angle) * radius * 0.92f;
            float pz = Mth.sin(angle) * radius * 0.92f;
            renderQuad(consumer, matrix, px - 0.08f, 0.08f, pz, px + 0.08f, 0.08f, pz, px, 1.4f, pz, px, 1.4f, pz,
                    0.65f, 0.08f, 0.1f, alpha * 0.42f);
        }
    }

    private void renderSeaOfDarkness(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        float radius = 512.0f;
        renderDisc(consumer, matrix, radius, 0.03f, 0.01f, 0.01f, 0.02f, alpha * 0.22f, 96);
        renderRing(consumer, matrix, radius * 0.985f, radius, 0.08f, 96, 0.08f, 0.08f, 0.14f, alpha * 0.55f);
        renderRing(consumer, matrix, radius * 0.72f, radius * 0.74f, 0.05f, 72, 0.04f, 0.04f, 0.07f, alpha * 0.18f);

        int pillars = 28;
        for (int i = 0; i < pillars; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / pillars) + tick * 0.003f);
            float px = Mth.cos(angle) * radius;
            float pz = Mth.sin(angle) * radius;
            renderQuad(consumer, matrix, px - 4.0f, 0.08f, pz, px + 4.0f, 0.08f, pz, px + 2.0f, 34.0f, pz, px - 2.0f, 34.0f, pz,
                    0.03f, 0.03f, 0.06f, alpha * 0.16f);
        }
    }

    private void renderCross(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                             float r, float g, float b, float alpha) {
        renderQuad(consumer, matrix, -radius, y, -0.08f, radius, y, -0.08f, radius, y, 0.08f, -radius, y, 0.08f, r, g, b, alpha);
        renderQuad(consumer, matrix, -0.08f, y, -radius, 0.08f, y, -radius, 0.08f, y, radius, -0.08f, y, radius, r, g, b, alpha);
    }

    private void renderDisc(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                            float r, float g, float b, float alpha, int segments) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * (Math.PI * 2.0 / segments));
            float angle2 = (float) ((i + 1) * (Math.PI * 2.0 / segments));
            addVertex(consumer, matrix, 0.0f, y, 0.0f, r, g, b, alpha * 0.45f);
            addVertex(consumer, matrix, Mth.cos(angle1) * radius, y, Mth.sin(angle1) * radius, r, g, b, alpha);
            addVertex(consumer, matrix, Mth.cos(angle2) * radius, y, Mth.sin(angle2) * radius, r, g, b, alpha);
        }
    }

    private void renderRing(VertexConsumer consumer, Matrix4f matrix, float innerRadius, float outerRadius, float y,
                            int segments, float r, float g, float b, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * (Math.PI * 2.0 / segments));
            float angle2 = (float) ((i + 1) * (Math.PI * 2.0 / segments));
            float ix1 = Mth.cos(angle1) * innerRadius;
            float iz1 = Mth.sin(angle1) * innerRadius;
            float ox1 = Mth.cos(angle1) * outerRadius;
            float oz1 = Mth.sin(angle1) * outerRadius;
            float ix2 = Mth.cos(angle2) * innerRadius;
            float iz2 = Mth.sin(angle2) * innerRadius;
            float ox2 = Mth.cos(angle2) * outerRadius;
            float oz2 = Mth.sin(angle2) * outerRadius;
            renderQuad(consumer, matrix, ix1, y, iz1, ix2, y, iz2, ox2, y, oz2, ox1, y, oz1, r, g, b, alpha);
        }
    }

    private void renderQuad(VertexConsumer consumer, Matrix4f matrix,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4,
                            float r, float g, float b, float alpha) {
        addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
        addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
        addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha * 0.72f);
        addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha * 0.72f);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float alpha) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, alpha);
    }
}
