package de.jakob.lotm.addons.factions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

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
    private Map<Integer, Integer> bank;

    private Set<Integer> hasPermission; // for churches only
    private ChunkPos coreClaim; //for nation only

    public FactionCore(String leader, int id, int type){
        this.id = id;
        level = 0;
        this.leader = leader;
        coLeaders = new LinkedList<>();
        nobles = new HashMap<>();
        citizens = new LinkedList<>();
        claimed = new HashSet<>();
        this.type = type;
        hasPermission = new HashSet<>();
        bank = new HashMap<>();

        coreClaim = new ChunkPos(0, 0);

        bank.put(1, 0);
        bank.put(2, 0);
    }

    public static int getNoblesAmountPerLevel(int level){
        return switch (level){
          case 0 -> 0;
          case 1 -> 1;
          case 2 -> 2;
          case 3 -> 4;
          default -> 0;
        };
    }

    public static int getCitizensAmountPerLevel(int level){
        return switch (level){
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 9;
            case 3 -> 13;
            default -> 0;
        };
    }

    public static int getCoLeadersAmountPerLevel(int level){
        return switch (level){
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 1;
            case 3 -> 2;
            default -> 0;
        };
    }

    public static int getClaimsPerLevelNation(int level){
        return switch (level){
            case 0 -> 1;
            case 1 -> 20;
            case 2 -> 60;
            case 3 -> 110;
            default -> 0;
        };
    }

    public static int getClaimsPerLevelChurch(int level){
        return switch (level){
            case 0 -> 1;
            case 1 -> 15;
            case 2 -> 40;
            case 3 -> 80;
            default -> 0;
        };
    }

    public static int getLevelUpPeople(int level){
        return switch (level){
            case 0 -> 2;
            case 1 -> 6;
            case 2 -> 10;
            default -> 0;
        };
    }

    public static int getLevelUpChunks(int level){
        return switch (level){
            case 0 -> 1;
            case 1 -> 15;
            case 2 -> 40;
            default -> 0;
        };
    }

    public static int getLevelUpPounds(int level){
        return switch (level){
          case 0 -> 250;
          case 1 -> 10000;
          case 2 -> 25000;
          default -> 0;
        };
    }

    public static int getLevelUpSoli(int level){
        return switch (level){
          case 0 -> 100;
          case 1 -> 5000;
          case 2 -> 15000;
          default -> 0;
        };
    }

    public static int getMaxLevel(){
        return 3;
    }

    public static int getMinSeqNation(){
        return 8;
    }

    public static int getMinSeqChurch(){
        return 6;
    }

    public boolean isOutOfSlotsCitizens(){
        return citizens.size() >= getCitizensAmountPerLevel(level);
    }

    public boolean isOutOfSlotsCoLeaders(){
        return coLeaders.size() >= getCoLeadersAmountPerLevel(level);
    }

    public boolean isOutOfSlotsNobles(){
        return nobles.size() >= getNoblesAmountPerLevel(level);
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

    public void setCore(ChunkPos pos){
        this.coreClaim = pos;
    }

    public ChunkPos getCore(){
        return coreClaim;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setLeader(String name){
        leader = name;
    }

    public void addCoLeader(String name){
        coLeaders.add(name);
    }

    public void addNoble(String name, int level){
        nobles.put(name, level);
    }

    public void addCitizen(String name){
        citizens.add(name);
    }

    public boolean isClaimed(ChunkPos pos){
        for (var obj : claimed){
            if(obj.pos().equals(pos))
                return true;
        }

        return false;
    }

    public void addPermission(Integer id){
        hasPermission.add(id);
    }

    public void removePermission(Integer id){
        hasPermission.remove(id);
    }

    public boolean hasPermission(Integer id){
        return hasPermission.contains(id);
    }

    public Set<ChunkPos> getClaimed(){
        return claimed.stream().map(ChunkInfo::pos).collect(Collectors.toSet());
    }

    public void removeClaimed(Set<ChunkPos> set){
        claimed.removeIf(obj -> set.contains(obj.pos()));
    }

    public void unclaim(ChunkPos pos){
        claimed.removeIf(obj -> obj.pos().equals(pos));
    }

    public int getClaimLevel(ChunkPos pos){
        if(!isClaimed(pos)) return -1;

        return claimed.stream().filter(i -> i.pos().equals(pos)).findFirst().get().accessLevel();
    }

    private ChunkInfo getClaim(ChunkPos pos){
        return claimed.stream().filter(i -> i.pos().equals(pos)).findFirst().get();
    }

    public List<String> getAllCitizens(){
        return citizens;
    }

    public Map<String, Integer> getAllNobles(){
        return nobles;
    }

    public List<String> getAllCoLeaders(){
        return coLeaders;
    }

    public void addClaim(ChunkPos pos, int level){
        if(isClaimed(pos)){
            var obj = getClaim(pos);
            if(obj.accessLevel() == level) return;

            claimed.remove(obj);
        }

        if(claimed.isEmpty()) {
            coreClaim = pos;
            level = 9;
        }

        claimed.add(new ChunkInfo(pos, level));
    }

    public void removeClaim(ChunkPos pos){
        if(!isClaimed(pos))
            return;

        claimed.remove(getClaim(pos));
    }

    public void setSoli(int amount){
        bank.put(1, amount);
    }

    public void setPound(int amount){
        bank.put(2, amount);
    }

    public int getSoli(){
        return bank.get(1);
    }

    public int getPound(){
        return bank.get(2);
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
        BlockPos center = new BlockPos(
                coreClaim.getMiddleBlockX(),
                0,
                coreClaim.getMiddleBlockZ()
        );

        return "ID: " + id
                + "\nLeader: " + leader
                + "\nName: " + name
                + "\nLevel: " + level
                + "\nType: " + (type == 1 ? "Nation" : "Church")
                + "\nCore: " + coreClaim.toString() + " - " + center.toString()
                + "\nClaims: " + claimed.size() + "/" + (type == 1? getClaimsPerLevelNation(level) : getClaimsPerLevelChurch(level))
                + "\nBank: " + bank.get(1) + " soli, " + bank.get(2) + " pounds"
                + "\nTotal people: " + getAllPlayers().size()
                + "\nCo-leaders: " + convertCoLeaders()
                + "\nNobles: " + convertNobles()
                + "\nCitizens: " + convertCitizens()
                ;
    }

    private String convertCoLeaders(){
        StringBuilder builder = new StringBuilder(coLeaders.size() +"/" + getCoLeadersAmountPerLevel(level) + "\n");
        for(var obj : coLeaders){
            builder.append("  ").append(obj).append('\n');
        }
        return builder.toString();
    }

    private String convertNobles(){
        StringBuilder builder = new StringBuilder(nobles.size() + "/" + getNoblesAmountPerLevel(level) + "\n");
        for(var obj : nobles.entrySet()){
            builder.append("  ").append(obj.getKey()).append(" -- ").append(obj.getValue()).append('\n');
        }
        return builder.toString();
    }

    private String convertCitizens(){
        StringBuilder builder = new StringBuilder(citizens.size() + "/" + getCitizensAmountPerLevel(level) + "\n");
        for(var obj : citizens){
            builder.append("  ").append(obj).append('\n');
        }
        return builder.toString();
    }

    public String getShortInfo(){
        return "ID: " + id + " --Leader: " + leader + " --Name: " + name + " --Type: " + (type == 1 ? "Nation" : "Church");
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

    public List<String> getAllPlayers(){
        List<String> result = new LinkedList<>();
        result.add(leader);
        result.addAll(coLeaders);
        result.addAll(nobles.keySet());
        result.addAll(citizens);
        return result;
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
        for (var entry : nobles.entrySet()) {
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

        tag.putIntArray("HasPermission", hasPermission.stream().mapToInt(Integer::intValue).toArray());

        tag.putInt("core_x", coreClaim.x);
        tag.putInt("core_z", coreClaim.z);

        ListTag bankList = new ListTag();
        for (var entry : bank.entrySet()) {
            CompoundTag obj = new CompoundTag();
            obj.putInt("type", entry.getKey());
            obj.putInt("amount", entry.getValue());
            bankList.add(obj);
        }
        tag.put("bank", bankList);

        return tag;
    }

    public static FactionCore load(CompoundTag tag, HolderLookup.Provider provider) {
        FactionCore faction = new FactionCore(tag.getString("leader"), tag.getInt("id"), tag.getInt("type"));

        faction.level = tag.getInt("level");

        ListTag bankList = tag.getList("bank", Tag.TAG_COMPOUND);
        for (Tag t : bankList) {
            CompoundTag obj = (CompoundTag) t;
            faction.bank.put(
                    obj.getInt("type"),
                    obj.getInt("amount")
            );
        }

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

        faction.hasPermission = Arrays.stream(tag.getIntArray("HasPermission"))
                .boxed()
                .collect(Collectors.toSet());

        int x = tag.getInt("core_x");
        int z = tag.getInt("core_z");

        faction.coreClaim = new ChunkPos(x, z);

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