package de.jakob.lotm.gui;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.artifact_wheel.ArtifactWheelMenu;
import de.jakob.lotm.gui.custom.flaming_jump.FlamingJumpMenu;
import de.jakob.lotm.gui.custom.honorific_names.HonorificNamesMenu;
import de.jakob.lotm.gui.custom.ability_wheel.AbilityWheelMenu;
import de.jakob.lotm.gui.custom.brewing_cauldron.BrewingCauldronMenu;
import de.jakob.lotm.gui.custom.introspect.IntrospectMenu;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenu;
import de.jakob.lotm.gui.custom.mass_puppeteering.MassPuppeteeringMenu;
import de.jakob.lotm.gui.custom.recipe.RecipeMenu;
import de.jakob.lotm.gui.custom.ritualistic_table.RitualMenu;
import de.jakob.lotm.gui.custom.trades.BeyonderTradeMenu;
import de.jakob.lotm.gui.custom.historical_void.HistoricalVoidMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, LOTMCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<IntrospectMenu>> INTROSPECT_MENU =
            MENU_TYPES.register("introspect_menu", () ->
                    IMenuTypeExtension.create(IntrospectMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MarionetteMenu>> MARIONETTE_MENU =
            MENU_TYPES.register("marionette_menu", () ->
                    IMenuTypeExtension.create(MarionetteMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BeyonderTradeMenu>> BEYONDER_TRADE_MENU = MENU_TYPES.register(
            "beyonder_trade_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                int npcEntityId = data.readVarInt();
                return new BeyonderTradeMenu(windowId, inv, npcEntityId);
            })
    );

    public static final DeferredHolder<MenuType<?>, MenuType<RitualMenu>> RITUAL_MENU =
            MENU_TYPES.register("ritual_menu", () ->
                    IMenuTypeExtension.create(RitualMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeMenu>> RECIPE_MENU =
            MENU_TYPES.register("recipe_menu", () ->
                    IMenuTypeExtension.create(RecipeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BrewingCauldronMenu>> BREWING_CAULDRON_MENU =
            MENU_TYPES.register("brewing_cauldron_menu", () ->
                IMenuTypeExtension.create(BrewingCauldronMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HonorificNamesMenu>> HONORIFIC_NAMES_MENU =
            MENU_TYPES.register("honorific_names_menu", () ->
                    IMenuTypeExtension.create(HonorificNamesMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AbilityWheelMenu>> ABILITY_WHEEL_MENU = MENU_TYPES.register(
            "ability_wheel_menu",
            () -> new MenuType<>(AbilityWheelMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<HistoricalVoidMenu>> HISTORICAL_VOID_MENU =
            MENU_TYPES.register("historical_void_menu",
                    () -> IMenuTypeExtension.create((containerId, inv, buf) -> new HistoricalVoidMenu(containerId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<FlamingJumpMenu>> FLAMING_JUMP_MENU =
            MENU_TYPES.register("flaming_jump_menu", () ->
                    IMenuTypeExtension.create(FlamingJumpMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MassPuppeteeringMenu>> MASS_PUPPETEERING_MENU =
            MENU_TYPES.register("mass_puppeteering_menu", () ->
                    IMenuTypeExtension.create(MassPuppeteeringMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArtifactWheelMenu>> ARTIFACT_WHEEL_MENU = MENU_TYPES.register(
            "artifact_wheel_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
                return new ArtifactWheelMenu(windowId, inv, stack);
            })
    );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

}
