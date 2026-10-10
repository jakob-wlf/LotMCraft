package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HolyEyeAbility extends ToggleAbility {
    public HolyEyeAbility(String id) {
        super(id, "light_source", "light_strong", "light_weak", "purification");
        postsUsedAbilityEventManually = true;
        tickRate = 1;

        baseDamage = 1f;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(100f, 75f, 50f, 35f, 30f));
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

        EffectManager.playDirectionalEffect(EffectIds.HOLY_BEAM,
                startPos.x, startPos.y, startPos.z,
                targetPos.x, targetPos.y, targetPos.z,
                2, (ServerLevel) level, entity);

        EffectManager.playEffect(EffectIds.HOLY_IMPACT, targetPos.x, targetPos.y, targetPos.z, (ServerLevel) level, entity);

        if(target != null && AbilityUtil.isUndeadOrEvil(target)) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.LIGHT, entity),  baseDamage);
        }

    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }
}
