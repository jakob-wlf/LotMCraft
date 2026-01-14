package de.jakob.lotm.abilities.fool.miracle_creation;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Optional;

public class MiracleHandler {

    public static void performMiracle(String miracleId, ServerLevel level, LivingEntity caster) {
        switch (miracleId) {
            case "summon_village" -> summonStructure(level, caster, "village_plains", "minecraft");
            case "summon_end_city" -> summonStructure(level, caster, "end_city", "minecraft");
            case "summon_pillager_outpost" -> summonStructure(level, caster, "pillager_outpost", "minecraft");
            case "summon_desert_temple" -> summonStructure(level, caster, "desert_pyramid", "minecraft");
            case "summon_evernight_church" -> summonStructure(level, caster, "evernight_church", LOTMCraft.MOD_ID);
        }
    }

    private static void summonStructure(ServerLevel level, LivingEntity caster, String structureName, String namespace) {
        BlockPos pos = caster.blockPosition();

        // Get the structure from registry
        ResourceKey<Structure> structureKey = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(namespace, structureName)
        );

        Optional<Holder.Reference<Structure>> structureHolder = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getHolder(structureKey);

        if (structureHolder.isEmpty()) {
            System.err.println("Could not find structure: " + structureName);
            return;
        }

        Structure structure = structureHolder.get().value();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        // Generate the structure
        StructureStart structureStart = structure.generate(
                level.registryAccess(),
                chunkGenerator,
                chunkGenerator.getBiomeSource(),
                level.getChunkSource().randomState(),
                level.getStructureManager(),
                level.getSeed(),
                new ChunkPos(pos),
                0,
                level,
                (biome) -> true
        );

        if (!structureStart.isValid()) {
            System.err.println("Failed to generate valid " + structureName + " structure");
            return;
        }

        // Calculate the chunk range needed
        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos minChunk = new ChunkPos(
                SectionPos.blockToSectionCoord(boundingBox.minX()),
                SectionPos.blockToSectionCoord(boundingBox.minZ())
        );
        ChunkPos maxChunk = new ChunkPos(
                SectionPos.blockToSectionCoord(boundingBox.maxX()),
                SectionPos.blockToSectionCoord(boundingBox.maxZ())
        );

        // Load all chunks in the structure's area
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            level.getChunk(chunkPos.x, chunkPos.z);
        });

        // Place the structure in all chunks
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            structureStart.placeInChunk(
                    level,
                    level.structureManager(),
                    chunkGenerator,
                    level.getRandom(),
                    new BoundingBox(
                            chunkPos.getMinBlockX(),
                            level.getMinBuildHeight(),
                            chunkPos.getMinBlockZ(),
                            chunkPos.getMaxBlockX(),
                            level.getMaxBuildHeight(),
                            chunkPos.getMaxBlockZ()
                    ),
                    chunkPos
            );
        });

        System.out.println("Successfully generated " + structureName + " at " + pos);

        // Mark chunks for saving
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            level.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true);
        });
    }
}