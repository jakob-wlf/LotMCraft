package de.jakob.lotm.beyonders.abilities.core;

import de.jakob.lotm.beyonders.abilities.abyss.passives.CriminalProficiencyAbility;
import de.jakob.lotm.beyonders.abilities.common.passives.ElevatedConcealmentAbility;
import de.jakob.lotm.beyonders.abilities.common.passives.ElevatedDivinationAbility;
import de.jakob.lotm.beyonders.abilities.common.passives.FateResistanceAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.MiracleOfResurrectionAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.PaperDaggersAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.PhysicalEnhancementsFoolAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.PuppeteeringEnhancementsAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.Passives.PhysicalEnhancementsTwilightGiantAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.Passives.WeaponMasteryPassiveAbility;
import de.jakob.lotm.beyonders.abilities.abyss.passives.FireResistanceAbyssAbility;
import de.jakob.lotm.beyonders.abilities.abyss.passives.PhysicalEnhancementsAbyssAbility;
import de.jakob.lotm.beyonders.abilities.abyss.passives.WordImmunityAbility;
import de.jakob.lotm.beyonders.abilities.black_emperor.passives.PhysicalEnhancementsBlackEmperorAbility;
import de.jakob.lotm.beyonders.abilities.darkness.passives.DarknessRevivalAbility;
import de.jakob.lotm.beyonders.abilities.darkness.passives.NocturnalityAbility;
import de.jakob.lotm.beyonders.abilities.darkness.passives.PhysicalEnhancementsDarknessAbility;
import de.jakob.lotm.beyonders.abilities.death.passives.PhysicalEnhancementsDeathAbility;
import de.jakob.lotm.beyonders.abilities.death.passives.ReincarnationAbility;
import de.jakob.lotm.beyonders.abilities.death.passives.SolarSensitivityAbility;
import de.jakob.lotm.beyonders.abilities.death.passives.UndeadIgnoranceAbility;
import de.jakob.lotm.beyonders.abilities.demoness.passives.BloodLossAbility;
import de.jakob.lotm.beyonders.abilities.demoness.passives.FeatherFallAbility;
import de.jakob.lotm.beyonders.abilities.demoness.passives.MirrorRevivalAbility;
import de.jakob.lotm.beyonders.abilities.demoness.passives.PhysicalEnhancementsDemonessAbility;
import de.jakob.lotm.beyonders.abilities.door.passives.PhysicalEnhancementsDoorAbility;
import de.jakob.lotm.beyonders.abilities.door.passives.SpiritWorldAwarenessAbility;
import de.jakob.lotm.beyonders.abilities.door.passives.VoidImmunityAbility;
import de.jakob.lotm.beyonders.abilities.error.passives.PassiveTheftAbility;
import de.jakob.lotm.beyonders.abilities.error.passives.PhysicalEnhancementsErrorAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.AcrobaticsAbility;
import de.jakob.lotm.beyonders.abilities.fool.passives.DangerPremonitionAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.passives.ChaosHuntingAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.passives.EnhancedMentalAttributesAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.passives.OrderJusticiarAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.passives.PhysicalEnhancementsJusticiarAbility;
import de.jakob.lotm.beyonders.abilities.mother.passives.PhysicalEnhancementsMotherAbility;
import de.jakob.lotm.beyonders.abilities.red_priest.passive.FireResistanceAbility;
import de.jakob.lotm.beyonders.abilities.red_priest.passive.FlamingHitAbility;
import de.jakob.lotm.beyonders.abilities.red_priest.passive.PhysicalEnhancementsRedPriestAbility;
import de.jakob.lotm.beyonders.abilities.sun.passives.PhysicalEnhancementsSunAbility;
import de.jakob.lotm.beyonders.abilities.tyrant.passives.LightningArrowAbility;
import de.jakob.lotm.beyonders.abilities.tyrant.passives.PhysicalEnhancementsTyrantAbility;
import de.jakob.lotm.beyonders.abilities.tyrant.passives.RiptideAbility;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.beyonders.abilities.visionary.passives.PhysicalEnhancementsVisionaryAbility;
import de.jakob.lotm.beyonders.abilities.visionary.passives.PureIdealism;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveCalamityAttraction;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.AbsolutePerceptionAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.InnateSpiritVisionAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.LuckPerceptionAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.MercuryBodyAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PhysicalEnhancementsWheelOfFortuneAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.RebootAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.HashSet;
import java.util.List;

public class PassiveAbilityHandler {

    public static HashSet<PassiveAbility> passiveAbilities = new HashSet<>();

    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_RED_PRIEST = ITEMS.registerItem("physical_enhancements_red_priest_ability", PhysicalEnhancementsRedPriestAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_SUN = ITEMS.registerItem("physical_enhancements_sun_ability", PhysicalEnhancementsSunAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_TYRANT = ITEMS.registerItem("physical_enhancements_tyrant_ability", PhysicalEnhancementsTyrantAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DEMONESS = ITEMS.registerItem("physical_enhancements_demoness_ability", PhysicalEnhancementsDemonessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_ABYSS = ITEMS.registerItem("physical_enhancements_abyss_ability", PhysicalEnhancementsAbyssAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_FOOL = ITEMS.registerItem("physical_enhancements_fool_ability", PhysicalEnhancementsFoolAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_ERROR = ITEMS.registerItem("physical_enhancements_error_ability", PhysicalEnhancementsErrorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DARKNESS = ITEMS.registerItem("physical_enhancements_darkness_ability", PhysicalEnhancementsDarknessAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_DOOR = ITEMS.registerItem("physical_enhancements_door_ability", PhysicalEnhancementsDoorAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_MOTHER = ITEMS.registerItem("physical_enhancements_mother_ability", PhysicalEnhancementsMotherAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_VISIONARY = ITEMS.registerItem("physical_enhancements_visionary_ability", PhysicalEnhancementsVisionaryAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_WHEEL_OF_FORTUNE = ITEMS.registerItem("physical_enhancements_wheel_of_fortune_ability", PhysicalEnhancementsWheelOfFortuneAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_JUSTICIAR = ITEMS.registerItem("physical_enhancements_justiciar_ability", PhysicalEnhancementsJusticiarAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> PHYSICAL_ENHANCEMENTS_Twilight_Giant = ITEMS.registerItem("physical_enhancements_twilight_giant_ability", PhysicalEnhancementsTwilightGiantAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> LUCK_PERCEPTION = ITEMS.registerItem("luck_perception_ability", LuckPerceptionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> MERCURY_BODY = ITEMS.registerItem("mercury_body_ability", MercuryBodyAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    //public static final DeferredItem<Item> ABSOLUTE_PERCEPTION = ITEMS.registerItem("absolute_perception_ability", AbsolutePerceptionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    //public static final DeferredItem<Item> INNATE_SPIRIT_VISION = ITEMS.registerItem("innate_spirit_vision_ability", InnateSpiritVisionAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //public static final DeferredItem<Item> REBOOT = ITEMS.registerItem("reboot_ability", RebootAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    //public static final DeferredItem<Item> WEAPON_MASTERY_PASSIVE = ITEMS.registerItem("weapon_mastery_ability", WeaponMasteryPassiveAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    //
    //// Sefirot passives
    //public static final DeferredItem<Item> ELEVATED_DIVINATION_PASSIVE = ITEMS.registerItem("elevated_divination_ability", ElevatedDivinationAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    //public static final DeferredItem<Item> ELEVATED_CONCEALMENT_PASSIVE = ITEMS.registerItem("elevated_concealment_ability", ElevatedConcealmentAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    //
    //// General tier passives
    //public static final DeferredItem<Item> FATE_RESISTANCE_PASSIVE = ITEMS.registerItem("fate_resistance_ability", FateResistanceAbility::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));


    public static void registerPassiveAbility() {
        // Physical enhancements
        passiveAbilities.add(new PhysicalEnhancementsRedPriestAbility("physical_enhancements_red_priest_ability"));
        passiveAbilities.add(new PhysicalEnhancementsSunAbility("physical_enhancements_sun_ability"));
        passiveAbilities.add(new PhysicalEnhancementsTyrantAbility("physical_enhancements_tyrant_ability"));
        passiveAbilities.add(new PhysicalEnhancementsDemonessAbility("physical_enhancements_demoness_ability"));
        passiveAbilities.add(new PhysicalEnhancementsAbyssAbility("physical_enhancements_abyss_ability"));
        passiveAbilities.add(new PhysicalEnhancementsFoolAbility("physical_enhancements_fool_ability"));
        passiveAbilities.add(new PhysicalEnhancementsErrorAbility("physical_enhancements_error_ability"));
        passiveAbilities.add(new PhysicalEnhancementsDarknessAbility("physical_enhancements_darkness_ability"));
        passiveAbilities.add(new PhysicalEnhancementsDoorAbility("physical_enhancements_door_ability"));
        passiveAbilities.add(new PhysicalEnhancementsMotherAbility("physical_enhancements_mother_ability"));
        passiveAbilities.add(new PhysicalEnhancementsVisionaryAbility("physical_enhancements_visionary_ability"));
        passiveAbilities.add(new PhysicalEnhancementsWheelOfFortuneAbility("physical_enhancements_wheel_of_fortune_ability"));
        passiveAbilities.add(new PhysicalEnhancementsJusticiarAbility("physical_enhancements_justiciar_ability"));
        passiveAbilities.add(new PhysicalEnhancementsBlackEmperorAbility("physical_enhancements_black_emperor_ability"));
        passiveAbilities.add(new PhysicalEnhancementsDeathAbility("physical_enhancements_death_ability"));

        // Justiciar
        passiveAbilities.add(new OrderJusticiarAbility("order_justiciar_ability"));
        passiveAbilities.add(new EnhancedMentalAttributesAbility("enhanced_mental_attributes_justiciar_ability"));
        passiveAbilities.add(new ChaosHuntingAbility("chaos_hunting_justiciar_ability"));

        // Red priest
        passiveAbilities.add(new FlamingHitAbility("flaming_hit_ability"));
        passiveAbilities.add(new FireResistanceAbility("fire_resistance_ability"));

        // Abyss
        passiveAbilities.add(new CriminalProficiencyAbility("criminal_proficiency_ability"));
        passiveAbilities.add(new FireResistanceAbyssAbility("fire_resistance_abyss_ability"));
        passiveAbilities.add(new WordImmunityAbility("word_immunity_abyss_ability"));

        // Door
        passiveAbilities.add(new SpiritWorldAwarenessAbility("spirit_world_awareness_ability"));
        passiveAbilities.add(new VoidImmunityAbility("void_immunity_ability"));

        // Wheel of fortune
        passiveAbilities.add(new PassiveLuckAbility("passive_luck_ability"));
        passiveAbilities.add(new PassiveCalamityAttraction("passive_calamity_attraction_ability"));
        passiveAbilities.add(new PassiveLuckAccumulationAbility("passive_luck_accumulation_ability"));

        // Fool
        passiveAbilities.add(new PaperDaggersAbility("paper_dagger_ability"));
        passiveAbilities.add(new AcrobaticsAbility("acrobatics_ability"));
        passiveAbilities.add(new DangerPremonitionAbility("danger_premonition_ability"));
        passiveAbilities.add(new PuppeteeringEnhancementsAbility("puppeteering_enhancements_ability"));
        passiveAbilities.add(new MiracleOfResurrectionAbility("miracle_of_resurrection_ability"));

        // Darkness
        passiveAbilities.add(new NocturnalityAbility("nocturnality_ability"));
        passiveAbilities.add(new DarknessRevivalAbility("darkness_revival_ability"));

        // Error
        passiveAbilities.add(new PassiveTheftAbility("passive_theft_ability"));

        // Demoness
        passiveAbilities.add(new FeatherFallAbility("feather_fall_ability"));
        passiveAbilities.add(new BloodLossAbility("blood_loss_ability"));
        passiveAbilities.add(new MirrorRevivalAbility("mirror_revival_ability"));

        // Tyrant
        passiveAbilities.add(new LightningArrowAbility("lightning_arrow_ability"));
        passiveAbilities.add(new RiptideAbility("riptide_ability"));

        // Death
        passiveAbilities.add(new UndeadIgnoranceAbility("undead_ignorance_ability"));
        passiveAbilities.add(new SolarSensitivityAbility("solar_sensitivity_ability"));
        passiveAbilities.add(new ReincarnationAbility("reincarnation_ability"));

        // Visionary
        passiveAbilities.add(new MetaAwarenessAbility("meta_awareness_ability"));
        passiveAbilities.add(new PureIdealism("pure_idealism_ability"));
    }




    public static PassiveAbility getById(String id) {
        for (PassiveAbility ability : passiveAbilities) {
            if (ability.getId().equals(id)) {
                return ability;
            }
        }
        return null;
    }

    public List<PassiveAbility> getPassiveAbilitiesForEntity(LivingEntity entity) {
        return passiveAbilities.stream()
                .filter(ability -> ability.shouldApplyTo(entity))
                .toList();
    }
}