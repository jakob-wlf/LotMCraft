package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FogOfWarAbility extends ToggleAbilityItem {
    public FogOfWarAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.FOG_OF_WAR.get(), entity.getEyePosition(), 20, 15, 5, 15, 0);
        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 20).forEach(e -> {
            e.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0, false, false, true));
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, true));
            BeyonderData.addModifier(e, "fog_of_war", .7);
            ServerScheduler.scheduleDelayed(20, () -> {
                if(e.distanceTo(entity) > 20) {
                    BeyonderData.removeModifier(e, "fog_of_war");
                }
            });
        });
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {

    }
}
