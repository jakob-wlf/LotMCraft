package de.jakob.lotm.datagen;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, LOTMCraft.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }

}