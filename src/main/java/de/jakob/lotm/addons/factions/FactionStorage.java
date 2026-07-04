package de.jakob.lotm.addons.factions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.playerMap.PlayerMap;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;

public class FactionStorage extends SavedData {
    public static final String NBT_MAP = "faction_map";
    public static final String NBT_CLASS = "factions";
    private final Map<Integer, FactionCore> factions;

    public static final SavedData.Factory<FactionStorage> FACTORY = new SavedData.Factory<>(
            FactionStorage::new,
            FactionStorage::load,
            null
    );

    public FactionStorage() {
        super();

        factions = new HashMap<>(300);
    }

    public void setFaction(int id, FactionCore faction){
        factions.put(id, faction);
        setDirty();
    }

    public void levelUp(int id){
        var faction = factions.get(id);

        int currentLevel = faction.getLevel();
        int soliN = FactionCore.getLevelUpSoli(currentLevel);
        int poundsN = FactionCore.getLevelUpPounds(currentLevel);

        faction.setLevel(currentLevel + 1);
        faction.setSoli(faction.getSoli() - soliN);
        faction.setPound(faction.getPound() - poundsN);

        factions.put(id, faction);
    }

    public static FactionStorage get(ServerLevel level) {
        LOTMCraft.LOGGER.info("Loading Factions");
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NBT_CLASS);
    }

    public @Nullable FactionCore getFaction(int id) {
        return factions.get(id);
    }

    public void promote(int id, String name, int level){
        var faction = factions.get(id);
        if(!faction.isPartOfFaction(name)) return;

        int pLevel = faction.getPlayerLevel(name);

        if(pLevel == 1){
            faction.removeCitizen(name);
        }
        else if(pLevel > 1 && pLevel < 8){
            faction.removeNoble(name);
        }
        else if(pLevel == 8){
            faction.removeCoLeader(name);
        }
        else if(pLevel == 9){
            faction.setLeader("NONE");
        }

        if(level == 1){
            faction.addCitizen(name);
        }
        else if(level > 1 && level < 8){
            faction.addNoble(name, level - 1);
        }
        else if(level == 8){
            faction.addCoLeader(name);
        }
        else if(level == 9){
            faction.setLeader(name);
        }

        factions.put(id, faction);

        setDirty();
    }

    public void createFaction(String leader, String factionName, int type) {
        FactionCore core = new FactionCore(leader, factions.size(), type);
        core.setName(factionName);

        factions.put(core.getId(), core);

        setDirty();
    }

    public boolean isCore(ChunkPos pos, int type){
        var factions = getFaction(pos);
        if(factions.isEmpty()) return false;

        var factionOp = factions.stream().filter(obj -> obj.getType() == type).findFirst();
        if(factionOp.isEmpty()) return false;

        return factionOp.get().getCore().equals(pos);
    }

    public boolean isNameUnique(String name) {
        for (var obj : factions.values()) {
            if (obj.getName().equalsIgnoreCase(name))
                return false;
        }

        return true;
    }

    public boolean hasAnyByType(String name, int type) {
        for (var faction : factions.values()) {
            if (faction.isPartOfFaction(name) && faction.getType() == type)
                return true;
        }

        return false;
    }

    public List<FactionCore> getPartOfFaction(String name) {
        List<FactionCore> result = new LinkedList<>();

        for (var obj : factions.values()) {
            if (obj.isPartOfFaction(name))
                result.add(obj);
        }

        return result;
    }

    public @Nullable FactionCore getPartOfFactionType(String name, int type) {
        for (var obj : factions.values()) {
            if (obj.isPartOfFaction(name) && obj.getType() == type)
                return obj;
        }

        return null;
    }

    public String getAllShortInfo() {
        StringBuilder builder = new StringBuilder("Factions: " + factions.size() + "\n");

        if (factions.isEmpty()) return builder.toString();

        for (var faction : factions.values()) {
            builder.append(faction.getShortInfo()).append("\n");
        }

        return builder.toString();
    }

    public void claim(int id, ChunkPos pos, int level) {
        var faction = factions.get(id);

        faction.addClaim(pos, level);

        factions.put(id, faction);

        setDirty();
    }

    public void unclaim(int id, ChunkPos pos) {
        var factionsChunk = getFaction(pos);

        var faction = getFaction(id);

        faction.unclaim(pos);

        if(faction.getType() != 1) return;
        for (var obj : factionsChunk) {
            if(obj.getId() == id) continue;

            obj.unclaim(pos);
        }
    }

    public void disband(int id){
        factions.remove(id);

        setDirty();
    }

    public boolean canClaimNation(int id, ChunkPos pos){
        var faction = getFaction(id);
        var claimedChunks = faction.getClaimed();

        if(claimedChunks.isEmpty()) return true;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                if (claimedChunks.contains(new ChunkPos(pos.x + dx, pos.z + dz))) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addCitizen(int id, String name){
        var faction = factions.get(id);

        faction.addCitizen(name);

        factions.put(id, faction);

        setDirty();
    }

    public void leave(int id, String name){
        var faction = factions.get(id);
        int level = faction.getPlayerLevel(name);

        if(level == 1){
            faction.removeCitizen(name);
        }
        else if(level > 1 && level < 8){
            faction.removeNoble(name);
        }
        else if(level == 8){
            faction.removeCoLeader(name);
        }

        factions.put(id, faction);

        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        LOTMCraft.LOGGER.info("Saving Factions");

        ListTag list = new ListTag();

        for (Map.Entry<Integer, FactionCore> entry : factions.entrySet()) {
            CompoundTag factionTag = new CompoundTag();

            factionTag.putInt("id", entry.getKey());

            CompoundTag data = new CompoundTag();
            entry.getValue().save(data, provider);

            factionTag.put("data", data);

            list.add(factionTag);
        }

        tag.put("factions", list);

        return tag;
    }

    public boolean isClaimed(ChunkPos pos, int type) {
        for (var faction : factions.values()) {
            if (faction.isClaimed(pos) && faction.getType() == type) return true;
        }

        return false;
    }

    public int getClaimLevel(ChunkPos pos, int type) {
        for (var faction : factions.values()) {
            if (faction.isClaimed(pos) && faction.getType() == type) return faction.getClaimLevel(pos);
        }

        return 0;
    }

    public boolean isClaimedType(ChunkPos pos, int type) {
        for (var faction : factions.values()) {
            if (faction.getType() == type && faction.isClaimed(pos)) return true;
        }

        return false;
    }

    public static FactionStorage load(CompoundTag tag, HolderLookup.Provider provider) {
        FactionStorage storage = new FactionStorage();

        if (!tag.contains("factions", Tag.TAG_LIST))
            return storage;

        ListTag list = tag.getList("factions", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag factionTag = (CompoundTag) t;

            int id = factionTag.getInt("id");
            FactionCore faction = FactionCore.load(
                    factionTag.getCompound("data"),
                    provider
            );

            storage.factions.put(id, faction);
        }

        return storage;
    }

    public List<FactionCore> getFaction(ChunkPos pos) {
        List<FactionCore> result = new LinkedList<>();

        for (var obj : factions.values()) {
            if (obj.isClaimed(pos))
                result.add(obj);
        }

        return result;
    }

    public void addPermission(int church, int nation) {
        if (!factions.containsKey(nation) && !factions.containsKey(church)) return;

        var faction = factions.get(church);
        faction.addPermission(nation);

        factions.put(church, faction);

        setDirty();
    }

    public void removePermission(int church, int nation) {
        if (!factions.containsKey(nation) && !factions.containsKey(church)) return;

        var faction = factions.get(church);
        if (!faction.hasPermission(nation)) return;

        faction.removePermission(nation);

        var nationF = factions.get(nation);
        faction.removeClaimed(nationF.getClaimed());

        factions.put(church, faction);

        setDirty();
    }

    public boolean hasPermission(int church, int nation) {
        if (!factions.containsKey(nation) && !factions.containsKey(church)) return false;

        return factions.get(church).hasPermission(nation);
    }

    public boolean contains(int id) {
        return factions.containsKey(id);
    }
}
