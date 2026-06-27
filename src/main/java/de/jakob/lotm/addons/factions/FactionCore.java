package de.jakob.lotm.addons.factions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.querz.mca.Chunk;

import java.util.*;

public class FactionCore {
    private final int id;
    private int level;
    private final int type; // 1 - nation, 2 - church
    private String leader;
    private String name;
    private List<String> coLeaders;
    private Map<String, Integer> nobles;
    private List<String> citizens;
    private Set<ChunkInfo> claimed;

    public FactionCore(String leader, int id, int type){
        this.id = id;
        level = 0;
        this.leader = leader;
        coLeaders = new LinkedList<>();
        nobles = new HashMap<>();
        citizens = new LinkedList<>();
        claimed = new HashSet<>();
        this.type = type;
    }

    public static int noblesAmountPerLevel(int level){
        return switch (level){
          case 0 -> 0;
          case 1 -> 5;
          case 2 -> 10;
          case 3 -> 20;
          default -> 0;
        };
    }

    public static int citizensAmountPerLevel(int level){
        return switch (level){
            case 0 -> 0;
            case 1 -> 30;
            case 2 -> 45;
            case 3 -> 60;
            default -> 0;
        };
    }

    public static int coLeadersAmountPerLevel(int level){
        return switch (level){
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            default -> 0;
        };
    }

    public boolean isOutOfSlotsCitizens(){
        return citizens.size() >= citizensAmountPerLevel(level);
    }

    public boolean isOutOfSlotsCoLeaders(){
        return coLeaders.size() >= coLeadersAmountPerLevel(level);
    }

    public boolean isOutOfSlotsNobles(){
        return nobles.size() >= noblesAmountPerLevel(level);
    }

    public boolean isPartOfFaction(String name){
        return leader.equals(name) || coLeaders.contains(name) || nobles.containsKey(name) || citizens.contains(name);
    }

    public boolean isLeader(String name){
        return leader.equals(name);
    }

    public boolean isCoLeader(String name){
        return coLeaders.contains(name);
    }

    public boolean isNoble(String name){
        return nobles.containsKey(name);
    }

    public boolean isCitizen(String name){
        return citizens.contains(name);
    }

    public int getLevel(){
        return level;
    }

    public int getNobleLevel(String name){
        if(!isNoble(name)) return -1;

        return nobles.get(name);
    }

    public String getName(){
        return name;
    }

    public int getId(){
        return id;
    }

    public int getType(){
        return type;
    }

    public String getLeader(){
        return leader;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setLeader(String name){
        leader = name;
    }

    public void addCoLeader(String name){
        if(isOutOfSlotsCoLeaders()) return;

        coLeaders.add(name);
    }

    public void addNoble(String name, int level){
        if(isOutOfSlotsNobles()) return;

        nobles.put(name, level);
    }

    public void addCitizen(String name){
        if(isOutOfSlotsCitizens()) return;

        citizens.add(name);
    }

    public boolean isClaimed(ChunkPos pos){
        for (var obj : claimed){
            if(obj.pos().equals(pos))
                return true;
        }

        return false;
    }

    public int getClaimLevel(ChunkPos pos){
        if(!isClaimed(pos)) return -1;

        return claimed.stream().filter(i -> i.pos().equals(pos)).findFirst().get().accessLevel();
    }

    private ChunkInfo getClaim(ChunkPos pos){
        return claimed.stream().filter(i -> i.pos().equals(pos)).findFirst().get();
    }

    public void addClaim(ChunkPos pos, int level){
        if(isClaimed(pos)){
            var obj = getClaim(pos);
            if(obj.accessLevel() == level) return;

            claimed.remove(obj);
        }

        claimed.add(new ChunkInfo(pos, level));
    }

    public void removeClaim(ChunkPos pos){
        if(!isClaimed(pos))
            return;

        claimed.remove(getClaim(pos));
    }

    public void removeCoLeader(String name){
        coLeaders.remove(name);
    }
    public void removeNoble(String name){
        nobles.remove(name);
    }
    public void removeCitizen(String name){
        citizens.remove(name);
    }

    public String getAllInfo(){
        return "Leader: " + leader
                + "\nName: " + name
                + "\nLevel: " + level
                + "\nType: " + (type == 1 ? "Nation" : "Church");
    }

    public String getShortInfo(){
        return "Leader: " + leader + " Name: " + name + " Type: " + (type == 1 ? "Nation" : "Church");
    }

    public boolean canDoAnything(String name, ChunkPos pos){
        if(leader.equals(name)) return true;

        var claimLevel = getClaimLevel(pos);
        var playerLevel = getPlayerLevel(name);

        return playerLevel >= claimLevel;
    }

    public int getPlayerLevel(String name){
        if(leader.equals(name)) return 9;

        if(coLeaders.contains(name)) return 8;

        if(nobles.containsKey(name)){
            return nobles.get(name) + 1;
        }

        if(citizens.contains(name)) return 1;
        return 0;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("level", level);
        tag.putString("leader", leader);
        tag.putInt("id", id);

        ListTag coLeaderList = new ListTag();
        for (String s : coLeaders) {
            coLeaderList.add(StringTag.valueOf(s));
        }
        tag.put("co_leaders", coLeaderList);

        ListTag nobleList = new ListTag();
        for (Map.Entry<String, Integer> entry : nobles.entrySet()) {
            CompoundTag noble = new CompoundTag();
            noble.putString("name", entry.getKey());
            noble.putInt("level", entry.getValue());
            nobleList.add(noble);
        }
        tag.put("nobles", nobleList);

        ListTag citizenList = new ListTag();
        for (String s : citizens) {
            citizenList.add(StringTag.valueOf(s));
        }
        tag.put("citizens", citizenList);

        ListTag claimList = new ListTag();
        for (ChunkInfo info : claimed) {
            claimList.add(info.toNBT());
        }
        tag.put("claimed", claimList);

        tag.putString("name", name);

        tag.putInt("type", type);

        return tag;
    }

    public static FactionCore load(CompoundTag tag, HolderLookup.Provider provider) {
        FactionCore faction = new FactionCore(tag.getString("leader"), tag.getInt("id"), tag.getInt("type"));

        faction.level = tag.getInt("level");

        ListTag coLeaderList = tag.getList("co_leaders", Tag.TAG_STRING);
        for (Tag t : coLeaderList) {
            faction.coLeaders.add(t.getAsString());
        }

        ListTag nobleList = tag.getList("nobles", Tag.TAG_COMPOUND);
        for (Tag t : nobleList) {
            CompoundTag noble = (CompoundTag) t;
            faction.nobles.put(
                    noble.getString("name"),
                    noble.getInt("level")
            );
        }

        ListTag citizenList = tag.getList("citizens", Tag.TAG_STRING);
        for (Tag t : citizenList) {
            faction.citizens.add(t.getAsString());
        }

        ListTag claimList = tag.getList("claimed", Tag.TAG_COMPOUND);
        for (Tag t : claimList) {
            faction.claimed.add(ChunkInfo.fromNBT((CompoundTag) t));
        }

        faction.name = tag.getString("name");

        return faction;
    }
}


// 0/-1 - not claimed/error
// 1 - claimed, citizen
// 2 - noble 1
// ...
// 7 - noble 6
// 8 - co-leader
// 9 - leader
record ChunkInfo(ChunkPos pos, int accessLevel){
    public CompoundTag toNBT(){
        var tag = new CompoundTag();
        tag.putInt("access_level", accessLevel);

        tag.putInt("x", pos.x);
        tag.putInt("z", pos.z);

        return tag;
    }

    public static ChunkInfo fromNBT(CompoundTag tag){
        int level = tag.getInt("access_level");
        int x = tag.getInt("x");
        int z = tag.getInt("z");

        return new ChunkInfo(new ChunkPos(x, z), level);
    }
}