package de.jakob.lotm.attachments;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.LinkedList;
import java.util.UUID;

public class MentalPlagueComponent {
    private boolean hasMentalPlague = false;
    private int sequence = 10;
    private boolean shouldBeActivated = false;
    private String ownerName = "";
    private boolean weakened = false;
    private int stage = 0;
    private int infected = 0;

    public static final int MAX_STAGE = 10;
    public static final int MAX_INFECTED = 15000;

    public static final String NBT_HAS_PLAGUE = "has_plague";
    public static final String NBT_SEQ = "plague_seq";
    public static final String NBT_SHOULD = "plague_should";
    public static final String NBT_OWNER = "plague_owner";
    public static final String NBT_WEAKENED = "plague_weakened";
    public static final String NBT_STAGE = "plague_stage";
    public static final String NBT_INFECTED = "infected";

    public boolean hasMentalPlague() {
        return hasMentalPlague;
    }

    public void setHasMentalPlague(boolean hasMentalPlague) {
        this.hasMentalPlague = hasMentalPlague;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public boolean shouldBeActivated() {
        return shouldBeActivated;
    }

    public void setShouldBeActivated(boolean shouldBeActivated) {
        this.shouldBeActivated = shouldBeActivated;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void reset(){
        hasMentalPlague = false;
        shouldBeActivated = false;
        sequence = 10;
        ownerName = "";
        weakened = false;
        stage = 0;
    }

    public void place(String name, int seq){
        if(hasMentalPlague){
            if(seq <= sequence){
                ownerName = name;
                shouldBeActivated = false;
                sequence = seq;
            }

            return;
        }

        ownerName = name;
        shouldBeActivated = false;
        sequence = seq;
        hasMentalPlague = true;
        weakened = false;
        stage = 0;
    }

    public void activate(){
        if(hasMentalPlague) shouldBeActivated = true;
    }

    public boolean isOwner(ServerPlayer entity){
        UUID id = BeyonderData.playerMap.getKeyByName(ownerName);
        if(id == null) return true;

        return id.equals(entity.getUUID());
    }

    public boolean isOwner(UUID targetId){
        UUID id = BeyonderData.playerMap.getKeyByName(ownerName);
        if(id == null) return true;

        return id.equals(targetId);
    }


    public static final IAttachmentSerializer<CompoundTag, MentalPlagueComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public MentalPlagueComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new MentalPlagueComponent();

                    component.hasMentalPlague = tag.getBoolean(NBT_HAS_PLAGUE);
                    component.shouldBeActivated = tag.getBoolean(NBT_SHOULD);
                    component.ownerName = tag.getString(NBT_OWNER);
                    component.sequence = tag.getInt(NBT_SEQ);
                    component.weakened = tag.getBoolean(NBT_WEAKENED);
                    component.stage = tag.getInt(NBT_STAGE);
                    component.infected = tag.getInt(NBT_INFECTED);

                    return component;
                }

                @Override
                public CompoundTag write(MentalPlagueComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    tag.putBoolean(NBT_HAS_PLAGUE, component.hasMentalPlague);
                    tag.putBoolean(NBT_SHOULD, component.shouldBeActivated);
                    tag.putInt(NBT_SEQ, component.sequence);
                    tag.putString(NBT_OWNER, component.ownerName);
                    tag.putBoolean(NBT_WEAKENED, component.weakened);
                    tag.putInt(NBT_STAGE, component.stage);
                    tag.putInt(NBT_INFECTED, component.infected);

                    return tag;
                }
            };

    public boolean isWeakened() {
        return weakened;
    }

    public void setWeakened(boolean weakened) {
        this.weakened = weakened;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getInfected() {
        return infected;
    }

    public void setInfected(int infected) {
        this.infected = infected;
    }
}
