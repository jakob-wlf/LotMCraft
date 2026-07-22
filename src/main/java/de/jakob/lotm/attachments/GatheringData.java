package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * Server-persistent data for the Sefirah Castle Gathering system.
 *
 * Stores:
 *  - gathering members per castle owner  (UUID → Set<UUID>)
 *  - return locations for gathered players (UUID → ServerLocation)
 *  - set of players currently inside an active gathering
 */
public class GatheringData extends SavedData {

    private static final String DATA_NAME = "gatheringData";

    /** Castle owner UUID → set of gathering member UUIDs. */
    private final Map<UUID, Set<UUID>> gatheringMembers = new HashMap<>();

    /** Gathered player UUID → serialised return location tag. */
    private final Map<UUID, CompoundTag> returnLocations = new HashMap<>();

    /** Players currently teleported into an active gathering (in-memory only, not persisted across restarts). */
    private static final Set<UUID> CURRENTLY_GATHERED = new HashSet<>();

    // ── Positions in sefirah_castle dimension (around spawn x=24, y=-57, z=0) ──
    /** Position for the gathering owner — center/head-of-table spot. */
    public static final double[] OWNER_POSITION = {24, -57, 0};

    public static final double[][] CHAIR_POSITIONS = {
            {24, -57,  4}, // N
            {28, -57,  3}, // NE
            {30, -57,  0}, // E
            {28, -57, -3}, // SE
            {24, -57, -4}, // S
            {20, -57, -3}, // SW
            {18, -57,  0}, // W
            {20, -57,  3}, // NW
            {26, -57,  1}, // inner 1
            {22, -57,  1}, // inner 2
    };

    // ── Static access ──────────────────────────────────────────────────────────

    public static GatheringData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(new Factory<GatheringData>(
                GatheringData::new,
                (tag, provider) -> GatheringData.load(tag)
        ), DATA_NAME);
    }

    // ── Member management ──────────────────────────────────────────────────────

    public Set<UUID> getMembers(UUID ownerUUID) {
        return Collections.unmodifiableSet(gatheringMembers.getOrDefault(ownerUUID, Collections.emptySet()));
    }

    public void addMember(UUID ownerUUID, UUID memberUUID) {
        gatheringMembers.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(memberUUID);
        setDirty();
    }

    public void removeMember(UUID ownerUUID, UUID memberUUID) {
        Set<UUID> members = gatheringMembers.get(ownerUUID);
        if (members != null) {
            members.remove(memberUUID);
            if (members.isEmpty()) gatheringMembers.remove(ownerUUID);
        }
        setDirty();
    }

    public boolean isMember(UUID ownerUUID, UUID memberUUID) {
        return gatheringMembers.getOrDefault(ownerUUID, Collections.emptySet()).contains(memberUUID);
    }

    /** Returns true if {@code memberUUID} is a gathering member of any castle owner. */
    public boolean isMemberOfAnyCastle(UUID memberUUID) {
        for (Set<UUID> members : gatheringMembers.values()) {
            if (members.contains(memberUUID)) return true;
        }
        return false;
    }

    /** Returns the castle owner UUID for the given member, or null if not a member of any castle. */
    public UUID getOwnerOfMember(UUID memberUUID) {
        for (Map.Entry<UUID, Set<UUID>> entry : gatheringMembers.entrySet()) {
            if (entry.getValue().contains(memberUUID)) return entry.getKey();
        }
        return null;
    }

    // ── Return locations ───────────────────────────────────────────────────────

    public void saveReturnLocation(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", player.getX());
        tag.putDouble("y", player.getY());
        tag.putDouble("z", player.getZ());
        tag.putString("dim", player.level().dimension().location().toString());
        returnLocations.put(player.getUUID(), tag);
        setDirty();
    }

    public CompoundTag getReturnLocation(UUID playerUUID) {
        return returnLocations.get(playerUUID);
    }

    public void clearReturnLocation(UUID playerUUID) {
        returnLocations.remove(playerUUID);
        setDirty();
    }

    // ── Active gathering state (in-memory) ────────────────────────────────────

    public static void markGathered(UUID playerUUID) {
        CURRENTLY_GATHERED.add(playerUUID);
    }

    public static void unmarkGathered(UUID playerUUID) {
        CURRENTLY_GATHERED.remove(playerUUID);
    }

    public static boolean isGathered(UUID playerUUID) {
        return CURRENTLY_GATHERED.contains(playerUUID);
    }

    public static Set<UUID> getAllGathered() {
        return Collections.unmodifiableSet(CURRENTLY_GATHERED);
    }

    /** Teleports the owner to the head-of-table position and marks them as gathered (but no immunity — handled by event handler). */
    public static boolean gatherOwner(ServerPlayer owner, MinecraftServer server) {
        return gatherPlayerToPosition(owner, OWNER_POSITION[0], OWNER_POSITION[1], OWNER_POSITION[2], server);
    }

    /** Teleports a player to a specific position in sefirah_castle and marks them as gathered. */
    public static boolean gatherPlayerToPosition(ServerPlayer player, double x, double y, double z, MinecraftServer server) {
        ResourceKey<Level> castleDim = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
        ServerLevel castleLevel = server.getLevel(castleDim);
        if (castleLevel == null) return false;

        GatheringData data = GatheringData.get(server);
        data.saveReturnLocation(player);

        player.teleportTo(castleLevel, x, y, z, 180f, 0f);
        markGathered(player.getUUID());
        return true;
    }

    /** Teleports a player to a chair slot in the gathering and marks them as gathered. */
    public static boolean gatherPlayer(ServerPlayer player, int slotIndex, MinecraftServer server) {
        ResourceKey<Level> castleDim = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
        ServerLevel castleLevel = server.getLevel(castleDim);
        if (castleLevel == null) return false;

        // Save their return location first
        GatheringData data = GatheringData.get(server);
        data.saveReturnLocation(player);

        double[] pos = CHAIR_POSITIONS[slotIndex % CHAIR_POSITIONS.length];
        player.teleportTo(castleLevel, pos[0], pos[1], pos[2], 180f, 0f);
        markGathered(player.getUUID());
        return true;
    }

    /** Returns a gathered player to their saved location and unmarks them. */
    public static void returnPlayer(ServerPlayer player, MinecraftServer server) {
        GatheringData data = GatheringData.get(server);
        CompoundTag loc = data.getReturnLocation(player.getUUID());
        if (loc != null) {
            String dimStr = loc.getString("dim");
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(dimStr));
            ServerLevel level = server.getLevel(dimKey);
            if (level == null) level = server.overworld();
            double x = loc.getDouble("x");
            double y = loc.getDouble("y");
            double z = loc.getDouble("z");
            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
            data.clearReturnLocation(player.getUUID());
        }
        unmarkGathered(player.getUUID());
    }

    // ── NBT serialization ──────────────────────────────────────────────────────

    public static GatheringData load(CompoundTag tag) {
        GatheringData data = new GatheringData();

        ListTag ownerList = tag.getList("owners", Tag.TAG_COMPOUND);
        for (Tag t : ownerList) {
            CompoundTag ownerTag = (CompoundTag) t;
            UUID ownerUUID = UUID.fromString(ownerTag.getString("owner"));
            ListTag memberList = ownerTag.getList("members", Tag.TAG_STRING);
            Set<UUID> members = new HashSet<>();
            for (Tag m : memberList) {
                members.add(UUID.fromString(m.getAsString()));
            }
            data.gatheringMembers.put(ownerUUID, members);
        }

        ListTag returnList = tag.getList("returns", Tag.TAG_COMPOUND);
        for (Tag t : returnList) {
            CompoundTag entry = (CompoundTag) t;
            UUID uuid = UUID.fromString(entry.getString("uuid"));
            data.returnLocations.put(uuid, entry.getCompound("loc"));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag ownerList = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> e : gatheringMembers.entrySet()) {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putString("owner", e.getKey().toString());
            ListTag memberList = new ListTag();
            for (UUID m : e.getValue()) memberList.add(StringTag.valueOf(m.toString()));
            ownerTag.put("members", memberList);
            ownerList.add(ownerTag);
        }
        tag.put("owners", ownerList);

        ListTag returnList = new ListTag();
        for (Map.Entry<UUID, CompoundTag> e : returnLocations.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("uuid", e.getKey().toString());
            entry.put("loc", e.getValue());
            returnList.add(entry);
        }
        tag.put("returns", returnList);

        return tag;
    }
}
