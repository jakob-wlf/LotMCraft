package de.jakob.lotm.gui.custom.spirit_housing;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SpiritHousingComponent;
import de.jakob.lotm.beyonders.abilities.darkness.SpiritCommandingAbility;
import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpiritManageMenu extends AbstractContainerMenu {
    private static final int BODY = 6;
    private static final int BIN = 15;
    private final SimpleContainer spirits = new SimpleContainer(16);

    public SpiritManageMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.SPIRIT_MANAGE_MENU.get(), containerId);
        int slotX = 48;
        addSlot(new SpiritSlot(spirits, 0, slotX, 18));
        addSlot(new SpiritSlot(spirits, 1, slotX, 38));
        addSlot(new SpiritSlot(spirits, 2, slotX, 58));
        addSlot(new SpiritSlot(spirits, 3, slotX + 18, 58));
        addSlot(new SpiritSlot(spirits, 4, slotX, 78));
        addSlot(new SpiritSlot(spirits, 5, slotX + 18, 78));
        addSlot(new SpiritSlot(spirits, BIN, slotX, 98));
        for (int i = 0; i < 9; i++) addSlot(new SpiritSlot(spirits, BODY + i, 8 + i * 18, 120));

        if (!inventory.player.level().isClientSide) {
            SpiritHousingComponent housing = inventory.player.getData(ModAttachments.SPIRIT_HOUSING);
            for (int i = 0; i < BODY; i++) {
                if (!housing.slots[i].isEmpty()) spirits.setItem(i, SpiritCommandingAbility.token(housing.slots[i]));
            }
            for (int i = 0; i < Math.min(9, housing.stored.size()); i++) {
                spirits.setItem(BODY + i, SpiritCommandingAbility.token(housing.stored.get(i)));
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        housing.stored.clear();
        for (int i = 0; i < BODY; i++) housing.slots[i] = "";
        for (int i = 0; i < BIN; i++) {
            String type = SpiritCommandingAbility.typeOf(spirits.getItem(i));
            if (type.isEmpty()) continue;
            if (i < BODY && SpiritCommandingAbility.allows(type, i)) housing.slots[i] = type;
            else if (housing.stored.size() < 9) housing.stored.add(type);
        }
        setCarried(ItemStack.EMPTY);
    }

    private static final class SpiritSlot extends Slot {
        private SpiritSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            String type = SpiritCommandingAbility.typeOf(stack);
            if (type.isEmpty()) return false;
            if (getContainerSlot() == BIN) return true;
            return getContainerSlot() >= BODY || SpiritCommandingAbility.allows(type, getContainerSlot());
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            super.set(getContainerSlot() == BIN ? ItemStack.EMPTY : stack);
        }
    }
}
