package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class SealedDimensionData extends SavedData {

    private static String name = "sealed_dimension_data";
    private String dimensionLocation;
    private int ticksRemaining = 0;

    public static final Factory<SealedDimensionData> FACTORY =
        new Factory<>(
            SealedDimensionData::new,
            SealedDimensionData::load,
            null
        );

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("ticksRemaining", ticksRemaining);
        tag.putString("dimension", dimensionLocation);
        return tag;
    }

    public static SealedDimensionData load(CompoundTag tag, HolderLookup.Provider registries) {
        SealedDimensionData data = new SealedDimensionData();
        data.ticksRemaining = tag.getInt("ticksRemaining");
        data.dimensionLocation = tag.getString("dimension");
        return data;
    }

    public static SealedDimensionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, name);
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public void activate(int durationTicks, String dimensionLocation) {
        this.dimensionLocation = dimensionLocation;
        ticksRemaining = durationTicks;
        setDirty();
    }

    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            setDirty();
        }
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public String getDimensionLocation() {
        return dimensionLocation;
    }

    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
        setDirty();
    }
}