package de.jakob.lotm.entity.quests.impl;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Quest type that requires talking to a villager with a specific profession
 */
public class TalkToVillagerQuest extends Quest {
    private VillagerProfession targetProfession;
    private boolean talkedTo;
    
    public TalkToVillagerQuest(String questId, String title, String description, VillagerProfession targetProfession) {
        super(questId, title, description);
        this.targetProfession = targetProfession;
        this.talkedTo = false;
    }
    
    @Override
    public boolean checkCompletion(Player player, Level level) {
        return talkedTo;
    }
    
    @Override
    public Component getProgressText() {
        if (talkedTo) {
            return Component.literal("§aTalked to " + targetProfession.name());
        }
        return Component.literal("Talk to a " + targetProfession.name());
    }
    
    @Override
    public void updateProgress(Player player, QuestUpdateEvent event) {
        if (event.getType() == QuestUpdateEvent.EventType.VILLAGER_TALKED) {
            if (event.getTalkedToProfession() == targetProfession) {
                talkedTo = true;
                player.sendSystemMessage(Component.literal("§aQuest objective completed!"));
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
        tag.putString("TargetProfession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(targetProfession).toString());
        tag.putBoolean("TalkedTo", talkedTo);
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
        this.targetProfession = BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.parse(tag.getString("TargetProfession")));
        this.talkedTo = tag.getBoolean("TalkedTo");
        this.completed = tag.getBoolean("Completed");
        this.experienceReward = tag.getInt("ExperienceReward");
        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }
    }
    
    @Override
    public String getQuestType() {
        return "talk_to_villager";
    }
    
    public VillagerProfession getTargetProfession() {
        return targetProfession;
    }
    
    public boolean hasTalkedTo() {
        return talkedTo;
    }
}