package de.jakob.lotm.rendering.effectRendering;

public class EffectFactory {
    
    public static ActiveEffect createEffect(int effectIndex, double x, double y, double z) {
        return switch (effectIndex) {
            case 0 -> new ThunderExplosionEffect(x, y, z);
            default -> throw new IllegalArgumentException("Unknown effect index: " + effectIndex);
        };
    }
}