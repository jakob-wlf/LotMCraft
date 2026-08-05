package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class LuckAccumulationComponent {
    public static final int MINIMUM_LUCK = -10000;

    private int storedLuck;
    private float regenerationProgress;
    private float regenerationRate;
    private long nextRegenerationChangeTick;

    public LuckAccumulationComponent() {}

    public int getStoredLuck() {
        return storedLuck;
    }

    public void setStoredLuck(int storedLuck, int maximum) {
        this.storedLuck = Math.clamp(storedLuck, MINIMUM_LUCK, maximum);
    }

    public void addStoredLuck(int amount, int maximum) {
        setStoredLuck(storedLuck + amount, maximum);
    }

    public boolean consumeStoredLuck(int amount) {
        if (amount < 0 || storedLuck < amount) {
            return false;
        }
        storedLuck -= amount;
        return true;
    }

    public float getRegenerationProgress() {
        return regenerationProgress;
    }

    public void setRegenerationProgress(float regenerationProgress) {
        this.regenerationProgress = Math.max(0, regenerationProgress);
    }

    public float getRegenerationRate() {
        return regenerationRate;
    }

    public void setRegenerationRate(float regenerationRate) {
        this.regenerationRate = Math.max(0, regenerationRate);
    }

    public long getNextRegenerationChangeTick() {
        return nextRegenerationChangeTick;
    }

    public void setNextRegenerationChangeTick(long nextRegenerationChangeTick) {
        this.nextRegenerationChangeTick = nextRegenerationChangeTick;
    }

    public long getTicksAccumulated() { return storedLuck; }

    public void setTicksAccumulated(long ticksAccumulated) {
        this.storedLuck = (int) Math.clamp(ticksAccumulated, 0, Integer.MAX_VALUE);
    }

    public static final IAttachmentSerializer<CompoundTag, LuckAccumulationComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public LuckAccumulationComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    LuckAccumulationComponent component = new LuckAccumulationComponent();
                    if (tag.contains("storedLuck")) {
                        component.storedLuck = tag.getInt("storedLuck");
                    } else {
                        component.storedLuck = (int) Math.clamp(tag.getLong("ticksAccumulated") / 1200, 0, Integer.MAX_VALUE);
                    }
                    component.regenerationProgress = tag.getFloat("regenerationProgress");
                    component.regenerationRate = tag.getFloat("regenerationRate");
                    component.nextRegenerationChangeTick = tag.getLong("nextRegenerationChangeTick");
                    return component;
                }

                @Override
                public CompoundTag write(LuckAccumulationComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("storedLuck", component.storedLuck);
                    tag.putFloat("regenerationProgress", component.regenerationProgress);
                    tag.putFloat("regenerationRate", component.regenerationRate);
                    tag.putLong("nextRegenerationChangeTick", component.nextRegenerationChangeTick);
                    return tag;
                }
            };
}