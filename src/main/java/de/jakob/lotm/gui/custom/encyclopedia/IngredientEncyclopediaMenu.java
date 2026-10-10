package de.jakob.lotm.gui.custom.encyclopedia;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class IngredientEncyclopediaMenu extends AbstractContainerMenu {
    @Nullable
    private final ResourceLocation focus;

    public IngredientEncyclopediaMenu(int containerId, Inventory inventory, @Nullable ResourceLocation focus) {
        super(ModMenuTypes.INGREDIENT_ENCYCLOPEDIA.get(), containerId);
        this.focus = focus;
    }

    public IngredientEncyclopediaMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, readFocus(buf));
    }

    public static void writeFocus(RegistryFriendlyByteBuf buf, @Nullable ResourceLocation focus) {
        buf.writeBoolean(focus != null);
        if (focus != null) buf.writeResourceLocation(focus);
    }

    @Nullable
    private static ResourceLocation readFocus(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Nullable
    public ResourceLocation getFocus() {
        return focus;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // no slots
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}