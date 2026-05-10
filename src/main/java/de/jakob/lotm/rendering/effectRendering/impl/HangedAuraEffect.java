package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveMovableEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class HangedAuraEffect extends ActiveMovableEffect {
    public enum Profile {
        SHADOW_CLOAK,
        FLESH_CLOAK,
        FLESH_FIELD,
        DEPRAVITY_ARMOR,
        BLOOD_POOL,
        SHADOW_BINDING,
        FLESH_MAW
    }

    private final Profile profile;

    public HangedAuraEffect(Location location, int maxDuration, boolean infinite, Profile profile) {
        super(location, maxDuration, infinite);
        this.profile = profile;
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        float progress = infinite ? 0.5f : Mth.clamp(getProgress(), 0.0f, 1.0f);
        float fade = infinite ? 1.0f : (progress < 0.18f ? progress / 0.18f : (progress > 0.82f ? 1.0f - ((progress - 0.82f) / 0.18f) : 1.0f));
        float pulse = 0.82f + 0.18f * Mth.sin(tick * 0.14f);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        switch (profile) {
            case SHADOW_CLOAK -> renderShadowCloak(consumer, matrix, tick, fade * pulse);
            case FLESH_CLOAK -> renderFleshCloak(consumer, matrix, tick, fade * pulse);
            case FLESH_FIELD -> renderFleshField(consumer, matrix, tick, fade);
            case DEPRAVITY_ARMOR -> renderDepravityArmor(consumer, matrix, tick, fade * pulse);
            case BLOOD_POOL -> renderBloodPool(consumer, matrix, tick, fade);
            case SHADOW_BINDING -> renderShadowBinding(consumer, matrix, tick, fade);
            case FLESH_MAW -> renderFleshMaw(consumer, matrix, tick, fade * pulse);
        }

        poseStack.popPose();
    }

    private void renderShadowCloak(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        renderRibbonCylinder(consumer, matrix, 0.85f, 2.3f, 18, tick * 0.04f, 0.03f, 0.03f, 0.05f, alpha);
        renderRing(consumer, matrix, 0.95f, 1.35f, 0.18f, 28, 0.12f, 0.12f, 0.18f, alpha * 0.65f);
        renderOrbitShards(consumer, matrix, 1.15f, 1.55f, 10, tick, 0.18f, 0.18f, 0.28f, alpha * 0.55f);
    }

    private void renderFleshCloak(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        renderRibbonCylinder(consumer, matrix, 0.92f, 2.2f, 20, -tick * 0.035f, 0.68f, 0.44f, 0.36f, alpha);
        renderRing(consumer, matrix, 0.8f, 1.28f, 0.12f, 26, 0.55f, 0.08f, 0.1f, alpha * 0.72f);
        renderOrbitShards(consumer, matrix, 0.95f, 1.1f, 12, tick, 0.78f, 0.3f, 0.24f, alpha * 0.5f);
    }

    private void renderFleshField(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        float radius = 4.8f + 0.7f * Mth.sin(tick * 0.06f);
        renderDisc(consumer, matrix, radius, 0.02f, 0.42f, 0.08f, 0.1f, alpha * 0.6f, 36);
        renderRing(consumer, matrix, radius - 0.45f, radius + 0.18f, 0.06f, 40, 0.82f, 0.38f, 0.34f, alpha * 0.9f);
        renderOrbitShards(consumer, matrix, radius * 0.7f, 0.28f, 18, tick, 0.75f, 0.12f, 0.15f, alpha * 0.42f);
    }

    private void renderDepravityArmor(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        renderRibbonCylinder(consumer, matrix, 1.0f, 2.55f, 16, tick * 0.03f, 0.08f, 0.06f, 0.08f, alpha);
        renderOrbitShards(consumer, matrix, 1.15f, 1.75f, 14, tick, 0.58f, 0.08f, 0.1f, alpha * 0.82f);
        renderRing(consumer, matrix, 1.0f, 1.28f, 1.95f, 30, 0.52f, 0.04f, 0.08f, alpha * 0.72f);
    }

    private void renderBloodPool(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        float radius = 1.15f + 0.12f * Mth.sin(tick * 0.2f);
        renderDisc(consumer, matrix, radius, 0.01f, 0.56f, 0.08f, 0.1f, alpha * 0.88f, 24);
        renderRing(consumer, matrix, radius * 0.72f, radius + 0.1f, 0.03f, 24, 0.82f, 0.28f, 0.22f, alpha * 0.58f);
        renderOrbitShards(consumer, matrix, radius * 0.82f, 0.16f, 10, tick, 0.7f, 0.16f, 0.18f, alpha * 0.34f);
    }

    private void renderShadowBinding(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        renderRing(consumer, matrix, 0.85f, 1.18f, 0.08f, 20, 0.12f, 0.12f, 0.18f, alpha * 0.72f);
        renderRing(consumer, matrix, 0.85f, 1.18f, 2.2f, 20, 0.16f, 0.16f, 0.24f, alpha * 0.58f);
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / 8.0) + tick * 0.015f);
            float x = Mth.cos(angle) * 1.02f;
            float z = Mth.sin(angle) * 1.02f;
            renderQuad(consumer, matrix, x - 0.06f, 0.0f, z, x + 0.06f, 0.0f, z, x + 0.04f, 2.25f, z, x - 0.04f, 2.25f, z,
                    0.06f, 0.06f, 0.1f, alpha * 0.95f);
        }
    }

    private void renderFleshMaw(VertexConsumer consumer, Matrix4f matrix, float tick, float alpha) {
        renderRing(consumer, matrix, 0.55f, 1.05f, 0.45f, 20, 0.52f, 0.06f, 0.08f, alpha * 0.82f);
        for (int i = 0; i < 14; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / 14.0) + tick * 0.01f);
            float inner = 0.56f;
            float outer = 1.18f + 0.08f * Mth.sin(tick * 0.15f + i);
            float x1 = Mth.cos(angle) * inner;
            float z1 = Mth.sin(angle) * inner;
            float x2 = Mth.cos(angle) * outer;
            float z2 = Mth.sin(angle) * outer;
            renderQuad(consumer, matrix, x1 - 0.04f, 0.15f, z1 - 0.04f, x1 + 0.04f, 0.15f, z1 + 0.04f, x2, 1.25f, z2, x2, 1.25f, z2,
                    0.82f, 0.5f, 0.42f, alpha * 0.76f);
        }
        renderDisc(consumer, matrix, 0.52f, 0.05f, 0.22f, 0.02f, 0.02f, alpha * 0.7f, 20);
    }

    private void renderRibbonCylinder(VertexConsumer consumer, Matrix4f matrix, float radius, float height, int segments,
                                      float rotation, float r, float g, float b, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * (Math.PI * 2.0 / segments) + rotation);
            float angle2 = (float) ((i + 1) * (Math.PI * 2.0 / segments) + rotation);
            float wave1 = 0.12f * Mth.sin(angle1 * 3.0f + currentTick * 0.06f);
            float wave2 = 0.12f * Mth.sin(angle2 * 3.0f + currentTick * 0.06f);
            float x1 = Mth.cos(angle1) * (radius + wave1);
            float z1 = Mth.sin(angle1) * (radius + wave1);
            float x2 = Mth.cos(angle2) * (radius + wave2);
            float z2 = Mth.sin(angle2) * (radius + wave2);
            renderQuad(consumer, matrix, x1, 0.08f, z1, x2, 0.08f, z2, x2 * 0.82f, height, z2 * 0.82f, x1 * 0.82f, height, z1 * 0.82f,
                    r, g, b, alpha * 0.7f);
        }
    }

    private void renderOrbitShards(VertexConsumer consumer, Matrix4f matrix, float radius, float height, int count,
                                   float tick, float r, float g, float b, float alpha) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (i * (Math.PI * 2.0 / count) + tick * 0.05f + i * 0.2f);
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;
            float y = height + 0.12f * Mth.sin(tick * 0.08f + i);
            renderQuad(consumer, matrix, x - 0.09f, y - 0.14f, z, x + 0.09f, y - 0.14f, z, x + 0.03f, y + 0.14f, z, x - 0.03f, y + 0.14f, z,
                    r, g, b, alpha);
        }
    }

    private void renderDisc(VertexConsumer consumer, Matrix4f matrix, float radius, float y,
                            float r, float g, float b, float alpha, int segments) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * (Math.PI * 2.0 / segments));
            float angle2 = (float) ((i + 1) * (Math.PI * 2.0 / segments));
            addVertex(consumer, matrix, 0.0f, y, 0.0f, r, g, b, alpha * 0.55f);
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
        addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha * 0.75f);
        addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha * 0.75f);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float alpha) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, alpha);
    }
}
