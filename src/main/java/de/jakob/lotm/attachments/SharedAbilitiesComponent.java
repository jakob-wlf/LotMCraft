package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the abilities a player contributes to a Red Priest team, keyed by the leader's UUID.
 * A player may only ever be in one team at a time, but we key by leaderUUID to allow safe cleanup.
 */
public class SharedAbilitiesComponent {

    private final Map<String, List<String>> contributions;

    public SharedAbilitiesComponent() {
        this.contributions = new HashMap<>();
    }

    private SharedAbilitiesComponent(Map<String, List<String>> contributions) {
        this.contributions = contributions;
    }

    /**
     * Returns a new instance with the contributions for the given leader set to the provided list.
     */
    public SharedAbilitiesComponent setContributions(String leaderUUID, List<String> abilityIds) {
        Map<String, List<String>> newMap = new HashMap<>(this.contributions);
        newMap.put(leaderUUID, new ArrayList<>(abilityIds));
        return new SharedAbilitiesComponent(newMap);
    }

    /**
     * Returns the ability IDs contributed under the given leader UUID, or an empty list if none.
     */
    public List<String> getContributions(String leaderUUID) {
        return contributions.getOrDefault(leaderUUID, List.of());
    }

    /**
     * Returns a new instance with the contributions for the given leader removed.
     */
    public SharedAbilitiesComponent removeContributions(String leaderUUID) {
        Map<String, List<String>> newMap = new HashMap<>(this.contributions);
        newMap.remove(leaderUUID);
        return new SharedAbilitiesComponent(newMap);
    }

    /**
     * Returns the full contributions map (unmodifiable view).
     */
    public Map<String, List<String>> getAllContributions() {
        return Map.copyOf(contributions);
    }

    public static final IAttachmentSerializer<CompoundTag, SharedAbilitiesComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SharedAbilitiesComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SharedAbilitiesComponent component = new SharedAbilitiesComponent();
                    CompoundTag entriesTag = tag.getCompound("contributions");
                    Map<String, List<String>> map = new HashMap<>();
                    for (String key : entriesTag.getAllKeys()) {
                        ListTag listTag = entriesTag.getList(key, Tag.TAG_STRING);
                        List<String> ids = new ArrayList<>();
                        for (int i = 0; i < listTag.size(); i++) {
                            ids.add(listTag.getString(i));
                        }
                        map.put(key, ids);
                    }
                    return new SharedAbilitiesComponent(map);
                }

                @Override
                public CompoundTag write(SharedAbilitiesComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    CompoundTag entriesTag = new CompoundTag();
                    for (Map.Entry<String, List<String>> entry : component.contributions.entrySet()) {
                        ListTag listTag = new ListTag();
                        for (String id : entry.getValue()) {
                            listTag.add(StringTag.valueOf(id));
                        }
                        entriesTag.put(entry.getKey(), listTag);
                    }
                    tag.put("contributions", entriesTag);
                    return tag;
                }
            };
}
