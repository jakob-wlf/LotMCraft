package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class BeyonderMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";

    public HashMap<UUID, StoredData> map;

    public BeyonderMap() {
        super();

        map = new HashMap<>(300);
    }


    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        LOTMCraft.LOGGER.info("in map: pathway: {}, seq: {}", BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

        map.put(entity.getUUID(), new StoredData(BeyonderData.getPathway(entity),
                BeyonderData.getSequence(entity)));
    }

    public Optional<StoredData> get(LivingEntity entity){
        if(!(entity instanceof ServerPlayer)) return Optional.empty();

        return Optional.of(map.get(entity.getUUID()));
    }

    public int count(String path, int seq){
        int res = 0;

        for(var obj : map.values()){
            if(obj.pathway().equals(path) && obj.sequence() == seq)
                res++;
        }

        return res;
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

    public static BeyonderMap load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        BeyonderMap data = new BeyonderMap();

        CompoundTag tag = compoundTag.getCompound(NBT_BEYONDER_MAP);

        for(var obj : tag.getAllKeys()){
            data.map.put(UUID.fromString(obj), StoredData.fromNBT(tag.getCompound(obj)));
        }

        return data;
    }


    private static final SavedData.Factory<BeyonderMap> BEYONDER_MAP_FACTORY =
            new SavedData.Factory<>(
                    BeyonderMap::new,
                    BeyonderMap::load
            );

    public static BeyonderMap get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BEYONDER_MAP_FACTORY,
                "beyonder_map_str"
        );
        }
}
