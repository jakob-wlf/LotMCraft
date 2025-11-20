package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.rendering.models.TyrantMythicalCreatureModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class TransformationRenderer {

    private static TyrantMythicalCreatureModel<Entity> tyrantMythicalCreatureModel;
    private static final ResourceLocation tyrantMythicalCreatureTexture = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/mythical_creatures/tyrant.png");

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
            case 4 -> renderAngelicWings(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
            case 6 -> renderEnergyMass(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
            case 101 -> renderTyrantMythicalCreature(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
        }
    }

    private static void renderTyrantMythicalCreature(PoseStack poseStack, MultiBufferSource bufferSource,
                                                     int packedLight, LivingEntity entity, float partialTick) {
        // Lazy initialization - only bake the model when first needed
        if (tyrantMythicalCreatureModel == null) {
            tyrantMythicalCreatureModel = new TyrantMythicalCreatureModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TyrantMythicalCreatureModel.LAYER_LOCATION)
            );
        }

        poseStack.pushPose();

        // Position at entity center
        poseStack.translate(0.0, entity.getBbHeight() / 2.0 + .5, 0.0);

        // Rotate with the player's body rotation
        // Use yBodyRot for smooth rotation, or getYRot() for instant rotation
        float yaw = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw)); // 180.0F to face the correct direction

        // Scale if needed
        poseStack.scale(1.0F, -1.0F, 1.0F);

        // Get the vertex consumer with your texture
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tyrantMythicalCreatureTexture));

        // Setup animation
        tyrantMythicalCreatureModel.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);

        // Render the model
        tyrantMythicalCreatureModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }

    private static void renderEnergyMass(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, LivingEntity entity, float partialTick) {
        poseStack.pushPose();

        // Get entity dimensions for scaling
        float entityHeight = entity.getBbHeight();
        float entityWidth = entity.getBbWidth();

        // Center the effect on the entity
        poseStack.translate(0, entityHeight / 2, 0);

        // Animation parameters
        long gameTime = entity.level().getGameTime();
        float time = (gameTime + partialTick) * 0.05F;

        // Pulsating scale effect
        float pulseScale = 1.0F + (Mth.sin(time * 2.0F) * 0.1F);
        poseStack.scale(pulseScale, pulseScale, pulseScale);

        // Rotation for dynamic effect
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 20.0F));

        // Get vertex consumer for rendering
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.energySwirl(
                ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png"),
                time * 0.01F,
                time * 0.01F
        ));

        // Render main energy sphere
        renderEnergySphere(poseStack, vertexConsumer, packedLight, entityWidth * 1.5F, 32, 16);

        // Render orbiting energy particles
        for (int i = 0; i < 8; i++) {
            poseStack.pushPose();

            float orbitAngle = (time + i * 45.0F) * (i % 2 == 0 ? 1.0F : -1.0F);
            float orbitRadius = entityWidth * 1.2F;
            float orbitHeight = Mth.sin(time + i) * entityHeight * 0.3F;

            poseStack.mulPose(Axis.YP.rotationDegrees(orbitAngle));
            poseStack.translate(orbitRadius, orbitHeight, 0);

            // Render small energy orbs
            renderEnergyOrb(poseStack, vertexConsumer, packedLight, 0.15F);

            poseStack.popPose();
        }

        // Render inner core with different color/glow
        VertexConsumer coreConsumer = multiBufferSource.getBuffer(RenderType.eyes(
                ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png")
        ));
        renderEnergySphere(poseStack, coreConsumer, 15728880, entityWidth * 0.8F, 16, 8);

        poseStack.popPose();
    }

    private static void renderEnergySphere(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float radius, int longitudeSegments, int latitudeSegments) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (int lat = 0; lat < latitudeSegments; lat++) {
            float theta1 = (lat * Mth.PI) / latitudeSegments;
            float theta2 = ((lat + 1) * Mth.PI) / latitudeSegments;

            for (int lon = 0; lon < longitudeSegments; lon++) {
                float phi1 = (lon * 2.0F * Mth.PI) / longitudeSegments;
                float phi2 = ((lon + 1) * 2.0F * Mth.PI) / longitudeSegments;

                // Calculate vertices for quad
                float x1 = radius * Mth.sin(theta1) * Mth.cos(phi1);
                float y1 = radius * Mth.cos(theta1);
                float z1 = radius * Mth.sin(theta1) * Mth.sin(phi1);

                float x2 = radius * Mth.sin(theta1) * Mth.cos(phi2);
                float y2 = radius * Mth.cos(theta1);
                float z2 = radius * Mth.sin(theta1) * Mth.sin(phi2);

                float x3 = radius * Mth.sin(theta2) * Mth.cos(phi2);
                float y3 = radius * Mth.cos(theta2);
                float z3 = radius * Mth.sin(theta2) * Mth.sin(phi2);

                float x4 = radius * Mth.sin(theta2) * Mth.cos(phi1);
                float y4 = radius * Mth.cos(theta2);
                float z4 = radius * Mth.sin(theta2) * Mth.sin(phi1);

                // Calculate UVs
                float u1 = (float) lon / longitudeSegments;
                float u2 = (float) (lon + 1) / longitudeSegments;
                float v1 = (float) lat / latitudeSegments;
                float v2 = (float) (lat + 1) / latitudeSegments;

                // Render quad
                addVertex(vertexConsumer, matrix, normal, x1, y1, z1, u1, v1, packedLight);
                addVertex(vertexConsumer, matrix, normal, x2, y2, z2, u2, v1, packedLight);
                addVertex(vertexConsumer, matrix, normal, x3, y3, z3, u2, v2, packedLight);
                addVertex(vertexConsumer, matrix, normal, x4, y4, z4, u1, v2, packedLight);
            }
        }
    }

    private static void renderEnergyOrb(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, float radius) {
        renderEnergySphere(poseStack, vertexConsumer, packedLight, radius, 8, 4);
    }

    private static void addVertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, int packedLight) {
        // Calculate normal vector
        float length = Mth.sqrt(x * x + y * y + z * z);
        float nx = x / length;
        float ny = y / length;
        float nz = z / length;

        vertexConsumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }

    private static void renderAngelicWings(PoseStack poseStack, MultiBufferSource buffer,
                                           int packedLight, LivingEntity entity, float partialTick) {
        poseStack.pushPose();

        // Position at shoulder height
        float entityHeight = entity.getBbHeight();
        poseStack.translate(0, entityHeight * 0.75f, 0);

        // Apply ONLY the body rotation - no camera adjustments
        // This keeps wings fixed to the player's back regardless of camera angle
        float yaw = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw));

        // Move wings behind the player
        poseStack.translate(0, 0, .3f);

        // Animation parameters
        float time = entity.tickCount + partialTick;
        float flapAngle = Mth.sin(time * 0.08f) * 15f; // Gentle flapping motion
        float glowPulse = 0.85f + Mth.sin(time * 0.12f) * 0.15f; // Pulsing glow

        // Wing dimensions
        float wingSpan = 2.2f; // How far out the wing extends
        float wingHeight = 2.8f; // Height of the wing

        // Use entity translucent emissive for glowing effect
        RenderType renderType = RenderType.entityTranslucentEmissive(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/sun/gold.png")
        );
        VertexConsumer consumer = buffer.getBuffer(renderType);

        // Render left wing
        poseStack.pushPose();
        poseStack.translate(-0.2f, 0, 0); // Offset from center
        poseStack.mulPose(Axis.YP.rotationDegrees(-35 - flapAngle)); // Angle outward with flap
        poseStack.mulPose(Axis.ZP.rotationDegrees(10)); // Slight upward tilt
        renderDetailedWing(poseStack, consumer, wingSpan, wingHeight, packedLight, glowPulse, false);
        poseStack.popPose();

        // Render right wing (mirrored)
        poseStack.pushPose();
        poseStack.translate(0.2f, 0, 0); // Offset from center
        poseStack.mulPose(Axis.YP.rotationDegrees(35 + flapAngle)); // Angle outward with flap (opposite)
        poseStack.mulPose(Axis.ZP.rotationDegrees(-10)); // Slight upward tilt (mirrored)
        renderDetailedWing(poseStack, consumer, wingSpan, wingHeight, packedLight, glowPulse, true);
        poseStack.popPose();

        // Add outer glow layer for extra radiance
        RenderType glowType = RenderType.energySwirl(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/sun/gold.png"),
                0, 0
        );
        VertexConsumer glowConsumer = buffer.getBuffer(glowType);

        // Left wing glow (slightly larger)
        poseStack.pushPose();
        poseStack.translate(-0.2f, 0, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-35 - flapAngle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(10));
        poseStack.scale(1.15f, 1.15f, 1.0f);
        renderDetailedWing(poseStack, glowConsumer, wingSpan, wingHeight, 15728880, glowPulse * 0.5f, false);
        poseStack.popPose();

        // Right wing glow
        poseStack.pushPose();
        poseStack.translate(0.2f, 0, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(35 + flapAngle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-10));
        poseStack.scale(1.15f, 1.15f, 1.0f);
        renderDetailedWing(poseStack, glowConsumer, wingSpan, wingHeight, 15728880, glowPulse * 0.5f, true);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderDetailedWing(PoseStack poseStack, VertexConsumer consumer,
                                           float span, float height, int light, float glowIntensity, boolean mirror) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        // Create a wing with proper feather-like shape
        // Wings are wider at the base and taper to a point at the top
        int horizontalSegments = 12; // More segments for smoother curve
        int verticalSegments = 16;

        for (int v = 0; v < verticalSegments; v++) {
            float v1 = (float) v / verticalSegments;
            float v2 = (float) (v + 1) / verticalSegments;

            for (int h = 0; h < horizontalSegments; h++) {
                float u1 = (float) h / horizontalSegments;
                float u2 = (float) (h + 1) / horizontalSegments;

                // Create wing shape points
                Vec3 p1 = getWingPoint(u1, v1, span, height, mirror);
                Vec3 p2 = getWingPoint(u2, v1, span, height, mirror);
                Vec3 p3 = getWingPoint(u2, v2, span, height, mirror);
                Vec3 p4 = getWingPoint(u1, v2, span, height, mirror);

                // Calculate alpha fade towards edges
                float edgeFade = 1.0f - (u1 * 0.3f); // Fade at wing tip
                float tipFade = 1.0f - (v1 * v1 * 0.4f); // Fade at top
                float alpha = glowIntensity * edgeFade * tipFade;
                int alphaValue = (int) (Mth.clamp(alpha, 0, 1) * 255);

                // Add vertices for this quad
                addWingVertex(consumer, matrix, normalMat, p1, u1, v1, light, alphaValue);
                addWingVertex(consumer, matrix, normalMat, p2, u2, v1, light, alphaValue);
                addWingVertex(consumer, matrix, normalMat, p3, u2, v2, light, alphaValue);
                addWingVertex(consumer, matrix, normalMat, p4, u1, v2, light, alphaValue);
            }
        }
    }

    private static Vec3 getWingPoint(float u, float v, float span, float height, boolean mirror) {
        // u (0 = near back), 1 = wing tip
        // v (0 = lower feathers), 1 = top taper

        // Quadratic wing taper (keeps thickness at base)
        float widthAtHeight = 1.0f - (v * v * 0.85f);

        // --- Main silhouette shape --- //
        // Bold shoulder, sweeping primaries
        float shoulderCurve = Mth.sin(u * 1.3f) * 0.55f;       // bulge near body
        float backwardSweep = -(u * u * 0.85f);                // swept primaries
        float primaryDip = Mth.sin((1 - u) * 1.1f) * 0.25f;    // dip outer feathers slightly

        float xOffset = ((shoulderCurve + u * span * widthAtHeight) * (mirror ? 1 : -1));
        float yOffset = (v * height) + primaryDip - (height * 0.2f);
        float zOffset = backwardSweep + v * 0.15f;

        // --- Feather band simulation (3 subtle rows) --- //
        float band = (float) Math.floor(v * 3f) / 3f;
        float bandOffset = (v - band) * 0.12f; // Small stagger between layers
        yOffset -= bandOffset;

        // --- Soft angelic ripple --- //
        // Much smoother + less noisy than before
        float ripple = Mth.sin(u * 6f + v * 2.5f) * 0.025f * (1.0f - v);
        zOffset += ripple;

        // --- Wing twist for organic flow --- //
        float twistDegrees = (u - 0.4f) * v * 14f;
        Quaternionf twist = Axis.XP.rotationDegrees(twistDegrees);

        Vector3f pos = new Vector3f(xOffset, yOffset, zOffset);
        pos.rotate(twist);

        return new Vec3(pos.x(), pos.y(), pos.z());
    }


    private static void addWingVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMat,
                                      Vec3 pos, float u, float v, int light, int alpha) {
        // Calculate normal based on position for proper lighting
        Vec3 normal = pos.normalize();

        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
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