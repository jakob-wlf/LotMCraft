package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class MirrorWorldTraversalComponent {

    private boolean inMirrorWorld = false;
    private int previousGameMode = 0;
    private long cooldownMillis = 0;

    public MirrorWorldTraversalComponent() {
    }

    public boolean isInMirrorWorld() {
        return inMirrorWorld;
    }

    public void setInMirrorWorld(boolean inMirrorWorld) {
        this.inMirrorWorld = inMirrorWorld;
    }

    public int getPreviousGameModeIndex() {
        return previousGameMode;
    }

    public void setPreviousGameModeIndex(int previousGameMode) {
        this.previousGameMode = previousGameMode;
    }

    public GameType getPreviousGameMode() {
        return GameType.byId(previousGameMode);
    }

    public void setPreviousGameMode(GameType gameType) {
        this.previousGameMode = gameType.getId();
    }

    public void setOnCooldown() {
        this.cooldownMillis = System.currentTimeMillis() + 2500;
    }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownMillis;
    }

    public static final IAttachmentSerializer<CompoundTag, MirrorWorldTraversalComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MirrorWorldTraversalComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    MirrorWorldTraversalComponent component = new MirrorWorldTraversalComponent();
                    component.inMirrorWorld = tag.getBoolean("isInMirrorWorld");
                    component.previousGameMode = tag.getInt("previousGameMode");
                    component.cooldownMillis = tag.getLong("cooldownMillis");
                    return component;
                }

                @Override
                public CompoundTag write(MirrorWorldTraversalComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean("isInMirrorWorld", component.inMirrorWorld);
                    tag.putInt("previousGameMode", component.previousGameMode);
                    tag.putLong("cooldownMillis", component.cooldownMillis);
                    return tag;
                }
            };
}
