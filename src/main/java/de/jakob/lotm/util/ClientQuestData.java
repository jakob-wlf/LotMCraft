package de.jakob.lotm.util;

import net.minecraft.world.item.ItemStack;
import java.util.*;

/**
 * Client-side storage for quest data received from server
 */
public class ClientQuestData {
    private static Set<String> completedQuests = new HashSet<>();
    private static String activeQuestId = null;
    private static float activeQuestProgress = 0f;
    private static String activeQuestName = "";
    private static String activeQuestDescription = "";
    private static List<ItemStack> activeQuestRewards = new ArrayList<>();
    private static int activeQuestDigestionReward = 0;

    public static void setCompletedQuests(Set<String> quests) {
        completedQuests = new HashSet<>(quests);
    }

    public static void setActiveQuest(String questId, float progress, String name, String description, 
                                      List<ItemStack> rewards, int digestionReward) {
        activeQuestId = questId;
        activeQuestProgress = progress;
        activeQuestName = name;
        activeQuestDescription = description;
        activeQuestRewards = new ArrayList<>(rewards);
        activeQuestDigestionReward = digestionReward;
    }

    public static void clearActiveQuest() {
        activeQuestId = null;
        activeQuestProgress = 0f;
        activeQuestName = "";
        activeQuestDescription = "";
        activeQuestRewards.clear();
        activeQuestDigestionReward = 0;
    }

    public static Set<String> getCompletedQuests() {
        return new HashSet<>(completedQuests);
    }

    public static boolean hasActiveQuest() {
        return activeQuestId != null;
    }

    public static String getActiveQuestId() {
        return activeQuestId;
    }

    public static float getActiveQuestProgress() {
        return activeQuestProgress;
    }

    public static String getActiveQuestName() {
        return activeQuestName;
    }

    public static String getActiveQuestDescription() {
        return activeQuestDescription;
    }

    public static List<ItemStack> getActiveQuestRewards() {
        return new ArrayList<>(activeQuestRewards);
    }

    public static int getActiveQuestDigestionReward() {
        return activeQuestDigestionReward;
    }
}