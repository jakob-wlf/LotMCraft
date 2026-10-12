package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DevilWorshipAbility extends Ability {
    public DevilWorshipAbility(String id) {
        super(id,20);
    }
    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        return;
    }

    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 7));}

    @Override
    protected float getSpiritualityCost() {
        return 5;
    }
}
