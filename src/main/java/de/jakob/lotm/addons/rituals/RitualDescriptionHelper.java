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
        description.put("wheel_of_fortune", wof);
    }

    public static String getRitualDescription(String path, int seq){
        if(!description.containsKey(path)) return "";

        var list = description.get(path);
        if(!list.containsKey(seq)) return "";

        return list.get(seq);
    }
}
