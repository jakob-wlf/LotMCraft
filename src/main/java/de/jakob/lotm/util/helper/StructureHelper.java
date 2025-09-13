package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureHelper {
    public static void placeEvernightChurch(ServerLevel level, BlockPos pos) {
        // Your structure location in resources/data/lotmcraft/structures/evernight_church.nbt
        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "evernight_church");

        // StructureTemplateManager handles loading structures
        var structureManager = level.getStructureManager();

        structureManager.get(structureId).ifPresentOrElse(template -> {
            // Placement settings (rotation, mirroring, etc.)
            StructurePlaceSettings settings = new StructurePlaceSettings();

            // Place the structure in the world
            template.placeInWorld(level, pos, pos, settings, level.random, 2);
        }, () -> {
            System.out.println("Failed to load structure: " + structureId);
        });
    }
}
