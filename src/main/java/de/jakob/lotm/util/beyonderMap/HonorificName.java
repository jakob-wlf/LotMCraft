package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.w3c.dom.Attr;

public record HonorificName(String first, String second, String third, String trueName) {
    static public String NBT_FIRST = "honorific_name_first";
    static public String NBT_SECOND = "honorific_name_second";
    static public String NBT_THIRD = "honorific_name_third";
    static public String NBT_NAME = "honorific_name_true_name";

    static public HonorificName EMPTY = new HonorificName("EMPTY", "EMPTY", "EMPTY", "EMPTY");

    public static int MAX_LENGTH = 300;

    public boolean compareFirst(String str){
        return first.equals(str);
    }

    public boolean compareSecond(String str){
        return  second.equals(str);
    }

    public boolean compareThird(String str){
        return third.equals(str);
    }

    public boolean isEmpty(){
        return first.equals(EMPTY.first)
                && second.equals(EMPTY.second)
                && third.equals(EMPTY.third)
                && trueName.equals(EMPTY.trueName);
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_FIRST, first);
        tag.putString(NBT_SECOND, second);
        tag.putString(NBT_THIRD, third);
        tag.putString(NBT_NAME, trueName);

        return tag;
    }

    static public HonorificName fromNBT(CompoundTag tag){
        String first = tag.getString(NBT_FIRST);
        String second = tag.getString(NBT_SECOND);
        String third = tag.getString(NBT_THIRD);
        String name = tag.getString(NBT_NAME);

        return new HonorificName(first, second, third, name);
    }

    public static HonorificName fromNetwork(FriendlyByteBuf buf) {
        return new HonorificName(
                buf.readUtf(MAX_LENGTH),
                buf.readUtf(MAX_LENGTH),
                buf.readUtf(MAX_LENGTH),
                buf.readUtf(MAX_LENGTH)
        );
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(first, MAX_LENGTH);
        buf.writeUtf(second, MAX_LENGTH);
        buf.writeUtf(third, MAX_LENGTH);
        buf.writeUtf(trueName, MAX_LENGTH);
    }
}
