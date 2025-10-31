package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.passives.PhysicalEnhancementsAbyssAbility;
import de.jakob.lotm.abilities.darkness.passives.NocturnalityAbility;
import de.jakob.lotm.abilities.darkness.passives.PhysicalEnhancementsDarknessAbility;
import de.jakob.lotm.abilities.demoness.passives.FeatherFallAbility;
import de.jakob.lotm.abilities.demoness.passives.PhysicalEnhancementsDemonessAbility;
import de.jakob.lotm.abilities.door.passives.PhysicalEnhancementsDoorAbility;
import de.jakob.lotm.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.abilities.fool.passives.PaperDaggersAbility;
import de.jakob.lotm.abilities.fool.passives.PhysicalEnhancementsFoolAbility;
import de.jakob.lotm.abilities.fool.passives.PuppeteeringEnhancementsAbility;
import de.jakob.lotm.abilities.mother.passives.PhysicalEnhancementsMotherAbility;
import de.jakob.lotm.abilities.red_priest.passive.FireResistanceAbility;
import de.jakob.lotm.abilities.red_priest.passive.FlamingHitAbility;
import de.jakob.lotm.abilities.red_priest.passive.PhysicalEnhancementsRedPriestAbility;
import de.jakob.lotm.abilities.sun.passives.PhysicalEnhancementsSunAbility;
import de.jakob.lotm.abilities.tyrant.passives.PhysicalEnhancementsTyrantAbility;
import de.jakob.lotm.abilities.tyrant.passives.RiptideAbility;
import de.jakob.lotm.abilities.visionary.passives.PhysicalEnhancementsVisionaryAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveCalamityAttraction;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveLuckAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PhysicalEnhancementsWheelOfFortuneAbility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PassiveAbilityHandler {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);

    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_RED_PRIEST = ITEMS.registerItem("physical_enhancements_red_priest_ability", PhysicalEnhancementsRedPriestAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_SUN = ITEMS.registerItem("physical_enhancements_sun_ability", PhysicalEnhancementsSunAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_TYRANT = ITEMS.registerItem("physical_enhancements_tyrant_ability", PhysicalEnhancementsTyrantAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DEMONESS = ITEMS.registerItem("physical_enhancements_demoness_ability", PhysicalEnhancementsDemonessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_ABYSS = ITEMS.registerItem("physical_enhancements_abyss_ability", PhysicalEnhancementsAbyssAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_FOOL = ITEMS.registerItem("physical_enhancements_fool_ability", PhysicalEnhancementsFoolAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DARKNESS = ITEMS.registerItem("physical_enhancements_darkness_ability", PhysicalEnhancementsDarknessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DOOR = ITEMS.registerItem("physical_enhancements_door_ability", PhysicalEnhancementsDoorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_MOTHER = ITEMS.registerItem("physical_enhancements_mother_ability", PhysicalEnhancementsMotherAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_VISIONARY = ITEMS.registerItem("physical_enhancements_visionary_ability", PhysicalEnhancementsVisionaryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_WHEEL_OF_FORTUNE = ITEMS.registerItem("physical_enhancements_wheel_of_fortune_ability", PhysicalEnhancementsWheelOfFortuneAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FLAMING_HIT = ITEMS.registerItem("flaming_hit_ability", FlamingHitAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> FIRE_RESISTANCE = ITEMS.registerItem("fire_resistance_ability", FireResistanceAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> VOID_IMMUNITY = ITEMS.registerItem("void_immunity_ability", VoidImmunityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PASSIVE_LUCK = ITEMS.registerItem("passive_luck_ability", PassiveLuckAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CALAMITY_ATTRACTION = ITEMS.registerItem("passive_calamity_attraction_ability", PassiveCalamityAttraction::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PASSIVE_LUCK_ACCUMULATION = ITEMS.registerItem("passive_luck_accumulation_ability", PassiveLuckAccumulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PAPER_DAGGERS = ITEMS.registerItem("paper_dagger_ability", PaperDaggersAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PUPPETEERING_ENHANCEMENTS = ITEMS.registerItem("puppeteering_enhancements_ability", PuppeteeringEnhancementsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> NOCTURNALITY = ITEMS.registerItem("nocturnality_ability", NocturnalityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FEATHER_FALL = ITEMS.registerItem("feather_fall_ability", FeatherFallAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> RIPTIDE = ITEMS.registerItem("riptide_ability", RiptideAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static void registerAbilities(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
