package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncFogPacket;
import de.jakob.lotm.network.packets.SyncShaderPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class FogComponent {

    private int fogIndex = 0;
    private long stopTime = 0;

    public FogComponent() {
    }

    public boolean isActive() {
        return stopTime > System.currentTimeMillis();
    }

    public void setActive(boolean shaderActive) {
        if (shaderActive) {
            stopTime = System.currentTimeMillis() + 2000;
        } else {
            stopTime = 0;
        }
    }

    public void setActiveAndSync(boolean transformed, LivingEntity entity) {
        setActive(transformed);
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), fogIndex));
    }

    public int getFogIndex() {
        return fogIndex;
    }

    public FOG_TYPE getFogType() {
        for (FOG_TYPE type : FOG_TYPE.values()) {
            if (type.getIndex() == fogIndex) {
                return type;
            }
        }
        return null;
    }

    public void setFogIndex(int fogIndex) {
        this.fogIndex = fogIndex;
    }

    public void setFogIndex(FOG_TYPE type) {
        this.fogIndex = type.getIndex();
    }

    public void setFogIndexAndSync(int transformationIndex, LivingEntity entity) {
        this.fogIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), transformationIndex));
    }

    public void setFogIndexAndSync(FOG_TYPE type, LivingEntity entity) {
        this.fogIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), fogIndex));
    }



    public static final IAttachmentSerializer<CompoundTag, FogComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public FogComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    FogComponent component = new FogComponent();
                    component.stopTime = tag.getLong("stopTime");
                    component.fogIndex = tag.getInt("fogIndex");
                    return component;
                }

                @Override
                public CompoundTag write(FogComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("stopTime", component.stopTime);
                    tag.putInt("fogIndex", component.fogIndex);
                    return tag;
                }
            };


    public enum FOG_TYPE {
        DROUGHT(0, 15f, 24.0f),
        BLIZZARD(1, 15f, 24.0f),
        FOG_OF_HISTORY(2, 7f, 12.0f);

        private final int index;
        private final float nearPlaneDistance;
        private final float farPlaneDistance;
        FOG_TYPE(int index, float nearPlaneDistance, float farPlaneDistance) {
            this.index = index;
            this.nearPlaneDistance = nearPlaneDistance;
            this.farPlaneDistance = farPlaneDistance;
        }

        public int getIndex() {
            return index;
        }

        public float getNearPlaneDistance() {
            return nearPlaneDistance;
        }

        public float getFarPlaneDistance() {
            return farPlaneDistance;
        }
    }
}
