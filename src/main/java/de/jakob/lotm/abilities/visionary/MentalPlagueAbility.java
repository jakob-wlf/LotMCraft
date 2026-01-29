package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MentalPlagueAbility extends Ability {
    public MentalPlagueAbility(String id) {
        super(id, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target").withColor(0xf5ca7f));
            return;
        }
        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.target_too_strong").withColor(0xf5ca7f));
            return;
        }

        target.addEffect(new MobEffectInstance(ModEffects.MENTAL_PLAGUE, 20 * 60 * 10, 4, false, false, false));
        ParticleUtil.spawnParticles((ServerLevel) target.level(), dust, target.getEyePosition(), 200, .4);
    }
}
