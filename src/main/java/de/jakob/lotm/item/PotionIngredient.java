package de.jakob.lotm.item;

import net.minecraft.world.item.Item;

public class PotionIngredient extends Item {
    private final int sequence;
    private final boolean isMainIngredient;

    public PotionIngredient(Properties properties, int sequence, boolean isMainIngredient) {
        super(properties);
        this.sequence = sequence;
        this.isMainIngredient = isMainIngredient;
    }

    public int getSequence() {
        return sequence;
    }

    public boolean isMainIngredient() {
        return isMainIngredient;
    }
}
