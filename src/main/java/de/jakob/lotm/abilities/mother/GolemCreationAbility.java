package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class GolemCreationAbility extends AbilityItem {
    public GolemCreationAbility(Properties properties) {
        super(properties, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        IronGolem golem = new IronGolem(EntityType.IRON_GOLEM, serverLevel);
        golem.setPos(entity.getX(), entity.getY(), entity.getZ());
        serverLevel.addFreshEntity(golem);
        AttributeInstance maxHealth = golem.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(200.0);
        }

        AttributeInstance attackDamage = golem.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(40.0);
        }

        golem.setHealth(200.0F);
        AllyUtil.makeAllies(entity, golem, false);
    }
}
