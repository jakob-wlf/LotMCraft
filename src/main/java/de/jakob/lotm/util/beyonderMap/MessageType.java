package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record MessageType(@Nullable String from, Long when, String title, String desc, boolean read) {
    public static String NBT_FROM = "message_from";
    public static String NBT_WHEN = "message_when";
    public static String NBT_TITLE = "message_title";
    public static String NBT_DESC = "message_desc";
    public static String NBT_READ = "message_read";

    public static int MAX_NAME_LENGTH = 30;
    public static int MAX_TITLE_LENGTH = 250;
    public static int MAX_MESSAGE_LENGTH = 5000;

    public static Long createTimestamp(){
        return java.time.Instant.now().toEpochMilli();
    }

    public static String readTimestamp(Long stamp){
        Instant instant = Instant.ofEpochMilli(stamp);
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - H:mm:ss");
        return time.format(formatter);
    }

    public MessageType setRead(boolean value){
        return new MessageType(from, when, title, desc, value);
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        if(from != null)
            tag.putString(NBT_FROM, from);

        tag.putLong(NBT_WHEN, when);

        tag.putString(NBT_TITLE, title);

        tag.putString(NBT_DESC, desc);

        tag.putBoolean(NBT_READ, read);

        return tag;
    }

    public static MessageType fromNBT(CompoundTag tag){
        String from = tag.getString(NBT_FROM);
        Long when = tag.getLong(NBT_WHEN);
        String title = tag.getString(NBT_TITLE);
        String desc = tag.getString(NBT_DESC);
        Boolean read = tag.getBoolean(NBT_READ);

        return new MessageType(from.isEmpty() ? null : from, when, title, desc, read);
    }

    public static MessageType fromNetwork(FriendlyByteBuf buf) {
        String from = null;
        if(buf.readBoolean())
            from = buf.readUtf(MAX_NAME_LENGTH);

        return new MessageType(
                from,
                buf.readLong(),
                buf.readUtf(MAX_TITLE_LENGTH),
                buf.readUtf(MAX_MESSAGE_LENGTH),
                buf.readBoolean()
        );
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(from != null);

        if(from != null)
        buf.writeUtf(from, MAX_NAME_LENGTH);

        buf.writeLong(when);
        buf.writeUtf(title, MAX_TITLE_LENGTH);
        buf.writeUtf(desc, MAX_MESSAGE_LENGTH);
        buf.writeBoolean(read);
    }
}
