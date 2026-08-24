package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ChaosVortexEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<VortexFragment> fragments = new ArrayList<>();

    private static final RenderType CHAOS_VORTEX_BODY = RenderType.create(
            "chaos_vortex_body",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final float MAX_RADIUS = 20.0f;
    private static final float FUNNEL_HEIGHT = 6.0f;
    private static final int RING_LAYERS = 16;
    private static final int RING_SEGMENTS = 56;
    private static final int FRAGMENT_COUNT = 90;

    private static final float COLOR_SATURATION = 0.85f;
    private static final float COLOR_BRIGHTNESS = 0.42f;
    private static final float FLICKER_THRESHOLD = 0.975f;
    private static final float FLICKER_BRIGHTNESS = 1.0f;

    private static final float DREAD_AURA_RADIUS_MULT = 1.8f;

    private static final float GROW_END = 0.22f;
    private static final float IMPLODE_START = 0.85f;

    private static final float ROTATION_SPEED = 1f;
    private static final float HUE_CYCLE_SPEED = 0.05f;

    private float directionX = 0;
    private float directionY = 1;
    private float directionZ = 0;

    private final Quaternionf directionRotation = new Quaternionf();
    private final Quaternionf inverseDirectionRotation = new Quaternionf();

    private boolean initialized = false;

    public ChaosVortexEffect(Location location, int duration, boolean infinite) {
        super(location, duration, infinite);
    }

    private void initFromParams() {
        if (initialized) return;
        initialized = true;

        float[] params = getParams();

        if (params != null && params.length >= 3
                && params[0] != -1 && params[1] != -1 && params[2] != -1) {
            directionX = params[0];
            directionY = params[1];
            directionZ = params[2];
        }

        Vector3f dir = new Vector3f(directionX, directionY, directionZ);
        if (dir.lengthSquared() < 1.0E-6f) {
            dir.set(0, 1, 0);
        } else {
            dir.normalize();
        }

        directionRotation.rotationTo(new Vector3f(0, 1, 0), dir);
        directionRotation.get(inverseDirectionRotation).conjugate();

        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            fragments.add(new VortexFragment());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        initFromParams();

        float progress = getProgress();

        float sizeFactor;
        if (progress < GROW_END) {
            float t = progress / GROW_END;
            sizeFactor = easeOutCubic(t);
        } else if (progress > IMPLODE_START) {
            float t = (progress - IMPLODE_START) / (1f - IMPLODE_START);
            sizeFactor = 1f - easeInCubic(t);
        } else {
            sizeFactor = 1f;
        }
        sizeFactor = Mth.clamp(sizeFactor, 0f, 1f);
        sizeFactor *= 1f + Mth.sin(tick * 0.1f) * 0.035f;

        float spinAccel = progress > IMPLODE_START
                ? 1f + (progress - IMPLODE_START) * 14f
                : 1f;
        float jitter = (hash(tick * 0.13f) - 0.5f) * 0.2f;
        float rotation = tick * ROTATION_SPEED * spinAccel + jitter;

        float alpha = Mth.clamp(sizeFactor * 1.15f, 0f, 1f);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());
        poseStack.mulPose(directionRotation);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        renderDreadAura(poseStack, bufferSource, sizeFactor, tick, alpha);
        renderFunnel(poseStack, bufferSource, sizeFactor, rotation, tick, alpha);
        renderVoidCore(poseStack, bufferSource, sizeFactor, tick, alpha);
        renderFragments(poseStack, bufferSource, progress, sizeFactor, rotation, tick, alpha);

        poseStack.popPose();
    }

    private void renderDreadAura(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                 float sizeFactor, float tick, float alpha) {
        if (alpha <= 0f || sizeFactor <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(CHAOS_VORTEX_BODY);
        Matrix4f matrix = poseStack.last().pose();

        float innerR = MAX_RADIUS * sizeFactor;
        float outerR = MAX_RADIUS * sizeFactor * DREAD_AURA_RADIUS_MULT;
        int segments = 48;
        float auraAlpha = alpha * 0.12f;

        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float wobble1 = turbulence(a1, tick * 0.3f, tick) * 1.5f;
            float wobble2 = turbulence(a2, tick * 0.3f, tick) * 1.5f;

            float xi1 = Mth.cos(a1) * innerR;
            float zi1 = Mth.sin(a1) * innerR;
            float xi2 = Mth.cos(a2) * innerR;
            float zi2 = Mth.sin(a2) * innerR;

            float xo1 = Mth.cos(a1) * (outerR + wobble1);
            float zo1 = Mth.sin(a1) * (outerR + wobble1);
            float xo2 = Mth.cos(a2) * (outerR + wobble2);
            float zo2 = Mth.sin(a2) * (outerR + wobble2);

            addVertex(consumer, matrix, xi1, 0.02f, zi1, 0.02f, 0.0f, 0.03f, auraAlpha);
            addVertex(consumer, matrix, xi2, 0.02f, zi2, 0.02f, 0.0f, 0.03f, auraAlpha);
            addVertex(consumer, matrix, xo2, 0.02f, zo2, 0.0f, 0.0f, 0.0f, 0f);
            addVertex(consumer, matrix, xo1, 0.02f, zo1, 0.0f, 0.0f, 0.0f, 0f);
        }
    }

    private void renderFunnel(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                              float sizeFactor, float rotation, float tick, float alpha) {
        if (alpha <= 0f || sizeFactor <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(CHAOS_VORTEX_BODY);
        Matrix4f matrix = poseStack.last().pose();

        float topRadius = MAX_RADIUS * sizeFactor;
        float height = FUNNEL_HEIGHT * sizeFactor;

        for (int layer = 0; layer < RING_LAYERS; layer++) {
            float t1 = (float) layer / RING_LAYERS;
            float t2 = (float) (layer + 1) / RING_LAYERS;

            float r1 = topRadius * (1f - t1 * 0.92f);
            float r2 = topRadius * (1f - t2 * 0.92f);
            float y1 = -height * t1;
            float y2 = -height * t2;

            float twist1 = rotation * (1f + t1 * 1.5f);
            float twist2 = rotation * (1f + t2 * 1.5f);

            float layerAlpha = alpha * (0.4f + 0.6f * (1f - t1));

            for (int i = 0; i < RING_SEGMENTS; i++) {
                float a1 = (float) (i * Math.PI * 2 / RING_SEGMENTS);
                float a2 = (float) ((i + 1) * Math.PI * 2 / RING_SEGMENTS);

                float rr1a = r1 + turbulence(a1, twist1, tick);
                float rr1b = r1 + turbulence(a2, twist1, tick);
                float rr2a = r2 + turbulence(a1, twist2, tick);
                float rr2b = r2 + turbulence(a2, twist2, tick);

                float x1a = Mth.cos(a1 + twist1) * rr1a;
                float z1a = Mth.sin(a1 + twist1) * rr1a;
                float x1b = Mth.cos(a2 + twist1) * rr1b;
                float z1b = Mth.sin(a2 + twist1) * rr1b;

                float x2a = Mth.cos(a1 + twist2) * rr2a;
                float z2a = Mth.sin(a1 + twist2) * rr2a;
                float x2b = Mth.cos(a2 + twist2) * rr2b;
                float z2b = Mth.sin(a2 + twist2) * rr2b;

                float[] colA = flickerHueColor(a1 + twist1, tick, layer, i);
                float[] colB = flickerHueColor(a2 + twist2, tick, layer, i + 1);

                addVertex(consumer, matrix, x1a, y1, z1a, colA[0], colA[1], colA[2], layerAlpha);
                addVertex(consumer, matrix, x1b, y1, z1b, colB[0], colB[1], colB[2], layerAlpha);
                addVertex(consumer, matrix, x2b, y2, z2b, colB[0], colB[1], colB[2], layerAlpha);
                addVertex(consumer, matrix, x2a, y2, z2a, colA[0], colA[1], colA[2], layerAlpha);
            }
        }
    }

    private void renderVoidCore(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                float sizeFactor, float tick, float alpha) {
        if (alpha <= 0f || sizeFactor <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(CHAOS_VORTEX_BODY);
        Matrix4f matrix = poseStack.last().pose();

        float breathe = 1f + Mth.sin(tick * 0.2f) * 0.08f;
        float coreRadius = MAX_RADIUS * sizeFactor * 0.16f * breathe;
        float coreY = -FUNNEL_HEIGHT * sizeFactor;
        int segments = 24;

        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float[] rimA = flickerHueColor(a1, tick, -1, i);
            float[] rimB = flickerHueColor(a2, tick, -1, i + 1);

            rimA[0] *= 0.5f; rimA[1] *= 0.3f; rimA[2] *= 0.3f;
            rimB[0] *= 0.5f; rimB[1] *= 0.3f; rimB[2] *= 0.3f;

            addVertex(consumer, matrix, 0, coreY, 0, 0f, 0f, 0f, alpha);
            addVertex(consumer, matrix, Mth.cos(a1) * coreRadius, coreY, Mth.sin(a1) * coreRadius,
                    rimA[0], rimA[1], rimA[2], alpha * 0.85f);
            addVertex(consumer, matrix, Mth.cos(a2) * coreRadius, coreY, Mth.sin(a2) * coreRadius,
                    rimB[0], rimB[1], rimB[2], alpha * 0.85f);
        }
    }

    private void renderFragments(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                 float progress, float sizeFactor, float rotation, float tick, float alpha) {
        if (alpha <= 0f) return;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (VortexFragment fragment : fragments) {
            fragment.update(sizeFactor, rotation, tick);
            if (fragment.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, fragment.x, fragment.y, fragment.z,
                    fragment.size, fragment.r, fragment.g, fragment.b, fragment.alpha * alpha);
        }
    }

    private float hash(float x) {
        float s = Mth.sin(x) * 43758.5453f;
        return s - Mth.floor(s);
    }

    private float turbulence(float angle, float twist, float tick) {
        float base = Mth.sin(angle * 3f + tick * 0.5f) * 0.4f
                + Mth.sin(angle * 7f - tick * 0.9f + twist) * 0.2f;
        float spike = hash(angle * 17f + tick * 0.37f);
        if (spike > 0.92f) {
            base += (spike - 0.92f) * 6f;
        }
        return base;
    }

    private float[] flickerHueColor(float angle, float tick, int layer, int segment) {
        float hue = ((angle / Mth.TWO_PI) + tick * HUE_CYCLE_SPEED) % 1f;
        if (hue < 0) hue += 1f;

        float flicker = hash(segment * 1.37f + layer * 7.13f + tick * 0.6f);
        float brightness = COLOR_BRIGHTNESS;
        float saturation = COLOR_SATURATION;
        if (flicker > FLICKER_THRESHOLD) {
            brightness = FLICKER_BRIGHTNESS;
            saturation = Math.min(1f, COLOR_SATURATION + 0.1f);
        }

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new float[]{
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >> 8) & 0xFF) / 255f,
                (rgb & 0xFF) / 255f
        };
    }

    private float easeOutCubic(float t) {
        float f = t - 1f;
        return f * f * f + 1f;
    }

    private float easeInCubic(float t) {
        return t * t * t;
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vector3f worldOffset = new Vector3f(x, y, z);
        directionRotation.transform(worldOffset);

        Vec3 worldPos = new Vec3(getX() + worldOffset.x, getY() + worldOffset.y, getZ() + worldOffset.z);
        Vec3 toCamera = cameraPos.subtract(worldPos).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        Vector3f rightLocal = new Vector3f((float) right.x, (float) right.y, (float) right.z);
        Vector3f upLocal = new Vector3f((float) up.x, (float) up.y, (float) up.z);
        inverseDirectionRotation.transform(rightLocal);
        inverseDirectionRotation.transform(upLocal);

        addVertex(consumer, matrix,
                x - rightLocal.x - upLocal.x, y - rightLocal.y - upLocal.y, z - rightLocal.z - upLocal.z,
                r, g, b, a);
        addVertex(consumer, matrix,
                x - rightLocal.x + upLocal.x, y - rightLocal.y + upLocal.y, z - rightLocal.z + upLocal.z,
                r, g, b, a);
        addVertex(consumer, matrix,
                x + rightLocal.x + upLocal.x, y + rightLocal.y + upLocal.y, z + rightLocal.z + upLocal.z,
                r, g, b, a);
        addVertex(consumer, matrix,
                x + rightLocal.x - upLocal.x, y + rightLocal.y - upLocal.y, z + rightLocal.z - upLocal.z,
                r, g, b, a);
    }

    private class VortexFragment {
        float x, y, z;
        float size;
        float alpha;
        float r, g, b;

        final float baseAngle;
        final float spiralSpeed;
        final float startRadiusMult;
        final float sizeBase;
        final float jitterSeed;
        boolean isEmber;

        VortexFragment() {
            this.baseAngle = random.nextFloat() * Mth.TWO_PI;
            this.spiralSpeed = 0.4f + random.nextFloat() * 0.7f;
            this.startRadiusMult = 0.6f + random.nextFloat() * 0.4f;
            this.sizeBase = 0.07f + random.nextFloat() * 0.09f;
            this.jitterSeed = random.nextFloat() * 1000f;

            if (random.nextFloat() < 0.15f) {
                isEmber = true;
                this.r = 0.7f + random.nextFloat() * 0.3f;
                this.g = 0.05f;
                this.b = 0.05f;
            } else {
                this.r = 0.05f;
                this.g = 0.05f;
                this.b = 0.06f;
            }
        }

        void update(float sizeFactor, float rotation, float tick) {
            float cycle = (tick * spiralSpeed * 0.045f) % 1f;

            float radius = MAX_RADIUS * sizeFactor * startRadiusMult * (1f - cycle);
            float depth = -FUNNEL_HEIGHT * sizeFactor * cycle;
            float angle = baseAngle + rotation * (1f + cycle * 2f);

            float jitterX = (hash(jitterSeed + tick * 0.4f) - 0.5f) * 0.3f;
            float jitterZ = (hash(jitterSeed + 50f + tick * 0.4f) - 0.5f) * 0.3f;

            this.x = Mth.cos(angle) * radius + jitterX;
            this.z = Mth.sin(angle) * radius + jitterZ;
            this.y = depth;

            this.size = sizeBase * sizeFactor * (1f - cycle * 0.7f);
            this.alpha = Mth.clamp((1f - cycle) * sizeFactor * 1.2f, 0f, 1f);

            if (isEmber) {
                float pulse = hash(jitterSeed + tick * 0.5f);
                if (pulse > 0.9f) {
                    this.alpha = Mth.clamp(this.alpha * 1.8f, 0f, 1f);
                }
            }
        }
    }
}