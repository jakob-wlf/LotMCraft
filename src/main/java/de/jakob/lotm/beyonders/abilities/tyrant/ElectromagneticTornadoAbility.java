package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.ElectromagneticTornadoEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ElectromagneticTornadoAbility extends Ability {
    public ElectromagneticTornadoAbility(String id) {
        super(id, 20f, "explosion");
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(5, 10));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(10000f, 5000f));

        baseDamage = 15f;
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

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (12* multiplier(entity)), 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, (int) (12* multiplier(entity)), 2);

        float damage = baseDamage;

        ElectromagneticTornadoEntity tornado = new ElectromagneticTornadoEntity(
                ModEntities.ELECTROMAGNETIC_TORNADO.get(),
                level, .4f, damage , entity, target);

        tornado.setPos(pos);
        level.addFreshEntity(tornado);
    }
}
