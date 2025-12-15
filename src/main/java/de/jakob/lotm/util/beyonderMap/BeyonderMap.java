package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SefirotData;
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

public class BeyonderMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";

    public HashMap<UUID, StoredData> map;

    public BeyonderMap() {
        super();

        map = new HashMap<>(300);
    }

    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        map.put(entity.getUUID(), new StoredData(BeyonderData.getPathway(entity),
                BeyonderData.getSequence(entity), null));
    }

    public void addHonorificName(LivingEntity entity, String name){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());

        map.put(entity.getUUID(), new StoredData(data.pathway(), data.sequence(), name));
    }

    public void remove(LivingEntity entity){
        map.remove(entity.getUUID());
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

    public boolean check(String path, int seq){
        int seq_0 = count(path, 0),
                seq_1 = count(path, 1),
                seq_2 = count(path, 2);

        switch (seq) {
            case 2:
                if (seq_2 + seq_1 >= 9) return false;
                break;
            case 1:
                if (seq_0 != 0 || seq_1 >= 3) return false;
                break;
            case 0:
                if (seq_0 != 0) return false;
                break;
        }

        return true;
    }

    public boolean contains(LivingEntity entity){
        return map.containsKey(entity.getUUID());
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

    public static BeyonderMap get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<BeyonderMap>(
                BeyonderMap::new, BeyonderMap::load),
                NBT_BEYONDER_MAP
        );
    }
}
