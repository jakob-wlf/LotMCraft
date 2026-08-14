package de.jakob.lotm.addons.rituals;

import de.jakob.lotm.addons.rituals.tyrant.Seq3;

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

        Map<Integer, String> tyrant = new HashMap<>();
        tyrant.put(5, "Find an ancient ocean guardian");
        tyrant.put(4, "Drink potion while facing natural disaster");
        tyrant.put(3, "Declare ocean as your domain, then kill anyone who is in it (if you kill the player - it will have greater effect). To declare - type this in chat:\n"+
                Seq3.message);
        tyrant.put(2, "Make your faction win a war, but you must not be an aggressor");
        tyrant.put(1, "Kill 3 angel players. Alternatively: kill 1 True Deity or 2 King of Angles");
        tyrant.put(0, "Become leader of level 3 faction and survive direct attack of True Deity");
        description.put("tyrant", tyrant);

        Map<Integer, String> sun = new HashMap<>();
        sun.put(5, "Drink the potion in pure darkness and buried in packed ice");
        sun.put(4, "Drink the potion in unstable state");
        sun.put(3, "Kill at least " + de.jakob.lotm.addons.rituals.sun.Seq3.AMOUNT
        + " demigods while following your justice");
        sun.put(2, "Find an item with at least an Angel-rank authority and a deep connection to the Sun in mysticism");
        sun.put(1, "Become worshipped");
        sun.put(0, "Immerse into the deep void while being almost dead with darkness corruption.\n" +
                "Note: you must be almost dead both physically and mentally");
        description.put("sun", sun);
    }

    public static String getRitualDescription(String path, int seq){
        if(!description.containsKey(path)) return "";

        var list = description.get(path);
        if(!list.containsKey(seq)) return "";

        return list.get(seq);
    }
}
