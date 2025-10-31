package de.jakob.lotm.entity.quests.impl;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest type that combines multiple sub-quests
 * All objectives must be completed for the quest to be finished
 */
public class CompoundQuest extends Quest {
    private List<Quest> subQuests;
    
    public CompoundQuest(String questId, String title, String description) {
        super(questId, title, description);
        this.subQuests = new ArrayList<>();
    }
    
    /**
     * Add a sub-quest objective
     */
    public CompoundQuest addSubQuest(Quest subQuest) {
        this.subQuests.add(subQuest);
        return this;
    }
    
    @Override
    public boolean checkCompletion(Player player, Level level) {
        for (Quest quest : subQuests) {
            if (!quest.checkCompletion(player, level)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Component getProgressText() {
        int completed = 0;
        for (Quest quest : subQuests) {
            if (quest.checkCompletion(null, null)) {
                completed++;
            }
        }
        return Component.literal("Objectives: " + completed + "/" + subQuests.size() + " completed");
    }
    
    @Override
    public void updateProgress(Player player, QuestUpdateEvent event) {
        for (Quest quest : subQuests) {
            quest.updateProgress(player, event);
        }
        
        if (checkCompletion(player, player.level())) {
            player.sendSystemMessage(Component.literal("§aQuest Completed: " + title));
        }
    }
    
    @Override
    public void onAccept(Player player) {
        super.onAccept(player);
        for (Quest quest : subQuests) {
            quest.onAccept(player);
        }
    }
    
    @Override
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestType", getQuestType());
        tag.putString("QuestId", questId);
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putBoolean("Completed", completed);
        tag.putInt("ExperienceReward", experienceReward);
        if (assignedPlayer != null) {
            tag.putUUID("AssignedPlayer", assignedPlayer);
        }
        
        ListTag subQuestList = new ListTag();
        for (Quest subQuest : subQuests) {
            subQuestList.add(subQuest.saveToNBT());
        }
        tag.put("SubQuests", subQuestList);
        
        return tag;
    }
    
    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.questId = tag.getString("QuestId");
        this.title = tag.getString("Title");
        this.description = tag.getString("Description");
        this.completed = tag.getBoolean("Completed");
        this.experienceReward = tag.getInt("ExperienceReward");
        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }
        
        // Sub-quests need to be reconstructed by QuestRegistry
        // This is handled externally
    }
    
    @Override
    public String getQuestType() {
        return "compound";
    }
    
    public List<Quest> getSubQuests() {
        return subQuests;
    }
    
    /**
     * Get detailed progress for all objectives
     */
    public List<Component> getDetailedProgress() {
        List<Component> progress = new ArrayList<>();
        for (int i = 0; i < subQuests.size(); i++) {
            Quest quest = subQuests.get(i);
            String status = quest.checkCompletion(null, null) ? "§a✓" : "§7○";
            progress.add(Component.literal(status + " " + quest.getProgressText().getString()));
        }
        return progress;
    }
}