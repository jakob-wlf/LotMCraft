package de.jakob.lotm.util.playerMap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.units.qual.C;

public class Characteristic {
    private final String pathway;
    private int stack;
    private final int sequence;
    private int disabledStacks = 0;

    public static final String CHAR_PATH = "pathway";
    public static final String CHAR_STACK = "stack";
    public static final String CHAR_SEQ = "sequence";
    public static final String CHAR_DISABLED_STACKS = "disabled_stacks";
    public static final String OLD_CHAR_PATH = "char_path";
    public static final String OLD_CHAR_STACK = "char_stack";
    public static final String OLD_CHAR_SEQ = "char_seq";


    public Characteristic(String pathway, int stack, int sequence){
        this.pathway = pathway;
        this.stack = stack;
        this.sequence = sequence;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag(4);
        tag.putString(CHAR_PATH, pathway);
        tag.putInt(CHAR_SEQ, sequence);
        tag.putInt(CHAR_STACK, stack);
        tag.putInt(CHAR_DISABLED_STACKS, disabledStacks);
        return tag;
    }

    public boolean isEnabled() {
        return stack > disabledStacks;
    }

    public int getDisabledStacks() {
        return disabledStacks;
    }

    public void setDisabledStacks(int disabledStacks) {
        this.disabledStacks = Math.max(0, Math.min(stack, disabledStacks));
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.disabledStacks = 0;
        } else {
            this.disabledStacks = this.stack;
        }
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
        String path = tag.contains(CHAR_PATH) ? tag.getString(CHAR_PATH) : tag.getString(OLD_CHAR_PATH);
        int stack = tag.contains(CHAR_STACK) ? tag.getInt(CHAR_STACK) : tag.getInt(OLD_CHAR_STACK);
        int seq = tag.contains(CHAR_SEQ) ? tag.getInt(CHAR_SEQ) : tag.getInt(OLD_CHAR_SEQ);
        Characteristic characteristic = new Characteristic(path, stack, seq);
        if (tag.contains(CHAR_DISABLED_STACKS)) {
            characteristic.setDisabledStacks(tag.getInt(CHAR_DISABLED_STACKS));
        } else if (tag.contains("disabled")) {
            characteristic.setEnabled(!tag.getBoolean("disabled"));
        }
        return characteristic;
    }



}
