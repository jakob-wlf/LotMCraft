package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TriggerPlayerContext extends TriggerContextBase {
    public List<String> names;
    public int range;

    public static String NBT_NAMES = "names";
    public static String NBT_RANGE = "range";

    public TriggerPlayerContext(UUID entityId) {
        super(entityId);

        names = new LinkedList<>();
        range = 0;
    }

    @Override
    public TriggerContextEnum getType() {
        return TriggerContextEnum.PLAYER;
    }

    @Override
    public TriggerContextBase fillFromStream(TokenStream stream) {
        stream.next();

        while(!stream.match("then")){
            try {
                range = Integer.parseInt(stream.peek());
            }catch (NumberFormatException e){
                names.add(stream.peek());
            }

            stream.next();
        }

        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        ListTag listTag = new ListTag();

        for (var str : names) {
            listTag.add(StringTag.valueOf(str));
        }
        tag.put(NBT_NAMES, listTag);

        tag.putInt(NBT_RANGE, range);

        return tag;
    }

    public static TriggerPlayerContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        var context = new TriggerPlayerContext(id);

        List<String> list = new LinkedList<>();

        ListTag listTag = tag.getList(NBT_NAMES, Tag.TAG_STRING);
        for (int i = 0; i < listTag.size(); i++) {
            list.add(listTag.getString(i));
        }

        int range = tag.getInt(NBT_RANGE);

        context.names = list;
        context.range = range;

        return context;
    }
}
