package de.jakob.lotm.abilities.core;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class AbilityUseEvent extends Event implements ICancellableEvent {

    private LivingEntity entity;
    private final Ability ability;

    public AbilityUseEvent(LivingEntity entity, Ability ability) {
        this.entity = entity;
        this.ability = ability;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }
}
