package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ConceptualizationAbility extends ToggleAbilityItem {
    public ConceptualizationAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 2;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        entity.setInvisible(true);
        entity.resetFallDistance();
        entity.fallDistance = 0;
        if(!entity.isShiftKeyDown())
            entity.setDeltaMovement(entity.getLookAngle().normalize().scale(2));
        else
            entity.setDeltaMovement(0, 0, 0);
        entity.hurtMarked = true;

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ENCHANT, entity.position(), 200, .8, .8, .8, .1);
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), entity.position(), 5, .4, .4, .4, .1);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        entity.setInvisible(false);
        ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
            entity.resetFallDistance();
            entity.fallDistance = 0;
        });
    }
}
