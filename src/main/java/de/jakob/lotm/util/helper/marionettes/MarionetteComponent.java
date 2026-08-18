package de.jakob.lotm.util.helper.marionettes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MarionetteComponent {
    public List<UUID> marionettes = new LinkedList<>();

    private boolean isMarionette = false;
    private String controllerUUID = "";
    private boolean followMode = true;
    private boolean shouldAttack = true;

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
    public boolean shouldAttack() { return shouldAttack; }
    public void setShouldAttack(boolean shouldAttack) { this.shouldAttack = shouldAttack; }
    
    public static final IAttachmentSerializer<CompoundTag, MarionetteComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MarionetteComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MarionetteComponent component = new MarionetteComponent();
                    component.isMarionette = tag.getBoolean("isMarionette");
                    component.controllerUUID = tag.getString("controllerUUID");
                    component.followMode = tag.getBoolean("followMode");
                    component.shouldAttack = tag.getBoolean("shouldAttack");

                    component.marionettes.clear();
                    ListTag list = tag.getList("marionettes", Tag.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag uuidTag = list.getCompound(i);

                        if (uuidTag.hasUUID("UUID")) {
                            component.marionettes.add(uuidTag.getUUID("UUID"));
                        }
                    }
                    return component;
                }

                @Override
                public CompoundTag write(MarionetteComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isMarionette", component.isMarionette);
                    tag.putString("controllerUUID", component.controllerUUID);
                    tag.putBoolean("followMode", component.followMode);
                    tag.putBoolean("shouldAttack", component.shouldAttack);

                    ListTag list = new ListTag();

                    for (UUID uuid : component.marionettes) {
                        CompoundTag uuidTag = new CompoundTag();
                        uuidTag.putUUID("UUID", uuid);
                        list.add(uuidTag);
                    }

                    tag.put("marionettes", list);

                    return tag;
                }
            };
}
