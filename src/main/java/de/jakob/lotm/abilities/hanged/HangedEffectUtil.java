package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class HangedEffectUtil {
    private static final DustParticleOptions SHADOW_OUTER =
            new DustParticleOptions(new Vector3f(0.16f, 0.16f, 0.18f), 1.25f);
    private static final DustParticleOptions SHADOW_INNER =
            new DustParticleOptions(new Vector3f(0.03f, 0.03f, 0.04f), 0.95f);
    private static final DustParticleOptions FLESH_MAIN =
            new DustParticleOptions(new Vector3f(0.78f, 0.64f, 0.48f), 1.1f);
    private static final DustParticleOptions FLESH_BLOOD =
            new DustParticleOptions(new Vector3f(0.52f, 0.07f, 0.08f), 1.25f);
    private static final DustParticleOptions DEPRAVITY_MAIN =
            new DustParticleOptions(new Vector3f(0.07f, 0.07f, 0.08f), 1.4f);
    private static final DustParticleOptions DEPRAVITY_GLOW =
            new DustParticleOptions(new Vector3f(0.55f, 0.08f, 0.08f), 1.1f);

    private HangedEffectUtil() {
    }

    public static void spawnShadowAura(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.55, 0);
        Vec3 feet = entity.position().add(0, 0.06, 0);
        double width = Math.max(0.35, entity.getBbWidth() * 0.45);
        double height = Math.max(0.45, entity.getBbHeight() * 0.35);
        spawnRitualCircle(level, feet, Math.max(0.55, width * 1.8), 22, SHADOW_OUTER, SHADOW_INNER);
        spawnRitualCircle(level, center.add(0, height * 0.2, 0), Math.max(0.35, width * 0.9), 16, SHADOW_INNER, SHADOW_OUTER);
        level.sendParticles(SHADOW_OUTER, center.x, center.y, center.z, 12, width, height, width, 0.02);
        level.sendParticles(SHADOW_INNER, center.x, center.y, center.z, 7, width * 0.75, height * 0.8, width * 0.75, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 4, width, height, width, 0.01);
        level.sendParticles(ParticleTypes.ASH, center.x, center.y, center.z, 3, width, height, width, 0.005);
    }

    public static void spawnFleshAura(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.55, 0);
        Vec3 feet = entity.position().add(0, 0.06, 0);
        double width = Math.max(0.35, entity.getBbWidth() * 0.42);
        double height = Math.max(0.45, entity.getBbHeight() * 0.35);
        spawnRitualCircle(level, feet, Math.max(0.55, width * 1.9), 22, FLESH_MAIN, FLESH_BLOOD);
        spawnRitualCircle(level, center.add(0, height * 0.16, 0), Math.max(0.35, width * 0.9), 16, FLESH_BLOOD, FLESH_MAIN);
        level.sendParticles(FLESH_MAIN, center.x, center.y, center.z, 12, width, height, width, 0.02);
        level.sendParticles(FLESH_BLOOD, center.x, center.y, center.z, 8, width * 0.7, height * 0.7, width * 0.7, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 4, width, height, width, 0.005);
    }

    public static void spawnShadowBurst(ServerLevel level, Vec3 center, double radius, int count) {
        spawnRitualCircle(level, center.add(0, -0.1, 0), Math.max(0.45, radius), Math.max(12, count / 2), SHADOW_OUTER, SHADOW_INNER);
        ParticleUtil.spawnSphereParticles(level, SHADOW_INNER, center, Math.max(0.25, radius * 0.68), Math.max(8, count / 3));
        level.sendParticles(SHADOW_OUTER, center.x, center.y, center.z, count, radius, radius * 0.7, radius, 0.03);
        level.sendParticles(SHADOW_INNER, center.x, center.y, center.z, Math.max(6, count / 2), radius * 0.75, radius * 0.55, radius * 0.75, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, Math.max(4, count / 4), radius * 0.45, radius * 0.35, radius * 0.45, 0.01);
    }

    public static void spawnFleshBurst(ServerLevel level, Vec3 center, double radius, int count) {
        spawnRitualCircle(level, center.add(0, -0.08, 0), Math.max(0.45, radius), Math.max(12, count / 2), FLESH_MAIN, FLESH_BLOOD);
        ParticleUtil.spawnSphereParticles(level, FLESH_BLOOD, center, Math.max(0.25, radius * 0.64), Math.max(8, count / 3));
        level.sendParticles(FLESH_MAIN, center.x, center.y, center.z, count, radius, radius * 0.75, radius, 0.03);
        level.sendParticles(FLESH_BLOOD, center.x, center.y, center.z, Math.max(6, count / 2), radius * 0.7, radius * 0.55, radius * 0.7, 0.02);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, Math.max(4, count / 4), radius * 0.4, radius * 0.35, radius * 0.4, 0.01);
    }

    public static void spawnDepravityAura(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.6, 0);
        Vec3 feet = entity.position().add(0, 0.06, 0);
        double width = Math.max(0.45, entity.getBbWidth() * 0.52);
        double height = Math.max(0.5, entity.getBbHeight() * 0.45);
        spawnRitualCircle(level, feet, Math.max(0.7, width * 2.05), 26, DEPRAVITY_MAIN, DEPRAVITY_GLOW);
        spawnRitualCircle(level, center.add(0, height * 0.12, 0), Math.max(0.45, width), 18, DEPRAVITY_GLOW, DEPRAVITY_MAIN);
        level.sendParticles(DEPRAVITY_MAIN, center.x, center.y, center.z, 14, width, height, width, 0.02);
        level.sendParticles(DEPRAVITY_GLOW, center.x, center.y, center.z, 8, width * 0.8, height * 0.7, width * 0.8, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 5, width, height, width, 0.01);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 3, width * 0.6, height * 0.6, width * 0.6, 0.001);
    }

    public static void spawnDepravityBurst(ServerLevel level, Vec3 center, double radius, int count) {
        spawnRitualCircle(level, center.add(0, -0.12, 0), Math.max(0.45, radius), Math.max(14, count / 2), DEPRAVITY_MAIN, DEPRAVITY_GLOW);
        ParticleUtil.spawnSphereParticles(level, DEPRAVITY_GLOW, center, Math.max(0.3, radius * 0.7), Math.max(8, count / 3));
        level.sendParticles(DEPRAVITY_MAIN, center.x, center.y, center.z, count, radius, radius * 0.75, radius, 0.03);
        level.sendParticles(DEPRAVITY_GLOW, center.x, center.y, center.z, Math.max(6, count / 2), radius * 0.7, radius * 0.5, radius * 0.7, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, Math.max(4, count / 3), radius * 0.5, radius * 0.35, radius * 0.5, 0.01);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, Math.max(3, count / 5), radius * 0.45, radius * 0.25, radius * 0.45, 0.001);
    }

    public static void spawnShadowField(ServerLevel level, Vec3 center, double radius, int pulseStep) {
        double pulseRadius = Math.max(0.45, radius * (0.7 + ((pulseStep % 12) * 0.025)));
        spawnRitualCircle(level, center.add(0, 0.05, 0), pulseRadius, Math.max(24, (int) Math.ceil(radius * 14)), SHADOW_OUTER, SHADOW_INNER);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.08, center.z, Math.max(8, (int) Math.ceil(radius * 4)), radius * 0.45, 0.08, radius * 0.45, 0.003);
    }

    public static void spawnFleshField(ServerLevel level, Vec3 center, double radius, int pulseStep) {
        double pulseRadius = Math.max(0.45, radius * (0.72 + ((pulseStep % 14) * 0.022)));
        spawnRitualCircle(level, center.add(0, 0.05, 0), pulseRadius, Math.max(28, (int) Math.ceil(radius * 15)), FLESH_MAIN, FLESH_BLOOD);
        level.sendParticles(FLESH_BLOOD, center.x, center.y + 0.06, center.z, Math.max(10, (int) Math.ceil(radius * 4)), radius * 0.36, 0.06, radius * 0.36, 0.015);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.08, center.z, Math.max(8, (int) Math.ceil(radius * 4)), radius * 0.45, 0.08, radius * 0.45, 0.002);
    }

    public static void spawnDepravityField(ServerLevel level, Vec3 center, double radius, int pulseStep) {
        double pulseRadius = Math.max(0.45, radius * (0.72 + ((pulseStep % 16) * 0.02)));
        spawnRitualCircle(level, center.add(0, 0.05, 0), pulseRadius, Math.max(28, (int) Math.ceil(radius * 16)), DEPRAVITY_MAIN, DEPRAVITY_GLOW);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.08, center.z, Math.max(6, (int) Math.ceil(radius * 3)), radius * 0.32, 0.04, radius * 0.32, 0.001);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.08, center.z, Math.max(8, (int) Math.ceil(radius * 4)), radius * 0.45, 0.08, radius * 0.45, 0.003);
    }

    public static void spawnDepravityTrail(ServerLevel level, Vec3 start, Vec3 end, double step) {
        spawnTrail(level, start, end, step, DEPRAVITY_MAIN, DEPRAVITY_GLOW);
    }

    public static void spawnShadowTrail(ServerLevel level, Vec3 start, Vec3 end, double step) {
        spawnTrail(level, start, end, step, SHADOW_OUTER, SHADOW_INNER);
    }

    public static void spawnFleshTrail(ServerLevel level, Vec3 start, Vec3 end, double step) {
        spawnTrail(level, start, end, step, FLESH_MAIN, FLESH_BLOOD);
    }

    private static void spawnTrail(ServerLevel level, Vec3 start, Vec3 end, double step,
                                   DustParticleOptions primary, DustParticleOptions accent) {
        Vec3 diff = end.subtract(start);
        double distance = diff.length();
        if (distance < 0.001) {
            return;
        }

        Vec3 direction = diff.normalize();
        for (double travelled = 0; travelled <= distance; travelled += step) {
            Vec3 point = start.add(direction.scale(travelled));
            level.sendParticles(primary, point.x, point.y, point.z, 1, 0.08, 0.08, 0.08, 0);
            level.sendParticles(accent, point.x, point.y, point.z, 1, 0.04, 0.04, 0.04, 0);
        }
    }

    private static void spawnRitualCircle(ServerLevel level, Vec3 center, double radius, int particleCount,
                                          DustParticleOptions primary, DustParticleOptions accent) {
        if (radius < 0.1) {
            return;
        }

        ParticleUtil.spawnCircleParticles(level, primary, center, radius, particleCount);
        ParticleUtil.spawnCircleParticles(level, accent, center.add(0, 0.09, 0), Math.max(0.25, radius * 0.72), Math.max(10, particleCount - 6));
        ParticleUtil.spawnSphereParticles(level, accent, center.add(0, 0.12, 0), Math.max(0.15, radius * 0.3), Math.max(6, particleCount / 3));
    }

    public static void playShadowCast(ServerLevel level, Vec3 center) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.45f, 0.55f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.85f, 0.7f);
    }

    public static void playShadowPulse(ServerLevel level, Vec3 center, float pitch) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.45f, pitch);
    }

    public static void playFleshCast(ServerLevel level, Vec3 center) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.55f, 0.6f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 0.85f, 0.85f);
    }

    public static void playFleshPulse(ServerLevel level, Vec3 center, float pitch) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 0.55f, pitch);
    }

    public static void playDepravityCast(ServerLevel level, Vec3 center) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.2f, 0.7f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.55f);
    }

    public static void playDepravityPulse(ServerLevel level, Vec3 center, float pitch) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.65f, pitch);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.45f, pitch * 0.9f);
    }
}
