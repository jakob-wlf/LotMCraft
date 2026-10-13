package de.jakob.lotm.util.data;

import java.util.Arrays;
import java.util.Comparator;

public enum PriestFlameVariants {
    DEFAULT(8),
    CRIMSON(7),
    WHITISH_RED(6),
    WHITE(5),
    WHITISH_BLUE(4),
    AZURE(3),
    AZURE_VIOLENT(2),
    VIOLENT(1),
    DARK(0);

    private final int id;
    private static final PriestFlameVariants[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(PriestFlameVariants::getId))
            .toArray(PriestFlameVariants[]::new);

    PriestFlameVariants(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static PriestFlameVariants byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)]; // Safe lookup against out of bounds
    }
}
