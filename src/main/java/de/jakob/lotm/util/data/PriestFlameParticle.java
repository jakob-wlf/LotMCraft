package de.jakob.lotm.util.data;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;

import java.util.Arrays;
import java.util.Comparator;

public enum PriestFlameParticle {
    DEFAULT(8, ParticleTypes.FLAME, ParticleTypes.SMALL_FLAME),
    CRIMSON(7, ParticleTypes.FLAME,  ParticleTypes.SMALL_FLAME),
    WHITISH_RED(6, ParticleTypes.DUST_PLUME,  ParticleTypes.SMALL_FLAME),
    WHITE(5,  ParticleTypes.DUST_PLUME, ParticleTypes.DUST_PLUME),
    WHITISH_BLUE(4, TrialSpawner.FlameParticle.OMINOUS.particleType, ParticleTypes.DUST_PLUME),
    AZURE(3, ParticleTypes.SOUL_FIRE_FLAME,  TrialSpawner.FlameParticle.OMINOUS.particleType),
    AZURE_VIOLENT(2, ParticleTypes.DRAGON_BREATH, TrialSpawner.FlameParticle.OMINOUS.particleType),
    VIOLENT(1,  ParticleTypes.DRAGON_BREATH, ParticleTypes.PORTAL),
    DARK(0, ParticleTypes.SMOKE, ParticleTypes.SMALL_FLAME);

    private final int id;
    private final SimpleParticleType type;
    private final SimpleParticleType subType;
    private static final PriestFlameParticle[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(PriestFlameParticle::getId))
            .toArray(PriestFlameParticle[]::new);

    PriestFlameParticle(int id, SimpleParticleType type, SimpleParticleType subType) {
        this.id = id;
        this.type = type;
        this.subType = subType;
    }

    public int getId() {
        return this.id;
    }
    public SimpleParticleType getType() {return this.type;}
    public SimpleParticleType getSubType() {return this.subType;}


    public static PriestFlameParticle byId(int id) {
        return BY_ID[Math.floorMod(id, BY_ID.length)]; // Safe lookup against out of bounds
    }
}
