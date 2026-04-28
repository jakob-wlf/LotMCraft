package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25 *(int) Math.max(multiplier(entity)/4,1), 1.4f);
        if (target == null) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.verdict_exile.no_target").withStyle(ChatFormatting.RED));
            }
            return;
        }
        Vec3 startPos = entity.position();
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 3, 1);
        Vec3 awayDir = target.position().subtract(entity.position()).multiply(1, 0, 1).normalize();


        if (awayDir.lengthSqr() == 0) {
            awayDir = entity.getLookAngle().multiply(1, 0, 1).normalize();
        }

        double tpUpDistance = 15.0 * (int) Math.max(multiplier(entity)/2,1) ;
        double tpAwayDistance = 10.0 * (int) Math.max(multiplier(entity)/2,1) ;

        target.teleportTo(
                target.getX() + (awayDir.x * tpAwayDistance),
                target.getY() + tpUpDistance,
                target.getZ() + (awayDir.z * tpAwayDistance)
        );

        double pushHorizontal = 2.0 * (int) Math.max(multiplier(entity)/2,1) ;
        double pushVertical = 1.5 *(int) Math.max(multiplier(entity)/2,1) ;

        target.setDeltaMovement(awayDir.x * pushHorizontal, pushVertical, awayDir.z * pushHorizontal);
        target.hurtMarked = true;

        target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200*(int) Math.max(multiplier(entity)/4,1), 0, false, false));

        Vec3 look = entity.getLookAngle();
        Vec3 origin = entity.getEyePosition();

        for (double radius = 0.5; radius <= 4.0; radius += 0.5) {
            for (double angle = 0; angle < Math.PI * 2; angle += 0.3) {
                // Build perpendicular basis from look direction
                Vec3 perp1 = new Vec3(-look.z, 0, look.x).normalize();
                Vec3 perp2 = look.cross(perp1).normalize();

                Vec3 offset = perp1.scale(Mth.cos((float) angle) * radius)
                        .add(perp2.scale(Mth.sin((float) angle) * radius));
                Vec3 particlePos = origin.add(look.scale(radius)).add(offset);

                if (angle % 0.6 < 0.31) {
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.GOLDEN_NOTE.get(),
                            particlePos, 1, 0.05, 0.1);
                } else {
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.HOLY_FLAME.get(),
                            particlePos, 1, 0.05, 0.1);
                }
            }
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 25, 20 * 2));
    }
}