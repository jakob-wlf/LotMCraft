package de.jakob.lotm.entity.quests;

import java.util.ArrayList;
import java.util.List;

public class QuestHandler {

    private static final Quest[] possibleQuests = new Quest[]{};

    public static ArrayList<Quest> getPossibleQuests() {
        return new ArrayList<>(List.of(possibleQuests));
    }

    public static Quest getByIndex(int index) {
        return possibleQuests.length >= index ? null : possibleQuests[index];
    }
}
