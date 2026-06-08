package de.jakob.lotm.attachments;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class EnvisionSplitComponent {
    public List<String> names = new LinkedList<>();
    public List<UUID> avatars = new LinkedList<>();

    public static final String NBT_NAMES = "names";
    public static final String NBT_IS_ENVISIONED = "is_envisioned";

    private boolean isEnvisioned = false;

    public void addAsAvatar(UUID id){
        avatars.add(id);
    }

    public void removeAsAvatar(UUID id){
        avatars.remove(id);
    }

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

                    var listTag = tag.getList("avatars", Tag.TAG_STRING);
                    List<UUID> uuids = new ArrayList<>();

                    for (int i = 0; i < listTag.size(); i++) {
                        uuids.add(UUID.fromString(listTag.getString(i)));
                    }

                    component.avatars = uuids;

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

                    ListTag listTag = new ListTag();

                    for (UUID uuid : component.avatars) {
                        listTag.add(StringTag.valueOf(uuid.toString()));
                    }
                    tag.put("avatars", listTag);

                    return tag;
                }
            };

    public boolean isEnvisioned() {
        return isEnvisioned;
    }

    public void setEnvisioned(boolean envisioned) {
        isEnvisioned = envisioned;
    }

    public boolean contains(LivingEntity entity){
        return names.contains(entity.getName().getString()) || avatars.contains(entity.getUUID());
    }

    public boolean willBeOutOfSlots(int seq1Amount){
        return avatars.size() + names.size() + 1 >= seq1Amount;
    }

    public void onJoin(ServerLevel level){
        for (var obj : avatars){
            var target = level.getEntity(obj);
            if(target == null){
                avatars.remove(obj);
            }
        }

        for(var obj : names){
            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(obj)).get();
            if(data.sequence() != 1){
                names.remove(obj);
            }
        }
    }
}
