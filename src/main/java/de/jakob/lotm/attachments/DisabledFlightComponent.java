package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class DisabledFlightComponent {

    private int cooldownTicks = 0;

    public DisabledFlightComponent() {
    }


    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }


    public static final IAttachmentSerializer<CompoundTag, DisabledFlightComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public DisabledFlightComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    DisabledFlightComponent component = new DisabledFlightComponent();
                    component.cooldownTicks = tag.getInt("cooldownTicks");
                    return component;
                }

                @Override
                public CompoundTag write(DisabledFlightComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("cooldownTicks", component.cooldownTicks);
                    return tag;
                }
            };
}
