package de.jakob.lotm.util.helper.subordinates;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SubordinateComponent {
    private boolean isSubordinate = false;
    private String controllerUUID = "";
    private boolean followMode = true;
    private boolean shouldAttack = true;

    public SubordinateComponent() {}

    public SubordinateComponent(boolean isMarionette, String controllerUUID) {
        this.isSubordinate = isMarionette;
        this.controllerUUID = controllerUUID;
    }
    
    // Getters and setters
    public boolean isSubordinate() { return isSubordinate; }
    public void setSubordinate(boolean subordinate) { this.isSubordinate = subordinate; }
    public String getControllerUUID() { return controllerUUID; }
    public void setControllerUUID(String controllerUUID) { this.controllerUUID = controllerUUID; }
    public boolean isFollowMode() { return followMode; }
    public void setFollowMode(boolean followMode) { this.followMode = followMode; }
    public boolean shouldAttack() { return shouldAttack; }
    public void setShouldAttack(boolean shouldAttack) { this.shouldAttack = shouldAttack; }
    
    public static final IAttachmentSerializer<CompoundTag, SubordinateComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SubordinateComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SubordinateComponent component = new SubordinateComponent();
                    component.isSubordinate = tag.getBoolean("isSubordinate");
                    component.controllerUUID = tag.getString("controllerUUID");
                    component.followMode = tag.getBoolean("followMode");
                    component.shouldAttack = tag.getBoolean("shouldAttack");
                    return component;
                }

                @Override
                public CompoundTag write(SubordinateComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isSubordinate", component.isSubordinate);
                    tag.putString("controllerUUID", component.controllerUUID);
                    tag.putBoolean("followMode", component.followMode);
                    tag.putBoolean("shouldAttack", component.shouldAttack);
                    return tag;
                }
            };
}
