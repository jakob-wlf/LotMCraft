package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class StartAdvanceSequencePathwayEvent extends Event implements ICancellableEvent {

    private final LivingEntity entity;
    private int sequence;
    private String pathway;
    private double failureChance;
    private int duration;

    public StartAdvanceSequencePathwayEvent(LivingEntity entity, int sequence, String pathway, double failureChance, int duration) {
        this.entity = entity;
        this.sequence = sequence;
        this.pathway = pathway;
        this.failureChance = failureChance;
        this.duration = duration;

    }

    public LivingEntity getEntity() {
        return entity;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    public double getFailureChance() {
        return failureChance;
    }

    public void setFailureChance(float failureChance) {
        this.failureChance = failureChance;
    }

    public void setFailureChance(double failureChance) {
        this.failureChance = failureChance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}