package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class KillCountComponent {

    private int killCount = 0;

    public KillCountComponent() {}

    public int getKillCount() {
        return killCount;
    }

    public void increment() {
        killCount++;
    }

    public void setKillCount(int count) {
        killCount = count;
    }

    public static final IAttachmentSerializer<CompoundTag, KillCountComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public KillCountComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    KillCountComponent component = new KillCountComponent();
                    component.killCount = tag.getInt("killCount");
                    return component;
                }

                @Override
                public CompoundTag write(KillCountComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("killCount", component.killCount);
                    return tag;
                }
            };
}
