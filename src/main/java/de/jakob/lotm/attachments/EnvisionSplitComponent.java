package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.LinkedList;
import java.util.List;

public class EnvisionSplitComponent {
    public List<String> names = new LinkedList<>();

    public static final String NBT_NAMES = "names";
    public static final String NBT_IS_ENVISIONED = "is_envisioned";

    private boolean isEnvisioned = false;

    public void add(String name){
        if(!names.contains(name))
            names.add(name);
    }

    public void remove(String name){
        names.remove(name);
    }

    public boolean contains(String name){
        return names.contains(name);
    }

    public static final IAttachmentSerializer<CompoundTag, EnvisionSplitComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public EnvisionSplitComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new EnvisionSplitComponent();

                    ListTag namesTag = tag.getList(NBT_NAMES, Tag.TAG_STRING);

                    for (int i = 0; i < namesTag.size(); i++) {
                        component.names.add(namesTag.getString(i));
                    }

                    component.isEnvisioned = tag.getBoolean(NBT_IS_ENVISIONED);

                    return component;
                }

                @Override
                public CompoundTag write(EnvisionSplitComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    ListTag namesTag = new ListTag();

                    for (String name : component.names) {
                        namesTag.add(StringTag.valueOf(name));
                    }

                    tag.put(NBT_NAMES, namesTag);
                    tag.putBoolean(NBT_IS_ENVISIONED, component.isEnvisioned);

                    return tag;
                }
            };

    public boolean isEnvisioned() {
        return isEnvisioned;
    }

    public void setEnvisioned(boolean envisioned) {
        isEnvisioned = envisioned;
    }
}
