package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public record StoredData(String pathway, Integer sequence, @Nullable String honorificName) {

    public static final String NBT_PATHWAY = "beyonder_map_pathway";
    public static final String NBT_SEQUENCE = "beyonder_map_sequence";
    public static final String NBT_HONORIFIC_NAME = "beyonder_map_honorific_name";

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);

        if(honorificName != null)
            tag.putString(NBT_HONORIFIC_NAME, honorificName);

        return tag;
    }

    public static StoredData fromNBT(CompoundTag tag){
        String path = tag.getString(NBT_PATHWAY);
        Integer seq = tag.getInt(NBT_SEQUENCE);
        String name = tag.getString(NBT_HONORIFIC_NAME);

        return new StoredData(path, seq, name);
    }

}
