package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Event fired when a target entity is being selected.
 * This event allows intercepting and modifying the target entity.
 * This event is NOT cancellable.
 */
public class TargetEntityEvent extends Event {
    
    private final LivingEntity sourceEntity;
    private final int radius;
    private final float entityDetectionRadius;
    private final boolean onlyAllowWithLineOfSight;
    private final boolean allowAllies;
    private LivingEntity targetEntity;

    public TargetEntityEvent(LivingEntity sourceEntity,
                            int radius,
                            float entityDetectionRadius,
                            boolean onlyAllowWithLineOfSight,
                            boolean allowAllies,
                            @Nullable LivingEntity targetEntity) {
        this.sourceEntity = sourceEntity;
        this.radius = radius;
        this.entityDetectionRadius = entityDetectionRadius;
        this.onlyAllowWithLineOfSight = onlyAllowWithLineOfSight;
        this.allowAllies = allowAllies;
        this.targetEntity = targetEntity;
    }

    /**
     * @return The entity that is looking for a target entity
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
     * @return Whether only entities with line of sight are allowed
     */
    public boolean isOnlyAllowWithLineOfSight() {
        return onlyAllowWithLineOfSight;
    }

    /**
     * @return Whether allies can be targeted (for support abilities)
     */
    public boolean isAllowAllies() {
        return allowAllies;
    }

    /**
     * @return The current target entity (may be null)
     */
    @Nullable
    public LivingEntity getTargetEntity() {
        return targetEntity;
    }

    /**
     * Sets a new target entity. This is the only mutable property of this event.
     * @param targetEntity The new target entity (can be null)
     */
    public void setTargetEntity(@Nullable LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

}