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

public class NightDomainEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<StarParticle> starParticles = new ArrayList<>();
    private final List<ShadowWisp> shadowWisps = new ArrayList<>();
    private final List<DarkSymbol> darkSymbols = new ArrayList<>();

    private static final float FIELD_RADIUS = 40f;
    private static final float DOME_HEIGHT = 15f;

    // Colors: Deep Black and Dark Blue - values for proper darkness
    private static final float BLACK_R = 0.01f;
    private static final float BLACK_G = 0.01f;
    private static final float BLACK_B = 0.03f;

    private static final float DARK_BLUE_R = 0.03f;
    private static final float DARK_BLUE_G = 0.05f;
    private static final float DARK_BLUE_B = 0.12f;

    private static final float ACCENT_BLUE_R = 0.1f;
    private static final float ACCENT_BLUE_G = 0.15f;
    private static final float ACCENT_BLUE_B = 0.35f;

    private float fieldIntensity = 0f;
    private float domeOpacity = 0f;

    public NightDomainEffect(double x, double y, double z) {
        super(x, y, z, 20 * 25); // 25 seconds duration

        // Initialize star-like particles floating in the darkness
        for (int i = 0; i < 300; i++) {
            starParticles.add(new StarParticle());
        }

        // Initialize shadow wisps that move through the domain
        for (int i = 0; i < 40; i++) {
            shadowWisps.add(new ShadowWisp());
        }

        // Initialize dark symbols on the dome
        for (int i = 0; i < 20; i++) {
            darkSymbols.add(new DarkSymbol());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Field expands quickly, sustains, then fades
        if (progress < 0.1f) {
            fieldIntensity = progress / 0.1f;
            domeOpacity = fieldIntensity;
        } else if (progress > 0.9f) {
            fieldIntensity = 1f - ((progress - 0.9f) / 0.1f);
            domeOpacity = fieldIntensity;
        } else {
            fieldIntensity = 1f;
            domeOpacity = 0.95f + 0.05f * Mth.sin(tick * 0.05f);
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render components in order - dome uses translucent for solid dark appearance
        renderGroundCircle(poseStack, bufferSource, progress);
        renderDome(poseStack, bufferSource, progress);
        renderDomeEdge(poseStack, bufferSource, progress);
        renderStarParticles(poseStack, bufferSource, progress);
        renderShadowWisps(poseStack, bufferSource, progress);
        renderDarkSymbols(poseStack, bufferSource, progress);
        renderDarknessPulses(poseStack, bufferSource, progress);
        renderDomeShimmer(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderGroundCircle(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        // Use translucent for proper dark rendering with alpha
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;
        int segments = 64;
        float yOffset = 0.02f;

        // Draw filled circle with dark gradient
        for (int ring = 0; ring < 8; ring++) {
            float innerRadius = radius * (ring / 8f);
            float outerRadius = radius * ((ring + 1) / 8f);

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1Inner = Mth.cos(angle1) * innerRadius;
                float z1Inner = Mth.sin(angle1) * innerRadius;
                float x1Outer = Mth.cos(angle1) * outerRadius;
                float z1Outer = Mth.sin(angle1) * outerRadius;

                float x2Inner = Mth.cos(angle2) * innerRadius;
                float z2Inner = Mth.sin(angle2) * innerRadius;
                float x2Outer = Mth.cos(angle2) * outerRadius;
                float z2Outer = Mth.sin(angle2) * outerRadius;

                // Gradient from pure black center to dark blue edges
                float t = ring / 8f;
                float r = Mth.lerp(t, BLACK_R, DARK_BLUE_R);
                float g = Mth.lerp(t, BLACK_G, DARK_BLUE_G);
                float b = Mth.lerp(t, BLACK_B, DARK_BLUE_B);

                float innerAlpha = 0.95f * fieldIntensity;
                float outerAlpha = 0.9f * fieldIntensity;

                addVertex(consumer, matrix, x1Inner, yOffset, z1Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Inner, yOffset, z2Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Outer, yOffset, z2Outer, r, g, b, outerAlpha);
                addVertex(consumer, matrix, x1Outer, yOffset, z1Outer, r, g, b, outerAlpha);
            }
        }

        // Add glowing dark blue edge ring
        float edgeThickness = 0.8f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1Inner = Mth.cos(angle1) * (radius - edgeThickness);
            float z1Inner = Mth.sin(angle1) * (radius - edgeThickness);
            float x1Outer = Mth.cos(angle1) * (radius + edgeThickness);
            float z1Outer = Mth.sin(angle1) * (radius + edgeThickness);

            float x2Inner = Mth.cos(angle2) * (radius - edgeThickness);
            float z2Inner = Mth.sin(angle2) * (radius - edgeThickness);
            float x2Outer = Mth.cos(angle2) * (radius + edgeThickness);
            float z2Outer = Mth.sin(angle2) * (radius + edgeThickness);

            float alpha = 0.8f * fieldIntensity;

            addVertex(consumer, matrix, x1Inner, yOffset + 0.01f, z1Inner, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, alpha);
            addVertex(consumer, matrix, x2Inner, yOffset + 0.01f, z2Inner, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, alpha);
            addVertex(consumer, matrix, x2Outer, yOffset + 0.01f, z2Outer, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, 0f);
            addVertex(consumer, matrix, x1Outer, yOffset + 0.01f, z1Outer, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, 0f);
        }
    }

    private void renderDome(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        // CRITICAL FIX: Use translucent instead of lightning for dark, solid appearance
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;
        float height = DOME_HEIGHT * fieldIntensity;

        int latSegments = 20;
        int lonSegments = 40;

        // Render the upper hemisphere (dome)
        for (int lat = 0; lat < latSegments / 2; lat++) {
            float theta1 = (float) (lat * Math.PI / latSegments);
            float theta2 = (float) ((lat + 1) * Math.PI / latSegments);

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / lonSegments);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / lonSegments);

                // Subtle distortion for an otherworldly effect
                float distortion = Mth.sin(phi1 * 4f + currentTick * 0.03f) * 0.3f;

                float x1 = (radius + distortion) * Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = height * Mth.cos(theta1);
                float z1 = (radius + distortion) * Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = (radius + distortion) * Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = height * Mth.cos(theta1);
                float z2 = (radius + distortion) * Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = (radius + distortion) * Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = height * Mth.cos(theta2);
                float z3 = (radius + distortion) * Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = (radius + distortion) * Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = height * Mth.cos(theta2);
                float z4 = (radius + distortion) * Mth.sin(theta2) * Mth.sin(phi1);

                // Gradient from deep black at bottom to dark blue at top
                float heightFactor = y1 / height;
                float r = Mth.lerp(heightFactor, BLACK_R, DARK_BLUE_R);
                float g = Mth.lerp(heightFactor, BLACK_G, DARK_BLUE_G);
                float b = Mth.lerp(heightFactor, BLACK_B, DARK_BLUE_B);

                // Add swirling darker patterns
                float darkSwirl = Mth.sin(phi1 * 3f - currentTick * 0.04f + theta1 * 4f) * 0.5f + 0.5f;
                r *= (0.5f + darkSwirl * 0.5f);
                g *= (0.5f + darkSwirl * 0.5f);
                b *= (0.5f + darkSwirl * 0.5f);

                // High opacity for solid appearance
                float alpha = 0.98f * fieldIntensity * (0.95f - heightFactor * 0.1f);

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha);
                addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha);
            }
        }
    }

    private void renderDomeEdge(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;

        // Vertical dark energy pillars around the dome edge
        int pillarCount = 32;
        for (int p = 0; p < pillarCount; p++) {
            float angle = (float) (p * 2 * Math.PI / pillarCount);
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            float height = DOME_HEIGHT * fieldIntensity * 0.4f;
            float width = 0.4f;

            float pulse = Mth.sin(currentTick * 0.08f + p * 0.3f) * 0.5f + 0.5f;
            float alpha = 0.6f * fieldIntensity * pulse;

            // Alternating between pure dark and dark blue
            float r = (p % 3 == 0) ? ACCENT_BLUE_R : DARK_BLUE_R;
            float g = (p % 3 == 0) ? ACCENT_BLUE_G : DARK_BLUE_G;
            float b = (p % 3 == 0) ? ACCENT_BLUE_B : DARK_BLUE_B;

            Vec3 perp = new Vec3(-Mth.sin(angle), 0, Mth.cos(angle)).scale(width);

            addVertex(consumer, matrix, (float)(x - perp.x), 0.05f, (float)(z - perp.z), r, g, b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), 0.05f, (float)(z + perp.z), r, g, b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), height, (float)(z + perp.z), r, g, b, 0f);
            addVertex(consumer, matrix, (float)(x - perp.x), height, (float)(z - perp.z), r, g, b, 0f);
        }
    }

    private void renderDarknessPulses(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        // Expanding darkness waves along the ground
        int pulseCount = 5;
        for (int p = 0; p < pulseCount; p++) {
            float pulseDelay = p * 0.2f;
            float pulseProgress = ((progress * 1.5f + pulseDelay) % 1f);

            float pulseRadius = FIELD_RADIUS * pulseProgress * 0.95f;
            float pulseAlpha = Mth.sin(pulseProgress * Mth.PI) * 0.4f * fieldIntensity;

            int segments = 48;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * pulseRadius;
                float z1 = Mth.sin(angle1) * pulseRadius;
                float x2 = Mth.cos(angle2) * pulseRadius;
                float z2 = Mth.sin(angle2) * pulseRadius;

                float r = (p % 2 == 0) ? DARK_BLUE_R : ACCENT_BLUE_R;
                float g = (p % 2 == 0) ? DARK_BLUE_G : ACCENT_BLUE_G;
                float b = (p % 2 == 0) ? DARK_BLUE_B : ACCENT_BLUE_B;

                addVertex(consumer, matrix, x1, 0.15f, z1, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x2, 0.15f, z2, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x2, 0.4f, z2, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x1, 0.4f, z1, r, g, b, pulseAlpha);
            }
        }
    }

    private void renderDomeShimmer(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        // Dark blue energy bands that move up the dome
        int bandCount = 4;
        for (int band = 0; band < bandCount; band++) {
            float bandProgress = ((progress * 0.6f + band * 0.25f) % 1f);
            float bandHeight = DOME_HEIGHT * fieldIntensity * bandProgress;

            float bandThickness = 2f;
            float bandAlpha = Mth.sin(bandProgress * Mth.PI) * 0.5f * fieldIntensity;

            int segments = 40;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float theta = (float) Math.acos(1f - 2f * bandProgress);
                float r = FIELD_RADIUS * Mth.sin(theta);

                float x1 = r * Mth.cos(angle1);
                float z1 = r * Mth.sin(angle1);
                float x2 = r * Mth.cos(angle2);
                float z2 = r * Mth.sin(angle2);

                addVertex(consumer, matrix, x1, bandHeight - bandThickness, z1, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, bandAlpha * 0.5f);
                addVertex(consumer, matrix, x2, bandHeight - bandThickness, z2, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, bandAlpha * 0.5f);
                addVertex(consumer, matrix, x2, bandHeight + bandThickness, z2, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, bandAlpha);
                addVertex(consumer, matrix, x1, bandHeight + bandThickness, z1, ACCENT_BLUE_R, ACCENT_BLUE_G, ACCENT_BLUE_B, bandAlpha);
            }
        }
    }

    private void renderStarParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        for (StarParticle particle : starParticles) {
            particle.update(progress, fieldIntensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha * fieldIntensity);
        }
    }

    private void renderShadowWisps(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        for (ShadowWisp wisp : shadowWisps) {
            wisp.update(progress, fieldIntensity);

            if (wisp.alpha <= 0f) continue;

            // Draw wisp trail
            for (int i = 0; i < wisp.trail.size() - 1; i++) {
                Vec3 p1 = wisp.trail.get(i);

                float trailAlpha = wisp.alpha * (1f - i / (float) wisp.trail.size()) * fieldIntensity;

                renderBillboardQuad(consumer, matrix, (float)p1.x, (float)p1.y, (float)p1.z,
                        0.2f, wisp.r, wisp.g, wisp.b, trailAlpha);
            }
        }
    }

    private void renderDarkSymbols(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        for (DarkSymbol symbol : darkSymbols) {
            symbol.update(progress, fieldIntensity);

            if (symbol.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, symbol.x, symbol.y, symbol.z,
                    symbol.size, symbol.r, symbol.g, symbol.b, symbol.alpha * fieldIntensity);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0f, 0f)
                .setLight(15728880)
                .setNormal(0f, 1f, 0f);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 toCamera = new Vec3(
                cameraPos.x - (this.x + x),
                cameraPos.y - (this.y + y),
                cameraPos.z - (this.z + z)
        ).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        addVertex(consumer, matrix,
                (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z),
                r, g, b, a);
        addVertex(consumer, matrix,
                (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z),
                r, g, b, a);
    }

    private class StarParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        float twinkleSpeed;
        float twinkleOffset;

        StarParticle() {
            random.setSeed(System.nanoTime());
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * FIELD_RADIUS * 0.95f;

            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * DOME_HEIGHT * 0.9f;
            this.z = Mth.sin(angle) * dist;

            float speed = 0.02f + random.nextFloat() * 0.04f;
            this.vx = (random.nextFloat() - 0.5f) * speed;
            this.vy = (random.nextFloat() - 0.3f) * speed * 0.3f;
            this.vz = (random.nextFloat() - 0.5f) * speed;

            this.size = 0.08f + random.nextFloat() * 0.12f;
            this.twinkleSpeed = 0.05f + random.nextFloat() * 0.1f;
            this.twinkleOffset = random.nextFloat() * Mth.TWO_PI;

            // Mix of dark blue and bright accent particles (like stars)
            if (random.nextFloat() < 0.3f) {
                this.r = ACCENT_BLUE_R * 1.5f;
                this.g = ACCENT_BLUE_G * 1.5f;
                this.b = ACCENT_BLUE_B * 1.5f;
            } else {
                this.r = DARK_BLUE_R;
                this.g = DARK_BLUE_G;
                this.b = DARK_BLUE_B;
            }
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;

            // Twinkling effect
            float twinkle = Mth.sin(currentTick * twinkleSpeed + twinkleOffset) * 0.5f + 0.5f;
            this.alpha = 0.3f + twinkle * 0.6f;

            // Keep within dome
            float dist = (float) Math.sqrt(x * x + z * z);
            if (dist > FIELD_RADIUS * 0.95f || y < 0f || y > DOME_HEIGHT * 0.95f) {
                respawn();
            }
        }
    }

    private class ShadowWisp {
        float angle;
        float radius;
        float height;
        float speed;
        float r, g, b;
        float alpha;
        List<Vec3> trail = new ArrayList<>();
        int maxTrailLength = 12;

        ShadowWisp() {
            random.setSeed(System.nanoTime());
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.radius = 8f + random.nextFloat() * (FIELD_RADIUS - 8f);
            this.height = random.nextFloat() * DOME_HEIGHT * 0.7f;
            this.speed = 0.02f + random.nextFloat() * 0.04f;
            this.alpha = 0.5f + random.nextFloat() * 0.4f;

            // Dark wispy colors
            if (random.nextFloat() < 0.4f) {
                this.r = ACCENT_BLUE_R;
                this.g = ACCENT_BLUE_G;
                this.b = ACCENT_BLUE_B;
            } else {
                this.r = DARK_BLUE_R * 0.8f;
                this.g = DARK_BLUE_G * 0.8f;
                this.b = DARK_BLUE_B * 0.8f;
            }
        }

        void update(float progress, float intensity) {
            this.angle += speed;

            float x = Mth.cos(angle) * radius;
            float y = height + Mth.sin(angle * 4f) * 3f;
            float z = Mth.sin(angle) * radius;

            trail.add(0, new Vec3(x, y, z));
            if (trail.size() > maxTrailLength) {
                trail.remove(trail.size() - 1);
            }
        }
    }

    private class DarkSymbol {
        float x, y, z;
        float r, g, b;
        float size;
        float alpha;
        float pulseSpeed;
        float pulseOffset;

        DarkSymbol() {
            random.setSeed(System.nanoTime());

            // Position on dome surface
            float angle = random.nextFloat() * Mth.TWO_PI;
            float heightFactor = 0.2f + random.nextFloat() * 0.6f;
            float theta = (float) Math.acos(1f - 2f * heightFactor);

            float r = FIELD_RADIUS * Mth.sin(theta);
            this.x = r * Mth.cos(angle);
            this.y = DOME_HEIGHT * Mth.cos(theta);
            this.z = r * Mth.sin(angle);

            this.size = 0.4f + random.nextFloat() * 0.5f;
            this.pulseSpeed = 0.08f + random.nextFloat() * 0.12f;
            this.pulseOffset = random.nextFloat() * Mth.TWO_PI;

            // Dark symbols with blue accents
            if (random.nextFloat() < 0.5f) {
                this.r = ACCENT_BLUE_R;
                this.g = ACCENT_BLUE_G;
                this.b = ACCENT_BLUE_B;
            } else {
                this.r = DARK_BLUE_R * 1.2f;
                this.g = DARK_BLUE_G * 1.2f;
                this.b = DARK_BLUE_B * 1.2f;
            }
        }

        void update(float progress, float intensity) {
            float pulse = Mth.sin(currentTick * pulseSpeed + pulseOffset) * 0.5f + 0.5f;
            this.alpha = 0.3f + pulse * 0.6f;
        }
    }
}