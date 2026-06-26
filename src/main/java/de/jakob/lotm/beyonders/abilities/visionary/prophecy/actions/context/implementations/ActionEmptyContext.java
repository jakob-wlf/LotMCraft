package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ActionEmptyContext extends ActionContextBase {
    public ActionEmptyContext(UUID entity) {
        super(entity);
    }

    @Override
    public ActionContextEnum getType() {
        return ActionContextEnum.EMPTY;
    }

    @Override
    public ActionContextBase fillFromStream(TokenStream stream) {
        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider){
        return super.toNBT(provider);
    }

    public static ActionEmptyContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        return new ActionEmptyContext(id);
    }
}
