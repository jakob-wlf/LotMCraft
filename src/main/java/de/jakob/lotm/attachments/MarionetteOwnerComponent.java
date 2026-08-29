package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarionetteOwnerComponent {
    private HashSet<UUID> marionettes = new HashSet<>();

    public MarionetteOwnerComponent() {}

    // Getters and setters
    public Set<UUID> getMarionettes() { return marionettes; }
    public void setMarionettes(HashSet<UUID> marionettes) { this.marionettes = marionettes; }
    public void addMarionette(UUID marionetteUUID) { this.marionettes.add(marionetteUUID); }
    public void removeMarionette(UUID marionetteUUID) { this.marionettes.remove(marionetteUUID); }
    
    public static final IAttachmentSerializer<CompoundTag, MarionetteOwnerComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MarionetteOwnerComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MarionetteOwnerComponent component = new MarionetteOwnerComponent();
                    if (tag.contains("marionettes", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("marionettes", Tag.TAG_STRING);
                        for (int i = 0; i < list.size(); i++) {
                            component.addMarionette(UUID.fromString(list.getString(i)));
                        }
                    }
                    return component;
                }

                @Override
                public CompoundTag write(MarionetteOwnerComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    ListTag list = new ListTag();
                    for (UUID uuid : component.getMarionettes()) {
                        list.add(StringTag.valueOf(uuid.toString()));
                    }
                    tag.put("marionettes", list);
                    return tag;
                }
            };
}
