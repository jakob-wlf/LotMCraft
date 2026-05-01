package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import de.jakob.lotm.rendering.effectRendering.impl.*;
import net.minecraft.world.phys.Vec3;

public class EffectFactory {

    /**
     * Create an effect at a fixed position with no time scaling (normal speed).
     */
    public static ActiveEffect createEffect(int effectIndex, double x, double y, double z) {
        return createEffect(effectIndex, x, y, z, null);
    }

    /**
     * Create an effect that automatically adjusts its playback speed based on
     * the entity's position inside a {@code TimeChangeEntity} area.
     * <p>
     * The entity's position is re-sampled every client tick, so the effect
     * responds continuously as the entity moves in or out of the area:
     * <ul>
     *   <li>Multiplier > 1 → effect plays faster / finishes sooner</li>
     *   <li>Multiplier < 1 → effect plays slower / finishes later</li>
     * </ul>
     * Falls back to normal speed (1.0) if the client level is unavailable or
     * the entity is {@code null}.
     *
     * @param effectIndex Index into the effect registry
     * @param x           Spawn X coordinate
     * @param y           Spawn Y coordinate
     * @param z           Spawn Z coordinate
     * @param entity      Entity whose position is used for the time lookup.
     *                    Pass {@code null} to get the same behaviour as the
     *                    no-entity overload.
     * @return Fully configured {@link ActiveEffect}
     */
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
            default -> throw new IllegalArgumentException("Unknown effect index: " + effectIndex);
        };

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            // The lambda re-evaluates entity.position() every tick, so the
            // multiplier tracks the entity as it moves around the world.
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity,
                            new Location(new Vec3(x, y, z), level))
            );
        }

        return effect;
    }
}