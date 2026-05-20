package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncTransformationPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class TransformationComponent {

    private boolean isTransformed = false;
    private int transformationIndex = 0;
    private String additionalData = "";

    public TransformationComponent() {
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
    }

    public void setTransformedAndSync(boolean transformed, LivingEntity entity) {
        isTransformed = transformed;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex, additionalData));
    }

    public int getTransformationIndex() {
        return transformationIndex;
    }

    public void setTransformationIndex(int transformationIndex) {
        this.transformationIndex = transformationIndex;
    }

    public void setTransformationIndex(TransformationType type) {
        this.transformationIndex = type.getIndex();
    }

    public void setTransformationIndexAndSync(int transformationIndex, LivingEntity entity) {
        this.transformationIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex, additionalData));
    }

    public void setTransformationIndexAndSync(TransformationType type, LivingEntity entity) {
        this.transformationIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex, additionalData));
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public void setAdditionalDataAndSync(String additionalData, LivingEntity entity) {
        this.additionalData = additionalData;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex, additionalData));
    }

    public void reset() {
        isTransformed = false;
        transformationIndex = 0;
        additionalData = "";
    }

    public boolean shouldCancelDefaultRendering() {
        return switch (transformationIndex) {
            case 4 -> false;
            default -> true;
        };
    }


    public static final IAttachmentSerializer<CompoundTag, TransformationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public TransformationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    TransformationComponent component = new TransformationComponent();
                    component.isTransformed = tag.getBoolean("active");
                    component.transformationIndex = tag.getInt("index");
                    component.additionalData = tag.getString("additionalData");
                    return component;
                }

                @Override
                public CompoundTag write(TransformationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("active", component.isTransformed);
                    tag.putInt("index", component.transformationIndex);
                    tag.putString("additionalData", component.additionalData);
                    return tag;
                }
            };


    public enum TransformationType {
        DEVIL(0),
        DESIRE_AVATAR(1),
        BEAR(2),
        SOLAR_ENVOY(3),
        WINGS_OF_LIGHT(4),
        FOG_OF_HISTORY(5),
        ENERGY(6),
        CONCEPTUALIZATION(7),
        PARASITATION(8),
        COFFIN(9),
        DREAM_DIVINATION(10),
        MYTHICAL_CREATURE(101);

        private final int index;
        TransformationType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
