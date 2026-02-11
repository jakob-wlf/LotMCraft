package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.HashMap;
import java.util.HashSet;

public class QuestComponent {

    private HashSet<String> completedQuests = new HashSet<>();
    private HashMap<String, Float> questProgress = new HashMap<>();

    public QuestComponent() {}

    public HashSet<String> getCompletedQuests() {
        return completedQuests;
    }

    public HashMap<String, Float> getQuestProgress() {
        return questProgress;
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
                    return tag;
                }
            };
}