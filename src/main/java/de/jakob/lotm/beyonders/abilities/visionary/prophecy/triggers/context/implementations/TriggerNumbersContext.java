package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TriggerNumbersContext extends TriggerContextBase {
    public double doubleValue;
    public int intValue;

    public boolean isDouble;
    public boolean isInt;

    public int operation; // -2 - lower, -1 - lower equal, 0 - equal, 1 - greater equal, 2 - greater

    public static String NBT_DOUBLE = "double";
    public static String NBT_INT = "int";
    public static String NBT_DOUBLE_BOOL = "double_bool";
    public static String NBT_INT_BOOL = "int_bool";
    public static String NBT_OPERATION = "operation";

    public TriggerNumbersContext(UUID entityId) {
        super(entityId);

        doubleValue = 0;
        isDouble = false;

        intValue = 0;
        isInt = false;

        operation = 0;
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.NUMBER;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        tag.putInt(NBT_INT, intValue);
        tag.putDouble(NBT_DOUBLE, doubleValue);

        tag.putBoolean(NBT_DOUBLE_BOOL, isDouble);
        tag.putBoolean(NBT_INT_BOOL, isInt);

        tag.putInt(NBT_OPERATION, operation);

        return tag;
    }

    @Override
    public TriggerNumbersContext fillFromStream(TokenStream stream) {
        stream.next();

        if(stream.isEmpty()) return this;

        try{
            intValue = Integer.parseInt(stream.peek());
            isInt = true;
        }catch (NumberFormatException e){
            try {
                doubleValue = Double.parseDouble(stream.peek());
                isDouble = true;
            }
            catch (NumberFormatException ignored){}
        }

        stream.next();
        try {
            operation = Integer.parseInt(stream.peek());
        }catch (NumberFormatException ignored){}

        operation = operation > 2 ? 2 : Math.max(operation, -2);

        return this;
    }

    public static TriggerNumbersContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider){
        var context = new TriggerNumbersContext(id);

        context.doubleValue = tag.getDouble(NBT_DOUBLE);
        context.isDouble = tag.getBoolean(NBT_DOUBLE_BOOL);

        context.intValue = tag.getInt(NBT_INT);
        context.isInt = tag.getBoolean(NBT_INT_BOOL);

        context.operation = tag.getInt(NBT_OPERATION);

        return context;
    }

    public boolean checkOperation(float value, float value2){
        return switch (operation) {
            case -2 -> value2 < value;
            case -1 -> value2 <= value;
            case 0 -> value2 == value;
            case 1 -> value2 >= value;
            case 2 -> value2 > value;
            default -> false;
        };
    }
}
