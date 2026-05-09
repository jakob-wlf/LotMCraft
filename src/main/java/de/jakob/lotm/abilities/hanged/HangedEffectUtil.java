package de.jakob.lotm.abilities.hanged;

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

    private HangedEffectUtil() {
    }

    public static void spawnShadowAura(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.55, 0);
        double width = Math.max(0.35, entity.getBbWidth() * 0.45);
        double height = Math.max(0.45, entity.getBbHeight() * 0.35);
        level.sendParticles(SHADOW_OUTER, center.x, center.y, center.z, 12, width, height, width, 0.02);
        level.sendParticles(SHADOW_INNER, center.x, center.y, center.z, 7, width * 0.75, height * 0.8, width * 0.75, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 4, width, height, width, 0.01);
        level.sendParticles(ParticleTypes.ASH, center.x, center.y, center.z, 3, width, height, width, 0.005);
    }

    public static void spawnFleshAura(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.55, 0);
        double width = Math.max(0.35, entity.getBbWidth() * 0.42);
        double height = Math.max(0.45, entity.getBbHeight() * 0.35);
        level.sendParticles(FLESH_MAIN, center.x, center.y, center.z, 12, width, height, width, 0.02);
        level.sendParticles(FLESH_BLOOD, center.x, center.y, center.z, 8, width * 0.7, height * 0.7, width * 0.7, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 4, width, height, width, 0.005);
    }

    public static void spawnShadowBurst(ServerLevel level, Vec3 center, double radius, int count) {
        level.sendParticles(SHADOW_OUTER, center.x, center.y, center.z, count, radius, radius * 0.7, radius, 0.03);
        level.sendParticles(SHADOW_INNER, center.x, center.y, center.z, Math.max(6, count / 2), radius * 0.75, radius * 0.55, radius * 0.75, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, Math.max(4, count / 4), radius * 0.45, radius * 0.35, radius * 0.45, 0.01);
    }

    public static void spawnFleshBurst(ServerLevel level, Vec3 center, double radius, int count) {
        level.sendParticles(FLESH_MAIN, center.x, center.y, center.z, count, radius, radius * 0.75, radius, 0.03);
        level.sendParticles(FLESH_BLOOD, center.x, center.y, center.z, Math.max(6, count / 2), radius * 0.7, radius * 0.55, radius * 0.7, 0.02);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, Math.max(4, count / 4), radius * 0.4, radius * 0.35, radius * 0.4, 0.01);
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
}
