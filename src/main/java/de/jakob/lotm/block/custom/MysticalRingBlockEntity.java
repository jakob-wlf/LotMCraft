package de.jakob.lotm.block.custom;

import de.jakob.lotm.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MysticalRingBlockEntity extends BlockEntity {
    private String pathway = null;
    private Integer sequence = null;

    public MysticalRingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MYSTICAL_RING_BE.get(), pos, state);
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
        setChanged(); // Mark as dirty to trigger save
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
        setChanged();
    }

    public String getPathway() {
        return pathway;
    }

    public Integer getSequence() {
        return sequence;
    }

    public boolean hasSettings() {
        return pathway != null && sequence != null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pathway != null) {
            tag.putString("pathway", pathway);
        }
        if (sequence != null) {
            tag.putInt("sequence", sequence);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("pathway")) {
            pathway = tag.getString("pathway");
        }
        if (tag.contains("sequence")) {
            sequence = tag.getInt("sequence");
        }
    }
}