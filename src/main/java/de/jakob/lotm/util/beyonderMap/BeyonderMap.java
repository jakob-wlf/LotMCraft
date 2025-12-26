package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

import static de.jakob.lotm.util.BeyonderData.beyonderMap;

public class BeyonderMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";
    public static final String NBT_BEYONDER_MAP_CLASS = "beyonder_map_class";

    public HashMap<UUID, StoredData> map;

    public BeyonderMap() {
        super();

        map = new HashMap<>(300);
    }

    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        var data = map.get(entity.getUUID());
        boolean isNull = data == null;

        map.put(entity.getUUID(), new StoredData(
                BeyonderData.getPathway(entity),
                BeyonderData.getSequence(entity),
                isNull? HonorificName.EMPTY : data.honorificName(),
                ((ServerPlayer) entity).getGameProfile().getName(),
                isNull ? new LinkedList<>() : data.msgs(),
                isNull ? new LinkedList<>() : data.knownNames()
        ));

        setDirty();
    }

    public void put(LivingEntity entity, StoredData data){
        if(!(entity instanceof ServerPlayer)) return;

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void addHonorificName(LivingEntity entity, HonorificName name){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        map.compute(entity.getUUID(), (k, data) -> new StoredData(data.pathway(),
                data.sequence(), name, data.trueName(), data.msgs(), data.knownNames()));

        setDirty();
    }

    public void addKnownHonorificName(LivingEntity entity, HonorificName name){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        data.knownNames().add(name);

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void removeKnownHonorificName(LivingEntity entity, HonorificName name){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        data.knownNames().remove(name);

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void addMessage(LivingEntity entity, MessageType msg){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        data.addMsg(msg);

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void removeMessage(LivingEntity entity, MessageType msg){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        data.removeMsg(msg);

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public @Nullable MessageType popMessage(LivingEntity entity){
        if(!(entity instanceof ServerPlayer)) return null;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        if(data.msgs().isEmpty()) return null;

        var buff = data.msgs().getFirst();
        data.removeMsg(buff);

        map.put(entity.getUUID(), data);

        setDirty();

        return buff;
    }

    public void markRead(LivingEntity entity, int index){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        if(data.msgs().isEmpty()) return;

        var msg = data.msgs().remove(index);
        msg.setRead(true);

        data.msgs().add(msg);

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void remove(LivingEntity entity){
        map.remove(entity.getUUID());

        setDirty();
    }

    public boolean isDiffPathSeq(LivingEntity entity){
        if(!(entity instanceof ServerPlayer) ) return false;
        if(!contains(entity)) put(entity);

        StoredData data = beyonderMap.get(entity).get();

        return (!data.pathway().equals(BeyonderData.getPathway(entity))
                || data.sequence() != BeyonderData.getSequence(entity));
    }

    public @Nullable UUID getKeyByName(String name){
        for(var obj : map.entrySet()){
            if(name.equals(obj.getValue().trueName())) return obj.getKey();
        }

        return null;
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
                if (seq_0 != 0 || seq_1 >= 1) return false;
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
                NBT_BEYONDER_MAP_CLASS
        );
    }
}
