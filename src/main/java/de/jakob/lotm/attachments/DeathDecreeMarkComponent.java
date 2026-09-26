package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Tracks accumulated Death Decree marks on a target. Stacks persist across separate
 * casts (and cooldowns) and only reset via a strong-enough purification cure — mirrors
 * EndpointComponent so it cannot be cleared by anything other than that or death.
 */
public class DeathDecreeMarkComponent {
    private int stacks = 0;
    private int casterSequence = 0;

    public static final String NBT_STACKS = "death_decree_stacks";
    public static final String NBT_CASTER_SEQUENCE = "death_decree_caster_sequence";

    public int getStacks() {
        return stacks;
    }

    public int getCasterSequence() {
        return casterSequence;
    }

    public void addStack(int casterSequence) {
        this.stacks++;
        this.casterSequence = casterSequence;
    }

    public void clear() {
        this.stacks = 0;
        this.casterSequence = 0;
    }

    public static final IAttachmentSerializer<CompoundTag, DeathDecreeMarkComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public DeathDecreeMarkComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new DeathDecreeMarkComponent();

                    component.stacks = tag.getInt(NBT_STACKS);
                    component.casterSequence = tag.getInt(NBT_CASTER_SEQUENCE);

                    return component;
                }

                @Override
                public CompoundTag write(DeathDecreeMarkComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putInt(NBT_STACKS, component.stacks);
                    tag.putInt(NBT_CASTER_SEQUENCE, component.casterSequence);

                    return tag;
                }
            };
}
