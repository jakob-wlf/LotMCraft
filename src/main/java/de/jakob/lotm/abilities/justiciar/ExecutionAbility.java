package de.jakob.lotm.abilities.justiciar;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.HashMap;

public class ExecutionAbility extends Ability {

    public ExecutionAbility(String id) {
        super(id, 30f, "execution");
        interactionRadius = 20;
        hasOptimalDistance = false;
        canBeShared = false;
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20 * (int) Math.max(multiplier(caster) / 4, 1), 1.5f);

        int targetSeq = BeyonderData.getSequence(target);
        int seq = BeyonderData.getSequence(caster);

        double failChance = 0;
        if (targetSeq < seq) {
            failChance = 1;
        } else if (targetSeq == seq) {
            failChance = 0.85f;
        } else if (Math.abs(targetSeq - seq) <= 2) {
            failChance = 0.7;
        } else {
            failChance = 0.25;
        }

        if (random.nextDouble() < failChance) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.execution.verdict_failed")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        if (target == null) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.execution.no_target")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        playGuillotineAnimation(serverLevel, target, caster);
    }

    private static void playGuillotineAnimation(ServerLevel serverLevel, LivingEntity target, LivingEntity caster) {
        final double tx = target.getX();
        final double ty = target.getY();
        final double tz = target.getZ();

        serverLevel.playSound(null, tx, ty, tz, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 0.5f);
        serverLevel.playSound(null, tx, ty, tz, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.4f, 1.8f);

        ServerScheduler.scheduleForDuration(0, 1, 40,
                () -> {
                    target.setOnGround(true);
                    Vec3 pos = target.position();
                    target.setDeltaMovement(Vec3.ZERO);
                    target.hurtMarked = true;
                    target.teleportTo(pos.x, pos.y, pos.z);
                });

        ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
            spawnGuillotineFrame(serverLevel, tx, ty, tz);
        });

        AtomicDouble bladeY = new AtomicDouble(ty + 8.0);
        AtomicDouble[] lastBladeY = { new AtomicDouble(ty + 8.0) };

        ServerScheduler.scheduleForDuration(0, 1, 40,
                () -> {
                    double by = bladeY.get();
                    spawnBlade(serverLevel, tx, by, tz);
                    lastBladeY[0].set(by);
                    bladeY.addAndGet(-0.2);

                    if (by < ty + 6.0 && by > ty + 1.0) {
                        serverLevel.playSound(null, tx, by, tz,
                                SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.3f, 0.6f + (float)((ty + 8.0 - by) / 8.0) * 0.8f);
                    }
                },
                () -> {
                    serverLevel.playSound(null, tx, ty, tz,
                            SoundEvents.IRON_GOLEM_DEATH, SoundSource.PLAYERS, 1.5f, 0.6f);
                    serverLevel.playSound(null, tx, ty, tz,
                            SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8f, 1.4f);
                    serverLevel.playSound(null, tx, ty, tz,
                            SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.5f, 1.6f);

                    spawnImpactBurst(serverLevel, tx, ty, tz);

                    LawAbility.SOLACE_KILLED.add(target.getUUID());
                    if (target instanceof ServerPlayer) {
                        int targetSeq = BeyonderData.getSequence(target);
                        int seq = BeyonderData.getSequence(caster);
                        if (targetSeq > seq) {
                            target.hurt(serverLevel.damageSources().magic(), Float.MAX_VALUE);
                        } else {
                            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster), 1);
                            target.setHealth(target.getHealth() - (target.getMaxHealth() * 0.7f));
                            target.hurtMarked = true;
                        }
                    } else {
                        target.kill();
                    }
                    ServerScheduler.scheduleDelayed(1, () -> LawAbility.SOLACE_KILLED.remove(target.getUUID()));
                },
                serverLevel
        );
    }

    private static void spawnGuillotineFrame(ServerLevel level, double tx, double ty, double tz) {
        for (double y = ty; y <= ty + 8.5; y += 0.35) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, tx - 1.0, y, tz, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, tx + 1.0, y, tz, 1, 0, 0, 0, 0);
        }

        for (double x = tx - 1.0; x <= tx + 1.0; x += 0.25) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, ty + 8.5, tz, 1, 0, 0, 0, 0);
        }

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tx - 1.0, ty, tz, 1, 0.05, 0.05, 0.05, 0.01);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tx + 1.0, ty, tz, 1, 0.05, 0.05, 0.05, 0.01);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tx - 1.0, ty + 8.5, tz, 1, 0.05, 0.05, 0.05, 0.01);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tx + 1.0, ty + 8.5, tz, 1, 0.05, 0.05, 0.05, 0.01);
    }

    private static void spawnBlade(ServerLevel level, double tx, double bladeY, double tz) {
        for (double x = tx - 1.0; x <= tx + 1.0; x += 0.1) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, x, bladeY, tz, 1, 0, 0, 0, 0.01);
        }

        for (double x = tx - 0.95; x <= tx + 0.95; x += 0.18) {
            level.sendParticles(ParticleTypes.END_ROD, x, bladeY - 0.08, tz, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.END_ROD, x, bladeY + 0.08, tz, 1, 0, 0, 0, 0);
        }

        for (double x = tx - 0.8; x <= tx + 0.8; x += 0.25) {
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, x, bladeY - 0.15, tz, 1, 0.02, 0.01, 0.02, 0.02);
        }

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, tx, bladeY, tz, 1, 0.8, 0, 0.8, 0);
    }

    private static void spawnImpactBurst(ServerLevel level, double tx, double ty, double tz) {
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, tx, ty + 1.0, tz, 80, 0.4, 0.6, 0.4, 0.4);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, tx, ty + 0.5, tz, 30, 0.6, 0.3, 0.6, 0.05);
        level.sendParticles(ParticleTypes.SOUL, tx, ty + 1.0, tz, 25, 0.5, 0.5, 0.5, 0.15);
        level.sendParticles(ParticleTypes.FLASH, tx, ty + 1.0, tz, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, tx, ty + 0.5, tz, 3, 0.3, 0.1, 0.3, 0);
        level.sendParticles(ParticleTypes.SNOWFLAKE, tx, ty + 1.5, tz, 40, 0.8, 0.5, 0.8, 0.2);
    }
}