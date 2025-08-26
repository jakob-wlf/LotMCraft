package de.jakob.lotm.structure;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSOR_TYPES =
        DeferredRegister.create(BuiltInRegistries.STRUCTURE_PROCESSOR, LOTMCraft.MOD_ID);
    
    public static final Supplier<StructureProcessorType<CauldronReplacementProcessor>> CAULDRON_REPLACEMENT =
        PROCESSOR_TYPES.register("cauldron_replacement", 
            () -> () -> CauldronReplacementProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        PROCESSOR_TYPES.register(eventBus);
    }
}