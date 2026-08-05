package de.jakob.lotm.structure;

import com.mojang.serialization.MapCodec;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructures {

    //tutorial for structure placement: https://github.com/TelepathicGrunt/StructureTutorialMod/blob/1.21-Neoforge-Jigsaw/src/main/java/com/telepathicgrunt/structure_tutorial/STStructures.java

    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(Registries.STRUCTURE_TYPE, LOTMCraft.MOD_ID);


    public static final DeferredHolder<StructureType<?>, StructureType<SimpleStructures>> SIMPLE_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("simple_structures", () -> explicitStructureTypeTyping(SimpleStructures.CODEC));
    public static final DeferredHolder<StructureType<?>, StructureType<CityStructure>> CITY_STRUCTURE =
            DEFERRED_REGISTRY_STRUCTURE.register("city_structure", () -> () -> CityStructure.CODEC);

    public static final DeferredHolder<StructureType<?>, StructureType<PairedStructure>> PAIRED_STRUCTURE =
            DEFERRED_REGISTRY_STRUCTURE.register("paired_structure", () -> () -> PairedStructure.CODEC);

    /**
     * Originally, I had a double lambda ()->()-> for the RegistryObject line above, but it turns out that
     * some IDEs cannot resolve the typing correctly. This method explicitly states what the return type
     * is so that the IDE can put it into the DeferredRegistry properly.
     */
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> structureCodec) {
        return () -> structureCodec;
    }

    public static void register(IEventBus eventBus) {
        DEFERRED_REGISTRY_STRUCTURE.register(eventBus);
    }

}
