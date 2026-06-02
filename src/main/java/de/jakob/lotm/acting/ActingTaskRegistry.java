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
    }
    
    record PathwaySequenceKey(String pathway, int sequence) {}
}