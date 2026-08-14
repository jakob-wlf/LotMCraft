package de.jakob.lotm.addons.rituals;

import java.util.HashMap;
import java.util.Map;

public class RitualDescriptionHelper {
    private static final Map<String, Map<Integer, String>> description = new HashMap<>();

    static {
        Map<Integer, String> wof = new HashMap<>();
        wof.put(5, "Survive a long time without any fortune");
        wof.put(4, "Meet a calamity while being at your lowest luck");
        wof.put(3, "Set a prophecy on sequence 3+ player and make it true no matter what\n" +
                "(To make a prophecy type in chat - \"Nickname_of_target will die\")");
        wof.put(2, "Set a prophecy on sequence 2+ player and make it true no matter what\n" +
                "(To make a prophecy type in chat - \"Nickname_of_target will die\")");
        wof.put(1, "Make visionary beyonder place sleep, stun and seal on you");
        wof.put(0, "Wait for the right opportunity");
        description.put("wheel_of_fortune", wof);

        Map<Integer, String> visionary = new HashMap<>();
        visionary.put(5, "Find a way to immerse in a dream and be unwilling to wake up. Then find a way to wake up");
        visionary.put(4, "Be in the middle of massive event");
        visionary.put(3, "Cause significant amount of players to be asleep at the same time");
        visionary.put(2, "Uncover deepest fear of significant amount of players");
        visionary.put(1, "Write a story that involves at least half of the server");
        visionary.put(0, "Write a story that involves the entire server");
        description.put("visionary", visionary);
    }

    public static String getRitualDescription(String path, int seq){
        if(!description.containsKey(path)) return "";

        var list = description.get(path);
        if(!list.containsKey(seq)) return "";

        return list.get(seq);
    }
}
