package de.jakob.lotm.entity.quests.impl;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Quest type that requires killing a certain number of specific mobs
 */
public class KillMobsQuest extends Quest {
    private EntityType<?> targetMob;
    private int requiredKills;
    private int currentKills;
    
    public KillMobsQuest(String questId, String title, String description, EntityType<?> targetMob, int requiredKills) {
        super(questId, title, description);
        this.targetMob = targetMob;
        this.requiredKills = requiredKills;
        this.currentKills = 0;
    }
    
    @Override
    public boolean checkCompletion(Player player, Level level) {
        return currentKills >= requiredKills;
    }
    
    @Override
    public Component getProgressText() {
        return Component.literal(currentKills + "/" + requiredKills + " " + targetMob.getDescription().getString() + " killed");
    }
    
    @Override
    public void updateProgress(Player player, QuestUpdateEvent event) {
        if (event.getType() == QuestUpdateEvent.EventType.MOB_KILLED) {
            if (event.getEntityType() == targetMob) {
                currentKills++;
                if (checkCompletion(player, player.level())) {
                    player.sendSystemMessage(Component.literal("§aQuest Completed: " + title));
                }
            }
        }
    }
    
    @Override
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestType", getQuestType());
        tag.putString("QuestId", questId);
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putString("TargetMob", BuiltInRegistries.ENTITY_TYPE.getKey(targetMob).toString());
        tag.putInt("RequiredKills", requiredKills);
        tag.putInt("CurrentKills", currentKills);
        tag.putBoolean("Completed", completed);
        tag.putInt("ExperienceReward", experienceReward);
        if (assignedPlayer != null) {
            tag.putUUID("AssignedPlayer", assignedPlayer);
        }
        return tag;
    }
    
    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.questId = tag.getString("QuestId");
        this.title = tag.getString("Title");
        this.description = tag.getString("Description");
        this.targetMob = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("TargetMob")));
        this.requiredKills = tag.getInt("RequiredKills");
        this.currentKills = tag.getInt("CurrentKills");
        this.completed = tag.getBoolean("Completed");
        this.experienceReward = tag.getInt("ExperienceReward");
        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }
    }
    
    @Override
    public String getQuestType() {
        return "kill_mobs";
    }
    
    public EntityType<?> getTargetMob() {
        return targetMob;
    }
    
    public int getRequiredKills() {
        return requiredKills;
    }
    
    public int getCurrentKills() {
        return currentKills;
    }
}