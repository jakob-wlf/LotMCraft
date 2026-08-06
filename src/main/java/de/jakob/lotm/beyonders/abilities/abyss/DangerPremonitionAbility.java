package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DangerPremonitionAbility extends ToggleAbility {

    private static final DustParticleOptions MALICE_DUST =
            new DustParticleOptions(new Vector3f(0.55f, 0.0f, 0.08f), 1.5f);
    private static final DustParticleOptions SENSE_DUST =
            new DustParticleOptions(new Vector3f(0.8f, 0.1f, 0.15f), 0.8f);

    private final Map<UUID, Set<UUID>> knownThreats = new HashMap<>();

    public DangerPremonitionAbility(String id) {
        super(id, "malice");
        doesNotIncreaseDigestion = true;
        tickRate = 10;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        knownThreats.put(entity.getUUID(), new HashSet<>());
        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.danger_premonition.activated")
                        .withColor(0x8B0000));
        ParticleUtil.spawnSphereParticles((ServerLevel) level, MALICE_DUST,
                entity.getEyePosition(), 3, 30);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        float spCost = 0.5f * multiplier(entity);
        if (!consumeSpiritualityQuietly(entity, spCost)) {
            cancel(serverLevel, entity);
            return;
        }

        Set<UUID> known = knownThreats.computeIfAbsent(entity.getUUID(), k -> new HashSet<>());
        int mySeq   = AbilityUtil.getSeqWithArt(entity, this);
        double range = 40 * multiplier(entity);

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), range)
                .stream()
                .filter(t -> isThreat(entity, t, mySeq))
                .forEach(threat -> {
                    UUID tid = threat.getUUID();

                    Vec3 dir = threat.position().subtract(entity.position()).normalize();
                    double angle = Math.toDegrees(Math.atan2(-dir.x, dir.z));
                    String compass = angleToCompass(angle, entity.getYRot());

                    if (!known.contains(tid)) {
                        known.add(tid);

                        AbilityUtil.sendActionBar(entity,
                                Component.translatable("ability.lotmcraft.danger_premonition.threat_detected",
                                                compass)
                                        .withColor(0xFF2020));

                        for (int i = 0; i < 5; i++) {
                            Vec3 pPos = entity.getEyePosition().add(dir.scale(1.0 + i * 0.4));
                            ParticleUtil.spawnParticles(serverLevel, MALICE_DUST, pPos, 2, 0.08, 0.0);
                        }
                        serverLevel.playSound(null,
                                entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value(),
                                entity.getSoundSource(), 0.6f, 0.4f);

                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,   20 * 6, 1, false, false, false));
                        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false));
                    } else {
                        if (random.nextInt(4) == 0) {
                            for (int i = 0; i < 3; i++) {
                                Vec3 pPos = entity.getEyePosition().add(dir.scale(1.0 + i * 0.5));
                                ParticleUtil.spawnParticles(serverLevel, SENSE_DUST, pPos, 1, 0.05, 0.0);
                            }
                        }
                    }
                });

        known.removeIf(tid -> {
            LivingEntity t = (LivingEntity) serverLevel.getEntity(tid);
            return t == null || !isThreat(entity, t, mySeq);
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        knownThreats.remove(entity.getUUID());
        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.danger_premonition.deactivated")
                        .withColor(0x4a0000));
    }

    private boolean consumeSpiritualityQuietly(LivingEntity entity, float amount) {
        if(BeyonderData.getSpirituality(entity) < amount) return false;
        BeyonderData.reduceSpirituality(entity, amount);
        return true;
    }

    private boolean isThreat(LivingEntity caster, LivingEntity candidate, int casterSeq) {
        if (!AbilityUtil.mayDamage(caster, candidate)) return false;
        int targetSeq = de.jakob.lotm.util.BeyonderData.getSequence(candidate);
        boolean seqDangerous = targetSeq <= casterSeq + 1;
        boolean activelyTargeting = candidate instanceof net.minecraft.world.entity.Mob mob
                && mob.getTarget() != null
                && mob.getTarget().getUUID().equals(caster.getUUID());
        return seqDangerous || activelyTargeting;
    }

    private String angleToCompass(double absoluteAngleDeg, float entityYaw) {
        double relative = (absoluteAngleDeg - entityYaw + 360) % 360;
        if (relative < 22.5 || relative >= 337.5) return "↑ N";
        if (relative < 67.5)  return "↗ NE";
        if (relative < 112.5) return "→ E";
        if (relative < 157.5) return "↘ SE";
        if (relative < 202.5) return "↓ S";
        if (relative < 247.5) return "↙ SW";
        if (relative < 292.5) return "← W";
        return "↖ NW";
    }
}