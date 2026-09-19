package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Context for "if <target> become <pathway> <sequence> then ..." predictions. */
public class TriggerPathSeqContext extends TriggerContextBase {
    public String pathway;
    public int sequence;

    public static final String NBT_PATHWAY = "pathway";
    public static final String NBT_SEQUENCE = "sequence";

    public TriggerPathSeqContext(UUID entityId) {
        super(entityId);

        pathway = "";
        sequence = 0;
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.PATH_SEQ;
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        stream.next();

        pathway = stream.peek();
        stream.next();

        try {
            sequence = Integer.parseInt(stream.peek());
        } catch (Exception ignored) {
        }
        stream.next();

        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        tag.putString(NBT_PATHWAY, pathway == null ? "" : pathway);
        tag.putInt(NBT_SEQUENCE, sequence);

        return tag;
    }

    public static TriggerPathSeqContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        TriggerPathSeqContext context = new TriggerPathSeqContext(id);
        context.pathway = tag.getString(NBT_PATHWAY);
        context.sequence = tag.getInt(NBT_SEQUENCE);
        return context;
    }
}
