package de.jakob.lotm.entity.quests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all quest types
 */
public abstract class Quest {
    protected String questId;
    protected String title;
    protected String description;
    protected List<ItemStack> rewards;
    protected int experienceReward;
    protected UUID assignedPlayer;
    protected boolean completed;
    
    public Quest(String questId, String title, String description) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.rewards = new ArrayList<>();
        this.experienceReward = 0;
        this.completed = false;
    }
    
    /**
     * Check if the quest objectives are completed
     */
    public abstract boolean checkCompletion(Player player, Level level);
    
    /**
     * Get the current progress of the quest
     */
    public abstract Component getProgressText();
    
    /**
     * Update quest progress based on an event
     */
    public abstract void updateProgress(Player player, QuestUpdateEvent event);
    
    /**
     * Save quest data to NBT
     */
    public abstract CompoundTag saveToNBT();
    
    /**
     * Load quest data from NBT
     */
    public abstract void loadFromNBT(CompoundTag tag);
    
    /**
     * Add an item reward to this quest
     */
    public Quest addReward(ItemStack reward) {
        this.rewards.add(reward);
        return this;
    }
    
    /**
     * Set experience reward
     */
    public Quest setExperienceReward(int experience) {
        this.experienceReward = experience;
        return this;
    }
    
    /**
     * Give rewards to the player
     */
    public void giveRewards(Player player) {
        for (ItemStack reward : rewards) {
            player.addItem(reward.copy());
        }
        if (experienceReward > 0) {
            player.giveExperiencePoints(experienceReward);
        }
    }
    
    public void assignToPlayer(UUID playerUUID) {
        this.assignedPlayer = playerUUID;
    }
    
    public UUID getAssignedPlayer() {
        return assignedPlayer;
    }
    
    public String getQuestId() {
        return questId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public List<ItemStack> getRewards() {
        return rewards;
    }
    
    public int getExperienceReward() {
        return experienceReward;
    }
    
    /**
     * Called when the quest is accepted by a player
     */
    public void onAccept(Player player) {
        assignToPlayer(player.getUUID());
    }
    
    /**
     * Called when the quest is completed
     */
    public void onComplete(Player player) {
        this.completed = true;
        giveRewards(player);
    }
    
    /**
     * Get the quest type identifier
     */
    public abstract String getQuestType();
}