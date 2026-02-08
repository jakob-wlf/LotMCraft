package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.LinkedList;

public record StoredData(String pathway, Integer sequence, HonorificName honorificName,
                         String trueName, LinkedList<MessageType> msgs, LinkedList<HonorificName> knownNames) {

    public static final String NBT_PATHWAY = "beyonder_map_pathway";
    public static final String NBT_SEQUENCE = "beyonder_map_sequence";
    public static final String NBT_HONORIFIC_NAME = "beyonder_map_honorific_name";
    public static final String NBT_TRUE_NAME = "beyonder_map_true_name";
    public static final String NBT_MESSAGES = "beyonder_map_messages";
    public static final String NBT_KNOWN_NAMES = "beyonder_map_known_names";

    public String getShortInfo() {
        return "Path: " + pathway
                + " -- Seq: " + sequence
                + " -- TN: " + trueName;
    }

    public String getAllInfo(){
        return getShortInfo();
    }

    public void addMsg(MessageType msg){
        msgs.add(msg);
    }

    public void removeMsg(MessageType msg){
        msgs.removeIf(str -> str.equals(msg));
    }

    public StoredData regressSeq(){
        return new StoredData(sequence + 1 == LOTMCraft.NON_BEYONDER_SEQ ?
                "none" : pathway, sequence + 1,
                honorificName, trueName, msgs, knownNames);
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

        ListTag list2 = new ListTag();
        for (MessageType value : msgs) {
            list2.add(value.toNBT());
        }

        tag.put(NBT_KNOWN_NAMES, list2);

        return tag;
    }

    public static StoredData fromNBT(CompoundTag tag){
        String path = tag.getString(NBT_PATHWAY);
        Integer seq = tag.getInt(NBT_SEQUENCE);
        HonorificName name = HonorificName.fromNBT(tag.getCompound(NBT_HONORIFIC_NAME));
        String trueName = tag.getString(NBT_TRUE_NAME);

        LinkedList<MessageType> list = new LinkedList<>();
        for (var t : tag.getList(NBT_MESSAGES, StringTag.TAG_COMPOUND)) {
            if(t instanceof CompoundTag compTag)
                list.add(MessageType.fromNBT(compTag));
        }

        LinkedList<HonorificName> names = new LinkedList<>();
        for (var t : tag.getList(NBT_KNOWN_NAMES, StringTag.TAG_COMPOUND)) {
            if(t instanceof CompoundTag compTag)
                names.add(HonorificName.fromNBT(compTag));
        }

        return new StoredData(path, seq, name, trueName, list, names);
    }

}
