package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class UndergroundTravelAbility extends Ability {
    public UndergroundTravelAbility(String id) {
        super(id, 3f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 80;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        BlockParticleOption dirt = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        Vec3 origin = entity.position();

        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1f, .7f);
        ParticleUtil.spawnCircleParticles((ServerLevel) level, dirt, origin, 1.2, 30);
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0, false, false, false));

        Vec3 destination = AbilityUtil.getTargetLocation(entity, 20, 1.5f, true);
        BlockPos ground = BlockPos.containing(destination);
        for(int i = 0; i < 20; i++) {
            BlockState state = level.getBlockState(ground.below());
            if(!state.getCollisionShape(level, ground.below()).isEmpty()) break;
            ground = ground.below();
        }

        BlockPos finalGround = ground;
        ServerScheduler.scheduleDelayed(12, () -> {
            Vec3 emergePos = finalGround.getCenter();
            entity.teleportTo(emergePos.x, emergePos.y, emergePos.z);
            entity.removeEffect(MobEffects.INVISIBILITY);

            level.playSound(null, emergePos.x, emergePos.y, emergePos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1f, 1.3f);
            ParticleUtil.spawnCircleParticles((ServerLevel) level, dirt, emergePos, 2.2, 50);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CRIT, emergePos.add(0, .5, 0), 20, .8, .4, .8, .05);

            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(5, .7) * multiplier(entity), emergePos, true, true,
                    ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity));

            for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, emergePos, 2.5)) {
                e.setDeltaMovement(e.getDeltaMovement().add(0, .6, 0));
                e.hurtMarked = true;
            }
        }, (ServerLevel) level);
    }
}