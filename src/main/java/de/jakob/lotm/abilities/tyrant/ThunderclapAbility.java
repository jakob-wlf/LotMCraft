package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThunderclapAbility extends AbilityItem {
    public ThunderclapAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 50, 2);
        Vec3 entityLoc = entity.position();
        Vec3 dir = targetLoc.subtract(entityLoc.add(0, 1, 0)).normalize();
        entity.teleportTo(entityLoc.x, entityLoc.y + 1, entityLoc.z);
        ServerScheduler.scheduleDelayed(1, () -> {
            entity.setDeltaMovement(new Vec3(dir.x, dir.y * .1, dir.z).scale(7));
            entity.hurtMarked = true;
        });

        AtomicBoolean hasLanded = new AtomicBoolean(false);

        ServerScheduler.scheduleForDuration(0, 0, 15, () -> {
            if(hasLanded.get())
                return;

            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EXPLOSION, entity.position(), 4, .5, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.LIGHTNING.get(), entity.position(), 80, 1, 0.1);
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2, 1);

            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 8, 25, entity.position(), true, false);

            entity.setDeltaMovement(new Vec3(dir.x, dir.y * .1, dir.z).scale(7));
            entity.hurtMarked = true;
            entity.fallDistance = 0;
            if(entity.position().distanceTo(targetLoc) < 3) {
                hasLanded.set(true);
                entity.setDeltaMovement(0, 0, 0);
                entity.hurtMarked = true;
            }
        }, () -> {
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
        }, (ServerLevel) level);
    }
}
