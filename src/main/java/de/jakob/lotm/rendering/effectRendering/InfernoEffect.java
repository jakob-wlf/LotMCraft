package de.jakob.lotm.rendering.effectRendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class InfernoEffect extends ActiveEffect {

    private float vortexProgress = 0f;
    private float intensity = 1f;
    private final RandomSource random = RandomSource.create();
    private final List<FlameSpiral> flameSpirals = new ArrayList<>();
    private final List<EmberParticle> emberParticles = new ArrayList<>();
    private final List<FireWave> fireWaves = new ArrayList<>();
    private final List<FlameWisp> flameWisps = new ArrayList<>();

    public InfernoEffect(double x, double y, double z) {
        super(x, y, z, 120); // 6 seconds (120 ticks at 20 tps)

        // Initialize flame spirals - the main body of the vortex
        for (int i = 0; i < 80; i++) {
            flameSpirals.add(new FlameSpiral());
        }

        // Initialize ember particles
        for (int i = 0; i < 400; i++) {
            emberParticles.add(new EmberParticle());
        }

        // Initialize fire waves that pulse outward
        for (int i = 0; i < 10; i++) {
            fireWaves.add(new FireWave(i * 0.1f));
        }

        // Initialize flame wisps for more fire-like appearance
        for (int i = 0; i < 150; i++) {
            flameWisps.add(new FlameWisp());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Update animation state
        float progress = getProgress();
        vortexProgress = Mth.clamp(progress * 1.1f, 0f, 1f);
        intensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.4));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render all layers of the fire vortex - reordered for better depth
        renderGroundFire(poseStack, vortexProgress, intensity);
        renderFireWaves(poseStack, vortexProgress, intensity);
        renderFireBurst(poseStack, vortexProgress, intensity);
        renderVortexCore(poseStack, vortexProgress, intensity);
        renderInnerFlames(poseStack, vortexProgress, intensity);
        renderSolidFlameColumn(poseStack, vortexProgress, intensity);
        renderPurpleFlames(poseStack, vortexProgress, intensity);
        renderFireSpirals(poseStack, vortexProgress, intensity);
        renderFlameSpirals(poseStack, vortexProgress, intensity);
        renderFlameWisps(poseStack, vortexProgress, intensity);
        //renderOuterVortex(poseStack, vortexProgress, intensity);
        renderEmberParticles(poseStack, vortexProgress, intensity);
        renderFlameTendrils(poseStack, vortexProgress, intensity);
        //renderVortexColumn(poseStack, vortexProgress, intensity);
        renderInfernoRings(poseStack, vortexProgress, intensity);
        renderHeatDistortion(poseStack, vortexProgress, intensity);
        renderFlameColumns(poseStack, vortexProgress, intensity);
        renderFirePillars(poseStack, vortexProgress, intensity);

        poseStack.popPose();
    }

    private void renderGroundFire(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Multi-layered ground fire for solidity
        for (int layer = 0; layer < 5; layer++) {
            float layerRadius = 4f + expansion * (25f + layer * 3f);
            float layerHeight = 0.1f + layer * 0.3f;
            int segments = 48;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            // Flickering effect
            float flicker = (float) Math.sin(currentTick * 0.4f + layer) * 0.15f + 0.85f;

            // Center point - bright but not white
            buffer.addVertex(matrix, 0, layerHeight, 0)
                    .setColor(1.0f, 0.6f, 0.1f, intensity * 0.6f * flicker);

            for (int i = 0; i <= segments; i++) {
                float angle = (float) (i * Math.PI * 2 / segments) + currentTick * 0.05f * (1 + layer * 0.2f);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float x = cos * layerRadius;
                float z = sin * layerRadius;

                // Color transitions from orange center to red-purple edges
                float edgeDist = (float) i / segments;
                float r = 1.0f - edgeDist * 0.3f;
                float g = 0.5f - edgeDist * 0.4f;
                float b = 0.05f + edgeDist * 0.5f;
                float a = intensity * (0.5f - edgeDist * 0.4f) * flicker;

                buffer.addVertex(matrix, x, layerHeight, z)
                        .setColor(r, g, b, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderSolidFlameColumn(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float height = expansion * 60f;
        int segments = 64;
        int heightSegments = 40;
        Matrix4f matrix = poseStack.last().pose();

        // Solid flame column with realistic fire colors
        for (int h = 0; h < heightSegments; h++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float t1 = (float) h / heightSegments;
            float t2 = (float) (h + 1) / heightSegments;
            float y1 = t1 * height;
            float y2 = t2 * height;

            float radius1 = (3f + expansion * 20f) * (1f + t1 * 1.5f);
            float radius2 = (3f + expansion * 20f) * (1f + t2 * 1.5f);

            for (int i = 0; i <= segments; i++) {
                float angle = (float) (i * Math.PI * 2 / segments) + currentTick * 0.12f + t1 * 1.5f;

                // Add noise to radius for flame-like flickering
                float flicker1 = (float) (Math.sin(angle * 3 + currentTick * 0.3f) * 0.15f +
                        Math.sin(angle * 7 + currentTick * 0.5f) * 0.1f);
                float flicker2 = (float) (Math.sin(angle * 3 + currentTick * 0.3f) * 0.15f +
                        Math.sin(angle * 7 + currentTick * 0.5f) * 0.1f);

                float r1 = radius1 * (1f + flicker1);
                float r2 = radius2 * (1f + flicker2);

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float x1 = cos * r1;
                float z1 = sin * r1;
                float x2 = cos * r2;
                float z2 = sin * r2;

                // Realistic fire colors - orange at base, transitioning to purple
                float r, g, b, a;

                if (t1 < 0.25f) {
                    // Bottom - bright orange flames
                    float bt = t1 / 0.25f;
                    r = 1.0f;
                    g = 0.5f - bt * 0.1f;
                    b = 0.05f;
                    a = intensity * 0.6f;
                } else if (t1 < 0.55f) {
                    // Lower-middle - orange-red flames
                    float mt = (t1 - 0.25f) / 0.3f;
                    r = 1.0f;
                    g = 0.4f - mt * 0.15f;
                    b = 0.05f + mt * 0.1f;
                    a = intensity * (0.55f - mt * 0.05f);
                } else if (t1 < 0.8f) {
                    // Upper-middle - red to magenta flames
                    float mt = (t1 - 0.55f) / 0.25f;
                    r = 1.0f - mt * 0.15f;
                    g = 0.25f - mt * 0.1f;
                    b = 0.15f + mt * 0.45f;
                    a = intensity * (0.5f - mt * 0.1f);
                } else {
                    // Top - purple flames
                    float tt = (t1 - 0.8f) / 0.2f;
                    r = 0.85f - tt * 0.25f;
                    g = 0.15f - tt * 0.05f;
                    b = 0.6f + tt * 0.3f;
                    a = intensity * (0.4f - tt * 0.2f);
                }

                buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
                buffer.addVertex(matrix, x2, y2, z2).setColor(r * 0.95f, g * 0.95f, b, a * 0.98f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFlameWisps(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (FlameWisp wisp : flameWisps) {
            wisp.update(expansion, intensity);

            if (wisp.alpha <= 0.05f) continue;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Wisp shape - elongated flame
            float width = wisp.size * 0.7f;
            float height = wisp.size * 2f;

            Vec3 pos = wisp.pos;

            // Flickering colors
            float r = wisp.isHot ? 1.0f : 0.9f;
            float g = wisp.isHot ? 0.8f : 0.4f;
            float b = wisp.isHot ? 0.2f : 0.1f;

            // Draw elongated quad
            buffer.addVertex(matrix, (float)(pos.x - width), (float)(pos.y), (float)(pos.z))
                    .setColor(r, g, b, wisp.alpha);
            buffer.addVertex(matrix, (float)(pos.x + width), (float)(pos.y), (float)(pos.z))
                    .setColor(r, g, b, wisp.alpha);
            buffer.addVertex(matrix, (float)(pos.x + width * 0.5f), (float)(pos.y + height), (float)(pos.z))
                    .setColor(r * 0.7f, g * 0.7f, b * 1.2f, wisp.alpha * 0.3f);
            buffer.addVertex(matrix, (float)(pos.x - width * 0.5f), (float)(pos.y + height), (float)(pos.z))
                    .setColor(r * 0.7f, g * 0.7f, b * 1.2f, wisp.alpha * 0.3f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFlameColumns(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.2f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float maxHeight = expansion * 65f;

        // Thick flame columns rising from base
        for (int i = 0; i < 20; i++) {
            float angle = (float) (i * Math.PI * 2 / 20) + currentTick * 0.03f;
            float baseRadius = 5f + expansion * 18f;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float xBase = cos * baseRadius;
            float zBase = sin * baseRadius;

            int segments = 20;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int seg = 0; seg <= segments; seg++) {
                float t = (float) seg / segments;
                float y = t * maxHeight * 0.8f;

                // Column tapers and waves
                float taper = 1f - t * 0.6f;
                float wave = (float) Math.sin(currentTick * 0.2f + i + t * 3f) * 0.3f;
                float columnRadius = (2f + wave) * taper;

                float x = xBase * taper;
                float z = zBase * taper;

                // Fire colors
                float r, g, b, a;
                if (t < 0.4f) {
                    r = 1.0f;
                    g = 0.9f - t * 0.5f;
                    b = 0.4f - t * 0.3f;
                    a = intensity * 0.8f;
                } else {
                    float tt = (t - 0.4f) / 0.6f;
                    r = 1.0f - tt * 0.2f;
                    g = 0.4f - tt * 0.2f;
                    b = 0.1f + tt * 0.4f;
                    a = intensity * (0.7f - tt * 0.5f);
                }

                buffer.addVertex(matrix, x + cos * columnRadius, y, z + sin * columnRadius)
                        .setColor(r, g, b, a);
                buffer.addVertex(matrix, x - cos * columnRadius, y, z - sin * columnRadius)
                        .setColor(r, g, b, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFirePillars(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float height = expansion * 70f;

        // Rotating fire pillars
        for (int pillar = 0; pillar < 8; pillar++) {
            float baseAngle = (float) (pillar * Math.PI * 2 / 8);
            float rotation = currentTick * 0.08f;
            float angle = baseAngle + rotation;

            float radius = 8f + expansion * 25f;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float xPos = cos * radius;
            float zPos = sin * radius;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            int heightSegments = 30;
            for (int h = 0; h <= heightSegments; h++) {
                float t = (float) h / heightSegments;
                float y = t * height;

                float pillarRadius = 1.5f * (1f - t * 0.7f);

                // Perpendicular to radius
                float perpX = -sin * pillarRadius;
                float perpZ = cos * pillarRadius;

                // Bright yellow-orange flames
                float r = 1.0f;
                float g = 0.8f - t * 0.5f;
                float b = 0.2f - t * 0.15f;
                float a = intensity * (0.9f - t * 0.6f);

                buffer.addVertex(matrix, xPos + perpX, y, zPos + perpZ)
                        .setColor(r, g, b, a);
                buffer.addVertex(matrix, xPos - perpX, y, zPos - perpZ)
                        .setColor(r, g, b, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderVortexCore(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.1f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float radius = 2.5f + expansion * 8f;
        float height = expansion * 65f;
        int segments = 32;
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        // Bright core with pulsing
        float pulse = (float) Math.sin(currentTick * 0.4f) * 0.2f + 0.8f;

        for (int i = 0; i < segments; i++) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(angle1) * radius * pulse;
            float z1 = (float) Math.sin(angle1) * radius * pulse;
            float x2 = (float) Math.cos(angle2) * radius * pulse;
            float z2 = (float) Math.sin(angle2) * radius * pulse;

            // Bottom - bright orange (not white)
            buffer.addVertex(matrix, x1, 0, z1).setColor(1.0f, 0.6f, 0.1f, intensity * 0.7f);
            buffer.addVertex(matrix, x2, 0, z2).setColor(1.0f, 0.6f, 0.1f, intensity * 0.7f);

            // Top - fading to purple
            buffer.addVertex(matrix, x1 * 0.4f, height, z1 * 0.4f).setColor(0.7f, 0.2f, 0.9f, 0f);
            buffer.addVertex(matrix, x2 * 0.4f, height, z2 * 0.4f).setColor(0.7f, 0.2f, 0.9f, 0f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderInnerFlames(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float height = expansion * 60f;
        int segments = 48;
        Matrix4f matrix = poseStack.last().pose();

        // Multiple layers of swirling flames with more intensity
        for (int layer = 0; layer < 4; layer++) {
            float layerOffset = layer * 0.25f;
            float baseRadius = 4f + layer * 2f;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float angle = t * (float) Math.PI * 2 + currentTick * 0.18f * (1 + layer * 0.6f);
                float heightPos = t * height;

                // Radius increases as we go up, creating vortex shape
                float radius = (baseRadius + expansion * (12f + layer * 4f)) * (1f + t * 2.2f);

                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;

                // Fire colors transitioning to purple
                float r, g, b, a;
                if (t < 0.3f) {
                    float bt = t / 0.3f;
                    r = 1.0f;
                    g = 0.6f - bt * 0.2f;
                    b = 0.1f;
                    a = intensity * 0.5f;
                } else if (t < 0.6f) {
                    float mt = (t - 0.3f) / 0.3f;
                    r = 1.0f;
                    g = 0.4f - mt * 0.2f;
                    b = 0.1f + mt * 0.2f;
                    a = intensity * (0.5f - mt * 0.1f);
                } else {
                    float tt = (t - 0.6f) / 0.4f;
                    r = 1.0f - tt * 0.35f;
                    g = 0.2f - tt * 0.1f;
                    b = 0.3f + tt * 0.6f;
                    a = intensity * (0.4f - tt * 0.2f);
                }

                buffer.addVertex(matrix, x, heightPos, z).setColor(r, g, b, a);
                buffer.addVertex(matrix, 0, heightPos, 0).setColor(r * 1.1f, g * 1.1f, Math.min(b * 1.2f, 1f), a * 0.8f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderPurpleFlames(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float height = expansion * 60f;
        int segments = 36;
        Matrix4f matrix = poseStack.last().pose();

        // Purple flame spirals mixed throughout
        for (int layer = 0; layer < 3; layer++) {
            float baseRadius = 5f + layer * 3f;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float angle = t * (float) Math.PI * 2 - currentTick * 0.15f * (1 + layer * 0.5f);
                float heightPos = t * height;

                float radius = (baseRadius + expansion * (15f + layer * 5f)) * (1f + t * 2f);

                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;

                // Purple-magenta flames
                float r, g, b, a;
                if (t < 0.4f) {
                    // Lower purple with some red
                    float bt = t / 0.4f;
                    r = 0.9f - bt * 0.2f;
                    g = 0.2f + bt * 0.1f;
                    b = 0.6f + bt * 0.2f;
                    a = intensity * 0.4f;
                } else {
                    // Upper purple fading
                    float tt = (t - 0.4f) / 0.6f;
                    r = 0.7f - tt * 0.15f;
                    g = 0.3f - tt * 0.15f;
                    b = 0.8f + tt * 0.2f;
                    a = intensity * (0.4f - tt * 0.25f);
                }

                buffer.addVertex(matrix, x, heightPos, z).setColor(r, g, b, a);
                buffer.addVertex(matrix, 0, heightPos, 0).setColor(r * 1.1f, g * 1.1f, Math.min(b * 1.1f, 1f), a * 0.7f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireSpirals(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float height = expansion * 60f;

        // More spiraling flame ribbons with better colors
        for (int spiral = 0; spiral < 25; spiral++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float spiralOffset = spiral * (360f / 25f);
            int points = 100;

            for (int i = 0; i < points - 1; i++) {
                float t = (float) i / points;
                float nextT = (float) (i + 1) / points;

                float angle1 = (float) Math.toRadians(spiralOffset + t * 1440f + currentTick * 12f);
                float angle2 = (float) Math.toRadians(spiralOffset + nextT * 1440f + currentTick * 12f);

                float radius1 = (2.5f + t * expansion * 30f) * (1f + t * 1.8f);
                float radius2 = (2.5f + nextT * expansion * 30f) * (1f + nextT * 1.8f);
                float height1 = t * height;
                float height2 = nextT * height;

                Vec3 pos1 = new Vec3(
                        Math.cos(angle1) * radius1,
                        height1,
                        Math.sin(angle1) * radius1
                );

                Vec3 pos2 = new Vec3(
                        Math.cos(angle2) * radius2,
                        height2,
                        Math.sin(angle2) * radius2
                );

                Vec3 dir = pos2.subtract(pos1).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(1.2f);

                // Fire colors with purple
                float r, g, b, a;
                if (t < 0.3f) {
                    r = 1.0f;
                    g = 0.6f;
                    b = 0.1f;
                    a = intensity * 0.6f;
                } else if (t < 0.6f) {
                    float mt = (t - 0.3f) / 0.3f;
                    r = 1.0f;
                    g = 0.6f - mt * 0.3f;
                    b = 0.1f + mt * 0.15f;
                    a = intensity * (0.55f - mt * 0.05f);
                } else {
                    float tt = (t - 0.6f) / 0.4f;
                    r = 1.0f - tt * 0.3f;
                    g = 0.3f - tt * 0.2f;
                    b = 0.25f + tt * 0.65f;
                    a = intensity * (0.5f - tt * 0.25f);
                }

                buffer.addVertex(matrix,
                                (float)(pos1.x + perp.x),
                                (float)(pos1.y),
                                (float)(pos1.z + perp.z))
                        .setColor(r, g, b, a);

                buffer.addVertex(matrix,
                                (float)(pos1.x - perp.x),
                                (float)(pos1.y),
                                (float)(pos1.z - perp.z))
                        .setColor(r, g, b, a);

                buffer.addVertex(matrix,
                                (float)(pos2.x - perp.x),
                                (float)(pos2.y),
                                (float)(pos2.z - perp.z))
                        .setColor(r * 0.95f, g * 0.95f, b, a * 0.95f);

                buffer.addVertex(matrix,
                                (float)(pos2.x + perp.x),
                                (float)(pos2.y),
                                (float)(pos2.z + perp.z))
                        .setColor(r * 0.95f, g * 0.95f, b, a * 0.95f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFlameSpirals(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (FlameSpiral spiral : flameSpirals) {
            spiral.update(expansion, intensity);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i < spiral.points.size() - 1; i++) {
                Vec3 current = spiral.points.get(i);
                Vec3 next = spiral.points.get(i + 1);

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.8f);

                float t = (float) i / spiral.points.size();
                float alpha = spiral.alpha * (1f - t * 0.3f) * 0.6f;

                // Vibrant fire colors with purple variation
                float r, g, b;
                if (spiral.isPurple) {
                    r = 0.75f + t * 0.15f;
                    g = 0.2f + t * 0.1f;
                    b = 0.9f;
                } else {
                    if (t < 0.5f) {
                        r = 1.0f;
                        g = 0.6f - t * 0.3f;
                        b = 0.1f;
                    } else {
                        r = 1.0f - (t - 0.5f) * 0.25f;
                        g = 0.45f - (t - 0.5f) * 0.3f;
                        b = 0.1f + (t - 0.5f) * 0.4f;
                    }
                }

                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y + perp.y),
                                (float)(current.z + perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y - perp.y),
                                (float)(current.z - perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y - perp.y),
                                (float)(next.z - perp.z))
                        .setColor(r * 0.9f, g * 0.9f, b * 0.95f, alpha * 0.85f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y + perp.y),
                                (float)(next.z + perp.z))
                        .setColor(r * 0.9f, g * 0.9f, b * 0.95f, alpha * 0.85f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderOuterVortex(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float height = expansion * 60f;
        int segments = 48;
        Matrix4f matrix = poseStack.last().pose();

        // Outer swirling vortex shell with fire colors
        for (int layer = 0; layer < 3; layer++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float layerMult = 1f + layer * 0.4f;

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float angle = t * (float) Math.PI * 2 - currentTick * 0.12f * layerMult;
                float heightPos = t * height;

                float radius = (6f + expansion * 40f) * (1f + t * 2.5f) * layerMult;

                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;

                // Fire colors for outer edge
                float r, g, b, a;
                if (t < 0.4f) {
                    r = 1.0f;
                    g = 0.7f - t * 0.3f;
                    b = 0.2f;
                    a = intensity * (0.6f - t * 0.2f);
                } else {
                    float tt = (t - 0.4f) / 0.6f;
                    r = 1.0f - tt * 0.2f;
                    g = 0.4f - tt * 0.2f;
                    b = 0.2f + tt * 0.6f;
                    a = intensity * (0.4f - tt * 0.35f);
                }

                buffer.addVertex(matrix, x, heightPos, z).setColor(r, g, b, a);
                buffer.addVertex(matrix, 0, heightPos, 0).setColor(r * 1.3f, g * 1.3f, Math.min(b * 1.2f, 1f), 0f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireWaves(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (FireWave wave : fireWaves) {
            wave.update(expansion, intensity);

            if (wave.progress < 0 || wave.progress > 1f) continue;

            float innerRadius = wave.progress * 45f;
            float outerRadius = innerRadius + 6f;
            int segments = 96;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= segments; i++) {
                float angle = (float) (i * 2 * Math.PI / segments);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float x1 = cos * innerRadius;
                float z1 = sin * innerRadius;
                float x2 = cos * outerRadius;
                float z2 = sin * outerRadius;

                float alpha = wave.alpha * (1f - wave.progress) * 0.9f;

                // Bright orange-yellow fire wave
                buffer.addVertex(matrix, x1, 0.3f, z1).setColor(1.0f, 0.8f, 0.2f, alpha);
                buffer.addVertex(matrix, x2, 0.3f, z2).setColor(1.0f, 0.5f, 0.1f, 0f);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEmberParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (EmberParticle particle : emberParticles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float r = particle.isPurple ? 0.8f : 1.0f;
            float g = particle.isPurple ? 0.4f : 0.7f;
            float b = particle.isPurple ? 0.9f : 0.3f;

            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x - size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y + size), (float)(particle.pos.z + size))
                    .setColor(r, g, b, particle.alpha);
            buffer.addVertex(matrix, (float)(particle.pos.x + size), (float)(particle.pos.y - size), (float)(particle.pos.z - size))
                    .setColor(r, g, b, particle.alpha);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderHeatDistortion(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float height = expansion * 60f;

        // Semi-transparent heat haze layers
        for (int wave = 0; wave < 5; wave++) {
            float waveProgress = (expansion + wave * 0.2f) % 1f;
            float radius = 5f + waveProgress * 40f;
            int segments = 48;

            Tesselator tesselator = Tesselator.getInstance();

            for (int i = 0; i < segments; i++) {
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float h1 = ((float) i / segments) * height;
                float h2 = ((float) (i + 1) / segments) * height;

                for (int j = 0; j <= 40; j++) {
                    float angle = (float) (j * Math.PI * 2 / 40);
                    float cos = (float) Math.cos(angle);
                    float sin = (float) Math.sin(angle);

                    float x = cos * radius * (1f + (float) i / segments);
                    float z = sin * radius * (1f + (float) i / segments);

                    float a = intensity * (1f - waveProgress) * 0.25f * (1f - (float) i / segments);

                    buffer.addVertex(matrix, x, h1, z).setColor(1.0f, 0.7f, 0.4f, a);
                    buffer.addVertex(matrix, x, h2, z).setColor(1.0f, 0.7f, 0.4f, a);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFlameTendrils(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.3f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        float maxHeight = expansion * 60f;

        // Flame tendrils shooting up from the vortex
        for (int i = 0; i < 40; i++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float angle = (float) (i * Math.PI * 2 / 40 + currentTick * 0.06f);
            float radius = 4f + expansion * 18f;

            Vec3 start = new Vec3(
                    Math.cos(angle) * radius,
                    0,
                    Math.sin(angle) * radius
            );

            int segments = 15;
            Vec3 current = start;

            for (int seg = 0; seg < segments; seg++) {
                float t = (float) (seg + 1) / segments;
                Vec3 next = new Vec3(
                        start.x * (1f - t * 0.6f),
                        t * maxHeight * 0.7f,
                        start.z * (1f - t * 0.6f)
                ).add(
                        (random.nextDouble() - 0.5) * 2.0,
                        0,
                        (random.nextDouble() - 0.5) * 2.0
                );

                Vec3 dir = next.subtract(current).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.6f);

                float alpha = intensity * (1f - t * 0.5f) * 0.85f;

                // Bright fire colors
                float r, g, b;
                if (t < 0.4f) {
                    r = 1.0f;
                    g = 0.9f - t * 0.3f;
                    b = 0.4f - t * 0.3f;
                } else if (t < 0.7f) {
                    float mt = (t - 0.4f) / 0.3f;
                    r = 1.0f;
                    g = 0.75f - mt * 0.3f;
                    b = 0.1f + mt * 0.1f;
                } else {
                    float tt = (t - 0.7f) / 0.3f;
                    r = 1.0f - tt * 0.3f;
                    g = 0.45f - tt * 0.2f;
                    b = 0.2f + tt * 0.7f;
                }

                buffer.addVertex(matrix,
                                (float)(current.x + perp.x),
                                (float)(current.y),
                                (float)(current.z + perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(current.x - perp.x),
                                (float)(current.y),
                                (float)(current.z - perp.z))
                        .setColor(r, g, b, alpha);

                buffer.addVertex(matrix,
                                (float)(next.x - perp.x),
                                (float)(next.y),
                                (float)(next.z - perp.z))
                        .setColor(r * 0.8f, g * 0.8f, b, alpha * 0.7f);

                buffer.addVertex(matrix,
                                (float)(next.x + perp.x),
                                (float)(next.y),
                                (float)(next.z + perp.z))
                        .setColor(r * 0.8f, g * 0.8f, b, alpha * 0.7f);

                current = next;
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderVortexColumn(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        float height = expansion * 60f;
        int segments = 60;
        Matrix4f matrix = poseStack.last().pose();

        // Main vortex column with swirling patterns
        for (int i = 0; i < segments; i++) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            float h1 = ((float) i / segments) * height;
            float h2 = ((float) (i + 1) / segments) * height;
            float t = (float) i / segments;

            for (int j = 0; j <= 40; j++) {
                float angle = (float) (j * Math.PI * 2 / 40) + currentTick * 0.1f + t * 2.5f;
                float radius = (3f + expansion * 25f) * (1f + t * 2f);

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float x1 = cos * radius;
                float z1 = sin * radius;

                // Vibrant fire gradient
                float r, g, b, a;
                if (t < 0.35f) {
                    r = 1.0f;
                    g = 0.85f - t * 0.3f;
                    b = 0.4f - t * 0.3f;
                    a = intensity * 0.7f;
                } else if (t < 0.7f) {
                    float mt = (t - 0.35f) / 0.35f;
                    r = 1.0f;
                    g = 0.7f - mt * 0.3f;
                    b = 0.1f + mt * 0.1f;
                    a = intensity * (0.65f - mt * 0.1f);
                } else {
                    float tt = (t - 0.7f) / 0.3f;
                    r = 1.0f - tt * 0.25f;
                    g = 0.4f - tt * 0.2f;
                    b = 0.2f + tt * 0.7f;
                    a = intensity * (0.55f - tt * 0.3f);
                }

                buffer.addVertex(matrix, x1, h1, z1).setColor(r, g, b, a);
                buffer.addVertex(matrix, x1, h2, z1).setColor(r, g, b, a);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderInfernoRings(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Rotating rings of fire around the vortex
        for (int ring = 0; ring < 18; ring++) {
            poseStack.pushPose();

            float angle = currentTick * 5f + ring * 20f;
            float heightPos = (ring / 18f) * expansion * 55f;
            poseStack.translate(0, heightPos, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(12f));

            float radius = 5f + expansion * 22f + (ring % 3) * 2.5f;
            int segments = 80;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            Matrix4f ringMatrix = poseStack.last().pose();

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float ringAngle = t * (float) Math.PI * 2;
                float cos = (float) Math.cos(ringAngle);
                float sin = (float) Math.sin(ringAngle);

                float x = cos * radius;
                float y = sin * radius;

                float alpha = intensity * 0.7f * (float) Math.sin(t * Math.PI);

                // Fire colored rings
                float heightRatio = (float) ring / 18f;
                float r, g, b;
                if (heightRatio < 0.5f) {
                    r = 1.0f;
                    g = 0.8f - heightRatio * 0.4f;
                    b = 0.3f - heightRatio * 0.2f;
                } else {
                    r = 1.0f - (heightRatio - 0.5f) * 0.3f;
                    g = 0.6f - (heightRatio - 0.5f) * 0.3f;
                    b = 0.2f + (heightRatio - 0.5f) * 0.6f;
                }

                buffer.addVertex(ringMatrix, x, y - 0.5f, 0).setColor(r, g, b, alpha);
                buffer.addVertex(ringMatrix, x, y + 0.5f, 0).setColor(r, g, b, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireBurst(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        // Explosive fire bursts radiating outward
        for (int i = 0; i < 120; i++) {
            float angle = (float) (i * Math.PI * 2 / 120);
            float length = expansion * 45f * (0.8f + (float) Math.sin(currentTick * 0.25f + i) * 0.2f);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float width = 0.8f * intensity;
            float perpX = -sin * width;
            float perpZ = cos * width;

            float startDist = 2.5f;

            float alpha1 = intensity * 0.9f;
            float alpha2 = 0f;

            // Bright fire bursts with variation
            boolean isPurple = i % 4 == 0;
            float r, g, b;
            if (isPurple) {
                r = 0.8f;
                g = 0.4f;
                b = 0.95f;
            } else {
                r = 1.0f;
                g = 0.75f - (i % 3) * 0.15f;
                b = 0.2f;
            }

            buffer.addVertex(matrix, cos * startDist + perpX, 0, sin * startDist + perpZ)
                    .setColor(r, g, b, alpha1);
            buffer.addVertex(matrix, cos * startDist - perpX, 0, sin * startDist - perpZ)
                    .setColor(r, g, b, alpha1);
            buffer.addVertex(matrix, cos * length - perpX, 0, sin * length - perpZ)
                    .setColor(r * 0.6f, g * 0.6f, b * 0.9f, alpha2);
            buffer.addVertex(matrix, cos * length + perpX, 0, sin * length + perpZ)
                    .setColor(r * 0.6f, g * 0.6f, b * 0.9f, alpha2);

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    // Helper classes for animated elements
    private class FlameSpiral {
        List<Vec3> points = new ArrayList<>();
        float alpha = 0f;
        float targetHeight;
        float currentHeight = 0f;
        boolean isPurple;
        float angleOffset;

        FlameSpiral() {
            targetHeight = 30f + random.nextFloat() * 25f;
            isPurple = random.nextFloat() < 0.3f; // 30% purple, 70% orange
            angleOffset = random.nextFloat() * 360f;
            regeneratePoints();
        }

        void regeneratePoints() {
            points.clear();

            int segments = 20;
            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float angle = (float) Math.toRadians(angleOffset + t * 900f);
                float radius = (2.5f + t * 15f);
                float height = t * currentHeight;

                Vec3 point = new Vec3(
                        Math.cos(angle) * radius + (random.nextDouble() - 0.5) * 1.0,
                        height,
                        Math.sin(angle) * radius + (random.nextDouble() - 0.5) * 1.0
                );
                points.add(point);
            }
        }

        void update(float expansion, float intensity) {
            currentHeight = expansion * targetHeight;
            alpha = intensity * (0.7f + random.nextFloat() * 0.3f);

            if (random.nextFloat() < 0.03f) {
                regeneratePoints();
            }
        }
    }

    private class EmberParticle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        boolean isPurple;

        EmberParticle() {
            reset();
        }

        void reset() {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float radius = random.nextFloat() * 4f;
            float height = random.nextFloat() * 6f;

            pos = new Vec3(
                    Math.cos(angle) * radius,
                    height,
                    Math.sin(angle) * radius
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.5,
                    random.nextDouble() * 1.0 + 0.5,
                    (random.nextDouble() - 0.5) * 0.5
            );

            size = 0.2f + random.nextFloat() * 0.25f;
            maxLifetime = 35f + random.nextFloat() * 60f;
            lifetime = 0f;
            alpha = 0f;
            isPurple = random.nextFloat() < 0.25f; // 25% purple, 75% orange
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            // Spiral upward motion
            float angle = lifetime * 0.18f;
            float spiralRadius = 2.5f + lifetime * 0.4f;
            pos = pos.add(
                    Math.cos(angle) * 0.12,
                    velocity.y * expansion,
                    Math.sin(angle) * 0.12
            );

            velocity = velocity.add(0, 0.025, 0);

            alpha = intensity * (float) Math.sin(progress * Math.PI) * 0.95f;
        }
    }

    private class FireWave {
        float progress = 0f;
        float alpha = 0f;
        float offset;

        FireWave(float offset) {
            this.offset = offset;
        }

        void update(float expansion, float intensity) {
            progress = expansion - offset;
            alpha = intensity;
        }
    }

    private class FlameWisp {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        boolean isHot;

        FlameWisp() {
            reset();
        }

        void reset() {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float radius = random.nextFloat() * 5f;
            float height = random.nextFloat() * 3f;

            pos = new Vec3(
                    Math.cos(angle) * radius,
                    height,
                    Math.sin(angle) * radius
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.3,
                    random.nextDouble() * 0.6 + 0.3,
                    (random.nextDouble() - 0.5) * 0.3
            );

            size = 0.3f + random.nextFloat() * 0.4f;
            maxLifetime = 25f + random.nextFloat() * 40f;
            lifetime = 0f;
            alpha = 0f;
            isHot = random.nextFloat() < 0.7f; // 70% hot (yellow-orange), 30% cooler (orange-red)
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            // Wavy upward motion like real flames
            float waveX = (float) Math.sin(lifetime * 0.15f) * 0.15f;
            float waveZ = (float) Math.cos(lifetime * 0.12f) * 0.15f;

            pos = pos.add(
                    waveX + velocity.x,
                    velocity.y * expansion * 1.2f,
                    waveZ + velocity.z
            );

            velocity = velocity.add(0, 0.015, 0);

            // Fade in and out
            if (progress < 0.2f) {
                alpha = intensity * (progress / 0.2f) * 0.9f;
            } else if (progress > 0.7f) {
                alpha = intensity * ((1f - progress) / 0.3f) * 0.9f;
            } else {
                alpha = intensity * 0.9f;
            }
        }
    }
}