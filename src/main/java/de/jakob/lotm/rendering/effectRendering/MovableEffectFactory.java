package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.HorrorAuraEffect;
import de.jakob.lotm.util.data.Location;

public class MovableEffectFactory {
    
    public static ActiveMovableEffect createEffect(int effectIndex, Location location,
                                                  int duration, boolean infinite) {
        return switch (effectIndex) {
            case 0 -> new HorrorAuraEffect(location, duration, infinite);
            default -> throw new IllegalArgumentException("Unknown movable effect index: " + effectIndex);
        };
    }
}