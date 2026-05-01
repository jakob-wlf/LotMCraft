package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

public class VerdictExileAbility extends Ability {

    public VerdictExileAbility(String id) {
        super(id, 12f, "exile");
        interactionRadius = 25;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25 * (int) Math.max(multiplier(entity) / 4, 1), 1.4f);
        if (target == null) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.verdict_exile.no_target").withStyle(ChatFormatting.RED));
            }
            return;
        }

        Vec3 awayDir = target.position().subtract(entity.position()).multiply(1, 0, 1).normalize();
        if (awayDir.lengthSqr() == 0) {
            awayDir = entity.getLookAngle().multiply(1, 0, 1).normalize();
        }

        double tpUpDistance = 15.0 * (int) Math.max(multiplier(entity) / 2, 1);
        double tpAwayDistance = 10.0 * (int) Math.max(multiplier(entity) / 2, 1);
        double pushHorizontal = 2.0 * (int) Math.max(multiplier(entity) / 2, 1);
        double pushVertical = 1.5 * (int) Math.max(multiplier(entity) / 2, 1);

        spawnCastEffect(serverLevel, entity, target);

        Vec3 finalAwayDir = awayDir;
        ServerScheduler.scheduleDelayed(8, () -> {
            Vec3 targetPosBefore = target.position();

            target.teleportTo(
                    target.getX() + (finalAwayDir.x * tpAwayDistance),
                    target.getY() + tpUpDistance,
                    target.getZ() + (finalAwayDir.z * tpAwayDistance)
            );
            target.setDeltaMovement(finalAwayDir.x * pushHorizontal, pushVertical, finalAwayDir.z * pushHorizontal);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200 * (int) Math.max(multiplier(entity) / 4, 1), 0, false, false));

            serverLevel.playSound(null, BlockPos.containing(target.position()), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 0.5f);
            serverLevel.playSound(null, BlockPos.containing(target.position()), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0f, 1.4f);

            spawnExileTrail(serverLevel, targetPosBefore, target.position());
            spawnAscendEffect(serverLevel, target);
        }, serverLevel);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 25, 20 * 2));
    }

    private void spawnCastEffect(ServerLevel level, LivingEntity caster, LivingEntity target) {
        Vec3 look = caster.getLookAngle();
        Vec3 origin = caster.getEyePosition();

        level.playSound(null, BlockPos.containing(origin), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0f, 1.1f);
        level.playSound(null, BlockPos.containing(origin), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 0.4f);

        Vec3 perp1 = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 perp2 = look.cross(perp1).normalize();

        for (double radius = 0.5; radius <= 4.0; radius += 0.5) {
            for (double angle = 0; angle < Math.PI * 2; angle += 0.3) {
                Vec3 offset = perp1.scale(Mth.cos((float) angle) * radius)
                        .add(perp2.scale(Mth.sin((float) angle) * radius));
                Vec3 particlePos = origin.add(look.scale(radius)).add(offset);

                if (angle % 0.6 < 0.31) {
                    ParticleUtil.spawnParticles(level, ModParticles.GOLDEN_NOTE.get(), particlePos, 1, 0.05, 0.1);
                } else {
                    ParticleUtil.spawnParticles(level, ModParticles.HOLY_FLAME.get(), particlePos, 1, 0.05, 0.1);
                }
            }
        }

        ParticleUtil.drawParticleLine(level, ModParticles.HOLY_FLAME.get(),
                origin, target.getEyePosition(), 0.3, 2);
        ParticleUtil.drawParticleLine(level, ModParticles.GOLDEN_NOTE.get(),
                origin, target.getEyePosition(), 0.5, 1);

        ParticleUtil.spawnSphereParticles(level, ModParticles.HOLY_FLAME.get(), target.position().add(0, 1, 0), 1.5, 20);
    }

    private void spawnExileTrail(ServerLevel level, Vec3 from, Vec3 to) {
        ParticleUtil.drawParticleLine(level, ModParticles.HOLY_FLAME.get(), from, to, 0.4, 2);
        ParticleUtil.drawParticleLine(level, ModParticles.GOLDEN_NOTE.get(), from, to, 0.7, 1);
        ParticleUtil.drawParticleLine(level, ParticleTypes.END_ROD, from, to, 0.5, 1);
    }

    private void spawnAscendEffect(ServerLevel level, LivingEntity target) {
        Location targetLoc = new Location(target.position(), level);

        ParticleUtil.createParticleSpirals(
                ModParticles.HOLY_FLAME.get(), targetLoc,
                0.3, 1.5, 6.0, 1.5, 1.8,
                60, 2, 5
        );

        ServerScheduler.scheduleForDuration(0, 4, 40, () -> {
            ParticleUtil.spawnCircleParticles(level, ModParticles.GOLDEN_NOTE.get(),
                    target.position().add(0, 0.5, 0), 1.2, 12);
            ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD,
                    target.position().add(0, 1, 0), 3, 0.5, 0.8);
        }, level);
    }
}