package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FlightAbility extends ToggleAbility {
    public FlightAbility(String id) {
        super(id);
    }

    @Override
    protected float getSpiritualityCost() {
        return 12;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 2));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // Allow Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        // Disable Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}
