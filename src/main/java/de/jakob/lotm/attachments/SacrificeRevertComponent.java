package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SacrificeRevertComponent {

    private long revertAtGameTime = -1; // -1 = not active
    private int revertToSequence = -1;
    private String pathway = "";
    private float savedDigestion = 0.0f;

    public SacrificeRevertComponent() {}

    public boolean isActive() {
        return revertAtGameTime >= 0;
    }

    public long getRevertAtGameTime() { return revertAtGameTime; }
    public int getRevertToSequence() { return revertToSequence; }
    public String getPathway() { return pathway; }
    public float getSavedDigestion() { return savedDigestion; }

    public void set(long revertAtGameTime, int revertToSequence, String pathway, float savedDigestion) {
        this.revertAtGameTime = revertAtGameTime;
        this.revertToSequence = revertToSequence;
        this.pathway = pathway;
        this.savedDigestion = savedDigestion;
    }

    public void clear() {
        this.revertAtGameTime = -1;
        this.revertToSequence = -1;
        this.pathway = "";
        this.savedDigestion = 0.0f;
    }

    public static final IAttachmentSerializer<CompoundTag, SacrificeRevertComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SacrificeRevertComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SacrificeRevertComponent c = new SacrificeRevertComponent();
                    c.revertAtGameTime = tag.getLong("revertAtGameTime");
                    c.revertToSequence = tag.getInt("revertToSequence");
                    c.pathway = tag.getString("pathway");
                    c.savedDigestion = tag.getFloat("savedDigestion");
                    return c;
                }

                @Override
                public CompoundTag write(SacrificeRevertComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("revertAtGameTime", component.revertAtGameTime);
                    tag.putInt("revertToSequence", component.revertToSequence);
                    tag.putString("pathway", component.pathway);
                    tag.putFloat("savedDigestion", component.savedDigestion);
                    return tag;
                }
            };
}
