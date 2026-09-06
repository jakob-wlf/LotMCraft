package de.jakob.lotm.block.entity;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.gui.custom.sefirah.SefirahMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SefirahBlockEntity extends BlockEntity implements MenuProvider {

    private BlockEffectExecutor fxExecutor;

    public SefirahBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SEFIRAH_BLOCK_BE.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lotmcraft.sefirah_block");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SefirahMenu(i, inventory, this);
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
            FX fx = FXHelper.getFX(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle_particles"));
            if (fx != null) {
                fxExecutor = new BlockEffectExecutor(fx, level, blockPos);
                fxExecutor.setOffset(0, -.5, 0);
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
