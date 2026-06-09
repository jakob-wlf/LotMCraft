package de.jakob.lotm.acting;

public class EventActingTask implements ActingTask {
    private final String id;
    private final float baseAmount;
    private final long cooldownTicks;

    public EventActingTask(String id, float baseAmount) {
        this(id, baseAmount, 0);
    }

    public EventActingTask(String id, float baseAmount, long cooldownTicks) {
        this.id = id;
        this.baseAmount = baseAmount * 0.007f; // Scaled down, number chosen arbitrarily
        this.cooldownTicks = cooldownTicks;
    }

    @Override public String getId() { return id; }
    @Override public float getBaseAmount() { return baseAmount; }
    @Override public long getCooldownTicks() { return cooldownTicks; }
}