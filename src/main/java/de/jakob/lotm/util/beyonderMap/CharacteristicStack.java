package de.jakob.lotm.util.beyonderMap;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public record CharacteristicStack(ArrayList<Integer> stacks, Boolean isUsed) {
    public static final String NBT_CHAR_STACK = "characteristics_stack";
    public static final String NBT_CHAR_STACK_USED = "characteristics_stack_used";

    public static final String SEQ_BOOST = "characteristics_stack_boost_";

    public CharacteristicStack() {
        this(new ArrayList<>(List.of(0,0,0,0,0,0,0,0,0,0)), false);
    }

    public static String boostId(int seq){
        return SEQ_BOOST + seq;
    }

    public String getInfo(){
        return stacks.isEmpty() ? "None" :
                "\n   Seq 9: " + stacks.get(9)
                +  "\n   Seq 8: " + stacks.get(8)
                +  "\n   Seq 7: " + stacks.get(7)
                +  "\n   Seq 6: " + stacks.get(6)
                +  "\n   Seq 5: " + stacks.get(5)
                +  "\n   Seq 4: " + stacks.get(4)
                +  "\n   Seq 3: " + stacks.get(3)
                +  "\n   Seq 2: " + stacks.get(2)
                +  "\n   Seq 1: " + stacks.get(1)
                ;
    }

    private static boolean zeroCheck(ArrayList<Integer> stacks){
        for (var val : stacks){
            if(val != 0)
                return true;
        }

        return false;
    }

    public CharacteristicStack set(int index, int value){
        var buff = new ArrayList<>(this.stacks);

        buff.set(index, value);

        return new CharacteristicStack(buff, zeroCheck(buff));
    }

    public CharacteristicStack clear(){
        return new CharacteristicStack();
    }

    public CharacteristicStack clear(int index){
        var buff = new ArrayList<>(this.stacks);

        buff.set(index, 0);
        return new CharacteristicStack(buff, zeroCheck(buff));
    }

    public int get(int index){
        return this.stacks.get(index);
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();

        tag.put(NBT_CHAR_STACK, new IntArrayTag(stacks.stream().mapToInt(Integer::intValue).toArray()));

        tag.putBoolean(NBT_CHAR_STACK_USED, isUsed);

        return tag;
    }

    static public CharacteristicStack fromNBT(CompoundTag tag){
        return new CharacteristicStack(new ArrayList<>(Arrays.stream(tag.getIntArray(NBT_CHAR_STACK)).boxed().toList()),
                tag.getBoolean(NBT_CHAR_STACK_USED));
    }
}
