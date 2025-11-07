package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncTransformationPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.swing.text.html.parser.Entity;

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

    public void setTransformedAndSync(boolean transformed, LivingEntity entity) {
        isTransformed = transformed;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex));
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

    public boolean shouldCancelDefaultRendering() {
        return switch (transformationIndex) {
            case 4 -> false;
            default -> true;
        };
    }

    public void setTransformationIndexAndSync(int transformationIndex, LivingEntity entity) {
        this.transformationIndex = transformationIndex;
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex));
    }

    public void setTransformationIndexAndSync(TransformationType type, LivingEntity entity) {
        this.transformationIndex = type.getIndex();
        PacketHandler.sendToAllPlayers(new SyncTransformationPacket(entity.getId(), isTransformed, transformationIndex));
    }



    public static final IAttachmentSerializer<CompoundTag, TransformationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public TransformationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    TransformationComponent component = new TransformationComponent();
                    component.isTransformed = tag.getBoolean("active");
                    component.transformationIndex = tag.getInt("index");
                    return component;
                }

                @Override
                public CompoundTag write(TransformationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("active", component.isTransformed);
                    tag.putInt("index", component.transformationIndex);
                    return tag;
                }
            };


    public enum TransformationType {
        DEVIL(0),
        DESIRE_AVATAR(1),
        BEAR(2),
        SOLAR_ENVOY(3),
        WINGS_OF_LIGHT(4),
        FOG_OF_HISTORY(5);

        private final int index;
        TransformationType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
