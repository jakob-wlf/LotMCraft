package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ElectromagneticTornadoEntity;
import de.jakob.lotm.entity.custom.TornadoEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ElectromagneticTornadoAbility extends AbilityItem {
    public ElectromagneticTornadoAbility(Properties properties) {
        super(properties, 2.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide()) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

        ElectromagneticTornadoEntity tornado = target == null ? new ElectromagneticTornadoEntity(ModEntities.ELECTROMAGNETIC_TORNADO.get(), level, .4f, (float) DamageLookup.lookupDamage(1, .8) * (float) multiplier(entity), entity) : new ElectromagneticTornadoEntity(ModEntities.ELECTROMAGNETIC_TORNADO.get(), level, .4f, 80 * (float) multiplier(entity), entity, target);
        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
