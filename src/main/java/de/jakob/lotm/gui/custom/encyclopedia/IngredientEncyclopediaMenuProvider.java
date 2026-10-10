package de.jakob.lotm.gui.custom.encyclopedia;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class IngredientEncyclopediaMenuProvider implements MenuProvider {
    @Nullable
    private final ResourceLocation focus;

    public IngredientEncyclopediaMenuProvider(@Nullable Item focus) {
        this.focus = focus == null ? null : BuiltInRegistries.ITEM.getKey(focus);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("ingredient.lotm.encyclopedia.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new IngredientEncyclopediaMenu(containerId, inventory, focus);
    }

    public static void open(ServerPlayer player, @Nullable Item focus) {
        IngredientEncyclopediaMenuProvider provider = new IngredientEncyclopediaMenuProvider(focus);
        player.openMenu(provider, buf -> IngredientEncyclopediaMenu.writeFocus(buf, provider.focus));
    }
}