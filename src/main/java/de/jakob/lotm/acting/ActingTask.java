package de.jakob.lotm.acting;

public interface ActingTask {
    String getId();
    float getBaseAmount();
    long getCooldownTicks();

    default float getScaledAmount(int sequence) {
        return getBaseAmount() * sequence;
    }
}