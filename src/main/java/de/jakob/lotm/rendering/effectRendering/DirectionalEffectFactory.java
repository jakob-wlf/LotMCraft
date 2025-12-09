package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.FateSiphoningEffect;

public class DirectionalEffectFactory {
    
    public static ActiveDirectionalEffect createEffect(int effectIndex, 
                                                      double startX, double startY, double startZ,
                                                      double endX, double endY, double endZ,
                                                      int duration) {
        return switch (effectIndex) {
            case 0 -> new FateSiphoningEffect(startX, startY, startZ, endX, endY, endZ, duration);
            default -> throw new IllegalArgumentException("Unknown directional effect index: " + effectIndex);
        };
    }
}