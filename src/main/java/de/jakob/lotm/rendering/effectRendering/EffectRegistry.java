package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.*;
import de.jakob.lotm.util.data.Location;

import java.util.HashMap;
import java.util.Map;

public class EffectRegistry {

    @FunctionalInterface
    public interface EffectSupplier {
        ActiveEffect create(Location location, int duration, boolean infinite);
    }

    private record EffectDefinition(EffectSupplier supplier, int defaultDuration, boolean defaultInfinite) {}

    private static final Map<Integer, EffectDefinition> REGISTRY = new HashMap<>();

    private EffectRegistry() {}

    public static void register(int id, EffectSupplier supplier, int defaultDuration) {
        register(id, supplier, defaultDuration, false);
    }

    public static void register(int id, EffectSupplier supplier, int defaultDuration, boolean defaultInfinite) {
        if (REGISTRY.putIfAbsent(id, new EffectDefinition(supplier, defaultDuration, defaultInfinite)) != null) {
            throw new IllegalStateException("Effect id already registered: " + id);
        }
    }

    public static ActiveEffect create(int id, Location location, EffectParams overrides) {
        EffectDefinition def = REGISTRY.get(id);
        if (def == null) throw new IllegalArgumentException("Unknown effect id: " + id);

        int duration = (overrides != null && overrides.duration() != null) ? overrides.duration() : def.defaultDuration();
        boolean infinite = (overrides != null && overrides.infinite() != null) ? overrides.infinite() : def.defaultInfinite();

        ActiveEffect effect = def.supplier().create(location, duration, infinite);

        if (overrides != null && overrides.params() != null) {
            effect.setParams(overrides.params());
        }
        return effect;
    }

    static {
        register(EffectIds.THUNDER_EXPLOSION, ThunderExplosionEffect::new, 60);
        register(EffectIds.PURE_WHITE_LIGHT, HolyLightEffect::new, 20 * 9);
        register(EffectIds.CONQUERING, ConqueringEffect::new, 70);
        register(EffectIds.INFERNO, InfernoEffect::new, 120);
        register(EffectIds.FLAME_VORTEX, FlameVortexEffect::new, 20 * 6);
        register(EffectIds.EXPLOSION, ExplosionEffect::new, 70);
        register(EffectIds.COLLAPSE, CollapseEffect::new, 70);
        register(EffectIds.APOCALYPSE, ApocalypseEffect::new, 140);
        register(EffectIds.SPACE_FRAGMENTATION, SpaceFragmentationEffect::new, 20 * 25);
        register(EffectIds.WAYPOINT, WaypointEffect::new, 8);
        register(EffectIds.SPACE_DISTORTION, SpaceDistortionEffect::new, 20 * 60);
        register(EffectIds.HOLY_LIGHT_SMALL, HolyLightSmallEffect::new, 70);
        register(EffectIds.LIGHT_OF_HOLINESS, LightOfHolinessEffect::new, 70);
        register(EffectIds.SEFIRAH_CASTLE_PARTICLES, SefirahCastleParticlesEffect::new, 20 * 3);
        register(EffectIds.SEFIRAH_CASTLE, SefirahCastleEffect::new, 20 * 4);
        register(EffectIds.GIFTING_PARTICLES, GiftingParticlesEffect::new, 20 * 3);
        register(EffectIds.ABILITY_THEFT, AbilityTheftEffect::new, 8);
        register(EffectIds.CONCEPTUAL_THEFT, ConceptualTheftEffect::new, 30);
        register(EffectIds.DECEPTION, DeceptionEffect::new, 20 * 4);
        register(EffectIds.LOOPHOLE, LoopholeEffect::new, 20 * 14);
        register(EffectIds.MISFORTUNE_FIELD, MisfortuneFieldEffect::new, 20 * 4);
        register(EffectIds.MISFORTUNE_CURSE, MisfortuneCurseEffect::new, 20 * 2);
        register(EffectIds.BLESSING, BlessingEffect::new, 20 * 2);
        register(EffectIds.NIGHT_DOMAIN, NightDomainEffect::new, 20 * 25);
        register(EffectIds.MIRACLE, MiracleEffect::new, 20 * 2);
        register(EffectIds.SPIRITUAL_BAPTISM, BaptismEffect::new, 20 * 5);
        register(EffectIds.CONCEALMENT, ConcealmentEffect::new, 20 * 5);
        register(EffectIds.ABYSS_PILLAR, AbyssPillarEffect::new, 20 * 7);
        register(EffectIds.ACID_SWAMP, AcidSwampEffect::new, 20 * 8);
        register(EffectIds.ARTIFACT_EXPLOSION, ArtifactExplosionEffect::new, 90);
        register(EffectIds.BLOOD_INFERNO, BloodInfernoEffect::new, 120);
        register(EffectIds.FOOLING, FoolingEffect::new, 40);
        register(EffectIds.ROTATING_RINGS, RotatingRingsEffect::new, 160);
        register(EffectIds.SPACE_TEARING, SpaceTearingEffect::new, 140);
        register(EffectIds.DISCERNMENT, DiscernEffect::new, 20 * 2);
        register(EffectIds.PROHIBITION, ProhibitionEffect::new, 160);
        register(EffectIds.IMPRISON, ImprisonEffect::new, 120);
        register(EffectIds.ANCIENT_COURT, AncientCourtEffect::new, 120);
        register(EffectIds.NATION_OF_THE_DEAD, NationOfTheDeadEffect::new, 20 * 106);
        register(EffectIds.HOLY_IMPACT, HolyImpactEffect::new, 20);
        register(EffectIds.UNIQUENESS_SPAWN, UniquenessSpawnEffect::new, 20 * 7);
        register(EffectIds.TELEPORTATION, TeleportationEffect::new, 25);
        register(EffectIds.BANISHMENT, BanishEffect::new, 30);
        register(EffectIds.BLOOD_SURGE, BloodSurgeEffect::new, 20 * 8);
        register(EffectIds.HISTORICAL_VOID_SUMMONING, HistoricalVoidSummonEffect::new, 30);
        register(EffectIds.FATE_SIPHONING, FateSiphoningEffect::new, 40);
        register(EffectIds.HOLY_BEAM, HolyBeamEffect::new, 40);
        register(EffectIds.HORROR_AURA, HorrorAuraEffect::new, 40, true);
        register(EffectIds.LIFE_AURA, LifeAuraEffect::new, 40, true);
        register(EffectIds.FEAR_AURA, FearAuraEffect::new, 40, true);
        register(EffectIds.BEAMS_OF_LIGHT, BeamsOfLightEffect::new, 40, false);
        register(EffectIds.SPACE_TEAR, SpaceTearEffect::new, 40, true);
        register(EffectIds.DEATH_DECREE_RING, DeathDecreeRingEffect::new, 20, true);
    }
}