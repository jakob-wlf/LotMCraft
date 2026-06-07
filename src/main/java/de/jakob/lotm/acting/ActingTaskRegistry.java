package de.jakob.lotm.acting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActingTaskRegistry {

    private static final Map<PathwaySequenceKey, List<ActingTask>> TASKS = new HashMap<>();

    public static void register(String pathway, int sequence, ActingTask task) {
        var key = new PathwaySequenceKey(pathway, sequence);
        TASKS.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
    }

    public static List<ActingTask> getTasksFor(String pathway, int sequence) {
        return TASKS.getOrDefault(new PathwaySequenceKey(pathway, sequence), List.of());
    }

    public static void init() {
        register("fool", 9, new EventActingTask("use_divination_ability", 0.2f, 20 * 10));
        register("fool", 9, new EventActingTask("use_spyglass", 0.02f, 20 * 8));
        register("fool", 9, new EventActingTask("use_compass", 0.02f, 20 * 8));
        register("fool", 9, new EventActingTask("read_written_book", 0.03f, 20 * 15));

        register("fool", 8, new EventActingTask("use_firework", 0.02f, 20));
        register("fool", 8, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));
        register("fool", 8, new EventActingTask("stand_at_height", 0.04f, 20 * 20));

        register("fool", 7, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
        register("fool", 7, new EventActingTask("use_flaming_jump_ability_while_low_health", 0.05f, 10));
        register("fool", 7, new EventActingTask("eat_golden_apple", 0.05f, 20 * 2));
        register("fool", 7, new EventActingTask("craft_painting", 0.03f, 20 * 30));

        register("fool", 6, new EventActingTask("use_shapeshifting_ability", 0.2f, 20 * 15));
        register("fool", 6, new EventActingTask("sneak_kill", 0.05f, 20 * 2));

        register("fool", 5, new EventActingTask("sneak_kill", 0.05f, 20 * 2));
        register("fool", 5, new EventActingTask("drink_invisibility_potion", 0.05f, 20 * 2));
        register("fool", 5, new EventActingTask("use_puppeteering_ability", 0.1f, 20 * 10));
        register("fool", 5, new EventActingTask("use_lead", 0.03f, 20 * 10));

        register("fool", 4, new EventActingTask("use_puppeteering_ability", 0.1f, 20 * 15));
        register("fool", 4, new EventActingTask("drink_invisibility_potion", 0.05f, 20 * 2));
        register("fool", 4, new EventActingTask("use_ender_pearl", 0.02f, 20 * 5));
        register("fool", 4, new EventActingTask("place_mob_head", 0.2f, 20 * 5));

        register("fool", 3, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("fool", 3, new EventActingTask("trade_with_villager", 0.2f, 20 * 20));
        register("fool", 3, new EventActingTask("read_written_book", 0.05f, 20 * 15));
        register("fool", 3, new EventActingTask("open_chiseled_bookshelf", 0.05f, 20 * 10));

        register("fool", 2, new EventActingTask("use_miracle_creation_ability", 0.3f, 20 * 20));
        register("fool", 2, new EventActingTask("trade_with_villager", 0.3f, 20 * 20));
        register("fool", 2, new EventActingTask("stand_in_sunlight", 0.04f, 20 * 30));

        register("fool", 1, new EventActingTask("place_soul_lantern", 0.02f, 20 * 2));
        register("fool", 1, new EventActingTask("eat_suspicious_stew", 0.02f, 20 * 2));
        register("fool", 1, new EventActingTask("kill_in_darkness", 0.2f, 20 * 2));
        register("fool", 1, new EventActingTask("stay_awake_through_night", 0.1f, 20 * 30));


        register("door", 9, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("door", 9, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("door", 9, new EventActingTask("use_cartography_table", 0.05f, 20 * 10));
        register("door", 9, new EventActingTask("read_written_book", 0.05f, 20 * 15));

        register("door", 8, new EventActingTask("use_note_block", 0.05f, 20 * 5));
        register("door", 8, new EventActingTask("use_firework", 0.02f, 20));
        register("door", 8, new EventActingTask("sneak_kill", 0.1f, 20 * 2));

        register("door", 7, new EventActingTask("use_compass", 0.05f, 20 * 8));
        register("door", 7, new EventActingTask("use_spyglass", 0.05f, 20 * 8));
        register("door", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("door", 7, new EventActingTask("stand_at_height", 0.05f, 20 * 20));

        register("door", 6, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("door", 6, new EventActingTask("craft_map", 0.15f, 20 * 15));
        register("door", 6, new EventActingTask("use_recording_ability", 0.2f, 20 * 15));
        register("door", 6, new EventActingTask("write_in_book", 0.05f, 20 * 15));

        register("door", 5, new EventActingTask("enter_nether", 0.2f, 20 * 30));
        register("door", 5, new EventActingTask("ride_boat", 0.05f, 20 * 10));
        register("door", 5, new EventActingTask("ride_minecart", 0.05f, 20 * 10));
        register("door", 5, new EventActingTask("use_travelers_door_ability", 0.2f, 20 * 10));

        register("door", 4, new EventActingTask("use_space_concealment_ability", 0.2f, 20 * 15));
        register("door", 4, new EventActingTask("use_cartography_table", 0.1f, 20 * 15));
        register("door", 4, new EventActingTask("stay_awake_through_night", 0.15f, 20 * 30));
        register("door", 4, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("door", 3, new EventActingTask("use_wandering_ability", 0.2f, 20 * 10));
        register("door", 3, new EventActingTask("enter_nether", 0.15f, 20 * 20));
        register("door", 3, new EventActingTask("catch_fish", 0.05f, 20 * 10));

        register("door", 2, new EventActingTask("enter_nether", 0.1f, 20 * 20));
        register("door", 2, new EventActingTask("enter_end", 0.1f, 20 * 20));
        register("door", 2, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("door", 2, new EventActingTask("craft_map", 0.1f, 20 * 15));

        register("door", 1, new EventActingTask("use_door_authority_ability", 0.3f, 20 * 20));
        register("door", 1, new EventActingTask("use_sealing_authority_ability", 0.3f, 20 * 20));
        register("door", 1, new EventActingTask("stay_awake_through_night", 0.1f, 20 * 30));
        register("door", 1, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));


        register("error", 9, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("error", 9, new EventActingTask("pickup_item", 0.05f, 20 * 5));
        register("error", 9, new EventActingTask("break_barrel", 0.05f, 20 * 5));
        register("error", 9, new EventActingTask("open_chest", 0.04f, 20 * 10));

        register("error", 8, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("error", 8, new EventActingTask("use_note_block", 0.05f, 20 * 5));
        register("error", 8, new EventActingTask("sneak_kill", 0.1f, 20 * 2));

        register("error", 7, new EventActingTask("use_lectern", 0.15f, 20 * 15));
        register("error", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("error", 7, new EventActingTask("use_decryption_ability", 0.2f, 20 * 15));
        register("error", 7, new EventActingTask("open_ender_chest", 0.05f, 20 * 10));

        register("error", 6, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("error", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("error", 6, new EventActingTask("use_ability_theft_ability", 0.2f, 20 * 20));

        register("error", 5, new EventActingTask("stay_awake_through_night", 0.2f, 20 * 30));
        register("error", 5, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("error", 5, new EventActingTask("linger_in_darkness", 0.08f, 20 * 30));

        register("error", 4, new EventActingTask("tame_animal", 0.1f, 20 * 10));
        register("error", 4, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("error", 4, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("error", 3, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("error", 3, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("error", 3, new EventActingTask("use_deceit_ability", 0.2f, 20 * 15));
        register("error", 3, new EventActingTask("write_in_book", 0.05f, 20 * 15));

        register("error", 2, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("error", 2, new EventActingTask("use_fate_siphoning_ability", 0.3f, 20 * 20));
        register("error", 2, new EventActingTask("pickup_rare_item", 0.05f, 20 * 30));

        register("error", 1, new EventActingTask("kill_untargeted_mob", 0.1f, 20 * 5));
        register("error", 1, new EventActingTask("use_time_manipulation_ability", 0.3f, 20 * 20));


        register("mother", 9, new EventActingTask("plant_crop", 0.2f, 20 * 15));
        register("mother", 9, new EventActingTask("plant_sapling", 0.1f, 20 * 10));
        register("mother", 9, new EventActingTask("harvest_crop", 0.15f, 20 * 10));

        register("mother", 8, new EventActingTask("feed_animal", 0.1f, 20 * 10));
        register("mother", 8, new EventActingTask("breed_animals", 0.15f, 20 * 20));
        register("mother", 8, new EventActingTask("use_healing_ability", 0.2f, 20 * 15));

        register("mother", 7, new EventActingTask("harvest_sweet_berries", 0.1f, 20 * 10));
        register("mother", 7, new EventActingTask("harvest_crop", 0.15f, 20 * 10));
        register("mother", 7, new EventActingTask("use_bonemeal", 0.1f, 20 * 8));

        register("mother", 6, new EventActingTask("breed_animals", 0.15f, 20 * 20));
        register("mother", 6, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("mother", 6, new EventActingTask("use_name_tag", 0.05f, 20 * 10));
        register("mother", 6, new EventActingTask("use_crossbreeding_ability", 0.2f, 20 * 20));

        register("mother", 5, new EventActingTask("feed_animal", 0.1f, 20 * 10));
        register("mother", 5, new EventActingTask("use_lead", 0.05f, 20 * 10));
        register("mother", 5, new EventActingTask("catch_fish", 0.1f, 20 * 10));
        register("mother", 5, new EventActingTask("stand_in_rain", 0.05f, 20 * 20));

        register("mother", 4, new EventActingTask("brew_potion", 0.15f, 20 * 20));
        register("mother", 4, new EventActingTask("smelt_item", 0.05f, 20 * 10));
        register("mother", 4, new EventActingTask("use_mutation_creation_ability", 0.2f, 20 * 20));

        register("mother", 3, new EventActingTask("place_mob_head", 0.2f, 20 * 10));
        register("mother", 3, new EventActingTask("mine_ore", 0.05f, 20 * 10));
        register("mother", 3, new EventActingTask("use_life_deprivation_ability", 0.2f, 20 * 20));
        register("mother", 3, new EventActingTask("kill_passive_mob", 0.05f, 20 * 5));

        register("mother", 2, new EventActingTask("plant_sapling", 0.1f, 20 * 15));
        register("mother", 2, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("mother", 2, new EventActingTask("use_blooming_area_ability", 0.3f, 20 * 20));

        register("mother", 1, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("mother", 1, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("mother", 1, new EventActingTask("use_wrath_of_nature_ability", 0.3f, 20 * 20));
        register("mother", 1, new EventActingTask("stand_in_rain", 0.05f, 20 * 20));


        register("red_priest", 9, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("red_priest", 9, new EventActingTask("catch_fish", 0.05f, 20 * 10));
        register("red_priest", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("red_priest", 9, new EventActingTask("hit_with_arrow", 0.04f, 20 * 3));

        register("red_priest", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("red_priest", 8, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("red_priest", 8, new EventActingTask("use_provoking_ability", 0.2f, 20 * 15));

        register("red_priest", 7, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("red_priest", 7, new EventActingTask("kill_burning_mob", 0.15f, 20 * 5));
        register("red_priest", 7, new EventActingTask("use_pyrokinesis_ability", 0.2f, 20 * 15));
        register("red_priest", 7, new EventActingTask("kill_during_storm", 0.08f, 20 * 5));

        register("red_priest", 6, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("red_priest", 6, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("red_priest", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("red_priest", 6, new EventActingTask("kill_with_ranged", 0.06f, 20 * 3));

        register("red_priest", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("red_priest", 5, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
        register("red_priest", 5, new EventActingTask("kill_undead", 0.08f, 20 * 5));

        register("red_priest", 4, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("red_priest", 4, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("red_priest", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("red_priest", 4, new EventActingTask("craft_iron_armor", 0.05f, 20 * 30));

        register("red_priest", 3, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("red_priest", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("red_priest", 3, new EventActingTask("use_war_cry_ability", 0.2f, 20 * 15));
        register("red_priest", 3, new EventActingTask("kill_during_storm", 0.1f, 20 * 5));

        register("red_priest", 2, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("red_priest", 2, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("red_priest", 2, new EventActingTask("use_weather_manipulation_ability", 0.3f, 20 * 20));
        register("red_priest", 2, new EventActingTask("stand_in_rain", 0.05f, 20 * 20));

        register("red_priest", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("red_priest", 1, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("red_priest", 1, new EventActingTask("use_conquering_ability", 0.3f, 20 * 20));
        register("red_priest", 1, new EventActingTask("kill_during_storm", 0.1f, 20 * 5));


        register("demoness", 9, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("demoness", 9, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("demoness", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("demoness", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("demoness", 8, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("demoness", 8, new EventActingTask("use_instigation_ability", 0.2f, 20 * 15));

        register("demoness", 7, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("demoness", 7, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("demoness", 7, new EventActingTask("brew_potion", 0.1f, 20 * 15));
        register("demoness", 7, new EventActingTask("craft_book", 0.03f, 20 * 20));

        register("demoness", 6, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("demoness", 6, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("demoness", 6, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("demoness", 6, new EventActingTask("drink_honey", 0.03f, 20 * 10));

        register("demoness", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("demoness", 5, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("demoness", 5, new EventActingTask("use_disease_ability", 0.2f, 20 * 20));

        register("demoness", 4, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));
        register("demoness", 4, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("demoness", 4, new EventActingTask("use_curse_ability", 0.2f, 20 * 20));
        register("demoness", 4, new EventActingTask("linger_in_darkness", 0.05f, 20 * 30));

        register("demoness", 3, new EventActingTask("use_anvil", 0.1f, 20 * 10));
        register("demoness", 3, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("demoness", 3, new EventActingTask("use_petrification_ability", 0.2f, 20 * 20));
        register("demoness", 3, new EventActingTask("mine_diamond_ore", 0.05f, 20 * 20));

        register("demoness", 2, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("demoness", 2, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("demoness", 2, new EventActingTask("use_disaster_manifestation_ability", 0.3f, 20 * 20));

        register("demoness", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("demoness", 1, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("demoness", 1, new EventActingTask("use_apocalypse_ability", 0.3f, 20 * 20));
        register("demoness", 1, new EventActingTask("destroy_spawner", 0.05f, 20 * 30));


        register("tyrant", 9, new EventActingTask("ride_boat", 0.1f, 20 * 10));
        register("tyrant", 9, new EventActingTask("catch_fish", 0.1f, 20 * 10));
        register("tyrant", 9, new EventActingTask("use_cartography_table", 0.05f, 20 * 10));
        register("tyrant", 9, new EventActingTask("submerge_in_water", 0.05f, 20 * 15));

        register("tyrant", 8, new EventActingTask("drink_potion", 0.1f, 20 * 10));
        register("tyrant", 8, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("tyrant", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("tyrant", 8, new EventActingTask("kill_during_storm", 0.08f, 20 * 5));

        register("tyrant", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("tyrant", 7, new EventActingTask("use_compass", 0.05f, 20 * 8));
        register("tyrant", 7, new EventActingTask("use_spyglass", 0.05f, 20 * 8));
        register("tyrant", 7, new EventActingTask("ride_boat", 0.05f, 20 * 10));

        register("tyrant", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("tyrant", 6, new EventActingTask("ride_minecart", 0.05f, 20 * 10));
        register("tyrant", 6, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("tyrant", 6, new EventActingTask("stand_at_height", 0.05f, 20 * 20));

        register("tyrant", 5, new EventActingTask("use_note_block", 0.1f, 20 * 10));
        register("tyrant", 5, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("tyrant", 5, new EventActingTask("use_siren_song_ability", 0.2f, 20 * 20));
        register("tyrant", 5, new EventActingTask("submerge_in_water", 0.05f, 20 * 15));

        register("tyrant", 4, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("tyrant", 4, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("tyrant", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("tyrant", 4, new EventActingTask("mine_ore", 0.04f, 20 * 10));

        register("tyrant", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("tyrant", 3, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("tyrant", 3, new EventActingTask("ride_boat", 0.05f, 20 * 10));
        register("tyrant", 3, new EventActingTask("kill_during_storm", 0.1f, 20 * 5));

        register("tyrant", 2, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("tyrant", 2, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("tyrant", 2, new EventActingTask("use_calamity_creation_ability", 0.3f, 20 * 20));
        register("tyrant", 2, new EventActingTask("kill_during_storm", 0.1f, 20 * 5));

        register("tyrant", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("tyrant", 1, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("tyrant", 1, new EventActingTask("use_heavenly_punishment_ability", 0.3f, 20 * 20));
        register("tyrant", 1, new EventActingTask("stand_in_rain", 0.05f, 20 * 20));


        register("sun", 9, new EventActingTask("use_note_block", 0.15f, 20 * 10));
        register("sun", 9, new EventActingTask("use_lectern", 0.1f, 20 * 15));
        register("sun", 9, new EventActingTask("breed_animals", 0.05f, 20 * 15));
        register("sun", 9, new EventActingTask("craft_painting", 0.05f, 20 * 30));

        register("sun", 8, new EventActingTask("brew_potion", 0.1f, 20 * 15));
        register("sun", 8, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("sun", 8, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("sun", 8, new EventActingTask("stand_in_sunlight", 0.06f, 20 * 30));

        register("sun", 7, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("sun", 7, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("sun", 7, new EventActingTask("use_sun_halo_ability", 0.2f, 20 * 20));
        register("sun", 7, new EventActingTask("stand_in_sunlight", 0.06f, 20 * 30));

        register("sun", 6, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("sun", 6, new EventActingTask("use_anvil", 0.05f, 20 * 10));
        register("sun", 6, new EventActingTask("use_notary_buff_ability", 0.2f, 20 * 20));
        register("sun", 6, new EventActingTask("write_in_book", 0.05f, 20 * 15));

        register("sun", 5, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("sun", 5, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("sun", 5, new EventActingTask("use_light_of_holiness_ability", 0.2f, 20 * 20));
        register("sun", 5, new EventActingTask("kill_undead", 0.1f, 20 * 5));

        register("sun", 4, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("sun", 4, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("sun", 4, new EventActingTask("use_unshadowed_domain_ability", 0.2f, 20 * 20));
        register("sun", 4, new EventActingTask("stand_in_sunlight", 0.06f, 20 * 30));

        register("sun", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("sun", 3, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("sun", 3, new EventActingTask("use_sword_of_justice_ability", 0.2f, 20 * 20));
        register("sun", 3, new EventActingTask("kill_undead", 0.1f, 20 * 5));

        register("sun", 2, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("sun", 2, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("sun", 2, new EventActingTask("use_solar_envoy_ability", 0.2f, 20 * 20));
        register("sun", 2, new EventActingTask("stand_in_sunlight", 0.06f, 20 * 30));

        register("sun", 1, new EventActingTask("kill_burning_mob", 0.15f, 20 * 5));
        register("sun", 1, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("sun", 1, new EventActingTask("use_divine_kingdom_manifestation_ability", 0.3f, 20 * 20));
        register("sun", 1, new EventActingTask("kill_undead", 0.1f, 20 * 5));


        register("visionary", 9, new EventActingTask("use_spyglass", 0.1f, 20 * 8));
        register("visionary", 9, new EventActingTask("use_lectern", 0.15f, 20 * 20));
        register("visionary", 9, new EventActingTask("use_cartography_table", 0.05f, 20 * 10));
        register("visionary", 9, new EventActingTask("read_written_book", 0.05f, 20 * 15));

        register("visionary", 8, new EventActingTask("use_lectern", 0.1f, 20 * 15));
        register("visionary", 8, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("visionary", 8, new EventActingTask("craft_map", 0.05f, 20 * 15));
        register("visionary", 8, new EventActingTask("open_chiseled_bookshelf", 0.05f, 20 * 10));

        register("visionary", 7, new EventActingTask("feed_animal", 0.1f, 20 * 10));
        register("visionary", 7, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("visionary", 7, new EventActingTask("breed_animals", 0.05f, 20 * 15));
        register("visionary", 7, new EventActingTask("drink_milk", 0.03f, 20 * 10));

        register("visionary", 6, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("visionary", 6, new EventActingTask("use_name_tag", 0.05f, 20 * 10));
        register("visionary", 6, new EventActingTask("use_battle_hypnosis_ability", 0.2f, 20 * 20));
        register("visionary", 6, new EventActingTask("linger_in_darkness", 0.04f, 20 * 30));

        register("visionary", 5, new EventActingTask("use_spyglass", 0.1f, 20 * 8));
        register("visionary", 5, new EventActingTask("sneak_kill", 0.05f, 20 * 5));
        register("visionary", 5, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("visionary", 5, new EventActingTask("stay_awake_through_night", 0.08f, 20 * 30));

        register("visionary", 4, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("visionary", 4, new EventActingTask("use_lead", 0.05f, 20 * 10));
        register("visionary", 4, new EventActingTask("use_manipulation_ability", 0.2f, 20 * 20));
        register("visionary", 4, new EventActingTask("trade_with_villager", 0.08f, 20 * 20));

        register("visionary", 3, new EventActingTask("craft_map", 0.1f, 20 * 15));
        register("visionary", 3, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("visionary", 3, new EventActingTask("use_dream_weave_ability", 0.2f, 20 * 20));
        register("visionary", 3, new EventActingTask("linger_in_darkness", 0.05f, 20 * 30));

        register("visionary", 2, new EventActingTask("use_spyglass", 0.1f, 20 * 8));
        register("visionary", 2, new EventActingTask("use_anvil", 0.05f, 20 * 10));
        register("visionary", 2, new EventActingTask("use_discernment_ability", 0.2f, 20 * 20));
        register("visionary", 2, new EventActingTask("read_written_book", 0.05f, 20 * 15));

        register("visionary", 1, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("visionary", 1, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("visionary", 1, new EventActingTask("use_story_writing_ability", 0.3f, 20 * 20));
        register("visionary", 1, new EventActingTask("write_in_book", 0.08f, 20 * 15));


        register("wheel_of_fortune", 9, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("wheel_of_fortune", 9, new EventActingTask("use_spyglass", 0.1f, 20 * 8));
        register("wheel_of_fortune", 9, new EventActingTask("catch_fish", 0.05f, 20 * 10));
        register("wheel_of_fortune", 9, new EventActingTask("eat_rotten_flesh", 0.03f, 20 * 5));

        register("wheel_of_fortune", 8, new EventActingTask("smelt_item", 0.1f, 20 * 10));
        register("wheel_of_fortune", 8, new EventActingTask("mine_ore", 0.1f, 20 * 10));
        register("wheel_of_fortune", 8, new EventActingTask("craft_iron_tool", 0.05f, 20 * 15));
        register("wheel_of_fortune", 8, new EventActingTask("use_grindstone", 0.04f, 20 * 10));

        register("wheel_of_fortune", 7, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("wheel_of_fortune", 7, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("wheel_of_fortune", 7, new EventActingTask("eat_suspicious_stew", 0.05f, 20 * 5));
        register("wheel_of_fortune", 7, new EventActingTask("catch_fish", 0.05f, 20 * 10));

        register("wheel_of_fortune", 6, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("wheel_of_fortune", 6, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("wheel_of_fortune", 6, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("wheel_of_fortune", 6, new EventActingTask("kill_during_storm", 0.08f, 20 * 5));

        register("wheel_of_fortune", 5, new EventActingTask("catch_fish", 0.1f, 20 * 10));
        register("wheel_of_fortune", 5, new EventActingTask("harvest_crop", 0.1f, 20 * 10));
        register("wheel_of_fortune", 5, new EventActingTask("eat_suspicious_stew", 0.05f, 20 * 5));
        register("wheel_of_fortune", 5, new EventActingTask("kill_while_low_health", 0.08f, 20 * 2));

        register("wheel_of_fortune", 4, new EventActingTask("kill_while_low_health", 0.2f, 20 * 2));
        register("wheel_of_fortune", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("wheel_of_fortune", 4, new EventActingTask("use_misfortune_field_ability", 0.2f, 20 * 20));
        register("wheel_of_fortune", 4, new EventActingTask("eat_rotten_flesh", 0.03f, 20 * 5));

        register("wheel_of_fortune", 3, new EventActingTask("outnumbered_kill", 0.15f, 20 * 5));
        register("wheel_of_fortune", 3, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("wheel_of_fortune", 3, new EventActingTask("use_spiritual_baptism_ability", 0.2f, 20 * 20));
        register("wheel_of_fortune", 3, new EventActingTask("destroy_spawner", 0.05f, 20 * 30));

        register("wheel_of_fortune", 2, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("wheel_of_fortune", 2, new EventActingTask("use_compass", 0.05f, 20 * 8));
        register("wheel_of_fortune", 2, new EventActingTask("use_prophecy_ability", 0.2f, 20 * 20));
        register("wheel_of_fortune", 2, new EventActingTask("stand_at_height", 0.05f, 20 * 20));

        register("wheel_of_fortune", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("wheel_of_fortune", 1, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("wheel_of_fortune", 1, new EventActingTask("use_cycle_of_fate_ability", 0.3f, 20 * 20));
        register("wheel_of_fortune", 1, new EventActingTask("pickup_rare_item", 0.05f, 20 * 30));


        register("abyss", 9, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("abyss", 9, new EventActingTask("eat_suspicious_stew", 0.05f, 20 * 5));
        register("abyss", 9, new EventActingTask("drink_potion", 0.1f, 20 * 10));
        register("abyss", 9, new EventActingTask("pickup_item", 0.03f, 20 * 5));

        register("abyss", 8, new EventActingTask("use_note_block", 0.1f, 20 * 10));
        register("abyss", 8, new EventActingTask("trade_with_villager", 0.1f, 20 * 15));
        register("abyss", 8, new EventActingTask("use_language_of_foulness_ability", 0.2f, 20 * 15));
        register("abyss", 8, new EventActingTask("pickup_undead_drop", 0.04f, 20 * 5));

        register("abyss", 7, new EventActingTask("feed_animal", 0.1f, 20 * 10));
        register("abyss", 7, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("abyss", 7, new EventActingTask("trade_with_villager", 0.1f, 20 * 15));
        register("abyss", 7, new EventActingTask("kill_passive_mob", 0.05f, 20 * 5));

        register("abyss", 6, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("abyss", 6, new EventActingTask("brew_potion", 0.1f, 20 * 15));
        register("abyss", 6, new EventActingTask("use_defiling_seed_ability", 0.2f, 20 * 20));
        register("abyss", 6, new EventActingTask("drink_honey", 0.03f, 20 * 10));

        register("abyss", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("abyss", 5, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
        register("abyss", 5, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("abyss", 5, new EventActingTask("pickup_rare_item", 0.04f, 20 * 30));

        register("abyss", 4, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("abyss", 4, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("abyss", 4, new EventActingTask("use_mind_fog_ability", 0.2f, 20 * 20));
        register("abyss", 4, new EventActingTask("linger_in_darkness", 0.06f, 20 * 30));

        register("abyss", 3, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("abyss", 3, new EventActingTask("place_mob_head", 0.1f, 20 * 10));
        register("abyss", 3, new EventActingTask("use_fear_aura_ability", 0.2f, 20 * 20));
        register("abyss", 3, new EventActingTask("destroy_spawner", 0.05f, 20 * 30));

        register("abyss", 2, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("abyss", 2, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("abyss", 2, new EventActingTask("use_flames_of_the_abyss_ability", 0.3f, 20 * 20));
        register("abyss", 2, new EventActingTask("kill_during_storm", 0.08f, 20 * 5));

        register("abyss", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("abyss", 1, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("abyss", 1, new EventActingTask("use_corrupting_voice_ability", 0.3f, 20 * 20));
        register("abyss", 1, new EventActingTask("kill_undead", 0.05f, 20 * 5));


        register("justiciar", 9, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("justiciar", 9, new EventActingTask("use_anvil", 0.1f, 20 * 10));
        register("justiciar", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("justiciar", 9, new EventActingTask("use_lectern", 0.05f, 20 * 15));

        register("justiciar", 8, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("justiciar", 8, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("justiciar", 8, new EventActingTask("use_recognition_ability", 0.2f, 20 * 15));
        register("justiciar", 8, new EventActingTask("kill_undead", 0.06f, 20 * 5));

        register("justiciar", 7, new EventActingTask("use_lectern", 0.15f, 20 * 15));
        register("justiciar", 7, new EventActingTask("use_enchanting_table", 0.05f, 20 * 10));
        register("justiciar", 7, new EventActingTask("use_eye_of_order_ability", 0.2f, 20 * 15));
        register("justiciar", 7, new EventActingTask("write_in_book", 0.05f, 20 * 15));

        register("justiciar", 6, new EventActingTask("use_anvil", 0.1f, 20 * 10));
        register("justiciar", 6, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("justiciar", 6, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("justiciar", 6, new EventActingTask("craft_iron_armor", 0.05f, 20 * 30));

        register("justiciar", 5, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("justiciar", 5, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("justiciar", 5, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
        register("justiciar", 5, new EventActingTask("kill_undead", 0.08f, 20 * 5));

        register("justiciar", 4, new EventActingTask("use_lectern", 0.15f, 20 * 20));
        register("justiciar", 4, new EventActingTask("craft_map", 0.05f, 20 * 15));
        register("justiciar", 4, new EventActingTask("use_prohibition_ability", 0.2f, 20 * 20));
        register("justiciar", 4, new EventActingTask("write_in_book", 0.05f, 20 * 15));

        register("justiciar", 3, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("justiciar", 3, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("justiciar", 3, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("justiciar", 3, new EventActingTask("use_balancing_ability", 0.2f, 20 * 20));
        register("justiciar", 3, new EventActingTask("kill_monster", 0.06f, 20 * 5));

        register("justiciar", 2, new EventActingTask("use_anvil", 0.1f, 20 * 10));
        register("justiciar", 2, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("justiciar", 2, new EventActingTask("level_up", 0.05f, 20 * 30));

        register("justiciar", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("justiciar", 1, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
        register("justiciar", 1, new EventActingTask("use_order_proxy_ability", 0.3f, 20 * 20));
        register("justiciar", 1, new EventActingTask("kill_undead", 0.08f, 20 * 5));


        register("darkness", 9, new EventActingTask("kill_in_darkness", 0.15f, 20 * 2));
        register("darkness", 9, new EventActingTask("use_spyglass", 0.05f, 20 * 8));
        register("darkness", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("darkness", 9, new EventActingTask("stay_awake_through_night", 0.1f, 20 * 30));

        register("darkness", 8, new EventActingTask("use_note_block", 0.15f, 20 * 10));
        register("darkness", 8, new EventActingTask("use_lectern", 0.15f, 20 * 15));
        register("darkness", 8, new EventActingTask("use_midnight_poem_ability", 0.2f, 20 * 15));
        register("darkness", 8, new EventActingTask("linger_in_darkness", 0.06f, 20 * 30));

        register("darkness", 7, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("darkness", 7, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("darkness", 7, new EventActingTask("use_nightmare_ability", 0.2f, 20 * 20));
        register("darkness", 7, new EventActingTask("linger_in_darkness", 0.06f, 20 * 30));

        register("darkness", 6, new EventActingTask("feed_animal", 0.1f, 20 * 10));
        register("darkness", 6, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("darkness", 6, new EventActingTask("use_requiem_ability", 0.2f, 20 * 20));
        register("darkness", 6, new EventActingTask("pickup_undead_drop", 0.04f, 20 * 5));

        register("darkness", 5, new EventActingTask("breed_animals", 0.1f, 20 * 15));
        register("darkness", 5, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("darkness", 5, new EventActingTask("stay_awake_through_night", 0.12f, 20 * 30));
        register("darkness", 5, new EventActingTask("linger_in_darkness", 0.06f, 20 * 30));

        register("darkness", 4, new EventActingTask("kill_in_darkness", 0.15f, 20 * 2));
        register("darkness", 4, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("darkness", 4, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("darkness", 4, new EventActingTask("stay_awake_through_night", 0.1f, 20 * 30));

        register("darkness", 3, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("darkness", 3, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("darkness", 3, new EventActingTask("use_horror_aura_ability", 0.2f, 20 * 20));
        register("darkness", 3, new EventActingTask("destroy_spawner", 0.05f, 20 * 30));

        register("darkness", 2, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("darkness", 2, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("darkness", 2, new EventActingTask("use_concealment_ability", 0.2f, 20 * 20));
        register("darkness", 2, new EventActingTask("linger_in_darkness", 0.06f, 20 * 30));

        register("darkness", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("darkness", 1, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("darkness", 1, new EventActingTask("use_sword_of_darkness_ability", 0.3f, 20 * 20));
        register("darkness", 1, new EventActingTask("kill_during_storm", 0.08f, 20 * 5));


        register("death", 9, new EventActingTask("place_mob_head", 0.15f, 20 * 10));
        register("death", 9, new EventActingTask("mine_ore", 0.05f, 20 * 10));
        register("death", 9, new EventActingTask("use_eye_of_death_ability", 0.2f, 20 * 15));
        register("death", 9, new EventActingTask("pickup_undead_drop", 0.05f, 20 * 5));

        register("death", 8, new EventActingTask("mine_ore", 0.1f, 20 * 10));
        register("death", 8, new EventActingTask("catch_fish", 0.05f, 20 * 10));
        register("death", 8, new EventActingTask("use_spirit_communication_ability", 0.2f, 20 * 15));
        register("death", 8, new EventActingTask("pickup_undead_drop", 0.05f, 20 * 5));

        register("death", 7, new EventActingTask("use_note_block", 0.1f, 20 * 10));
        register("death", 7, new EventActingTask("brew_potion", 0.1f, 20 * 15));
        register("death", 7, new EventActingTask("use_spirit_channeling_ability", 0.2f, 20 * 20));
        register("death", 7, new EventActingTask("place_soul_lantern", 0.05f, 20 * 10));

        register("death", 6, new EventActingTask("use_lead", 0.05f, 20 * 10));
        register("death", 6, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("death", 6, new EventActingTask("use_word_of_spirit_ability", 0.2f, 20 * 20));
        register("death", 6, new EventActingTask("place_soul_lantern", 0.05f, 20 * 10));

        register("death", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("death", 5, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));
        register("death", 5, new EventActingTask("use_door_to_the_underworld_ability", 0.2f, 20 * 20));
        register("death", 5, new EventActingTask("kill_undead", 0.08f, 20 * 5));

        register("death", 4, new EventActingTask("kill_while_low_health", 0.2f, 20 * 2));
        register("death", 4, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("death", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));

        register("death", 3, new EventActingTask("ride_boat", 0.1f, 20 * 10));
        register("death", 3, new EventActingTask("place_soul_lantern", 0.1f, 20 * 10));
        register("death", 3, new EventActingTask("use_death_spells_ability", 0.2f, 20 * 20));
        register("death", 3, new EventActingTask("submerge_in_water", 0.05f, 20 * 15));

        register("death", 2, new EventActingTask("place_mob_head", 0.1f, 20 * 10));
        register("death", 2, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("death", 2, new EventActingTask("use_nation_of_the_dead_ability", 0.3f, 20 * 20));
        register("death", 2, new EventActingTask("kill_undead", 0.06f, 20 * 5));

        register("death", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("death", 1, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));
        register("death", 1, new EventActingTask("use_hand_of_death_ability", 0.3f, 20 * 20));
    }

    record PathwaySequenceKey(String pathway, int sequence) {
    }
}