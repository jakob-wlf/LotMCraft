package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context;

import net.minecraft.nbt.CompoundTag;

public enum TriggerContextEnum {
    POSITION,
    ITEM,
    EMPTY,
    NUMBER,
    STRING,
    PLAYER
    ;

    public static TriggerContextEnum fromNBT(CompoundTag tag, String key) {
        String name = tag.getString(key);

        try {
            return TriggerContextEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }

    public void toNBT(CompoundTag tag, String key) {
        tag.putString(key, this.name());
    }
}
