package de.jakob.lotm.abilities.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class AbilityUsedEvent extends Event implements ICancellableEvent {

    private final ServerLevel level;
    private final Vec3 position;
    private LivingEntity entity;
    private final Ability ability;
    private final ArrayList<String> interactionFlags;
    private final double interactionRadius;
    private final int interactionCacheTime;

    public AbilityUsedEvent(ServerLevel serverLevel, Vec3 position, LivingEntity entity, Ability ability, String[] interactionFlags, double interactionRadius, int interactionCacheTime) {
        this.level = serverLevel;
        this.position = position;
        this.entity = entity;
        this.ability = ability;
        this.interactionFlags = new ArrayList<>(Arrays.asList(interactionFlags));
        this.interactionRadius = interactionRadius;
        this.interactionCacheTime = interactionCacheTime;
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

    public ServerLevel getLevel() {
        return level;
    }

    public ArrayList<String> getInteractionFlags() {
        return interactionFlags;
    }

    public double getInteractionRadius() {
        return interactionRadius;
    }

    public Vec3 getPosition() {
        return position;
    }

    public int getInteractionCacheTime() {
        return interactionCacheTime;
    }
}
