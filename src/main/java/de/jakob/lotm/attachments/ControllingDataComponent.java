package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncControllingDataS2CPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.UUID;

public class ControllingDataComponent {
    private boolean isControlling = false;
    private boolean isControlled = false;
    private UUID ownerUUID = null;
    private String ownerName = null;
    private UUID bodyUUID = null;
    private UUID targetUUID = null;
    private CompoundTag targetEntity = null;
    private CompoundTag bodyEntity = null;

    public ControllingDataComponent() {}

    public boolean isControlling() {
        return isControlling;
    }

    public void setControlling(boolean isControlling) {
        setControlling(isControlling, null);
    }

    public void setControlling(boolean isControlling, ServerPlayer player) {
        this.isControlling = isControlling;
        syncData(player);

    }

    public boolean isControlled() {
        return isControlled;
    }

    public void setIsControlled(boolean isControlled) {
        this.isControlled = isControlled;
    }


    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }


    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }


    public UUID getBodyUUID() {
        return bodyUUID;
    }

    public void setBodyUUID(UUID bodyUUID) {
        this.bodyUUID = bodyUUID;
    }


    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }


    public CompoundTag getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(CompoundTag targetEntity) {
        this.targetEntity = targetEntity;
    }


    public CompoundTag getBodyEntity() {
        return bodyEntity;
    }

    public void setBodyEntity(CompoundTag bodyEntity) {
        setBodyEntity(bodyEntity, null);
    }

    public void setBodyEntity(CompoundTag bodyEntity, ServerPlayer player) {
        this.bodyEntity = bodyEntity;
        syncData(player);
    }


    public void syncData(ServerPlayer player) {
        if (player != null) {
            PacketHandler.sendToPlayer(player, new SyncControllingDataS2CPacket(this.isControlling, this.bodyEntity, player.getId()));
        }
    }


    public static final IAttachmentSerializer<CompoundTag, ControllingDataComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public ControllingDataComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    ControllingDataComponent component = new ControllingDataComponent();
                    if (tag.getBoolean("isControlling")) component.isControlling = tag.getBoolean("isControlling");
                    if (tag.getBoolean("isControlled")) component.isControlled = tag.getBoolean("isControlled");
                    if (tag.hasUUID("ownerUUID")) component.ownerUUID = tag.getUUID("ownerUUID");
                    if (tag.contains("ownerName")) component.ownerName = tag.getString("ownerName");
                    if (tag.hasUUID("bodyUUID")) component.bodyUUID = tag.getUUID("bodyUUID");
                    if (tag.hasUUID("targetUUID")) component.targetUUID = tag.getUUID("targetUUID");
                    if (tag.contains("targetEntity")) component.targetEntity = tag.getCompound("targetEntity");
                    if (tag.contains("bodyEntity")) component.bodyEntity = tag.getCompound("bodyEntity");
                    return component;
                }

                @Override
                public CompoundTag write(ControllingDataComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isControlling", component.isControlling);
                    tag.putBoolean("isControlled", component.isControlled);
                    if (component.getOwnerUUID() != null) tag.putUUID("ownerUUID", component.getOwnerUUID()); else tag.remove("ownerUUID");
                    if (component.getOwnerName() != null) tag.putString("ownerName", component.getOwnerName()); else tag.remove("ownerName");
                    if (component.getBodyUUID() != null) tag.putUUID("bodyUUID", component.getBodyUUID()); else tag.remove("bodyUUID");
                    if (component.getTargetUUID() != null) tag.putUUID("targetUUID", component.getTargetUUID()); else tag.remove("targetUUID");
                    if (component.getTargetEntity() != null) tag.put("targetEntity", component.getTargetEntity()); else tag.remove("targetEntity");
                    if (component.getBodyEntity() != null) tag.put("bodyEntity", component.getBodyEntity()); else tag.remove("bodyEntity");
                    return tag;
                }
            };
}
