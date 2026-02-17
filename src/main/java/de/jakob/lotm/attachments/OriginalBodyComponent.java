package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;

public class OriginalBodyComponent {
    private UUID ownerUUID = null;
    private UUID bodyUUID = null;
    private UUID targetUUID = null;
    private CompoundTag targetEntity = null;

    public OriginalBodyComponent() {}

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID controllerUUID) {
        this.ownerUUID = controllerUUID;
    }

    public void removeOwnerUUID() {
        this.ownerUUID = null;
    }


    public UUID getBodyUUID() {
        return bodyUUID;
    }

    public void setBodyUUID(UUID controllerUUID) {
        this.bodyUUID = controllerUUID;
    }

    public void removeBodyUUID() {
        this.bodyUUID = null;
    }


    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID controllerUUID) {
        this.targetUUID = controllerUUID;
    }

    public void removeTargetUUID() {
        this.targetUUID = null;
    }

    public CompoundTag getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(CompoundTag controllerUUID) {
        this.targetEntity = controllerUUID;
    }

    public void removeTargetEntity() {
        this.targetEntity = null;
    }


    public static final IAttachmentSerializer<CompoundTag, OriginalBodyComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public OriginalBodyComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    OriginalBodyComponent component = new OriginalBodyComponent();
                    if (tag.hasUUID("ownerUUID")) component.ownerUUID = tag.getUUID("ownerUUID");
                    if (tag.hasUUID("bodyUUID")) component.bodyUUID = tag.getUUID("bodyUUID");
                    if (tag.hasUUID("targetUUID")) component.targetUUID = tag.getUUID("targetUUID");
                    if (tag.hasUUID("targetEntity")) component.targetEntity = tag.getCompound("targetEntity");
                    return component;
                }

                @Override
                public CompoundTag write(OriginalBodyComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    if (component.ownerUUID != null) tag.putUUID("ownerUUID", component.ownerUUID);
                    if (component.bodyUUID != null) tag.putUUID("bodyUUID", component.bodyUUID);
                    if (component.targetUUID != null) tag.putUUID("targetUUID", component.targetUUID);
                    if (component.targetEntity != null) tag.put("targetEntity", component.targetEntity);
                    return tag;
                }
            };
}
