package de.jakob.lotm.util.helper.marionettes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class MarionetteComponent {
    private boolean isMarionette = false;
    private String controllerUUID = "";
    private boolean followMode = true;
    
    public MarionetteComponent() {}
    
    public MarionetteComponent(boolean isMarionette, String controllerUUID) {
        this.isMarionette = isMarionette;
        this.controllerUUID = controllerUUID;
    }
    
    // Getters and setters
    public boolean isMarionette() { return isMarionette; }
    public void setMarionette(boolean marionette) { this.isMarionette = marionette; }
    public String getControllerUUID() { return controllerUUID; }
    public void setControllerUUID(String controllerUUID) { this.controllerUUID = controllerUUID; }
    public boolean isFollowMode() { return followMode; }
    public void setFollowMode(boolean followMode) { this.followMode = followMode; }
    
    public static final IAttachmentSerializer<CompoundTag, MarionetteComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MarionetteComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MarionetteComponent component = new MarionetteComponent();
                    component.isMarionette = tag.getBoolean("isMarionette");
                    component.controllerUUID = tag.getString("controllerUUID");
                    component.followMode = tag.getBoolean("followMode");
                    return component;
                }

                @Override
                public CompoundTag write(MarionetteComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isMarionette", component.isMarionette);
                    tag.putString("controllerUUID", component.controllerUUID);
                    tag.putBoolean("followMode", component.followMode);
                    return tag;
                }
            };
}
