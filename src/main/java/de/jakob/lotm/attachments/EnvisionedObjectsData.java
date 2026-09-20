package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

public final class EnvisionedObjectsData extends SavedData {
    private static final String DATA_NAME = "envisioned_objects";

    private final Map<String, PlacedObject> placedObjects = new HashMap<>();

    public static EnvisionedObjectsData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                EnvisionedObjectsData::new,
                EnvisionedObjectsData::load
        ), DATA_NAME);
    }

    public void put(String dimension, long position, long expiryMs, String blockId) {
        placedObjects.put(key(dimension, position), new PlacedObject(dimension, position, expiryMs, blockId));
        setDirty();
    }

    public PlacedObject remove(String dimension, long position) {
        PlacedObject removed = placedObjects.remove(key(dimension, position));
        if (removed != null) setDirty();
        return removed;
    }

    public Map<String, PlacedObject> getPlacedObjects() {
        return placedObjects;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag entries = new ListTag();
        for (PlacedObject object : placedObjects.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", object.dimension());
            entry.putLong("position", object.position());
            entry.putLong("expiryMs", object.expiryMs());
            entry.putString("blockId", object.blockId());
            entries.add(entry);
        }
        tag.put("placedObjects", entries);
        return tag;
    }

    private static EnvisionedObjectsData load(CompoundTag tag, HolderLookup.Provider provider) {
        EnvisionedObjectsData data = new EnvisionedObjectsData();
        ListTag entries = tag.getList("placedObjects", Tag.TAG_COMPOUND);
        for (Tag rawEntry : entries) {
            CompoundTag entry = (CompoundTag) rawEntry;
            PlacedObject object = new PlacedObject(
                    entry.getString("dimension"),
                    entry.getLong("position"),
                    entry.getLong("expiryMs"),
                    entry.getString("blockId")
            );
            data.placedObjects.put(key(object.dimension(), object.position()), object);
        }
        return data;
    }

    private static String key(String dimension, long position) {
        return dimension + ":" + position;
    }

    public record PlacedObject(String dimension, long position, long expiryMs, String blockId) {
    }
}