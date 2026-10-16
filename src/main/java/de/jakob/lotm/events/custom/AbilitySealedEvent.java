package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public class AbilitySealedEvent extends Event implements ICancellableEvent {
    private @Nullable LivingEntity sourceEntity;
    private LivingEntity targetEntity;
    private int duration;
    private String cause;
    private @Nullable String ability;

    public AbilitySealedEvent(@Nullable LivingEntity sourceEntity, LivingEntity targetEntity, int duration, String cause) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.duration = duration;
        this.cause = cause;
    }

    public AbilitySealedEvent(@Nullable LivingEntity sourceEntity, LivingEntity targetEntity, int duration, String cause, @Nullable String ability) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.duration = duration;
        this.cause = cause;
        this.ability = ability;
    }

    public boolean isSealingAllAbilities() {
        return this.ability == null;
    }

    public @Nullable LivingEntity getSourceEntity() { return sourceEntity; }
    public LivingEntity getTargetEntity() { return targetEntity; }
    public int getDuration() { return duration; }
    public String getCause() { return cause; }
    public @Nullable String getAbility() { return ability; }

    public void setSourceEntity(@Nullable LivingEntity sourceEntity) { this.sourceEntity = sourceEntity; }
    public void setTargetEntity(LivingEntity targetEntity) { this.targetEntity = targetEntity; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setCause(String cause) { this.cause = cause; }
    public void setAbility(@Nullable String ability) { this.ability = ability; }
}