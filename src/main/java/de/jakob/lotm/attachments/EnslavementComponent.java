package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;

/**
 * Tracks the Enslavement ability's master/slave relationship. Stored on both sides:
 * the slave holds their master's UUID (and whether their abilities are sealed), the
 * master holds their current slave's UUID so a caster can be limited to one slave
 * at a time without scanning every entity. Persists across restarts/relogs like
 * EndpointComponent/DeathDecreeMarkComponent; not copied on death since dying frees
 * the slave (matches Kill instantly clearing the mark).
 */
public class EnslavementComponent {
    private UUID masterUUID = null;
    private UUID slaveUUID = null;
    private boolean sealed = false;

    public static final String NBT_MASTER = "enslavement_master";
    public static final String NBT_SLAVE = "enslavement_slave";
    public static final String NBT_SEALED = "enslavement_sealed";

    public UUID getMaster() {
        return masterUUID;
    }

    public UUID getSlave() {
        return slaveUUID;
    }

    public boolean isSealed() {
        return sealed;
    }

    public void setMaster(UUID masterUUID) {
        this.masterUUID = masterUUID;
    }

    public void setSlave(UUID slaveUUID) {
        this.slaveUUID = slaveUUID;
    }

    public void setSealed(boolean sealed) {
        this.sealed = sealed;
    }

    public void clearMaster() {
        this.masterUUID = null;
        this.sealed = false;
    }

    public void clearSlave() {
        this.slaveUUID = null;
    }

    public static final IAttachmentSerializer<CompoundTag, EnslavementComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public EnslavementComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new EnslavementComponent();

                    if (tag.hasUUID(NBT_MASTER)) {
                        component.masterUUID = tag.getUUID(NBT_MASTER);
                    }
                    if (tag.hasUUID(NBT_SLAVE)) {
                        component.slaveUUID = tag.getUUID(NBT_SLAVE);
                    }
                    component.sealed = tag.getBoolean(NBT_SEALED);

                    return component;
                }

                @Override
                public CompoundTag write(EnslavementComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    if (component.masterUUID != null) {
                        tag.putUUID(NBT_MASTER, component.masterUUID);
                    }
                    if (component.slaveUUID != null) {
                        tag.putUUID(NBT_SLAVE, component.slaveUUID);
                    }
                    tag.putBoolean(NBT_SEALED, component.sealed);

                    return tag;
                }
            };
}
