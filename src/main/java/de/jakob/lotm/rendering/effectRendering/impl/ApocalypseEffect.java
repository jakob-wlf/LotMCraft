package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ApocalypseEffect extends ActiveEffect {

    private float expansionProgress = 0f;
    private float apocalypseIntensity = 1f;
    private final RandomSource random = RandomSource.create();
    private final List<VoidParticle> voidParticles = new ArrayList<>();
    private final List<RealityTear> realityTears = new ArrayList<>();
    private final List<CollapsingRing> collapsingRings = new ArrayList<>();
    private final List<ChaosLightning> chaosLightning = new ArrayList<>();
    private final List<VoidTentacle> voidTentacles = new ArrayList<>();
    private final List<EnergySpiral> energySpirals = new ArrayList<>();

    // === Cached sphere trig data ===
    private static final int SPHERE_SEGMENTS = 24; // was 32/36/40, this is enough for VFX

    private static final float[] SPHERE_THETA       = new float[SPHERE_SEGMENTS + 1];
    private static final float[] SPHERE_PHI         = new float[SPHERE_SEGMENTS + 1];
    private static final float[] SPHERE_SIN_THETA   = new float[SPHERE_SEGMENTS + 1];
    private static final float[] SPHERE_COS_THETA   = new float[SPHERE_SEGMENTS + 1];
    private static final float[] SPHERE_SIN_PHI     = new float[SPHERE_SEGMENTS + 1];
    private static final float[] SPHERE_COS_PHI     = new float[SPHERE_SEGMENTS + 1];

    static {
        // Precompute theta (0..π)
        for (int i = 0; i <= SPHERE_SEGMENTS; i++) {
            float theta = (float) (Math.PI * i / SPHERE_SEGMENTS);
            SPHERE_THETA[i]     = theta;
            SPHERE_SIN_THETA[i] = Mth.sin(theta);
            SPHERE_COS_THETA[i] = Mth.cos(theta);
        }

        // Precompute phi (0..2π)
        for (int i = 0; i <= SPHERE_SEGMENTS; i++) {
            float phi = (float) (2.0 * Math.PI * i / SPHERE_SEGMENTS);
            SPHERE_PHI[i]     = phi;
            SPHERE_SIN_PHI[i] = Mth.sin(phi);
            SPHERE_COS_PHI[i] = Mth.cos(phi);
        }
    }

    // Helper using cached trig
    private static Vec3 spherePointCached(float radius, int thetaIndex, int phiIndex) {
        float sinTheta = SPHERE_SIN_THETA[thetaIndex];
        float cosTheta = SPHERE_COS_THETA[thetaIndex];
        float sinPhi   = SPHERE_SIN_PHI[phiIndex];
        float cosPhi   = SPHERE_COS_PHI[phiIndex];

        float x = radius * sinTheta * cosPhi;
        float y = radius * cosTheta;
        float z = radius * sinTheta * sinPhi;

        return new Vec3(x, y, z);
    }

    @FunctionalInterface
    private interface SphereColorFunction {
        void apply(BufferBuilder buffer, Matrix4f matrix, Vec3 p1, Vec3 p2, float theta1, float phi);
    }

    private void buildSphereLayer(
            BufferBuilder buffer,
            Matrix4f matrix,
            float radius,
            float intensity,
            SphereColorFunction colorFunc
    ) {
        int segments = SPHERE_SEGMENTS;

        for (int lat = 0; lat < segments; lat++) {
            int thetaIdx1 = lat;
            int thetaIdx2 = lat + 1;
            float theta1 = SPHERE_THETA[thetaIdx1];

            for (int lon = 0; lon <= segments; lon++) {
                int phiIdx = lon;
                float phi = SPHERE_PHI[phiIdx];

                Vec3 p1 = spherePointCached(radius, thetaIdx1, phiIdx);
                Vec3 p2 = spherePointCached(radius, thetaIdx2, phiIdx);

                colorFunc.apply(buffer, matrix, p1, p2, theta1, phi);
            }
        }
    }


    public ApocalypseEffect(double x, double y, double z) {
        super(x, y, z, 140); // 7 seconds of total annihilation

        // Initialize void particles - MASSIVE amount for density
        for (int i = 0; i < 800; i++) {
            voidParticles.add(new VoidParticle());
        }

        // Initialize reality tears
        for (int i = 0; i < 30; i++) {
            realityTears.add(new RealityTear());
        }

        // Initialize collapsing rings
        for (int i = 0; i < 14; i++) {
            collapsingRings.add(new CollapsingRing(i * 0.04f));
        }

        // Initialize chaos lightning
        for (int i = 0; i < 40; i++) {
            chaosLightning.add(new ChaosLightning());
        }

        // Initialize void tentacles - new visual element
        for (int i = 0; i < 25; i++) {
            voidTentacles.add(new VoidTentacle());
        }

        // Initialize energy spirals
        for (int i = 0; i < 15; i++) {
            energySpirals.add(new EnergySpiral(i));
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Update animation state
        float progress = getProgress();
        expansionProgress = Mth.clamp(progress * 1.1f, 0f, 1f);
        apocalypseIntensity = (float) Math.max(0f, 1f - Math.pow(progress, 0.35));

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // Render all effect layers - MANY layers for density
        renderDenseFog(poseStack, expansionProgress, apocalypseIntensity);
        renderVoidCore(poseStack, expansionProgress, apocalypseIntensity);
        renderInnerDarkness(poseStack, expansionProgress, apocalypseIntensity);
        renderCollapsingRings(poseStack, expansionProgress, apocalypseIntensity);
        renderVoidTentacles(poseStack, expansionProgress, apocalypseIntensity);
        renderEnergySpirals(poseStack, expansionProgress, apocalypseIntensity);
        renderRealityTears(poseStack, expansionProgress, apocalypseIntensity);
        renderChaosLightning(poseStack, expansionProgress, apocalypseIntensity);
        renderVoidParticles(poseStack, expansionProgress, apocalypseIntensity);
        renderOuterShell(poseStack, expansionProgress, apocalypseIntensity);

        poseStack.popPose();
    }

    private void renderDenseFog(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.1f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        for (int fogLayer = 0; fogLayer < 8; fogLayer++) {
            float baseRadius = 6f + expansion * 44f;
            float radius = baseRadius + fogLayer * 3f;
            float alpha = intensity * (0.25f - fogLayer * 0.025f);
            if (alpha <= 0.0f) continue;

            float timeOffset = currentTick * 0.03f + fogLayer * 0.5f;

            BufferBuilder buffer =
                    tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            buildSphereLayer(buffer, matrix, radius, intensity,
                    (buf, mat, p1, p2, theta1, phi) -> {
                        float swirl = (float) Math.sin(phi * 4 + theta1 * 3 + timeOffset) * 0.5f + 0.5f;

                        float r = 0.1f + swirl * 0.15f;
                        float g = 0.02f;
                        float b = 0.15f + swirl * 0.2f;

                        buf.addVertex(mat, (float)p1.x, (float)p1.y, (float)p1.z).setColor(r, g, b, alpha);
                        buf.addVertex(mat, (float)p2.x, (float)p2.y, (float)p2.z).setColor(r, g, b, alpha);
                    }
            );

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }




    private void renderVoidCore(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.1f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        float baseRadius = 8f + expansion * 42f;
        float pulseRadius = baseRadius + (float) Math.sin(currentTick * 0.3f) * 2f;

        for (int layer = 0; layer < 5; layer++) {
            float scale = 1f - layer * 0.08f;
            float radius = pulseRadius * scale;
            float alpha = intensity * (0.5f - layer * 0.08f);
            if (alpha <= 0.0f) continue;

            BufferBuilder buffer =
                    tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            buildSphereLayer(buffer, matrix, radius, intensity,
                    (buf, mat, p1, p2, theta1, phi) -> {
                        float distortion =
                                (float) Math.sin(currentTick * 0.08f + phi * 3 + theta1 * 2) * 0.3f;

                        Vec3 d1 = new Vec3(p1.x + distortion, p1.y + distortion, p1.z + distortion);
                        Vec3 d2 = new Vec3(p2.x + distortion, p2.y + distortion, p2.z + distortion);

                        float vein = (float) Math.sin(phi * 6 + theta1 * 4 + currentTick * 0.1f);
                        boolean isVein = vein > 0.6f;

                        float r = isVein ? 0.05f : 0.35f;
                        float g = isVein ? 0.02f : 0.08f;
                        float b = isVein ? 0.08f : 0.6f;

                        buf.addVertex(mat, (float)d1.x, (float)d1.y, (float)d1.z).setColor(r, g, b, alpha);
                        buf.addVertex(mat, (float)d2.x, (float)d2.y, (float)d2.z).setColor(r, g, b, alpha);
                    }
            );

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }



    private void renderInnerDarkness(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.1f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        float radius = 5f + expansion * 38f;
        float alpha = intensity * 0.7f;

        BufferBuilder buffer =
                tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        buildSphereLayer(buffer, matrix, radius, intensity,
                (buf, mat, p1, p2, theta, phi) -> {
                    buf.addVertex(mat, (float)p1.x, (float)p1.y, (float)p1.z).setColor(0.02f, 0f, 0.05f, alpha);
                    buf.addVertex(mat, (float)p2.x, (float)p2.y, (float)p2.z).setColor(0.02f, 0f, 0.05f, alpha);
                }
        );

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }



    private void renderCollapsingRings(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (CollapsingRing ring : collapsingRings) {
            ring.update(expansion, intensity);

            if (ring.alpha <= 0.05f) continue;

            float radius = ring.radius;
            int segments = 80;

            // Render thick rings with multiple passes
            for (int pass = 0; pass < 3; pass++) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float thickness = 0.8f + pass * 0.3f;

                for (int i = 0; i <= segments; i++) {
                    float angle = (float) (i * 2 * Math.PI / segments);
                    float x = (float) Math.cos(angle) * radius;
                    float z = (float) Math.sin(angle) * radius;
                    float y = (float) Math.sin(angle * 3 + currentTick * 0.15f) * 1.5f;

                    float pulse = (float) Math.sin(currentTick * 0.25f + i * 0.08f) * 0.4f + 0.6f;
                    float alpha = ring.alpha * (1f - pass * 0.2f);

                    buffer.addVertex(matrix, x, y - thickness, z)
                            .setColor(0.5f * pulse, 0.08f, 0.7f * pulse, alpha);
                    buffer.addVertex(matrix, x, y + thickness, z)
                            .setColor(0.3f * pulse, 0.05f, 0.5f * pulse, alpha);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderVoidTentacles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (VoidTentacle tentacle : voidTentacles) {
            tentacle.update(expansion, intensity);

            if (tentacle.alpha <= 0.05f || tentacle.segments.size() < 2) continue;

            // Render tentacle as a thick tube
            for (int i = 0; i < tentacle.segments.size() - 1; i++) {
                Vec3 p1 = tentacle.segments.get(i);
                Vec3 p2 = tentacle.segments.get(i + 1);

                Vec3 dir = p2.subtract(p1).normalize();
                Vec3 perp1 = new Vec3(-dir.z, 0, dir.x).normalize();
                Vec3 perp2 = new Vec3(0, 1, 0).cross(dir).normalize();

                float width = 0.6f * (1f - (float)i / tentacle.segments.size());

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                // Black tentacle with purple edges
                float edgeR = 0.4f;
                float edgeG = 0.05f;
                float edgeB = 0.6f;
                float coreR = 0.05f;
                float coreG = 0.02f;
                float coreB = 0.1f;

                // Four sides of the tentacle
                Vec3[] offsets = {
                        perp1.scale(width),
                        perp2.scale(width),
                        perp1.scale(-width),
                        perp2.scale(-width)
                };

                for (int side = 0; side < 4; side++) {
                    Vec3 off1 = offsets[side];
                    Vec3 off2 = offsets[(side + 1) % 4];

                    buffer.addVertex(matrix,
                                    (float)(p1.x + off1.x), (float)(p1.y + off1.y), (float)(p1.z + off1.z))
                            .setColor(edgeR, edgeG, edgeB, tentacle.alpha);
                    buffer.addVertex(matrix,
                                    (float)(p1.x + off2.x), (float)(p1.y + off2.y), (float)(p1.z + off2.z))
                            .setColor(coreR, coreG, coreB, tentacle.alpha);
                    buffer.addVertex(matrix,
                                    (float)(p2.x + off2.x), (float)(p2.y + off2.y), (float)(p2.z + off2.z))
                            .setColor(coreR, coreG, coreB, tentacle.alpha);
                    buffer.addVertex(matrix,
                                    (float)(p2.x + off1.x), (float)(p2.y + off1.y), (float)(p2.z + off1.z))
                            .setColor(edgeR, edgeG, edgeB, tentacle.alpha);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEnergySpirals(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (EnergySpiral spiral : energySpirals) {
            spiral.update(expansion, intensity);

            if (spiral.alpha <= 0.05f) continue;

            int segments = 100;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= segments; i++) {
                float t = (float) i / segments;
                float angle = t * (float) Math.PI * 8 + spiral.rotationOffset;
                float radius = t * spiral.maxRadius * expansion;

                float x = (float) Math.cos(angle) * radius;
                float z = (float) Math.sin(angle) * radius;
                float y = (float) Math.sin(t * Math.PI * 4 + currentTick * 0.1f) * 8f;

                float brightness = (float) Math.sin(t * Math.PI) * 0.6f + 0.4f;
                buffer.addVertex(matrix, x, y, z)
                        .setColor(0.6f * brightness, 0.1f * brightness, 0.8f * brightness, spiral.alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderRealityTears(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (RealityTear tear : realityTears) {
            tear.update(expansion, intensity);

            if (tear.alpha <= 0.05f || tear.points.size() < 2) continue;

            // Render tear with multiple width layers
            for (int layer = 0; layer < 3; layer++) {
                float widthMult = 1f + layer * 0.4f;

                for (int i = 0; i < tear.points.size() - 1; i++) {
                    Vec3 p1 = tear.points.get(i);
                    Vec3 p2 = tear.points.get(i + 1);

                    Vec3 dir = p2.subtract(p1).normalize();
                    Vec3 perpendicular = new Vec3(-dir.z, 0, dir.x).normalize().scale(0.5f * widthMult);

                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

                    float layerAlpha = tear.alpha * (1f - layer * 0.25f);

                    buffer.addVertex(matrix,
                                    (float)(p1.x - perpendicular.x), (float)(p1.y - perpendicular.y), (float)(p1.z - perpendicular.z))
                            .setColor(0.6f, 0.1f, 0.8f, layerAlpha);
                    buffer.addVertex(matrix,
                                    (float)(p1.x + perpendicular.x), (float)(p1.y + perpendicular.y), (float)(p1.z + perpendicular.z))
                            .setColor(0.2f, 0.03f, 0.3f, layerAlpha);
                    buffer.addVertex(matrix,
                                    (float)(p2.x + perpendicular.x), (float)(p2.y + perpendicular.y), (float)(p2.z + perpendicular.z))
                            .setColor(0.05f, 0.01f, 0.1f, layerAlpha);
                    buffer.addVertex(matrix,
                                    (float)(p2.x - perpendicular.x), (float)(p2.y - perpendicular.y), (float)(p2.z - perpendicular.z))
                            .setColor(0.05f, 0.01f, 0.1f, layerAlpha);

                    BufferUploader.drawWithShader(buffer.buildOrThrow());
                }
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderChaosLightning(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (ChaosLightning lightning : chaosLightning) {
            lightning.update(expansion, intensity);

            if (lightning.alpha <= 0.05f || lightning.points.size() < 2) continue;

            // Render lightning with glow effect
            for (int pass = 0; pass < 2; pass++) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                float alphaMult = pass == 0 ? 1f : 0.4f;

                for (Vec3 point : lightning.points) {
                    buffer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                            .setColor(0.8f, 0.2f, 1.0f, lightning.alpha * alphaMult);
                }

                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderVoidParticles(PoseStack poseStack, float expansion, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (VoidParticle particle : voidParticles) {
            particle.update(expansion, intensity);

            if (particle.alpha <= 0.05f) continue;

            float size = particle.size;

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float r = particle.isPurple ? 0.6f : 0.08f;
            float g = particle.isPurple ? 0.1f : 0.03f;
            float b = particle.isPurple ? 0.8f : 0.12f;

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

    private void renderOuterShell(PoseStack poseStack, float expansion, float intensity) {
        if (intensity < 0.1f) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        float radius = 10f + expansion * 45f;
        float baseAlpha = intensity * 0.3f;

        BufferBuilder buffer =
                tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        buildSphereLayer(buffer, matrix, radius, intensity,
                (buf, mat, p1, p2, theta1, phi) -> {
                    float crackle =
                            (float) Math.sin(phi * 8 + theta1 * 6 + currentTick * 0.4f) * 0.5f + 0.5f;

                    float r = 0.4f + crackle * 0.3f;
                    float g = 0.1f;
                    float b = 0.6f + crackle * 0.3f;

                    buf.addVertex(mat, (float)p1.x, (float)p1.y, (float)p1.z).setColor(r, g, b, baseAlpha);
                    buf.addVertex(mat, (float)p2.x, (float)p2.y, (float)p2.z).setColor(r, g, b, baseAlpha);
                }
        );

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }



    // Helper classes for animated elements
    private class VoidParticle {
        Vec3 pos;
        Vec3 velocity;
        float alpha;
        float size;
        float lifetime;
        float maxLifetime;
        boolean isPurple;

        VoidParticle() {
            reset();
        }

        void reset() {
            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float dist = random.nextFloat() * 8f;

            pos = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * dist,
                    Math.cos(theta) * dist,
                    Math.sin(theta) * Math.sin(phi) * dist
            );

            velocity = new Vec3(
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5
            );

            size = 0.2f + random.nextFloat() * 0.3f;
            maxLifetime = 35f + random.nextFloat() * 70f;
            lifetime = 0f;
            alpha = 0f;
            isPurple = random.nextFloat() < 0.75f; // 75% purple, 25% black
        }

        void update(float expansion, float intensity) {
            lifetime++;

            float progress = lifetime / maxLifetime;

            if (progress > 1f) {
                reset();
                return;
            }

            // Particles spiral and drift
            float angle = lifetime * 0.08f;
            Vec3 spiral = new Vec3(
                    Math.cos(angle) * 0.08,
                    Math.sin(lifetime * 0.05f) * 0.04,
                    Math.sin(angle) * 0.08
            );

            pos = pos.add(velocity.scale(expansion * 0.9)).add(spiral);
            velocity = velocity.add(pos.normalize().scale(-0.018));

            alpha = intensity * (float) Math.sin(progress * Math.PI) * 0.8f;
        }
    }

    private class RealityTear {
        List<Vec3> points = new ArrayList<>();
        float alpha = 0f;
        float maxLength;
        float currentLength = 0f;
        Vec3 direction;
        float lifetime = 0f;

        RealityTear() {
            maxLength = 20f + random.nextFloat() * 40f;
            reset();
        }

        void reset() {
            points.clear();
            lifetime = 0f;

            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;

            direction = new Vec3(
                    Math.sin(theta) * Math.cos(phi),
                    Math.cos(theta),
                    Math.sin(theta) * Math.sin(phi)
            ).normalize();

            points.add(Vec3.ZERO);
        }

        void update(float expansion, float intensity) {
            lifetime++;
            currentLength = expansion * maxLength;
            alpha = intensity * (0.7f + random.nextFloat() * 0.3f);

            if (lifetime % 2 == 0) {
                points.clear();
                points.add(Vec3.ZERO);

                int segments = (int) (currentLength / 1.5);
                for (int i = 1; i <= segments; i++) {
                    float t = (float) i / segments;
                    Vec3 point = direction.scale(currentLength * t)
                            .add(
                                    (random.nextDouble() - 0.5) * 2.0,
                                    (random.nextDouble() - 0.5) * 2.0,
                                    (random.nextDouble() - 0.5) * 2.0
                            );
                    points.add(point);
                }
            }
        }
    }

    private class CollapsingRing {
        float radius;
        float targetRadius;
        float alpha = 0f;
        float offset;

        CollapsingRing(float offset) {
            this.offset = offset;
            targetRadius = 25f + random.nextFloat() * 30f;
        }

        void update(float expansion, float intensity) {
            float progress = Mth.clamp(expansion - offset, 0f, 1f);
            radius = progress * targetRadius;
            alpha = intensity * (1f - progress * 0.7f) * 0.9f;
        }
    }

    private class ChaosLightning {
        List<Vec3> points = new ArrayList<>();
        float alpha = 0f;
        float lifetime = 0f;
        float maxLifetime;

        ChaosLightning() {
            maxLifetime = 10f + random.nextFloat() * 15f;
            reset();
        }

        void reset() {
            points.clear();
            lifetime = 0f;

            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;
            float length = 15f + random.nextFloat() * 35f;

            Vec3 start = new Vec3(
                    Math.sin(theta) * Math.cos(phi) * 8,
                    Math.cos(theta) * 8,
                    Math.sin(theta) * Math.sin(phi) * 8
            );

            Vec3 end = start.add(
                    Math.sin(theta) * Math.cos(phi) * length,
                    Math.cos(theta) * length,
                    Math.sin(theta) * Math.sin(phi) * length
            );

            generateLightningBolt(start, end, 5);
        }

        void generateLightningBolt(Vec3 start, Vec3 end, int depth) {
            if (depth <= 0) {
                points.add(start);
                points.add(end);
                return;
            }

            Vec3 mid = start.add(end).scale(0.5)
                    .add(
                            (random.nextDouble() - 0.5) * 4,
                            (random.nextDouble() - 0.5) * 4,
                            (random.nextDouble() - 0.5) * 4
                    );

            generateLightningBolt(start, mid, depth - 1);
            generateLightningBolt(mid, end, depth - 1);
        }

        void update(float expansion, float intensity) {
            lifetime++;

            if (lifetime > maxLifetime) {
                reset();
            }

            float progress = lifetime / maxLifetime;
            alpha = intensity * (float) Math.sin(progress * Math.PI) * 1.0f;
        }
    }

    private class VoidTentacle {
        List<Vec3> segments = new ArrayList<>();
        float alpha = 0f;
        float maxLength;
        float currentLength = 0f;
        Vec3 direction;
        float swayPhase;
        float lifetime = 0f;

        VoidTentacle() {
            maxLength = 25f + random.nextFloat() * 35f;
            swayPhase = random.nextFloat() * (float) Math.PI * 2;
            reset();
        }

        void reset() {
            segments.clear();
            lifetime = 0f;

            float theta = random.nextFloat() * (float) Math.PI;
            float phi = random.nextFloat() * (float) Math.PI * 2;

            direction = new Vec3(
                    Math.sin(theta) * Math.cos(phi),
                    Math.cos(theta),
                    Math.sin(theta) * Math.sin(phi)
            ).normalize();
        }

        void update(float expansion, float intensity) {
            lifetime++;
            currentLength = expansion * maxLength;
            alpha = intensity * (0.6f + random.nextFloat() * 0.3f);

            if (lifetime % 3 == 0) {
                segments.clear();
                segments.add(Vec3.ZERO);

                int segmentCount = 20;
                for (int i = 1; i <= segmentCount; i++) {
                    float t = (float) i / segmentCount;

                    float sway = (float) Math.sin(t * Math.PI * 3 + lifetime * 0.1f + swayPhase) * 2f;

                    Vec3 segment = direction.scale(currentLength * t)
                            .add(
                                    Math.sin(t * Math.PI * 2 + swayPhase) * sway,
                                    Math.cos(t * Math.PI * 3) * 1.5,
                                    Math.cos(t * Math.PI * 2 + swayPhase) * sway
                            );
                    segments.add(segment);
                }
            }
        }
    }

    private class EnergySpiral {
        float maxRadius;
        float alpha = 0f;
        float rotationOffset;
        int spiralIndex;

        EnergySpiral(int index) {
            spiralIndex = index;
            maxRadius = 30f + random.nextFloat() * 25f;
            rotationOffset = random.nextFloat() * (float) Math.PI * 2;
        }

        void update(float expansion, float intensity) {
            alpha = intensity * (0.5f + random.nextFloat() * 0.4f);
            rotationOffset += 0.02f;
        }
    }
}