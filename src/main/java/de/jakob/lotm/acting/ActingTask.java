package de.jakob.lotm.acting;

public interface ActingTask {
    String getId();
    float getBaseAmount();
    
    default float getScaledAmount(int sequence) {
        float multiplier = (sequence / 9.0f) * .1f;
        return getBaseAmount() * multiplier;
    }
}