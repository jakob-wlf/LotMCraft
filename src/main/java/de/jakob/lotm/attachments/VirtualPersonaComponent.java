package de.jakob.lotm.attachments;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class VirtualPersonaComponent {
    private List<VirtualPersona> affectedBy = new LinkedList<>();
    private List<UUID> affects = new LinkedList<>();

    private int ownPersonasOnSelf = 0;
    private float maxHealth = 0;
    private float health = 0;

    public boolean outOfSlots(int seq){
        return affects.size() + ownPersonasOnSelf >= getMaxPerSeq(seq);
    }

    public boolean hasOnSelf(){
        return ownPersonasOnSelf > 0;
    }

    public void placeBy(ServerPlayer player, ServerPlayer victim){
        var component = player.getData(ModAttachments.VIRTUAL_PERSONAS.get());
        int seq = BeyonderData.getSequence(player);

        if(component.outOfSlots(seq)) return;
        if(component.ownPersonasOnSelf == 0) return;

        float health = getMaxHealthPerSeq(seq);
        var persona = new VirtualPersona(player.getUUID(), health, health, seq);

        if(!component.affectedBy.stream().filter(obj
                -> obj.owner.equals(player.getUUID())).toList().isEmpty())
            return;

        affectedBy.add(persona);

        component.ownPersonasOnSelf--;
        component.affects.add(victim.getUUID());
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

    public void onJoin(){
        var buff = new LinkedList<VirtualPersona>();

        for(var obj : affectedBy){
            var seq = BeyonderData.playerMap.get(obj.owner).get().sequence();
            if(seq > obj.seq)
                buff.add(obj);
        }

        affectedBy.removeAll(buff);
    }

    public void removeAffectedBy(UUID owner){
        affectedBy.removeIf(obj -> {return obj.owner.equals(owner);});
    }

    public void onDeath(ServerLevel level, UUID owner){
        if(level.getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)){
            for(var obj : affects){
                level.getPlayerByUUID(obj).getData(ModAttachments.VIRTUAL_PERSONAS.get()).removeAffectedBy(owner);
            }

            affects.clear();
        }
    }

    public static int getMaxPerSeq(int seq){
        return switch (seq){
            case 4 -> 13;
            case 3 -> 30;
            case 2 -> 150;
            case 1 -> 250;
            case 0 -> 500;
            case -1 -> 1000;
            default -> 0;
        };
    }

    public static float getMaxHealthPerSeq(int seq){
        return switch (seq){
            case 4 -> 30;
            case 3 -> 50;
            case 2 -> 100;
            case 1 -> 175;
            case 0 -> 300;
            case -1 -> 100000;
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
                        component.affects.add(UUID.fromString(tag1.getAsString()));
                    }

                    component.ownPersonasOnSelf = tag.getInt("own");
                    component.health = tag.getFloat("health");
                    component.maxHealth = tag.getFloat("max_health");

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
                    for (UUID uuid : component.affects) {
                        affectsList.add(StringTag.valueOf(uuid.toString()));
                    }
                    tag.put("affects", affectsList);

                    tag.putInt("own", component.ownPersonasOnSelf);
                    tag.putFloat("health", component.health);
                    tag.putFloat("max_health", component.maxHealth);

                    return tag;
                }
            };
}

class VirtualPersona{
    public UUID owner;
    public float health;
    public float maxHealth;
    public int seq;

    public VirtualPersona(UUID id, float h, float m, int s){
      owner = id;
      health = h;
      maxHealth = m;
      seq = s;
    }

    public CompoundTag toNbt(){
        var tag = new CompoundTag();

        tag.putUUID("owner", owner);
        tag.putFloat("health", health);
        tag.putFloat("max_health", maxHealth);
        tag.putInt("seq", seq);

        return tag;
    }

    public static VirtualPersona fromNbt(CompoundTag tag){
        return new VirtualPersona(
                tag.getUUID("owner"),
                tag.getFloat("health"),
                tag.getFloat("max_health"),
                tag.getInt("seq")
        );
    }
}