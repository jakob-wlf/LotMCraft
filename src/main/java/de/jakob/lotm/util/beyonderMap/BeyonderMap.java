package de.jakob.lotm.util.beyonderMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static de.jakob.lotm.util.BeyonderData.beyonderMap;

public class BeyonderMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";
    public static final String NBT_BEYONDER_MAP_CLASS = "beyonder_map_class";

    private HashMap<UUID, StoredData> map;

    private ServerLevel server;

    public static final SavedData.Factory<BeyonderMap> FACTORY = new SavedData.Factory<>(
            BeyonderMap::new,
            BeyonderMap::new,
            null
    );


    public BeyonderMap() {
        super();

        map = new HashMap<>(300);
    }

    public BeyonderMap(CompoundTag nbt, HolderLookup.Provider provider) {
        this();

        if (nbt.contains(NBT_BEYONDER_MAP, Tag.TAG_COMPOUND)) {
            CompoundTag mapTag = nbt.getCompound(NBT_BEYONDER_MAP);

            for (String key : mapTag.getAllKeys()) {
                map.put(UUID.fromString(key), StoredData.fromNBT(mapTag.getCompound(key)));
            }
        }
    }

    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        // Don't store if this is default/empty data
        if(pathway.equals("none") || sequence == LOTMCraft.NON_BEYONDER_SEQ) {
            return; // Don't overwrite existing data with empty data
        }

        var data = map.get(entity.getUUID());
        boolean isNull = data == null;

        LOTMCraft.LOGGER.info("Put BeyonderMap: name {}, seq {}, path {}\n\tPrevious: name {}, seq {}, path {}",
                ((ServerPlayer) entity).getGameProfile().getName(), sequence, pathway,
                isNull ? "none" : data.trueName(), isNull ? LOTMCraft.NON_BEYONDER_SEQ : data.sequence(), isNull ? "none" : data.pathway());

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(data)
                .pathway(pathway)
                .sequence(sequence)
                .trueName(((ServerPlayer) entity).getGameProfile().getName())
                .build());

        setDirty();
    }

    public void put(LivingEntity entity, StoredData data){
        if(!(entity instanceof ServerPlayer)) return;

        map.put(entity.getUUID(), data);

        setDirty();
    }

    public void put(UUID entity, StoredData data){
        map.put(entity, data);

        setDirty();
    }

    public void markModified(UUID id, Boolean value){
        map.compute(id, (k, data) -> StoredData.builder
                .copyFrom(data).modified(value).build());

        setDirty();
    }

    public void markModified(LivingEntity entity, Boolean value){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        markModified(entity.getUUID(), value);
    }

    public void addHonorificName(LivingEntity entity, HonorificName name){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

        put(entity.getUUID(), StoredData.builder.copyFrom(map.get(entity.getUUID())).honorificName(name).build());
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
        LOTMCraft.LOGGER.info("Remove BeyonderMap: name {}", entity.getDisplayName().getString());

        map.remove(entity.getUUID());

        setDirty();
    }

    public void remove(UUID entity){
        map.remove(entity);

        setDirty();
    }

    public boolean isDiffPathSeq(LivingEntity entity){
        if(!(entity instanceof ServerPlayer) ) return false;
        if(!contains(entity)) put(entity);

        StoredData data = beyonderMap.get(entity).get();

        var pathway = BeyonderData.getPathway(entity);
        var sequence = BeyonderData.getSequence(entity);

        LOTMCraft.LOGGER.info("isDiffPathSeq BeyonderMap: name {}, seq {}, path {}\n\tPrevious: name {}, seq {}, path {}",
                ((ServerPlayer) entity).getGameProfile().getName(), sequence, pathway,
                data.trueName(),data.sequence(), data.pathway());

        return (!data.pathway().equals(pathway)
                || !data.sequence().equals(sequence));
    }

    public @Nullable UUID getKeyByName(String name){
        for(var obj : map.entrySet()){
            if(name.equals(obj.getValue().trueName())) return obj.getKey();
        }

        return null;
    }

    public Optional<StoredData> get(LivingEntity entity){
        if(!(entity instanceof ServerPlayer)) return Optional.empty();

        if(!map.containsKey(entity.getUUID()) || map.get(entity.getUUID()) == null) return Optional.empty();

        return Optional.of(map.get(entity.getUUID()));
    }

    public Optional<StoredData> get(UUID entity){
        if(!map.containsKey(entity)) return Optional.empty();

        return Optional.of(map.get(entity));
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
                if (seq_2 + seq_1 >= server.getGameRules().getInt(ModGameRules.SEQ_2_AMOUNT)) return false;
                break;
            case 1:
                if (seq_0 >= server.getGameRules().getInt(ModGameRules.SEQ_0_AMOUNT)
                        || seq_1 >= server.getGameRules().getInt(ModGameRules.SEQ_1_AMOUNT)) return false;
                break;
            case 0:
                if (seq_0 >= server.getGameRules().getInt(ModGameRules.SEQ_0_AMOUNT)) return false;
                break;
        }

        return true;
    }

    public boolean contains(LivingEntity entity){
        return map.containsKey(entity.getUUID());
    }

    public boolean contains(UUID id){
        return map.containsKey(id);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        LOTMCraft.LOGGER.info("Saving BeyonderMap");

        CompoundTag tag = new CompoundTag();
        for(var obj : map.entrySet()){
            tag.put(obj.getKey().toString(), obj.getValue().toNBT());
        }

        compoundTag.put(NBT_BEYONDER_MAP, tag);

        return compoundTag;
    }

    public static BeyonderMap get(ServerLevel level) {
        LOTMCraft.LOGGER.info("Loading beyonderMap");
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NBT_BEYONDER_MAP_CLASS);
    }

    public void setLevel(ServerLevel level){
        server = level;
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }

    public Set<Map.Entry<UUID, StoredData>> entrySet(){
        return map.entrySet();
    }

    public void clear(){
        map.clear();

        setDirty();
    }

    public boolean containsHonorificNameWithFirstLine(String str) {
        for(var data : map.values()){
             if(!data.honorificName().isEmpty()
                     && data.honorificName().lines().getFirst().equalsIgnoreCase(str))
                 return true;
        }

        return false;
    }

    public boolean containsHonorificNameWithLastLine(String str) {
        for(var data : map.values()){
            if(!data.honorificName().isEmpty()
                    && data.honorificName().lines().getLast().equalsIgnoreCase(str))
                return true;
        }

        return false;
    }

    public boolean containsHonorificNameWithLine(String str){
        for(var data : map.values()){
            if(!data.honorificName().isEmpty()
                    && ListHelper.containsString(data.honorificName().lines(), str))
                return true;
        }

        return false;
    }

    public @Nullable UUID findCandidat(LinkedList<String> list){
        if(list.size() < 3) return null;

        UUID originalTarget = null;

        for(var obj : map.entrySet()){
            if(ListHelper.compareLists(obj.getValue().honorificName().lines(), list)) {
                originalTarget = obj.getKey();
                break;
            }
        }

        LinkedList<UUID> possibleTargets = new LinkedList<>();

        for(var obj : map.entrySet()){
            if(obj.getKey().equals(originalTarget))
                continue;

            for(var str : list){
                if(ListHelper.containsString(obj.getValue().honorificName().lines(), str)){
                    possibleTargets.add(obj.getKey());
                }
            }
        }

        if(!possibleTargets.isEmpty()){
            if(originalTarget != null) {
                for(var obj : possibleTargets){
                    if (map.get(obj).sequence() < map.get(originalTarget).sequence())
                        return obj;
                }
            }
            else{
               return possibleTargets.stream()
                        .sorted(Comparator.comparingInt(e -> map.get(e).sequence()))
                        .toList().getFirst();
            }
        }

        return originalTarget;
    }

}

class ListHelper{
    public static boolean compareLists(List<String> list1, List<String> list2){
        if (list1.size() != list2.size()) return false;

        return IntStream.range(0, list1.size())
                .allMatch(i -> list1.get(i).equalsIgnoreCase(list2.get(i)));
    }

    public static boolean containsString(List<String> list, String str){
        return list.stream().anyMatch(line -> line.equalsIgnoreCase(str));
    }
}
