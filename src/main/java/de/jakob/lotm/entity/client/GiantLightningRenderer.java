package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.custom.GiantLightningEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class GiantLightningRenderer extends EntityRenderer<GiantLightningEntity> {

    public GiantLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GiantLightningEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        List<Vec3> points = entity.getLightningPoints();

        if (points.size() < 2) {
            return;
        }

        poseStack.pushPose();

        Vec3 entityPos = entity.position();
        poseStack.translate(-entityPos.x, -entityPos.y, -entityPos.z);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        int color = entity.getColor();

        // Render the main lightning bolt with multiple passes for depth
        // Each pass uses cylindrical geometry that faces the camera

        // Pass 1: Outer glow (widest, most transparent)
        renderCylindricalBolt(consumer, poseStack, points, 1.8f, 0.2f, color, 1.3f);

        // Pass 2: Mid glow
        renderCylindricalBolt(consumer, poseStack, points, 1.0f, 0.45f, color, 1.8f);

        // Pass 3: Core (brightest, thinnest)
        renderCylindricalBolt(consumer, poseStack, points, 0.5f, 0.85f, color, 2.2f);

        // Pass 4: Hot core (very thin, very bright)
        renderCylindricalBolt(consumer, poseStack, points, 0.2f, 1.0f, color, 3.0f);

        poseStack.popPose();
    }

    private void renderCylindricalBolt(VertexConsumer consumer, PoseStack poseStack,
                                       List<Vec3> points, float baseWidth, float alpha,
                                       int color, float glowMultiplier) {
        Matrix4f matrix = poseStack.last().pose();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Apply glow
        r = Math.min(255, (int)(r * glowMultiplier));
        g = Math.min(255, (int)(g * glowMultiplier));
        b = Math.min(255, (int)(b * glowMultiplier));

        Vec3 cameraPos = entityRenderDispatcher.camera.getPosition();

        // Animated pulsing
        double time = System.currentTimeMillis() * 0.008;
        float pulse = 0.9f + 0.1f * (float)Math.sin(time * 2.0);

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            // Direction of the segment
            Vec3 segmentDir = p2.subtract(p1).normalize();

            // Calculate width based on position (taper slightly at ends)
            float t1 = (float)i / (points.size() - 1);
            float t2 = (float)(i + 1) / (points.size() - 1);
            float taper1 = 0.7f + 0.3f * (float)Math.sin(t1 * Math.PI);
            float taper2 = 0.7f + 0.3f * (float)Math.sin(t2 * Math.PI);

            float width1 = baseWidth * pulse * taper1;
            float width2 = baseWidth * pulse * taper2;

            // Per-segment flicker
            float segmentFlicker = 0.95f + 0.05f * (float)Math.sin(time * 3.0 + i * 0.8);
            width1 *= segmentFlicker;
            width2 *= segmentFlicker;

            // Alpha variation for depth
            int a1 = (int)(255 * alpha * taper1);
            int a2 = (int)(255 * alpha * taper2);

            // Create quad facing camera using 4 vertices around each point
            // Draw multiple quads rotated around the segment axis for cylindrical appearance
            int numSides = 4; // 4 sides creates a good cylindrical look without too many vertices

            for (int side = 0; side < numSides; side++) {
                float angle1 = (float)(side * 2.0 * Math.PI / numSides);
                float angle2 = (float)((side + 1) * 2.0 * Math.PI / numSides);

                // Get perpendicular vectors to segment for this rotation
                Vec3 toCamera = cameraPos.subtract(p1).normalize();
                Vec3 perpBase = segmentDir.cross(toCamera).normalize();

                // Rotate perpendicular vector around segment
                Vec3 perp1_1 = rotateAroundAxis(perpBase, segmentDir, angle1).scale(width1);
                Vec3 perp1_2 = rotateAroundAxis(perpBase, segmentDir, angle2).scale(width1);
                Vec3 perp2_1 = rotateAroundAxis(perpBase, segmentDir, angle1).scale(width2);
                Vec3 perp2_2 = rotateAroundAxis(perpBase, segmentDir, angle2).scale(width2);

                // Draw quad for this segment of the cylinder
                addVertex(consumer, matrix, p1.add(perp1_1), r, g, b, a1);
                addVertex(consumer, matrix, p1.add(perp1_2), r, g, b, a1);
                addVertex(consumer, matrix, p2.add(perp2_2), r, g, b, a2);
                addVertex(consumer, matrix, p2.add(perp2_1), r, g, b, a2);
            }
        }
    }

    // Rotate a vector around an axis by an angle
    private Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, float angle) {
        // Rodrigues' rotation formula
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vec3 axisPart = axis.scale(axis.dot(vector) * (1 - cos));
        Vec3 cosPart = vector.scale(cos);
        Vec3 sinPart = axis.cross(vector).scale(sin);

        return axisPart.add(cosPart).add(sinPart);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, int r, int g, int b, int a) {
        consumer.addVertex(matrix, (float)pos.x, (float)pos.y, (float)pos.z)
                .setColor(r, g, b, a);
    }

    @Override
    public ResourceLocation getTextureLocation(GiantLightningEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(GiantLightningEntity livingEntity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }
}