package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SpaceFragmentationEffect extends ActiveEffect {
    private static final ResourceLocation DISTORTION_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/obsidian.png");
    private static final ResourceLocation CRACK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/glass.png");

    // Cached data structures for massive visual density
    private final List<MassiveRealityCrack> realityCracks;
    private final List<VortexLayer> vortexLayers;
    private final List<ChaoticEnergyBolt> energyBolts;
    private final float[][][] cachedVortexGeometry; // [layer][segment][xyz]

    // Color constants - much more vibrant
    private static final int[] COLOR_VOID_DEEP = {5, 0, 10};
    private static final int[] COLOR_MAGENTA_BRIGHT = {255, 20, 255};
    private static final int[] COLOR_CYAN_BRIGHT = {20, 255, 255};
    private static final int[] COLOR_MAGENTA_DARK = {120, 0, 140};
    private static final int[] COLOR_CYAN_DARK = {0, 120, 140};
    private static final int[] COLOR_WHITE = {255, 255, 255};

    private static final float EFFECT_RADIUS = 24.0f;
    private static final int DENSITY_MULTIPLIER = 4; // Massive density increase

    public SpaceFragmentationEffect(double x, double y, double z) {
        super(x, y, z, 20 * 12);

        // Pre-calculate vortex geometry - multiple dense layers
        cachedVortexGeometry = calculateVortexGeometry(8, 48, 40);

        // Massive reality cracks that cover the entire area
        realityCracks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            realityCracks.add(new MassiveRealityCrack(i));
        }

        // Multiple vortex layers for depth
        vortexLayers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            vortexLayers.add(new VortexLayer(i));
        }

        // Chaotic energy bolts everywhere
        energyBolts = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            energyBolts.add(new ChaoticEnergyBolt(i));
        }
    }

    private float[][][] calculateVortexGeometry(int layers, int segments, int heightRings) {
        float[][][] geometry = new float[layers][segments * heightRings][3];

        for (int layer = 0; layer < layers; layer++) {
            int idx = 0;
            for (int h = 0; h < heightRings; h++) {
                float heightRatio = h / (float) heightRings;
                for (int s = 0; s < segments; s++) {
                    float segRatio = s / (float) segments;
                    geometry[layer][idx][0] = segRatio;      // normalized segment
                    geometry[layer][idx][1] = heightRatio;   // normalized height
                    geometry[layer][idx][2] = layer;         // layer index
                    idx++;
                }
            }
        }

        return geometry;
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        float progress = getProgress();
        float intensity = calculateIntensity(progress);

        // Render in layers - complete visual chaos
        renderVoidSphere(poseStack, tick, intensity);          // Complete blackout sphere
        renderSecondVoidSphere(poseStack, tick, intensity);          // Complete blackout sphere
        //renderBlackVoidClouds(poseStack, tick, intensity);     // Dense black clouds
        renderMassiveVortex(poseStack, tick, intensity);       // Main spiraling vortex
        renderSmallerVortex(poseStack, tick, intensity);       // Main spiraling vortex
        renderSecondaryVortices(poseStack, tick, intensity);   // Counter-rotating vortices
        //renderRealityCracks(poseStack, tick, intensity);       // Massive cracks everywhere
        //renderBlackVoidPillars(poseStack, tick, intensity);    // Black void columns
        renderEnergyStorm(poseStack, tick, intensity);         // Dense energy bolts
        renderFragmentationWaves(poseStack, tick, intensity);  // Pulsating waves
        renderVoidTendrils(poseStack, tick, intensity);        // Writhing tentacles
        //renderBlackVoidParticles(poseStack, tick, intensity);  // Floating black debris
        renderCatastrophicCore(poseStack, tick, intensity);    // Devastating core

        poseStack.popPose();
    }

    private float calculateIntensity(float progress) {
        if (progress < 0.1f) {
            return progress / 0.1f;
        } else if (progress > 0.9f) {
            return 1.0f - ((progress - 0.9f) / 0.1f);
        }
        return 1.0f;
    }

    private void renderVoidSphere(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, DISTORTION_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull(); // Render from inside too!

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Multiple overlapping void spheres for complete coverage
        for (int sphere = 0; sphere < 6; sphere++) { // Doubled from 3 to 6
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(tick * (0.3f + sphere * 0.2f)));
            poseStack.mulPose(Axis.XP.rotationDegrees(tick * 0.2f * (sphere + 1)));

            Matrix4f pose = poseStack.last().pose();
            int segments = 32;
            int rings = 24;
            float radius = EFFECT_RADIUS * (0.92f + sphere * 0.03f) * 1.35f;

            for (int ring = 0; ring < rings - 1; ring++) {
                for (int seg = 0; seg < segments; seg++) {
                    float phi1 = (ring / (float) rings) * Mth.PI;
                    float phi2 = ((ring + 1) / (float) rings) * Mth.PI;
                    float theta1 = (seg / (float) segments) * Mth.TWO_PI;
                    float theta2 = ((seg + 1) / (float) segments) * Mth.TWO_PI;

                    float distortion = Mth.sin(tick * 0.15f + phi1 * 3 + theta1 * 2) * 0.1f;
                    float r = radius * (1.0f + distortion);

                    float x1 = Mth.sin(phi1) * Mth.cos(theta1) * r;
                    float y1 = Mth.cos(phi1) * r;
                    float z1 = Mth.sin(phi1) * Mth.sin(theta1) * r;

                    float x2 = Mth.sin(phi1) * Mth.cos(theta2) * r;
                    float y2 = Mth.cos(phi1) * r;
                    float z2 = Mth.sin(phi1) * Mth.sin(theta2) * r;

                    float x3 = Mth.sin(phi2) * Mth.cos(theta1) * r;
                    float y3 = Mth.cos(phi2) * r;
                    float z3 = Mth.sin(phi2) * Mth.sin(theta1) * r;

                    float x4 = Mth.sin(phi2) * Mth.cos(theta2) * r;
                    float y4 = Mth.cos(phi2) * r;
                    float z4 = Mth.sin(phi2) * Mth.sin(theta2) * r;

                    // Deep void with magenta/cyan swirls
                    float swirl = Mth.sin(tick * 0.1f + phi1 * 5 + theta1 * 3);
                    int r_col = (int)(COLOR_VOID_DEEP[0] + Math.abs(swirl) * 80);
                    int g_col = (int)(COLOR_VOID_DEEP[1] + (swirl > 0 ? 0 : 100));
                    int b_col = (int)(COLOR_VOID_DEEP[2] + Math.abs(swirl) * 120);
                    int a = (int)(intensity * 220); // Very opaque to block terrain

                    // Render both sides for inside viewing
                    buffer.addVertex(pose, x1, y1, z1).setColor(r_col, g_col, b_col, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y2, z2).setColor(r_col, g_col, b_col, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y4, z4).setColor(r_col, g_col, b_col, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y3, z3).setColor(r_col, g_col, b_col, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                    // Reverse winding for inside
                    buffer.addVertex(pose, x1, y1, z1).setColor(r_col, g_col, b_col, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y3, z3).setColor(r_col, g_col, b_col, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y4, z4).setColor(r_col, g_col, b_col, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y2, z2).setColor(r_col, g_col, b_col, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderSecondVoidSphere(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, DISTORTION_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull(); // Render from inside too!

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Multiple overlapping void spheres for complete coverage
        for (int sphere = 0; sphere < 6; sphere++) { // Doubled from 3 to 6
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(tick * (0.3f + sphere * 0.2f)));
            poseStack.mulPose(Axis.XP.rotationDegrees(tick * 0.2f * (sphere + 1)));

            Matrix4f pose = poseStack.last().pose();
            int segments = 32;
            int rings = 24;
            float radius = EFFECT_RADIUS * (0.92f + sphere * 0.03f);

            for (int ring = 0; ring < rings - 1; ring++) {
                for (int seg = 0; seg < segments; seg++) {
                    float phi1 = (ring / (float) rings) * Mth.PI;
                    float phi2 = ((ring + 1) / (float) rings) * Mth.PI;
                    float theta1 = (seg / (float) segments) * Mth.TWO_PI;
                    float theta2 = ((seg + 1) / (float) segments) * Mth.TWO_PI;

                    float distortion = Mth.sin(tick * 0.15f + phi1 * 3 + theta1 * 2) * 0.1f;
                    float r = radius * (1.0f + distortion);

                    float x1 = Mth.sin(phi1) * Mth.cos(theta1) * r;
                    float y1 = Mth.cos(phi1) * r;
                    float z1 = Mth.sin(phi1) * Mth.sin(theta1) * r;

                    float x2 = Mth.sin(phi1) * Mth.cos(theta2) * r;
                    float y2 = Mth.cos(phi1) * r;
                    float z2 = Mth.sin(phi1) * Mth.sin(theta2) * r;

                    float x3 = Mth.sin(phi2) * Mth.cos(theta1) * r;
                    float y3 = Mth.cos(phi2) * r;
                    float z3 = Mth.sin(phi2) * Mth.sin(theta1) * r;

                    float x4 = Mth.sin(phi2) * Mth.cos(theta2) * r;
                    float y4 = Mth.cos(phi2) * r;
                    float z4 = Mth.sin(phi2) * Mth.sin(theta2) * r;

                    // Deep void with magenta/cyan swirls
                    float swirl = Mth.sin(tick * 0.1f + phi1 * 5 + theta1 * 3);
                    int r_col = (int)(COLOR_VOID_DEEP[0] + Math.abs(swirl) * 80);
                    int g_col = (int)(COLOR_VOID_DEEP[1] + (swirl > 0 ? 0 : 100));
                    int b_col = (int)(COLOR_VOID_DEEP[2] + Math.abs(swirl) * 120);
                    int a = (int)(intensity * 220); // Very opaque to block terrain

                    // Render both sides for inside viewing
                    buffer.addVertex(pose, x1, y1, z1).setColor(r_col, g_col, b_col, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y2, z2).setColor(r_col, g_col, b_col, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y4, z4).setColor(r_col, g_col, b_col, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y3, z3).setColor(r_col, g_col, b_col, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                    // Reverse winding for inside
                    buffer.addVertex(pose, x1, y1, z1).setColor(r_col, g_col, b_col, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y3, z3).setColor(r_col, g_col, b_col, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y4, z4).setColor(r_col, g_col, b_col, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y2, z2).setColor(r_col, g_col, b_col, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }


    private void renderMassiveVortex(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, CRACK_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull(); // Visible from inside too!

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Main vortex - thick, solid, CYLINDRICAL spiraling destruction
        int layers = 7;
        int segments = 48;
        int heightSegments = 35; // Reduced further
        float maxHeight = EFFECT_RADIUS * 0.9f; // Even shorter - 60% of radius
        float vortexRadius = EFFECT_RADIUS * 0.7f; // CONSTANT radius - perfect cylinder

        for (int layer = 0; layer < layers; layer++) {
            poseStack.pushPose();
            float layerRotation = tick * (8.0f - layer * 1.5f) + layer * 72;
            poseStack.mulPose(Axis.YP.rotationDegrees(layerRotation));

            Matrix4f pose = poseStack.last().pose();

            for (int h = 0; h < heightSegments - 1; h++) {
                float t1 = h / (float) heightSegments;
                float t2 = (h + 1) / (float) heightSegments;

                float y1 = (t1 - 0.5f) * maxHeight;
                float y2 = (t2 - 0.5f) * maxHeight;

                float layerOffset = layer * 0.15f;
                float radius1 = vortexRadius * (1.0f + layerOffset);
                float radius2 = vortexRadius * (1.0f + layerOffset);

                float twist1 = t1 * 1080.0f + tick * 15.0f;
                float twist2 = t2 * 1080.0f + tick * 15.0f;

                for (int s = 0; s < segments; s++) {
                    float angle1 = (s / (float) segments) * 360.0f + twist1;
                    float angle2 = ((s + 1) / (float) segments) * 360.0f + twist1;
                    float angle3 = (s / (float) segments) * 360.0f + twist2;
                    float angle4 = ((s + 1) / (float) segments) * 360.0f + twist2;

                    float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float x3 = Mth.cos(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float z3 = Mth.sin(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float x4 = Mth.cos(angle4 * Mth.DEG_TO_RAD) * radius2;
                    float z4 = Mth.sin(angle4 * Mth.DEG_TO_RAD) * radius2;

                    // Alternating magenta and cyan bands
                    boolean isMagenta = ((h / 3 + s / 6 + layer) % 2) == 0;
                    int[] baseColor = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;

                    // Add pulsing
                    float pulse = 1.0f + Mth.sin(tick * 0.3f + h * 0.5f) * 0.3f;
                    int r = (int)(baseColor[0] * pulse);
                    int g = (int)(baseColor[1] * pulse);
                    int b = (int)(baseColor[2] * pulse);
                    int a = (int)(intensity * 240); // Very solid

                    // Outside faces
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                    // Inside faces (reversed winding)
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderSmallerVortex(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, CRACK_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull(); // Visible from inside too!

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Main vortex - thick, solid, CYLINDRICAL spiraling destruction
        int layers = 4;
        int segments = 48;
        int heightSegments = 15; // Reduced further
        float maxHeight = EFFECT_RADIUS * 0.55f; // Even shorter - 60% of radius
        float vortexRadius = EFFECT_RADIUS * 0.55f;

        for (int layer = 0; layer < layers; layer++) {
            poseStack.pushPose();
            float layerRotation = tick * (8.0f - layer * 1.5f) + layer * 72;
            poseStack.mulPose(Axis.YP.rotationDegrees(layerRotation));

            Matrix4f pose = poseStack.last().pose();

            for (int h = 0; h < heightSegments - 1; h++) {
                float t1 = h / (float) heightSegments;
                float t2 = (h + 1) / (float) heightSegments;

                float y1 = (t1 - 0.5f) * maxHeight;
                float y2 = (t2 - 0.5f) * maxHeight;

                float layerOffset = layer * 0.15f;
                float radius1 = vortexRadius * (1.0f + layerOffset);
                float radius2 = vortexRadius * (1.0f + layerOffset);

                float twist1 = t1 * 1080.0f + tick * 15.0f;
                float twist2 = t2 * 1080.0f + tick * 15.0f;

                for (int s = 0; s < segments; s++) {
                    float angle1 = (s / (float) segments) * 360.0f + twist1;
                    float angle2 = ((s + 1) / (float) segments) * 360.0f + twist1;
                    float angle3 = (s / (float) segments) * 360.0f + twist2;
                    float angle4 = ((s + 1) / (float) segments) * 360.0f + twist2;

                    float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float x3 = Mth.cos(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float z3 = Mth.sin(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float x4 = Mth.cos(angle4 * Mth.DEG_TO_RAD) * radius2;
                    float z4 = Mth.sin(angle4 * Mth.DEG_TO_RAD) * radius2;

                    // Alternating magenta and cyan bands
                    boolean isMagenta = ((h / 3 + s / 6 + layer) % 2) == 0;
                    int[] baseColor = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;

                    // Add pulsing
                    float pulse = 1.0f + Mth.sin(tick * 0.3f + h * 0.5f) * 0.3f;
                    int r = (int)(baseColor[0] * pulse);
                    int g = (int)(baseColor[1] * pulse);
                    int b = (int)(baseColor[2] * pulse);
                    int a = (int)(intensity * 240); // Very solid

                    // Outside faces
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                    // Inside faces (reversed winding)
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderSecondaryVortices(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, CRACK_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull(); // Visible from inside!

        Tesselator tesselator = Tesselator.getInstance();

        // Counter-rotating smaller vortices for chaos
        for (VortexLayer vortex : vortexLayers) {
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            vortex.render(buffer, poseStack, tick, intensity);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderRealityCracks(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, CRACK_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_COLOR);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        for (MassiveRealityCrack crack : realityCracks) {
            crack.render(buffer, poseStack, tick, intensity);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderEnergyStorm(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, DISTORTION_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        Matrix4f pose = poseStack.last().pose();

        for (ChaoticEnergyBolt bolt : energyBolts) {
            bolt.render(buffer, pose, tick, intensity);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderFragmentationWaves(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, DISTORTION_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Expanding rings of destruction
        int numWaves = 8;
        int segments = 64;

        for (int wave = 0; wave < numWaves; wave++) {
            float waveTime = (tick * 0.08f + wave * 0.3f) % 2.0f;
            float waveRadius = EFFECT_RADIUS * waveTime * 0.8f;
            float waveHeight = Mth.sin(waveTime * Mth.PI) * 8.0f;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(wave * 45 + tick * 2));

            Matrix4f pose = poseStack.last().pose();

            for (int s = 0; s < segments; s++) {
                float angle1 = (s / (float) segments) * Mth.TWO_PI;
                float angle2 = ((s + 1) / (float) segments) * Mth.TWO_PI;

                float x1_inner = Mth.cos(angle1) * (waveRadius - 1.0f);
                float z1_inner = Mth.sin(angle1) * (waveRadius - 1.0f);
                float x2_inner = Mth.cos(angle2) * (waveRadius - 1.0f);
                float z2_inner = Mth.sin(angle2) * (waveRadius - 1.0f);

                float x1_outer = Mth.cos(angle1) * (waveRadius + 1.0f);
                float z1_outer = Mth.sin(angle1) * (waveRadius + 1.0f);
                float x2_outer = Mth.cos(angle2) * (waveRadius + 1.0f);
                float z2_outer = Mth.sin(angle2) * (waveRadius + 1.0f);

                boolean isMagenta = (wave % 2) == 0;
                int[] color = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;
                int r = color[0];
                int g = color[1];
                int b = color[2];
                int a = (int)(intensity * 200 * (1.0f - waveTime * 0.5f));

                buffer.addVertex(pose, x1_inner, waveHeight, z1_inner).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2_inner, waveHeight, z2_inner).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2_outer, waveHeight, z2_outer).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x1_outer, waveHeight, z1_outer).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderVoidTendrils(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, CRACK_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Writhing tentacles of void energy
        int numTendrils = 24;

        for (int tendril = 0; tendril < numTendrils; tendril++) {
            poseStack.pushPose();

            float baseAngle = (tendril / (float) numTendrils) * 360.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(baseAngle + tick * 3.0f));

            Matrix4f pose = poseStack.last().pose();

            int segments = 40;
            float prevX = 0, prevY = 0, prevZ = 0;

            for (int i = 0; i <= segments; i++) {
                float t = i / (float) segments;

                float radius = EFFECT_RADIUS * 0.9f * t;
                float angle = t * 540.0f + tick * 5.0f + tendril * 15;

                float x = Mth.cos(angle * Mth.DEG_TO_RAD) * radius;
                float y = (t - 0.5f) * EFFECT_RADIUS * 1.5f + Mth.sin(tick * 0.2f + tendril + t * Mth.PI * 3) * 4.0f;
                float z = Mth.sin(angle * Mth.DEG_TO_RAD) * radius;

                if (i > 0) {
                    float width = 0.8f * (1.0f - t * 0.3f);

                    boolean isMagenta = (tendril % 3) != 1;
                    int[] color = isMagenta ? COLOR_MAGENTA_DARK : COLOR_CYAN_DARK;
                    int r = color[0];
                    int g = color[1];
                    int b = color[2];
                    int a = (int)(intensity * 200 * (1.0f - t * 0.4f));

                    buffer.addVertex(pose, prevX - width, prevY, prevZ).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x - width, y, z).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x + width, y, z).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, prevX + width, prevY, prevZ).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                }

                prevX = x;
                prevY = y;
                prevZ = z;
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
    }

    private void renderCatastrophicCore(PoseStack poseStack, float tick, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, DISTORTION_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        // Violently pulsating core with multiple layers
        for (int layer = 0; layer < 4; layer++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(tick * (10.0f - layer * 2.0f)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tick * (5.0f + layer)));

            Matrix4f pose = poseStack.last().pose();

            float pulse = 1.0f + Mth.sin(tick * 0.4f + layer) * 0.5f;
            float coreSize = (3.0f + layer * 0.5f) * pulse;
            int segments = 24;

            for (int i = 0; i < segments; i++) {
                float angle1 = (i / (float) segments) * Mth.TWO_PI;
                float angle2 = ((i + 1) / (float) segments) * Mth.TWO_PI;

                float x1 = Mth.cos(angle1) * coreSize;
                float z1 = Mth.sin(angle1) * coreSize;
                float x2 = Mth.cos(angle2) * coreSize;
                float z2 = Mth.sin(angle2) * coreSize;

                // White hot core blending to magenta/cyan
                int r, g, b;
                if (layer == 0) {
                    r = COLOR_WHITE[0];
                    g = COLOR_WHITE[1];
                    b = COLOR_WHITE[2];
                } else {
                    boolean isMagenta = (layer % 2) == 0;
                    int[] color = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;
                    r = color[0];
                    g = color[1];
                    b = color[2];
                }

                int a = (int)(intensity * 250);

                // Top pyramid
                buffer.addVertex(pose, 0, coreSize * 1.5f, 0).setColor(r, g, b, a).setUv(0.5f, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x1, 0, z1).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2, 0, z2).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, 0, coreSize * 1.5f, 0).setColor(r, g, b, a).setUv(0.5f, 0).setLight(LightTexture.FULL_BRIGHT);

                // Bottom pyramid
                buffer.addVertex(pose, 0, -coreSize * 1.5f, 0).setColor(r, g, b, a).setUv(0.5f, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2, 0, z2).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x1, 0, z1).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, 0, -coreSize * 1.5f, 0).setColor(r, g, b, a).setUv(0.5f, 0).setLight(LightTexture.FULL_BRIGHT);
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    // Cached particle classes for maximum performance
    private static class MassiveRealityCrack {
        private final Vector3f[] path;
        private final float speed;
        private final float width;
        private final boolean isMagenta;

        public MassiveRealityCrack(int seed) {
            this.speed = 0.8f + (seed % 4) * 0.3f;
            this.width = 1.2f + (seed % 3) * 0.4f;
            this.isMagenta = (seed % 2) == 0;

            // Pre-calculate massive crack path
            int pathLength = 60;
            path = new Vector3f[pathLength];

            float baseAngle = seed * 37.0f;
            float heightStart = (seed % 5) * 8.0f - 20.0f;

            for (int i = 0; i < pathLength; i++) {
                float t = i / (float) pathLength;

                float angle = baseAngle + t * 720.0f + Mth.sin(t * Mth.PI * 4) * 60.0f;
                float radius = EFFECT_RADIUS * (0.2f + t * 0.8f);
                float height = heightStart + t * 40.0f + Mth.sin(t * Mth.PI * 3) * 10.0f;

                path[i] = new Vector3f(
                        Mth.cos(angle * Mth.DEG_TO_RAD) * radius,
                        height,
                        Mth.sin(angle * Mth.DEG_TO_RAD) * radius
                );
            }
        }

        public void render(BufferBuilder buffer, PoseStack poseStack, float tick, float intensity) {
            Matrix4f pose = poseStack.last().pose();
            float animOffset = (tick * speed * 0.05f) % 1.0f;

            for (int i = 0; i < path.length - 1; i++) {
                Vector3f p1 = path[i];
                Vector3f p2 = path[i + 1];

                float t = (i / (float) path.length + animOffset) % 1.0f;
                float currentWidth = width * (1.0f + Mth.sin(tick * 0.3f + i * 0.5f) * 0.4f);

                int[] color = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;
                float brightness = 1.0f + Mth.sin(tick * 0.4f + i * 0.3f) * 0.5f;

                int r = (int)Math.min(255, color[0] * brightness);
                int g = (int)Math.min(255, color[1] * brightness);
                int b = (int)Math.min(255, color[2] * brightness);
                int a = (int)(intensity * 230);

                // Draw thick crack ribbon
                buffer.addVertex(pose, p1.x - currentWidth, p1.y, p1.z).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, p1.x + currentWidth, p1.y, p1.z).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, p2.x + currentWidth, p2.y, p2.z).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, p2.x - currentWidth, p2.y, p2.z).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
            }
        }
    }

    private static class VortexLayer {
        private final int layerIndex;
        private final float radiusMultiplier;
        private final float rotationSpeed;
        private final boolean reverse;

        public VortexLayer(int index) {
            this.layerIndex = index;
            this.radiusMultiplier = 0.4f + index * 0.1f;
            this.rotationSpeed = 6.0f + index * 1.5f;
            this.reverse = (index % 2) == 1;
        }

        public void render(BufferBuilder buffer, PoseStack poseStack, float tick, float intensity) {
            poseStack.pushPose();

            float rotation = tick * rotationSpeed * (reverse ? -1 : 1) + layerIndex * 60;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(layerIndex * 15));

            Matrix4f pose = poseStack.last().pose();

            int segments = 36;
            int heightSegments = 20; // Reduced from 30
            float maxHeight = EFFECT_RADIUS * 0.5f; // Reduced from 0.7f

            for (int h = 0; h < heightSegments - 1; h++) {
                float t1 = h / (float) heightSegments;
                float t2 = (h + 1) / (float) heightSegments;

                float y1 = (t1 - 0.5f) * maxHeight;
                float y2 = (t2 - 0.5f) * maxHeight;

                // PERFECT CYLINDER - no variation with height!
                float baseRadius = EFFECT_RADIUS * radiusMultiplier;
                float radius1 = baseRadius;
                float radius2 = baseRadius; // Same radius!

                float twist1 = t1 * 720.0f;
                float twist2 = t2 * 720.0f;

                for (int s = 0; s < segments; s++) {
                    float angle1 = (s / (float) segments) * 360.0f + twist1;
                    float angle2 = ((s + 1) / (float) segments) * 360.0f + twist1;
                    float angle3 = (s / (float) segments) * 360.0f + twist2;
                    float angle4 = ((s + 1) / (float) segments) * 360.0f + twist2;

                    float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float x3 = Mth.cos(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float z3 = Mth.sin(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float x4 = Mth.cos(angle4 * Mth.DEG_TO_RAD) * radius2;
                    float z4 = Mth.sin(angle4 * Mth.DEG_TO_RAD) * radius2;

                    boolean isMagenta = ((layerIndex + s / 3) % 2) == 0;
                    int[] color = isMagenta ? COLOR_MAGENTA_DARK : COLOR_CYAN_DARK;

                    int r = color[0];
                    int g = color[1];
                    int b = color[2];
                    int a = (int)(intensity * 180);

                    // Outside
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                    // Inside (reversed)
                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }
    }

    private static class ChaoticEnergyBolt {
        private final float orbitRadius;
        private final float orbitSpeed;
        private final float heightSpeed;
        private final float angleOffset;
        private final float size;
        private final boolean isMagenta;
        private final float cracklePhase;

        public ChaoticEnergyBolt(int seed) {
            this.orbitRadius = EFFECT_RADIUS * (0.2f + (seed % 15) * 0.05f);
            this.orbitSpeed = 2.0f + (seed % 7) * 0.4f;
            this.heightSpeed = 1.5f + (seed % 5) * 0.3f;
            this.angleOffset = seed * 1.8f;
            this.size = 0.3f + (seed % 4) * 0.1f;
            this.isMagenta = (seed % 5) < 3;
            this.cracklePhase = seed * 0.7f;
        }

        public void render(BufferBuilder buffer, Matrix4f pose, float tick, float intensity) {
            float angle = (tick * orbitSpeed * 0.15f + angleOffset) * Mth.DEG_TO_RAD;
            float x = Mth.cos(angle) * orbitRadius;
            float z = Mth.sin(angle) * orbitRadius;
            float y = Mth.sin(tick * heightSpeed * 0.1f + angleOffset) * EFFECT_RADIUS * 0.8f;

            // Crackle effect
            float crackle = Mth.sin(tick * 0.6f + cracklePhase);
            if (crackle < 0) return; // Flicker on/off

            float currentSize = size * (1.0f + crackle * 0.8f);

            int[] color = isMagenta ? COLOR_MAGENTA_BRIGHT : COLOR_CYAN_BRIGHT;
            int r = color[0];
            int g = color[1];
            int b = color[2];
            int a = (int)(intensity * 255 * crackle);

            buffer.addVertex(pose, x - currentSize, y - currentSize, z).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x + currentSize, y - currentSize, z).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x + currentSize, y + currentSize, z).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x - currentSize, y + currentSize, z).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
        }
    }
}