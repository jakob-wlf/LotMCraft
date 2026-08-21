package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import de.jakob.lotm.rendering.effectRendering.impl.*;
import net.minecraft.world.phys.Vec3;

public class EffectFactory {


    public static ActiveEffect createEffect(int effectIndex, double x, double y, double z) {
        return createEffect(effectIndex, x, y, z, null);
    }

    public static ActiveEffect createEffect(int effectIndex,
                                            double x, double y, double z,
                                            LivingEntity entity) {
        ActiveEffect effect = switch (effectIndex) {
            case 0  -> new ThunderExplosionEffect(x, y, z);
            case 1  -> new HolyLightEffect(x, y, z);
            case 2  -> new ConqueringEffect(x, y, z);
            case 3  -> new InfernoEffect(x, y, z);
            case 4  -> new FlameVortexEffect(x, y, z);
            case 5  -> new ExplosionEffect(x, y, z);
            case 6  -> new CollapseEffect(x, y, z);
            case 7  -> new ApocalypseEffect(x, y, z);
            case 8  -> new SpaceFragmentationEffect(x, y, z);
            case 9  -> new WaypointEffect(x, y, z);
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
            case 27 -> new AbyssPillarEffect(x, y, z);
            case 28 -> new AcidSwampEffect(x, y, z);
            case 29 -> new ArtifactExplosionEffect(x, y, z);
            case 30 -> new BloodInfernoEffect(x, y, z);
            case 31 -> new FoolingEffect(x, y, z);
            case 32 -> new RotatingRingsEffect(x, y, z);
            case 33 -> new SpaceTearingEffect(x, y, z);
            case 34 -> new DiscernEffect(x, y, z);
            case 35 -> new ProhibitionEffect(x, y, z);
            case 36 -> new ImprisonEffect(x, y, z);
            case 37 -> new AncientCourtEffect(x, y, z);
            case 38 -> new NationOfTheDeadEffect(x, y, z);
            case 39 -> new HolyImpactEffect(x, y, z);
            case 40 -> new UniquenessSpawnEffect(x, y, z);
            case 41 -> new TeleportationEffect(x, y, z);
            case 42 -> new BanishEffect(x, y, z);
            case 43 -> new BloodSurgeEffect(x, y, z);
            case 44 -> new HistoricalVoidSummonEffect(x, y, z);
            default -> throw new IllegalArgumentException("Unknown effect index: " + effectIndex);
        };

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity,
                            new Location(new Vec3(x, y, z), level))
            );
        }

        return effect;
    }
}