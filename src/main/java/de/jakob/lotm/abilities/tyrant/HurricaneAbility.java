package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TornadoEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class HurricaneAbility extends AbilityItem {
    public HurricaneAbility(Properties properties) {
        super(properties, 2.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

        TornadoEntity tornado = new TornadoEntity(ModEntities.TORNADO.get(), level, 1.75f, 8f, entity);
        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
