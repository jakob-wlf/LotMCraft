package de.jakob.lotm.fluid;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, LOTMCraft.MOD_ID);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> DROPS_OF_ETERNAL_DARKNESS_SOURCE =
            FLUIDS.register("drops_of_eternal_darkness",
                    () -> new BaseFlowingFluid.Source(dropsProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> DROPS_OF_ETERNAL_DARKNESS_FLOWING =
            FLUIDS.register("flowing_drops_of_eternal_darkness",
                    () -> new BaseFlowingFluid.Flowing(dropsProperties()));

    private static BaseFlowingFluid.Properties dropsProperties() {
        return PropertiesHolder.DROPS_OF_ETERNAL_DARKNESS_PROPERTIES;
    }

    private static final class PropertiesHolder {
        private static final BaseFlowingFluid.Properties DROPS_OF_ETERNAL_DARKNESS_PROPERTIES =
                new BaseFlowingFluid.Properties(
                        ModFluidTypes.DROPS_OF_ETERNAL_DARKNESS_TYPE,
                        DROPS_OF_ETERNAL_DARKNESS_SOURCE,
                        DROPS_OF_ETERNAL_DARKNESS_FLOWING)
                        .block(ModBlocks.DROPS_OF_ETERNAL_DARKNESS)
                        .bucket(ModItems.DROPS_OF_ETERNAL_DARKNESS_BUCKET)
                        .levelDecreasePerBlock(1)
                        .slopeFindDistance(4)
                        .tickRate(5)
                        .explosionResistance(100.0f);

        private PropertiesHolder() {
        }
    }

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
