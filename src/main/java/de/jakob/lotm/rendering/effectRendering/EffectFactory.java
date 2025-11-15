package de.jakob.lotm.rendering.effectRendering;

public class EffectFactory {
    
    public static ActiveEffect createEffect(int effectIndex, double x, double y, double z) {
        return switch (effectIndex) {
            case 0 -> new ThunderExplosionEffect(x, y, z);
            case 1 -> new HolyLightEffect(x, y, z);
            case 2 -> new ConqueringEffect(x, y, z);
            case 3 -> new InfernoEffect(x, y, z);
            case 4 -> new FlameVortexEffect(x, y, z);
            case 5 -> new ExplosionEffect(x, y, z);
            case 6 -> new CollapseEffect(x, y, z);
            case 7 -> new ApocalypseEffect(x, y, z);
            case 8 -> new SpaceFragmentationEffect(x, y, z);
            case 9 -> new WaypointEffect(x, y, z);
            case 10 -> new SpaceDistortionEffect(x, y, z);
            default -> throw new IllegalArgumentException("Unknown effect index: " + effectIndex);
        };
    }
}