package de.jakob.lotm.attachments;

import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;

public class BeyonderComponent implements INBTSerializable<CompoundTag> {

    private int sequence = de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ;
    private String pathway = "none";
    private String[] pathwayHistory = new String[10];
    private ArrayList<Characteristic> charList = new ArrayList<Characteristic>();
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
        for(int i = sequence; i < 10; i++) {
            if (pathwayHistory[i] == null || pathwayHistory[i].isEmpty()) {
                pathwayHistory[i] = pathway;
            }
        }
        return pathwayHistory;
    }

    public void setPathwayHistory(String[] pathwayHistory) {
        this.pathwayHistory = pathwayHistory;
    }

    public ArrayList<Characteristic> getCharacteristicList(){
        return charList;
    }


    public void setCharacteristic(int stackValue, int sequence, String pathway) {
        if(sequence <= 9 && sequence >= 0) {
            Characteristic tmp = this.charList.stream()
                    .filter(characteristic -> characteristic.sequence() == sequence && characteristic.pathway().equals(pathway))
                    .findAny()
                    .orElse(null);

            if (tmp == null) {
                if (stackValue > 0) {
                    this.charList.add(new Characteristic(pathway, stackValue, sequence));
                }
            } else {
                if (stackValue <= 0) {
                    this.charList.remove(tmp);
                } else {
                    tmp.setStack(stackValue);
                }
            }
            syncHighest();
        }
    }

    public void syncHighest() {
        if (charList.isEmpty()) {
            this.pathway = "none";
            this.sequence = de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ;
            return;
        }

        int min = charList.stream().mapToInt(Characteristic::sequence).min().orElse(de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ);
        this.sequence = min;

        boolean currentMatches = charList.stream().anyMatch(c -> c.sequence() == min && c.pathway().equals(this.pathway));
        if (!currentMatches) {
            this.pathway = charList.stream()
                    .filter(c -> c.sequence() == min)
                    .map(Characteristic::pathway)
                    .findFirst()
                    .orElse("none");
        }
    }

    public void clearCharacteristics() {
        this.charList = new ArrayList<>();
        syncHighest();
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

        ListTag characteristicListTag = new ListTag();
        for(Characteristic characteristic : charList){
            characteristicListTag.add(characteristic.toNBT(provider));
        }
        tag.put("characteristicList", characteristicListTag);

        tag.putFloat("spirituality", spirituality);
        tag.putFloat("digestionProgress", digestionProgress);
        tag.putBoolean("isGriefingEnabled", isGriefingEnabled);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        this.pathway = compoundTag.contains("pathway") ? compoundTag.getString("pathway") : "none";
        if (this.pathway.isEmpty()) this.pathway = "none";
        this.sequence = compoundTag.contains("sequence") ? compoundTag.getInt("sequence") : de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ;

        ListTag pathwayHistoryTag = compoundTag.getList("pathwayHistory", 8); // 8 is the ID for StringTag
        // Ensure a fixed-size history array of length 10 and don't trust the serialized size.
        this.pathwayHistory = new String[10];
        for (int i = 0; i < Math.min(10, pathwayHistoryTag.size()); i++) {
            String pathwayEntry = pathwayHistoryTag.getString(i);
            this.pathwayHistory[i] = pathwayEntry.isEmpty() ? null : pathwayEntry;
        }
        // Fill remaining slots with nulls explicitly (already null by default, but kept for clarity)
        for (int i = pathwayHistoryTag.size(); i < 10; i++) {
            this.pathwayHistory[i] = null;
        }

        this.charList = new ArrayList<>();
        if (compoundTag.contains("characteristicList", Tag.TAG_LIST)) {
            ListTag characteristicListTag = compoundTag.getList("characteristicList", Tag.TAG_COMPOUND);
            for (int i = 0; i < characteristicListTag.size(); i++) {
                this.charList.add(Characteristic.fromNBT(characteristicListTag.getCompound(i), provider));
            }
        } else {
            // Check both possible legacy keys
            String legacyKey = compoundTag.contains("characteristicStack") ? "characteristicStack" : (compoundTag.contains("beyonder_map_char_stack") ? "beyonder_map_char_stack" : null);

            if (legacyKey != null) {
                Tag stackTag = compoundTag.get(legacyKey);
                de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: found legacy key {} with tag class {}", legacyKey, stackTag.getClass().getSimpleName());
                if (stackTag instanceof IntArrayTag intArrayTag) {
                    int[] array = intArrayTag.getAsIntArray();
                    de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: migrating IntArrayTag {}", java.util.Arrays.toString(java.util.Arrays.copyOf(array, Math.min(array.length, 10))));
                    for (int i = 0; i < Math.min(array.length, 10); i++) {
                    if (array[i] > 0 && this.sequence <= i) {
                            String charPath = this.pathway.equals("none") ? "placeholder" : this.pathway;
                            // Legacy stored values were (stack - 1) in some versions; migrate to proper stack by +1
                            this.charList.add(new Characteristic(charPath, array[i] + 1, i));
                        }
                    }
                } else if (stackTag instanceof ListTag listTag) {
                    de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: migrating ListTag of size {} and elementType {}", listTag.size(), listTag.getElementType());
                    if (listTag.getElementType() == Tag.TAG_INT) {
                        for (int i = 0; i < Math.min(listTag.size(), 10); i++) {
                            int value = listTag.getInt(i);
                            if (value > 0 && this.sequence <= i) {
                                String charPath = this.pathway.equals("none") ? "placeholder" : this.pathway;
                                // Legacy int lists represent (stack - 1) in some versions; migrate by +1
                                this.charList.add(new Characteristic(charPath, value + 1, i));
                            }
                        }
                    } else if (listTag.getElementType() == Tag.TAG_COMPOUND) {
                        // Some legacy formats stored a list of compounds with {index:int, value:int} entries.
                        // Detect that shape and migrate accordingly (applying +1 like older code did).
                        // Detect per-entry whether this list is index/value pairs or full Characteristic compounds.
                        boolean anyIndexValue = false;
                        for (int j = 0; j < listTag.size(); j++) {
                            CompoundTag cand = listTag.getCompound(j);
                            if (cand.contains("index", Tag.TAG_INT) && cand.contains("value", Tag.TAG_INT)) {
                                anyIndexValue = true;
                                break;
                            }
                        }

                        if (anyIndexValue) {
                            int[] characteristicStack = new int[10];
                            for (int j = 0; j < listTag.size(); j++) {
                                CompoundTag entry = listTag.getCompound(j);
                                // Read index/value robustly across numeric tag types
                                int index = 0;
                                int value = 0;
                                if (entry.contains("index", Tag.TAG_INT)) index = entry.getInt("index");
                                else if (entry.contains("index", Tag.TAG_SHORT)) index = entry.getShort("index");
                                else if (entry.contains("index", Tag.TAG_BYTE)) index = entry.getByte("index");
                                else if (entry.contains("index", Tag.TAG_LONG)) index = (int) entry.getLong("index");

                                if (entry.contains("value", Tag.TAG_INT)) value = entry.getInt("value");
                                else if (entry.contains("value", Tag.TAG_SHORT)) value = entry.getShort("value");
                                else if (entry.contains("value", Tag.TAG_BYTE)) value = entry.getByte("value");
                                else if (entry.contains("value", Tag.TAG_LONG)) value = (int) entry.getLong("value");

                                de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: legacy entry {} -> {} (raw keys {})", index, value, entry.getAllKeys());
                                if (index >= 0 && index < characteristicStack.length) {
                                    characteristicStack[index] = value;
                                    de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: wrote characteristicStack[{}] = {}", index, value);
                                } else {
                                    de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: legacy entry index {} out of bounds", index);
                                }
                            }
                            de.jakob.lotm.LOTMCraft.LOGGER.info("BeyonderComponent.deserializeNBT: migrated characteristicStack {}", java.util.Arrays.toString(characteristicStack));
                            if (this.sequence < de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ) {
                                for (int i = 0; i < 10; i++) {
                                    if (this.sequence <= i) {
                                        String ph = (this.pathwayHistory[i] == null || this.pathwayHistory[i].isEmpty()) ? this.pathway : this.pathwayHistory[i];
                                        this.charList.add(new Characteristic(ph, characteristicStack[i] + 1, i));
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < listTag.size(); i++) {
                                this.charList.add(Characteristic.fromNBT(listTag.getCompound(i), provider));
                            }
                        }
                    }
                }
            }
        }

        // If we have a pathway but no characteristics were found in migration,
        // add at least the base characteristic for the current sequence.
        if (this.charList.isEmpty() && !this.pathway.equals("none") && this.sequence < de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ) {
            this.charList.add(new Characteristic(this.pathway, 1, this.sequence));
        }

        this.spirituality = compoundTag.getFloat("spirituality");
        this.digestionProgress = compoundTag.getFloat("digestionProgress");
        this.isGriefingEnabled = compoundTag.getBoolean("isGriefingEnabled");
        syncHighest();
    }
}
