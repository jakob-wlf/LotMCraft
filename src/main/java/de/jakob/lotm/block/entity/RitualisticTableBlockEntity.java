package de.jakob.lotm.block.entity;

import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.gui.custom.ritualistic_table.RitualMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class RitualisticTableBlockEntity extends BlockEntity implements MenuProvider {

    private String honorificLine1 = "";
    private String honorificLine2 = "";
    private String honorificLine3 = "";

    private static final int SLOT_COUNT = 4;

    public final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.getChunkSource().getLightEngine().checkBlock(worldPosition);

                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public RitualisticTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RITUALISTIC_TABLE_BE.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lotmcraft.ritualistic_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RitualMenu(i, inventory, this);
    }

    public void drops() {
        if (this.level == null)
            return;

        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public String getHonorificLine1() { return honorificLine1; }
    public String getHonorificLine2() { return honorificLine2; }
    public String getHonorificLine3() { return honorificLine3; }

    public void setHonorificLines(String line1, String line2, String line3) {
        this.honorificLine1 = line1 == null ? "" : line1;
        this.honorificLine2 = line2 == null ? "" : line2;
        this.honorificLine3 = line3 == null ? "" : line3;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putString("honorific1", honorificLine1);
        pTag.putString("honorific2", honorificLine2);
        pTag.putString("honorific3", honorificLine3);
        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        CompoundTag inv = pTag.getCompound("inventory");
        if (inv.contains("Size") && inv.getInt("Size") == itemHandler.getSlots()) {
            itemHandler.deserializeNBT(pRegistries, inv);
        }

        honorificLine1 = pTag.getString("honorific1");
        honorificLine2 = pTag.getString("honorific2");
        honorificLine3 = pTag.getString("honorific3");
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


}