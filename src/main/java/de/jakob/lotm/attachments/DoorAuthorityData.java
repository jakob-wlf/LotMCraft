package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DoorAuthorityData extends SavedData {

    private static String name = "door_authority_data";
    private String effectId = "";
    private int ticksRemaining = 0;

    public static final SavedData.Factory<DoorAuthorityData> FACTORY =
        new SavedData.Factory<>(
            DoorAuthorityData::new,
            DoorAuthorityData::load,
            null
        );

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("ticksRemaining", ticksRemaining);
        tag.putString("id", effectId);
        return tag;
    }

    public static DoorAuthorityData load(CompoundTag tag, HolderLookup.Provider registries) {
        DoorAuthorityData data = new DoorAuthorityData();
        data.ticksRemaining = tag.getInt("ticksRemaining");
        data.effectId = tag.getString("id");
        return data;
    }

    public static DoorAuthorityData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, name);
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public void activate(int durationTicks, String effectId) {
        ticksRemaining = durationTicks;
        this.effectId = effectId;
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

    public String getEffectId() {
        return effectId;
    }
}