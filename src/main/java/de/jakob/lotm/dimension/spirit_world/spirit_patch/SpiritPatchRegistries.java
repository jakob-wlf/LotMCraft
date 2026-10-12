package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SpiritPatchRegistries {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LOTMCraft.MOD_ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, LOTMCraft.MOD_ID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, LOTMCraft.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, LOTMCraft.MOD_ID);

    public static final Supplier<StructureType<SpiritShrineStructure>> SHRINE_TYPE =
            STRUCTURE_TYPES.register("spirit_shrine",
                    () -> (StructureType<SpiritShrineStructure>) () -> SpiritShrineStructure.CODEC);

    public static final Supplier<StructurePieceType> SHRINE_PIECE =
            PIECES.register("spirit_shrine",
                    () -> (StructurePieceType.ContextlessType) SpiritShrinePiece::new);

    public static final Supplier<Feature<NoneFeatureConfiguration>> PATCH_DECOR =
            FEATURES.register("spirit_patch_decor",
                    () -> new SpiritPatchFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        STRUCTURE_TYPES.register(bus);
        PIECES.register(bus);
        FEATURES.register(bus);
    }
}
