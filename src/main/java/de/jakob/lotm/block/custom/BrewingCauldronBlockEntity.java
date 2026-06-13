package de.jakob.lotm.block.custom;

import de.jakob.lotm.block.ModBlockEntities;
import de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlockEntity extends BlockEntity implements MenuProvider {
    /**
     * Tracks all loaded instances so the card-uniqueness system can find
     * Blasphemy Cards sitting in cauldron recipe slots.
     * Uses a WeakHashMap so GC'd (unloaded) entries are collected automatically.
     */
    public static final java.util.Set<BrewingCauldronBlockEntity> INSTANCES =
            java.util.Collections.synchronizedSet(
                    java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @org.jetbrains.annotations.NotNull ItemStack stack) {
            if (slot == INPUT_SLOT_RECIPE) {
                // Recipe slot accepts Blasphemy Cards and Potion Recipe Items
                return stack.getItem() instanceof BlasphemyCardItem
                        || stack.getItem() instanceof PotionRecipeItem;
            }
            return true;
        }
    };

    private static final int INPUT_SLOT_SUPP_1 = 0;
    private static final int INPUT_SLOT_SUPP_2 = 1;
    private static final int INPUT_SLOT_MAIN = 2;
    private static final int INPUT_SLOT_RECIPE = 4;
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

            //how many variables get saved in the data (progress + maxProgress) (I would 100% forget this without having written that comment :))
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

    @Override
    public void onLoad() {
        super.onLoad();
        INSTANCES.add(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        INSTANCES.remove(this);
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

        CompoundTag inv = pTag.getCompound("inventory");
        if (inv.contains("Size") && inv.getInt("Size") == itemHandler.getSlots()) {
            itemHandler.deserializeNBT(pRegistries, inv);
        }

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
        if(level.isClientSide) {
            return;
        }

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

        // Decrement envisioned card uses; destroy the card when uses reach 0
        ItemStack recipeStack = itemHandler.getStackInSlot(INPUT_SLOT_RECIPE);
        if (!recipeStack.isEmpty() && recipeStack.getItem() instanceof BlasphemyCardItem bc
                && de.jakob.lotm.events.BlasphemySlateItemHandler.isEnvisionSummoned(recipeStack)) {
            int remaining = de.jakob.lotm.events.BlasphemySlateItemHandler.decrementEnvisionedUses(recipeStack);
            if (remaining <= 0) {
                itemHandler.setStackInSlot(INPUT_SLOT_RECIPE, ItemStack.EMPTY);
                // Put the pathway on a 5-hour cooldown for the player who summoned this card
                if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                    de.jakob.lotm.attachments.SummonedBlasphemyData sbd =
                            de.jakob.lotm.attachments.SummonedBlasphemyData.get(sl.getServer());
                    String pathway = bc.getPathway();
                    // Find which online player has this pathway summoned and apply the cooldown
                    for (net.minecraft.server.level.ServerPlayer sp : sl.getServer().getPlayerList().getPlayers()) {
                        if (sbd.hasSummoned(sp.getUUID(), pathway)) {
                            sbd.forceDismiss(sp.getUUID(), pathway);
                            sbd.putOnCooldown(sp.getUUID(), pathway,
                                    de.jakob.lotm.attachments.SummonedBlasphemyData.DESTROYED_COOLDOWN_MS);
                            de.jakob.lotm.network.PacketHandler.sendToPlayer(sp,
                                    new de.jakob.lotm.network.packets.toClient.SyncSummonedBlasphemyPacket(
                                            sbd.getCards(sp.getUUID()),
                                            sbd.getLockedCards(sp.getUUID()),
                                            sbd.getCooldownExpiryMap(sp.getUUID())));
                            break;
                        }
                    }
                }
            }
        }

        if(BeyonderData.playerMap.check(potion.getPathway(), potion.getSequence()))
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

        if (itemHandler.getStackInSlot(INPUT_SLOT_MAIN)
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .contains("VoidSummonTime")) {
            return false;
        }
        if(potion == null)
            return false;

        ItemStack recipeItem = itemHandler.getStackInSlot(INPUT_SLOT_RECIPE);
        if (recipeItem.getItem() instanceof BlasphemyCardItem card) {
            // A Blasphemy Card in the recipe slot acts as a wildcard recipe item for its pathway.
            if (!card.getPathway().equals(potion.getPathway())) {
                return false;
            }
        } else {
            if (recipeItem.isEmpty() || !(recipeItem.getItem() instanceof PotionRecipeItem recipe)) {
                return false;
            }
            if (recipe.getRecipe().potion().getSequence() != potion.getSequence() || !recipe.getRecipe().potion().getPathway().equals(potion.getPathway())) {
                return false;
            }
        }

        if (!itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) return false;
        return true;
    }
}
