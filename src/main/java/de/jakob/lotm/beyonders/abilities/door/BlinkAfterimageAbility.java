package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

public class BlinkAfterImageAbility extends ToggleAbility {
    public BlinkAfterImageAbility(String id) {
        super(id);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    @Override
    public void stop(Level level, LivingEntity entity) {

    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("door", 4);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
}
