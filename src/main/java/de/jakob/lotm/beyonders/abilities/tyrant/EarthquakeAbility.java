package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.*;

public class EarthquakeAbility extends Ability {
    private static final Earthquake EARTHQUAKE = new Earthquake();

    public EarthquakeAbility(String id) {
        super(id, 16, "explosion");
        interactionRadius = 70;
        interactionCacheTicks = 20 * 25;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(8500f, 3400f, 2000f, 1300f, 1000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(8, 10, 12, 14, 16));

        baseDamage = 2.5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 400;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        double multiplier = multiplier(entity);
        float damage = baseDamage;

        EARTHQUAKE.spawnCalamity((ServerLevel) level,
                startPos,
                griefing,
                (int) (65* multiplier), damage,
                entity, false);
        }
}
