package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import de.jakob.lotm.entity.custom.ElectromagneticTornadoEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ElectromagneticTornadoRenderer extends EntityRenderer<ElectromagneticTornadoEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/glass.png");

    public ElectromagneticTornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Custom translucent render type (non-additive)
    private static final RenderType TORNADO_TRANSLUCENT = RenderType.create(
            "tornado_translucent",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(true)
    );


    @Override
    public void render(ElectromagneticTornadoEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float rotation = entity.getRotation() + partialTick * 15.0f;
        float ageInTicks = entity.tickCount + partialTick;

        // Render main tornado spiral
        renderTornadoSpiral(entity, poseStack, bufferSource, rotation, ageInTicks);

        // Render electromagnetic energy rings
        renderEnergyRings(entity, poseStack, bufferSource, rotation, ageInTicks);

        // Render electric arcs
        renderElectricArcs(entity, poseStack, bufferSource, ageInTicks);

        // Render core glow
        renderCoreGlow(entity, poseStack, bufferSource, ageInTicks);

        // Render outer swirls for motion effect
        renderOuterSwirls(entity, poseStack, bufferSource, rotation, ageInTicks);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderTornadoSpiral(ElectromagneticTornadoEntity entity, PoseStack poseStack,
                                     MultiBufferSource bufferSource, float rotation, float ageInTicks) {
        VertexConsumer consumer = bufferSource.getBuffer(TORNADO_TRANSLUCENT);

        int segments = 32;
        int heightSegments = 28;
        float maxRadius = 10.0f;
        float maxHeight = 20.0f;
        float minHeight = 0.5f; // Start slightly above ground

        for (int layer = 0; layer < 5; layer++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 1.5f + layer * 72 + ageInTicks * 3.0f));

            for (int h = 0; h < heightSegments; h++) {
                float heightRatio = h / (float) heightSegments;
                float nextHeightRatio = (h + 1) / (float) heightSegments;

                float y1 = minHeight + heightRatio * maxHeight;
                float y2 = minHeight + nextHeightRatio * maxHeight;

                // Tornado shape: narrower at bottom, wider at top (inverted)
                float radius1 = maxRadius * (0.25f + heightRatio * 0.75f);
                float radius2 = maxRadius * (0.25f + nextHeightRatio * 0.75f);

                // Spiral twist - faster spinning
                float twist1 = heightRatio * 1080.0f + (ageInTicks * 4.0f);
                float twist2 = nextHeightRatio * 1080.0f + (ageInTicks * 4.0f);

                for (int i = 0; i < segments; i++) {
                    float angle1 = (i / (float) segments) * 360.0f + twist1;
                    float angle2 = ((i + 1) / (float) segments) * 360.0f + twist1;

                    float angle3 = (i / (float) segments) * 360.0f + twist2;
                    float angle4 = ((i + 1) / (float) segments) * 360.0f + twist2;

                    // Calculate vertex positions
                    float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius1;
                    float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius1;

                    float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius1;
                    float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius1;

                    float x3 = Mth.cos(angle3 * Mth.DEG_TO_RAD) * radius2;
                    float z3 = Mth.sin(angle3 * Mth.DEG_TO_RAD) * radius2;

                    float x4 = Mth.cos(angle4 * Mth.DEG_TO_RAD) * radius2;
                    float z4 = Mth.sin(angle4 * Mth.DEG_TO_RAD) * radius2;

                    // Vibrant cyan-purple gradient - purple at bottom, cyan at top
                    int r, g, b;
                    if (heightRatio < 0.5f) {
                        // Purple at bottom
                        float t = heightRatio * 2.0f;
                        r = (int) (180 + t * 50);
                        g = (int) (50 + t * 150);
                        b = 255;
                    } else {
                        // Cyan at top
                        float t = (heightRatio - 0.5f) * 2.0f;
                        r = (int) (230 - t * 130);
                        g = (int) (200 + t * 55);
                        b = 255;
                    }

                    float alpha = 0.35f + (layer % 2) * 0.1f;

                    Matrix4f pose = poseStack.last().pose();
                    Matrix3f normal = poseStack.last().normal();

                    // Draw quad
                    addVertex(consumer, pose, normal, x1, y1, z1, 0, 0, r, g, b, alpha);
                    addVertex(consumer, pose, normal, x2, y1, z2, 1, 0, r, g, b, alpha);
                    addVertex(consumer, pose, normal, x4, y2, z4, 1, 1, r, g, b, alpha);
                    addVertex(consumer, pose, normal, x3, y2, z3, 0, 1, r, g, b, alpha);
                }
            }

            poseStack.popPose();
        }
    }

    private void renderOuterSwirls(ElectromagneticTornadoEntity entity, PoseStack poseStack,
                                   MultiBufferSource bufferSource, float rotation, float ageInTicks) {
        VertexConsumer consumer = bufferSource.getBuffer(TORNADO_TRANSLUCENT);

        int numSwirls = 8; // Number of spiral ribbons
        float maxHeight = 20.0f;
        float minHeight = 0.5f;
        int heightSegments = 40;

        for (int swirl = 0; swirl < numSwirls; swirl++) {
            poseStack.pushPose();

            // Offset each swirl around the tornado
            float swirlAngle = (swirl / (float) numSwirls) * 360.0f + rotation * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(swirlAngle));

            // Alternate colors for swirls
            int r, g, b;
            if (swirl % 2 == 0) {
                // Bright cyan
                r = 50;
                g = 255;
                b = 255;
            } else {
                // Bright purple
                r = 200;
                g = 50;
                b = 255;
            }

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            float ribbonWidth = 0.4f;

            // Draw spiral ribbon from bottom to top
            for (int h = 0; h < heightSegments; h++) {
                float heightRatio = h / (float) heightSegments;
                float nextHeightRatio = (h + 1) / (float) heightSegments;

                float y1 = minHeight + heightRatio * maxHeight;
                float y2 = minHeight + nextHeightRatio * maxHeight;

                // Radius increases with height (following tornado shape)
                float baseRadius = 10.0f * (0.25f + heightRatio * 0.75f);
                float nextBaseRadius = 10.0f * (0.25f + nextHeightRatio * 0.75f);

                // Add some outward offset and wave motion
                float offset = 0.5f + Mth.sin(ageInTicks * 0.1f + swirl + heightRatio * Mth.PI * 2) * 0.3f;
                float radius1 = baseRadius + offset;
                float radius2 = nextBaseRadius + offset;

                // Spiral twist - this creates the swirling effect
                float spiralTwist1 = heightRatio * 720.0f + (ageInTicks * 6.0f);
                float spiralTwist2 = nextHeightRatio * 720.0f + (ageInTicks * 6.0f);

                // Calculate ribbon positions
                float angle1 = spiralTwist1 * Mth.DEG_TO_RAD;
                float angle2 = spiralTwist2 * Mth.DEG_TO_RAD;

                // Inner edge of ribbon
                float x1_inner = Mth.cos(angle1) * radius1;
                float z1_inner = Mth.sin(angle1) * radius1;
                float x2_inner = Mth.cos(angle2) * radius2;
                float z2_inner = Mth.sin(angle2) * radius2;

                // Outer edge of ribbon (slightly further out)
                float x1_outer = Mth.cos(angle1) * (radius1 + ribbonWidth);
                float z1_outer = Mth.sin(angle1) * (radius1 + ribbonWidth);
                float x2_outer = Mth.cos(angle2) * (radius2 + ribbonWidth);
                float z2_outer = Mth.sin(angle2) * (radius2 + ribbonWidth);

                // Fade alpha based on height for smooth appearance
                float alpha = 0.6f * (1.0f - heightRatio * 0.3f);

                // Draw ribbon quad (outer face)
                addVertex(consumer, pose, normal, x1_inner, y1, z1_inner, 0, 0, r, g, b, alpha);
                addVertex(consumer, pose, normal, x1_outer, y1, z1_outer, 1, 0, r, g, b, alpha);
                addVertex(consumer, pose, normal, x2_outer, y2, z2_outer, 1, 1, r, g, b, alpha);
                addVertex(consumer, pose, normal, x2_inner, y2, z2_inner, 0, 1, r, g, b, alpha);

                // Draw ribbon quad (inner face for visibility from all angles)
                addVertex(consumer, pose, normal, x1_outer, y1, z1_outer, 0, 0, r, g, b, alpha * 0.7f);
                addVertex(consumer, pose, normal, x1_inner, y1, z1_inner, 1, 0, r, g, b, alpha * 0.7f);
                addVertex(consumer, pose, normal, x2_inner, y2, z2_inner, 1, 1, r, g, b, alpha * 0.7f);
                addVertex(consumer, pose, normal, x2_outer, y2, z2_outer, 0, 1, r, g, b, alpha * 0.7f);
            }

            poseStack.popPose();
        }
    }

    private void renderEnergyRings(ElectromagneticTornadoEntity entity, PoseStack poseStack,
                                   MultiBufferSource bufferSource, float rotation, float ageInTicks) {
        VertexConsumer consumer = bufferSource.getBuffer(TORNADO_TRANSLUCENT);

        int numRings = 18;
        float maxHeight = 20.0f;
        float minHeight = 0.5f;

        for (int ring = 0; ring < numRings; ring++) {
            float heightRatio = ring / (float) numRings;
            float y = minHeight + heightRatio * maxHeight;
            float radius = 5.0f * (0.25f + heightRatio * 0.75f);

            // Make rings spin continuously
            float ringRotation = rotation * 3.0f + ring * 30.0f + ageInTicks * 5.0f;

            poseStack.pushPose();
            poseStack.translate(0, y, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(ringRotation));

            int segments = 64;
            float thickness = 0.15f;

            // Alternating bright cyan and purple rings
            int r, g, b;
            if (ring % 2 == 0) {
                // Bright cyan
                r = 50 + (int)(100 * Mth.sin(ageInTicks * 0.1f + ring));
                g = 200 + (int)(55 * Mth.cos(ageInTicks * 0.15f));
                b = 255;
            } else {
                // Bright purple
                r = 200 + (int)(55 * Mth.sin(ageInTicks * 0.1f + ring));
                g = 50 + (int)(100 * Mth.cos(ageInTicks * 0.15f));
                b = 255;
            }

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            // Draw complete ring as connected segments
            for (int i = 0; i < segments; i++) {
                float angle1 = (i / (float) segments) * 360.0f;
                float angle2 = ((i + 1) / (float) segments) * 360.0f;

                float x1 = Mth.cos(angle1 * Mth.DEG_TO_RAD) * radius;
                float z1 = Mth.sin(angle1 * Mth.DEG_TO_RAD) * radius;

                float x2 = Mth.cos(angle2 * Mth.DEG_TO_RAD) * radius;
                float z2 = Mth.sin(angle2 * Mth.DEG_TO_RAD) * radius;

                // Draw vertical quad facing outward
                addVertex(consumer, pose, normal, x1, -thickness, z1, 0, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, -thickness, z2, 1, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, thickness, z2, 1, 1, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, thickness, z1, 0, 1, r, g, b, 0.775f);

                // Draw opposite facing quad (facing inward)
                addVertex(consumer, pose, normal, x2, -thickness, z2, 0, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, -thickness, z1, 1, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, thickness, z1, 1, 1, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, thickness, z2, 0, 1, r, g, b, 0.775f);

                // Draw top cap
                addVertex(consumer, pose, normal, x1, thickness, z1, 0, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, thickness, z2, 1, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, thickness, z2, 1, 1, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, thickness, z1, 0, 1, r, g, b, 0.775f);

                // Draw bottom cap
                addVertex(consumer, pose, normal, x2, -thickness, z2, 0, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, -thickness, z1, 1, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x1, -thickness, z1, 1, 1, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x2, -thickness, z2, 0, 1, r, g, b, 0.775f);
            }

            poseStack.popPose();
        }
    }

    private void renderElectricArcs(ElectromagneticTornadoEntity entity, PoseStack poseStack,
                                    MultiBufferSource bufferSource, float ageInTicks) {
        VertexConsumer consumer = bufferSource.getBuffer(TORNADO_TRANSLUCENT);

        int numArcs = 20;
        float maxHeight = 20.0f;
        float minHeight = 0.5f;

        for (int arc = 0; arc < numArcs; arc++) {
            // Make arcs spin continuously
            float baseAngle = (arc / (float) numArcs) * 360.0f + ageInTicks * 8.0f;
            float radius = 4.0f;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(baseAngle));

            int segments = 18;
            float prevX = radius * 0.25f;
            float prevY = minHeight;
            float prevZ = 0;

            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                float heightRadius = radius * (0.25f + t * 0.75f);
                float x = heightRadius * Mth.cos(t * Mth.PI * 5);
                float y = minHeight + t * maxHeight;
                float z = heightRadius * Mth.sin(t * Mth.PI * 5) * 0.4f;

                // Add randomness for electric effect
                x += Mth.sin(ageInTicks * 0.5f + arc + i) * 0.4f;
                z += Mth.cos(ageInTicks * 0.5f + arc + i) * 0.4f;

                // Alternate between bright cyan and purple
                int r, g, b;
                if (arc % 3 == 0) {
                    // Bright cyan
                    r = 50;
                    g = 255;
                    b = 255;
                } else if (arc % 3 == 1) {
                    // Bright purple
                    r = 200;
                    g = 50;
                    b = 255;
                } else {
                    // Bright blue
                    r = 100;
                    g = 150;
                    b = 255;
                }

                Matrix4f pose = poseStack.last().pose();
                Matrix3f normal = poseStack.last().normal();

                float thickness = 0.12f;

                // Draw arc segment
                addVertex(consumer, pose, normal, prevX - thickness, prevY, prevZ, 0, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x - thickness, y, z, 1, 0, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, x + thickness, y, z, 1, 1, r, g, b, 0.775f);
                addVertex(consumer, pose, normal, prevX + thickness, prevY, prevZ, 0, 1, r, g, b, 0.775f);

                prevX = x;
                prevY = y;
                prevZ = z;
            }

            poseStack.popPose();
        }
    }

    private void renderCoreGlow(ElectromagneticTornadoEntity entity, PoseStack poseStack,
                                MultiBufferSource bufferSource, float ageInTicks) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));

        float maxHeight = 20.0f;
        float minHeight = 0.5f;
        int segments = 32;
        int heightSegments = 24;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 6.0f));

        for (int h = 0; h < heightSegments; h++) {
            float heightRatio = h / (float) heightSegments;
            float nextHeightRatio = (h + 1) / (float) heightSegments;

            float y1 = minHeight + heightRatio * maxHeight;
            float y2 = minHeight + nextHeightRatio * maxHeight;

            float radius1 = 0.5f * (0.25f + heightRatio * 0.75f);
            float radius2 = 0.5f * (0.25f + nextHeightRatio * 0.75f);

            // Pulsating effect
            float pulse = 1.0f + Mth.sin(ageInTicks * 0.2f + heightRatio * Mth.PI) * 0.3f;
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

                // Bright cyan-white core with gradient
                int r, g, b;
                if (heightRatio < 0.3f) {
                    // Purple-ish at bottom
                    r = 200;
                    g = 150;
                    b = 255;
                } else if (heightRatio < 0.7f) {
                    // Cyan in middle
                    r = 150;
                    g = 255;
                    b = 255;
                } else {
                    // Lighter cyan at top
                    r = 200;
                    g = 255;
                    b = 255;
                }

                Matrix4f pose = poseStack.last().pose();
                Matrix3f normal = poseStack.last().normal();

                addVertex(consumer, pose, normal, x1, y1, z1, 0, 0, r, g, b, 0.75f);
                addVertex(consumer, pose, normal, x2, y1, z2, 1, 0, r, g, b, 0.75f);
                addVertex(consumer, pose, normal, x4, y2, z4, 1, 1, r, g, b, 0.75f);
                addVertex(consumer, pose, normal, x3, y2, z3, 0, 1, r, g, b, 0.75f);
            }
        }

        poseStack.popPose();
    }

    private void addVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                           float x, float y, float z, float u, float v,
                           int r, int g, int b, float alpha) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, (int)(alpha * 255))
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(ElectromagneticTornadoEntity entity) {
        return TEXTURE;
    }
}