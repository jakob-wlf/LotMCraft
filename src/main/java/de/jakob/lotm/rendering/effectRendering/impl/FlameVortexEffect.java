package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class FlameVortexEffect extends ActiveEffect {
    private static final ResourceLocation FLAME_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/netherrack.png");

    public FlameVortexEffect(double x, double y, double z) {
        super(x, y, z, 20 * 6);
    }

    // Custom additive render type for fire effect
    private static final RenderType FIRE_ADDITIVE = RenderType.create(
            "fire_vortex_additive",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(FLAME_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true)
    );

    @Override
    protected void render(PoseStack poseStack, float tick) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        Tesselator tesselator = Tesselator.getInstance();

        float rotation = tick * 12.0f;
        float ageInTicks = tick;

        // Render main fire spiral
        renderFireSpiral(poseStack, tesselator, rotation, ageInTicks);

        // Render inner flames
        renderInnerFlames(poseStack, tesselator, rotation, ageInTicks);

        // Render fire ribbons
        renderFireRibbons(poseStack, tesselator, rotation, ageInTicks);

        // Render ember particles
        renderEmberSwirls(poseStack, tesselator, rotation, ageInTicks);

        // Render core heat glow
        renderCoreGlow(poseStack, tesselator, ageInTicks);

        poseStack.popPose();
    }

    private void renderFireSpiral(PoseStack poseStack, Tesselator tesselator, float rotation, float ageInTicks) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, FLAME_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int segments = 24;
        int heightSegments = 20;
        float maxRadius = 6.0f;
        float maxHeight = 6.0f;

        for (int layer = 0; layer < 3; layer++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 1.2f + layer * 120 + ageInTicks * 2.5f));

            for (int h = 0; h < heightSegments; h++) {
                float heightRatio = h / (float) heightSegments;
                float nextHeightRatio = (h + 1) / (float) heightSegments;

                float y1 = heightRatio * maxHeight;
                float y2 = nextHeightRatio * maxHeight;

                float radius1 = maxRadius * (heightRatio * 0.6f + 0.4f);
                float radius2 = maxRadius * (nextHeightRatio * 0.6f + 0.4f);


                // Spiral twist
                float twist1 = heightRatio * 720.0f + (ageInTicks * 5.0f);
                float twist2 = nextHeightRatio * 720.0f + (ageInTicks * 5.0f);

                for (int i = 0; i < segments; i++) {
                    float angle1 = (i / (float) segments) * 360.0f + twist1;
                    float angle2 = ((i + 1) / (float) segments) * 360.0f + twist1;
                    float angle3 = (i / (float) segments) * 360.0f + twist2;
                    float angle4 = ((i + 1) / (float) segments) * 360.0f + twist2;

                    float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float x3 = Mth.cos(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float z3 = Mth.sin(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float x4 = Mth.cos(angle4 * Mth.DEG_TO_RAD) * radius2;
                    float z4 = Mth.sin(angle4 * Mth.DEG_TO_RAD) * radius2;

                    // Orange at bottom, purple at top
                    int r, g, b;
                    if (heightRatio < 0.4f) {
                        // Orange-red at bottom
                        float t = heightRatio / 0.4f;
                        r = 255;
                        g = (int)(100 + t * 50);
                        b = (int)(20 * (1.0f - t));
                    } else {
                        // Purple at top
                        float t = (heightRatio - 0.4f) / 0.6f;
                        r = (int)(255 - t * 100);
                        g = (int)(150 - t * 100);
                        b = (int)(20 + t * 235);
                    }

                    float alpha = 0.5f + (layer % 2) * 0.15f;
                    int a = (int)(alpha * 255);

                    Matrix4f pose = poseStack.last().pose();

                    buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                    buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
                }
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderInnerFlames(PoseStack poseStack, Tesselator tesselator, float rotation, float ageInTicks) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, FLAME_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int numFlames = 12;
        float maxHeight = 6.0f;

        for (int flame = 0; flame < numFlames; flame++) {
            poseStack.pushPose();

            float flameAngle = (flame / (float) numFlames) * 360.0f + rotation * 3.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(flameAngle));

            int segments = 16;
            float prevX = 2.5f;
            float prevY = 0;
            float prevZ = 0;

            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                float radius = 2.5f * (1.0f - t * 0.7f);

                float x = radius + Mth.sin(ageInTicks * 0.4f + flame + t * Mth.PI * 3) * 0.5f;
                float y = t * maxHeight;
                float z = Mth.sin(t * Mth.PI * 4 + ageInTicks * 0.3f) * 0.4f;

                // Bright orange-yellow at bottom, dark purple at top
                int r, g, b;
                if (t < 0.3f) {
                    // Bright orange-yellow
                    r = 255;
                    g = (int)(180 + Mth.sin(ageInTicks * 0.5f + flame) * 50);
                    b = 30;
                } else if (t < 0.6f) {
                    // Orange to magenta
                    float blend = (t - 0.3f) / 0.3f;
                    r = 255;
                    g = (int)(180 * (1.0f - blend) + 80 * blend);
                    b = (int)(30 + blend * 170);
                } else {
                    // Deep purple
                    r = (int)(200 - (t - 0.6f) * 50);
                    g = 50;
                    b = (int)(200 + (t - 0.6f) * 55);
                }

                float alpha = 0.6f * (1.0f - t * 0.3f);
                int a = (int)(alpha * 255);

                Matrix4f pose = poseStack.last().pose();
                float thickness = 0.2f;

                buffer.addVertex(pose, prevX - thickness, prevY, prevZ).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x - thickness, y, z).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x + thickness, y, z).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, prevX + thickness, prevY, prevZ).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);

                prevX = x;
                prevY = y;
                prevZ = z;
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderFireRibbons(PoseStack poseStack, Tesselator tesselator, float rotation, float ageInTicks) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, FLAME_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int numRibbons = 6;
        float maxHeight = 6.0f;
        int heightSegments = 30;

        for (int ribbon = 0; ribbon < numRibbons; ribbon++) {
            poseStack.pushPose();

            float ribbonAngle = (ribbon / (float) numRibbons) * 360.0f + rotation * 1.5f;
            poseStack.mulPose(Axis.YP.rotationDegrees(ribbonAngle));

            Matrix4f pose = poseStack.last().pose();
            float ribbonWidth = 0.5f;

            for (int h = 0; h < heightSegments; h++) {
                float heightRatio = h / (float) heightSegments;
                float nextHeightRatio = (h + 1) / (float) heightSegments;

                float y1 = heightRatio * maxHeight;
                float y2 = nextHeightRatio * maxHeight;

                float baseRadius = 6.0f * (heightRatio * 0.6f + 0.4f);
                float nextBaseRadius = 6.0f * (nextHeightRatio * 0.6f + 0.4f);

                float offset = 0.6f + Mth.sin(ageInTicks * 0.15f + ribbon + heightRatio * Mth.PI * 3) * 0.4f;
                float radius1 = baseRadius + offset;
                float radius2 = nextBaseRadius + offset;

                float spiralTwist1 = heightRatio * 540.0f + (ageInTicks * 7.0f);
                float spiralTwist2 = nextHeightRatio * 540.0f + (ageInTicks * 7.0f);

                float angle1 = spiralTwist1 * Mth.DEG_TO_RAD;
                float angle2 = spiralTwist2 * Mth.DEG_TO_RAD;

                float x1_inner = Mth.cos(angle1) * radius1;
                float z1_inner = Mth.sin(angle1) * radius1;
                float x2_inner = Mth.cos(angle2) * radius2;
                float z2_inner = Mth.sin(angle2) * radius2;

                float x1_outer = Mth.cos(angle1) * (radius1 + ribbonWidth);
                float z1_outer = Mth.sin(angle1) * (radius1 + ribbonWidth);
                float x2_outer = Mth.cos(angle2) * (radius2 + ribbonWidth);
                float z2_outer = Mth.sin(angle2) * (radius2 + ribbonWidth);

                // Alternating orange and purple ribbons
                int r, g, b;
                if (ribbon % 2 == 0) {
                    // Bright orange with yellow tint
                    r = 255;
                    g = (int)(140 + Mth.sin(ageInTicks * 0.2f + heightRatio * 2) * 40);
                    b = 20;
                } else {
                    // Vibrant purple
                    r = (int)(200 + Mth.sin(ageInTicks * 0.2f + heightRatio * 2) * 30);
                    g = 60;
                    b = 255;
                }

                float alpha = 0.7f * (1.0f - heightRatio * 0.4f);
                int a = (int)(alpha * 255);

                buffer.addVertex(pose, x1_inner, y1, z1_inner).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x1_outer, y1, z1_outer).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2_outer, y2, z2_outer).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2_inner, y2, z2_inner).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
            }

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderEmberSwirls(PoseStack poseStack, Tesselator tesselator, float rotation, float ageInTicks) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, FLAME_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        int numEmbers = 30;
        float maxHeight = 6.0f;

        for (int ember = 0; ember < numEmbers; ember++) {
            float t = (ageInTicks * 0.05f + ember * 0.1f) % 1.0f;
            float y = t * maxHeight;
            float radius = 5.0f * (1.0f - t * 0.5f);
            float angle = (ember * 137.5f + ageInTicks * 8.0f + t * 360.0f) * Mth.DEG_TO_RAD;

            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            float size = 0.15f * (1.0f - t * 0.5f);

            // Glowing orange-yellow embers
            int r = 255;
            int g = (int)(200 - t * 100);
            int b = (int)(50 * (1.0f - t));
            int a = (int)((1.0f - t) * 200);

            Matrix4f pose = poseStack.last().pose();

            buffer.addVertex(pose, x - size, y, z - size).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x + size, y, z - size).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x + size, y, z + size).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, x - size, y, z + size).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderCoreGlow(PoseStack poseStack, Tesselator tesselator, float ageInTicks) {
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, FLAME_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        float maxHeight = 6.0f;
        int segments = 16;
        int heightSegments = 18;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 8.0f));

        for (int h = 0; h < heightSegments; h++) {
            float heightRatio = h / (float) heightSegments;
            float nextHeightRatio = (h + 1) / (float) heightSegments;

            float y1 = heightRatio * maxHeight;
            float y2 = nextHeightRatio * maxHeight;

            float radius1 = 0.8f * (heightRatio * 0.6f + 0.4f);
            float radius2 = 0.8f * (nextHeightRatio * 0.6f + 0.4f);

            float pulse = 1.0f + Mth.sin(ageInTicks * 0.3f + heightRatio * Mth.PI) * 0.2f;
            radius1 *= pulse;
            radius2 *= pulse;

            for (int i = 0; i < segments; i++) {
                float angle1 = (i / (float) segments) * 360.0f;
                float angle2 = ((i + 1) / (float) segments) * 360.0f;

                float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;
                float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;
                float x3 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius2;
                float z3 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius2;
                float x4 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius2;
                float z4 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius2;

                // Bright white-yellow core
                int r = 255;
                int g = (int)(250 - heightRatio * 50);
                int b = (int)(200 - heightRatio * 180);
                int a = (int)(0.8f * 255);

                Matrix4f pose = poseStack.last().pose();

                buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x4, y2, z4).setColor(r, g, b, a).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT);
                buffer.addVertex(pose, x3, y2, z3).setColor(r, g, b, a).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT);
            }
        }

        poseStack.popPose();

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}