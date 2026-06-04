package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MysteriousTabletData extends SavedData {

    public enum FragmentType {
        UPPER("upper"),
        RIGHT("right"),
        LEFT("left"),
        LOWER("lower");

        private final String id;

        FragmentType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static FragmentType fromId(String id) {
            for (FragmentType type : values()) {
                if (type.id.equalsIgnoreCase(id)) {
                    return type;
                }
            }
            return null;
        }
    }

    private static final String DATA_NAME = "mysterious_tablet_data";

    private final EnumMap<FragmentType, UUID> fragmentIds = new EnumMap<>(FragmentType.class);
    private final EnumMap<FragmentType, Long> fragmentLastSeen = new EnumMap<>(FragmentType.class);

    private UUID tabletId = null;
    private long tabletLastSeen = 0L;

    private boolean lockedByCastle = false;

    // All ancient city chest positions that have received a LOWER fragment copy.
    private final Set<BlockPos> ancientCityChestPositions = new HashSet<>();
    // All spirit world structure chest positions that have received a LEFT fragment copy.
    private final Set<BlockPos> spiritChestPositions = new HashSet<>();

    public static MysteriousTabletData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        MysteriousTabletData data = storage.computeIfAbsent(new Factory<>(
                MysteriousTabletData::new,
                MysteriousTabletData::load
        ), DATA_NAME);

        boolean castleClaimed = SefirotData.get(server).isSefirotClaimed("sefirah_castle");
        if (castleClaimed && !data.lockedByCastle) {
            data.setLockedByCastle(true);
        } else if (!castleClaimed && data.lockedByCastle) {
            data.setLockedByCastle(false);
        }

        return data;
    }

    public boolean isLockedByCastle() {
        return lockedByCastle;
    }

    public void setLockedByCastle(boolean locked) {
        if (this.lockedByCastle == locked) {
            return;
        }
        this.lockedByCastle = locked;
        if (locked) {
            clearTablet();
            clearAllFragments();
        }
        setDirty();
    }

    public boolean tabletExists() {
        return tabletId != null;
    }

    public UUID getTabletId() {
        return tabletId;
    }

    public long getTabletLastSeen() {
        return tabletLastSeen;
    }

    public void markTabletExists(UUID id, long tick) {
        if (id == null) {
            return;
        }
        tabletId = id;
        tabletLastSeen = tick;
        clearAllFragments();
        setDirty();
    }

    public void markTabletSeen(UUID id, long tick) {
        if (!isTabletOwner(id)) {
            return;
        }
        tabletLastSeen = tick;
        setDirty();
    }

    public void clearTablet() {
        tabletId = null;
        tabletLastSeen = 0L;
        setDirty();
    }

    public boolean hasAnyFragments() {
        return !fragmentIds.isEmpty();
    }

    public boolean hasFragment(FragmentType type) {
        return fragmentIds.containsKey(type);
    }

    public UUID getFragmentId(FragmentType type) {
        return fragmentIds.get(type);
    }

    public long getFragmentLastSeen(FragmentType type) {
        return fragmentLastSeen.getOrDefault(type, 0L);
    }

    public boolean isFragmentOwner(FragmentType type, UUID id) {
        return id != null && id.equals(fragmentIds.get(type));
    }

    public boolean isTabletOwner(UUID id) {
        return id != null && id.equals(tabletId);
    }

    public boolean canSpawnFragment(FragmentType type) {
        if (lockedByCastle || tabletExists()) {
            return false;
        }
        return !fragmentIds.containsKey(type);
    }

    public boolean canSpawnTablet() {
        return !lockedByCastle && tabletId == null && fragmentIds.isEmpty();
    }

    public void markFragmentExists(FragmentType type, UUID id, long tick) {
        if (type == null || id == null) {
            return;
        }
        fragmentIds.put(type, id);
        fragmentLastSeen.put(type, tick);
        setDirty();
    }

    public void markFragmentSeen(FragmentType type, UUID id, long tick) {
        if (!isFragmentOwner(type, id)) {
            return;
        }
        fragmentLastSeen.put(type, tick);
        setDirty();
    }

    public void clearFragment(FragmentType type) {
        fragmentIds.remove(type);
        fragmentLastSeen.remove(type);
        // Chest positions are intentionally kept — the refiller uses them to re-place chest copies.
        setDirty();
    }

    public void clearAllFragments() {
        fragmentIds.clear();
        fragmentLastSeen.clear();
        spiritChestPositions.clear();
        ancientCityChestPositions.clear();
        setDirty();
    }

    public Set<BlockPos> getAncientCityChestPositions() {
        return Collections.unmodifiableSet(ancientCityChestPositions);
    }

    public void addAncientCityChestPos(BlockPos pos) {
        ancientCityChestPositions.add(pos);
        setDirty();
    }

    public Set<BlockPos> getSpiritChestPositions() {
        return Collections.unmodifiableSet(spiritChestPositions);
    }

    public void addSpiritChestPos(BlockPos pos) {
        spiritChestPositions.add(pos);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("LockedByCastle", lockedByCastle);

        if (tabletId != null) {
            tag.putUUID("TabletId", tabletId);
            tag.putLong("TabletLastSeen", tabletLastSeen);
        }

        ListTag fragmentsList = new ListTag();
        for (FragmentType type : fragmentIds.keySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Type", type.getId());
            entry.putUUID("Id", fragmentIds.get(type));
            entry.putLong("LastSeen", fragmentLastSeen.getOrDefault(type, 0L));
            fragmentsList.add(entry);
        }
        tag.put("Fragments", fragmentsList);

        tag.putLongArray("AncientCityChestPositions",
                ancientCityChestPositions.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("SpiritChestPositions",
                spiritChestPositions.stream().mapToLong(BlockPos::asLong).toArray());

        return tag;
    }

    public static MysteriousTabletData load(CompoundTag tag, HolderLookup.Provider provider) {
        MysteriousTabletData data = new MysteriousTabletData();
        data.lockedByCastle = tag.getBoolean("LockedByCastle");

        if (tag.hasUUID("TabletId")) {
            data.tabletId = tag.getUUID("TabletId");
            data.tabletLastSeen = tag.getLong("TabletLastSeen");
        }

        ListTag fragmentsList = tag.getList("Fragments", Tag.TAG_COMPOUND);
        for (int i = 0; i < fragmentsList.size(); i++) {
            CompoundTag entry = fragmentsList.getCompound(i);
            FragmentType type = FragmentType.fromId(entry.getString("Type"));
            if (type == null || !entry.hasUUID("Id")) {
                continue;
            }
            data.fragmentIds.put(type, entry.getUUID("Id"));
            data.fragmentLastSeen.put(type, entry.getLong("LastSeen"));
        }

        // New format: arrays of serialised BlockPos
        for (long value : tag.getLongArray("AncientCityChestPositions")) {
            data.ancientCityChestPositions.add(BlockPos.of(value));
        }
        for (long value : tag.getLongArray("SpiritChestPositions")) {
            data.spiritChestPositions.add(BlockPos.of(value));
        }
        // Migration: old single-position fields from before multi-chest support
        if (tag.contains("AncientCityChestPos")) {
            data.ancientCityChestPositions.add(BlockPos.of(tag.getLong("AncientCityChestPos")));
        }
        if (tag.contains("SpiritChestPos")) {
            data.spiritChestPositions.add(BlockPos.of(tag.getLong("SpiritChestPos")));
        }

        return data;
    }
}
