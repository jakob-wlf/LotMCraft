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

public class MiracleEffect extends ActiveEffect {

    private final RandomSource random = RandomSource.create();
    private final List<HolyParticle> holyParticles = new ArrayList<>();
    private final List<LightBeam> lightBeams = new ArrayList<>();
    private final List<DivineStar> divineStars = new ArrayList<>();
    
    private static final float MIRACLE_RADIUS = 5f;
    private static final float BEAM_HEIGHT = 12f;
    
    // Color scheme: Purple, Golden, White
    private static final float PURPLE_R = 147f / 255f;
    private static final float PURPLE_G = 112f / 255f;
    private static final float PURPLE_B = 219f / 255f;
    
    private static final float GOLD_R = 255f / 255f;
    private static final float GOLD_G = 215f / 255f;
    private static final float GOLD_B = 0f / 255f;
    
    private static final float WHITE_R = 1f;
    private static final float WHITE_G = 1f;
    private static final float WHITE_B = 1f;
    
    private float intensity = 0f;

    public MiracleEffect(double x, double y, double z) {
        super(x, y, z, 20 * 2); // 2 seconds duration

        // Initialize ascending holy particles
        for (int i = 0; i < 150; i++) {
            holyParticles.add(new HolyParticle());
        }

        // Initialize divine light beams
        for (int i = 0; i < 8; i++) {
            lightBeams.add(new LightBeam(i));
        }

        // Initialize divine stars
        for (int i = 0; i < 12; i++) {
            divineStars.add(new DivineStar());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = tick / maxDuration;

        // Intensity: quick rise, sustain, gentle fade
        if (progress < 0.2f) {
            intensity = progress / 0.2f;
        } else if (progress > 0.8f) {
            intensity = 1f - ((progress - 0.8f) / 0.2f);
        } else {
            intensity = 1f;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render in layers
        renderGroundCircles(poseStack, bufferSource, progress);
        renderLightPillar(poseStack, bufferSource, progress);
        renderLightBeams(poseStack, bufferSource, progress);
        renderHolyParticles(poseStack, bufferSource, progress);
        renderDivineStars(poseStack, bufferSource, progress);
        renderExpandingRings(poseStack, bufferSource, progress);
        renderHeavenlyGlow(poseStack, bufferSource, progress);

        poseStack.popPose();
    }

    private void renderGroundCircles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float baseRadius = MIRACLE_RADIUS * intensity;
        int segments = 48;
        float yOffset = 0.01f;

        // Main holy circle with rotating pattern
        for (int ring = 0; ring < 4; ring++) {
            float innerRadius = baseRadius * (ring / 4f) * 0.8f;
            float outerRadius = baseRadius * ((ring + 1) / 4f) * 0.8f;
            
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

                // Rotating color pattern
                float colorPhase = angle1 + currentTick * 0.05f;
                float colorMix = Mth.sin(colorPhase * 3f) * 0.5f + 0.5f;
                
                float r, g, b;
                if (colorMix < 0.33f) {
                    float t = colorMix / 0.33f;
                    r = Mth.lerp(t, PURPLE_R, GOLD_R);
                    g = Mth.lerp(t, PURPLE_G, GOLD_G);
                    b = Mth.lerp(t, PURPLE_B, GOLD_B);
                } else if (colorMix < 0.66f) {
                    float t = (colorMix - 0.33f) / 0.33f;
                    r = Mth.lerp(t, GOLD_R, WHITE_R);
                    g = Mth.lerp(t, GOLD_G, WHITE_G);
                    b = Mth.lerp(t, GOLD_B, WHITE_B);
                } else {
                    float t = (colorMix - 0.66f) / 0.34f;
                    r = Mth.lerp(t, WHITE_R, PURPLE_R);
                    g = Mth.lerp(t, WHITE_G, PURPLE_G);
                    b = Mth.lerp(t, WHITE_B, PURPLE_B);
                }

                float innerAlpha = 0.5f * intensity;
                float outerAlpha = 0.2f * intensity;

                addVertex(consumer, matrix, x1Inner, yOffset, z1Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Inner, yOffset, z2Inner, r, g, b, innerAlpha);
                addVertex(consumer, matrix, x2Outer, yOffset, z2Outer, r, g, b, outerAlpha);
                addVertex(consumer, matrix, x1Outer, yOffset, z1Outer, r, g, b, outerAlpha);
            }
        }

        // Glowing holy symbols at cardinal directions
        for (int dir = 0; dir < 4; dir++) {
            float angle = dir * Mth.HALF_PI;
            float symbolDist = baseRadius * 0.6f;
            float x = Mth.cos(angle) * symbolDist;
            float z = Mth.sin(angle) * symbolDist;
            
            float symbolSize = 0.4f;
            float symbolPulse = Mth.sin(currentTick * 0.15f + dir) * 0.5f + 0.5f;
            float alpha = 0.7f * intensity * symbolPulse;

            // Draw cross symbol
            addVertex(consumer, matrix, x - symbolSize, yOffset + 0.02f, z - 0.1f, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x + symbolSize, yOffset + 0.02f, z - 0.1f, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x + symbolSize, yOffset + 0.02f, z + 0.1f, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x - symbolSize, yOffset + 0.02f, z + 0.1f, GOLD_R, GOLD_G, GOLD_B, alpha);

            addVertex(consumer, matrix, x - 0.1f, yOffset + 0.02f, z - symbolSize, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x + 0.1f, yOffset + 0.02f, z - symbolSize, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x + 0.1f, yOffset + 0.02f, z + symbolSize, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x - 0.1f, yOffset + 0.02f, z + symbolSize, GOLD_R, GOLD_G, GOLD_B, alpha);
        }
    }

    private void renderLightPillar(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Central divine light pillar
        float pillarRadius = 0.8f * intensity;
        float height = BEAM_HEIGHT * intensity;
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            // Swirling effect
            float twist = currentTick * 0.08f;
            float x1Bottom = Mth.cos(angle1 + twist) * pillarRadius;
            float z1Bottom = Mth.sin(angle1 + twist) * pillarRadius;
            float x2Bottom = Mth.cos(angle2 + twist) * pillarRadius;
            float z2Bottom = Mth.sin(angle2 + twist) * pillarRadius;

            float x1Top = Mth.cos(angle1 + twist + 1.5f) * pillarRadius * 0.5f;
            float z1Top = Mth.sin(angle1 + twist + 1.5f) * pillarRadius * 0.5f;
            float x2Top = Mth.cos(angle2 + twist + 1.5f) * pillarRadius * 0.5f;
            float z2Top = Mth.sin(angle2 + twist + 1.5f) * pillarRadius * 0.5f;

            // Color gradient from gold at bottom to white at top
            float alpha = 0.6f * intensity;

            addVertex(consumer, matrix, x1Bottom, 0f, z1Bottom, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x2Bottom, 0f, z2Bottom, GOLD_R, GOLD_G, GOLD_B, alpha);
            addVertex(consumer, matrix, x2Top, height, z2Top, WHITE_R, WHITE_G, WHITE_B, alpha * 0.3f);
            addVertex(consumer, matrix, x1Top, height, z1Top, WHITE_R, WHITE_G, WHITE_B, alpha * 0.3f);
        }
    }

    private void renderLightBeams(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (LightBeam beam : lightBeams) {
            beam.update(progress, intensity);

            float width = beam.width * intensity;
            float height = BEAM_HEIGHT * beam.heightMult * intensity;
            
            Vec3 dir = new Vec3(Mth.cos(beam.angle), 0, Mth.sin(beam.angle));
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).scale(width);

            float x = (float) (dir.x * beam.distance);
            float z = (float) (dir.z * beam.distance);

            float alpha = beam.alpha * intensity;

            addVertex(consumer, matrix, (float)(x - perp.x), 0f, (float)(z - perp.z), beam.r, beam.g, beam.b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), 0f, (float)(z + perp.z), beam.r, beam.g, beam.b, alpha);
            addVertex(consumer, matrix, (float)(x + perp.x), height, (float)(z + perp.z), WHITE_R, WHITE_G, WHITE_B, alpha * 0.2f);
            addVertex(consumer, matrix, (float)(x - perp.x), height, (float)(z - perp.z), WHITE_R, WHITE_G, WHITE_B, alpha * 0.2f);
        }
    }

    private void renderExpandingRings(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Multiple expanding holy rings
        int ringCount = 3;
        for (int r = 0; r < ringCount; r++) {
            float ringDelay = r * 0.15f;
            float ringProgress = ((progress * 1.5f + ringDelay) % 1f);
            
            float ringRadius = MIRACLE_RADIUS * ringProgress;
            float ringAlpha = Mth.sin(ringProgress * Mth.PI) * 0.6f * intensity;
            float ringHeight = 0.5f + ringProgress * 2f;

            int segments = 40;
            float thickness = 0.2f;

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1Inner = Mth.cos(angle1) * (ringRadius - thickness);
                float z1Inner = Mth.sin(angle1) * (ringRadius - thickness);
                float x1Outer = Mth.cos(angle1) * (ringRadius + thickness);
                float z1Outer = Mth.sin(angle1) * (ringRadius + thickness);

                float x2Inner = Mth.cos(angle2) * (ringRadius - thickness);
                float z2Inner = Mth.sin(angle2) * (ringRadius - thickness);
                float x2Outer = Mth.cos(angle2) * (ringRadius + thickness);
                float z2Outer = Mth.sin(angle2) * (ringRadius + thickness);

                float colorT = (r % 3) / 2f;
                float red = colorT < 0.5f ? Mth.lerp(colorT * 2f, PURPLE_R, GOLD_R) : Mth.lerp((colorT - 0.5f) * 2f, GOLD_R, WHITE_R);
                float green = colorT < 0.5f ? Mth.lerp(colorT * 2f, PURPLE_G, GOLD_G) : Mth.lerp((colorT - 0.5f) * 2f, GOLD_G, WHITE_G);
                float blue = colorT < 0.5f ? Mth.lerp(colorT * 2f, PURPLE_B, GOLD_B) : Mth.lerp((colorT - 0.5f) * 2f, GOLD_B, WHITE_B);

                addVertex(consumer, matrix, x1Inner, ringHeight, z1Inner, red, green, blue, ringAlpha);
                addVertex(consumer, matrix, x2Inner, ringHeight, z2Inner, red, green, blue, ringAlpha);
                addVertex(consumer, matrix, x2Outer, ringHeight, z2Outer, red, green, blue, ringAlpha * 0.5f);
                addVertex(consumer, matrix, x1Outer, ringHeight, z1Outer, red, green, blue, ringAlpha * 0.5f);
            }
        }
    }

    private void renderHeavenlyGlow(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Pulsing glow at center
        float glowRadius = 1.5f * intensity;
        float glowPulse = Mth.sin(currentTick * 0.12f) * 0.3f + 0.7f;
        float glowAlpha = 0.4f * intensity * glowPulse;
        
        int segments = 24;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * glowRadius;
            float z1 = Mth.sin(angle1) * glowRadius;
            float x2 = Mth.cos(angle2) * glowRadius;
            float z2 = Mth.sin(angle2) * glowRadius;

            addVertex(consumer, matrix, 0f, 0.1f, 0f, WHITE_R, WHITE_G, WHITE_B, glowAlpha);
            addVertex(consumer, matrix, x1, 0.1f, z1, GOLD_R, GOLD_G, GOLD_B, glowAlpha * 0.5f);
            addVertex(consumer, matrix, x2, 0.1f, z2, GOLD_R, GOLD_G, GOLD_B, glowAlpha * 0.5f);
        }
    }

    private void renderHolyParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (HolyParticle particle : holyParticles) {
            particle.update(progress, intensity);

            if (particle.alpha <= 0f) continue;

            renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                particle.size, particle.r, particle.g, particle.b, particle.alpha * intensity);
        }
    }

    private void renderDivineStars(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float progress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        for (DivineStar star : divineStars) {
            star.update(progress, intensity);

            if (star.alpha <= 0f) continue;

            // Draw star with rotating points
            float rotation = currentTick * star.rotSpeed;
            int points = 5;
            
            for (int i = 0; i < points; i++) {
                float angle = rotation + (i * Mth.TWO_PI / points);
                float nextAngle = rotation + ((i + 1) * Mth.TWO_PI / points);
                
                float outerX = Mth.cos(angle) * star.size;
                float outerZ = Mth.sin(angle) * star.size;
                float innerX = Mth.cos((angle + nextAngle) / 2f) * star.size * 0.4f;
                float innerZ = Mth.sin((angle + nextAngle) / 2f) * star.size * 0.4f;

                renderBillboardQuad(consumer, matrix, 
                    star.x + outerX, star.y, star.z + outerZ,
                    star.size * 0.2f, star.r, star.g, star.b, star.alpha * intensity);
            }
            
            // Center glow
            renderBillboardQuad(consumer, matrix, star.x, star.y, star.z,
                star.size * 0.5f, WHITE_R, WHITE_G, WHITE_B, star.alpha * intensity * 0.8f);
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

    private class HolyParticle {
        float x, y, z;
        float vy;
        float size;
        float alpha;
        float r, g, b;
        float swirl;
        float swirlSpeed;

        HolyParticle() {
            random.setSeed(System.nanoTime());
            respawn();
        }

        void respawn() {
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = random.nextFloat() * MIRACLE_RADIUS * 0.9f;

            this.swirl = angle;
            this.x = Mth.cos(angle) * dist;
            this.y = random.nextFloat() * 0.5f;
            this.z = Mth.sin(angle) * dist;

            this.vy = 0.08f + random.nextFloat() * 0.12f;
            this.swirlSpeed = 0.03f + random.nextFloat() * 0.05f;

            this.size = 0.08f + random.nextFloat() * 0.12f;
            this.alpha = 0.6f + random.nextFloat() * 0.3f;

            int colorChoice = random.nextInt(3);
            if (colorChoice == 0) {
                this.r = PURPLE_R;
                this.g = PURPLE_G;
                this.b = PURPLE_B;
            } else if (colorChoice == 1) {
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            } else {
                this.r = WHITE_R;
                this.g = WHITE_G;
                this.b = WHITE_B;
            }
        }

        void update(float progress, float intensity) {
            this.y += vy;
            this.swirl += swirlSpeed;
            
            float dist = (float) Math.sqrt(x * x + z * z);
            this.x = Mth.cos(swirl) * dist;
            this.z = Mth.sin(swirl) * dist;

            if (y > BEAM_HEIGHT * 0.8f) {
                respawn();
            }
        }
    }

    private class LightBeam {
        float angle;
        float distance;
        float width;
        float heightMult;
        float alpha;
        float r, g, b;

        LightBeam(int index) {
            random.setSeed(System.nanoTime() + index);
            this.angle = (float) (index * Math.PI * 2 / 8);
            this.distance = 2f + random.nextFloat() * 2f;
            this.width = 0.15f + random.nextFloat() * 0.15f;
            this.heightMult = 0.7f + random.nextFloat() * 0.3f;
            this.alpha = 0.4f + random.nextFloat() * 0.3f;

            if (index % 2 == 0) {
                this.r = PURPLE_R;
                this.g = PURPLE_G;
                this.b = PURPLE_B;
            } else {
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            }
        }

        void update(float progress, float intensity) {
            this.angle += 0.01f;
        }
    }

    private class DivineStar {
        float x, y, z;
        float r, g, b;
        float size;
        float alpha;
        float rotSpeed;

        DivineStar() {
            random.setSeed(System.nanoTime());
            
            float angle = random.nextFloat() * Mth.TWO_PI;
            float dist = 1f + random.nextFloat() * (MIRACLE_RADIUS - 1f);
            
            this.x = Mth.cos(angle) * dist;
            this.y = 3f + random.nextFloat() * 5f;
            this.z = Mth.sin(angle) * dist;

            this.size = 0.3f + random.nextFloat() * 0.4f;
            this.rotSpeed = 0.05f + random.nextFloat() * 0.1f;

            int colorChoice = random.nextInt(2);
            if (colorChoice == 0) {
                this.r = GOLD_R;
                this.g = GOLD_G;
                this.b = GOLD_B;
            } else {
                this.r = WHITE_R;
                this.g = WHITE_G;
                this.b = WHITE_B;
            }
        }

        void update(float progress, float intensity) {
            float pulse = Mth.sin(currentTick * 0.15f + x + z) * 0.5f + 0.5f;
            this.alpha = 0.5f + pulse * 0.4f;
        }
    }
}