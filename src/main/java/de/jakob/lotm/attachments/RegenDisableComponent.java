package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class RegenDisableComponent {
    private boolean isDisabled = false;
    private int disabledFor = 0;
    private int count = 0;

    public static final String NBT_IS_DISABLED = "is_regen_disabled";
    public static final String NBT_DISABLED_FOR = "regen_disabled_for";
    public static final String NBT_COUNT = "regen_count";

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public int getDisabledFor() {
        return disabledFor;
    }

    public void setDisabledFor(int disabledFor) {
        this.disabledFor = disabledFor;
    }

    public void incrementCount(){
        if(!isDisabled || disabledFor == 0) return;

        count++;
        if(count >= disabledFor){
            isDisabled = false;
            disabledFor = 0;
            count = 0;
        }
    }

    public void disableFor(int seconds){
        if(isDisabled) return;

        disabledFor = seconds;
        count = 0;
        isDisabled = true;
    }

    public static final IAttachmentSerializer<CompoundTag, RegenDisableComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public RegenDisableComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new RegenDisableComponent();

                    component.isDisabled = tag.getBoolean(NBT_IS_DISABLED);
                    component.disabledFor = tag.getInt(NBT_DISABLED_FOR);
                    component.count = tag.getInt(NBT_COUNT);

                    return component;
                }

                @Override
                public CompoundTag write(RegenDisableComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean(NBT_IS_DISABLED, component.isDisabled);
                    tag.putInt(NBT_DISABLED_FOR, component.disabledFor);
                    tag.putInt(NBT_COUNT, component.count);

                    return tag;
                }
            };
}
