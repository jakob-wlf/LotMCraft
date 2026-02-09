package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.LinkedList;
import java.util.List;

public record HonorificName(LinkedList<String> lines) {
    static public String NBT_LINES = "honorific_name_lines";

    static public final HonorificName EMPTY = new HonorificName(new LinkedList<>());

    public static int MAX_LENGTH = 300;

    public boolean contains(String str) {return lines.contains(str);}

    public boolean isEmpty(){
        return lines.isEmpty();
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        ListTag list = new ListTag();
        for (var value : lines) {
            list.add(StringTag.valueOf(value));
        }

        tag.put(NBT_LINES, list);

        return tag;
    }

    static public HonorificName fromNBT(CompoundTag tag){
        LinkedList<String> list = new LinkedList<>();

        ListTag listTag = tag.getList(NBT_LINES, Tag.TAG_STRING);
        for (var obj : listTag) {
            list.add(obj.getAsString());
        }

        return new HonorificName(list);
    }

    public static HonorificName fromNetwork(FriendlyByteBuf buf) {
//        return new HonorificName(
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH),
//                buf.readUtf(MAX_LENGTH)
//        );

        return HonorificName.EMPTY;
    }

    public void toNetwork(FriendlyByteBuf buf) {
//        buf.writeUtf(first, MAX_LENGTH);
//        buf.writeUtf(second, MAX_LENGTH);
//        buf.writeUtf(third, MAX_LENGTH);
//        buf.writeUtf(trueName, MAX_LENGTH);
    }
}
