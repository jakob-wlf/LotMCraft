package de.jakob.lotm.gui;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.ArtifactWheel.ArtifactWheelMenu;
import de.jakob.lotm.gui.custom.Gathering.GatheringMenu;
import de.jakob.lotm.gui.custom.HonorificNames.HonorificNamesMenu;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelMenu;
import de.jakob.lotm.gui.custom.BrewingCauldron.BrewingCauldronMenu;
import de.jakob.lotm.gui.custom.CopiedAbilityWheel.CopiedAbilityWheelMenu;
import de.jakob.lotm.gui.custom.Introspect.IntrospectMenu;
import de.jakob.lotm.gui.custom.Recipe.RecipeMenu;
import de.jakob.lotm.gui.custom.RiverAuthority.RiverAuthorityMenu;
import de.jakob.lotm.gui.custom.RiverBlessing.RiverBlessingMenu;
import de.jakob.lotm.gui.custom.ChaosSeaAuthority.ChaosSeaAuthorityMenu;
import de.jakob.lotm.gui.custom.RiverSefirotAuthority.RiverSefirotAuthorityMenu;
import de.jakob.lotm.gui.custom.SefirotAuthority.SefirotAuthorityMenu;
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

    public static final DeferredHolder<MenuType<?>, MenuType<RecipeMenu>> RECIPE_MENU =
            MENU_TYPES.register("recipe_menu", () ->
                    IMenuTypeExtension.create(RecipeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BrewingCauldronMenu>> BREWING_CAULDRON_MENU =
            MENU_TYPES.register("brewing_cauldron_menu", () ->
                IMenuTypeExtension.create(BrewingCauldronMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HonorificNamesMenu>> HONORIFIC_NAMES_MENU =
            MENU_TYPES.register("honorific_names_menu", () ->
                    IMenuTypeExtension.create(HonorificNamesMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<de.jakob.lotm.gui.custom.Prey.PreyMenu>> PREY_MENU =
            MENU_TYPES.register("prey_menu", () ->
                    IMenuTypeExtension.create(de.jakob.lotm.gui.custom.Prey.PreyMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AbilityWheelMenu>> ABILITY_WHEEL_MENU = MENU_TYPES.register(
            "ability_wheel_menu",
            () -> new MenuType<>(AbilityWheelMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<CopiedAbilityWheelMenu>> COPIED_ABILITY_WHEEL_MENU = MENU_TYPES.register(
            "copied_ability_wheel_menu",
            () -> new MenuType<>(CopiedAbilityWheelMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ArtifactWheelMenu>> ARTIFACT_WHEEL_MENU = MENU_TYPES.register(
            "artifact_wheel_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
                return new ArtifactWheelMenu(windowId, inv, stack);
            })
    );

    public static final DeferredHolder<MenuType<?>, MenuType<SefirotAuthorityMenu>> SEFIROT_AUTHORITY_MENU = MENU_TYPES.register(
            "sefirot_authority_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new SefirotAuthorityMenu(windowId, inv, data))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<RiverAuthorityMenu>> RIVER_AUTHORITY_MENU = MENU_TYPES.register(
            "river_authority_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new RiverAuthorityMenu(windowId, inv, data))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<RiverSefirotAuthorityMenu>> RIVER_SEFIROT_AUTHORITY_MENU = MENU_TYPES.register(
            "river_sefirot_authority_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new RiverSefirotAuthorityMenu(windowId, inv, data))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ChaosSeaAuthorityMenu>> CHAOS_SEA_AUTHORITY_MENU = MENU_TYPES.register(
            "chaos_sea_authority_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new ChaosSeaAuthorityMenu(windowId, inv, data))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<GatheringMenu>> GATHERING_MENU = MENU_TYPES.register(
            "gathering_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new GatheringMenu(windowId, inv, data))
    );

    public static final DeferredHolder<MenuType<?>, MenuType<RiverBlessingMenu>> RIVER_BLESSING_MENU = MENU_TYPES.register(
            "river_blessing_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new RiverBlessingMenu(windowId, inv, data))
    );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

}
