package de.jakob.lotm.quest;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.quest.impl.DefendVillageQuest;
import de.jakob.lotm.quest.impl.KillZombiesQuest;
import de.jakob.lotm.quest.impl.kill_beyonder_quests.KillBeyonderGenericQuest;
import de.jakob.lotm.quest.impl.kill_beyonder_quests.KillTyrantSeq4Quest;

import java.util.HashMap;
import java.util.List;
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
        registerQuest(new KillZombiesQuest("kill_zombies", 25, 9));
        registerQuest(new DefendVillageQuest("defend_village", 8));
        registerQuest(new KillTyrantSeq4Quest("kill_tyrant_seq4", 5));
        registerQuest(new KillBeyonderGenericQuest("kill_seq4", 5, 4));
        registerQuest(new KillBeyonderGenericQuest("kill_seq3", 4, 3));
        registerQuest(new KillBeyonderGenericQuest("kill_seq2", 3, 2));
        registerQuest(new KillBeyonderGenericQuest("kill_seq1", 2, 1));
    }

    public static String getRandomQuestId() {
        if(QUESTS.isEmpty())
            return null;
        int index = (int) (Math.random() * QUESTS.size());
        return QUESTS.keySet().stream().toList().get(index);
    }

    public static String getRandomMatchingQuest(BeyonderNPCEntity npc) {
        if(QUESTS.isEmpty())
            return null;
        List<Quest> possibleQuests = QUESTS.values().stream().filter(q -> q.canGiveQuest(npc)).toList();
        if(possibleQuests.isEmpty())
            return null;
        int index = (int) (Math.random() * possibleQuests.size());
        return possibleQuests.get(index).getId();
    }
}
