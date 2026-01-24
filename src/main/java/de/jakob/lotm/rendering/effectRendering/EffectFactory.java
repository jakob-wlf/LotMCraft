package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.abilities.wheel_of_fortune.SpiritualBaptismAbility;
import de.jakob.lotm.rendering.effectRendering.ActiveEffect;
import de.jakob.lotm.rendering.effectRendering.impl.*;

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
            case 11 -> new HolyLightSmallEffect(x, y, z);
            case 12 -> new LightOfHolinessEffect(x, y, z);
            case 13 -> new SefirahCastleParticlesEffect(x, y, z);
            case 14 -> new SefirahCastleEffect(x, y, z);
            case 15 -> new GiftingParticlesEffect(x, y, z);
            case 16 -> new AbilityTheftEffect(x, y, z);
            case 17 -> new ConceptualTheftEffect(x, y, z);
            case 18 -> new DeceptionEffect(x, y, z);
            case 19 -> new LoopholeEffect(x, y, z);
            case 20 -> new MisfortuneFieldEffect(x, y, z);
            case 21 -> new MisfortuneCurseEffect(x, y, z);
            case 22 -> new BlessingEffect(x, y, z);
            case 23 -> new NightDomainEffect(x, y, z);
            case 24 -> new MiracleEffect(x, y, z);
            case 25 -> new BaptismEffect(x, y, z);
            case 26 -> new ConcealmentEffect(x, y, z);
            default -> throw new IllegalArgumentException("Unknown effect index: " + effectIndex);
        };
    }
}