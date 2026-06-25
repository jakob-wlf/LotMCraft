package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers;

import net.minecraft.nbt.CompoundTag;

public enum TriggerEnum {
    POSITION,
    PICK_UP,
    INSTANT,
    HEALTH,
    SANITY,
    PLAYER,
    SEALED,
    HUNGER,
    RIDING,
    SPIRITUALITY,
    SEQUENCE,
    PATHWAY,
    LIGHT,
    ASLEEP
    ;

    public static TriggerEnum  fromNBT(CompoundTag tag, String key) {
        String name = tag.getString(key);
        try {
            return TriggerEnum .valueOf(name);
        } catch (Exception e) {
            return POSITION; // fallback
        }
    }

    public void toNBT(CompoundTag tag, String key) {
        tag.putString(key, this.name());
    }
}
