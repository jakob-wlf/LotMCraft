package de.jakob.lotm.fluid;

import de.jakob.lotm.LOTMCraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluidTypes {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, LOTMCraft.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> DROPS_OF_ETERNAL_DARKNESS_TYPE = FLUID_TYPES.register(
            "drops_of_eternal_darkness",
            () -> new FluidType(FluidType.Properties.create()
                    .canSwim(true)
                    .canDrown(true)
                    .supportsBoating(true)
                    .density(1000)
                    .viscosity(1000)
                    .lightLevel(0))
    );

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
