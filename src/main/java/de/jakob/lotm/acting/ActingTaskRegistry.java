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
        register("mother", 9, new EventActingTask("plant_crop", 0.02f));
        register("mother", 9, new EventActingTask("harvest_crop", 0.015f));
        register("mother", 9, new EventActingTask("use_bonemeal", 0.01f));
        register("mother", 8, new EventActingTask("feed_animal", 0.015f));
        register("mother", 8, new EventActingTask("harvest_crop", 0.008f));
        register("mother", 7, new EventActingTask("use_bonemeal", 0.012f));
        register("mother", 7, new EventActingTask("plant_crop", 0.01f));
        register("mother", 6, new EventActingTask("breed_animals", 0.025f));
        register("mother", 6, new EventActingTask("feed_animal", 0.008f));
        register("mother", 5, new EventActingTask("tame_animal", 0.04f));
        register("mother", 5, new EventActingTask("breed_animals", 0.015f));
        register("mother", 4, new EventActingTask("brew_potion", 0.02f));
        register("mother", 4, new EventActingTask("breed_animals", 0.01f));

        register("demoness", 9, new EventActingTask("sneak_kill", 0.02f));
        register("demoness", 9, new EventActingTask("kill_in_darkness", 0.015f));
        register("demoness", 8, new EventActingTask("throw_splash_potion", 0.02f));
        register("demoness", 8, new EventActingTask("kill_untargeted_mob", 0.008f));
        register("demoness", 7, new EventActingTask("enter_nether", 0.05f));
        register("demoness", 7, new EventActingTask("kill_untargeted_mob", 0.006f));
        register("demoness", 6, new EventActingTask("eat_golden_apple", 0.04f));
        register("demoness", 6, new EventActingTask("throw_splash_potion", 0.008f));
        register("demoness", 5, new EventActingTask("kill_while_hurt", 0.02f));
        register("demoness", 5, new EventActingTask("throw_splash_potion", 0.012f));
        register("demoness", 4, new EventActingTask("kill_while_low_health", 0.04f));
        register("demoness", 4, new EventActingTask("kill_while_hurt", 0.01f));

        register("red_priest", 9, new EventActingTask("kill_strong_mobs", 0.005f));
        register("red_priest", 9, new EventActingTask("use_environment", 0.0001f));
        register("red_priest", 8, new EventActingTask("attack_stronger_mob", 0.005f));
        register("red_priest", 7, new EventActingTask("set_fire", 0.002f));
        register("red_priest", 7, new EventActingTask("kill_burning_mob", 0.004f));
        register("red_priest", 6, new EventActingTask("kill_untargeted_mob", 0.0025f));
        register("red_priest", 6, new EventActingTask("sneak_kill", 0.003f));
        register("red_priest", 5, new EventActingTask("kill_strong_mobs", 0.0025f));
        register("red_priest", 4, new EventActingTask("outnumbered_kill", 0.005f));
        register("red_priest", 4, new EventActingTask("kill_strong_mobs", 0.002f));
    }
    record PathwaySequenceKey(String pathway, int sequence) {}
}