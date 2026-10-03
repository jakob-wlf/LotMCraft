package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public abstract class ActionContextBase {
    public static final String TARGET_ID = "target_id";

    protected UUID targetId;

    public ActionContextBase(LivingEntity entity){
        targetId = entity.getUUID();
    }

    public ActionContextBase(UUID entityId){
        targetId = entityId;
    }

    public abstract ActionContextEnum getType();

    public abstract ActionContextBase fillFromStream(TokenStream stream);

    public CompoundTag toNBT(HolderLookup.Provider provider){
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TARGET_ID, targetId);
        return tag;
    }

    public static ActionContextBase load(ActionContextEnum type, CompoundTag tag, HolderLookup.Provider provider) {
        UUID id = tag.getUUID(TARGET_ID);

        return switch (type) {
            case POSITION -> ActionPositionContext.load(tag, id, provider);
            case ITEM -> ActionItemsContext.load(tag, id, provider);
            case NUMBER -> ActionNumberContext.load(tag, id, provider);
            case EMPTY -> ActionEmptyContext.load(tag, id, provider);
            case STRING -> ActionStringContext.load(tag, id, provider);
        };
    }

    public static ActionContextBase create(ActionContextEnum type, UUID id){
        return switch (type){
            case ITEM -> new ActionItemsContext(id);
            case POSITION -> new ActionPositionContext(id);
            case NUMBER -> new ActionNumberContext(id);
            case EMPTY -> new ActionEmptyContext(id);
            case STRING -> new ActionStringContext(id);
        };
    }
}
