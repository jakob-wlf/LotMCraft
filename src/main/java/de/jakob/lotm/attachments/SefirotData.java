package de.jakob.lotm.attachments;

import de.jakob.lotm.util.data.LocationWithLevelKey;
import de.jakob.lotm.util.data.ServerLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class SefirotData extends SavedData {

    private static final String DATA_NAME = "sefirotData";

    private final HashMap<UUID, String> claimedSefirah = new HashMap<>();
    private final HashMap<UUID, LocationWithLevelKey> returnLocations = new HashMap<>();
    private final HashSet<UUID> isInSefirot = new HashSet<>();

    public static SefirotData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<>(
                SefirotData::new,
                SefirotData::load
        ), DATA_NAME);
    }

    public boolean claimSefirot(UUID uuid, String sefirot) {
        if(claimedSefirah.containsKey(uuid)) {
            return false;
        }

        claimedSefirah.put(uuid, sefirot);
        setDirty();
        return true;
    }

    public String getClaimedSefirot(UUID uuid) {
        return claimedSefirah.getOrDefault(uuid, "");
    }

    public void setIsInSefirot(UUID uuid, boolean inSefirot) {
        if(!inSefirot) {
            isInSefirot.remove(uuid);
        }
        else {
            isInSefirot.add(uuid);
        }

        setDirty();
    }

    public boolean isInSefirot(ServerPlayer player) {
        return isInSefirot.contains(player.getUUID());
    }

    public void setLastReturnLocation(ServerPlayer player) {
        returnLocations.put(player.getUUID(), new LocationWithLevelKey(new Vec3(player.getX(), player.getY(), player.getZ()), player.level().dimension().location().toString()));
        setDirty();
    }

    public ServerLocation getReturnLocationForPlayer(ServerPlayer player) {
        if(!returnLocations.containsKey(player.getUUID())) {
            return null;
        }

        LocationWithLevelKey locationWithLevelKey = returnLocations.get(player.getUUID());
        String levelId = locationWithLevelKey.getLevelKey();

        ResourceLocation levelLocation = ResourceLocation.parse(levelId);
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, levelLocation);

        ServerLevel level = player.server.getLevel(levelKey);
        if(level == null) {
            return null;
        }

        return new ServerLocation(new Vec3(locationWithLevelKey.getPosition().x, locationWithLevelKey.getPosition().y, locationWithLevelKey.getPosition().z), level);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag claimedSefirahList = new ListTag();
        for (Map.Entry<UUID, String> entry : claimedSefirah.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putString("Sefirot", entry.getValue());
            claimedSefirahList.add(entryTag);
        }
        tag.put("claimedSefirah", claimedSefirahList);

        ListTag returnLocationsList = new ListTag();
        for (Map.Entry<UUID, LocationWithLevelKey> entry : returnLocations.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putString("Server", entry.getValue().getLevelKey());
            entryTag.putDouble("X", entry.getValue().getPosition().x);
            entryTag.putDouble("Y", entry.getValue().getPosition().y);
            entryTag.putDouble("Z", entry.getValue().getPosition().z);
            returnLocationsList.add(entryTag);
        }
        tag.put("returnLocations", returnLocationsList);

        ListTag playersInSefirotList = new ListTag();
        for (UUID uuid : isInSefirot) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", uuid);
            playersInSefirotList.add(entryTag);
        }
        tag.put("playersInSefirot", playersInSefirotList);

        return tag;
    }

    public static SefirotData load(CompoundTag tag, HolderLookup.Provider provider) {
        SefirotData data = new SefirotData();

        ListTag claimedSefirahList = tag.getList("claimedSefirah", Tag.TAG_COMPOUND);
        for (int i = 0; i < claimedSefirahList.size(); i++) {
            CompoundTag entryTag = claimedSefirahList.getCompound(i);
            UUID uuid = entryTag.getUUID("UUID");
            String sefirot = entryTag.getString("Sefirot");
            data.claimedSefirah.put(uuid, sefirot);
        }

        ListTag returnLocationsList = tag.getList("returnLocations", Tag.TAG_COMPOUND);
        for (int i = 0; i < returnLocationsList.size(); i++) {
            CompoundTag entryTag = returnLocationsList.getCompound(i);
            UUID uuid = entryTag.getUUID("UUID");
            String levelKey = entryTag.getString("Server");
            double x = entryTag.getShort("X");
            double y = entryTag.getShort("Y");
            double z = entryTag.getShort("Z");
            data.returnLocations.put(uuid, new LocationWithLevelKey(new Vec3(x, y, z), levelKey));
        }

        ListTag playersInSefirotList = tag.getList("playersInSefirot", Tag.TAG_COMPOUND);
        for (int i = 0; i < playersInSefirotList.size(); i++) {
            CompoundTag entryTag = playersInSefirotList.getCompound(i);
            UUID uuid = entryTag.getUUID("UUID");
            data.isInSefirot.add(uuid);
        }

        return data;
    }
}
