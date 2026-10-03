package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TriggerStringContext extends TriggerContextBase {
    public String string;

    public static String NBT_STRING = "string";

    public TriggerStringContext(UUID entityId) {
        super(entityId);

        string = "";
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.STRING;
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        stream.next();

        while (!stream.match("then")){
            string += stream.peek() + " ";
            stream.next();
        }

        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        tag.putString(NBT_STRING, string);

        return tag;
    }

    public static TriggerStringContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        TriggerStringContext  context = new TriggerStringContext (id);
        context.string = tag.getString(NBT_STRING);

        return context;
    }
}
