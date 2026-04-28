package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledFlightComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class FlightAbility extends ToggleAbility {
    public FlightAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return 60;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 2));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        DisabledFlightComponent disabledFlightComponent = entity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            cancel((ServerLevel) level, entity);
            return;
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        DisabledFlightComponent disabledFlightComponent = entity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            cancel((ServerLevel) level, entity);
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
            if(!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            player.onUpdateAbilities();
        }
    }
}
