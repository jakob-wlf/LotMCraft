package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HurricaneAbility extends Ability {
    public HurricaneAbility(String id) {
        super(id, 10f, "explosion");

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(9000f, 4000f, 2250f, 1500f, 1200f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(6, 7, 8, 9, 10));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

        float damage = (float) (DamageLookup.lookupDamage(4, .3)* multiplier(entity)/4);

        TornadoEntity tornado =
                new TornadoEntity(ModEntities.TORNADO.get(), level,
                        .15f, damage , entity, target, 1.5f);

        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
