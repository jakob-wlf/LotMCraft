package de.jakob.lotm.abilities.visionary.prophecy.actions;

import de.jakob.lotm.abilities.visionary.prophecy.actions.implementations.DropItemAction;
import net.minecraft.nbt.CompoundTag;

public enum ActionsEnum {
    DROP_ITEM,
    TELEPORT,
    DIGESTION,
    SANITY,
    HEALTH,
    CALAMITY,
    STUN,
    SKILL,
    CONFUSION,
    SEAL,
    UNSEAL,
    SPAWN,
    SAY,
    WEATHER,
    TIME,
    WHISPERS
    //DOUBLE
    ;

    public static ActionsEnum fromNBT(CompoundTag tag, String key) {
        String name = tag.getString(key);
        try {
            return ActionsEnum.valueOf(name);
        } catch (Exception e) {
            return DROP_ITEM; // fallback
        }
    }

    public void toNBT(CompoundTag tag, String key) {
        tag.putString(key, this.name());
    }
}
