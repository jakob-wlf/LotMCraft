package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.JusticeSwordEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SwordOfJusticeAbility extends Ability {
    public SwordOfJusticeAbility(String id) {
        super(id, 6f, "purification", "purification_holy", "light_source", "light_strong", "light_weak");
        postsUsedAbilityEventManually = true;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 2, 3, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(5000f, 2500f, 1500f, 1000f));

        baseDamage = 15f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, baseDistance, 2).add(0, 15, 0);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, baseDistance, 2);
        if(targetEntity != null) {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 4, false, false, false));
        }

        JusticeSwordEntity swordEntity = new JusticeSwordEntity(level, targetLoc, baseDamage, entity, this);
        level.addFreshEntity(swordEntity);
    }
}
