package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;

public record StoredData(String pathway, Integer sequence) {

    public static final String NBT_PATHWAY = "beyonder_map_pathway";
    public static final String NBT_SEQUENCE = "beyonder_map_sequence";

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);

        return tag;
    }

    public static StoredData fromNBT(CompoundTag tag){
        String path = tag.getString(NBT_PATHWAY);
        Integer seq = tag.getInt(NBT_SEQUENCE);

        return new StoredData(path, seq);
    }

}
