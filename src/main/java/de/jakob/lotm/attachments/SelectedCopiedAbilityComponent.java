package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SelectedCopiedAbilityComponent {
    private int selectedIndex = -1;
    private int remainingUses = 0;

    public SelectedCopiedAbilityComponent() {}

    public int getSelectedIndex() { return selectedIndex; }
    public int getRemainingUses() { return remainingUses; }

    public void setSelection(int index, int uses) {
        this.selectedIndex = index;
        this.remainingUses = uses;
    }

    public boolean hasSelection() { return selectedIndex >= 0; }
    public void clear() { selectedIndex = -1; remainingUses = 0; }

    public static final IAttachmentSerializer<CompoundTag, SelectedCopiedAbilityComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SelectedCopiedAbilityComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SelectedCopiedAbilityComponent component = new SelectedCopiedAbilityComponent();
                    component.selectedIndex = tag.getInt("SelectedIndex");
                    component.remainingUses = tag.getInt("RemainingUses");
                    return component;
                }

                @Override
                public CompoundTag write(SelectedCopiedAbilityComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("SelectedIndex", component.selectedIndex);
                    tag.putInt("RemainingUses", component.remainingUses);
                    return tag;
                }
            };
}
