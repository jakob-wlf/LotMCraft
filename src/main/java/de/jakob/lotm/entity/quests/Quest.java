package de.jakob.lotm.entity.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
    protected UUID questGiver; // The NPC who gave this quest
    protected long acceptedTime; // When the quest was accepted (in game ticks)
    protected boolean completed;

    private static final long EXPIRATION_TIME = 12000; // 10 minutes in ticks (20 ticks/sec * 60 sec * 10 min)

    public Quest(String questId, String title, String description) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.rewards = new ArrayList<>();
        this.experienceReward = 0;
        this.completed = false;
        this.acceptedTime = -1;
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
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestType", getQuestType());
        tag.putString("QuestId", questId);
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putInt("ExperienceReward", experienceReward);
        tag.putBoolean("Completed", completed);
        tag.putLong("AcceptedTime", acceptedTime);

        if (assignedPlayer != null) {
            tag.putUUID("AssignedPlayer", assignedPlayer);
        }

        if (questGiver != null) {
            tag.putUUID("QuestGiver", questGiver);
        }

        return tag;
    }

    /**
     * Load quest data from NBT
     */
    public void loadFromNBT(CompoundTag tag) {
        this.questId = tag.getString("QuestId");
        this.title = tag.getString("Title");
        this.description = tag.getString("Description");
        this.experienceReward = tag.getInt("ExperienceReward");
        this.completed = tag.getBoolean("Completed");
        this.acceptedTime = tag.getLong("AcceptedTime");

        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }

        if (tag.hasUUID("QuestGiver")) {
            this.questGiver = tag.getUUID("QuestGiver");
        }
    }

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

    public void setQuestGiver(UUID questGiverUUID) {
        this.questGiver = questGiverUUID;
    }

    public UUID getQuestGiver() {
        return questGiver;
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
     * Check if quest has expired (more than 10 minutes old)
     */
    public boolean hasExpired(long currentGameTime) {
        if (acceptedTime == -1) {
            return false; // Not yet accepted
        }
        return (currentGameTime - acceptedTime) > EXPIRATION_TIME;
    }

    /**
     * Check if quest giver still exists in the world
     */
    public boolean isQuestGiverValid(Level level) {
        if (questGiver == null || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Check if the entity with this UUID still exists
        return serverLevel.getEntity(questGiver) != null;
    }

    /**
     * Called when the quest is accepted by a player
     */
    public void onAccept(Player player) {
        assignToPlayer(player.getUUID());
        this.acceptedTime = player.level().getGameTime();
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