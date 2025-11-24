package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class GuidingBookComponent {
    private boolean hasReceivedBook = false;
    public GuidingBookComponent() {}

    
    // Getters and setters


    public boolean isHasReceivedBook() {
        return hasReceivedBook;
    }

    public void setHasReceivedBook(boolean hasReceivedBook) {
        this.hasReceivedBook = hasReceivedBook;
    }

    public static final IAttachmentSerializer<CompoundTag, GuidingBookComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public GuidingBookComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    GuidingBookComponent component = new GuidingBookComponent();
                    component.hasReceivedBook = tag.getBoolean("hasReceivedBook");
                    return component;
                }

                @Override
                public CompoundTag write(GuidingBookComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("hasReceivedBook", component.hasReceivedBook);
                    return tag;
                }
            };
}