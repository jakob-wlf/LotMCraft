package de.jakob.lotm.block.custom;

import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.gui.custom.BrewingCauldronMenu;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionRecipes;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT_SUPP_1 = 0;
    private static final int INPUT_SLOT_SUPP_2 = 1;
    private static final int INPUT_SLOT_MAIN = 2;
    private static final int OUTPUT_SLOT = 3;

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 72;

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BREWING_BLOCK_BE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> BrewingCauldronBlockEntity.this.progress;
                    case 1 -> BrewingCauldronBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0 -> BrewingCauldronBlockEntity.this.progress = value;
                    case 1 -> BrewingCauldronBlockEntity.this.maxProgress = value;
                }
            }

            //how many variables get saved in the data (progress + maxProgress)
            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lotmcraft.brewing_cauldron");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BrewingCauldronMenu(i, inventory, this, this.data);
    }

    public void drops() {
        if(this.level == null)
            return;

        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("brewing_cauldron.progress", progress);
        pTag.putInt("brewing_cauldron.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("brewing_cauldron.progress");
        maxProgress = pTag.getInt("brewing_cauldron.max_progress");
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
        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);

            if(hasCraftingFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void craftItem() {
        BeyonderPotion potion = PotionRecipes.getByIngredients(
                itemHandler.getStackInSlot(INPUT_SLOT_SUPP_1),
                itemHandler.getStackInSlot(INPUT_SLOT_SUPP_2),
                itemHandler.getStackInSlot(INPUT_SLOT_MAIN)
        );

        if(potion == null)
            return;

        itemHandler.setStackInSlot(INPUT_SLOT_SUPP_1, ItemStack.EMPTY);
        itemHandler.setStackInSlot(INPUT_SLOT_SUPP_2, ItemStack.EMPTY);
        itemHandler.setStackInSlot(INPUT_SLOT_MAIN, ItemStack.EMPTY);
        itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(potion, 1));
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 72;
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        BeyonderPotion potion = PotionRecipes.getByIngredients(
                itemHandler.getStackInSlot(INPUT_SLOT_SUPP_1),
                itemHandler.getStackInSlot(INPUT_SLOT_SUPP_2),
                itemHandler.getStackInSlot(INPUT_SLOT_MAIN)
        );

        if(potion == null)
            return false;

        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty();
    }
}
