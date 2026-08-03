package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.TsunamiEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TsunamiAbility extends Ability {
    public TsunamiAbility(String id) {
        super(id, 12, "water", "water_strong");

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(8500f, 3400f, 2000f, 1300f, 1000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(8, 10, 12, 14, 16));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 position = VectorUtil.getRelativePosition(entity.position(), entity.getLookAngle().normalize(), 10, random.nextDouble(-11, 11), 0);
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 40, 2);
        Vec3 direction = targetPos.subtract(position).normalize();

        level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_SPLASH, entity.getSoundSource(), 5, 1.0f);

        float damage = (float) (DamageLookup.lookupDamage(4, .1)* multiplier(entity));
        TsunamiEntity tsunami = new TsunamiEntity(level, position, direction, damage, BeyonderData.isGriefingEnabled(entity), entity);
        level.addFreshEntity(tsunami);
    }
}
