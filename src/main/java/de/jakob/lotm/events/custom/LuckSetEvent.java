package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public class LuckSetEvent extends Event implements ICancellableEvent {
    private @Nullable LivingEntity sourceEntity;
    private LivingEntity targetEntity;
    private int luck;
    private boolean isLuckIncreased;


    public LuckSetEvent(@Nullable LivingEntity sourceEntity, LivingEntity targetEntity, int luck, boolean isLuckPositive) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.luck = luck;
        this.isLuckIncreased = isLuckPositive;
    }

    public @Nullable LivingEntity getSourceEntity() { return sourceEntity; }
    public LivingEntity getTargetEntity() { return targetEntity; }
    public int getLuck() { return luck; }
    public boolean isLuckIncreased() { return isLuckIncreased; }

    public void setSourceEntity(@Nullable LivingEntity sourceEntity) { this.sourceEntity = sourceEntity; }
    public void setTargetEntity(LivingEntity targetEntity) { this.targetEntity = targetEntity; }
    public void setLuck(int luck) { this.luck = luck; }
    public void setLuckIncreased(boolean isLuckPositive) { this.isLuckIncreased = isLuckPositive; }
}