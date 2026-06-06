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

       register("fool", 8, new EventActingTask("use_firework", 0.02f, 20));
       register("fool", 8, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));

       register("fool", 7, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));
       register("fool", 7, new EventActingTask("use_flaming_jump_ability_while_low_health", 0.05f, 10));
       register("fool", 7, new EventActingTask("eat_golden_apple", 0.05f, 20 * 2));

       register("fool", 6, new EventActingTask("use_shapeshifting_ability", 0.2f, 20 * 15));

       register("fool", 5, new EventActingTask("sneak_kill", 0.05f, 20 * 2));
       register("fool", 5, new EventActingTask("drink_invisibility_potion", 0.05f, 20 * 2));
       register("fool", 5, new EventActingTask("use_puppeteering_ability", 0.1f, 20 * 10));

       register("fool", 4, new EventActingTask("use_puppeteering_ability", 0.1f, 20 * 15));
       register("fool", 4, new EventActingTask("drink_invisibility_potion", 0.05f, 20 * 2));
       register("fool", 4, new EventActingTask("use_enderpearl", 0.02f, 20 * 5));
       register("fool", 4, new EventActingTask("place_mob_head", 0.2f, 20 * 5));

       register("fool", 3, new EventActingTask("use_lectern", 0.2f, 20 * 20));
       register("fool", 3, new EventActingTask("trade_with_villager", 0.2f, 20 * 20));

       register("fool", 2, new EventActingTask("use_miracle_creation_ability", 0.3f, 20 * 20));
       register("fool", 2, new EventActingTask("trade_with_villager", 0.3f, 20 * 20));

       register("fool", 1, new EventActingTask("place_soul_lantern", 0.02f, 20 * 2));
       register("fool", 1, new EventActingTask("eat_suspicious_stew", 0.02f, 20 * 2));
       register("fool", 1, new EventActingTask("kill_in_darkness", 0.2f, 20 * 2));


        register("door", 9, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("door", 9, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("door", 9, new EventActingTask("use_cartography_table", 0.05f, 20 * 10));

        register("door", 8, new EventActingTask("use_note_block", 0.05f, 20 * 5));
        register("door", 8, new EventActingTask("use_firework", 0.02f, 20));
        register("door", 8, new EventActingTask("sneak_kill", 0.1f, 20 * 2));

        register("door", 7, new EventActingTask("use_compass", 0.05f, 20 * 8));
        register("door", 7, new EventActingTask("use_spyglass", 0.05f, 20 * 8));
        register("door", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));

        register("door", 6, new EventActingTask("use_lectern", 0.2f, 20 * 20));
        register("door", 6, new EventActingTask("craft_map", 0.15f, 20 * 15));
        register("door", 6, new EventActingTask("use_recording_ability", 0.2f, 20 * 15));

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

        register("error", 8, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("error", 8, new EventActingTask("use_note_block", 0.05f, 20 * 5));
        register("error", 8, new EventActingTask("sneak_kill", 0.1f, 20 * 2));

        register("error", 7, new EventActingTask("use_lectern", 0.15f, 20 * 15));
        register("error", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("error", 7, new EventActingTask("use_decryption_ability", 0.2f, 20 * 15));

        register("error", 6, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("error", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("error", 6, new EventActingTask("use_ability_theft_ability", 0.2f, 20 * 20));

        register("error", 5, new EventActingTask("stay_awake_through_night", 0.2f, 20 * 30));
        register("error", 5, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));

        register("error", 4, new EventActingTask("tame_animal", 0.1f, 20 * 10));
        register("error", 4, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("error", 4, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("error", 3, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("error", 3, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("error", 3, new EventActingTask("use_deceit_ability", 0.2f, 20 * 15));

        register("error", 2, new EventActingTask("trade_with_villager", 0.1f, 20 * 20));
        register("error", 2, new EventActingTask("use_fate_siphoning_ability", 0.3f, 20 * 20));

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

        register("mother", 4, new EventActingTask("brew_potion", 0.15f, 20 * 20));
        register("mother", 4, new EventActingTask("smelt_item", 0.05f, 20 * 10));
        register("mother", 4, new EventActingTask("use_mutation_creation_ability", 0.2f, 20 * 20));

        register("mother", 3, new EventActingTask("place_mob_head", 0.2f, 20 * 10));
        register("mother", 3, new EventActingTask("mine_ore", 0.05f, 20 * 10));
        register("mother", 3, new EventActingTask("use_life_deprivation_ability", 0.2f, 20 * 20));

        register("mother", 2, new EventActingTask("plant_sapling", 0.1f, 20 * 15));
        register("mother", 2, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("mother", 2, new EventActingTask("use_blooming_area_ability", 0.3f, 20 * 20));

        register("mother", 1, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("mother", 1, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("mother", 1, new EventActingTask("use_wrath_of_nature_ability", 0.3f, 20 * 20));


        register("red_priest", 9, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("red_priest", 9, new EventActingTask("catch_fish", 0.05f, 20 * 10));
        register("red_priest", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("red_priest", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("red_priest", 8, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("red_priest", 8, new EventActingTask("use_provoking_ability", 0.2f, 20 * 15));

        register("red_priest", 7, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("red_priest", 7, new EventActingTask("kill_burning_mob", 0.15f, 20 * 5));
        register("red_priest", 7, new EventActingTask("use_pyrokinesis_ability", 0.2f, 20 * 15));

        register("red_priest", 6, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("red_priest", 6, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("red_priest", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));

        register("red_priest", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("red_priest", 5, new EventActingTask("kill_while_full_health", 0.1f, 20 * 2));

        register("red_priest", 4, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("red_priest", 4, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("red_priest", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));

        register("red_priest", 3, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("red_priest", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("red_priest", 3, new EventActingTask("use_war_cry_ability", 0.2f, 20 * 15));

        register("red_priest", 2, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("red_priest", 2, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("red_priest", 2, new EventActingTask("use_weather_manipulation_ability", 0.3f, 20 * 20));

        register("red_priest", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("red_priest", 1, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("red_priest", 1, new EventActingTask("use_conquering_ability", 0.3f, 20 * 20));


        register("demoness", 9, new EventActingTask("sneak_kill", 0.1f, 20 * 2));
        register("demoness", 9, new EventActingTask("kill_in_darkness", 0.1f, 20 * 2));
        register("demoness", 9, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));

        register("demoness", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));
        register("demoness", 8, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("demoness", 8, new EventActingTask("use_instigation_ability", 0.2f, 20 * 15));

        register("demoness", 7, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("demoness", 7, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("demoness", 7, new EventActingTask("brew_potion", 0.1f, 20 * 15));

        register("demoness", 6, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("demoness", 6, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("demoness", 6, new EventActingTask("tame_animal", 0.1f, 20 * 15));

        register("demoness", 5, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("demoness", 5, new EventActingTask("throw_splash_potion", 0.1f, 20 * 5));
        register("demoness", 5, new EventActingTask("use_disease_ability", 0.2f, 20 * 20));

        register("demoness", 4, new EventActingTask("kill_while_low_health", 0.1f, 20 * 2));
        register("demoness", 4, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("demoness", 4, new EventActingTask("use_curse_ability", 0.2f, 20 * 20));

        register("demoness", 3, new EventActingTask("use_anvil", 0.1f, 20 * 10));
        register("demoness", 3, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("demoness", 3, new EventActingTask("use_petrification_ability", 0.2f, 20 * 20));

        register("demoness", 2, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("demoness", 2, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("demoness", 2, new EventActingTask("use_disaster_manifestation_ability", 0.3f, 20 * 20));

        register("demoness", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("demoness", 1, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("demoness", 1, new EventActingTask("use_apocalypse_ability", 0.3f, 20 * 20));


        register("tyrant", 9, new EventActingTask("ride_boat", 0.1f, 20 * 10));
        register("tyrant", 9, new EventActingTask("catch_fish", 0.1f, 20 * 10));
        register("tyrant", 9, new EventActingTask("use_cartography_table", 0.05f, 20 * 10));

        register("tyrant", 8, new EventActingTask("drink_potion", 0.1f, 20 * 10));
        register("tyrant", 8, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("tyrant", 8, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));

        register("tyrant", 7, new EventActingTask("use_cartography_table", 0.1f, 20 * 10));
        register("tyrant", 7, new EventActingTask("use_compass", 0.05f, 20 * 8));
        register("tyrant", 7, new EventActingTask("use_spyglass", 0.05f, 20 * 8));

        register("tyrant", 6, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("tyrant", 6, new EventActingTask("ride_minecart", 0.05f, 20 * 10));
        register("tyrant", 6, new EventActingTask("use_environment", 0.1f, 20 * 15));

        register("tyrant", 5, new EventActingTask("use_note_block", 0.1f, 20 * 10));
        register("tyrant", 5, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("tyrant", 5, new EventActingTask("use_siren_song_ability", 0.2f, 20 * 20));

        register("tyrant", 4, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("tyrant", 4, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("tyrant", 4, new EventActingTask("attack_stronger_mob", 0.1f, 20 * 5));

        register("tyrant", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("tyrant", 3, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("tyrant", 3, new EventActingTask("ride_boat", 0.05f, 20 * 10));

        register("tyrant", 2, new EventActingTask("set_fire", 0.1f, 20 * 5));
        register("tyrant", 2, new EventActingTask("place_tnt", 0.1f, 20 * 10));
        register("tyrant", 2, new EventActingTask("use_calamity_creation_ability", 0.3f, 20 * 20));

        register("tyrant", 1, new EventActingTask("kill_strong_mobs", 0.15f, 20 * 5));
        register("tyrant", 1, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("tyrant", 1, new EventActingTask("use_heavenly_punishment_ability", 0.3f, 20 * 20));


        register("sun", 9, new EventActingTask("use_note_block", 0.15f, 20 * 10));
        register("sun", 9, new EventActingTask("use_lectern", 0.1f, 20 * 15));
        register("sun", 9, new EventActingTask("breed_animals", 0.05f, 20 * 15));

        register("sun", 8, new EventActingTask("brew_potion", 0.1f, 20 * 15));
        register("sun", 8, new EventActingTask("use_enchanting_table", 0.1f, 20 * 10));
        register("sun", 8, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));

        register("sun", 7, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("sun", 7, new EventActingTask("feed_animal", 0.05f, 20 * 10));
        register("sun", 7, new EventActingTask("use_sun_halo_ability", 0.2f, 20 * 20));

        register("sun", 6, new EventActingTask("trade_with_villager", 0.15f, 20 * 20));
        register("sun", 6, new EventActingTask("use_anvil", 0.05f, 20 * 10));
        register("sun", 6, new EventActingTask("use_notary_buff_ability", 0.2f, 20 * 20));

        register("sun", 5, new EventActingTask("kill_burning_mob", 0.1f, 20 * 5));
        register("sun", 5, new EventActingTask("kill_untargeted_mob", 0.05f, 20 * 5));
        register("sun", 5, new EventActingTask("use_light_of_holiness_ability", 0.2f, 20 * 20));

        register("sun", 4, new EventActingTask("kill_while_low_health", 0.15f, 20 * 2));
        register("sun", 4, new EventActingTask("eat_golden_apple", 0.1f, 20 * 10));
        register("sun", 4, new EventActingTask("use_unshadowed_domain_ability", 0.2f, 20 * 20));

        register("sun", 3, new EventActingTask("tame_animal", 0.1f, 20 * 15));
        register("sun", 3, new EventActingTask("outnumbered_kill", 0.1f, 20 * 5));
        register("sun", 3, new EventActingTask("use_sword_of_justice_ability", 0.2f, 20 * 20));

        register("sun", 2, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("sun", 2, new EventActingTask("use_environment", 0.1f, 20 * 15));
        register("sun", 2, new EventActingTask("use_solar_envoy_ability", 0.2f, 20 * 20));

        register("sun", 1, new EventActingTask("kill_burning_mob", 0.15f, 20 * 5));
        register("sun", 1, new EventActingTask("kill_strong_mobs", 0.1f, 20 * 5));
        register("sun", 1, new EventActingTask("use_divine_kingdom_manifestation_ability", 0.3f, 20 * 20));
    }
    record PathwaySequenceKey(String pathway, int sequence) {}
}