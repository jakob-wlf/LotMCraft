package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AvatarOfDesireAbility extends ToggleAbilityItem {
    public AvatarOfDesireAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {

    }

    @Override
    protected void stop(Level level, LivingEntity entity) {

    }
}
