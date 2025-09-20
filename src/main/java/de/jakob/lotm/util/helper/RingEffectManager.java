package de.jakob.lotm.util.helper;

import de.jakob.lotm.network.packets.RingEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side ring effect manager - handles creating ring effects that all players can see
 */
public class RingEffectManager {
    
    /**
     * Creates a ring effect that ALL PLAYERS can see (requires server-side call)
     */
    public static void createRingForAll(Vec3 center, float maxRadius, int duration,
                                        float red, float green, float blue, float alpha,
                                        float ringThickness, float ringHeight,
                                        float expansionSpeed, boolean smoothExpansion,
                                        ServerLevel level) {
        // Send packet to all players in range
        RingEffectPacket packet = new RingEffectPacket(
                center.x, center.y, center.z,
                maxRadius, duration,
                red, green, blue, alpha,
                ringThickness, ringHeight, expansionSpeed, smoothExpansion
        );

        // Send to all players within 64 blocks using AABB
        AABB area = new AABB(center.subtract(64, 64, 64), center.add(64, 64, 64));
        for (ServerPlayer player : level.players()) {
            if (area.contains(player.position())) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    /**
     * Creates a ring effect for all players with simple parameters
     */
    public static void createRingForAll(Vec3 center, float maxRadius, int duration,
                                        float red, float green, float blue, float alpha,
                                        float ringThickness, float ringHeight,
                                        ServerLevel level) {
        createRingForAll(center, maxRadius, duration, red, green, blue, alpha,
                ringThickness, ringHeight, 1.0f, true, level);
    }

    /**
     * Creates a pulsing ring effect visible to all players
     */
    public static void createPulsingRingForAll(Vec3 center, float maxRadius, int pulseCount,
                                               int pulseDuration, int delayBetweenPulses,
                                               float red, float green, float blue, float alpha,
                                               float ringThickness, float ringHeight,
                                               ServerLevel level) {
        for (int i = 0; i < pulseCount; i++) {
            // Schedule each pulse with a delay
            int finalI = i;
            level.getServer().execute(() -> {
                try {
                    Thread.sleep(finalI * delayBetweenPulses * 50L); // Convert ticks to ms
                    createRingForAll(center, maxRadius, pulseDuration, red, green, blue, alpha,
                            ringThickness, ringHeight, level);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    /**
     * Preset effect creators for networked effects - ALL PLAYERS SEE THEM
     */
    public static class Presets {
        public static void explosionRing(Vec3 center, ServerLevel level) {
            createRingForAll(center, 8.0f, 30, 1.0f, 0.4f, 0.0f, 0.8f, 0.8f, 2.0f, level);
        }

        public static void healingRing(Vec3 center, ServerLevel level) {
            createRingForAll(center, 5.0f, 60, 0.0f, 1.0f, 0.4f, 0.6f, 0.5f, 1.5f, level);
        }

        public static void magicRipple(Vec3 center, ServerLevel level) {
            createPulsingRingForAll(center, 6.0f, 3, 40, 10, 0.6f, 0.0f, 1.0f, 0.7f, 0.3f, 0.8f, level);
        }

        public static void playerAreaEffect(Vec3 center, float radius, ServerLevel level) {
            createRingForAll(center, radius, 100, 0.0f, 0.8f, 1.0f, 0.5f, 1.0f, 3.0f, level);
        }

        public static void shockwave(Vec3 center, ServerLevel level) {
            createRingForAll(center, 15.0f, 25, 1.0f, 1.0f, 1.0f, 0.9f, 2.0f, 0.5f, level);
        }

        public static void portalRing(Vec3 center, ServerLevel level) {
            createRingForAll(center, 4.0f, 200, 0.5f, 0.0f, 1.0f, 0.8f, 0.3f, 4.0f, level);
        }

        public static void beyonderAbility(Vec3 center, String pathway, ServerLevel level) {
            // Different ring effects based on pathway
            switch (pathway.toLowerCase()) {
                case "fool" -> createRingForAll(center, 12.0f, 80, 0.8f, 0.8f, 0.0f, 0.7f, 1.2f, 2.5f, level);
                case "door" -> createRingForAll(center, 10.0f, 60, 0.6f, 0.0f, 0.8f, 0.7f, 0.4f, 4.0f, level);
                case "error" -> createPulsingRingForAll(center, 8.0f, 4, 30, 8, 1.0f, 0.2f, 0.2f, 0.8f, 0.6f, 1.0f, level);
                default -> createRingForAll(center, 8.0f, 60, 1.0f, 1.0f, 1.0f, 0.6f, 0.8f, 2.0f, level);
            }
        }

        public static void cleansingRing(Vec3 center, ServerLevel level) {
            createRingForAll(center, 2, 60, 122 / 255f, 235 / 255f, 124 / 255f, 1, .5f, .75f, level);
        }
    }
}