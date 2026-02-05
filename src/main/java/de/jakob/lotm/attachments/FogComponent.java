package de.jakob.lotm.attachments;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncFogPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class FogComponent {

    private int fogIndex = 0;
    private long stopTime = 0;
    private Vec3f color = new Vec3f(1, 1, 1);

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
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), fogIndex, color.x(), color.y(), color.z()));
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

    public Vec3f getColor() {
        return color;
    }

    public void setColor(Vec3f color) {
        this.color = color;
    }

    public void setFogIndex(int fogIndex) {
        this.fogIndex = fogIndex;
    }

    public void setFogIndex(FOG_TYPE type) {
        this.fogIndex = type.getIndex();
    }

    public void setFogIndexAndSync(int transformationIndex, LivingEntity entity) {
        this.fogIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), transformationIndex, color.x(), color.y(), color.z()));
    }

    public void setFogIndexAndSync(FOG_TYPE type, LivingEntity entity) {
        this.fogIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), fogIndex, color.x(), color.y(), color.z()));
    }

    public void setFogColorAndSync(Vec3f color, LivingEntity entity) {
        this.color = color;
        PacketHandler.sendToAllPlayers(new SyncFogPacket(entity.getId(), isActive(), getFogIndex(), color.x(), color.y(), color.z()));
    }



    public static final IAttachmentSerializer<CompoundTag, FogComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public FogComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    FogComponent component = new FogComponent();
                    component.stopTime = tag.getLong("stopTime");
                    component.fogIndex = tag.getInt("fogIndex");
                    component.color = new Vec3f(
                            tag.getFloat("colorX"),
                            tag.getFloat("colorY"),
                            tag.getFloat("colorZ")
                    );
                    return component;
                }

                @Override
                public CompoundTag write(FogComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("stopTime", component.stopTime);
                    tag.putInt("fogIndex", component.fogIndex);
                    tag.putFloat("colorX", component.color.x());
                    tag.putFloat("colorY", component.color.y());
                    tag.putFloat("colorZ", component.color.z());
                    return tag;
                }
            };


    public enum FOG_TYPE {
        DROUGHT(0, 15f, 24.0f),
        BLIZZARD(1, 15f, 24.0f),
        FOG_OF_HISTORY(2, 7f, 12.0f),
        ADVANCING(3, 9f, 18.0f);

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