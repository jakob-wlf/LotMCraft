package de.jakob.lotm.beyonders.abilities.darkness.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NocturnalityAbility extends PassiveAbilityItem {

    private final Set<MobEffect> modifiedEffectTypes = new HashSet<>();
    private int tick = 0;

    public NocturnalityAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 9));
    }

    private boolean isNocturnal(Level level, LivingEntity entity) {
        return level.isNight()
                || ConcealedDomainEntity.ENTITIES_INSIDE_DOMAIN.contains(entity.getUUID());
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (isNocturnal(level, entity)) {
            // Nocturnal bonuses apply here — fill in as needed
        }
    }
}
