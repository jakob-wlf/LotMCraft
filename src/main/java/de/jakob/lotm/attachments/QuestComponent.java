package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class QuestComponent {

    private HashSet<String> completedQuests = new HashSet<>();
    private HashMap<String, Float> questProgress = new HashMap<>();
    private HashMap<String, Vec3> questLocation = new HashMap<>();
    private HashMap<String, List<ItemStack>> questRewardCache = new HashMap<>();

    public QuestComponent() {}

    public HashSet<String> getCompletedQuests() {
        return completedQuests;
    }

    public HashMap<String, Float> getQuestProgress() {
        return questProgress;
    }

    public HashMap<String, Vec3> getQuestLocation() {
        return questLocation;
    }

    public HashMap<String, List<ItemStack>> getQuestRewardCache() {
        return questRewardCache;
    }

    public static final IAttachmentSerializer<CompoundTag, QuestComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public QuestComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    QuestComponent component = new QuestComponent();
                    if (tag.contains("CompletedQuests", 9)) {
                        ListTag completedList = tag.getList("CompletedQuests", Tag.TAG_STRING);
                        for (int i = 0; i < completedList.size(); i++) {
                            component.completedQuests.add(completedList.getString(i));
                        }
                    }

                    if (tag.contains("QuestProgress", 10)) {
                        CompoundTag progressTag = tag.getCompound("QuestProgress");
                        for (String questId : progressTag.getAllKeys()) {
                            component.questProgress.put(questId, progressTag.getFloat(questId));
                        }
                    }

                    if(tag.contains("QuestLocation", 10)) {
                        CompoundTag locationTag = tag.getCompound("QuestLocation");
                        for (String questId : locationTag.getAllKeys()) {
                            CompoundTag vecTag = locationTag.getCompound(questId);
                            float x = vecTag.getFloat("x");
                            float y = vecTag.getFloat("y");
                            float z = vecTag.getFloat("z");
                            component.questLocation.put(questId, new Vec3(x, y, z));
                        }
                    }

                    if (tag.contains("QuestRewardCache", Tag.TAG_COMPOUND)) {
                        CompoundTag rewardTag = tag.getCompound("QuestRewardCache");
                        for (String questId : rewardTag.getAllKeys()) {
                            ListTag rewardList = rewardTag.getList(questId, Tag.TAG_COMPOUND);
                            List<ItemStack> cachedRewards = new ArrayList<>();
                            for (int i = 0; i < rewardList.size(); i++) {
                                Tag stackTag = rewardList.get(i);
                                ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, stackTag)
                                        .result()
                                        .orElse(ItemStack.EMPTY);
                                if (!stack.isEmpty()) {
                                    cachedRewards.add(stack);
                                }
                            }
                            component.questRewardCache.put(questId, cachedRewards);
                        }
                    }

                    return component;
                }

                @Override
                public CompoundTag write(QuestComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    ListTag completedList = new ListTag();
                    for (String questId : component.completedQuests) {
                        completedList.add(StringTag.valueOf(questId));
                    }
                    tag.put("CompletedQuests", completedList);

                    CompoundTag progressTag = new CompoundTag();
                    for (String questId : component.questProgress.keySet()) {
                        progressTag.putFloat(questId, component.questProgress.get(questId));
                    }
                    tag.put("QuestProgress", progressTag);

                    CompoundTag locationTag = new CompoundTag();
                    for(String questId : component.questLocation.keySet()) {
                        Vec3 vec = component.questLocation.get(questId);
                        CompoundTag vecTag = new CompoundTag();
                        vecTag.putFloat("x", (float) vec.x);
                        vecTag.putFloat("y", (float) vec.y);
                        vecTag.putFloat("z", (float) vec.z);
                        locationTag.put(questId, vecTag);
                    }

                    CompoundTag rewardTag = new CompoundTag();
                    for (String questId : component.questRewardCache.keySet()) {
                        ListTag rewardList = new ListTag();
                        for (ItemStack stack : component.questRewardCache.get(questId)) {
                            ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, stack)
                                    .result()
                                    .ifPresent(rewardList::add);
                        }
                        rewardTag.put(questId, rewardList);
                    }
                    tag.put("QuestRewardCache", rewardTag);

                    return tag;
                }
            };
}
