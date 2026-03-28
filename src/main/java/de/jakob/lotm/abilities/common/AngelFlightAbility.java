package de.jakob.lotm.abilities.common;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledFlightComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AngelFlightAbility extends ToggleAbility {
    public AngelFlightAbility(String id) {
        super(id);
        this.canBeUsedByNPC = false;

        this.shouldBeHidden = true;
        this.canBeCopied = false;
        this.cannotBeStolen = true;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // If disabled by cooldown, stop
        DisabledFlightComponent disabledFlightComponent = entity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            cancel((ServerLevel) level, entity);
            return;
        }

        // Allow Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(.05f);
            player.onUpdateAbilities();
        }

        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (transformationComponent.isTransformed() && transformationComponent.getTransformationIndex()
                != TransformationComponent.TransformationType.MYTHICAL_CREATURE.getIndex()) {
            cancel((ServerLevel) level, entity);
            return;
        }
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
    public void stop(Level level, LivingEntity entity) {
        if(entity instanceof Player player) {
            if(!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            player.getAbilities().setFlyingSpeed(.05f);
            player.onUpdateAbilities();
        }

        ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
            entity.resetFallDistance();
            entity.fallDistance = 0;
        });
    }


    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap();

        for(String pathway : BeyonderData.pathways) {
            reqs.put(pathway, 2);
        }
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 1;
    }
}
