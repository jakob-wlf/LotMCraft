package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SetBeyonderAuditLog extends SavedData {

    public static final String NBT_KEY = "setbeyonder_audit_log";
    private static final int MAX_ENTRIES = 500;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static final SavedData.Factory<SetBeyonderAuditLog> FACTORY = new SavedData.Factory<>(
            SetBeyonderAuditLog::new,
            SetBeyonderAuditLog::new,
            null
    );

    public record AuditEntry(long timestamp, String executorName, String targetName, String pathway, int sequence, String fullCommand) {

        public String format() {
            String time = FORMATTER.format(Instant.ofEpochSecond(timestamp));
            return "[" + time + "] " + executorName + " -> " + targetName
                    + " | " + pathway + " seq " + sequence
                    + " | /" + fullCommand;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("timestamp", timestamp);
            tag.putString("executor", executorName);
            tag.putString("target", targetName);
            tag.putString("pathway", pathway);
            tag.putInt("sequence", sequence);
            tag.putString("command", fullCommand);
            return tag;
        }

        public static AuditEntry fromNBT(CompoundTag tag) {
            return new AuditEntry(
                    tag.getLong("timestamp"),
                    tag.getString("executor"),
                    tag.getString("target"),
                    tag.getString("pathway"),
                    tag.getInt("sequence"),
                    tag.getString("command")
            );
        }
    }

    private final Deque<AuditEntry> entries = new ArrayDeque<>();

    public SetBeyonderAuditLog() {
        super();
    }

    public SetBeyonderAuditLog(CompoundTag nbt, HolderLookup.Provider provider) {
        this();
        if (nbt.contains(NBT_KEY, Tag.TAG_LIST)) {
            ListTag list = nbt.getList(NBT_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                entries.addLast(AuditEntry.fromNBT(list.getCompound(i)));
            }
        }
    }

    public void addEntry(String executorName, String targetName, String pathway, int sequence, String fullCommand) {
        AuditEntry entry = new AuditEntry(
                Instant.now().getEpochSecond(),
                executorName,
                targetName,
                pathway,
                sequence,
                fullCommand
        );
        entries.addLast(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();
        }
        setDirty();
        LOTMCraft.LOGGER.info("[SetBeyonder Audit] {}", entry.format());
    }

    /** Returns up to {@code limit} most recent entries, newest first. */
    public List<AuditEntry> getRecent(int limit) {
        List<AuditEntry> list = new ArrayList<>(entries);
        int start = Math.max(0, list.size() - limit);
        List<AuditEntry> result = new ArrayList<>(list.subList(start, list.size()));
        java.util.Collections.reverse(result);
        return result;
    }

    public int totalEntries() {
        return entries.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (AuditEntry entry : entries) {
            list.add(entry.toNBT());
        }
        tag.put(NBT_KEY, list);
        return tag;
    }

    public static SetBeyonderAuditLog get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, NBT_KEY);
    }
}
