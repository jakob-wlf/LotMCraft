package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Event fired when a target location is being selected.
 * This event allows intercepting and modifying the target location.
 * This event is NOT cancellable.
 */
public class TargetLocationEvent extends Event {
    
    private final LivingEntity sourceEntity;
    private final int radius;
    private final float entityDetectionRadius;
    private final boolean positionAtEntityFeet;
    private final boolean allowAllies;
    private Vec3 targetLocation;

    public TargetLocationEvent(LivingEntity sourceEntity, 
                               int radius, 
                               float entityDetectionRadius, 
                               boolean positionAtEntityFeet, 
                               boolean allowAllies,
                               Vec3 targetLocation) {
        this.sourceEntity = sourceEntity;
        this.radius = radius;
        this.entityDetectionRadius = entityDetectionRadius;
        this.positionAtEntityFeet = positionAtEntityFeet;
        this.allowAllies = allowAllies;
        this.targetLocation = targetLocation;
    }

    /**
     * @return The entity that is looking for a target location
     */
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }

    /**
     * @return The maximum search radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * @return The detection radius for entities along the ray
     */
    public float getEntityDetectionRadius() {
        return entityDetectionRadius;
    }

    /**
     * @return Whether the position should be at entity feet (true) or eye position (false)
     */
    public boolean isPositionAtEntityFeet() {
        return positionAtEntityFeet;
    }

    /**
     * @return Whether allies can be targeted (for support abilities)
     */
    public boolean isAllowAllies() {
        return allowAllies;
    }

    /**
     * @return The current target location
     */
    public Vec3 getTargetLocation() {
        return targetLocation;
    }

    /**
     * Sets a new target location. This is the only mutable property of this event.
     * @param targetLocation The new target location
     */
    public void setTargetLocation(Vec3 targetLocation) {
        this.targetLocation = targetLocation;
    }
}