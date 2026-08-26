package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class MarionetteComponent {
    private boolean isMarionette = false;
    private String controllerUUID = "";
    private MarionetteMode currentMode = MarionetteMode.FOLLOW;
    private boolean shouldAttack = true;
    private boolean hasWorm = false;

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
    public MarionetteMode getCurrentMode() { return currentMode; }
    public void setCurrentMode(MarionetteMode currentMode) { this.currentMode = currentMode; }
    public boolean shouldAttack() { return shouldAttack; }
    public void setShouldAttack(boolean shouldAttack) { this.shouldAttack = shouldAttack; }
    public boolean hasWorm() { return hasWorm; }
    public void setHasWorm(boolean hasWorm) { this.hasWorm = hasWorm; }
    
    public static final IAttachmentSerializer<CompoundTag, MarionetteComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MarionetteComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MarionetteComponent component = new MarionetteComponent();
                    component.isMarionette = tag.getBoolean("isMarionette");
                    component.controllerUUID = tag.getString("controllerUUID");
                    MarionetteMode mode;
                    try {
                        mode = MarionetteMode.valueOf(tag.getString("currentMode"));
                    } catch (IllegalArgumentException e) {
                        mode = MarionetteMode.FOLLOW; // safe fallback for old/renamed/corrupt data
                    }
                    component.currentMode = mode;
                    component.shouldAttack = tag.getBoolean("shouldAttack");
                    component.hasWorm = tag.getBoolean("hasWorm");
                    return component;
                }

                @Override
                public CompoundTag write(MarionetteComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isMarionette", component.isMarionette);
                    tag.putString("controllerUUID", component.controllerUUID);
                    tag.putString("currentMode", component.currentMode.name());
                    tag.putBoolean("shouldAttack", component.shouldAttack);
                    tag.putBoolean("hasWorm", component.hasWorm);
                    return tag;
                }
            };

    public enum MarionetteMode {
        FOLLOW(true),
        STAY(false),
        WANDER(true);

        MarionetteMode(boolean shouldAttack) {
            this.shouldAttack = shouldAttack;
        }

        public final boolean shouldAttack;
    }
}
