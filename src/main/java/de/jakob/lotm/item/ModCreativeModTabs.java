package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.beyonders.potions.PotionItemHandler;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LOTMCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> LOTM_TAB = CREATIVE_MODE_TABS.register("lotm_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MOD_ICON.get()))
                    .title(Component.translatable("creativetab.lotmcraft.lotm_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FOOL_Card.get());
                        output.accept(ModItems.CRYSTAL_BALL.get());
                        output.accept(ModItems.CANE.get());
                        output.accept(ModItems.GUIDING_BOOK.get());
                        output.accept(ModItems.UPPER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get());
                        output.accept(ModItems.RIGHT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get());
                        output.accept(ModItems.LEFT_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get());
                        output.accept(ModItems.LOWER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get());
                        output.accept(ModItems.MYSTERIOUS_TABLET.get());
                        output.accept(ModItems.MYSTICAL_RING.get());
                        output.accept(ModBlocks.BREWING_CAULDRON.get());
                        output.accept(ModBlocks.VOID);
                        output.accept(ModBlocks.SOLID_VOID);
                        output.accept(ModBlocks.REALITY_PORTAL);
                        output.accept(ModItems.PAPER_SWORD);
                        output.accept(ModItems.PAPER_PICKAXE);
                        output.accept(ModItems.PAPER_AXE);
                        output.accept(ModItems.PAPER_SHOVEL);
                        output.accept(ModItems.ONE_POUND);
                        output.accept(ModItems.ONE_SOLI);
                        output.accept(ModItems.UNIQUENESS_MAP);
                        output.accept(ModItems.CITY_MAP);
                        output.accept(ModItems.DROPS_OF_ETERNAL_DARKNESS_BUCKET.get());
                        output.accept(ModItems.SEALED_BOTTLE.get());
                        output.accept(ModItems.ETERNAL_DARKNESS_RIVER_WATER_BOTTLE.get());
                        // ── Blasphemy Cards ──
                        output.accept(ModItems.FOOL_BLASPHEMY_CARD.get());
                        output.accept(ModItems.DOOR_BLASPHEMY_CARD.get());
                        output.accept(ModItems.ERROR_BLASPHEMY_CARD.get());
                        output.accept(ModItems.SUN_BLASPHEMY_CARD.get());
                        output.accept(ModItems.TYRANT_BLASPHEMY_CARD.get());
                        output.accept(ModItems.VISIONARY_BLASPHEMY_CARD.get());
                        output.accept(ModItems.DARKNESS_BLASPHEMY_CARD.get());
                        output.accept(ModItems.DEATH_BLASPHEMY_CARD.get());
                        output.accept(ModItems.TWILIGHT_GIANT_BLASPHEMY_CARD.get());
                        output.accept(ModItems.DEMONESS_BLASPHEMY_CARD.get());
                        output.accept(ModItems.RED_PRIEST_BLASPHEMY_CARD.get());
                        output.accept(ModItems.MOTHER_BLASPHEMY_CARD.get());
                        output.accept(ModItems.ABYSS_BLASPHEMY_CARD.get());
                        output.accept(ModItems.WHEEL_OF_FORTUNE_BLASPHEMY_CARD.get());
                        output.accept(ModItems.BLACK_EMPEROR_BLASPHEMY_CARD.get());
                        output.accept(ModItems.JUSTICIAR_BLASPHEMY_CARD.get());
                        // ── Blasphemy Slate ──
                        output.accept(ModItems.BLASPHEMY_SLATE_LEFT_HALF.get());
                        output.accept(ModItems.BLASPHEMY_SLATE_RIGHT_HALF.get());
                        output.accept(ModItems.BLASPHEMY_SLATE.get());
                    })
                    .build());

    public static final Supplier<CreativeModeTab> PASSIVE_ABILITIES_TAB = CREATIVE_MODE_TABS.register("passive_abilities_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(PassiveAbilityHandler.PHYSICAL_ENHANCEMENTS_RED_PRIEST.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "lotm_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.passive_abilities_tab"))
                    .displayItems((parameters, output) -> {
                        PassiveAbilityHandler.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());
                        });
                    })
                    .build());

    public static final Supplier<CreativeModeTab> BEYONDER_POTIONS_TAB = CREATIVE_MODE_TABS.register("beyonder_potions_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(PotionItemHandler.SEER_POTION.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "passive_abilities_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.beyonder_potions_tab"))
                    .displayItems((parameters, output) -> {
                        PotionItemHandler.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());
                        });
                    })
                    .build());

    public static final Supplier<CreativeModeTab> BEYONDER_INGREDIENTS_TAB = CREATIVE_MODE_TABS.register("beyonder_ingredients_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModIngredients.LAVOS_SQUID_BLOOD.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "beyonder_potions_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.beyonder_ingredients_tab"))
                    .displayItems((parameters, output) -> {
                        ModIngredients.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());

                        });
                    })
                    .build());

    public static final Supplier<CreativeModeTab> BEYONDER_POTION_RECIPES_TAB = CREATIVE_MODE_TABS.register("beyonder_recipes_tab",
            () -> CreativeModeTab.builder().icon(() -> {
                Optional<DeferredHolder<Item, ? extends Item>> optionalItem = PotionRecipeItemHandler.ITEMS.getEntries().stream().findFirst();
                        return optionalItem.map(itemDeferredHolder -> new ItemStack(itemDeferredHolder.get())).orElseGet(() -> new ItemStack(ModItems.FOOL_Card.get()));
                    })
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "beyonder_ingredients_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.beyonder_recipes_tab"))
                    .displayItems((parameters, output) -> {
                        PotionRecipeItemHandler.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());
                        });
                    })
                    .build());

    public static final Supplier<CreativeModeTab> BEYONDER_CHARACTERISTICS_TAB = CREATIVE_MODE_TABS.register("beyonder_characteristic_tab",
            () -> CreativeModeTab.builder().icon(() -> {
                        Optional<DeferredHolder<Item, ? extends Item>> optionalItem = BeyonderCharacteristicItemHandler.ITEMS.getEntries().stream().findFirst();
                        return optionalItem.map(itemDeferredHolder -> new ItemStack(itemDeferredHolder.get())).orElseGet(() -> new ItemStack(ModItems.FOOL_Card.get()));
                    })
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "beyonder_recipes_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.beyonder_characteristic_tab"))
                    .displayItems((parameters, output) -> {
                        BeyonderCharacteristicItemHandler.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());
                        });
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
