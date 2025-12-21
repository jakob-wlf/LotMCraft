package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class NightDomainAbility extends AbilityItem {
    public NightDomainAbility(Properties properties) {
        super(properties, 30);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 5);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 startPos = entity.position();

        EffectManager.playEffect(EffectManager.Effect.NIGHT_DOMAIN, entity.position().x, entity.position().y, entity.position().z, serverLevel);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
            ParticleUtil.spawnParticles(serverLevel, dust, startPos, 80, 35, .25, 35, 0);
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35, startPos, new MobEffectInstance(MobEffects.BLINDNESS, 20, 20, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35, startPos, new MobEffectInstance(MobEffects.DARKNESS, 20, 20, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35, startPos, new MobEffectInstance(ModEffects.UNLUCK, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35, startPos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 5, false, false, false));

            AbilityUtil.damageNearbyEntities(serverLevel, entity, 35, DamageLookup.lookupDps(4, .85, 2, 20) * multiplier(entity), startPos, true, false);

            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 35).forEach(e -> BeyonderData.addModifierWithTimeLimit(e, "night_domain_debuff", .65, 2000));
            BeyonderData.addModifierWithTimeLimit(entity, "night_domain_buff", 1.35f, 2000);

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 2, 2, false, false, false));
        });
    }
}
