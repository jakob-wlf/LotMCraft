package de.jakob.lotm.entity.quests.impl;

import de.jakob.lotm.entity.quests.Quest;
import de.jakob.lotm.entity.quests.QuestUpdateEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Quest type that requires collecting/giving specific items
 */
public class CollectItemsQuest extends Quest {
    private Item targetItem;
    private int requiredAmount;
    private boolean consumeItems; // Whether to take the items from player
    
    public CollectItemsQuest(String questId, String title, String description, Item targetItem, int requiredAmount, boolean consumeItems) {
        super(questId, title, description);
        this.targetItem = targetItem;
        this.requiredAmount = requiredAmount;
        this.consumeItems = consumeItems;
    }
    
    @Override
    public boolean checkCompletion(Player player, Level level) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == targetItem) {
                count += stack.getCount();
            }
        }
        return count >= requiredAmount;
    }
    
    @Override
    public Component getProgressText() {
        return Component.literal("Collect " + requiredAmount + " " + targetItem.getDescription().getString());
    }
    
    @Override
    public void updateProgress(Player player, QuestUpdateEvent event) {
        // This quest checks inventory directly
    }
    
    @Override
    public void onComplete(Player player) {
        if (consumeItems) {
            int remaining = requiredAmount;
            for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().items.get(i);
                if (stack.getItem() == targetItem) {
                    int toRemove = Math.min(remaining, stack.getCount());
                    stack.shrink(toRemove);
                    remaining -= toRemove;
                }
            }
        }
        super.onComplete(player);
    }
    
    @Override
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("QuestType", getQuestType());
        tag.putString("QuestId", questId);
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putString("TargetItem", BuiltInRegistries.ITEM.getKey(targetItem).toString());
        tag.putInt("RequiredAmount", requiredAmount);
        tag.putBoolean("ConsumeItems", consumeItems);
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
        this.targetItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("TargetItem")));
        this.requiredAmount = tag.getInt("RequiredAmount");
        this.consumeItems = tag.getBoolean("ConsumeItems");
        this.completed = tag.getBoolean("Completed");
        this.experienceReward = tag.getInt("ExperienceReward");
        if (tag.hasUUID("AssignedPlayer")) {
            this.assignedPlayer = tag.getUUID("AssignedPlayer");
        }
    }
    
    @Override
    public String getQuestType() {
        return "collect_items";
    }
    
    public Item getTargetItem() {
        return targetItem;
    }
    
    public int getRequiredAmount() {
        return requiredAmount;
    }
    
    public boolean shouldConsumeItems() {
        return consumeItems;
    }
}