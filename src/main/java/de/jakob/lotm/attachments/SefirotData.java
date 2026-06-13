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

import java.util.*;

public class SefirotData extends SavedData {

    private static final String DATA_NAME = "sefirotData";

    private final HashMap<UUID, String> claimedSefirah = new HashMap<>();
    private final HashMap<UUID, LocationWithLevelKey> returnLocations = new HashMap<>();
    private final HashSet<UUID> isInSefirot = new HashSet<>();

    // ── Mental Imprint ────────────────────────────────────────────────────────
    /** sefirot name → UUID of the very first player to ever claim it (never changes once set). */
    private final HashMap<String, UUID> firstOwners = new HashMap<>();
    /** sefirot name → imprint percentage 0–100. Grows while first owner holds it; can only drop to 10. */
    private final HashMap<String, Integer> mentalImprintPercent = new HashMap<>();
    /** sefirot name → total game-seconds the original owner has been online holding it. */
    private final HashMap<String, Long> originalOwnerSecondsOnline = new HashMap<>();
    /** sefirot name → total game-seconds the current non-original owner has been online reducing it. */
    private final HashMap<String, Long> currentOwnerSecondsOnline = new HashMap<>();
    /** sefirot name → UUID of original owner when a reclaim is pending (original owner was offline). */
    private final HashMap<String, UUID> pendingReclaims = new HashMap<>();

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

        if(claimedSefirah.containsValue(sefirot)) {
            return false;
        }

        claimedSefirah.put(uuid, sefirot);
        setDirty();
        return true;
    }

    public void unclaimSefirot(UUID id){
        claimedSefirah.remove(id);

        setDirty();
    }

    public void unclaimAllByString(String sefirot){
        var buff = new LinkedList<UUID>();

        for (var obj : claimedSefirah.entrySet()){
            if(obj.getValue().equals(sefirot))
                buff.add(obj.getKey());
        }

        for (var obj : buff){
            claimedSefirah.remove(obj);
        }

        setDirty();
    }

    public String getClaimedSefirot(UUID uuid) {
        return claimedSefirah.getOrDefault(uuid, "");
    }

    public boolean isSefirotClaimed(String sefirot) {
        return claimedSefirah.containsValue(sefirot);
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

    // ── Mental Imprint API ────────────────────────────────────────────────────

    /** Sets the first owner for a sefirot only if none has been recorded yet. */
    public void setFirstOwnerIfAbsent(String sefirot, UUID uuid) {
        if (!firstOwners.containsKey(sefirot)) {
            firstOwners.put(sefirot, uuid);
            setDirty();
        }
    }

    /** Returns the UUID of the original first claimer, or {@code null} if none set. */
    public UUID getFirstOwner(String sefirot) {
        return firstOwners.get(sefirot);
    }

    public boolean hasFirstOwner(String sefirot) {
        return firstOwners.containsKey(sefirot);
    }

    /** Returns the current imprint percentage (0–100) for the given sefirot. */
    public int getMentalImprint(String sefirot) {
        return mentalImprintPercent.getOrDefault(sefirot, 0);
    }

    /** Directly sets the imprint percentage (admin/command use). Clamps to 0–100. */
    public void setMentalImprintDirect(String sefirot, int percent) {
        mentalImprintPercent.put(sefirot, Math.max(0, Math.min(100, percent)));
        setDirty();
    }

    /** Clears all imprint data for the given sefirot (first owner, imprint %, counters). */
    public void clearMentalImprint(String sefirot) {
        firstOwners.remove(sefirot);
        mentalImprintPercent.remove(sefirot);
        originalOwnerSecondsOnline.remove(sefirot);
        currentOwnerSecondsOnline.remove(sefirot);
        pendingReclaims.remove(sefirot);
        setDirty();
    }

    /**
     * Ticks the original owner's online time by one game-second.
     * Increments imprint by 1% for every 3600 accumulated game-seconds (≈1 real hour at 20 TPS).
     *
     * @return {@code true} if the imprint percentage increased.
     */
    public boolean tickOriginalOwner(String sefirot) {
        long prev = originalOwnerSecondsOnline.getOrDefault(sefirot, 0L);
        long next = prev + 1;
        originalOwnerSecondsOnline.put(sefirot, next);
        setDirty();
        if (prev / 3600 < next / 3600) {
            int current = mentalImprintPercent.getOrDefault(sefirot, 0);
            mentalImprintPercent.put(sefirot, current + 1);
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * Ticks the current non-original owner's hold time by one game-second.
     * Decrements imprint by 1% for every 3600 accumulated game-seconds, but never below 10%.
     *
     * @return {@code true} if the imprint percentage decreased.
     */
    public boolean tickCurrentOwnerReduction(String sefirot) {
        long prev = currentOwnerSecondsOnline.getOrDefault(sefirot, 0L);
        long next = prev + 1;
        currentOwnerSecondsOnline.put(sefirot, next);
        setDirty();
        if (prev / 3600 < next / 3600) {
            int current = mentalImprintPercent.getOrDefault(sefirot, 0);
            if (current > 10) {
                mentalImprintPercent.put(sefirot, current - 1);
                setDirty();
                return true;
            }
        }
        return false;
    }

    /** Resets the current-owner reduction counter (call when a new non-original owner takes the sefirot). */
    public void resetCurrentOwnerSeconds(String sefirot) {
        currentOwnerSecondsOnline.remove(sefirot);
        setDirty();
    }

    /** Returns the UUID of whichever player currently holds the given sefirot, or {@code null}. */
    public UUID getHolderOf(String sefirot) {
        for (Map.Entry<UUID, String> e : claimedSefirah.entrySet()) {
            if (e.getValue().equals(sefirot)) return e.getKey();
        }
        return null;
    }

    // ── Pending Reclaim ───────────────────────────────────────────────────────

    public void setPendingReclaim(String sefirot, UUID originalOwner) {
        pendingReclaims.put(sefirot, originalOwner);
        setDirty();
    }

    public void clearPendingReclaim(String sefirot) {
        pendingReclaims.remove(sefirot);
        setDirty();
    }

    public UUID getPendingReclaim(String sefirot) {
        return pendingReclaims.get(sefirot);
    }

    /** Returns a snapshot of all pending reclaims (sefirot → original owner UUID). */
    public java.util.Map<String, UUID> getAllPendingReclaims() {
        return java.util.Collections.unmodifiableMap(pendingReclaims);
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

        // Mental Imprint
        ListTag firstOwnersList = new ListTag();
        for (Map.Entry<String, UUID> entry : firstOwners.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("Sefirot", entry.getKey());
            e.putUUID("Owner", entry.getValue());
            firstOwnersList.add(e);
        }
        tag.put("firstOwners", firstOwnersList);

        ListTag imprintList = new ListTag();
        for (Map.Entry<String, Integer> entry : mentalImprintPercent.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("Sefirot", entry.getKey());
            e.putInt("Imprint", entry.getValue());
            imprintList.add(e);
        }
        tag.put("mentalImprintPercent", imprintList);

        ListTag origSecondsList = new ListTag();
        for (Map.Entry<String, Long> entry : originalOwnerSecondsOnline.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("Sefirot", entry.getKey());
            e.putLong("Seconds", entry.getValue());
            origSecondsList.add(e);
        }
        tag.put("originalOwnerSecondsOnline", origSecondsList);

        ListTag currSecondsList = new ListTag();
        for (Map.Entry<String, Long> entry : currentOwnerSecondsOnline.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("Sefirot", entry.getKey());
            e.putLong("Seconds", entry.getValue());
            currSecondsList.add(e);
        }
        tag.put("currentOwnerSecondsOnline", currSecondsList);

        ListTag pendingReclaimsList = new ListTag();
        for (Map.Entry<String, UUID> entry : pendingReclaims.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("Sefirot", entry.getKey());
            e.putUUID("Owner", entry.getValue());
            pendingReclaimsList.add(e);
        }
        tag.put("pendingReclaims", pendingReclaimsList);

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

        // Mental Imprint
        if (tag.contains("firstOwners")) {
            ListTag list = tag.getList("firstOwners", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompound(i);
                data.firstOwners.put(e.getString("Sefirot"), e.getUUID("Owner"));
            }
        }

        if (tag.contains("mentalImprintPercent")) {
            ListTag list = tag.getList("mentalImprintPercent", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompound(i);
                data.mentalImprintPercent.put(e.getString("Sefirot"), e.getInt("Imprint"));
            }
        }

        if (tag.contains("originalOwnerSecondsOnline")) {
            ListTag list = tag.getList("originalOwnerSecondsOnline", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompound(i);
                data.originalOwnerSecondsOnline.put(e.getString("Sefirot"), e.getLong("Seconds"));
            }
        }

        if (tag.contains("currentOwnerSecondsOnline")) {
            ListTag list = tag.getList("currentOwnerSecondsOnline", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompound(i);
                data.currentOwnerSecondsOnline.put(e.getString("Sefirot"), e.getLong("Seconds"));
            }
        }

        if (tag.contains("pendingReclaims")) {
            ListTag list = tag.getList("pendingReclaims", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompound(i);
                data.pendingReclaims.put(e.getString("Sefirot"), e.getUUID("Owner"));
            }
        }

        return data;
    }
}
