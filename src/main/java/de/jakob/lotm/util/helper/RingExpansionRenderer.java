package de.jakob.lotm.util.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.RingEffectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for rendering expanding ring/hollow cylinder effects in the world
 * Supports both client-only effects and networked effects visible to all players
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class RingExpansionRenderer {
    private static final List<RingEffect> activeEffects = new ArrayList<>();

    /**
     * Represents a single expanding ring/hollow cylinder effect
     */
    public static class RingEffect {
        private final Vec3 center;
        private final float maxRadius;
        private final int duration; // in ticks
        private final float red, green, blue, alpha;
        private final float ringThickness; // thickness of the ring wall
        private final float ringHeight; // height of the cylinder
        private final float expansionSpeed; // multiplier for expansion speed (1.0 = normal)
        private final boolean smoothExpansion; // whether to use smooth or linear expansion
        private final boolean enableOcclusion; // whether to test for block occlusion
        private int currentTick;

        public RingEffect(Vec3 center, float maxRadius, int duration,
                          float red, float green, float blue, float alpha,
                          float ringThickness, float ringHeight, float expansionSpeed,
                          boolean smoothExpansion) {
            this(center, maxRadius, duration, red, green, blue, alpha,
                    ringThickness, ringHeight, expansionSpeed, smoothExpansion, true);
        }

        public RingEffect(Vec3 center, float maxRadius, int duration,
                          float red, float green, float blue, float alpha,
                          float ringThickness, float ringHeight, float expansionSpeed,
                          boolean smoothExpansion, boolean enableOcclusion) {
            this.center = center;
            this.maxRadius = maxRadius;
            this.duration = duration;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.ringThickness = Math.max(0.1f, ringThickness); // Minimum thickness
            this.ringHeight = Math.max(0.1f, ringHeight); // Minimum height
            this.expansionSpeed = Math.max(0.1f, expansionSpeed); // Minimum speed
            this.smoothExpansion = smoothExpansion;
            this.enableOcclusion = enableOcclusion;
            this.currentTick = 0;
        }

        public boolean tick() {
            currentTick++;
            return currentTick >= duration;
        }

        public float getCurrentRadius() {
            float progress = (currentTick / (float) duration) * expansionSpeed;

            if (smoothExpansion) {
                // Ease-out curve for smoother expansion
                progress = (float)(1.0 - Math.pow(1.0 - Math.min(progress, 1.0), 2.0));
            }

            return Math.min(progress, 1.0f) * maxRadius;
        }

        public float getCurrentAlpha() {
            // Fade out over time
            float progress = currentTick / (float) duration;
            return alpha * (1.0f - progress);
        }

        public float getRingThickness() {
            return ringThickness;
        }

        public float getRingHeight() {
            return ringHeight;
        }

        public boolean isOcclusionEnabled() {
            return enableOcclusion;
        }
    }

    // ===========================================
    // CLIENT-ONLY METHODS (Only you see them)
    // ===========================================

    /**
     * Creates a new expanding ring effect at the player's position (CLIENT-ONLY)
     */
    public static void createRingAtPlayer(float maxRadius, int duration, float thickness, float height) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            createRingClientOnly(player.position(), maxRadius, duration,
                    1.0f, 1.0f, 1.0f, 0.8f, thickness, height, 1.0f, true);
        }
    }

    /**
     * Creates a new expanding ring effect at the specified position (CLIENT-ONLY)
     */
    public static void createRingClientOnly(Vec3 center, float maxRadius, int duration,
                                            float red, float green, float blue, float alpha,
                                            float ringThickness, float ringHeight) {
        createRingClientOnly(center, maxRadius, duration, red, green, blue, alpha,
                ringThickness, ringHeight, 1.0f, true);
    }

    /**
     * Creates a new expanding ring effect with full customization (CLIENT-ONLY)
     */
    public static void createRingClientOnly(Vec3 center, float maxRadius, int duration,
                                            float red, float green, float blue, float alpha,
                                            float ringThickness, float ringHeight,
                                            float expansionSpeed, boolean smoothExpansion) {
        createRingClientOnly(center, maxRadius, duration, red, green, blue, alpha,
                ringThickness, ringHeight, expansionSpeed, smoothExpansion, true);
    }

    /**
     * Creates a new expanding ring effect with full customization including occlusion (CLIENT-ONLY)
     */
    public static void createRingClientOnly(Vec3 center, float maxRadius, int duration,
                                            float red, float green, float blue, float alpha,
                                            float ringThickness, float ringHeight,
                                            float expansionSpeed, boolean smoothExpansion,
                                            boolean enableOcclusion) {
        RingEffect effect = new RingEffect(center, maxRadius, duration,
                red, green, blue, alpha, ringThickness, ringHeight, expansionSpeed,
                smoothExpansion, enableOcclusion);
        activeEffects.add(effect);
    }

    /**
     * Handle incoming ring effect packet from server (called by packet handler)
     */
    public static void handleRingEffectPacket(RingEffectPacket packet) {
        Vec3 center = new Vec3(packet.x(), packet.y(), packet.z());
        RingEffect effect = new RingEffect(
                center, packet.maxRadius(), packet.duration(),
                packet.red(), packet.green(), packet.blue(), packet.alpha(),
                packet.ringThickness(), packet.ringHeight(),
                packet.expansionSpeed(), packet.smoothExpansion()
        );
        activeEffects.add(effect);
    }

    /**
     * Creates a pulsing ring effect (multiple expanding rings) - CLIENT ONLY
     */
    public static void createPulsingRingClientOnly(Vec3 center, float maxRadius, int pulseCount,
                                                   int pulseDuration, int delayBetweenPulses,
                                                   float red, float green, float blue, float alpha,
                                                   float ringThickness, float ringHeight) {
        for (int i = 0; i < pulseCount; i++) {
            scheduleDelayedRing(center, maxRadius, pulseDuration,
                    red, green, blue, alpha, ringThickness, ringHeight, i * delayBetweenPulses);
        }
    }

    private static void scheduleDelayedRing(Vec3 center, float maxRadius, int duration,
                                            float red, float green, float blue, float alpha,
                                            float ringThickness, float ringHeight, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay * 50L); // Convert ticks to milliseconds (50ms per tick)
                createRingClientOnly(center, maxRadius, duration, red, green, blue, alpha,
                        ringThickness, ringHeight);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            renderAllRings(event.getPoseStack(), partialTick);
        }
    }

    private static void renderAllRings(PoseStack poseStack, float partialTick) {
        if (activeEffects.isEmpty()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // Set up rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest(); // Enable depth testing for occlusion
        RenderSystem.depthFunc(515); // GL_LEQUAL - render if depth is less than or equal
        RenderSystem.depthMask(false); // Don't write to depth buffer (for transparency)
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();

        // Get camera position for relative rendering
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();

        // Translate to world origin relative to camera
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f matrix = poseStack.last().pose();

        // Render each ring effect
        for (RingEffect effect : activeEffects) {
            renderRingEffect(tesselator, matrix, effect, partialTick);
        }

        poseStack.popPose();

        // Restore rendering state
        RenderSystem.depthMask(true); // Re-enable depth writing
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        // Tick and remove finished effects
        Iterator<RingEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            RingEffect effect = iterator.next();
            if (effect.tick()) {
                iterator.remove();
            }
        }
    }

    private static void renderRingEffect(Tesselator tesselator, Matrix4f matrix,
                                         RingEffect effect, float partialTick) {
        float radius = effect.getCurrentRadius();
        float alpha = effect.getCurrentAlpha();

        if (radius <= 0 || alpha <= 0) return;

        Vec3 center = effect.center;
        float thickness = effect.getRingThickness();
        float height = effect.getRingHeight();

        int segments = Math.max(16, (int)(radius * 2)); // More segments for larger rings

        // Check if we should use occlusion testing
        Player player = Minecraft.getInstance().player;
        boolean useOcclusion = effect.isOcclusionEnabled() && player != null && player.level() != null;

        renderHollowCylinder(tesselator, matrix, center, radius, thickness, height, segments,
                effect.red, effect.green, effect.blue, alpha, useOcclusion);
    }

    private static void renderHollowCylinder(Tesselator tesselator, Matrix4f matrix, Vec3 center,
                                             float radius, float thickness, float height, int segments,
                                             float red, float green, float blue, float alpha, boolean useOcclusion) {
        float outerRadius = radius;
        float innerRadius = Math.max(0, radius - thickness);
        float halfHeight = height / 2.0f;

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        Player player = Minecraft.getInstance().player;

        // Render the cylinder walls (outer and inner surfaces)
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(2.0 * Math.PI * i / segments);
            float angle2 = (float)(2.0 * Math.PI * (i + 1) / segments);

            float cos1 = (float)Math.cos(angle1);
            float sin1 = (float)Math.sin(angle1);
            float cos2 = (float)Math.cos(angle2);
            float sin2 = (float)Math.sin(angle2);

            float centerX = (float)center.x;
            float centerY = (float)center.y;
            float centerZ = (float)center.z;

            // Calculate vertex positions for occlusion testing
            Vec3[] vertices = {
                    new Vec3(centerX + cos1 * outerRadius, centerY - halfHeight, centerZ + sin1 * outerRadius),
                    new Vec3(centerX + cos2 * outerRadius, centerY - halfHeight, centerZ + sin2 * outerRadius),
                    new Vec3(centerX + cos1 * outerRadius, centerY + halfHeight, centerZ + sin1 * outerRadius),
                    new Vec3(centerX + cos2 * outerRadius, centerY + halfHeight, centerZ + sin2 * outerRadius)
            };

            // Calculate segment alpha based on occlusion
            float segmentAlpha = alpha;
            if (useOcclusion && player != null) {
                segmentAlpha *= calculateSegmentVisibility(vertices, player);
            }

            if (segmentAlpha <= 0.01f) continue; // Skip nearly invisible segments

            // Outer wall
            // Bottom triangle 1
            buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY - halfHeight, centerZ + sin1 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);
            buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY - halfHeight, centerZ + sin2 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);
            buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY + halfHeight, centerZ + sin1 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);

            // Bottom triangle 2
            buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY - halfHeight, centerZ + sin2 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);
            buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY + halfHeight, centerZ + sin2 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);
            buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY + halfHeight, centerZ + sin1 * outerRadius)
                    .setColor(red, green, blue, segmentAlpha);

            // Inner wall (if thickness creates visible inner surface)
            if (innerRadius > 0) {
                // Bottom triangle 1 (reversed winding for inner surface)
                buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY + halfHeight, centerZ + sin1 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY - halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY - halfHeight, centerZ + sin1 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);

                // Bottom triangle 2 (reversed winding for inner surface)
                buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY + halfHeight, centerZ + sin1 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY + halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY - halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
            }

            // Top ring face
            if (thickness > 0) {
                // Top face triangle 1
                buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY + halfHeight, centerZ + sin1 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY + halfHeight, centerZ + sin1 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY + halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);

                // Top face triangle 2
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY + halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY + halfHeight, centerZ + sin1 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY + halfHeight, centerZ + sin2 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);

                // Bottom ring face
                // Bottom face triangle 1 (reversed winding)
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY - halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY - halfHeight, centerZ + sin1 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * innerRadius, centerY - halfHeight, centerZ + sin1 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);

                // Bottom face triangle 2 (reversed winding)
                buffer.addVertex(matrix, centerX + cos2 * outerRadius, centerY - halfHeight, centerZ + sin2 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos1 * outerRadius, centerY - halfHeight, centerZ + sin1 * outerRadius)
                        .setColor(red, green, blue, segmentAlpha);
                buffer.addVertex(matrix, centerX + cos2 * innerRadius, centerY - halfHeight, centerZ + sin2 * innerRadius)
                        .setColor(red, green, blue, segmentAlpha);
            }
        }

        try {BufferUploader.drawWithShader(buffer.buildOrThrow());}
        catch (Exception ignored) {}
    }

    /**
     * Calculate visibility of a ring segment based on block occlusion and distance
     */
    private static float calculateSegmentVisibility(Vec3[] vertices, Player player) {
        if (player == null || player.level() == null) return 1.0f;

        Vec3 playerEyePos = player.getEyePosition();
        float totalVisibility = 0.0f;
        int samples = vertices.length;

        // Sample visibility at each vertex
        for (Vec3 vertex : vertices) {
            float visibility = calculatePointVisibility(vertex, playerEyePos, player.level());
            totalVisibility += visibility;
        }

        // Average visibility across all vertices
        float avgVisibility = totalVisibility / samples;

        // Apply distance-based attenuation
        float distance = (float) playerEyePos.distanceTo(vertices[0]);
        float distanceAttenuation = Math.max(0.1f, Math.min(1.0f, 32.0f / distance));

        return avgVisibility * distanceAttenuation;
    }

    /**
     * Calculate visibility of a single point from the player's perspective
     */
    private static float calculatePointVisibility(Vec3 point, Vec3 eyePos, net.minecraft.world.level.Level level) {
        // Perform raycast from player eye to the point
        Vec3 direction = point.subtract(eyePos);
        double distance = direction.length();

        if (distance < 0.1) return 1.0f; // Very close, assume visible

        direction = direction.normalize();

        // Sample along the ray at regular intervals
        int samples = Math.max(3, (int)(distance * 2)); // More samples for longer distances
        float visibilitySum = 0.0f;

        for (int i = 1; i <= samples; i++) {
            float t = (float)i / samples;
            Vec3 samplePoint = eyePos.add(direction.scale(distance * t));

            // Check if this point is inside a solid block
            net.minecraft.core.BlockPos blockPos = net.minecraft.core.BlockPos.containing(samplePoint);
            net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(blockPos);

            if (!blockState.isAir() && blockState.isSolidRender(level, blockPos)) {
                // Hit a solid block, reduce visibility
                visibilitySum += 0.0f;
            } else {
                visibilitySum += 1.0f;
            }
        }

        return visibilitySum / samples;
    }

    /**
     * Clears all active ring effects
     */
    public static void clearAllEffects() {
        activeEffects.clear();
    }

    /**
     * Gets the number of active ring effects
     */
    public static int getActiveEffectCount() {
        return activeEffects.size();
    }

    /**
     * Preset effect creators for common use cases - CLIENT ONLY
     */
    public static class PresetsClientOnly {
        public static void explosionRing(Vec3 center) {
            createRingClientOnly(center, 8.0f, 30, 1.0f, 0.4f, 0.0f, 0.8f, 0.8f, 2.0f, 1.2f, false);
        }

        public static void healingRing(Vec3 center) {
            createRingClientOnly(center, 5.0f, 60, 0.0f, 1.0f, 0.4f, 0.6f, 0.5f, 1.5f, 0.8f, true);
        }

        public static void magicRipple(Vec3 center) {
            createPulsingRingClientOnly(center, 6.0f, 3, 40, 10, 0.6f, 0.0f, 1.0f, 0.7f, 0.3f, 0.8f);
        }

        public static void playerAreaEffect(Player player, float radius) {
            if (player != null) {
                createRingClientOnly(player.position(), radius, 100, 0.0f, 0.8f, 1.0f, 0.5f, 1.0f, 3.0f, 0.6f, true);
            }
        }

        public static void shockwave(Vec3 center) {
            createRingClientOnly(center, 15.0f, 25, 1.0f, 1.0f, 1.0f, 0.9f, 2.0f, 0.5f, 2.0f, false);
        }

        public static void portalRing(Vec3 center) {
            createRingClientOnly(center, 4.0f, 200, 0.5f, 0.0f, 1.0f, 0.8f, 0.3f, 4.0f, 0.3f, true);
        }
    }
}