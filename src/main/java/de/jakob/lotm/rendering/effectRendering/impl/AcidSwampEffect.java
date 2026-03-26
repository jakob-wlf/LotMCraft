package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class AcidSwampEffect extends ActiveEffect {

    private static final float SWAMP_RADIUS = 15f;
    // Base green colour components
    private static final float CR = 0.08f;
    private static final float CG = 0.55f;
    private static final float CB = 0.04f;

    private final RandomSource random = RandomSource.create();
    private final List<RippleRing> ripples = new ArrayList<>();
    private final List<SwampBubble> bubbles = new ArrayList<>();
    private final List<SwampMist> mists = new ArrayList<>();
    private float intensity;

    public AcidSwampEffect(double x, double y, double z) {
        super(x, y, z, 20 * 8);
        for (int i = 0; i < 5; i++) {
            ripples.add(new RippleRing(i));
        }
        for (int i = 0; i < 60; i++) {
            bubbles.add(new SwampBubble());
        }
        for (int i = 0; i < 20; i++) {
            mists.add(new SwampMist(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();
        if (progress < 0.1f) {
            intensity = progress / 0.1f;
        } else if (progress > 0.85f) {
            intensity = 1f - ((progress - 0.85f) / 0.15f);
        } else {
            intensity = 1f;
        }

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        renderSwampFloor(consumer, matrix, tick);
        renderBorderRing(consumer, matrix, tick);

        for (RippleRing ripple : ripples) {
            ripple.update(tick);
            renderRipple(consumer, matrix, ripple);
        }
        for (SwampMist mist : mists) {
            mist.update(tick);
            renderMist(consumer, matrix, mist);
        }
        for (SwampBubble bubble : bubbles) {
            bubble.update(tick);
            if (bubble.alpha > 0.01f) {
                renderBillboardQuad(consumer, matrix, bubble.x, bubble.y, bubble.z,
                        bubble.size, CR * 0.5f, CG, CB * 0.5f, bubble.alpha * intensity);
            }
        }

        poseStack.popPose();
    }

    /** Flat filled disk rendered as concentric annular rings with subtle wave distortion. */
    private void renderSwampFloor(VertexConsumer consumer, Matrix4f matrix, float tick) {
        int segments = 64;
        int rings = 12;
        float pulse = 0.02f * Mth.sin(tick * 0.05f);

        for (int r = 0; r < rings; r++) {
            float innerR = (r / (float) rings) * SWAMP_RADIUS;
            float outerR = ((r + 1) / (float) rings) * SWAMP_RADIUS;
            // Slightly alternate shading between rings for a murky, layered look
            float shade = (r % 2 == 0) ? 0.55f : 0.70f;
            float alpha = intensity * (0.50f + 0.10f * Mth.sin(tick * 0.04f + r * 0.5f));
            float distortAmt = 0.20f * Mth.sin(tick * 0.03f + r * 0.8f);

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float d1 = distortAmt * Mth.sin(angle1 * 3f + tick * 0.05f);
                float d2 = distortAmt * Mth.sin(angle2 * 3f + tick * 0.05f);

                float ix1 = Mth.cos(angle1) * (innerR + d1);
                float iz1 = Mth.sin(angle1) * (innerR + d1);
                float ox1 = Mth.cos(angle1) * (outerR + d1);
                float oz1 = Mth.sin(angle1) * (outerR + d1);
                float ix2 = Mth.cos(angle2) * (innerR + d2);
                float iz2 = Mth.sin(angle2) * (innerR + d2);
                float ox2 = Mth.cos(angle2) * (outerR + d2);
                float oz2 = Mth.sin(angle2) * (outerR + d2);

                float y = 0.05f + pulse;
                addVertex(consumer, matrix, ix1, y, iz1, CR * shade, CG * shade, CB * shade, alpha);
                addVertex(consumer, matrix, ox1, y, oz1, CR * shade, CG * shade, CB * shade, alpha);
                addVertex(consumer, matrix, ox2, y, oz2, CR * shade, CG * shade, CB * shade, alpha);
                addVertex(consumer, matrix, ix2, y, iz2, CR * shade, CG * shade, CB * shade, alpha);
            }
        }
    }

    /** Bright glowing edge ring that pulses and turbulates. */
    private void renderBorderRing(VertexConsumer consumer, Matrix4f matrix, float tick) {
        int segments = 80;
        float ringHeight = 0.30f;
        float borderAlpha = intensity * (0.70f + 0.30f * Mth.sin(tick * 0.07f));

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);
            float t1 = 0.30f * Mth.sin(angle1 * 5f + tick * 0.06f);
            float t2 = 0.30f * Mth.sin(angle2 * 5f + tick * 0.06f);

            float x1 = Mth.cos(angle1) * (SWAMP_RADIUS + t1);
            float z1 = Mth.sin(angle1) * (SWAMP_RADIUS + t1);
            float x2 = Mth.cos(angle2) * (SWAMP_RADIUS + t2);
            float z2 = Mth.sin(angle2) * (SWAMP_RADIUS + t2);

            // Bright at ground, fades upward
            addVertex(consumer, matrix, x1, 0.05f,      z1, CR * 2f, CG * 2f, CB * 2f, borderAlpha);
            addVertex(consumer, matrix, x2, 0.05f,      z2, CR * 2f, CG * 2f, CB * 2f, borderAlpha);
            addVertex(consumer, matrix, x2, ringHeight, z2, CR,       CG,       CB,       borderAlpha * 0.2f);
            addVertex(consumer, matrix, x1, ringHeight, z1, CR,       CG,       CB,       borderAlpha * 0.2f);
        }
    }

    /** Expanding ripple rings that scroll outward from the centre and fade. */
    private void renderRipple(VertexConsumer consumer, Matrix4f matrix, RippleRing ripple) {
        if (ripple.alpha <= 0.01f) return;
        int segments = 56;
        float thickness = 0.25f;
        float a = ripple.alpha * intensity;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1i = Mth.cos(angle1) * ripple.radius;
            float z1i = Mth.sin(angle1) * ripple.radius;
            float x1o = Mth.cos(angle1) * (ripple.radius + thickness);
            float z1o = Mth.sin(angle1) * (ripple.radius + thickness);
            float x2i = Mth.cos(angle2) * ripple.radius;
            float z2i = Mth.sin(angle2) * ripple.radius;
            float x2o = Mth.cos(angle2) * (ripple.radius + thickness);
            float z2o = Mth.sin(angle2) * (ripple.radius + thickness);

            addVertex(consumer, matrix, x1i, 0.09f, z1i, CR * 2f, CG * 1.8f, CB * 2f, a);
            addVertex(consumer, matrix, x1o, 0.09f, z1o, CR * 2f, CG * 1.8f, CB * 2f, a);
            addVertex(consumer, matrix, x2o, 0.09f, z2o, CR * 2f, CG * 1.8f, CB * 2f, a);
            addVertex(consumer, matrix, x2i, 0.09f, z2i, CR * 2f, CG * 1.8f, CB * 2f, a);
        }
    }

    /** Thin vertical wisps of green gas rising near the edge of the swamp. */
    private void renderMist(VertexConsumer consumer, Matrix4f matrix, SwampMist mist) {
        if (mist.alpha <= 0.01f) return;
        float halfW = 0.35f;
        float perpAngle = mist.angle + Mth.HALF_PI;
        float px = Mth.cos(perpAngle) * halfW;
        float pz = Mth.sin(perpAngle) * halfW;

        float a = mist.alpha * intensity;
        addVertex(consumer, matrix, mist.x - px, 0.05f,       mist.z - pz, CR,        CG * 0.9f, CB,        a);
        addVertex(consumer, matrix, mist.x + px, 0.05f,       mist.z + pz, CR,        CG * 0.9f, CB,        a);
        addVertex(consumer, matrix, mist.x + px, mist.height, mist.z + pz, CR * 0.4f, CG * 0.4f, CB * 0.4f, 0f);
        addVertex(consumer, matrix, mist.x - px, mist.height, mist.z - pz, CR * 0.4f, CG * 0.4f, CB * 0.4f, 0f);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 toCamera = new Vec3(
                cameraPos.x - (getX() + x),
                cameraPos.y - (getY() + y),
                cameraPos.z - (getZ() + z)
        ).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);
        addVertex(consumer, matrix, (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z), r, g, b, a);
        addVertex(consumer, matrix, (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z), r, g, b, a);
        addVertex(consumer, matrix, (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z), r, g, b, a);
        addVertex(consumer, matrix, (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z), r, g, b, a);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    // ---- Inner classes ----

    private class RippleRing {
        float radius;
        float alpha;
        final float phaseOffset;

        RippleRing(int index) {
            this.phaseOffset = index * (1f / 5);
        }

        void update(float tick) {
            float t = (tick * 0.008f + phaseOffset) % 1f;
            this.radius = t * SWAMP_RADIUS;
            // Fade in then out as the ring expands outward
            this.alpha = Mth.sin(t * Mth.PI) * 0.55f;
        }
    }

    private class SwampBubble {
        final float x, z;
        float y;
        float size;
        float alpha;
        final float yPhase;
        final float alphaPhase;

        SwampBubble() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = (float) Math.sqrt(random.nextFloat()) * SWAMP_RADIUS * 0.95f;
            this.x = Mth.cos(angle) * dist;
            this.z = Mth.sin(angle) * dist;
            this.size = 0.08f + random.nextFloat() * 0.18f;
            this.yPhase = random.nextFloat() * Mth.TWO_PI;
            this.alphaPhase = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float tick) {
            this.y = 0.15f + 0.08f * Mth.sin(tick * 0.06f + yPhase);
            this.alpha = 0.25f + 0.20f * Mth.sin(tick * 0.04f + alphaPhase);
        }
    }

    private class SwampMist {
        final float angle;
        final float x, z;
        float height;
        float alpha;
        final float phaseOffset;

        SwampMist(int index) {
            this.angle = (index / 20f) * Mth.TWO_PI + random.nextFloat() * 0.3f;
            float dist = SWAMP_RADIUS * (0.65f + random.nextFloat() * 0.35f);
            this.x = Mth.cos(angle) * dist;
            this.z = Mth.sin(angle) * dist;
            this.phaseOffset = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float tick) {
            this.alpha = 0.20f + 0.18f * Mth.sin(tick * 0.04f + phaseOffset);
            this.height = 0.5f + 0.30f * Mth.sin(tick * 0.03f + phaseOffset);
        }
    }
}
