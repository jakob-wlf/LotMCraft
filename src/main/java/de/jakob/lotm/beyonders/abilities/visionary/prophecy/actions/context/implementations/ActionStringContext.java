package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ActionStringContext extends ActionContextBase {
    public String string;

    public static String NBT_STRING = "string";

    public ActionStringContext(UUID entityId) {
        super(entityId);

        string = "";
    }

    @Override
    public ActionContextEnum getType() {
        return ActionContextEnum.STRING;
    }

    @Override
    public ActionContextBase fillFromStream(TokenStream stream) {
        stream.next();

        while (!stream.isEmpty()){
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

    public static ActionStringContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        ActionStringContext context = new ActionStringContext(id);
        context.string = tag.getString(NBT_STRING);

        return context;
    }
}
