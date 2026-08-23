package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Real state for the Endpoint marker. Stored in an attachment (like FoolingComponent)
 * rather than purely as a MobEffect so it cannot be removed by milk or other
 * effect-clearing mechanics — only a strong-enough purification cure or death clears it.
 */
public class EndpointComponent {
    private boolean active = false;
    private int casterSequence = 0;

    public static final String NBT_ACTIVE = "endpoint_active";
    public static final String NBT_CASTER_SEQUENCE = "endpoint_caster_sequence";

    public boolean isActive() {
        return active;
    }

    public int getCasterSequence() {
        return casterSequence;
    }

    public void apply(int casterSequence) {
        this.active = true;
        this.casterSequence = casterSequence;
    }

    public void clear() {
        this.active = false;
        this.casterSequence = 0;
    }

    public static final IAttachmentSerializer<CompoundTag, EndpointComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public EndpointComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new EndpointComponent();

                    component.active = tag.getBoolean(NBT_ACTIVE);
                    component.casterSequence = tag.getInt(NBT_CASTER_SEQUENCE);

                    return component;
                }

                @Override
                public CompoundTag write(EndpointComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean(NBT_ACTIVE, component.active);
                    tag.putInt(NBT_CASTER_SEQUENCE, component.casterSequence);

                    return tag;
                }
            };
}
