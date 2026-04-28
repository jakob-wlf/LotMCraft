package de.jakob.lotm.util.playerMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static de.jakob.lotm.util.BeyonderData.playerMap;

public class PlayerMap extends SavedData {
    public static final String NBT_BEYONDER_MAP = "beyonder_map";
    public static final String NBT_BEYONDER_MAP_CLASS = "beyonder_map_class";

    private HashMap<UUID, StoredData> map;

    private ServerLevel server;

    public static final SavedData.Factory<PlayerMap> FACTORY = new SavedData.Factory<>(
            PlayerMap::new,
            PlayerMap::new,
            null
    );

    public PlayerMap() {
        super();

        map = new HashMap<>(300);
    }

    public PlayerMap(CompoundTag nbt, HolderLookup.Provider provider) {
        this();

        if (nbt.contains(NBT_BEYONDER_MAP, Tag.TAG_COMPOUND)) {
            CompoundTag mapTag = nbt.getCompound(NBT_BEYONDER_MAP);

            for (String key : mapTag.getAllKeys()) {
                map.put(UUID.fromString(key), StoredData.fromNBT(mapTag.getCompound(key), provider));
            }
        }
    }

    public void setSefirot(UUID id, String value){
        map.compute(id, (k, data) -> StoredData.builder.copyFrom(data).sefirot(value).build());

        setDirty();
    }

    public @Nullable UUID findBySefirot(String sefirot){
        for(var obj : map.entrySet()){
            if(obj.getValue().claimedSefirot().equals(sefirot))
                return obj.getKey();
        }

        return null;
    }

    public boolean anyPlayerHoldsUniqueness(String pathway) {
        for (var data : map.values()) {
            if (data.uniqueness().equalsIgnoreCase(pathway)) {
                return true;
            }
        }
        return false;
    }

    public void setUniqueness(LivingEntity entity, String pathway) {
        if (!(entity instanceof ServerPlayer)) return;

        if (!contains(entity)) put(entity);

        var data = map.get(entity.getUUID());
        map.put(entity.getUUID(), StoredData.builder.copyFrom(data).uniqueness(pathway).build());

        setDirty();
    }

    public void setProphecies(UUID entity, List<Prophecy> list){
        map.put(entity, StoredData.builder.copyFrom(map.get(entity)).prophecies((LinkedList<Prophecy>) list).build());

        setDirty();
    }

    public void addProphecy(UUID entity, Prophecy prophecy){
        var list = map.get(entity).prophecies();
        list.add(prophecy);

        map.put(entity, StoredData.builder.copyFrom(map.get(entity)).prophecies(list).build());

        setDirty();
    }

    public void removeProphecy(UUID entity, Prophecy prophecy){
        var list = map.get(entity).prophecies();
        list.remove(prophecy);

        map.put(entity, StoredData.builder.copyFrom(map.get(entity)).prophecies(list).build());

        setDirty();
    }

    public void addProphecy(LivingEntity entity, Prophecy prophecy){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) put(entity);

       addProphecy(entity.getUUID(), prophecy);
    }

    public void removeProphecy(LivingEntity entity, Prophecy prophecy){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) {
            put(entity);
            return;
        }

        removeProphecy(entity.getUUID(), prophecy);
    }

    public void onPlayerUUIDChange(ServerPlayer player){
        String name = player.getGameProfile().getName();

        UUID inMapId = getKeyByName(name);
        if(inMapId == null) return;

        if(!inMapId.equals(player.getUUID())){
            var data = map.get(inMapId);
            remove(inMapId);

            put(player, data);
        }
    }

    public void put(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer)) return;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        UniquenessComponent uniquenessComponent = entity.getData(ModAttachments.UNIQUENESS_COMPONENT);
        String uniqueness = uniquenessComponent.hasUniqueness() ? uniquenessComponent.getUniquenessPathway() : "none";


        // Don't store if this is default/empty data
//        if(pathway.equals("none") || sequence == LOTMCraft.NON_BEYONDER_SEQ) {
//            return; // Don't overwrite existing data with empty data
//        }

        var data = map.get(entity.getUUID());
        boolean isNull = data == null;

        LOTMCraft.LOGGER.info("Put BeyonderMap: name {}, seq {}, path {}\n\tPrevious: name {}, seq {}, path {}",
                ((ServerPlayer) entity).getGameProfile().getName(), sequence, pathway,
                isNull ? "none" : data.trueName(), isNull ? LOTMCraft.NON_BEYONDER_SEQ : data.sequence(), isNull ? "none" : data.pathway());

        int[] componentCharStack = entity.getData(ModAttachments.BEYONDER_COMPONENT)
                .getCharacteristicStack();

        String[] pathwayHistory = entity.getData(ModAttachments.BEYONDER_COMPONENT).getPathwayHistory();

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(data)
                .pathway(pathway)
                .sequence(sequence)
                .trueName(((ServerPlayer) entity).getGameProfile().getName())
                .charStackArray(componentCharStack)
                .pathwayHistory(pathwayHistory)
                .uniqueness(uniqueness)
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

    public void removeHonorificName(LivingEntity entity){
        if(!(entity instanceof ServerPlayer)) return;

        if(!contains(entity)) {
            put(entity);

            return;
        }

        put(entity.getUUID(), StoredData.builder.copyFrom(map.get(entity.getUUID())).honorificName(HonorificName.EMPTY).build());
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

        StoredData data = playerMap.get(entity).get();

        var pathway = BeyonderData.getPathway(entity);
        var sequence = BeyonderData.getSequence(entity);

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

    public int count(String path, int seq) {
        int res = 0;
        for (var obj : map.values()) {
            if (obj.pathway().equals(path) && obj.sequence() == seq) {
                res++;
                res += obj.charStack()[seq];
            }
        }
        return res;
    }

    public boolean check(String path, int seq){
        int seq_0 = count(path, 0),
                seq_1 = count(path, 1),
                seq_2 = count(path, 2),
                seq_3 = count(path, 3),
                seq_4 = count(path, 4),
                seq_5 = count(path, 5),
                seq_6 = count(path, 6),
                seq_7 = count(path, 7),
                seq_8 = count(path, 8)
        ;

        switch (seq) {
            case 0:
                if (seq_0 >= server.getGameRules().getInt(ModGameRules.SEQ_0_AMOUNT)) return false;
                break;
            case 1:
                if (seq_0 >= server.getGameRules().getInt(ModGameRules.SEQ_0_AMOUNT)
                        || seq_1 >= server.getGameRules().getInt(ModGameRules.SEQ_1_AMOUNT)) return false;
                break;
            case 2:
                if (seq_2 + seq_1 >= server.getGameRules().getInt(ModGameRules.SEQ_2_AMOUNT)) return false;
                break;
            case 3:
                if (seq_3 + seq_2 >= server.getGameRules().getInt(ModGameRules.SEQ_3_AMOUNT)) return false;
                break;
            case 4:
                if (seq_4 + seq_3 >= server.getGameRules().getInt(ModGameRules.SEQ_4_AMOUNT)) return false;
                break;
            case 5:
                if (seq_5 + seq_4 >= server.getGameRules().getInt(ModGameRules.SEQ_5_AMOUNT)) return false;
                break;
            case 6:
                if (seq_6 + seq_5 >= server.getGameRules().getInt(ModGameRules.SEQ_6_AMOUNT)) return false;
                break;
            case 7:
                if (seq_7 + seq_6 >= server.getGameRules().getInt(ModGameRules.SEQ_7_AMOUNT)) return false;
                break;
            case 8:
                if (seq_8 + seq_7 >= server.getGameRules().getInt(ModGameRules.SEQ_8_AMOUNT)) return false;
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
            tag.put(obj.getKey().toString(), obj.getValue().toNBT(provider));
        }

        compoundTag.put(NBT_BEYONDER_MAP, tag);

        return compoundTag;
    }

    public static PlayerMap get(ServerLevel level) {
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

    public @Nullable UUID findCandidate(LinkedList<String> list){
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

    public void addLastPosition(LivingEntity entity){
        if(!contains(entity)) put(entity);

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(map.get(entity.getUUID()))
                .lastPosition(entity.position())
                .build());

        setDirty();
    }

    public void addStack(LivingEntity entity, int value, int sequence) {
        if (!contains(entity)) put(entity);

        int current = playerMap.get(entity.getUUID()).get().charStack()[sequence];
        setStack(entity, current + value, sequence);
    }

    /**
     * Records that the player switched pathways at the given sequence.
     * The previousPathway is stored so regression can restore it.
     */
    public void recordPathwaySwitch(LivingEntity entity, int sequence, String previousPathway) {
        if (!contains(entity)) put(entity);

        var current = playerMap.get(entity.getUUID()).get();
        String[] history = Arrays.copyOf(current.pathwayHistory(), 10);
        history[sequence] = previousPathway;

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(current)
                .pathwayHistory(history)
                .build());

        entity.getData(ModAttachments.BEYONDER_COMPONENT).setPathwayHistory(history);

        setDirty();
    }

    public void setStack(LivingEntity entity, int value, int sequence) {
        if (!contains(entity)) put(entity);

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(map.get(entity.getUUID()))
                .charStack(value, sequence)
                .build());

        setDirty();
    }

    public void clearStack(LivingEntity entity) {
        if (!contains(entity)) put(entity);

        map.put(entity.getUUID(), StoredData.builder
                .copyFrom(map.get(entity.getUUID()))
                .clearCharStack()
                .build());

        setDirty();
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
