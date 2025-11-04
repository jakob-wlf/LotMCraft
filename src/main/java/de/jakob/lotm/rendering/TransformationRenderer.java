package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class TransformationRenderer {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        TransformationComponent component = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        
        if (!component.isTransformed()) {
            return;
        }

        if(component.shouldCancelDefaultRendering()) {
            event.setCanceled(true);
        }

        switch (component.getTransformationIndex()) {
            case 1 -> renderDesireApostleMass(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
            case 3 -> renderSolarEnvoy(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
            case 4 -> renderLightWings(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
        }
    }

    private static void renderLightWings(PoseStack poseStack, MultiBufferSource buffer,
                                         int packedLight, LivingEntity entity, float partialTick) {
        poseStack.pushPose();

        // Position wings at the entity's back (upper torso)
        float backHeight = entity.getBbHeight() * 0.65f;
        poseStack.translate(0, backHeight, 0);

        // Use additive blending for glowing effect
        RenderType renderType = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/sun/gold.png"));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // Animation parameters
        float time = entity.tickCount + partialTick;
        float flapAngle = Mth.sin(time * 0.1f) * 0.3f; // Gentle wing flapping angle
        float shimmer = 0.7f + Mth.sin(time * 0.15f) * 0.3f;

        // Render left wing
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(30 + flapAngle * 20)); // Spread angle + flap
        poseStack.mulPose(Axis.ZP.rotationDegrees(-10)); // Slight upward tilt
        renderWingMesh(poseStack, vertexConsumer, packedLight, shimmer, true);
        poseStack.popPose();

        // Render right wing
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-30 - flapAngle * 20));
        poseStack.mulPose(Axis.ZP.rotationDegrees(10));
        renderWingMesh(poseStack, vertexConsumer, packedLight, shimmer, false);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderWingMesh(PoseStack poseStack, VertexConsumer consumer,
                                       int light, float shimmer, boolean isLeft) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        int segments = 8;
        float wingLength = 1.8f;
        float wingWidth = 1.2f;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            // Create wing shape - wider at base, narrowing to tip
            float width1 = wingWidth * (1.0f - t1 * 0.7f);
            float width2 = wingWidth * (1.0f - t2 * 0.7f);

            // Wing extends outward (X) and slightly back (Z)
            float x1 = wingLength * t1;
            float x2 = wingLength * t2;
            float z = -t1 * 0.5f; // Slight backward curve

            // Calculate alpha - brighter at base, fade to tip
            int alpha1 = (int) (255 * shimmer * (1.0f - t1 * 0.6f));
            int alpha2 = (int) (255 * shimmer * (1.0f - t2 * 0.6f));

            // Create quad for this segment
            // Bottom edge
            float y1Bottom = -width1 * 0.3f;
            float y2Bottom = -width2 * 0.3f;
            // Top edge
            float y1Top = width1;
            float y2Top = width2;

            // Add slight wave pattern
            float wave1 = Mth.sin(t1 * Mth.PI * 2) * 0.05f;
            float wave2 = Mth.sin(t2 * Mth.PI * 2) * 0.05f;

            // Render the quad (correct vertex order for proper face culling)
            addWingVertex(consumer, matrix, normalMat, x1, y1Top + wave1, z, t1, 0, light, alpha1);
            addWingVertex(consumer, matrix, normalMat, x2, y2Top + wave2, z - (t2 - t1) * 0.5f, t2, 0, light, alpha2);
            addWingVertex(consumer, matrix, normalMat, x2, y2Bottom, z - (t2 - t1) * 0.5f, t2, 1, light, alpha2);
            addWingVertex(consumer, matrix, normalMat, x1, y1Bottom, z, t1, 1, light, alpha1);
        }

        // Add second layer for more volume
        for (int i = 0; i < segments - 2; i++) {
            float t1 = (float) i / segments + 0.1f;
            float t2 = (float) (i + 1) / segments + 0.1f;

            float width1 = wingWidth * 0.8f * (1.0f - t1 * 0.7f);
            float width2 = wingWidth * 0.8f * (1.0f - t2 * 0.7f);

            float x1 = wingLength * t1;
            float x2 = wingLength * t2;
            float z = -t1 * 0.5f - 0.05f;

            int alpha1 = (int) (200 * shimmer * (1.0f - t1 * 0.7f));
            int alpha2 = (int) (200 * shimmer * (1.0f - t2 * 0.7f));

            float y1Bottom = -width1 * 0.3f;
            float y2Bottom = -width2 * 0.3f;
            float y1Top = width1 * 0.9f;
            float y2Top = width2 * 0.9f;

            addWingVertex(consumer, matrix, normalMat, x1, y1Top, z, t1, 0, light, alpha1);
            addWingVertex(consumer, matrix, normalMat, x2, y2Top, z - (t2 - t1) * 0.5f, t2, 0, light, alpha2);
            addWingVertex(consumer, matrix, normalMat, x2, y2Bottom, z - (t2 - t1) * 0.5f, t2, 1, light, alpha2);
            addWingVertex(consumer, matrix, normalMat, x1, y1Bottom, z, t1, 1, light, alpha1);
        }
    }

    private static void addWingVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMat,
                                      float x, float y, float z, float u, float v, int light, int alpha) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 250, 200, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(240) // Full brightness for glow
                .setNormal(0, 0, 1);
    }

    private static void renderSolarEnvoy(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float partialTick) {
        poseStack.pushPose();

        float radius = 2.5f;
        float height = 2.5f;

        RenderType renderType = RenderType.entitySolid(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,"textures/entity/sun/gold.png"));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // Render sphere using latitude/longitude rings
        int segments = 24; // Horizontal segments (around)
        int rings = 18; // Vertical segments (up and down)

        for (int ring = 0; ring < rings; ring++) {
            float theta1 = ((float) ring / rings) * Mth.PI;
            float theta2 = ((float) (ring + 1) / rings) * Mth.PI;

            for (int seg = 0; seg < segments; seg++) {
                float phi1 = ((float) seg / segments) * Mth.TWO_PI;
                float phi2 = ((float) (seg + 1) / segments) * Mth.TWO_PI;

                // Calculate vertices for the quad
                Vec3 v1 = getSpherePoint(radius, height, theta1, phi1, 1);
                Vec3 v2 = getSpherePoint(radius, height, theta1, phi2, 1);
                Vec3 v3 = getSpherePoint(radius, height, theta2, phi2, 1);
                Vec3 v4 = getSpherePoint(radius, height, theta2, phi1, 1);

                Matrix4f matrix = poseStack.last().pose();
                Matrix3f normalMat = poseStack.last().normal();

                // Render the quad with slight transparency for depth
                int alpha = 200; // Slightly transparent
                addVertex(vertexConsumer, matrix, normalMat, v1, 0, 0, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v2, 1, 0, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v3, 1, 1, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v4, 0, 1, packedLight, alpha);
            }
        }

        poseStack.popPose();
    }

    private static void renderDesireApostleMass(PoseStack poseStack, MultiBufferSource buffer,
                                                int packedLight, LivingEntity entity, float partialTick) {
        poseStack.pushPose();

        // Calculate the actual ground position
        // The entity's Y position minus its height gets us to the ground
        double groundOffset = -(entity.getBbHeight() / 15) + 0.01; // Slightly above ground
        poseStack.translate(0, groundOffset, 0);

        float radius = 3.5f;
        float height = .75f; // Make it slightly flattened

        // Optional: Add pulsing animation
        float pulse = 1.0f + (Mth.sin(entity.tickCount * 0.1f) * 0.05f);

        // Use a solid translucent render type for a more ethereal look
        RenderType renderType = RenderType.entitySolid(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,"textures/entity/black_hole/black.png"));
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // Render sphere using latitude/longitude rings
        int segments = 16; // Horizontal segments (around)
        int rings = 12; // Vertical segments (up and down)

        for (int ring = 0; ring < rings; ring++) {
            float theta1 = ((float) ring / rings) * Mth.PI;
            float theta2 = ((float) (ring + 1) / rings) * Mth.PI;

            for (int seg = 0; seg < segments; seg++) {
                float phi1 = ((float) seg / segments) * Mth.TWO_PI;
                float phi2 = ((float) (seg + 1) / segments) * Mth.TWO_PI;

                // Calculate vertices for the quad
                Vec3 v1 = getSpherePoint(radius, height, theta1, phi1, pulse);
                Vec3 v2 = getSpherePoint(radius, height, theta1, phi2, pulse);
                Vec3 v3 = getSpherePoint(radius, height, theta2, phi2, pulse);
                Vec3 v4 = getSpherePoint(radius, height, theta2, phi1, pulse);

                // Calculate normal (pointing outward from center)
                Vec3 normal1 = getNormal(v1, v2, v3);

                Matrix4f matrix = poseStack.last().pose();
                Matrix3f normalMat = poseStack.last().normal();

                // Render the quad with slight transparency for depth
                int alpha = 200; // Slightly transparent
                addVertex(vertexConsumer, matrix, normalMat, v1, 0, 0, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v2, 1, 0, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v3, 1, 1, packedLight, alpha);
                addVertex(vertexConsumer, matrix, normalMat, v4, 0, 1, packedLight, alpha);
            }
        }

        poseStack.popPose();
    }

    private static Vec3 getSpherePoint(float radius, float height, float theta, float phi, float pulse) {
        float x = radius * Mth.sin(theta) * Mth.cos(phi) * pulse;
        float y = height * Mth.cos(theta) * pulse; // Use height for vertical axis
        float z = radius * Mth.sin(theta) * Mth.sin(phi) * pulse;
        return new Vec3(x, y, z);
    }

    private static Vec3 getNormal(Vec3 v1, Vec3 v2, Vec3 v3) {
        Vec3 edge1 = v2.subtract(v1);
        Vec3 edge2 = v3.subtract(v1);
        return edge1.cross(edge2).normalize();
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMat,
                                  Vec3 pos, float u, float v, int light, int alpha) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }
}