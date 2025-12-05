package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class BeyonderMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";

    private HashMap<UUID, StoredData> map = new HashMap<>(300);

    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        if(!BeyonderData.isBeyonder(entity)) return;

        map.put(entity.getUUID(), new StoredData(BeyonderData.getPathway(entity),
                BeyonderData.getSequence(entity)));
    }

    public Optional<StoredData> get(LivingEntity entity){
        if(!(entity instanceof ServerPlayer)) return Optional.empty();

        if(!BeyonderData.isBeyonder(entity)) return Optional.empty();

        return Optional.of(map.get(entity.getUUID()));
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for(var obj : map.entrySet()){
            tag.put(obj.getKey().toString(), obj.getValue().toNBT());
        }

        compoundTag.put(NBT_BEYONDER_MAP, tag);

        return compoundTag;
    }

    public static BeyonderMap load(CompoundTag compoundTag) {
        BeyonderMap data = new BeyonderMap();

        CompoundTag tag = compoundTag.getCompound(NBT_BEYONDER_MAP);

        for(var obj : tag.getAllKeys()){
            data.map.put(UUID.fromString(obj), StoredData.fromNBT(tag.getCompound(obj)));
        }

        return data;
    }
}
