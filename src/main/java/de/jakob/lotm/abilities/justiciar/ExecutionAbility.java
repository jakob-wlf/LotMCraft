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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.HashMap;

public class ExecutionAbility extends Ability {

    public ExecutionAbility(String id) {
        super(id, 40f, "execution");
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
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20*(int) Math.max(multiplier(caster)/4,1), 1.5f);

        int targetSeq = BeyonderData.getSequence(target);
        int seq = BeyonderData.getSequence(caster);

        double failChance =0;
        if (targetSeq<seq) {
            failChance = 1;
        } else if (targetSeq == seq) {
            failChance = 0.7f;
        } else {
            failChance = 0.85f;
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

        playGuillotineAnimation(serverLevel, target,caster);
    }

    private static void playGuillotineAnimation(ServerLevel serverLevel, LivingEntity target,LivingEntity entity) {
        final double tx = target.getX();
        final double ty = target.getY();
        final double tz = target.getZ();

        // stun

        ServerScheduler.scheduleForDuration(0, 1, 40,
                () -> {
                    target.setOnGround(true);
                    var pos = target.position();
                    target.setDeltaMovement(new Vec3(0, 0, 0));
                    target.hurtMarked = true;

                    target.teleportTo(pos.x, pos.y, pos.z);
                });



        // Blade descends from Y+8 down to Y+0 over 40 ticks (0.2 per tick)
        AtomicDouble bladeY = new AtomicDouble(ty + 8.0);

        ServerScheduler.scheduleForDuration(0, 1, 40,
                () -> {
                    double by = bladeY.get();

                    // Two vertical LARGE_SMOKE pillars at X±1 (static frame)
                    for (double pillarY = ty; pillarY <= ty + 8.0; pillarY += 0.4) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, tx - 1, pillarY, tz, 1, 0, 0, 0, 0);
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, tx + 1, pillarY, tz, 1, 0, 0, 0, 0);
                    }

                    // Horizontal ASH blade bar
                    for (double bx = tx - 1.0; bx <= tx + 1.0; bx += 0.15) {
                        serverLevel.sendParticles(ParticleTypes.ASH, bx, by, tz, 1, 0, 0, 0, 0);
                    }

                    bladeY.addAndGet(-0.2);
                },
                () -> {
                    // TOTEM_OF_UNDYING burst at target position
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            target.getX(), target.getY() + 1.0, target.getZ(),
                            60, 0.5, 0.5, 0.5, 0.3);

                    // Kill bypassing revival
                    LawAbility.SOLACE_KILLED.add(target.getUUID());
                    if (target instanceof ServerPlayer) {
                        int targetSeq = BeyonderData.getSequence(target);
                        int seq = BeyonderData.getSequence(entity);
                        if (targetSeq<seq) {
                            target.hurt(serverLevel.damageSources().magic(), Float.MAX_VALUE);
                        }else{
                            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), 1);
                            target.setHealth(target.getHealth() - (target.getMaxHealth() * 0.7f));
                            target.hurtMarked = true;
                        };
                    } else {
                        target.kill();
                    }
                    ServerScheduler.scheduleDelayed(1, () -> LawAbility.SOLACE_KILLED.remove(target.getUUID()));
                },
                serverLevel
        );
    }
}
