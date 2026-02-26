package de.jakob.lotm.gui;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.HonorificNames.HonorificNamesMenu;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelMenu;
import de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu;
import de.jakob.lotm.gui.custom.Introspect.IntrospectMenu;
import de.jakob.lotm.gui.custom.Messages.MessagesMenu;
import de.jakob.lotm.gui.custom.Recipe.RecipeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, LOTMCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<IntrospectMenu>> INTROSPECT_MENU =
            MENU_TYPES.register("introspect_menu", () ->
                    IMenuTypeExtension.create(IntrospectMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeMenu>> RECIPE_MENU =
            MENU_TYPES.register("recipe_menu", () ->
                    IMenuTypeExtension.create(RecipeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BrewingCauldronMenu>> BREWING_CAULDRON_MENU =
            MENU_TYPES.register("brewing_cauldron_menu", () ->
                IMenuTypeExtension.create(BrewingCauldronMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MessagesMenu>> MESSAGES_MENU =
            MENU_TYPES.register("messages_menu", () ->
                    IMenuTypeExtension.create(MessagesMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HonorificNamesMenu>> HONORIFIC_NAMES_MENU =
            MENU_TYPES.register("honorific_names_menu", () ->
                    IMenuTypeExtension.create(HonorificNamesMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AbilityWheelMenu>> ABILITY_WHEEL_MENU = MENU_TYPES.register(
            "ability_wheel_menu",
            () -> new MenuType<>(AbilityWheelMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

}
