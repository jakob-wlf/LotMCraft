package de.jakob.lotm.entity.client.ability_entities.tyrant_pathway.strong_lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.StrongLightningEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class StrongLightningRenderer extends EntityRenderer<StrongLightningEntity> {

    public StrongLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StrongLightningEntity entity, float entityYaw, float partialTick,
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

        renderCylindricalBolt(consumer, poseStack, points, 0.8f,  0.15f, color);
        renderCylindricalBolt(consumer, poseStack, points, 0.45f, 0.35f, color);
        renderCylindricalBolt(consumer, poseStack, points, 0.22f, 0.75f, color);
        renderCylindricalBolt(consumer, poseStack, points, 0.10f, 1.0f,  0xFFFFFF);

        poseStack.popPose();
    }

    private void renderCylindricalBolt(VertexConsumer consumer, PoseStack poseStack,
                                       List<Vec3> points, float baseWidth, float alpha,
                                       int color) {
        Matrix4f matrix = poseStack.last().pose();

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Vec3 cameraPos = entityRenderDispatcher.camera.getPosition();

        double time = System.currentTimeMillis() * 0.008;
        float pulse = 0.9f + 0.1f * (float)Math.sin(time * 2.0);

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            Vec3 segmentDir = p2.subtract(p1).normalize();

            float t1 = (float)i / (points.size() - 1);
            float t2 = (float)(i + 1) / (points.size() - 1);
            float taper1 = 0.7f + 0.3f * (float)Math.sin(t1 * Math.PI);
            float taper2 = 0.7f + 0.3f * (float)Math.sin(t2 * Math.PI);

            float width1 = baseWidth * pulse * taper1;
            float width2 = baseWidth * pulse * taper2;

            float segmentFlicker = 0.95f + 0.05f * (float)Math.sin(time * 3.0 + i * 0.8);
            width1 *= segmentFlicker;
            width2 *= segmentFlicker;

            int a1 = (int)(255 * alpha * taper1);
            int a2 = (int)(255 * alpha * taper2);

            int numSides = 4;

            for (int side = 0; side < numSides; side++) {
                float angle1 = (float)(side * 2.0 * Math.PI / numSides);
                float angle2 = (float)((side + 1) * 2.0 * Math.PI / numSides);

                Vec3 toCamera = cameraPos.subtract(p1).normalize();
                Vec3 perpBase = segmentDir.cross(toCamera).normalize();

                Vec3 perp1_1 = rotateAroundAxis(perpBase, segmentDir, angle1).scale(width1);
                Vec3 perp1_2 = rotateAroundAxis(perpBase, segmentDir, angle2).scale(width1);
                Vec3 perp2_1 = rotateAroundAxis(perpBase, segmentDir, angle1).scale(width2);
                Vec3 perp2_2 = rotateAroundAxis(perpBase, segmentDir, angle2).scale(width2);

                addVertex(consumer, matrix, p1.add(perp1_1), r, g, b, a1);
                addVertex(consumer, matrix, p1.add(perp1_2), r, g, b, a1);
                addVertex(consumer, matrix, p2.add(perp2_2), r, g, b, a2);
                addVertex(consumer, matrix, p2.add(perp2_1), r, g, b, a2);
            }
        }
    }

    private Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, float angle) {
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
    public ResourceLocation getTextureLocation(StrongLightningEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(StrongLightningEntity livingEntity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }
}