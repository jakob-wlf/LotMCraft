package de.jakob.lotm.gui.custom.brewing_cauldron;

import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.block.entity.BrewingCauldronBlockEntity;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BrewingCauldronMenu extends AbstractContainerMenu {
    public static final int SLOT_MAIN_INGREDIENT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_RECIPE = 4;
    public static final int[] SLOTS_SUPPLEMENTARY = {0, 1, 5, 6};

    private static final int[][] CAULDRON_SLOT_POS = {
            {48, 37},   // 0 supplementary (inner left)
            {112, 37},  // 1 supplementary (inner right)
            {80, 72},   // 2 main ingredient
            {80, 122},  // 3 output
            {152, 122}, // 4 recipe
            {14, 37},   // 5 supplementary (outer left)
            {146, 37}   // 6 supplementary (outer right)
    };

    public final BrewingCauldronBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public BrewingCauldronMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv,
                extraData != null ? inv.player.level().getBlockEntity(extraData.readBlockPos()) : null,
                new SimpleContainerData(2));
    }

    public BrewingCauldronMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.BREWING_CAULDRON_MENU.get(), pContainerId);

        if (entity instanceof BrewingCauldronBlockEntity brewingCauldronBlockEntity) {
            this.blockEntity = brewingCauldronBlockEntity;
            this.level = inv.player.level();
            this.data = data;

            addPlayerInventory(inv);
            addPlayerHotbar(inv);

            for (int i = 0; i < CAULDRON_SLOT_POS.length; i++) {
                this.addSlot(new SlotItemHandler(blockEntity.itemHandler, i,
                        CAULDRON_SLOT_POS[i][0], CAULDRON_SLOT_POS[i][1]));
            }

            addDataSlots(data);
        } else {
            this.blockEntity = null;
            this.level = inv.player.level();
            this.data = data;

            addPlayerInventory(inv);
            addPlayerHotbar(inv);

            SimpleContainer dummy = new SimpleContainer(CAULDRON_SLOT_POS.length);
            for (int i = 0; i < CAULDRON_SLOT_POS.length; i++) {
                this.addSlot(new Slot(dummy, i, CAULDRON_SLOT_POS[i][0], CAULDRON_SLOT_POS[i][1]));
            }

            addDataSlots(data);
        }
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getProgress()    { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 26  = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  27 - 35 = hotbar slots (which map to the InventoryPlayer slot numbers 0 - 8)
    //  36 - 42 = cauldron slots, which map to our block entity slot numbers 0 - 6
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = CAULDRON_SLOT_POS.length;  // 7 slots now
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (blockEntity == null) return false;
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.BREWING_CAULDRON.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 158 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 216));
        }
    }
}