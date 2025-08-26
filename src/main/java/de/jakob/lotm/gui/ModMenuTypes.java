package de.jakob.lotm.gui;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.AbilitySelectionMenu;
import de.jakob.lotm.gui.custom.BrewingCauldronMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, LOTMCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AbilitySelectionMenu>> ABILITY_SELECTION_MENU =
            MENU_TYPES.register("ability_selection_menu", () ->
                    IMenuTypeExtension.create(AbilitySelectionMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BrewingCauldronMenu>> BREWING_CAULDRON_MENU =
            MENU_TYPES.register("brewing_cauldron_menu", () ->
                IMenuTypeExtension.create(BrewingCauldronMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

}
