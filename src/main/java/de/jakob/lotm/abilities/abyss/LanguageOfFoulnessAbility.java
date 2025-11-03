package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class LanguageOfFoulnessAbility extends SelectableAbilityItem {
    public LanguageOfFoulnessAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.language_of_foulness.slow", "ability.lotmcraft.language_of_foulness.corruption", "ability.lotmcraft.language_of_foulness.death"};
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(114 / 255f, 59 / 255f, 148 / 255f), 1.5f);

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 15, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.language_of_foulness.no_target").withColor(0x723b94));
            return;
        }

        ParticleUtil.spawnParticles(serverLevel, dust, target.getEyePosition(), 120, .5, .5, .5, 0.1);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, target.getEyePosition(), 120, .5, .5, .5, 0.1);

        switch (abilityIndex) {
            case 0 -> castSlow(serverLevel, entity, target);
            case 1 -> castCorruption(serverLevel, entity, target);
            case 2 -> castDeath(serverLevel, entity, target);
        }
    }

    private void castDeath(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            if(random.nextInt(8) == 0) {
                target.hurt(serverLevel.damageSources().wither(), (float) (3.5F * multiplier(entity)));
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 15, 8, false, false, false));
        });
    }

    private void castCorruption(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8, 1, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 8, 1, false, false, false));
        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, random.nextInt(3), false, false, false));
    }

    private void castSlow(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        ServerScheduler.scheduleForDuration(0, 1, 20 * 8, () -> {
            target.setDeltaMovement(0, 0, 0);
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 20, false, false, false));
        });
    }
}
