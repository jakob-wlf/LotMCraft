package de.jakob.lotm.addons.factions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.querz.mca.Chunk;

import java.util.*;

public class FactionCore extends SavedData {
    private int level;
    private String leader;
    private List<String> coLeaders;
    private Map<String, Integer> nobles;
    private List<String> citizens;
    private Set<ChunkInfo> claimed;

    public FactionCore(String leader){
        level = 0;
        this.leader = leader;
        coLeaders = new LinkedList<>();
        nobles = new HashMap<>();
        citizens = new LinkedList<>();
        claimed = new HashSet<>();
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

    public void setLevel(int level){
        this.level = level;
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

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        return null;
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