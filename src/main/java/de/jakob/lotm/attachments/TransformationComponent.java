package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncTransformationPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class TransformationComponent {

    private boolean isTransformed = false;
    private int transformationIndex = 0;

    public TransformationComponent() {
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
    }

    public void setTransformedAndSync(boolean transformed) {
        isTransformed = transformed;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(isTransformed, transformationIndex));
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

    public void setTransformationIndexAndSync(int transformationIndex) {
        this.transformationIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(isTransformed, transformationIndex));
    }

    public void setTransformationIndexAndSync(TransformationType type) {
        this.transformationIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(isTransformed, transformationIndex));
    }



    public static final IAttachmentSerializer<CompoundTag, TransformationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public TransformationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    TransformationComponent component = new TransformationComponent();
                    component.isTransformed = tag.getBoolean("isTransformed");
                    component.transformationIndex = tag.getInt("transformationIndex");
                    return component;
                }

                @Override
                public CompoundTag write(TransformationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isTransformed", component.isTransformed);
                    tag.putInt("transformationIndex", component.transformationIndex);
                    return tag;
                }
            };


    public enum TransformationType {
        DEVIL(0),
        DESIRE_AVATAR(1),
        BEAR(2);

        private final int index;
        TransformationType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
