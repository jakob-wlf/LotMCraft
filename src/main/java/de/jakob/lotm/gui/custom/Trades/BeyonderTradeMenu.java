package de.jakob.lotm.gui.custom.Trades;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import org.jetbrains.annotations.NotNull;

public class BeyonderTradeMenu extends AbstractContainerMenu {

    private final int npcEntityId;
    private final BeyonderNPCEntity npc;
    private final Container inputSlotsContainer;

    private static final int INPUT_SLOTS_PER_TRADE = 2;

    public BeyonderTradeMenu(int windowId, Inventory playerInventory, int npcEntityId) {
        super(ModMenuTypes.BEYONDER_TRADE_MENU.get(), windowId);
        this.npcEntityId = npcEntityId;

        net.minecraft.world.entity.Entity entity = playerInventory.player.level().getEntity(npcEntityId);
        if (entity instanceof BeyonderNPCEntity beyonderNPC) {
            this.npc = beyonderNPC;
        } else {
            this.npc = null;
        }

        int tradeCount = this.npc != null ? this.npc.getCurrentTrades().size() : 0;
        this.inputSlotsContainer = new SimpleContainer(Math.max(1, tradeCount) * INPUT_SLOTS_PER_TRADE);

        for (int i = 0; i < tradeCount; i++) {
            final int tradeIndex = i;
            BeyonderNPCEntity.TradeEntry trade = this.npc.getCurrentTrades().get(i);

            int slotAIndex = tradeIndex * 2;
            this.addSlot(new Slot(inputSlotsContainer, slotAIndex, 0, 0) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return true;
                }

                @Override
                public int getMaxStackSize() {
                    return 64;
                }
            });

            int slotBIndex = tradeIndex * 2 + 1;
            this.addSlot(new Slot(inputSlotsContainer, slotBIndex, 0, 0) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return !trade.costB().isEmpty();
                }

                @Override
                public int getMaxStackSize() {
                    return 64;
                }
            });
        }

        int invStartY = 140;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, invStartY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, invStartY + 58));
        }
    }

    public BeyonderNPCEntity getNpc() {
        return npc;
    }

    public int getNpcEntityId() {
        return npcEntityId;
    }

    public Container getInputSlotsContainer() {
        return inputSlotsContainer;
    }

    public static int getInputSlotAIndex(int tradeIndex) {
        return tradeIndex * INPUT_SLOTS_PER_TRADE;
    }

    public static int getInputSlotBIndex(int tradeIndex) {
        return tradeIndex * INPUT_SLOTS_PER_TRADE + 1;
    }

    public void returnInputItems(Player player, int tradeIndex) {
        int slotA = getInputSlotAIndex(tradeIndex);
        int slotB = getInputSlotBIndex(tradeIndex);

        ItemStack a = inputSlotsContainer.getItem(slotA);
        if (!a.isEmpty()) {
            giveOrDrop(player, a.copy());
            inputSlotsContainer.setItem(slotA, ItemStack.EMPTY);
        }

        ItemStack b = inputSlotsContainer.getItem(slotB);
        if (!b.isEmpty()) {
            giveOrDrop(player, b.copy());
            inputSlotsContainer.setItem(slotB, ItemStack.EMPTY);
        }
    }

    public static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        boolean added = player.getInventory().add(stack);
        if (!added || !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack original = stackInSlot.copy();
        int inputSlotCount = inputSlotsContainer.getContainerSize();

        if (index < inputSlotCount) {
            if (!moveItemStackTo(stackInSlot, inputSlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int i = 0; i < inputSlotCount; i++) {
                Slot inputSlot = this.slots.get(i);
                if (inputSlot.mayPlace(stackInSlot) && !inputSlot.hasItem()) {
                    if (moveItemStackTo(stackInSlot, i, i + 1, false)) {
                        moved = true;
                        break;
                    }
                }
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
        return original;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < inputSlotsContainer.getContainerSize(); i++) {
                ItemStack stack = inputSlotsContainer.getItem(i);
                if (!stack.isEmpty()) {
                    giveOrDrop(player, stack);
                    inputSlotsContainer.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

}