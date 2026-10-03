package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public abstract class TriggerContextBase {
    public static final String TARGET_ID = "target_id";

    protected UUID targetId;

    public TriggerContextBase(LivingEntity entity){
        targetId = entity.getUUID();
    }

    public TriggerContextBase(UUID entityId){
        targetId = entityId;
    }

    public abstract TriggerContextEnum getType();

    public abstract TriggerContextBase fillFromStream(TokenStream stream);

    public UUID getTargetId(){
        return targetId;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider){
        CompoundTag tag = new CompoundTag();
        tag.putUUID(TARGET_ID, targetId);
        return tag;
    }

    public static TriggerContextBase load(TriggerContextEnum type, CompoundTag tag, HolderLookup.Provider provider) {
        UUID id = tag.getUUID(TARGET_ID);

        return switch (type) {
            case POSITION -> TriggerPositionContext.load(tag, id, provider);
            case ITEM -> TriggerItemsContext.load(tag, id, provider);
            case EMPTY -> TriggerEmptyContext.load(tag, id, provider);
            case NUMBER -> TriggerNumbersContext.load(tag, id, provider);
            case STRING -> TriggerStringContext.load(tag, id, provider);
            case PLAYER -> TriggerPlayerContext.load(tag, id, provider);
        };
    }

    public static TriggerContextBase create(TriggerContextEnum type, UUID id){
        return switch (type){
            case POSITION -> new TriggerPositionContext(id);
            case ITEM -> new TriggerItemsContext(id);
            case EMPTY -> new TriggerEmptyContext(id);
            case NUMBER -> new TriggerNumbersContext(id);
            case STRING -> new TriggerStringContext(id);
            case PLAYER -> new TriggerPlayerContext(id);
        };
    }
}
