package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.ElectromagneticTornadoEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ElectromagneticTornadoAbility extends Ability {
    public ElectromagneticTornadoAbility(String id) {
        super(id, 20f, "explosion");
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 3000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12* (int) Math.max(multiplier(entity)/4,1), 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12* (int) Math.max(multiplier(entity)/4,1), 2);

        ElectromagneticTornadoEntity tornado = target == null ?
                new ElectromagneticTornadoEntity(ModEntities.ELECTROMAGNETIC_TORNADO.get(), level, .4f, (float) DamageLookup.lookupDamage(1, .6) * (int) Math.max(multiplier(entity)/4,1), entity) :
                new ElectromagneticTornadoEntity(ModEntities.ELECTROMAGNETIC_TORNADO.get(), level, .4f, (float) DamageLookup.lookupDamage(1, .6) * (int) Math.max(multiplier(entity)/4,1), target);
        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
