package de.jakob.lotm.dimension.abyss.Flith;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFilth {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, LOTMCraft.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, LOTMCraft.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LOTMCraft.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);
    public static final DeferredHolder<FluidType, FluidType> LIQUID_FILTH_TYPE = FLUID_TYPES.register("liquid_filth", () -> new FluidType(FluidType.Properties.create().descriptionId("block.lotmcraft.liquid_filth").canSwim(true).canDrown(false).fallDistanceModifier(0.0f).density(2000).viscosity(3000)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SOURCE = FLUIDS.register("liquid_filth", () -> new BaseFlowingFluid.Source(ModFilth.PROPERTIES));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING = FLUIDS.register("flowing_liquid_filth", () -> new BaseFlowingFluid.Flowing(ModFilth.PROPERTIES));
    public static final DeferredBlock<LiquidBlock> BLOCK = BLOCKS.register("liquid_filth", () -> new LiquidBlock(SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_BLACK)));
    private static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(LIQUID_FILTH_TYPE, SOURCE, FLOWING).block(BLOCK).slopeFindDistance(4).levelDecreasePerBlock(1).tickRate(8);
    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
    }
}