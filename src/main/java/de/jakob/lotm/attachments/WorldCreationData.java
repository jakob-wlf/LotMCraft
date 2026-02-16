package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class WorldCreationData extends SavedData {
    private static final String DATA_NAME = "world_creation_data";
    private static final int SPACING = 10000; // Space between pocket dimensions

    private final Map<UUID, BlockPos> playerPocketLocations = new HashMap<>();
    private final Set<UUID> visitedPlayers = new HashSet<>();
    private int nextPocketIndex = 0;

    public WorldCreationData() {
        super();
    }
    
    public static WorldCreationData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
            WorldCreationData::new,
            WorldCreationData::load
        ), DATA_NAME);
    }
    
    public BlockPos getOrCreatePocketLocation(UUID playerUUID) {
        if (!playerPocketLocations.containsKey(playerUUID)) {
            // Create a new pocket dimension location in The End
            // Place them far from 0,0,0 and space them out
            int x = 10000 + (nextPocketIndex * SPACING);
            int y = 128;
            int z = 10000 + ((nextPocketIndex / 10) * SPACING);
            
            BlockPos newLocation = new BlockPos(x, y, z);
            playerPocketLocations.put(playerUUID, newLocation);
            nextPocketIndex++;
            setDirty();
        }
        return playerPocketLocations.get(playerUUID);
    }
    
    public boolean isFirstVisit(UUID playerUUID) {
        return !visitedPlayers.contains(playerUUID);
    }
    
    public void markVisited(UUID playerUUID) {
        visitedPlayers.add(playerUUID);
        setDirty();
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag locationsList = new ListTag();
        for (Map.Entry<UUID, BlockPos> entry : playerPocketLocations.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putInt("X", entry.getValue().getX());
            entryTag.putInt("Y", entry.getValue().getY());
            entryTag.putInt("Z", entry.getValue().getZ());
            locationsList.add(entryTag);
        }
        tag.put("Locations", locationsList);
        
        ListTag visitedList = new ListTag();
        for (UUID uuid : visitedPlayers) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            visitedList.add(uuidTag);
        }
        tag.put("Visited", visitedList);
        
        tag.putInt("NextIndex", nextPocketIndex);
        
        return tag;
    }
    
    public static WorldCreationData load(CompoundTag tag, HolderLookup.Provider provider) {
        WorldCreationData data = new WorldCreationData();
        
        ListTag locationsList = tag.getList("Locations", Tag.TAG_COMPOUND);
        for (int i = 0; i < locationsList.size(); i++) {
            CompoundTag entryTag = locationsList.getCompound(i);
            UUID uuid = entryTag.getUUID("UUID");
            BlockPos pos = new BlockPos(
                entryTag.getInt("X"),
                entryTag.getInt("Y"),
                entryTag.getInt("Z")
            );
            data.playerPocketLocations.put(uuid, pos);
        }
        
        ListTag visitedList = tag.getList("Visited", Tag.TAG_COMPOUND);
        for (int i = 0; i < visitedList.size(); i++) {
            CompoundTag uuidTag = visitedList.getCompound(i);
            data.visitedPlayers.add(uuidTag.getUUID("UUID"));
        }
        
        data.nextPocketIndex = tag.getInt("NextIndex");
        
        return data;
    }
}