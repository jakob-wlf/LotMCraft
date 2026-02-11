package de.jakob.lotm.util.helper;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.ChangePlayerPerspectivePacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

import static de.jakob.lotm.util.BeyonderData.*;

public class AdvancementUtil {

    public static void advance(LivingEntity entity, String pathway, int sequence) {
        if(entity instanceof Player player && player.isCreative()) {
            setBeyonder(entity, pathway, sequence);
            return;
        }

        SanityComponent sanityComp = entity.getData(ModAttachments.SANITY_COMPONENT);
        float sanity = sanityComp.getSanity();

        // First time becoming a beyonder
        if(!isBeyonder(entity)) {
            double failureChance = calculateFailureChanceForFirstTime(sequence, sanity);
            int duration = calculateAdvancementDuration(sequence);

            // Start particle effects
            scheduleAdvancementParticles(entity, pathway, duration);
            scheduleFloating(entity, duration);
            scheduleThirdPerson(entity, duration);
            scheduleFog(entity, duration, pathway);
            scheduleRandomDamage(entity, duration, sequence);

            if(failureChance >= 1.0 || Math.random() < failureChance) {
                // Advancement will fail - death at random point during advancement
                int deathTime = (int)(Math.random() * duration);
                ServerScheduler.scheduleDelayed(deathTime, () -> {
                    if(!entity.isDeadOrDying())
                        entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
                });
                return;
            }

            ServerScheduler.scheduleDelayed(duration, () -> {
                setBeyonder(entity, pathway, sequence);
                if(entity instanceof ServerPlayer serverPlayer) {
                    PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.THIRD.getValue()));
                }
            });
            return;
        }

        String prevPathway = getPathway(entity);

        // Wrong pathway - automatic failure
        if(!prevPathway.equals(pathway)) {
            int duration = calculateAdvancementDuration(sequence);

            // Start particle effects
            scheduleAdvancementParticles(entity, pathway, duration);
            scheduleFloating(entity, duration);
            scheduleThirdPerson(entity, duration);
            scheduleFog(entity, duration, pathway);
            scheduleRandomDamage(entity, duration, sequence);

            int deathTime = (int)(Math.random() * duration);
            ServerScheduler.scheduleDelayed(deathTime, () -> {
                if(!entity.isDeadOrDying())
                    entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
            });
            return;
        }

        int prevSequence = getSequence(entity);

        // Can't advance to same or higher sequence number (lower power)
        if(prevSequence <= sequence) {
            // Just return - no advancement happens
            return;
        }

        // Get digestion progress
        float digestionProgress = 0;
        if(entity instanceof Player player) {
            digestionProgress = BeyonderData.getDigestionProgress(player);
        }

        // Calculate sequence difference (how many sequences jumping)
        int difference = Math.abs(prevSequence - sequence);

        // Calculate failure chance
        double failureChance = calculateFailureChance(difference, digestionProgress, sanity);
        int duration = calculateAdvancementDuration(sequence);

        // Start particle effects
        scheduleAdvancementParticles(entity, pathway, duration);
        scheduleFloating(entity, duration);
        scheduleThirdPerson(entity, duration);
        scheduleFog(entity, duration, pathway);
        scheduleRandomDamage(entity, duration, sequence);

        // Check if advancement will fail
        if(failureChance >= 1.0 || Math.random() < failureChance) {
            // Advancement will fail - death at random point during advancement
            int deathTime = (int)(Math.random() * duration);
            ServerScheduler.scheduleDelayed(deathTime, () -> {
                if(!entity.isDeadOrDying())
                    entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
            });
            return;
        }

        // Advancement will succeed - apply after full duration
        ServerScheduler.scheduleDelayed(duration, () -> {
            if(!entity.isDeadOrDying()) {
                setBeyonder(entity, pathway, sequence);
                if(entity instanceof ServerPlayer serverPlayer) {
                    PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.THIRD.getValue()));
                }
            }
        });
    }

    private static void scheduleFog(LivingEntity entity, int duration, String pathway) {
        if(!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int colorInt = BeyonderData.pathwayInfos.get(pathway).color();
        float baseRed = ((colorInt >> 16) & 0xFF) / 255.0f;
        float baseGreen = ((colorInt >> 8) & 0xFF) / 255.0f;
        float baseBlue = (colorInt & 0xFF) / 255.0f;

        AtomicInteger tickCounter = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            int tick = tickCounter.getAndIncrement();

            // Create a slow sine wave for hue shifting (0.05 is the shift speed)
            float hueShift = (float)Math.sin(tick * 0.05f) * 0.3f; // ±0.15 hue shift

            // Apply hue shift to each color channel
            float red = Math.max(0.0f, Math.min(1.0f, baseRed + hueShift));
            float green = Math.max(0.0f, Math.min(1.0f, baseGreen + hueShift * 0.8f));
            float blue = Math.max(0.0f, Math.min(1.0f, baseBlue + hueShift * 0.6f));

            FogComponent fogComponent = serverPlayer.getData(ModAttachments.FOG_COMPONENT);
            fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.ADVANCING, entity);
            fogComponent.setActiveAndSync(true, entity);
            fogComponent.setFogColorAndSync(new Vec3f(red, green, blue), entity);
        });
    }

    /**
     * Schedule random damage events during advancement
     * Damage scales with sequence power but never kills the entity
     * Lower sequences (more powerful) cause more frequent and intense damage
     */
    private static void scheduleRandomDamage(LivingEntity entity, int duration, int sequence) {
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int baseDamageInterval = 15;

        // Calculate how many damage events will occur
        int damageEventCount = duration / baseDamageInterval;

        for(int i = 0; i < damageEventCount; i++) {
            // Random offset to make timing unpredictable
            int randomOffset = (int)(Math.random() * (baseDamageInterval / 2));
            int damageTime = (i * baseDamageInterval) + randomOffset;

            ServerScheduler.scheduleDelayed(damageTime, () -> {
                if(entity.isDeadOrDying()) return;

                float currentHealth = entity.getHealth();

                // Calculate damage based on sequence
                // Sequence 9: 1-2 hearts damage
                // Sequence 5: 2-4 hearts damage
                // Sequence 1: 3-6 hearts damage
                float baseDamage = 2.0f + (9 - sequence) * 0.5f;
                float randomVariation = (float)(Math.random() * 2.0f);
                float damage = baseDamage + randomVariation;

                // Never reduce health below 2 hearts (4 health points)
                float minHealthAfterDamage = 4.0f;
                float safeMaxDamage = currentHealth - minHealthAfterDamage;

                if(safeMaxDamage > 0) {
                    float finalDamage = Math.min(damage, safeMaxDamage);
                    entity.hurt(entity.damageSources().magic(), finalDamage);
                }
            }, serverLevel);
        }
    }

    private static void scheduleThirdPerson(LivingEntity entity, int duration) {
        if(!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            if(entity.isDeadOrDying()) return;

            PacketHandler.sendToPlayer(serverPlayer, new ChangePlayerPerspectivePacket(entity.getId(), ChangePlayerPerspectivePacket.PERSPECTIVE.THIRD.getValue()));
        });
    }

    private static void scheduleFloating(LivingEntity entity, int duration) {
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 position = entity.position().add(0, 1.5, 0);
        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(entity.isDeadOrDying()) return;

            entity.teleportTo(position.x, position.y, position.z);
        }, serverLevel);
    }

    /**
     * Schedule particle effects for advancement
     * Creates converging dust particles and spiral enchantment particles
     */
    private static void scheduleAdvancementParticles(LivingEntity entity, String pathway, int duration) {
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get pathway color
        int colorInt = BeyonderData.pathwayInfos.get(pathway).color();
        float red = ((colorInt >> 16) & 0xFF) / 255.0f;
        float green = ((colorInt >> 8) & 0xFF) / 255.0f;
        float blue = (colorInt & 0xFF) / 255.0f;

        DustParticleOptions dustParticle = new DustParticleOptions(
                new Vector3f(red, green, blue),
                2.5f
        );

        Vec3 center = entity.position().add(0, entity.getBbHeight() / 2 + 1.5, 0);

        // Phase 1: Converging particles (first 60% of duration)
        int convergingDuration = (int)(duration * 0.6);
        scheduleConvergingParticles(serverLevel, entity, convergingDuration);

        // Phase 2: Sphere formation (next 30% of duration)
        int sphereStart = convergingDuration;
        int sphereDuration = (int)(duration * 0.3);
        scheduleSphereParticles(serverLevel, entity, dustParticle, sphereStart, sphereDuration);

        // Phase 3: Fade out (last 10% of duration)
        int fadeStart = sphereStart + sphereDuration;
        int fadeDuration = duration - fadeStart;
        scheduleFadeParticles(serverLevel, entity, dustParticle, fadeStart, fadeDuration);

        // Secondary effect: Enchantment spiral throughout entire duration
        ParticleUtil.createParticleSpirals(
                serverLevel,
                ParticleTypes.ENCHANT,
                center,
                2,  // start radius
                2,  // end radius
                entity.getBbHeight(),  // height
                .75,  // speed
                2,  // density
                duration,
                8,    // spiral count
                2     // delay between spirals
        );
    }

    /**
     * Phase 1: Particles converge from outer radius toward player
     */
    private static void scheduleConvergingParticles(ServerLevel level, LivingEntity entity, int duration) {
        AtomicInteger currentTick = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(entity.isDeadOrDying()) return;

            int tick = currentTick.getAndIncrement();
            Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);

            // Increase particle count over time (accelerating effect)
            int particleCount = 8 + (tick * 15) / duration; // 8 to 23 particles

            // Speed increases over time
            double progressRatio = (double)tick / duration;
            double speed = 0.15 + (progressRatio * 0.45); // 0.15 to 0.6 speed

            for(int i = 0; i < particleCount; i++) {
                // Random spherical position around player (full 3D sphere)
                double radius = 2 + Math.random() * 2.0; // Random radius between 4-6 blocks
                double theta = Math.random() * 2 * Math.PI; // Random angle around Y axis
                double phi = Math.random() * Math.PI; // Random angle from top to bottom

                // Convert spherical to cartesian coordinates
                double startX = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double startY = center.y + radius * Math.cos(phi);
                double startZ = center.z + radius * Math.sin(phi) * Math.sin(theta);

                // Calculate direction vector toward center
                Vec3 direction = center.subtract(new Vec3(startX, startY, startZ)).normalize();

                // Set count to 1 and use offsets as velocity (dx, dy, dz parameters act as velocity)
                level.sendParticles(
                        ParticleTypes.SMOKE,
                        startX,
                        startY,
                        startZ,
                        0,
                        direction.x * speed,
                        direction.y * speed,
                        direction.z * speed,
                        1.0
                );
            }
        }, level);
    }

    /**
     * Phase 2: Sphere formation around player
     */
    private static void scheduleSphereParticles(ServerLevel level, LivingEntity entity,
                                                DustParticleOptions particle, int startDelay, int duration) {
        ServerScheduler.scheduleDelayed(startDelay, () -> {
            if(entity.isDeadOrDying()) return;

            Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);
            double radius = 1.5;

            ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
                if(entity.isDeadOrDying()) return;

                Vec3 currentCenter = entity.position().add(0, entity.getBbHeight() / 2, 0);

                // Create sphere of particles
                ParticleUtil.spawnSphereParticles(level, particle, currentCenter, radius, 60);
            }, level);
        });
    }

    /**
     * Phase 3: Fade out effect
     */
    private static void scheduleFadeParticles(ServerLevel level, LivingEntity entity,
                                              DustParticleOptions particle, int startDelay, int duration) {
        ServerScheduler.scheduleDelayed(startDelay, () -> {
            if(entity.isDeadOrDying()) return;

            AtomicInteger currentTick = new AtomicInteger(0);

            ServerScheduler.scheduleForDuration(0, 3, duration, () -> {
                if(entity.isDeadOrDying()) return;

                int tick = currentTick.getAndIncrement();
                Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);

                // Fewer particles over time
                double progressRatio = (double)tick / (duration / 3);
                int particleCount = (int)(60 * (1.0 - progressRatio));

                if(particleCount > 0) {
                    double radius = 1.5 + (progressRatio * 0.5); // Slightly expanding
                    ParticleUtil.spawnSphereParticles(level, particle, center, radius, particleCount);
                }
            }, level);
        });
    }

    /**
     * Calculate advancement duration based on sequence
     * Higher sequences (numerically lower) take longer
     * Sequence 9: ~5 seconds
     * Sequence 1: ~30 seconds
     */
    private static int calculateAdvancementDuration(int sequence) {
        // Base duration: lower sequence number = longer time
        // seq 9 = 5s, seq 5 = 15s, seq 1 = 30s
        int baseSeconds = 5 + (9 - sequence) * 3;
        return 20 * baseSeconds; // Convert to ticks
    }

    /**
     * Calculate failure chance for first time becoming a beyonder
     * Starting at high sequences (seq 9, 8, 7) is safer
     * Starting at low sequences (seq 1, 2, 3) is extremely dangerous
     * Returns 0.0 (0% failure) to 1.0 (100% failure)
     */
    private static double calculateFailureChanceForFirstTime(int sequence, float sanity) {
        // Sanity below 0.2 = automatic failure
        if(sanity < 0.2f) {
            return 1.0;
        }

        double baseChance;

        if(sequence >= 9) {
            baseChance = 0.0;
        } else if(sequence >= 7) {
            baseChance = 0.85;
        } else {
            baseChance = 1;
        }

        // Sanity penalty - only matters below 0.8
        double sanityPenalty = 0;
        if(sanity < 0.8f) {
            sanityPenalty = (0.8f - sanity) * 0.4; // 0 to 0.24 range (at 0.2 sanity = 0.24 penalty)
        }

        double totalChance = baseChance + sanityPenalty;
        return Math.min(1.0, Math.max(0.0, totalChance));
    }

    /**
     * Calculate failure chance based on sequence difference, digestion progress, and sanity
     * Returns 0.0 (0% failure) to 1.0 (100% failure)
     *
     * Perfect conditions (diff=1, digestion>=0.95, sanity>=0.8) = 0% failure
     * Sanity below 0.2 = automatic failure (100%)
     * Difference > 2 = automatic failure (100%)
     */
    private static double calculateFailureChance(int sequenceDifference, float digestion, float sanity) {
        // Automatic failure conditions
        if(sanity < 0.2f || sequenceDifference > 2) {
            return 1.0;
        }

        double baseChance;

        if(sequenceDifference == 1) {
            // Single sequence advancement - safe if conditions are met
            if(digestion >= 0.95f && sanity >= 0.8f) {
                baseChance = 0.0; // Perfect conditions
            } else if(digestion >= 0.95f || sanity >= 0.8f) {
                // One condition met but not both
                if(digestion >= 0.95f) {
                    // Good digestion but low sanity
                    baseChance = 0.65; // 20% failure
                } else {
                    // Good sanity but low digestion
                    baseChance = 0.6; // 10% failure
                }
            } else {
                // Neither condition met
                baseChance = 0.3; // 30% failure
            }
        } else if(sequenceDifference == 2) {
            // Two sequences - very dangerous regardless
            baseChance = 0.9; // 60% base failure
        } else {
            // This shouldn't happen due to the check above, but just in case
            return 1.0;
        }

        // Digestion penalty - maximum +0.2 (20%) impact
        double digestionPenalty = 0;
        if(digestion < 0.95f) {
            digestionPenalty = Math.min(0.5, (0.95f - digestion) * 0.4);
        }

        // Sanity penalty - only applies below 0.8, scales heavily
        double sanityPenalty = 0;
        if(sanity < 0.8f) {
            // Scale from 0 to 0.4 based on how far below 0.8
            // At 0.2 sanity, penalty = 0.3 (near automatic failure when combined with base)
            sanityPenalty = (0.8f - sanity) * 0.5; // 0 to 0.3 range
        }

        // Combine all factors
        double totalChance = baseChance + digestionPenalty + sanityPenalty;

        // Cap at 1.0 (100% failure)
        return Math.min(1.0, Math.max(0.0, totalChance));
    }

}