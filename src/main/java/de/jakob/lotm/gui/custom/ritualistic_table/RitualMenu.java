package de.jakob.lotm.gui.custom.ritualistic_table;

import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.block.custom.RitualisticTableBlockEntity;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class RitualMenu extends AbstractContainerMenu {

    public static final int CANDLE_SLOT = 0;
    public static final int SACRIFICE_SLOT_1 = 1;
    public static final int SACRIFICE_SLOT_2 = 2;
    public static final int SACRIFICE_SLOT_3 = 3;
    private static final int RITUAL_SLOT_COUNT = 4;

    public final RitualisticTableBlockEntity blockEntity;
    private final Level level;

    public RitualMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv,
                extraData != null ? inv.player.level().getBlockEntity(extraData.readBlockPos()) : null);
    }

    public RitualMenu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.RITUAL_MENU.get(), pContainerId);

        if (entity instanceof RitualisticTableBlockEntity ritualisticTableBlockEntity) {
            this.blockEntity = ritualisticTableBlockEntity;
            this.level = inv.player.level();

            addPlayerInventory(inv);
            addPlayerHotbar(inv);

            // Candle slot
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, CANDLE_SLOT, 79, 20));

            // Sacrificial item slots (up to 3)
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, SACRIFICE_SLOT_1, 53, 50));
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, SACRIFICE_SLOT_2, 79, 50));
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, SACRIFICE_SLOT_3, 105, 50));
        } else {
            // Client-side fallback when block entity isn't available
            this.blockEntity = null;
            this.level = inv.player.level();

            addPlayerInventory(inv);
            addPlayerHotbar(inv);

            this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(RITUAL_SLOT_COUNT), CANDLE_SLOT, 79, 20));
            this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(RITUAL_SLOT_COUNT), SACRIFICE_SLOT_1, 53, 50));
            this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(RITUAL_SLOT_COUNT), SACRIFICE_SLOT_2, 79, 50));
            this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(RITUAL_SLOT_COUNT), SACRIFICE_SLOT_3, 105, 50));
        }
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = RITUAL_SLOT_COUNT;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
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
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.RITUALISTIC_TABLE.get()); // adjust to your actual block field name
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