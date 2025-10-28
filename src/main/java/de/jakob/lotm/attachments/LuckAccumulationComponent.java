package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class LuckAccumulationComponent {
    private long ticksAccumulated = 0;
    public LuckAccumulationComponent() {}

    
    // Getters and setters

    public long getTicksAccumulated() { return ticksAccumulated; }

    public void setTicksAccumulated(long ticksAccumulated) {
        this.ticksAccumulated = ticksAccumulated;
    }

    public static final IAttachmentSerializer<CompoundTag, LuckAccumulationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public LuckAccumulationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    LuckAccumulationComponent component = new LuckAccumulationComponent();
                    component.ticksAccumulated = tag.getLong("ticksAccumulated");
                    return component;
                }

                @Override
                public CompoundTag write(LuckAccumulationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("ticksAccumulated", component.ticksAccumulated);
                    return tag;
                }
            };
}