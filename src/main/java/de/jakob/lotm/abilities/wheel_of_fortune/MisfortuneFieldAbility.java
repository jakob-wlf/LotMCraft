package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class MisfortuneFieldAbility extends AbilityItem {
    public MisfortuneFieldAbility(Properties properties) {
        super(properties, 30);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_FIELD, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        Vec3 startPos = entity.position();
        int amplifier = (int) Math.round(multiplier(entity) * 3f);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 20, startPos, new MobEffectInstance(ModEffects.UNLUCK, 40, amplifier));
        });
    }
}
