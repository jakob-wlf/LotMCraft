package de.jakob.lotm.util.playerMap;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.StoredDataBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.LinkedList;

public record StoredData(String pathway, Integer sequence, HonorificName honorificName,
                         String trueName,
                         Boolean modified, Vec3 lastPosition,
                         int[] charStack,
                         String[] pathwayHistory,
                         String uniqueness, //none if no uniqueness :)
                         LinkedList<Prophecy> prophecies,
                         String claimedSefirot
) {

    public static final String NBT_PATHWAY         = "beyonder_map_pathway";
    public static final String NBT_SEQUENCE        = "beyonder_map_sequence";
    public static final String NBT_HONORIFIC_NAME  = "beyonder_map_honorific_name";
    public static final String NBT_TRUE_NAME       = "beyonder_map_true_name";
    public static final String NBT_MODIFIED        = "beyonder_map_modified";
    public static final String NBT_CHAR_STACK      = "beyonder_map_char_stack";
    public static final String NBT_PATHWAY_HISTORY = "beyonder_map_pathway_history";
    public static final String NBT_PROPHECIES      = "beyonder_map_prophecies";
    public static final String NBT_UNIQUENESS = "beyonder_map_uniqueness";
    public static final String NBT_SEFIROT = "beyonder_map_claimed_sefirot";

    public static final String NBT_LAST_POSITION_X = "beyonder_map_last_position_x";
    public static final String NBT_LAST_POSITION_Y = "beyonder_map_last_position_y";
    public static final String NBT_LAST_POSITION_Z = "beyonder_map_last_position_z";

    public static final StoredDataBuilder builder = new StoredDataBuilder();

    // ── helpers ──────────────────────────────────────────────────────────────

    public String getShortInfo() {
        return "Path: " + pathway + " -- Seq: " + sequence + " -- TN: " + trueName;
    }

    public String getAllInfo() {
        return "Name: " + trueName
                + "\n--- Path: " + pathway
                + "\n--- Seq: " + sequence
                + "\n--- Honorific Name: " + honorificName.getAllInfo()
                + "\n--- Logout Position: " + (int) lastPosition.x + " " + (int) lastPosition.y + " " + (int) lastPosition.z
                + "\n--- Char stack: " + java.util.Arrays.toString(charStack)
                + "\n--- Pathway history: " + getPathwayHistoryInfo()
                + "\n--- Amount of prophecies: " + prophecies.size()
                + "\n--- Sefirot: " + (claimedSefirot.isEmpty() ? "none" : claimedSefirot)
                + "\n--- Was modified: " + modified
                ;
    }

    private String getPathwayHistoryInfo() {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (int i = 9; i >= 0; i--) {
            String p = pathwayHistory[i];
            if (p != null && !p.isEmpty()) {
                sb.append("\n   Seq ").append(i).append(": ").append(p);
                any = true;
            }
        }
        return any ? sb.toString() : " None";
    }

    // ── regression ───────────────────────────────────────────────────────────

    public StoredData regressSeq() { return regressSeq(true); }

    public StoredData regressSeq(boolean respectCharStack) {
        if (respectCharStack && charStack[sequence] > 0) {
            // Still has stacks — lose one, stay at current sequence
            return builder.copyFrom(this).charStack(charStack[sequence] - 1, sequence).build();
        }

        int newSequence = sequence + 1;
        boolean becomesNonBeyonder = (newSequence == LOTMCraft.NON_BEYONDER_SEQ);
        String sefirot = claimedSefirot;

        // Revert pathway from history if a domain-switch was recorded here
        String regressedPathway;
        if (becomesNonBeyonder) {
            regressedPathway = "none";
            sefirot = "";
        } else {
            String historyEntry = pathwayHistory[newSequence];
            regressedPathway = (historyEntry != null && !historyEntry.isEmpty()) ? historyEntry : pathway;
        }

        // Clear history slots that are no longer valid
        String[] clearedHistory;
        if (becomesNonBeyonder) {
            clearedHistory = new String[10];
        } else {
            clearedHistory = Arrays.copyOf(pathwayHistory, 10);
            for (int i = 0; i <= newSequence; i++) {
                clearedHistory[i] = null;
            }
        }

        return builder
                .copyFrom(this)
                .pathway(regressedPathway)
                .sequence(newSequence)
                .honorificName((newSequence >= 3) ? HonorificName.EMPTY : honorificName)
                .charStack(0, sequence)   // reset stack on regression
                .pathwayHistory(becomesNonBeyonder ? new String[10] : clearedHistory)
                .uniqueness("none")
                .sefirot(sefirot)
                .build();
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_PATHWAY, pathway);
        tag.putInt(NBT_SEQUENCE, sequence);
        tag.put(NBT_HONORIFIC_NAME, honorificName.toNBT());
        tag.putString(NBT_TRUE_NAME, trueName);

        tag.putString(NBT_UNIQUENESS, uniqueness == null || uniqueness.isBlank() ? "none" : uniqueness);
        tag.putBoolean(NBT_MODIFIED, modified);

        tag.putDouble(NBT_LAST_POSITION_X, lastPosition.x());
        tag.putDouble(NBT_LAST_POSITION_Y, lastPosition.y());
        tag.putDouble(NBT_LAST_POSITION_Z, lastPosition.z());

        // single int stack
        ListTag charStackList = new ListTag();
        for (int stackCount : charStack) {
            charStackList.add(IntTag.valueOf(stackCount));
        }
        tag.put(NBT_CHAR_STACK, charStackList);

        ListTag propheciesList = new ListTag();
        for (var prophecy : prophecies) {
            propheciesList.add(prophecy.toNBT(provider));
        }
        tag.put(NBT_PROPHECIES, propheciesList);

        // String[10] pathway history stored as a ListTag of StringTags
        ListTag histList = new ListTag();
        for (String entry : pathwayHistory) {
            histList.add(StringTag.valueOf(entry == null ? "" : entry));
        }
        tag.put(NBT_PATHWAY_HISTORY, histList);

        tag.putString(NBT_SEFIROT, claimedSefirot);

        return tag;
    }

    public static StoredData fromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        String path     = tag.getString(NBT_PATHWAY);
        int    seq      = tag.getInt(NBT_SEQUENCE);
        HonorificName name = HonorificName.fromNBT(tag.getCompound(NBT_HONORIFIC_NAME));
        String trueName = tag.getString(NBT_TRUE_NAME);

        boolean modified = tag.getBoolean(NBT_MODIFIED);
        String uniqueness = tag.contains(NBT_UNIQUENESS) ? tag.getString(NBT_UNIQUENESS) : "none";

        Vec3 lastPos = new Vec3(
                tag.getDouble(NBT_LAST_POSITION_X),
                tag.getDouble(NBT_LAST_POSITION_Y),
                tag.getDouble(NBT_LAST_POSITION_Z));

        int[] charStack = new int[10];
        if (tag.contains(NBT_CHAR_STACK, Tag.TAG_LIST)) {
            ListTag charStackList = tag.getList(NBT_CHAR_STACK, Tag.TAG_INT);
            for (int i = 0; i < Math.min(charStackList.size(), 10); i++) {
                charStack[i] = charStackList.getInt(i);
            }
        }

        String[] history = new String[10];
        if (tag.contains(NBT_PATHWAY_HISTORY, Tag.TAG_LIST)) {
            ListTag histList = tag.getList(NBT_PATHWAY_HISTORY, Tag.TAG_STRING);
            for (int i = 0; i < Math.min(histList.size(), 10); i++) {
                String val = histList.getString(i);
                history[i] = val.isEmpty() ? null : val;
            }
        }

        LinkedList<Prophecy> prophecies = new LinkedList<>();
        if (tag.contains(NBT_PROPHECIES, Tag.TAG_LIST)) {
            ListTag propList = tag.getList(NBT_PROPHECIES, Tag.TAG_COMPOUND);
            for (var obj : propList) {
                if (obj instanceof CompoundTag compound)
                    prophecies.add(Prophecy.fromNBT(compound, provider));
            }
        }

        String sefirot = tag.getString(NBT_SEFIROT);

        return new StoredData(path, seq, name, trueName, modified, lastPos,
                charStack, history, uniqueness, prophecies, sefirot);
    }
}