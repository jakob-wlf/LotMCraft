package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class RiverOfEternalDarknessData extends SavedData {

    private static final String DATA_NAME = "river_of_eternal_darkness";

    private boolean wellPlaced = false;
    private BlockPos wellPos = null;
    private String wellDimension = "";
    private boolean riverBuilt = false;

    /** owner UUID → set of blessed player UUIDs (persistent) */
    private final Map<UUID, Set<UUID>> blessingsByOwner = new HashMap<>();

    // ── Blessing persistence API ──────────────────────────────────────────────

    public void addBlessing(UUID ownerUUID, UUID targetUUID) {
        blessingsByOwner.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(targetUUID);
        setDirty();
    }

    public void removeBlessing(UUID ownerUUID, UUID targetUUID) {
        Set<UUID> set = blessingsByOwner.get(ownerUUID);
        if (set != null) {
            set.remove(targetUUID);
            if (set.isEmpty()) blessingsByOwner.remove(ownerUUID);
        }
        setDirty();
    }

    public void clearBlessingsByOwner(UUID ownerUUID) {
        blessingsByOwner.remove(ownerUUID);
        setDirty();
    }

    /** Returns a snapshot of all blessings for server-start loading. */
    public Map<UUID, Set<UUID>> getAllBlessings() {
        return Collections.unmodifiableMap(blessingsByOwner);
    }

    public static RiverOfEternalDarknessData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                RiverOfEternalDarknessData::new,
                RiverOfEternalDarknessData::load
        ), DATA_NAME);
    }

    public boolean isWellPlaced() {
        return wellPlaced;
    }

    public BlockPos getWellPos() {
        return wellPos;
    }

    public String getWellDimension() {
        return wellDimension;
    }

    public void setWellPlaced(BlockPos pos, ResourceLocation dimension) {
        this.wellPlaced = true;
        this.wellPos = pos;
        this.wellDimension = dimension.toString();
        setDirty();
    }

    public boolean isRiverBuilt() {
        return riverBuilt;
    }

    public void setRiverBuilt(boolean built) {
        this.riverBuilt = built;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        tag.putBoolean("wellPlaced", wellPlaced);
        tag.putBoolean("riverBuilt", riverBuilt);

        if (wellPos != null) {
            tag.putInt("wellX", wellPos.getX());
            tag.putInt("wellY", wellPos.getY());
            tag.putInt("wellZ", wellPos.getZ());
        }

        if (wellDimension != null) {
            tag.putString("wellDimension", wellDimension);
        }

        // Save blessings
        ListTag blessingList = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : blessingsByOwner.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            CompoundTag bt = new CompoundTag();
            bt.putUUID("owner", entry.getKey());
            ListTag targets = new ListTag();
            for (UUID t : entry.getValue()) targets.add(StringTag.valueOf(t.toString()));
            bt.put("targets", targets);
            blessingList.add(bt);
        }
        tag.put("blessings", blessingList);

        return tag;
    }

    public static RiverOfEternalDarknessData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        RiverOfEternalDarknessData data = new RiverOfEternalDarknessData();
        data.wellPlaced = tag.getBoolean("wellPlaced");
        data.riverBuilt = tag.getBoolean("riverBuilt");

        if (tag.contains("wellX")) {
            int x = tag.getInt("wellX");
            int y = tag.getInt("wellY");
            int z = tag.getInt("wellZ");
            data.wellPos = new BlockPos(x, y, z);
        }

        if (tag.contains("wellDimension")) {
            data.wellDimension = tag.getString("wellDimension");
        }

        // Load blessings
        if (tag.contains("blessings")) {
            ListTag blessingList = tag.getList("blessings", 10 /* COMPOUND */);
            for (int i = 0; i < blessingList.size(); i++) {
                CompoundTag bt = blessingList.getCompound(i);
                UUID owner = bt.getUUID("owner");
                Set<UUID> targets = new HashSet<>();
                ListTag tl = bt.getList("targets", 8 /* STRING */);
                for (int j = 0; j < tl.size(); j++) {
                    try { targets.add(UUID.fromString(tl.getString(j))); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (!targets.isEmpty()) data.blessingsByOwner.put(owner, targets);
            }
        }

        return data;
    }
}
