package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;

public record StoredData(String pathway, Integer sequence, HonorificName honorificName,
                         String trueName, LinkedList<MessageType> msgs, LinkedList<HonorificName> knownNames,
                         Boolean modified, Vec3 lastPosition, Integer charStack) {

    public static final String NBT_PATHWAY = "beyonder_map_pathway";
    public static final String NBT_SEQUENCE = "beyonder_map_sequence";
    public static final String NBT_HONORIFIC_NAME = "beyonder_map_honorific_name";
    public static final String NBT_TRUE_NAME = "beyonder_map_true_name";
    public static final String NBT_MESSAGES = "beyonder_map_messages";
    public static final String NBT_KNOWN_NAMES = "beyonder_map_known_names";
    public static final String NBT_MODIFIED = "beyonder_map_modified";
    public static final String NBT_CHAR_STACK = "beyonder_map_char_stack";

    public static final String NBT_LAST_POSITION_X = "beyonder_map_last_position_x";
    public static final String NBT_LAST_POSITION_Y = "beyonder_map_last_position_y";
    public static final String NBT_LAST_POSITION_Z = "beyonder_map_last_position_z";

    public static StoredDataBuilder builder = new StoredDataBuilder();

    public String getShortInfo() {
        return "Path: " + pathway
                + " -- Seq: " + sequence
                + " -- TN: " + trueName;
    }

    public String getAllInfo(){
        return "Name: " + trueName
                + "\n--- Path: " + pathway
                + "\n--- Seq: " + sequence
                + "\n--- Honorific Name: " + honorificName.getAllInfo()
                + "\n--- Logout Position: " + (int) lastPosition.x + " " + (int) lastPosition.y + " " + (int) lastPosition.z
                + "\n--- Characteristics stack: " + charStack
                + "\n--- Was modified: " + modified
                ;
    }

    public void addMsg(MessageType msg){
        msgs.add(msg);
    }

    public void removeMsg(MessageType msg){
        msgs.removeIf(str -> str.equals(msg));
    }

    public StoredData regressSeq(){
        return builder
                .copyFrom(this)
                .pathway((sequence + 1 == LOTMCraft.NON_BEYONDER_SEQ) ? "none" : pathway)
                .sequence(sequence + 1)
                .honorificName((sequence + 1 >= 3) ? HonorificName.EMPTY : honorificName)
                .charStack(0)
                .build();
    }


    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);

        tag.put(NBT_HONORIFIC_NAME, honorificName.toNBT());

        tag.putString(NBT_TRUE_NAME, trueName);

        ListTag list = new ListTag();
        for (MessageType value : msgs) {
            list.add(value.toNBT());
        }

        tag.put(NBT_MESSAGES, list);

//        ListTag list2 = new ListTag();
//        for (MessageType value : knownNames) {
//            list2.add(value.toNBT());
//        }
//
//        tag.put(NBT_KNOWN_NAMES, list2);

        tag.putBoolean(NBT_MODIFIED, modified);

        tag.putDouble(NBT_LAST_POSITION_X, lastPosition.x());
        tag.putDouble(NBT_LAST_POSITION_Y, lastPosition.y());
        tag.putDouble(NBT_LAST_POSITION_Z, lastPosition.z());

        tag.putInt(NBT_CHAR_STACK, charStack);

        return tag;
    }

    public static StoredData fromNBT(CompoundTag tag){
        String path = tag.getString(NBT_PATHWAY);
        Integer seq = tag.getInt(NBT_SEQUENCE);
        HonorificName name = HonorificName.fromNBT(tag.getCompound(NBT_HONORIFIC_NAME));
        String trueName = tag.getString(NBT_TRUE_NAME);

        LinkedList<MessageType> list = new LinkedList<>();
        for (var t : tag.getList(NBT_MESSAGES, Tag.TAG_COMPOUND)) {
            if(t instanceof CompoundTag compTag)
                list.add(MessageType.fromNBT(compTag));
        }

        LinkedList<HonorificName> names = new LinkedList<>();
//        for (var t : tag.getList(NBT_KNOWN_NAMES, Tag.TAG_COMPOUND)) {
//            if(t instanceof CompoundTag compTag)
//                names.add(HonorificName.fromNBT(compTag));
//        }

        Boolean modified = tag.getBoolean(NBT_MODIFIED);

        Vec3 vec = new Vec3(tag.getDouble(NBT_LAST_POSITION_X),
                tag.getDouble(NBT_LAST_POSITION_Y),
                tag.getDouble(NBT_LAST_POSITION_Z));

        Integer stack = tag.getInt(NBT_CHAR_STACK);

        return new StoredData(path, seq, name, trueName, list, names, modified, vec, stack);
    }

}
