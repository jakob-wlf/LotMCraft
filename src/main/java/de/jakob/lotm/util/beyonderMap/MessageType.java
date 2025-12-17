package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public record MessageType(@Nullable String from, Long when,@Nullable String title, String desc) {
    public static String NBT_FROM = "message_from";
    public static String NBT_WHEN = "message_when";
    public static String NBT_TITLE = "message_title";
    public static String NBT_DESC = "message_desc";

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        if(from != null)
            tag.putString(NBT_FROM, from);

        tag.putLong(NBT_WHEN, when);

        if(title != null)
            tag.putString(NBT_TITLE, title);

        tag.putString(NBT_DESC, desc);

        return tag;
    }

    public static MessageType fromNBT(CompoundTag tag){
        String from = tag.getString(NBT_FROM);
        Long when = tag.getLong(NBT_WHEN);
        String title = tag.getString(NBT_TITLE);
        String desc = tag.getString(NBT_DESC);

        return new MessageType(from.isEmpty() ? null : from, when, title.isEmpty() ? null : title, desc);
    }
}
