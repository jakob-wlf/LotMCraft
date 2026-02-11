package de.jakob.lotm.quest;

import de.jakob.lotm.quest.impl.KillZombiesQuest;

import java.util.HashMap;
import java.util.Map;

public class QuestRegistry {

    private static final Map<String, Quest> QUESTS = new HashMap<>();

    public static void registerQuest(Quest quest) {
        QUESTS.put(quest.getId(), quest);
    }

    public static Quest getQuest(String id) {
        return QUESTS.get(id);
    }

    public static Map<String, Quest> getQuests() {
        return QUESTS;
    }

    public static void init() {
        registerQuest(new KillZombiesQuest("kill_zombies", 25));
    }

    public static String getRandomQuestId() {
        if(QUESTS.isEmpty())
            return null;
        int index = (int) (Math.random() * QUESTS.size());
        return QUESTS.keySet().stream().toList().get(index);
    }
}
