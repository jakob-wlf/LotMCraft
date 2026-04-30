package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PaleEyeAbility extends ToggleAbility {

    private final DustParticleOptions paleDust = new DustParticleOptions(new Vector3f(0.9f, 0.9f, 0.9f), 1f);

    public PaleEyeAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        ParticleUtil.spawnSphereParticles((ServerLevel) level, paleDust, entity.getEyePosition(), .35, 20);

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 25, 1.5f);
        if(targetEntity == null) return;

        int casterSequence = AbilityUtil.getSeqWithArt(entity, this);
        int targetSequence = BeyonderData.getSequence(targetEntity);

        ParticleUtil.spawnParticles((ServerLevel) level, paleDust, targetEntity.getEyePosition(), 30, 0.5, 0.5, 0.5, 0.1);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SOUL, targetEntity.getEyePosition(), 30, 0.5, 0.5, 0.5, 0.05);
        ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.SOUL_FIRE_FLAME, targetEntity.position(), 1.5, 60);

        if(casterSequence + 1 < targetSequence) {
            level.playSound(null, targetEntity.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.5f, 1.2f);
            ParticleUtil.spawnParticles((ServerLevel) level, paleDust, targetEntity.getEyePosition(), 100, 0.5, 0.5, 0.5, 0.1);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SOUL, targetEntity.getEyePosition(), 100, 0.5, 0.5, 0.5, 0.05);
            ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.SOUL_FIRE_FLAME, targetEntity.position(), 1.1, 90);
            targetEntity.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC), Float.MAX_VALUE);
            return;
        }

        targetEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 3, 1, false, false, false));
        targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3, 2, false, false, false));

        targetEntity.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) (DamageLookup.lookupDps(3, .9, 5, 35) * multiplier(entity)));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        level.playSound(null, entity.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}
