package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnchorComponent {
    // Map of Anchor UUID to their "strength" or "loyalty" (0.0 to 1.0)
    private final Map<UUID, Float> anchors = new HashMap<>();

    public Map<UUID, Float> getAnchors() {
        return anchors;
    }

    public void addOrUpdateAnchor(UUID anchorUUID, float initialStrength) {
        anchors.put(anchorUUID, initialStrength);
    }

    public void removeAnchor(UUID anchorUUID) {
        anchors.remove(anchorUUID);
    }

    public static final IAttachmentSerializer<CompoundTag, AnchorComponent> SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public AnchorComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
            AnchorComponent component = new AnchorComponent();
            if (tag.contains("anchors", Tag.TAG_LIST)) {
                ListTag list = tag.getList("anchors", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag anchorTag = list.getCompound(i);
                    UUID uuid = anchorTag.getUUID("uuid");
                    float strength = anchorTag.getFloat("strength");
                    component.anchors.put(uuid, strength);
                }
            }
            return component;
        }

        @Override
        public CompoundTag write(AnchorComponent component, HolderLookup.Provider lookup) {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (Map.Entry<UUID, Float> entry : component.anchors.entrySet()) {
                CompoundTag anchorTag = new CompoundTag();
                anchorTag.putUUID("uuid", entry.getKey());
                anchorTag.putFloat("strength", entry.getValue());
                list.add(anchorTag);
            }
            tag.put("anchors", list);
            return tag;
        }
    };
}
