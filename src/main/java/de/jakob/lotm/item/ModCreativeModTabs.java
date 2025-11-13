package de.jakob.lotm.item;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
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
                        output.accept(ModBlocks.BREWING_CAULDRON.get());
                        output.accept(ModBlocks.VOID);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> ABILITIES_TAB = CREATIVE_MODE_TABS.register("abilities_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(AbilityItemHandler.COGITATION.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "lotm_tab"))
                    .title(Component.translatable("creativetab.lotmcraft.abilities_tab"))
                    .displayItems((parameters, output) -> {
                        AbilityItemHandler.ITEMS.getEntries().forEach(itemHolder -> {
                            output.accept(itemHolder.get());
                        });
                    })
                    .build());

    public static final Supplier<CreativeModeTab> PASSIVE_ABILITIES_TAB = CREATIVE_MODE_TABS.register("passive_abilities_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(PassiveAbilityHandler.PHYSICAL_ENHANCEMENTS_RED_PRIEST.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "abilities_tab"))
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
                        //String[] lastPathway = new String[]{"none"};
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


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
