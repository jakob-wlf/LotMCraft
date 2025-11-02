package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.JusticeSwordEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SwordOfJusticeAbility extends AbilityItem {
    public SwordOfJusticeAbility(Properties properties) {
        super(properties, 2.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 20, 2).add(0, 15, 0);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(targetEntity != null) {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
        }
        JusticeSwordEntity swordEntity = new JusticeSwordEntity(level, targetLoc, (float) (60.0f * multiplier(entity)), entity);
        level.addFreshEntity(swordEntity);
    }
}
