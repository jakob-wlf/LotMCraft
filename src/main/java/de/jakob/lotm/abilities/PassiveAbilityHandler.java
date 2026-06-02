package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.passives.FireResistanceAbyssAbility;
import de.jakob.lotm.abilities.abyss.passives.PhysicalEnhancementsAbyssAbility;
import de.jakob.lotm.abilities.abyss.passives.WordImmunityAbility;
import de.jakob.lotm.abilities.darkness.passives.DarknessRevivalAbility;
import de.jakob.lotm.abilities.darkness.passives.NocturnalityAbility;
import de.jakob.lotm.abilities.darkness.passives.PhysicalEnhancementsDarknessAbility;
import de.jakob.lotm.abilities.death.passives.PhysicalEnhancementsDeathAbility;
import de.jakob.lotm.abilities.death.passives.ReincarnationAbility;
import de.jakob.lotm.abilities.death.passives.SolarSensitivityAbility;
import de.jakob.lotm.abilities.death.passives.UndeadIgnoranceAbility;
import de.jakob.lotm.abilities.demoness.passives.BloodLossAbility;
import de.jakob.lotm.abilities.demoness.passives.FeatherFallAbility;
import de.jakob.lotm.abilities.demoness.passives.MirrorRevivalAbility;
import de.jakob.lotm.abilities.demoness.passives.PhysicalEnhancementsDemonessAbility;
import de.jakob.lotm.abilities.door.passives.PhysicalEnhancementsDoorAbility;
import de.jakob.lotm.abilities.door.passives.SpiritWorldAwarenessAbility;
import de.jakob.lotm.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.abilities.error.passives.PassiveTheftAbility;
import de.jakob.lotm.abilities.error.passives.PhysicalEnhancementsErrorAbility;
import de.jakob.lotm.abilities.fool.passives.MiracleOfResurrectionAbility;
import de.jakob.lotm.abilities.justiciar.passives.ChaosHuntingAbility;
import de.jakob.lotm.abilities.justiciar.passives.EnhancedMentalAttributesAbility;
import de.jakob.lotm.abilities.justiciar.passives.OrderJusticiarAbility;
import de.jakob.lotm.abilities.justiciar.passives.PhysicalEnhancementsJusticiarAbility;
import de.jakob.lotm.abilities.fool.passives.PaperDaggersAbility;
import de.jakob.lotm.abilities.fool.passives.PhysicalEnhancementsFoolAbility;
import de.jakob.lotm.abilities.fool.passives.PuppeteeringEnhancementsAbility;
import de.jakob.lotm.abilities.mother.passives.PhysicalEnhancementsMotherAbility;
import de.jakob.lotm.abilities.red_priest.passive.FireResistanceAbility;
import de.jakob.lotm.abilities.red_priest.passive.FlamingHitAbility;
import de.jakob.lotm.abilities.red_priest.passive.PhysicalEnhancementsRedPriestAbility;
import de.jakob.lotm.abilities.sun.passives.PhysicalEnhancementsSunAbility;
import de.jakob.lotm.abilities.tyrant.passives.LightningArrowAbility;
import de.jakob.lotm.abilities.tyrant.passives.PhysicalEnhancementsTyrantAbility;
import de.jakob.lotm.abilities.tyrant.passives.RiptideAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.abilities.visionary.passives.PhysicalEnhancementsVisionaryAbility;
import de.jakob.lotm.abilities.visionary.passives.PureIdealism;
import de.jakob.lotm.abilities.visionary.passives.VirtualAvatarAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveCalamityAttraction;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveLuckAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.passives.PhysicalEnhancementsWheelOfFortuneAbility;
import de.jakob.lotm.events.BeyonderDataTickHandler;
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
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_ERROR = ITEMS.registerItem("physical_enhancements_error_ability", PhysicalEnhancementsErrorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DARKNESS = ITEMS.registerItem("physical_enhancements_darkness_ability", PhysicalEnhancementsDarknessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DOOR = ITEMS.registerItem("physical_enhancements_door_ability", PhysicalEnhancementsDoorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_MOTHER = ITEMS.registerItem("physical_enhancements_mother_ability", PhysicalEnhancementsMotherAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_VISIONARY = ITEMS.registerItem("physical_enhancements_visionary_ability", PhysicalEnhancementsVisionaryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_WHEEL_OF_FORTUNE = ITEMS.registerItem("physical_enhancements_wheel_of_fortune_ability", PhysicalEnhancementsWheelOfFortuneAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_JUSTICIAR = ITEMS.registerItem("physical_enhancements_justiciar_ability", PhysicalEnhancementsJusticiarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> ORDER_JUSTICIAR = ITEMS.registerItem("order_justiciar_ability", OrderJusticiarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ENHANCED_MENTAL_ATTRIBUTES_JUSTICIAR = ITEMS.registerItem("enhanced_mental_attributes_justiciar_ability", EnhancedMentalAttributesAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CHAOS_HUNTING_JUSTICIAR = ITEMS.registerItem("chaos_hunting_justiciar_ability", ChaosHuntingAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));


    public static final DeferredItem<Item> FLAMING_HIT = ITEMS.registerItem("flaming_hit_ability", FlamingHitAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> FIRE_RESISTANCE = ITEMS.registerItem("fire_resistance_ability", FireResistanceAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FIRE_RESISTANCE_ABYSS = ITEMS.registerItem("fire_resistance_abyss_ability", FireResistanceAbyssAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> WORD_IMMUNITY_ABYSS = ITEMS.registerItem("word_immunity_abyss_ability", WordImmunityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> SPIRIT_WORLD_AWARENESS = ITEMS.registerItem("spirit_world_awareness_ability", SpiritWorldAwarenessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> VOID_IMMUNITY = ITEMS.registerItem("void_immunity_ability", VoidImmunityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PASSIVE_LUCK = ITEMS.registerItem("passive_luck_ability", PassiveLuckAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CALAMITY_ATTRACTION = ITEMS.registerItem("passive_calamity_attraction_ability", PassiveCalamityAttraction::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PASSIVE_LUCK_ACCUMULATION = ITEMS.registerItem("passive_luck_accumulation_ability", PassiveLuckAccumulationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PAPER_DAGGERS = ITEMS.registerItem("paper_dagger_ability", PaperDaggersAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PUPPETEERING_ENHANCEMENTS = ITEMS.registerItem("puppeteering_enhancements_ability", PuppeteeringEnhancementsAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MIRACLE_OF_RESURRECTION = ITEMS.registerItem("miracle_of_resurrection_ability", MiracleOfResurrectionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> NOCTURNALITY = ITEMS.registerItem("nocturnality_ability", NocturnalityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> DARKNESS_REVIVAL = ITEMS.registerItem("darkness_revival_ability", DarknessRevivalAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    
    public static final DeferredItem<Item> PASSIVE_THEFT = ITEMS.registerItem("passive_theft_ability", PassiveTheftAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> FEATHER_FALL = ITEMS.registerItem("feather_fall_ability", FeatherFallAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> BLOOD_LOSS = ITEMS.registerItem("blood_loss_ability", BloodLossAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MIRROR_REVIVAL = ITEMS.registerItem("mirror_revival_ability", MirrorRevivalAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> LIGHTNING_ARROW = ITEMS.registerItem("lightning_arrow_ability", LightningArrowAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RIPTIDE = ITEMS.registerItem("riptide_ability", RiptideAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DEATH = ITEMS.registerItem("physical_enhancements_death_ability", PhysicalEnhancementsDeathAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> UNDEAD_IGNORANCE = ITEMS.registerItem("undead_ignorance_ability", UndeadIgnoranceAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SOLAR_SENSITIVITY = ITEMS.registerItem("solar_sensitivity_ability", SolarSensitivityAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> REINCARNATION = ITEMS.registerItem("reincarnation_ability", ReincarnationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> VIRTUAL_AVATAR = ITEMS.registerItem("virtual_avatar_ability", VirtualAvatarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> META_AWARENESS = ITEMS.registerItem("meta_awareness_ability", MetaAwarenessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PURE_IDEALISM = ITEMS.registerItem("pure_idealism_ability", PureIdealism::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static void registerAbilities(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
