package de.jakob.lotm.addons.factions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.PlayerMap;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;

public class FactionStorage extends SavedData {
    public static final String NBT_MAP = "faction_map";
    public static final String NBT_CLASS = "factions";
    private final Map<Integer, FactionCore> factions;

    private final Map<Integer, Long> lastOnline;

    public static final SavedData.Factory<FactionStorage> FACTORY = new SavedData.Factory<>(
            FactionStorage::new,
            FactionStorage::load,
            null
    );

    public FactionStorage() {
        super();

        factions = new HashMap<>(300);
        lastOnline = new HashMap<>(300);
    }

    public void setFaction(int id, FactionCore faction) {
        factions.put(id, faction);
        setDirty();
    }

    public Set<Map.Entry<Integer, Long>> getLastOnlineEntrySet(){
        return lastOnline.entrySet();
    }

    public void levelUp(int id) {
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

    public void promote(int id, String name, int level) {
        var faction = factions.get(id);
        if (!faction.isPartOfFaction(name)) return;

        int pLevel = faction.getPlayerLevel(name);

        if (pLevel == 1) {
            faction.removeCitizen(name);
        } else if (pLevel > 1 && pLevel < 8) {
            faction.removeNoble(name);
        } else if (pLevel == 8) {
            faction.removeCoLeader(name);
        } else if (pLevel == 9) {
            faction.setLeader("NONE");
        }

        if (level == 1) {
            faction.addCitizen(name);
        } else if (level > 1 && level < 8) {
            faction.addNoble(name, level - 1);
        } else if (level == 8) {
            faction.addCoLeader(name);
        } else if (level == 9) {
            faction.setLeader(name);
        }

        factions.put(id, faction);

        setDirty();
    }

    public void createFaction(String leader, String factionName, int type) {
        int id = 0;

        if (!factions.isEmpty())
            id = Collections.max(factions.keySet()) + 1;

        FactionCore core = new FactionCore(leader, id, type);
        core.setName(factionName);

        factions.put(core.getId(), core);

        setDirty();
    }

    public boolean isCore(ChunkPos pos, int type) {
        var factions = getFaction(pos);
        if (factions.isEmpty()) return false;

        var factionOp = factions.stream().filter(obj -> obj.getType() == type).findFirst();
        if (factionOp.isEmpty()) return false;

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

        if (faction.getType() != 1) return;
        for (var obj : factionsChunk) {
            if (obj.getId() == id) continue;

            obj.unclaim(pos);
        }
    }

    public void disband(int id) {
        factions.remove(id);
        lastOnline.remove(id);

        setDirty();
    }

    public boolean canClaimNation(int id, ChunkPos pos) {
        var faction = getFaction(id);
        var claimedChunks = faction.getClaimed();

        if (claimedChunks.isEmpty()) return true;

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

    public void setLevel(int id, int level){
        var faction = getFaction(id);
        faction.setLevel(level);

        factions.put(id, faction);
        setDirty();
    }

    public void addCitizen(int id, String name) {
        var faction = factions.get(id);

        faction.addCitizen(name);

        factions.put(id, faction);

        setDirty();
    }

    public void leave(int id, String name) {
        var faction = factions.get(id);
        int level = faction.getPlayerLevel(name);

        if (level == 1) {
            faction.removeCitizen(name);
        } else if (level > 1 && level < 8) {
            faction.removeNoble(name);
        } else if (level == 8) {
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

        for (Map.Entry<Integer, Long> entry : lastOnline.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("key", entry.getKey());
            entryTag.putLong("value", entry.getValue());
            list.add(entryTag);
        }

        tag.put("last_online", list);


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

        storage.lastOnline.clear();

        ListTag list2 = tag.getList("last_online", Tag.TAG_COMPOUND);
        for (int i = 0; i < list2.size(); i++) {
            CompoundTag entryTag = list2.getCompound(i);

            int key = entryTag.getInt("key");
            long value = entryTag.getLong("value");

            storage.lastOnline.put(key, value);
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

    public void messageEveryoneInFaction(ServerLevel level, int id, Component msg) {
        var faction = factions.get(id);
        var all = faction.getAllPlayers();

        for (var obj : all) {
            var pId = BeyonderData.playerMap.getKeyByName(obj);
            if (pId == null) continue;

            var player = level.getPlayerByUUID(pId);
            if (player == null) continue;

            player.sendSystemMessage(msg);
        }
    }

    // 0 - no war, 1 - war against nation, 2 - war against church
    public int isAtWar(String playerName, ChunkPos pos) {
        var partOf = getPartOfFaction(playerName);
        var factions = getFaction(pos);

        if (factions.isEmpty()) return 0;

        for (var obj : partOf) {
            if (obj.isAtWar(factions.getFirst().getId())) return 1;
            if (obj.isAtWar(factions.getLast().getId())) return 2;
        }

        return 0;
    }

    public boolean isPartOfAndClaimed(String name, ChunkPos pos){
        var factions = getPartOfFaction(name);

        for(var obj : factions){
            if(obj.isClaimed(pos)) return true;
        }

        return false;
    }

    public int getFactionIdFromPosType(ChunkPos pos, int type){
        var list = getFaction(pos);
        for(var obj : list){
            if(obj.getType() == type) return obj.getId();
        }

        return -1;
    }

    public void declareWar(int aggressor, int victim) {
        var a = factions.get(aggressor);
        var v = factions.get(victim);

        a.addAtWar(victim, true);
        v.addAtWar(aggressor, false);

        if (a.hasPermission(victim)) {
            a.removePermission(victim);
        } else if (v.hasPermission(aggressor)) {
            v.removePermission(aggressor);
        }

        factions.put(aggressor, a);
        factions.put(victim, v);

        setDirty();
    }

    public void winWar(List<Integer> winnersId, int looserId, ServerLevel level) {
        var looser = factions.get(looserId);
        int looserLevel = looser.getLevel();

        for(var winnerId : winnersId) {
            var winner = factions.get(winnerId);

            if(winner.getLevel() <= looserLevel)
                winner.setTotalWins(winner.getTotalWins() + 1);
            winner.removeAtWar(looserId);

            factions.put(winnerId, winner);

            messageEveryoneInFaction(level, winnerId, Component.literal("Your faction has won the war against \"" + factions.get(looserId).getName() + "\"").withStyle(ChatFormatting.GREEN));
        }

        factions.remove(looserId);

        setDirty();
    }

    public void stopWar(int id1, int id2) {
        var faction1 = factions.get(id1);
        var faction2 = factions.get(id2);

        faction1.removeAtWar(id2);
        faction2.removeAtWar(id1);

        factions.put(id1, faction1);
        factions.put(id2, faction2);

        setDirty();
    }

    //returns the timestamp of when at least half of members of faction were online
    public long getLastOnline(int id){
        if(lastOnline.containsKey(id)){
            return lastOnline.get(id);
        }

        return System.currentTimeMillis();
    }

    public void setLastOnline(int id, long value){
        lastOnline.put(id, value);
    }

    public boolean isAtAnyWar(int id){
        var faction = factions.get(id);
        return !faction.getAllAtWar().isEmpty();
    }

    public void updateLastOnline(int id, ServerLevel level){
        var faction = factions.get(id);

        int online = 0;
        for(var obj : level.players()){
            var name = obj.getName().getString();

            if(faction.isPartOfFaction(name)){
                online++;
            }
        }

        if(online >= faction.getAllPlayers().size() / 2){
            lastOnline.put(id, System.currentTimeMillis());
        }
    }
}
