package de.jakob.lotm.acting;

public class EventActingTask implements ActingTask {
    private final String id;
    private final float baseAmount;
    
    public EventActingTask(String id, float baseAmount) {
        this.id = id;
        this.baseAmount = baseAmount;
    }
    
    @Override public String getId() { return id; }
    @Override public float getBaseAmount() { return baseAmount; }
}