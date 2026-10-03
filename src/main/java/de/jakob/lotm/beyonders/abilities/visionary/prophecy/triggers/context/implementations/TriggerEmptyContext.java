package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TriggerEmptyContext extends TriggerContextBase {
    public TriggerEmptyContext(UUID entity) {
        super(entity);
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.EMPTY;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider){
        return super.toNBT(provider);
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        return this;
    }

    public static TriggerEmptyContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        return new TriggerEmptyContext(id);
    }
}
