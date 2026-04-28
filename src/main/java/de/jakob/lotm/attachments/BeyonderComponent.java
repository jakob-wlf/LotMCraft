package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.List;

public class BeyonderComponent implements INBTSerializable<CompoundTag> {

    private int sequence = 10;
    private String pathway = "none";
    private String[] pathwayHistory = new String[10];
    private int[] characteristicStack = new int[11];
    private float spirituality = 0;
    private float digestionProgress = 0;
    private boolean isGriefingEnabled = true;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    public String[] getPathwayHistory() {
        for(int i = 0; i < sequence; i++) {
            pathwayHistory[i] = null;
        }
        return pathwayHistory;
    }

    public void setPathwayHistory(String[] pathwayHistory) {
        this.pathwayHistory = pathwayHistory;
    }

    public int[] getCharacteristicStack() {
        return characteristicStack;
    }

    public void setCharacteristicStack(int characteristicStack, int sequence) {
        this.characteristicStack[sequence] = characteristicStack;
    }

    public void clearCharacteristicStack() {
        this.characteristicStack = new int[11];
    }

    public float getSpirituality() {
        return spirituality;
    }

    public void setSpirituality(float spirituality) {
        this.spirituality = spirituality;
    }

    public float getDigestionProgress() {
        return digestionProgress;
    }

    public void setDigestionProgress(float digestionProgress) {
        this.digestionProgress = digestionProgress;
    }

    public boolean isGriefingEnabled() {
        return isGriefingEnabled;
    }

    public void setGriefingEnabled(boolean griefingEnabled) {
        isGriefingEnabled = griefingEnabled;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("sequence", sequence);
        tag.putString("pathway", pathway);

        ListTag pathwayHistoryTag = new ListTag();
        for (String pathwayEntry : pathwayHistory) {
            if(pathwayEntry == null) {
                pathwayHistoryTag.add(StringTag.valueOf(""));
                continue;
            }
            pathwayHistoryTag.add(StringTag.valueOf(pathwayEntry));
        }
        tag.put("pathwayHistory", pathwayHistoryTag);

        ListTag characteristicStackTag = new ListTag();
        for (int i = 0; i < characteristicStack.length; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("index", i);
            entry.putInt("value", characteristicStack[i]);
            characteristicStackTag.add(entry);
        }
        tag.put("characteristicStack", characteristicStackTag);

        tag.putFloat("spirituality", spirituality);
        tag.putFloat("digestionProgress", digestionProgress);
        tag.putBoolean("isGriefingEnabled", isGriefingEnabled);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        this.sequence = compoundTag.getInt("sequence");
        this.pathway = compoundTag.getString("pathway");

        ListTag pathwayHistoryTag = compoundTag.getList("pathwayHistory", 8); // 8 is the ID for StringTag
        this.pathwayHistory = new String[pathwayHistoryTag.size()];
        for (int i = 0; i < pathwayHistoryTag.size(); i++) {
            String pathwayEntry = pathwayHistoryTag.getString(i);
            if(pathwayEntry.isEmpty()) {
                this.pathwayHistory[i] = null;
            } else {
                this.pathwayHistory[i] = pathwayEntry;
            }
        }

        this.characteristicStack = new int[11];
        ListTag characteristicStackTag = compoundTag.getList("characteristicStack", 10); // 10 = CompoundTag
        for (int i = 0; i < characteristicStackTag.size(); i++) {
            CompoundTag entry = characteristicStackTag.getCompound(i);
            int index = entry.getInt("index");
            int value = entry.getInt("value");
            this.characteristicStack[index] = value;
        }

        this.spirituality = compoundTag.getFloat("spirituality");
        this.digestionProgress = compoundTag.getFloat("digestionProgress");
        this.isGriefingEnabled = compoundTag.getBoolean("isGriefingEnabled");
    }
}
