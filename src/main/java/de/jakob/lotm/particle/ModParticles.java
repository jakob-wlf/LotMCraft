package de.jakob.lotm.particle;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, LOTMCraft.MOD_ID);

    public static final Supplier<SimpleParticleType> HOLY_FLAME = PARTICLE_TYPES.register("holy_flame_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> DARKER_FLAME = PARTICLE_TYPES.register("darker_flame_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> GREEN_FLAME = PARTICLE_TYPES.register("green_flame_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> BLACK_FLAME = PARTICLE_TYPES.register("black_flame_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> TOXIC_SMOKE = PARTICLE_TYPES.register("toxic_smoke_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> HEALING = PARTICLE_TYPES.register("healing_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> CRIMSON_LEAF = PARTICLE_TYPES.register("crimson_leaf_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> BLACK_NOTE = PARTICLE_TYPES.register("black_note_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> GOLDEN_NOTE = PARTICLE_TYPES.register("golden_note_particles", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> DISEASE = PARTICLE_TYPES.register("disease_particles", () -> new SimpleParticleType(true));
    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
