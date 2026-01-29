package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

public class LightOfHolinessEffect extends ActiveEffect {

    private float beamProgress;
    private float expansionProgress;
    private float intensity = 1f;
    private int particleSpawnTimer = 0;

    private static final float BEAM_RADIUS = 3.5f; // Thicker beam
    private static final float MAX_CIRCLE_RADIUS = 40f; // Larger circles
    private static final float BEAM_HEIGHT_ABOVE = 60f;

    // Core colors
    private static final float GOLD_R = 1.0f;
    private static final float GOLD_G = 0.6f;
    private static final float GOLD_B = 0.05f;
    
    // White core accent
    private static final float WHITE_R = 1.0f;
    private static final float WHITE_G = 1.0f;
    private static final float WHITE_B = 1.0f;

    public LightOfHolinessEffect(double x, double y, double z) {
        super(x, y, z, 70); // Same duration - 70 ticks
        this.currentTick = 0;
        this.beamProgress = 0f;
        this.expansionProgress = 0f;
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float progress = tick / maxDuration;

        // Beam descends (0.0 → 0.2)
        beamProgress = Mth.clamp(progress / 0.2f, 0f, 1f);

        // Expansion starts after beam hits (≥ 0.2)
        if (progress > 0.2f) {
            expansionProgress = Mth.clamp((progress - 0.2f) / 0.8f, 0f, 1f);
        } else {
            expansionProgress = 0f;
        }

        // Pulsing intensity
        intensity = 1f - (float) Math.pow(progress, 1.5);
        intensity *= (1f + 0.15f * Mth.sin(tick * 0.3f));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render beam (visible until 0.35 progress)
        if (progress <= 0.35f) {
            float beamFade = 1f;
            if (progress > 0.2f) {
                beamFade = 1f - ((progress - 0.2f) / 0.15f);
                beamFade = Mth.clamp(beamFade, 0f, 1f);
            }
            renderBeam(poseStack, bufferSource, beamProgress, intensity * beamFade);
        }

        // Render expanding circles - more dramatic
        if (expansionProgress > 0f) {
            renderExpandingCircles(poseStack, bufferSource, expansionProgress, intensity, progress);
        }

        poseStack.popPose();
    }

    private void renderBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                            float progress, float intensity) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        float beamLength = BEAM_HEIGHT_ABOVE * progress;
        float topY = BEAM_HEIGHT_ABOVE;
        float bottomY = BEAM_HEIGHT_ABOVE - beamLength;

        int segments = 48; // More segments for smoother beam

        // Bright white core
        float coreRadius = BEAM_RADIUS * 0.3f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * coreRadius;
            float z1 = Mth.sin(angle1) * coreRadius;
            float x2 = Mth.cos(angle2) * coreRadius;
            float z2 = Mth.sin(angle2) * coreRadius;

            addVertex(consumer, matrix, x1, topY, z1, WHITE_R, WHITE_G, WHITE_B, intensity);
            addVertex(consumer, matrix, x2, topY, z2, WHITE_R, WHITE_G, WHITE_B, intensity);
            addVertex(consumer, matrix, x2, bottomY, z2, WHITE_R, WHITE_G, WHITE_B, intensity);
            addVertex(consumer, matrix, x1, bottomY, z1, WHITE_R, WHITE_G, WHITE_B, intensity);
        }

        // Main gold beam cylinder
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = Mth.cos(angle1) * BEAM_RADIUS;
            float z1 = Mth.sin(angle1) * BEAM_RADIUS;
            float x2 = Mth.cos(angle2) * BEAM_RADIUS;
            float z2 = Mth.sin(angle2) * BEAM_RADIUS;

            addVertex(consumer, matrix, x1, topY, z1, GOLD_R, GOLD_G, GOLD_B, intensity * 0.95f);
            addVertex(consumer, matrix, x2, topY, z2, GOLD_R, GOLD_G, GOLD_B, intensity * 0.95f);
            addVertex(consumer, matrix, x2, bottomY, z2, GOLD_R, GOLD_G, GOLD_B, intensity);
            addVertex(consumer, matrix, x1, bottomY, z1, GOLD_R, GOLD_G, GOLD_B, intensity);
        }

        // Multiple glow layers for thickness
        for (int layer = 1; layer <= 3; layer++) {
            float glowRadius = BEAM_RADIUS + layer * 0.7f;
            float alpha = intensity * (0.7f - layer * 0.18f);

            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * glowRadius;
                float z1 = Mth.sin(angle1) * glowRadius;
                float x2 = Mth.cos(angle2) * glowRadius;
                float z2 = Mth.sin(angle2) * glowRadius;

                addVertex(consumer, matrix, x1, topY, z1, GOLD_R, GOLD_G * 0.85f, GOLD_B, alpha * 0.5f);
                addVertex(consumer, matrix, x2, topY, z2, GOLD_R, GOLD_G * 0.85f, GOLD_B, alpha * 0.5f);
                addVertex(consumer, matrix, x2, bottomY, z2, GOLD_R, GOLD_G * 0.85f, GOLD_B, alpha);
                addVertex(consumer, matrix, x1, bottomY, z1, GOLD_R, GOLD_G * 0.85f, GOLD_B, alpha);
            }
        }
    }

    private void renderExpandingCircles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                        float progress, float intensity, float overallProgress) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        int circleCount = 5; // More circles
        int segments = 64; // Smoother circles

        for (int c = 0; c < circleCount; c++) {
            float delay = c * 0.12f;
            float circleProgress = Mth.clamp((progress - delay) * 1.4f, 0f, 1f);

            if (circleProgress <= 0f) continue;

            // Easing for more dramatic expansion
            float easedProgress = (float) (1 - Math.pow(1 - circleProgress, 3));
            float radius = MAX_CIRCLE_RADIUS * easedProgress;
            float yOffset = 0.05f + c * 0.015f;
            float alpha = intensity * (0.9f - c * 0.12f) * (1f - circleProgress * 0.3f);

            // Pulsing effect
            float pulse = 1f + 0.2f * Mth.sin(overallProgress * 10 + c * 2);
            alpha *= pulse;

            // White center flash for first circle
            if (c == 0 && circleProgress < 0.3f) {
                float flashAlpha = alpha * (1f - circleProgress / 0.3f);
                for (int i = 0; i < segments; i++) {
                    float angle1 = (float) (i * Math.PI * 2 / segments);
                    float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                    float x1 = Mth.cos(angle1) * radius * 0.5f;
                    float z1 = Mth.sin(angle1) * radius * 0.5f;
                    float x2 = Mth.cos(angle2) * radius * 0.5f;
                    float z2 = Mth.sin(angle2) * radius * 0.5f;

                    addVertex(consumer, matrix, 0, yOffset, 0, WHITE_R, WHITE_G, WHITE_B, flashAlpha);
                    addVertex(consumer, matrix, x1, yOffset, z1, WHITE_R, WHITE_G, WHITE_B, flashAlpha * 0.7f);
                    addVertex(consumer, matrix, x2, yOffset, z2, WHITE_R, WHITE_G, WHITE_B, flashAlpha * 0.7f);
                    addVertex(consumer, matrix, 0, yOffset, 0, WHITE_R, WHITE_G, WHITE_B, flashAlpha);
                }
            }

            // Main circle - gold
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(angle1) * radius;
                float z1 = Mth.sin(angle1) * radius;
                float x2 = Mth.cos(angle2) * radius;
                float z2 = Mth.sin(angle2) * radius;

                addVertex(consumer, matrix, 0, yOffset, 0, GOLD_R, GOLD_G, GOLD_B, alpha);
                addVertex(consumer, matrix, x1, yOffset, z1, GOLD_R, GOLD_G * 0.7f, GOLD_B * 0.2f, alpha * 0.85f);
                addVertex(consumer, matrix, x2, yOffset, z2, GOLD_R, GOLD_G * 0.7f, GOLD_B * 0.2f, alpha * 0.85f);
                addVertex(consumer, matrix, 0, yOffset, 0, GOLD_R, GOLD_G, GOLD_B, alpha);
            }

            // Double glow ring for more impressive look
            for (int glowLayer = 1; glowLayer <= 2; glowLayer++) {
                float outerRadius = radius * (1.1f + glowLayer * 0.08f);
                float glowAlpha = alpha * (0.6f - glowLayer * 0.2f);
                
                for (int i = 0; i < segments; i++) {
                    float angle1 = (float) (i * Math.PI * 2 / segments);
                    float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

                    float x1Inner = Mth.cos(angle1) * radius;
                    float z1Inner = Mth.sin(angle1) * radius;
                    float x1Outer = Mth.cos(angle1) * outerRadius;
                    float z1Outer = Mth.sin(angle1) * outerRadius;

                    float x2Inner = Mth.cos(angle2) * radius;
                    float z2Inner = Mth.sin(angle2) * radius;
                    float x2Outer = Mth.cos(angle2) * outerRadius;
                    float z2Outer = Mth.sin(angle2) * outerRadius;

                    addVertex(consumer, matrix, x1Inner, yOffset, z1Inner, GOLD_R, GOLD_G * 0.7f, GOLD_B * 0.2f, glowAlpha);
                    addVertex(consumer, matrix, x2Inner, yOffset, z2Inner, GOLD_R, GOLD_G * 0.7f, GOLD_B * 0.2f, glowAlpha);
                    addVertex(consumer, matrix, x2Outer, yOffset, z2Outer, GOLD_R, GOLD_G * 0.4f, GOLD_B * 0.05f, 0f);
                    addVertex(consumer, matrix, x1Outer, yOffset, z1Outer, GOLD_R, GOLD_G * 0.4f, GOLD_B * 0.05f, 0f);
                }
            }
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                           float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }
}