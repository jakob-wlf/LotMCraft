package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TriggerChainContext extends TriggerContextBase {
    public String trigger = "";

    public static String NBT_TRIGGER_STR = "trigger_str";

    public TriggerChainContext(UUID entityId) {
        super(entityId);
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.CHAIN;
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        stream.next();

        while (!stream.isEmpty() && !stream.match("then")){
            trigger += stream.peek() + " ";
            stream.next();
        }

        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        tag.putString(NBT_TRIGGER_STR, trigger);

        return tag;
    }

    public static TriggerChainContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        TriggerChainContext context = new TriggerChainContext(id);
        context.trigger = tag.getString(NBT_TRIGGER_STR);

        return context;
    }
}
