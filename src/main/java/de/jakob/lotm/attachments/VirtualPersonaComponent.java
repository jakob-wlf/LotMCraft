package de.jakob.lotm.attachments;

import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class VirtualPersonaComponent {
    private List<VirtualPersona> affectedBy = new LinkedList<>();
    private List<String> affects = new LinkedList<>();
    private List<UUID> avatars = new LinkedList<>();

    private int ownPersonasOnSelf = 0;
    private float maxHealth = 0;
    private float health = 0;

    public boolean outOfSlots(int seq){
        return affects.size() + ownPersonasOnSelf >= getMaxPerSeq(seq);
    }

    public boolean hasOnSelf(){
        return ownPersonasOnSelf > 0;
    }

    public int getUsedSlots(){
        return affects.size() + ownPersonasOnSelf + avatars.size();
    }

    public boolean affects(String id){
        return affects.contains(id);
    }

    public boolean isAffectedBy(String name){
        return !affectedBy.stream().filter(obj -> obj.owner.equals(name)).toList().isEmpty();
    }

    public void createAvatar(UUID id){
        if(!hasOnSelf()) return;

        ownPersonasOnSelf--;
        avatars.add(id);
    }

    public void removeAvatar(UUID id){
        avatars.remove(id);
    }

    public void placeBy(ServerPlayer player, ServerPlayer victim){
        var component = player.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        int seq = BeyonderData.getSequence(player);

        if(component.ownPersonasOnSelf == 0) return;

        float health = getMaxHealthPerSeq(seq);
        var persona = new VirtualPersona(player.getName().getString(), health, health, seq);

        if(!affectedBy.stream().filter(obj
                -> obj.owner.equals(player.getName().getString())).toList().isEmpty())
            return;

        affectedBy.add(persona);

        component.ownPersonasOnSelf--;
        component.affects.add(victim.getName().getString());
    }

    public void create(int seq){
        if(outOfSlots(seq)) return;
        float health = getMaxHealthPerSeq(seq);

        ownPersonasOnSelf++;
        maxHealth = health;
        this.health = health;
    }

    public void heal(ServerPlayer player){
        if(player.tickCount % 1500 == 0){
            for (var obj : affectedBy){
                if(obj.health + 1 <= obj.maxHealth)
                    obj.health++;
            }

            if(health + 1 <= maxHealth)
                health++;
        }
    }

    public float block(float amount) {
        if (ownPersonasOnSelf <= 0 || amount >= 0) {
            return amount;
        }

        float remainingHealth = health + amount;

        if (remainingHealth > 0) {
            health = remainingHealth;

            return 0;
        }

        ownPersonasOnSelf--;
        health = maxHealth;

        float leftoverDamage = remainingHealth;

        return block(leftoverDamage);
    }

    public void onJoin(ServerLevel level, String name){
        var buff = new LinkedList<VirtualPersona>();

        for(var obj : affectedBy){
            var seq = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(obj.owner)).get().sequence();
            if(seq > obj.seq)
                buff.add(obj);
        }

        for(var obj : affects){
            var target = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(obj));
            if(target != null){
                var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());

                if(component.isAffectedBy(name)){
                    removeAffects(target.getName().getString(), name, level);
                }
            }
        }

        var buff2 = new LinkedList<UUID>();
        for(var obj : avatars){
            var target = level.getEntity(obj);
            if(target == null)
                buff2.add(obj);
        }
        avatars.removeAll(buff2);

        affectedBy.removeAll(buff);
    }

    public List<String> getAffects(){
        return new LinkedList<>(affects);
    }

    public String getGeneralInfo(int seq){
        return new String(
                "Slots: " + getUsedSlots() + "/" + getMaxPerSeq(seq)
                + "\nOn self: " + ownPersonasOnSelf
                + "\nHealth: " + health + "/" + maxHealth
        );
    }

    public List<UUID> getAvatars(){
        return avatars;
    }

    public int getAvatarsSive(){
        return avatars.size();
    }

    public List<String> getAffectedBy(int seq){
        return new LinkedList<>(affectedBy).stream().filter(obj -> obj.seq >= seq).map(obj -> obj.owner).toList();
    }

    private void removeAffectedBy(String owner){
        affectedBy.removeIf(obj -> obj.owner.equals(owner));
    }

    public void removeAffects(String targetName, String ownerName, ServerLevel level){
        if(!affects(targetName)) return;

        var target = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(targetName));
        if(target == null) return;

        affects.remove(targetName);
        ownPersonasOnSelf++;

        var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        component.removeAffectedBy(ownerName);
    }

    public void damageAffectedBy(float amount, ServerLevel level, String name, int seq){
        var buff = new LinkedList<VirtualPersona>();

        for(var obj : affectedBy){
            if(seq > obj.seq) continue;

            obj.health -= amount;
            if(obj.health <= 0)
                buff.add(obj);
        }

        for(var obj : buff){
            var target = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(obj.owner));
            if(target != null){
                var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());
                component.removeAffects(name, target.getName().getString(), level);
            }
        }

        affectedBy.removeAll(buff);
    }

    public void onDeath(ServerLevel level, String owner){
        if(level.getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)){
            for(var obj : affects){
                var target = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(obj));
                if(target != null){
                    var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());
                    component.removeAffectedBy(owner);
                    }
            }

            affects.clear();
        }
    }

    public void clean(ServerLevel level, String owner){
        for(var obj : affects){
            var target = level.getPlayerByUUID(BeyonderData.playerMap.getKeyByName(obj));
            if(target != null){
                var component = target.getData(ModAttachments.VIRTUAL_PERSONAS.get());
                component.removeAffectedBy(owner);
            }
        }

        affects.clear();

        List<UUID> toKill = new ArrayList<>(avatars);

        for (UUID id : toKill) {
            var entity = level.getEntity(id);
            if (entity != null)
                entity.kill();
        }

        avatars.clear();

        ownPersonasOnSelf = 0;
        health = 0;
    }

    public static int getMaxPerSeq(int seq){
        return switch (seq){
            case 4 -> 5;
            case 3 -> 12;
            case 2 -> 32;
            case 1 -> 60;
            case 0 -> 100;
            case -1 -> 1000;
            default -> 0;
        };
    }

    public static float getMaxHealthPerSeq(int seq){
        return switch (seq){
            case 4 -> 1;
            case 3 -> 2;
            case 2 -> 3;
            case 1 -> 6;
            case 0 -> 12;
            case -1 -> 50;
            default -> 0;
        };
    }

    public static final IAttachmentSerializer<CompoundTag, VirtualPersonaComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public VirtualPersonaComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new VirtualPersonaComponent();

                    ListTag affectedByList = tag.getList("affectedBy", Tag.TAG_COMPOUND);

                    for (Tag tag1 : affectedByList) {
                        component.affectedBy.add(VirtualPersona.fromNbt((CompoundTag) tag1));
                    }

                    ListTag affectsList = tag.getList("affects", Tag.TAG_STRING);

                    for (Tag tag1 : affectsList) {
                        component.affects.add(tag1.getAsString());
                    }

                    component.ownPersonasOnSelf = tag.getInt("own");
                    component.health = tag.getFloat("health");
                    component.maxHealth = tag.getFloat("max_health");

                    var listTag = tag.getList("avatars", Tag.TAG_STRING);
                    List<UUID> uuids = new ArrayList<>();

                    for (int i = 0; i < listTag.size(); i++) {
                        uuids.add(UUID.fromString(listTag.getString(i)));
                    }

                    component.avatars = uuids;

                    return component;
                }

                @Override
                public CompoundTag write(VirtualPersonaComponent component, HolderLookup.Provider lookup) {
                    var tag = new CompoundTag();

                    ListTag affectedByList = new ListTag();
                    for (VirtualPersona persona : component.affectedBy) {
                        affectedByList.add(persona.toNbt());
                    }
                    tag.put("affectedBy", affectedByList);

                    ListTag affectsList = new ListTag();
                    for (var obj : component.affects) {
                        affectsList.add(StringTag.valueOf(obj));
                    }
                    tag.put("affects", affectsList);

                    tag.putInt("own", component.ownPersonasOnSelf);
                    tag.putFloat("health", component.health);
                    tag.putFloat("max_health", component.maxHealth);

                    ListTag listTag = new ListTag();

                    for (UUID uuid : component.avatars) {
                        listTag.add(StringTag.valueOf(uuid.toString()));
                    }
                    tag.put("avatars", listTag);

                    return tag;
                }
            };
}

class VirtualPersona{
    public String owner;
    public float health;
    public float maxHealth;
    public int seq;

    public VirtualPersona(String id, float h, float m, int s){
      owner = id;
      health = h;
      maxHealth = m;
      seq = s;
    }

    public CompoundTag toNbt(){
        var tag = new CompoundTag();

        tag.putString("owner", owner);
        tag.putFloat("health", health);
        tag.putFloat("max_health", maxHealth);
        tag.putInt("seq", seq);

        return tag;
    }

    public static VirtualPersona fromNbt(CompoundTag tag){
        return new VirtualPersona(
                tag.getString("owner"),
                tag.getFloat("health"),
                tag.getFloat("max_health"),
                tag.getInt("seq")
        );
    }
}