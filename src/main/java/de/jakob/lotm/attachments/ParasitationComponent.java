package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;

public class ParasitationComponent {

    private boolean isParasited = false;
    private UUID parasiteUUID = null;

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

    public static final IAttachmentSerializer<CompoundTag, ParasitationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public ParasitationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    ParasitationComponent component = new ParasitationComponent();
                    component.parasiteUUID = tag.hasUUID("hostUUID") ? tag.getUUID("hostUUID") : null;
                    component.isParasited = tag.getBoolean("isParasited");
                    return component;
                }

                @Override
                public CompoundTag write(ParasitationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    if (component.parasiteUUID != null) {
                        tag.putUUID("hostUUID", component.parasiteUUID);
                    }
                    tag.putBoolean("isParasited", component.isParasited);
                    return tag;
                }
            };
}
