package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class MiracleOfResurrectionComponent {

    private int resurrectionAttempts = 0;

    public MiracleOfResurrectionComponent() {}

    public int getResurrectionAttempts() {
        return resurrectionAttempts;
    }

    public void setResurrectionAttempts(int count) {
        this.resurrectionAttempts = count;
    }

    public static final IAttachmentSerializer<CompoundTag, MiracleOfResurrectionComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MiracleOfResurrectionComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MiracleOfResurrectionComponent component = new MiracleOfResurrectionComponent();
                    component.resurrectionAttempts = tag.getInt("resurrectionAttempts");
                    return component;
                }

                @Override
                public CompoundTag write(MiracleOfResurrectionComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("resurrectionAttempts", component.resurrectionAttempts);
                    return tag;
                }
            };
}
