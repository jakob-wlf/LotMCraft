package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;

public class ParasitationComponent {

    private boolean isParasiting = false;
    private boolean isParasited = false;
    private UUID parasiteUUID = null;
    private UUID parasitingUUID = null;

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
                    component.isParasiting = tag.getBoolean("is_parasiting");

                    component.parasitingUUID = tag.hasUUID("parasiting") ? tag.getUUID("parasiting") : null;
                    return component;
                }

                @Override
                public CompoundTag write(ParasitationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    if (component.parasiteUUID != null) {
                        tag.putUUID("hostUUID", component.parasiteUUID);
                    }
                    tag.putBoolean("isParasited", component.isParasited);
                    tag.putBoolean("is_parasiting", component.isParasiting);

                    if (component.parasitingUUID != null) {
                        tag.putUUID("parasiting", component.parasitingUUID);
                    }
                    return tag;
                }
            };

    public boolean isParasiting() {
        return isParasiting;
    }

    public void setParasiting(boolean parasiting) {
        isParasiting = parasiting;
    }

    public UUID getParasitingUUID() {
        return parasitingUUID;
    }

    public void setParasitingUUID(UUID parasitingUUID) {
        this.parasitingUUID = parasitingUUID;
    }
}
