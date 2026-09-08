package de.jakob.lotm.addons.anchoring;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class AnchoringCore {
    private int avatarAmount = 0;
    private Set<UUID> avatarIds = new HashSet<>();

    private int anchoring = 0;
    private int inUse = 0;

    public static int getNeededAnchoringPerSeq(int seq){
        return switch (seq){
            case 2 -> 12;
            case 1 -> 34;
            case 0 -> 68;
            default -> 0;
        };
    }

    public static int getAnchoringValueForSeq(int seq){
        return switch (seq){
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 1;
            case 3 -> 2;
            case 4 -> 2;
            case 5 -> 4;
            case 6 -> 4;
            case 7 -> 5;
            case 8 -> 5;
            case 9 -> 5;
            case 10 -> 6;
            default -> 0;
        };
    }

    public static float getSanityLossBase(int seq){
        return switch (seq){
            case 0 -> 0.3f;
            case 1 -> 0.2f;
            case 2 -> 0.1f;
            default -> 0f;
        };
    }

    public static int getAnchoringValueForAvatar(){
        return 1;
    }

    public static int getAnchoringOnNobleLevel(int level){
        return switch (level){
          case 9 -> 1;
          case 8 -> 2;
          case 7 -> 3;
          case 6 -> 4;
          default -> 0;
        };
    }

    public static int getMinimalNobleLevel(){
        return 6; //has to be the same as lowest of getAnchoringOnNobleLevel
    }

    public int getInUse(){return inUse;}
    public int getAnchoring(){return anchoring;}
    public int getAvatarAmount(){return avatarAmount;}

    public void addAvatar(UUID id){
        if(avatarIds.contains(id)) return;

        avatarIds.add(id);
        avatarAmount++;

        anchoring += getAnchoringValueForAvatar();
    }

    public void recalculateAvatars(ServerLevel level){
        Set<UUID> buff = new HashSet<>();

        for(var obj : avatarIds){
            var target = level.getEntity(obj);

            if(target != null){
                buff.add(obj);
            }
        }

        int difference = avatarIds.size() - buff.size();
        if(difference > 0){
            anchoring -= difference;
        }

        avatarAmount = buff.size();
        avatarIds = buff;
    }

    public void addTotalAnchoring(int amount){
        anchoring += amount;
        inUse += amount;
    }

    public void removeTotalAnchoring(int amount){
        if(anchoring < amount) amount = anchoring;

        if(anchoring == 0) return;

        anchoring -= amount;
        inUse -= amount;
    }

    public void setAnchoring(int value){
        anchoring = value;
        inUse = value;
    }

    public String getInfo(int seq){
        return "Anchoring status: " + inUse + "/" + getNeededAnchoringPerSeq(seq)
                + " | Avatars: " + avatarAmount
                + (seq == 0 ? "" : "\nNext sequence will need: " + getNeededAnchoringPerSeq(seq-1))
                + "\n";
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("avatar_amount", avatarAmount);

        ListTag avatarIdList = new ListTag();
        for (UUID uuid : avatarIds) {
            avatarIdList.add(NbtUtils.createUUID(uuid));
        }
        tag.put("avatar_ids", avatarIdList);

        tag.putInt("anchoring", anchoring);
        tag.putInt("in_use", inUse);

        return tag;
    }

    public static AnchoringCore fromNbt(CompoundTag tag) {
        var obj = new AnchoringCore();
        obj.avatarAmount = tag.getInt("avatar_amount");

        ListTag avatarIdList = tag.getList("avatar_ids", Tag.TAG_INT_ARRAY);

        obj.avatarIds.clear();
        for (Tag uuidTag : avatarIdList) {
            obj.avatarIds.add(NbtUtils.loadUUID(uuidTag));
        }

        obj.anchoring = tag.getInt("anchoring");
        obj.inUse = tag.getInt("in_use");

        return obj;
    }
}


