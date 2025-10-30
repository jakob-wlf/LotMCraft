package de.jakob.lotm.entity.quests;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores quest data for a player
 * Attach this to the player using an attachment or capability
 */
public class PlayerQuestData {
    private List<Quest> activeQuests;
    private List<UUID> assignedNPCs; // Track which NPCs assigned quests
    
    public PlayerQuestData() {
        this.activeQuests = new ArrayList<>();
        this.assignedNPCs = new ArrayList<>();
    }
    
    public void addQuest(Quest quest, UUID npcUUID) {
        activeQuests.add(quest);
        assignedNPCs.add(npcUUID);
    }
    
    public void removeQuest(Quest quest) {
        int index = activeQuests.indexOf(quest);
        if (index >= 0) {
            activeQuests.remove(index);
            if (index < assignedNPCs.size()) {
                assignedNPCs.remove(index);
            }
        }
    }
    
    public List<Quest> getActiveQuests() {
        return activeQuests;
    }
    
    public Quest getQuestByNPC(UUID npcUUID) {
        int index = assignedNPCs.indexOf(npcUUID);
        if (index >= 0 && index < activeQuests.size()) {
            return activeQuests.get(index);
        }
        return null;
    }
    
    public boolean hasQuestFromNPC(UUID npcUUID) {
        return assignedNPCs.contains(npcUUID);
    }
    
    public void updateQuests(Player player, QuestUpdateEvent event) {
        for (Quest quest : activeQuests) {
            if (!quest.isCompleted()) {
                quest.updateProgress(player, event);
            }
        }
    }
    
    /**
     * Check all active quests for completion
     */
    public void checkQuestCompletions(Player player) {
        for (Quest quest : new ArrayList<>(activeQuests)) {
            if (!quest.isCompleted() && quest.checkCompletion(player, player.level())) {
                // Quest is ready to be completed but not yet turned in
            }
        }
    }

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag questList = new ListTag();
        for (Quest quest : activeQuests) {
            questList.add(quest.saveToNBT());
        }
        tag.put("ActiveQuests", questList);

        ListTag npcList = new ListTag();
        for (UUID uuid : assignedNPCs) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("NPC", uuid);
            npcList.add(uuidTag);
        }
        tag.put("AssignedNPCs", npcList);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag) {
        activeQuests.clear();
        assignedNPCs.clear();

        if (tag.contains("ActiveQuests")) {
            ListTag questList = tag.getList("ActiveQuests", Tag.TAG_COMPOUND);
            for (Tag questTag : questList) {
                Quest quest = QuestRegistry.loadQuestFromNBT((CompoundTag) questTag);
                if (quest != null) {
                    activeQuests.add(quest);
                }
            }
        }

        if (tag.contains("AssignedNPCs")) {
            ListTag npcList = tag.getList("AssignedNPCs", Tag.TAG_COMPOUND);
            for (Tag npcTag : npcList) {
                CompoundTag uuidTag = (CompoundTag) npcTag;
                assignedNPCs.add(uuidTag.getUUID("NPC"));
            }
        }
    }
}