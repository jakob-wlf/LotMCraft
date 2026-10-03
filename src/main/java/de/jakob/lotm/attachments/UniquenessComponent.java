package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Attachment that tracks whether a player has picked up their uniqueness,
 * and how many kills they have accumulated for apotheosis purposes.
 * Kill count persists after death; hasUniqueness is reset on death (entity respawns).
 */
public class UniquenessComponent {

    private boolean hasUniqueness = false;
    private String uniquenessPathway = "";
    private int killCount = 0;

    public UniquenessComponent() {}

    public boolean hasUniqueness() {
        return hasUniqueness;
    }

    public void setHasUniqueness(boolean hasUniqueness) {
        this.hasUniqueness = hasUniqueness;
    }

    public String getUniquenessPathway() {
        return uniquenessPathway;
    }

    public void setUniquenessPathway(String pathway) {
        this.uniquenessPathway = pathway == null ? "" : pathway;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementKillCount() {
        killCount++;
    }

    public void setKillCount(int count) {
        killCount = count;
    }

    public void resetKillCount() {
        killCount = 0;
    }

    public static final IAttachmentSerializer<CompoundTag, UniquenessComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public UniquenessComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    UniquenessComponent component = new UniquenessComponent();
                    component.hasUniqueness = tag.getBoolean("hasUniqueness");
                    component.uniquenessPathway = tag.getString("uniquenessPathway");
                    component.killCount = tag.getInt("killCount");
                    return component;
                }

                @Override
                public CompoundTag write(UniquenessComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("hasUniqueness", component.hasUniqueness);
                    tag.putString("uniquenessPathway", component.uniquenessPathway);
                    tag.putInt("killCount", component.killCount);
                    return tag;
                }
            };
}
