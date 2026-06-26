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
import java.util.Map;

public class HurricaneAbility extends Ability {
    public HurricaneAbility(String id) {
        super(id, 10f, "explosion");
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

        TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) (DamageLookup.lookupDamage(4, .65)* multiplier(entity)), entity) : new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) (DamageLookup.lookupDamage(4, .65)* multiplier(entity)), entity, target, 1.5f);
        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
