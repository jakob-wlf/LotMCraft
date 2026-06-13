package de.jakob.lotm.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.Optional;
import java.util.UUID;

public class CorruptedPlayerComponent {
    private boolean fullyCorrupted = false;
    private UUID npcUUID = null;

    public boolean isFullyCorrupted() {
        return fullyCorrupted;
    }

    public void setFullyCorrupted(boolean fullyCorrupted) {
        this.fullyCorrupted = fullyCorrupted;
    }

    public UUID getNpcUUID() {
        return npcUUID;
    }

    public void setNpcUUID(UUID npcUUID) {
        this.npcUUID = npcUUID;
    }

    public static final IAttachmentSerializer<CompoundTag, CorruptedPlayerComponent> SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public CorruptedPlayerComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            CorruptedPlayerComponent component = new CorruptedPlayerComponent();
            component.fullyCorrupted = tag.getBoolean("fullyCorrupted");
            if (tag.hasUUID("npcUUID")) {
                component.npcUUID = tag.getUUID("npcUUID");
            }
            return component;
        }

        @Override
        public CompoundTag write(CorruptedPlayerComponent attachment, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("fullyCorrupted", attachment.fullyCorrupted);
            if (attachment.npcUUID != null) {
                tag.putUUID("npcUUID", attachment.npcUUID);
            }
            return tag;
        }
    };
}
