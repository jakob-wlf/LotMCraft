package de.jakob.lotm.block.entity;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BreedingBlockEntity extends BlockEntity {

    private BlockEffectExecutor fxExecutor;

    public BreedingBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BREEDING_BLOCK_BE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide() && fxExecutor == null) {
            FX fx = FXHelper.getFX(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "breeding_block_particles"));
            if (fx != null) {
                fxExecutor = new BlockEffectExecutor(fx, level, blockPos);
                fxExecutor.setCheckState(true);
                fxExecutor.setAllowMulti(false);
                fxExecutor.start();
            }
        }
    }
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (fxExecutor != null && fxExecutor.getRuntime() != null) {
            fxExecutor.getRuntime().destroy(true);
        }
    }
}
