package de.jakob.lotm.item;

import net.minecraft.world.item.Item;

public class PotionIngredient extends Item {
    private final int sequence;
    private final String[] pathways;
    private final boolean isMainIngredient;

    public PotionIngredient(Properties properties, int sequence, boolean isMainIngredient, String... pathways) {
        super(properties);
        this.sequence = sequence;
        this.isMainIngredient = isMainIngredient;
        this.pathways = pathways;
    }

    public int getSequence() {
        return sequence;
    }

    public String[] getPathways() {
        return pathways;
    }

    public boolean isMainIngredient() {
        return isMainIngredient;
    }
}
