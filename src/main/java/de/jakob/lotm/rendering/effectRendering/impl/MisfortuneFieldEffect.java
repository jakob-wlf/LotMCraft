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

public class MisfortuneFieldEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<MisfortuneParticle> misfortuneParticles = new ArrayList<>();
    private final List<CurseWisp> curseWisps = new ArrayList<>();
    private final List<OminousSymbol> ominousSymbols = new ArrayList<>();
    
    private static final float FIELD_RADIUS = 20f;
    private static final float DOME_HEIGHT = 15f;
    
    // Colors: Bronze/Orange and Light Blue
    private static final float BRONZE_R = 171f / 255f;
    private static final float BRONZE_G = 116f / 255f;
    private static final float BRONZE_B = 56f / 255f;
    
    private static final float BLUE_R = 170f / 255f;
    private static final float BLUE_G = 204f / 255f;
    private static final float BLUE_B = 230f / 255f;
    
    private float fieldIntensity = 0f;
    private float domeOpacity = 0f;

    public MisfortuneFieldEffect(double x, double y, double z) {
        super(x, y, z, 20 * 4); // 4 seconds duration

        // Initialize misfortune particles floating throughout the field
        for (int i = 0; i < 200; i++) {
            misfortuneParticles.add(new MisfortuneParticle());
        }

        // Initialize curse wisps that spiral around
        for (int i = 0; i < 30; i++) {
            curseWisps.add(new CurseWisp());
        }

        // Initialize ominous symbols that appear on the dome
        for (int i = 0; i < 15; i++) {
            ominousSymbols.add(new OminousSymbol());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Field expands quickly, sustains, then fades
        if (progress < 0.15f) {
            fieldIntensity = progress / 0.15f;
            domeOpacity = fieldIntensity * 0.8f;
        } else if (progress > 0.85f) {
            fieldIntensity = 1f - ((progress - 0.85f) / 0.15f);
            domeOpacity = fieldIntensity * 0.8f;
        } else {
            fieldIntensity = 1f;
            domeOpacity = 0.8f + 0.2f * Mth.sin(tick * 0.1f);
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render components in order
        renderGroundCircle(poseStack, bufferSource, progress);
        renderDome(poseStack, bufferSource, progress);
        renderDomeEdge(poseStack, bufferSource, progress);
        renderMisfortuneParticles(poseStack, bufferSource, progress);
        renderCurseWisps(poseStack, bufferSource, progress);
        renderOminousSymbols(poseStack, bufferSource, progress);
        renderEnergyPulses(poseStack, bufferSource, progress);
        renderDomeShimmer(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderGroundCircle(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;
        int segments = 64;
        float yOffset = 0.02f;

        // Draw filled circle on the ground with gradient
        for (int ring = 0; ring < 5; ring++) {
            float innerRadius = radius * (ring / 5f);
            float outerRadius = radius * ((ring + 1) / 5f);
            
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

                // Alternate colors between bronze and blue in rings
                float t = ring / 5f;
                float r = Mth.lerp(t, BRONZE_R, BLUE_R);
                float g = Mth.lerp(t, BRONZE_G, BLUE_G);
                float b = Mth.lerp(t, BRONZE_B, BLUE_B);

                float innerAlpha = 0.4f * fieldIntensity;
                float outerAlpha = 0.2f * fieldIntensity;

                // Draw quad
                addVertex(consumer, matrix, x1Inner, yOffset, z1Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Inner, yOffset, z2Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Outer, yOffset, z2Outer, r, g, b, outerAlpha);
                addVertex(consumer, matrix, x1Outer, yOffset, z1Outer, r, g, b, outerAlpha);
            }
        }

        // Add glowing edge ring
        float edgeThickness = 0.5f;
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

            float alpha = 0.6f * fieldIntensity;

            addVertex(consumer, matrix, x1Inner, yOffset + 0.01f, z1Inner, BRONZE_R, BRONZE_G, BRONZE_B, alpha);
            addVertex(consumer, matrix, x2Inner, yOffset + 0.01f, z2Inner, BRONZE_R, BRONZE_G, BRONZE_B, alpha);
            addVertex(consumer, matrix, x2Outer, yOffset + 0.01f, z2Outer, BRONZE_R, BRONZE_G, BRONZE_B, 0f);
            addVertex(consumer, matrix, x1Outer, yOffset + 0.01f, z1Outer, BRONZE_R, BRONZE_G, BRONZE_B, 0f);
        }
    }

    private void renderDome(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;
        float height = DOME_HEIGHT * fieldIntensity;
        
        int latSegments = 16;
        int lonSegments = 32;

        // Only render the upper hemisphere (dome)
        for (int lat = 0; lat < latSegments / 2; lat++) {
            float theta1 = (float) (lat * Math.PI / latSegments);
            float theta2 = (float) ((lat + 1) * Math.PI / latSegments);

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi1 = (float) (lon * 2 * Math.PI / lonSegments);
                float phi2 = (float) ((lon + 1) * 2 * Math.PI / lonSegments);

                // Add subtle distortion
                float distortion = Mth.sin(phi1 * 3f + currentTick * 0.05f) * 0.2f;

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

                // Color gradient from bronze at bottom to blue at top
                float heightFactor = y1 / height;
                float r = Mth.lerp(heightFactor, BRONZE_R, BLUE_R);
                float g = Mth.lerp(heightFactor, BRONZE_G, BLUE_G);
                float b = Mth.lerp(heightFactor, BRONZE_B, BLUE_B);

                // Swirling color effect
                float colorSwirl = Mth.sin(phi1 * 2f - currentTick * 0.08f + theta1 * 3f) * 0.5f + 0.5f;
                r = Mth.lerp(colorSwirl * 0.3f, r, heightFactor > 0.5f ? BLUE_R : BRONZE_R);
                g = Mth.lerp(colorSwirl * 0.3f, g, heightFactor > 0.5f ? BLUE_G : BRONZE_G);
                b = Mth.lerp(colorSwirl * 0.3f, b, heightFactor > 0.5f ? BLUE_B : BRONZE_B);

                float alpha = domeOpacity * 0.3f * (1f - heightFactor * 0.5f);

                addVertex(consumer, matrix, x1, y1, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y2, z2, r, g, b, alpha);
                addVertex(consumer, matrix, x3, y3, z3, r, g, b, alpha);
                addVertex(consumer, matrix, x4, y4, z4, r, g, b, alpha);
            }
        }
    }

    private void renderDomeEdge(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float radius = FIELD_RADIUS * fieldIntensity;
        int segments = 64;

        // Vertical glowing pillars around the dome edge
        int pillarCount = 24;
        for (int p = 0; p < pillarCount; p++) {
            float angle = (float) (p * 2 * Math.PI / pillarCount);
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            float height = DOME_HEIGHT * fieldIntensity * 0.3f;
            float width = 0.3f;

            float pulse = Mth.sin(currentTick * 0.1f + p * 0.5f) * 0.5f + 0.5f;
            float alpha = 0.5f * fieldIntensity * pulse;

            // Alternating colors
            float r = (p % 2 == 0) ? BRONZE_R : BLUE_R;
            float g = (p % 2 == 0) ? BRONZE_G : BLUE_G;
            float b = (p % 2 == 0) ? BRONZE_B : BLUE_B;

            // Draw pillar as vertical quad
            Vec3 perp = new Vec3(-Mth.sin(angle), 0, Mth.cos(angle)).scale(width);

            addVertex(consumer, matrix, (float)(x - perp.x), 0.05f, (float)(z - perp.z), r, g, b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), 0.05f, (float)(z + perp.z), r, g, b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), height, (float)(z + perp.z), r, g, b, 0f);
            addVertex(consumer, matrix, (float)(x - perp.x), height, (float)(z - perp.z), r, g, b, 0f);
        }
    }

    private void renderEnergyPulses(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Expanding energy rings along the ground
        int pulseCount = 4;
        for (int p = 0; p < pulseCount; p++) {
            float pulseDelay = p * 0.25f;
            float pulseProgress = ((progress * 2f + pulseDelay) % 1f);
            
            float pulseRadius = FIELD_RADIUS * pulseProgress * 0.9f;
            float pulseAlpha = Mth.sin(pulseProgress * Mth.PI) * 0.5f * fieldIntensity;

            int segments = 48;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * pulseRadius;
                float z1 = Mth.sin(angle1) * pulseRadius;
                float x2 = Mth.cos(angle2) * pulseRadius;
                float z2 = Mth.sin(angle2) * pulseRadius;

                float r = (p % 2 == 0) ? BRONZE_R : BLUE_R;
                float g = (p % 2 == 0) ? BRONZE_G : BLUE_G;
                float b = (p % 2 == 0) ? BRONZE_B : BLUE_B;

                addVertex(consumer, matrix, x1, 0.1f, z1, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x2, 0.1f, z2, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x2, 0.3f, z2, r, g, b, pulseAlpha);
                addVertex(consumer, matrix, x1, 0.3f, z1, r, g, b, pulseAlpha);
            }
        }
    }

    private void renderDomeShimmer(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Shimmering bands that move up the dome
        int bandCount = 3;
        for (int band = 0; band < bandCount; band++) {
            float bandProgress = ((progress * 0.8f + band * 0.33f) % 1f);
            float bandHeight = DOME_HEIGHT * fieldIntensity * bandProgress;
            
            float bandThickness = 1.5f;
            float bandAlpha = Mth.sin(bandProgress * Mth.PI) * 0.4f * fieldIntensity;

            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                // Calculate dome surface position at this height
                float theta = (float) Math.acos(1f - 2f * bandProgress);
                float r = FIELD_RADIUS * Mth.sin(theta);

                float x1 = r * Mth.cos(angle1);
                float z1 = r * Mth.sin(angle1);
                float x2 = r * Mth.cos(angle2);
                float z2 = r * Mth.sin(angle2);

                float color = (band % 2 == 0) ? 0f : 1f;
                float red = Mth.lerp(color, BRONZE_R, BLUE_R);
                float green = Mth.lerp(color, BRONZE_G, BLUE_G);
                float blue = Mth.lerp(color, BRONZE_B, BLUE_B);

                addVertex(consumer, matrix, x1, bandHeight - bandThickness, z1, red, green, blue, bandAlpha * 0.5f);
                addVertex(consumer, matrix, x2, bandHeight - bandThickness, z2, red, green, blue, bandAlpha * 0.5f);
                addVertex(consumer, matrix, x2, bandHeight + bandThickness, z2, red, green, blue, bandAlpha);
                addVertex(consumer, matrix, x1, bandHeight + bandThickness, z1, red, green, blue, bandAlpha);
            }
        }
    }

    private void renderMisfortuneParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (MisfortuneParticle particle : misfortuneParticles) {
            particle.update(progress, fieldIntensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                particle.size, particle.r, particle.g, particle.b, particle.alpha * fieldIntensity);
        }
    }

    private void renderCurseWisps(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (CurseWisp wisp : curseWisps) {
            wisp.update(progress, fieldIntensity);

            if (wisp.alpha <= 0f) continue;

            // Draw wisp trail
            for (int i = 0; i < wisp.trail.size() - 1; i++) {
                Vec3 p1 = wisp.trail.get(i);
                Vec3 p2 = wisp.trail.get(i + 1);

                float trailAlpha = wisp.alpha * (1f - i / (float) wisp.trail.size()) * fieldIntensity;

                renderBillboardQuad(consumer, matrix, (float)p1.x, (float)p1.y, (float)p1.z,
                    0.15f, wisp.r, wisp.g, wisp.b, trailAlpha);
            }
        }
    }

    private void renderOminousSymbols(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (OminousSymbol symbol : ominousSymbols) {
            symbol.update(progress, fieldIntensity);

            if (symbol.alpha <= 0f) continue;

            // Draw symbol as a simple cross pattern
            float size = symbol.size;
            renderBillboardQuad(consumer, matrix, symbol.x, symbol.y, symbol.z,
                size, symbol.r, symbol.g, symbol.b, symbol.alpha * fieldIntensity);
        }
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

    private class MisfortuneParticle {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float alpha;
        float r, g, b;
        boolean isBronze;

        MisfortuneParticle() {
            random.setSeed(System.nanoTime());
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * FIELD_RADIUS * 0.9f;

            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * DOME_HEIGHT * 0.8f;
            this.z = Mth.sin(angle) * dist;

            float speed = 0.05f + random.nextFloat() * 0.08f;
            this.vx = (random.nextFloat() - 0.5f) * speed;
            this.vy = (random.nextFloat() - 0.3f) * speed * 0.5f;
            this.vz = (random.nextFloat() - 0.5f) * speed;

            this.size = 0.1f + random.nextFloat() * 0.15f;
            this.alpha = 0.5f + random.nextFloat() * 0.4f;

            this.isBronze = random.nextBoolean();
            if (isBronze) {
                this.r = BRONZE_R;
                this.g = BRONZE_G;
                this.b = BRONZE_B;
            } else {
                this.r = BLUE_R;
                this.g = BLUE_G;
                this.b = BLUE_B;
            }
        }

        void update(float progress, float intensity) {
            this.x += vx;
            this.y += vy;
            this.z += vz;

            // Keep within dome
            float dist = (float) Math.sqrt(x * x + z * z);
            if (dist > FIELD_RADIUS * 0.95f || y < 0f || y > DOME_HEIGHT * 0.9f) {
                respawn();
            }
        }
    }

    private class CurseWisp {
        float angle;
        float radius;
        float height;
        float speed;
        float r, g, b;
        float alpha;
        List<Vec3> trail = new ArrayList<>();
        int maxTrailLength = 10;

        CurseWisp() {
            random.setSeed(System.nanoTime());
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.radius = 5f + random.nextFloat() * (FIELD_RADIUS - 5f);
            this.height = random.nextFloat() * DOME_HEIGHT * 0.6f;
            this.speed = 0.03f + random.nextFloat() * 0.05f;
            this.alpha = 0.6f + random.nextFloat() * 0.3f;

            boolean isBronze = random.nextBoolean();
            if (isBronze) {
                this.r = BRONZE_R;
                this.g = BRONZE_G;
                this.b = BRONZE_B;
            } else {
                this.r = BLUE_R;
                this.g = BLUE_G;
                this.b = BLUE_B;
            }
        }

        void update(float progress, float intensity) {
            this.angle += speed;

            float x = Mth.cos(angle) * radius;
            float y = height + Mth.sin(angle * 3f) * 2f;
            float z = Mth.sin(angle) * radius;

            trail.add(0, new Vec3(x, y, z));
            if (trail.size() > maxTrailLength) {
                trail.remove(trail.size() - 1);
            }
        }
    }

    private class OminousSymbol {
        float x, y, z;
        float r, g, b;
        float size;
        float alpha;
        float pulseSpeed;
        float pulseOffset;

        OminousSymbol() {
            random.setSeed(System.nanoTime());
            
            // Position on dome surface
            float angle = random.nextFloat() * Mth.TWO_PI;
            float heightFactor = 0.3f + random.nextFloat() * 0.5f;
            float theta = (float) Math.acos(1f - 2f * heightFactor);
            
            float r = FIELD_RADIUS * Mth.sin(theta);
            this.x = r * Mth.cos(angle);
            this.y = DOME_HEIGHT * Mth.cos(theta);
            this.z = r * Mth.sin(angle);

            this.size = 0.3f + random.nextFloat() * 0.4f;
            this.pulseSpeed = 0.1f + random.nextFloat() * 0.15f;
            this.pulseOffset = random.nextFloat() * Mth.TWO_PI;

            boolean isBronze = random.nextBoolean();
            if (isBronze) {
                this.r = BRONZE_R;
                this.g = BRONZE_G;
                this.b = BRONZE_B;
            } else {
                this.r = BLUE_R;
                this.g = BLUE_G;
                this.b = BLUE_B;
            }
        }

        void update(float progress, float intensity) {
            float pulse = Mth.sin(currentTick * pulseSpeed + pulseOffset) * 0.5f + 0.5f;
            this.alpha = 0.4f + pulse * 0.5f;
        }
    }
}