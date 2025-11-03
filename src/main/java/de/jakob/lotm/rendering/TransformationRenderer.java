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

        event.setCanceled(true);

        switch (component.getTransformationIndex()) {
            case 1 -> renderDesireApostleMass(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getPackedLight(), entity, event.getPartialTick());
        }
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
        RenderType renderType = RenderType.entitySolid(ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png"));
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
                .setColor(0, 0, 0, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null && shouldApplyEffect(player)) {
                applyShader(mc);
            } else {
                removeShader(mc);
            }
        }
    }

    private static boolean shouldApplyEffect(Player player) {
        return player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).isTransformed() &&
               player.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).getTransformationIndex() == TransformationComponent.TransformationType.DESIRE_AVATAR.getIndex();
    }

    private static void applyShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() == null ||
                !mc.gameRenderer.currentEffect().getName().equals("abyssal_distortion")) {
            try {
                mc.gameRenderer.loadEffect(
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "shaders/post/abyssal_distortion.json")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void removeShader(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() != null) {
            mc.gameRenderer.shutdownEffect();
        }
    }
}