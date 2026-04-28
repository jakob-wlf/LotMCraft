package de.jakob.lotm.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DreamMazeData extends SavedData {

    private static final String DATA_NAME = "dream_maze_data";

    // Spacing between per-player maze regions in the dream maze dimension
    public static final int MAZE_SIZE = 100; // editable — full width/depth of one maze region
    private static final int SPACING = MAZE_SIZE + 64; // gap between regions
    private final Map<UUID, BlockPos> mazeOrigins = new HashMap<>();
    private final Set<UUID> generatedMazes = new HashSet<>();
    private int nextMazeIndex = 0;

    private final Map<UUID, Set<UUID>> occupants = new HashMap<>();
    private final Map<UUID, double[]> returnPositions = new HashMap<>();
    private final Map<UUID, String> returnDimensions = new HashMap<>();
    private final Map<UUID, UUID> occupantToCaster = new HashMap<>();

    public DreamMazeData() {
        super();
    }

    public static DreamMazeData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                DreamMazeData::new,
                DreamMazeData::load
        ), DATA_NAME);
    }

    public BlockPos getOrCreateMazeOrigin(UUID casterUUID) {
        if (!mazeOrigins.containsKey(casterUUID)) {
            int col = nextMazeIndex % 100;
            int row = nextMazeIndex / 100;
            int x = col * SPACING;
            int z = row * SPACING;
            mazeOrigins.put(casterUUID, new BlockPos(x, 64, z));
            nextMazeIndex++;
            setDirty();
        }
        return mazeOrigins.get(casterUUID);
    }

    public boolean isMazeGenerated(UUID casterUUID) {
        return generatedMazes.contains(casterUUID);
    }

    public void markMazeGenerated(UUID casterUUID) {
        generatedMazes.add(casterUUID);
        setDirty();
    }

    public void addOccupant(UUID casterUUID, UUID occupantUUID, Vec3 returnPos, ResourceKey<Level> returnDim) {
        occupants.computeIfAbsent(casterUUID, k -> new HashSet<>()).add(occupantUUID);
        returnPositions.put(occupantUUID, new double[]{returnPos.x, returnPos.y, returnPos.z});
        returnDimensions.put(occupantUUID, returnDim.location().toString());
        occupantToCaster.put(occupantUUID, casterUUID);
        setDirty();
    }

    public void removeOccupant(UUID occupantUUID) {
        UUID casterUUID = occupantToCaster.remove(occupantUUID);
        if (casterUUID != null) {
            Set<UUID> set = occupants.get(casterUUID);
            if (set != null) {
                set.remove(occupantUUID);
                if (set.isEmpty()) occupants.remove(casterUUID);
            }
        }
        returnPositions.remove(occupantUUID);
        returnDimensions.remove(occupantUUID);
        setDirty();
    }

    public boolean isOccupant(UUID uuid) {
        return occupantToCaster.containsKey(uuid);
    }

    public UUID getCasterForOccupant(UUID occupantUUID) {
        return occupantToCaster.get(occupantUUID);
    }

    public double[] getReturnPosition(UUID occupantUUID) {
        return returnPositions.get(occupantUUID);
    }

    public String getReturnDimension(UUID occupantUUID) {
        return returnDimensions.get(occupantUUID);
    }

    public Set<UUID> getOccupants(UUID casterUUID) {
        return occupants.getOrDefault(casterUUID, Collections.emptySet());
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Maze origins
        ListTag originsList = new ListTag();
        for (Map.Entry<UUID, BlockPos> entry : mazeOrigins.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", entry.getKey());
            t.putInt("X", entry.getValue().getX());
            t.putInt("Y", entry.getValue().getY());
            t.putInt("Z", entry.getValue().getZ());
            originsList.add(t);
        }
        tag.put("MazeOrigins", originsList);

        ListTag genList = new ListTag();
        for (UUID uuid : generatedMazes) {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", uuid);
            genList.add(t);
        }
        tag.put("GeneratedMazes", genList);

        tag.putInt("NextIndex", nextMazeIndex);

        ListTag returnList = new ListTag();
        for (Map.Entry<UUID, double[]> entry : returnPositions.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", entry.getKey());
            t.putDouble("X", entry.getValue()[0]);
            t.putDouble("Y", entry.getValue()[1]);
            t.putDouble("Z", entry.getValue()[2]);
            t.putString("Dim", returnDimensions.getOrDefault(entry.getKey(), "minecraft:overworld"));
            returnList.add(t);
        }
        tag.put("ReturnPositions", returnList);

        ListTag occList = new ListTag();
        for (Map.Entry<UUID, UUID> entry : occupantToCaster.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Occupant", entry.getKey());
            t.putUUID("Caster", entry.getValue());
            occList.add(t);
        }
        tag.put("OccupantToCaster", occList);

        return tag;
    }

    public static DreamMazeData load(CompoundTag tag, HolderLookup.Provider provider) {
        DreamMazeData data = new DreamMazeData();

        ListTag originsList = tag.getList("MazeOrigins", Tag.TAG_COMPOUND);
        for (int i = 0; i < originsList.size(); i++) {
            CompoundTag t = originsList.getCompound(i);
            data.mazeOrigins.put(t.getUUID("UUID"),
                    new BlockPos(t.getInt("X"), t.getInt("Y"), t.getInt("Z")));
        }

        ListTag genList = tag.getList("GeneratedMazes", Tag.TAG_COMPOUND);
        for (int i = 0; i < genList.size(); i++) {
            data.generatedMazes.add(genList.getCompound(i).getUUID("UUID"));
        }

        data.nextMazeIndex = tag.getInt("NextIndex");

        ListTag returnList = tag.getList("ReturnPositions", Tag.TAG_COMPOUND);
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag t = returnList.getCompound(i);
            UUID uuid = t.getUUID("UUID");
            data.returnPositions.put(uuid, new double[]{t.getDouble("X"), t.getDouble("Y"), t.getDouble("Z")});
            data.returnDimensions.put(uuid, t.getString("Dim"));
        }

        ListTag occList = tag.getList("OccupantToCaster", Tag.TAG_COMPOUND);
        for (int i = 0; i < occList.size(); i++) {
            CompoundTag t = occList.getCompound(i);
            UUID occupant = t.getUUID("Occupant");
            UUID caster = t.getUUID("Caster");
            data.occupantToCaster.put(occupant, caster);
            data.occupants.computeIfAbsent(caster, k -> new HashSet<>()).add(occupant);
        }

        return data;
    }
}
