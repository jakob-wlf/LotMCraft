package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.DirectionalEffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class HolyEyeAbility extends ToggleAbility {
    public HolyEyeAbility(String id) {
        super(id, "light_source", "light_strong", "light_weak", "purification");
        postsUsedAbilityEventManually = true;
        tickRate = 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2);
        Vec3 targetPos = target != null ? target.getEyePosition() : AbilityUtil.getTargetLocation(entity, 25, 2);

        Vec3 startPos = entity.getEyePosition().add(entity.getLookAngle().normalize().scale(1.5));

        DirectionalEffectManager.playEffect(DirectionalEffectManager.DirectionalEffect.HOLY_BEAM,
                startPos.x, startPos.y, startPos.z,
                targetPos.x, targetPos.y, targetPos.z,
                2, (ServerLevel) level, entity);

        EffectManager.playEffect(EffectManager.Effect.HOLY_IMPACT, targetPos.x, targetPos.y, targetPos.z, (ServerLevel) level, entity);

        if(target != null) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, entity),  (float) DamageLookup.lookupDps(4, 1, 7, 20) * multiplier(entity));
        }

    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}
