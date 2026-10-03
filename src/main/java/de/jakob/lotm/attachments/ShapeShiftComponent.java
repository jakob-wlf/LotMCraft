package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class ShapeShiftComponent {

    private String shape = "";
    private boolean skinOnly = false;

    public ShapeShiftComponent() {}

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public boolean isSkinOnly() {
        return skinOnly;
    }

    public void setSkinOnly(boolean skinOnly) {
        this.skinOnly = skinOnly;
    }

    public static final IAttachmentSerializer<CompoundTag, ShapeShiftComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public ShapeShiftComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    ShapeShiftComponent component = new ShapeShiftComponent();
                    component.shape = tag.getString("Shape");
                    component.skinOnly = tag.getBoolean("skinOnly");
                    return component;
                }

                @Override
                public CompoundTag write(ShapeShiftComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Shape", component.shape);
                    tag.putBoolean("skinOnly", component.skinOnly);
                    return tag;
                }
            };
}
