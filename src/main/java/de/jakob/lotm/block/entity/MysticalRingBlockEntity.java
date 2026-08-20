package de.jakob.lotm.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MysticalRingBlockEntity extends BlockEntity {
    private String pathway = null;
    private Integer sequence = null;
    private List<BlockPos> blocksToRemovedWhenActivated = null;

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

    public List<BlockPos> getBlocksToRemovedWhenActivated() {
        return blocksToRemovedWhenActivated;
    }

    public void setBlocksToRemovedWhenActivated(List<BlockPos> blocksToRemovedWhenActivated) {
        this.blocksToRemovedWhenActivated = blocksToRemovedWhenActivated;
        setChanged();
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
        if(blocksToRemovedWhenActivated != null) {
            long[] blockPosArray = blocksToRemovedWhenActivated.stream().mapToLong(BlockPos::asLong).toArray();
            tag.putLongArray("blocksToRemove", blockPosArray);
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
        if (tag.contains("blocksToRemove")) {
            long[] blockPosArray = tag.getLongArray("blocksToRemove");
            blocksToRemovedWhenActivated = new java.util.ArrayList<>();
            for (long posLong : blockPosArray) {
                blocksToRemovedWhenActivated.add(BlockPos.of(posLong));
            }
        }
    }
}