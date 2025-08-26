package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.entity.custom.ElectricShockEntity;
import de.jakob.lotm.util.data.ModRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ElectricShockRenderer extends EntityRenderer<ElectricShockEntity> {

    public ElectricShockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ElectricShockEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        List<Vec3> points = entity.getLightningPoints();

        if (points.size() < 2) {
            return;
        }

        poseStack.pushPose();

        // Move to entity position for proper world-space rendering
        Vec3 entityPos = entity.position();
        poseStack.translate(-entityPos.x, -entityPos.y, -entityPos.z);

        // Use lightning render type for emissive electric effect
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        // Render main lightning bolt
        renderLightningBolt(consumer, poseStack, points, 0.07f, 1.0f, 0x00FFFF); // Cyan main bolt

        poseStack.popPose();
    }

    private void renderLightningBolt(VertexConsumer consumer, PoseStack poseStack,
                                     List<Vec3> points, float width, float alpha, int color) {
        Matrix4f matrix = poseStack.last().pose();

        // Extract RGB from hex color
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int)(255 * alpha);

        Vec3 cameraPos = entityRenderDispatcher.camera.getPosition();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            // Calculate direction and create billboard quad
            Vec3 direction = p2.subtract(p1);
            Vec3 toCameraP1 = cameraPos.subtract(p1).normalize();
            Vec3 toCameraP2 = cameraPos.subtract(p2).normalize();

            // Create perpendicular vectors for billboard effect
            Vec3 perp1 = direction.normalize().cross(toCameraP1).normalize().scale(width);
            Vec3 perp2 = direction.normalize().cross(toCameraP2).normalize().scale(width);

            // Add some random flicker to the width
            float flicker = 0.8f + 0.4f * (float)Math.sin(System.currentTimeMillis() * 0.01 + i);
            perp1 = perp1.scale(flicker);
            perp2 = perp2.scale(flicker);

            // Draw quad between the two points
            addVertex(consumer, matrix, p1.subtract(perp1), r, g, b, a);
            addVertex(consumer, matrix, p1.add(perp1), r, g, b, a);
            addVertex(consumer, matrix, p2.add(perp2), r, g, b, a);
            addVertex(consumer, matrix, p2.subtract(perp2), r, g, b, a);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, int r, int g, int b, int a) {
        consumer.addVertex(matrix, (float)pos.x, (float)pos.y, (float)pos.z)
                .setColor(r, g, b, a);
    }

    private void spawnElectricParticles(ElectricShockEntity entity, List<Vec3> points, float partialTick) {
        // Only spawn particles occasionally to avoid lag
        if (entity.tickCount % 3 != 0) return;

        for (int i = 0; i < points.size() - 1; i += 2) {
            Vec3 point = points.get(i);

            // Add small electric sparks
            if (entity.getRandom().nextFloat() < 0.3f) {
                double offsetX = (entity.getRandom().nextDouble() - 0.5) * 0.2;
                double offsetY = (entity.getRandom().nextDouble() - 0.5) * 0.2;
                double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 0.2;

                entity.level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                        0, 0, 0
                );
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ElectricShockEntity entity) {
        // Lightning render type doesn't use textures
        return null;
    }

    @Override
    public boolean shouldRender(ElectricShockEntity livingEntity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        // Always render if in range - electric effects should be visible
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }
}