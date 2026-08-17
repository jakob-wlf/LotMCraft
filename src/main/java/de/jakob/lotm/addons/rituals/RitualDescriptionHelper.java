package de.jakob.lotm.addons.rituals;

import de.jakob.lotm.addons.rituals.red_priest.Seq4;
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
        sun.put(0, "Immerse into deep void while enveiled with true darkness that symbolizes the destination of all things.\n" +
                "Note: you must be almost dead both physically and mentally");
        description.put("sun", sun);

        Map<Integer, String> red_priest = new HashMap<>();
        red_priest.put(5, "Capture weakened beyonder whose sequence is higher than yours");
        red_priest.put(4, "Help at least " + Seq4.MIN_ALLY_AMOUNT + " allies progress to your sequence");
        red_priest.put(3, "Win a faction war");
        red_priest.put(2, "Forcefully change the weather of a region without external aid");
        red_priest.put(1, "Win a war against far superior enemy nation");
        red_priest.put(0, "Put the world into war");
        description.put("red_priest", red_priest);

        Map<Integer, String> mother = new HashMap<>();
        mother.put(5, "Examine physical structure of various ordinary creatures");
        mother.put(4, "Collect large amount of life essence from beyonder creatures");
        mother.put(3, "Witness death of Mythical Creature of darkness pathway");
        mother.put(2, "Nurture your very own faction to it's maximum");
        mother.put(1, "Find dimension without any element and dimension filled with elements");
        mother.put(0, "Give birth to a Deity");
        description.put("mother", mother);

        Map<Integer, String> fool = new HashMap<>();
        fool.put(5, "Drink potion while under effect of buffing siren song");
        fool.put(4, "Orchestrate a grand performance before many spectators to kill " +
                "a Beyonder creature at the level of a demigod or higher");
        fool.put(3, "Be separated from reality for a long time");
        fool.put(2, "Return a piece of history that has been left behind to the present era");
        fool.put(0, "Fool time or fate once");
        description.put("fool", fool);

        Map<Integer, String> error = new HashMap<>();
        error.put(5, "Examine dreams of many players");
        error.put(4, "Convince" + de.jakob.lotm.addons.rituals.error.Seq4.AMOUNT
                + " targets to willingly offer all they possess to sustain you");
        error.put(2, "Replace high-tier target in it's own social circle");
        error.put(1, "Plunge a faction into temporal disorder");
        error.put(0, "Replace someone during \"Their\" Sequence 0 apotheosis ritual");
        description.put("error", error);

        Map<Integer, String> door = new HashMap<>();
        door.put(5, "Set up special coordinates in four completely different spots" +
                " which are all set up extremely far away from each other");
        door.put(4, "Seal hostile demigod");
        door.put(3, "Wander through different dimensions");
        door.put(2, "Wander through different dimensions that are inaccessible");
        door.put(1, "Find a way to establish sufficient mystical connections with objects from space");
        door.put(0, "Break out of the seal and banishment of a True God");
        description.put("door", door);

        Map<Integer, String> demoness = new HashMap<>();
        demoness.put(5, "Without substitutes, be burned for a long time");
        demoness.put(4, "Kill significant amount of players with plague");
        demoness.put(3, "Find and kill your mirror self");
        demoness.put(2, "Destroy at least 2 factions");
        demoness.put(1, "Advance during the signs and process of the apocalypse's arrival");
        demoness.put(0, "Collect sequence 1 characteristic of every pathway related to Chaos Sea");
        description.put("demoness", demoness);

        Map<Integer, String> darkness = new HashMap<>();
        darkness.put(5, "Find a way to enter Spirit World");
        darkness.put(4, "Collect bloodline characteristics of Mythical Creatures' descendants");
        darkness.put(3, "Drink the potion in near true death state");
        darkness.put(2, "Live isolated in darkness and silence for long period of time");
        darkness.put(1, "Bring Misfortune to an Archangel");
        darkness.put(0, "Induce eternal slumber upon A True Deity");
        description.put("darkness", darkness);
    }

    public static String getRitualDescription(String path, int seq){
        if(!description.containsKey(path)) return "";

        var list = description.get(path);
        if(!list.containsKey(seq)) return "";

        return list.get(seq);
    }
}
