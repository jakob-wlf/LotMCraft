package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.HashMap;
import java.util.Map;

public class GolemCreationAbility extends Ability {
    public GolemCreationAbility(String id) {
        super(id, 10);
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        IronGolem golem = new IronGolem(EntityType.IRON_GOLEM, serverLevel);
        golem.setPos(entity.getX(), entity.getY(), entity.getZ());
        serverLevel.addFreshEntity(golem);
        AttributeInstance maxHealth = golem.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(200.0*multiplier(entity));
        }
        AttributeInstance attribute = golem.getAttribute(Attributes.SCALE);
        if (attribute != null) {
            attribute.setBaseValue(2*multiplier(entity));
        }
        AttributeInstance attackDamage = golem.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(80.0*(int)Math.max(multiplier(entity)/2,1));
        }

        golem.setHealth(200.0F*multiplier(entity));
        AllyUtil.makeAllies(entity, golem, false);
    }
}
