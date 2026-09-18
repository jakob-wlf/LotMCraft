package de.jakob.lotm.block.entity;

import de.jakob.lotm.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RitualisticTablePartBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos mainPos;

    public RitualisticTablePartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUALISTIC_TABLE_PART_BE.get(), pos, state);
    }

    @Nullable
    public BlockPos getMainPos() {
        return mainPos;
    }

    public void setMainPos(BlockPos mainPos) {
        this.mainPos = mainPos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (mainPos != null) {
            tag.putLong("MainPos", mainPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("MainPos")) {
            mainPos = BlockPos.of(tag.getLong("MainPos"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}