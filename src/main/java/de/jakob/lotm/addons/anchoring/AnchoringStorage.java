package de.jakob.lotm.addons.anchoring;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.factions.FactionCore;
import de.jakob.lotm.addons.factions.FactionStorage;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnchoringStorage extends SavedData {
    public static final String NBT_MAP = "anchoring_map";
    public static final String NBT_CLASS = "anchoring";

    private final Map<String, AnchoringCore> anchoring;

    public static final SavedData.Factory<AnchoringStorage> FACTORY = new SavedData.Factory<>(
            AnchoringStorage::new,
            AnchoringStorage::load,
            null
    );

    public AnchoringStorage(){
        super();

        anchoring = new HashMap<>(100);
    }


    public AnchoringCore getAnchoring(String name){
        if(!anchoring.containsKey(name)) createAnchoring(name);

        return anchoring.get(name);
    }

    public void createAnchoring(String name){
        if(anchoring.containsKey(name)) return;

        var obj = new AnchoringCore();
        anchoring.put(name, obj);

        setDirty();
    }

    public void addAnchoring(String name, int amount){
        if(!anchoring.containsKey(name)) createAnchoring(name);

        var obj = anchoring.get(name);
        obj.addTotalAnchoring(amount);
        anchoring.put(name, obj);

        setDirty();
    }

    public void removeAnchoring(String name, int amount){
        if(!anchoring.containsKey(name)){
            createAnchoring(name);
            return;
        }

        var obj = anchoring.get(name);
        obj.removeTotalAnchoring(amount);
        anchoring.put(name, obj);

        setDirty();
    }

    public void recalculateAnchoring(String name, ServerLevel level){
        if(!anchoring.containsKey(name)){
            createAnchoring(name);
            return;
        }

        var obj = anchoring.get(name);

        var church = BeyonderData.factionStorage.getPartOfFactionType(name, 2);
        if(church != null) {
            int modifier = AnchoringCore.getAnchoringOnNobleLevel(church.getPlayerLevel(name));

            if(modifier != 0) {
                int checkSum = 0;
                var citizenList = church.getAllCitizens();

                for (var cname : citizenList){
                    var id = BeyonderData.playerMap.getKeyByName(cname);
                    if(id == null) continue;

                    var seq = BeyonderData.playerMap.get(id).get().sequence();
                    int anchoringLevel = AnchoringCore.getAnchoringValueForSeq(seq);

                    var buff = anchoring.get(cname);

                    checkSum += (anchoringLevel + (buff.getAvatarAmount() != 0 ? buff.getAvatarAmount()/2 : 0));
                }

                var nobleList = church.getAllNobles();
                for(var noble : nobleList.entrySet()){
                    if(noble.getValue() >= AnchoringCore.getMinimalNobleLevel()) continue;

                    var id = BeyonderData.playerMap.getKeyByName(noble.getKey());
                    if(id == null) continue;

                    var seq = BeyonderData.playerMap.get(id).get().sequence();
                    int anchoringLevel = AnchoringCore.getAnchoringValueForSeq(seq);

                    var buff = anchoring.get(noble.getKey());

                    checkSum += (anchoringLevel + (buff.getAvatarAmount() != 0 ? buff.getAvatarAmount()/2 : 0));
                }

                obj.setAnchoring(checkSum/modifier + obj.getAvatarAmount());
            }
        }

        anchoring.put(name, obj);
        setDirty();
    }

    public void recalculateAvatars(ServerLevel level){
        for(var obj : anchoring.entrySet()){
            obj.getValue().recalculateAvatars(level);

            anchoring.put(obj.getKey(), obj.getValue());
        }

        setDirty();
    }

    public void addAvatar(String name, UUID avatarId){
        if(!anchoring.containsKey(name)){
            createAnchoring(name);
        }

        var obj = anchoring.get(name);
        obj.addAvatar(avatarId);
        anchoring.put(name, obj);

        setDirty();
    }


    public static AnchoringStorage load(CompoundTag tag, HolderLookup.Provider provider) {
        AnchoringStorage storage = new AnchoringStorage();

        if (!tag.contains(NBT_MAP, Tag.TAG_LIST))
            return storage;

        ListTag list = tag.getList(NBT_MAP, Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag anchoringTag = (CompoundTag) t;

            String id = anchoringTag.getString("id");

            storage.anchoring.put(id, AnchoringCore.fromNbt(
                    anchoringTag.getCompound("data")
            ));
        }

        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        LOTMCraft.LOGGER.info("Saving Anchoring");

        ListTag list = new ListTag();

        for (Map.Entry<String, AnchoringCore> entry : anchoring.entrySet()) {
            CompoundTag anchoringTag = new CompoundTag();

            anchoringTag.putString("id", entry.getKey());

            CompoundTag data = entry.getValue().toNbt();

            anchoringTag.put("data", data);

            list.add(anchoringTag);
        }

        tag.put(NBT_MAP, list);

        return tag;
    }

    public static AnchoringStorage get(ServerLevel level) {
        LOTMCraft.LOGGER.info("Loading Anchoring");
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NBT_CLASS);
    }
}
