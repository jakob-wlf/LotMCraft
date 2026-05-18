package de.jakob.lotm.util.playerMap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.units.qual.C;

public class Characteristic {
    private final String pathway;
    private int stack;
    private final int sequence;

    public static final String CHAR_PATH = "char_path";
    public static final String CHAR_STACK = "char_stack";
    public static final String CHAR_SEQ = "char_seq";


    public Characteristic(String pathway, int stack, int sequence){
        this.pathway = pathway;
        this.stack = stack;
        this.sequence = sequence;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag(3);
        tag.putString(CHAR_PATH, pathway);
        tag.putInt(CHAR_SEQ, sequence);
    tag.putInt(CHAR_STACK, stack);
        return tag;
    }

    public String pathway(){
        return pathway;
    }

    public void setStack(int stack){
        this.stack = stack;
    }

    public int stack(){
        return stack;
    }

    public int sequence(){
        return sequence;
    }

    public String toString(){
        return this.pathway + " " + this.sequence + ": " + this.stack;
    }

    public static Characteristic fromNBT(CompoundTag tag, HolderLookup.Provider provider){
        return new Characteristic(tag.getString(CHAR_PATH), tag.getInt(CHAR_STACK), tag.getInt(CHAR_SEQ));
    }



}
