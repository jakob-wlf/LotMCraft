package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class ParasitationComponent {

    private boolean isParasited = false;
    private UUID parasiteUUID = null;
    private boolean hasTimeWorm = false;
    private UUID timeWormOwnerUUID = null;
    private int timeWormSequence = 10;
    private final ArrayList<String> timeWormAddedAbilityIds = new ArrayList<>();

    public ParasitationComponent() {
    }

    public boolean isParasited() {
        return isParasited;
    }

    public void setParasited(boolean parasited) {
        isParasited = parasited;
    }

    public UUID getParasiteUUID() {
        return parasiteUUID;
    }

    public void setParasiteUUID(UUID parasiteUUID) {
        this.parasiteUUID = parasiteUUID;
    }

    public boolean hasTimeWorm() {
        return hasTimeWorm;
    }

    public void setHasTimeWorm(boolean hasTimeWorm) {
        this.hasTimeWorm = hasTimeWorm;
    }

    public UUID getTimeWormOwnerUUID() {
        return timeWormOwnerUUID;
    }

    public int getTimeWormSequence() {
        return timeWormSequence;
    }

    public void setTimeWormOwner(UUID ownerUUID, int sequence) {
        this.timeWormOwnerUUID = ownerUUID;
        this.timeWormSequence = sequence;
    }

    public List<String> getTimeWormAddedAbilityIds() {
        return timeWormAddedAbilityIds;
    }

    public void setTimeWormAddedAbilityIds(List<String> abilityIds) {
        timeWormAddedAbilityIds.clear();
        timeWormAddedAbilityIds.addAll(abilityIds);
    }

    public void clearTimeWormData() {
        hasTimeWorm = false;
        timeWormOwnerUUID = null;
        timeWormSequence = 10;
        timeWormAddedAbilityIds.clear();
    }

    public static final IAttachmentSerializer<CompoundTag, ParasitationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public ParasitationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    ParasitationComponent component = new ParasitationComponent();
                    component.parasiteUUID = tag.hasUUID("hostUUID") ? tag.getUUID("hostUUID") : null;
                    component.isParasited = tag.getBoolean("isParasited");
                    component.hasTimeWorm = tag.getBoolean("hasTimeWorm");
                    component.timeWormOwnerUUID = tag.hasUUID("timeWormOwnerUUID") ? tag.getUUID("timeWormOwnerUUID") : null;
                    component.timeWormSequence = tag.contains("timeWormSequence") ? tag.getInt("timeWormSequence") : 10;
                    if (tag.contains("timeWormAddedAbilityIds", CompoundTag.TAG_LIST)) {
                        var abilityIds = tag.getList("timeWormAddedAbilityIds", 8);
                        for (int i = 0; i < abilityIds.size(); i++) {
                            component.timeWormAddedAbilityIds.add(abilityIds.getString(i));
                        }
                    }
                    return component;
                }

                @Override
                public CompoundTag write(ParasitationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    if (component.parasiteUUID != null) {
                        tag.putUUID("hostUUID", component.parasiteUUID);
                    }
                    tag.putBoolean("isParasited", component.isParasited);
                    tag.putBoolean("hasTimeWorm", component.hasTimeWorm);
                    if (component.timeWormOwnerUUID != null) {
                        tag.putUUID("timeWormOwnerUUID", component.timeWormOwnerUUID);
                    }
                    tag.putInt("timeWormSequence", component.timeWormSequence);
                    var abilityIds = new net.minecraft.nbt.ListTag();
                    component.timeWormAddedAbilityIds.forEach(id -> abilityIds.add(net.minecraft.nbt.StringTag.valueOf(id)));
                    tag.put("timeWormAddedAbilityIds", abilityIds);
                    return tag;
                }
            };
}
