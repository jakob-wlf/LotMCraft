package de.jakob.lotm.potions;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PotionItemHandler {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LOTMCraft.MOD_ID);


    public static DeferredItem<Item> SEER_POTION = ITEMS.registerItem("seer_potion", properties ->
            new BeyonderPotion(properties, 9, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CLOWN_POTION = ITEMS.registerItem("clown_potion", properties ->
                    new BeyonderPotion(properties, 8, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MAGICIAN_POTION = ITEMS.registerItem("magician_potion", properties ->
                    new BeyonderPotion(properties, 7, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> FACELESS_POTION = ITEMS.registerItem("faceless_potion", properties ->
                    new BeyonderPotion(properties, 6, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MARIONETTIST_POTION = ITEMS.registerItem("marionettist_potion", properties ->
                    new BeyonderPotion(properties, 5, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> BIZARRO_SORCERER_POTION = ITEMS.registerItem("bizarro_sorcerer_potion", properties ->
                    new BeyonderPotion(properties, 4, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SCHOLAR_OF_YORE_POTION = ITEMS.registerItem("scholar_of_yore_potion", properties ->
                    new BeyonderPotion(properties, 3, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MIRACLE_INVOKER_POTION = ITEMS.registerItem("miracle_invoker_potion", properties ->
                    new BeyonderPotion(properties, 2, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> ATTENDANT_OF_MYSTERIES_POTION = ITEMS.registerItem("attendant_of_mysteries_potion", properties ->
                    new BeyonderPotion(properties, 1, "fool"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> BARD_POTION = ITEMS.registerItem("bard_potion", properties ->
                    new BeyonderPotion(properties, 9, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> LIGHT_SUPPLICANT_POTION = ITEMS.registerItem("light_supplicant_potion", properties ->
                    new BeyonderPotion(properties, 8, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SOLAR_HIGH_PRIEST_POTION = ITEMS.registerItem("solar_high_priest_potion", properties ->
                    new BeyonderPotion(properties, 7, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NOTARY_POTION = ITEMS.registerItem("notary_potion", properties ->
                    new BeyonderPotion(properties, 6, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PRIEST_OF_LIGHT_POTION = ITEMS.registerItem("priest_of_light_potion", properties ->
                    new BeyonderPotion(properties, 5, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> UNSHADOWED = ITEMS.registerItem("unshadowed_potion", properties ->
                    new BeyonderPotion(properties, 4, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> JUSTICE_MENTOR_POTION = ITEMS.registerItem("justice_mentor_potion", properties ->
                    new BeyonderPotion(properties, 3, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> LIGHTSEEKER_POTION = ITEMS.registerItem("lightseeker_potion", properties ->
                    new BeyonderPotion(properties, 2, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WHITE_ANGEL_POTION = ITEMS.registerItem("white_angel_potion", properties ->
                    new BeyonderPotion(properties, 1, "sun"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> SAILOR_POTION = ITEMS.registerItem("sailor_potion", properties ->
                    new BeyonderPotion(properties, 9, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> FOLK_OF_RAGE_POTION = ITEMS.registerItem("folk_of_rage_potion", properties ->
                    new BeyonderPotion(properties, 8, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SEAFARER_POTION = ITEMS.registerItem("seafarer_potion", properties ->
                    new BeyonderPotion(properties, 7, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WIND_BLESSED_POTION = ITEMS.registerItem("wind_blessed_potion", properties ->
                    new BeyonderPotion(properties, 6, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> OCEAN_SONGSTER_POTION = ITEMS.registerItem("ocean_songster_potion", properties ->
                    new BeyonderPotion(properties, 5, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CATACLYSMIC_INTERRER_POTION = ITEMS.registerItem("cataclysmic_interrer_potion", properties ->
                    new BeyonderPotion(properties, 4, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SEA_KING_POTION = ITEMS.registerItem("sea_king_potion", properties ->
                    new BeyonderPotion(properties, 3, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CALAMITY_POTION = ITEMS.registerItem("calamity_potion", properties ->
                    new BeyonderPotion(properties, 2, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> THUNDER_GOD_POTION = ITEMS.registerItem("thunder_god_potion", properties ->
                    new BeyonderPotion(properties, 1, "tyrant"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));



    public static DeferredItem<Item> HUNTER_POTION = ITEMS.registerItem("hunter_potion", properties ->
                    new BeyonderPotion(properties, 9, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PROVOKER_POTION = ITEMS.registerItem("provoker_potion", properties ->
                    new BeyonderPotion(properties, 8, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PYROMANIAC_POTION = ITEMS.registerItem("pyromaniac_potion", properties ->
                    new BeyonderPotion(properties, 7, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CONSPIRER_POTION = ITEMS.registerItem("conspirer_potion", properties ->
                    new BeyonderPotion(properties, 6, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> REAPER_POTION = ITEMS.registerItem("reaper_potion", properties ->
                    new BeyonderPotion(properties, 5, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> IRON_BLOODED_KNIGHT_POTION = ITEMS.registerItem("iron_blooded_knight_potion", properties ->
                    new BeyonderPotion(properties, 4, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WAR_BISHOP_POTION = ITEMS.registerItem("war_bishop_potion", properties ->
                    new BeyonderPotion(properties, 3, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WEATHER_WARLOCK_POTION = ITEMS.registerItem("weather_warlock_potion", properties ->
                    new BeyonderPotion(properties, 2, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CONQUEROR_POTION = ITEMS.registerItem("conqueror_potion", properties ->
                    new BeyonderPotion(properties, 1, "red_priest"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> APPRENTICE_POTION = ITEMS.registerItem("apprentice_potion", properties ->
                    new BeyonderPotion(properties, 9, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TRICKMASTER_POTION = ITEMS.registerItem("trickmaster_potion", properties ->
                    new BeyonderPotion(properties, 8, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> ASTROLOGER_POTION = ITEMS.registerItem("astrologer_potion", properties ->
                    new BeyonderPotion(properties, 7, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SCRIBE_POTION = ITEMS.registerItem("scribe_potion", properties ->
                    new BeyonderPotion(properties, 6, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TRAVELER_POTION = ITEMS.registerItem("traveler_potion", properties ->
                    new BeyonderPotion(properties, 5, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SECRETS_SORCERER_POTION = ITEMS.registerItem("secrets_sorcerer_potion", properties ->
                    new BeyonderPotion(properties, 4, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WANDERER_POTION = ITEMS.registerItem("wanderer_potion", properties ->
                    new BeyonderPotion(properties, 3, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PLANESWALKER_POTION = ITEMS.registerItem("planeswalker_potion", properties ->
                    new BeyonderPotion(properties, 2, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> KEY_OF_STARS_POTION = ITEMS.registerItem("key_of_stars_potion", properties ->
                    new BeyonderPotion(properties, 1, "door"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));



    public static DeferredItem<Item> CRIMINAL_POTION = ITEMS.registerItem("criminal_potion", properties ->
                    new BeyonderPotion(properties, 9, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> UNWINGED_ANGEL_POTION = ITEMS.registerItem("unwinged_angel_potion", properties ->
                    new BeyonderPotion(properties, 8, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SERIAL_KILLER_POTION = ITEMS.registerItem("serial_killer_potion", properties ->
                    new BeyonderPotion(properties, 7, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEVIL_POTION = ITEMS.registerItem("devil_potion", properties ->
                    new BeyonderPotion(properties, 6, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DESIRE_APOSTLE_POTION = ITEMS.registerItem("desire_apostle_potion", properties ->
                    new BeyonderPotion(properties, 5, "abyss"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> SLEEPLESS_POTION = ITEMS.registerItem("sleepless_potion", properties ->
                    new BeyonderPotion(properties, 9, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MIDNIGHT_POET_POTION = ITEMS.registerItem("midnight_poet_potion", properties ->
                    new BeyonderPotion(properties, 8, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NIGHTMARE_POTION = ITEMS.registerItem("nightmare_potion", properties ->
                    new BeyonderPotion(properties, 7, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SOUL_ASSURER_POTION = ITEMS.registerItem("soul_assurer_potion", properties ->
                    new BeyonderPotion(properties, 6, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SPIRIT_WARLOCK_POTION = ITEMS.registerItem("spirit_warlock_potion", properties ->
                    new BeyonderPotion(properties, 5, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NIGHTWATCHER_POTION = ITEMS.registerItem("nightwatcher_potion", properties ->
                    new BeyonderPotion(properties, 4, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> HORROR_BISHOP_POTION = ITEMS.registerItem("horror_bishop_potion", properties ->
                    new BeyonderPotion(properties, 3, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SERVANT_OF_CONCEALMENT_POTION = ITEMS.registerItem("servant_of_concealment_potion", properties ->
                    new BeyonderPotion(properties, 2, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> KNIGHT_OF_MISFORTUNE_POTION = ITEMS.registerItem("knight_of_misfortune_potion", properties ->
                    new BeyonderPotion(properties, 1, "darkness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> PLANTER_POTION = ITEMS.registerItem("planter_potion", properties ->
                    new BeyonderPotion(properties, 9, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DOCTOR_POTION = ITEMS.registerItem("doctor_potion", properties ->
                    new BeyonderPotion(properties, 8, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> HARVEST_PRIEST_POTION = ITEMS.registerItem("harvest_priest_potion", properties ->
                    new BeyonderPotion(properties, 7, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> BIOLOGIST_POTION = ITEMS.registerItem("biologist_potion", properties ->
                    new BeyonderPotion(properties, 6, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DRUID_POTION = ITEMS.registerItem("druid_potion", properties ->
                    new BeyonderPotion(properties, 5, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CLASSICAL_ALCHEMIST_POTION = ITEMS.registerItem("classical_alchemist_potion", properties ->
                    new BeyonderPotion(properties, 4, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PALLBEARER_POTION = ITEMS.registerItem("pallbearer_potion", properties ->
                    new BeyonderPotion(properties, 3, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DESOLATE_MATRIARCH_POTION = ITEMS.registerItem("desolate_matriarch_potion", properties ->
                    new BeyonderPotion(properties, 2, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> NATUREWALKER_POTION = ITEMS.registerItem("naturewalker_potion", properties ->
                    new BeyonderPotion(properties, 1, "mother"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> ASSASSIN_POTION = ITEMS.registerItem("assassin_potion", properties ->
                    new BeyonderPotion(properties, 9, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> INSTIGATOR_POTION = ITEMS.registerItem("instigator_potion", properties ->
                    new BeyonderPotion(properties, 8, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WITCH_POTION = ITEMS.registerItem("witch_potion", properties ->
                    new BeyonderPotion(properties, 7, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_PLEASURE_POTION = ITEMS.registerItem("demoness_of_pleasure_potion", properties ->
                    new BeyonderPotion(properties, 6, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_AFFLICTION_POTION = ITEMS.registerItem("demoness_of_affliction_potion", properties ->
                    new BeyonderPotion(properties, 5, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_DESPAIR_POTION = ITEMS.registerItem("demoness_of_despair_potion", properties ->
                    new BeyonderPotion(properties, 4, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_UNAGING_POTION = ITEMS.registerItem("demoness_of_unaging_potion", properties ->
                    new BeyonderPotion(properties, 3, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_CATASTROPHE_POTION = ITEMS.registerItem("demoness_of_catastrophe_potion", properties ->
                    new BeyonderPotion(properties, 2, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DEMONESS_OF_APOCALYPSE_POTION = ITEMS.registerItem("demoness_of_apocalypse_potion", properties ->
                    new BeyonderPotion(properties, 1, "demoness"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> SPECTATOR_POTION = ITEMS.registerItem("spectator_potion", properties ->
                    new BeyonderPotion(properties, 9, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TELEPATHIST_POTION = ITEMS.registerItem("telepathist_potion", properties ->
                    new BeyonderPotion(properties, 8, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PSYCHIATRIST_POTION = ITEMS.registerItem("psychiatrist_potion", properties ->
                    new BeyonderPotion(properties, 7, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> HYPNOTIST_POTION = ITEMS.registerItem("hypnotist_potion", properties ->
                    new BeyonderPotion(properties, 6, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DREAMWALKER_POTION = ITEMS.registerItem("dreamwalker_potion", properties ->
                    new BeyonderPotion(properties, 5, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MANIPULATOR_POTION = ITEMS.registerItem("manipulator_potion", properties ->
                    new BeyonderPotion(properties, 4, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DREAM_WEAVER_POTION = ITEMS.registerItem("dream_weaver_potion", properties ->
                    new BeyonderPotion(properties, 3, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DISCERNER_POTION = ITEMS.registerItem("discerner_potion", properties ->
                    new BeyonderPotion(properties, 2, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> AUTHOR_POTION = ITEMS.registerItem("author_potion", properties ->
                    new BeyonderPotion(properties, 1, "visionary"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> MARAUDER_POTION = ITEMS.registerItem("marauder_potion", properties ->
                    new BeyonderPotion(properties, 9, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SWINDLER_POTION = ITEMS.registerItem("swindler_potion", properties ->
                    new BeyonderPotion(properties, 8, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CRYPTOLOGITS_POTION = ITEMS.registerItem("cryptologist_potion", properties ->
                    new BeyonderPotion(properties, 7, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PROMETHEUS_POTION = ITEMS.registerItem("prometheus_potion", properties ->
                    new BeyonderPotion(properties, 6, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> DREAM_STEALER_POTION = ITEMS.registerItem("dream_stealer_potion", properties ->
                    new BeyonderPotion(properties, 5, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> PARASITE_POTION = ITEMS.registerItem("parasite_potion", properties ->
                    new BeyonderPotion(properties, 4, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MENTOR_OF_DECEIT_POTION = ITEMS.registerItem("mentor_of_deceit_potion", properties ->
                    new BeyonderPotion(properties, 3, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> TROJAN_HORSE_OF_DESTINY_POTION = ITEMS.registerItem("trojan_horse_of_destiny_potion", properties ->
                    new BeyonderPotion(properties, 2, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WORM_OF_TIME_POTION = ITEMS.registerItem("worm_of_time_potion", properties ->
                    new BeyonderPotion(properties, 1, "error"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));


    public static DeferredItem<Item> MONSTER_POTION = ITEMS.registerItem("monster_potion", properties ->
                    new BeyonderPotion(properties, 9, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> ROBOT_POTION = ITEMS.registerItem("robot_potion", properties ->
                    new BeyonderPotion(properties, 8, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> LUCKY_ONE_POTION = ITEMS.registerItem("lucky_one_potion", properties ->
                    new BeyonderPotion(properties, 7, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CALAMITY_PRIEST_POTION = ITEMS.registerItem("calamity_priest_potion", properties ->
                    new BeyonderPotion(properties, 6, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> WINNER_POTION = ITEMS.registerItem("winner_potion", properties ->
                    new BeyonderPotion(properties, 5, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> MISFORTUNE_MAGE_POTION = ITEMS.registerItem("misfortune_mage_potion", properties ->
                    new BeyonderPotion(properties, 4, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> CHAOSWALKER_POTION = ITEMS.registerItem("chaoswalker_potion", properties ->
                    new BeyonderPotion(properties, 3, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SOOTHSAYER_POTION = ITEMS.registerItem("soothsayer_potion", properties ->
                    new BeyonderPotion(properties, 2, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> SNAKE_OF_MERCURY_POTION = ITEMS.registerItem("snake_of_mercury_potion", properties ->
                    new BeyonderPotion(properties, 1, "wheel_of_fortune"),
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static DeferredItem<Item> EMPTY_BOTTLE = ITEMS.registerItem("empty_bottle", Item::new);


    public static void registerPotions(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static BeyonderPotion selectRandomPotion(Random random) {
        List<BeyonderPotion> potions = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .toList();

        if (potions.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<BeyonderPotion, Integer> potionWeights = new HashMap<>();
        int totalWeight = 0;

        for (BeyonderPotion potion : potions) {
            int weight = potion.getSequence() + 1; // Higher sequence = more common = higher weight
            potionWeights.put(potion, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<BeyonderPotion, Integer> entry : potionWeights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return potions.get(potions.size() - 1);
    }

    public static BeyonderPotion selectRandomPotionOfPathway(Random random, String pathway) {
        List<BeyonderPotion> potions = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .filter(i -> i.getPathway().equals(pathway))
                .toList();

        if (potions.isEmpty()) {
            return null;
        }

        // Calculate weights for each potion
        // Higher sequence = more common = higher weight
        // Weight formula: sequence + 1 makes sequence 9 -> weight 10, sequence 0 -> weight 1
        Map<BeyonderPotion, Integer> potionWeights = new HashMap<>();
        int totalWeight = 0;

        for (BeyonderPotion potion : potions) {
            int weight = potion.getSequence() + 1; // Higher sequence = more common = higher weight
            potionWeights.put(potion, weight);
            totalWeight += weight;
        }

        // Generate random number between 0 and totalWeight-1
        int randomValue = random.nextInt(totalWeight);

        // Find the selected potion based on cumulative weights
        int cumulativeWeight = 0;
        for (Map.Entry<BeyonderPotion, Integer> entry : potionWeights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }

        // Fallback (should never reach here with valid input)
        return potions.get(potions.size() - 1);
    }

    public static List<BeyonderPotion> selectAllPotionsOfPathway(String pathway) {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .filter(i -> i.getPathway().equals(pathway))
                .toList();
    }

    public static List<BeyonderPotion> selectAllPotions() {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .toList();
    }

    public static BeyonderPotion selectRandomPotionOfSequence(Random random, int sequence) {
        List<BeyonderPotion> recipes = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .filter(i -> i.getSequence() == sequence)
                .toList();

        if (recipes.isEmpty()) {
            return null;
        }

        return recipes.get(random.nextInt(recipes.size()));
    }

    public static BeyonderPotion selectPotionOfPathwayAndSequence(Random random, String pathway, int sequence) {
        List<BeyonderPotion> potions = ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof BeyonderPotion)
                .map(i -> ((BeyonderPotion) i))
                .filter(i -> i.getPathway().equals(pathway))
                .filter(i -> i.getSequence() == sequence)
                .toList();

        if (potions.isEmpty()) {
            return null;
        }

        return potions.get(random.nextInt(potions.size()));
    }

}
