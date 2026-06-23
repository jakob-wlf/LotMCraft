package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.RiverOfEternalDarknessData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages River-of-Eternal-Darkness blessings.
 *
 * The River owner may bless players who have prayed to them, granting:
 *   1. Immunity to the ASLEEP mob effect.
 *   2. Passive concealment: divination attempts are blocked for anyone at the
 *      same sequence or up to 3 sequences above the blessed player (lower number).
 *      Only a diviner who is MORE than 3 sequences above (i.e. divinerSeq <= blessedSeq - 4)
 *      can pierce the protection.
 *
 * Blessing limits scale with the River owner's sequence:
 *   Seq > 4          → 0 blessings (cannot bless yet)
 *   Seq 3–4          → 1 blessed player at a time
 *   Seq ≤ 2 (or GOO) → 2 blessed players at a time
 */
public class RiverBlessingManager {

    // ── In-memory state (server-only) ─────────────────────────────────────────

    /** Maps River owner UUID → set of blessed player UUIDs. */
    private static final Map<UUID, Set<UUID>> blessingsByOwner = new ConcurrentHashMap<>();

    /** Reverse lookup: blessed player UUID → owner UUID. */
    private static final Map<UUID, UUID> blessedToOwner = new ConcurrentHashMap<>();
    // ── Persistence ─────────────────────────────────────────────────────────────────

    /**
     * Populates the in-memory maps from the SavedData store.
     * Call once on server start / world load.
     */
    public static void loadFromData(MinecraftServer server) {
        blessingsByOwner.clear();
        blessedToOwner.clear();
        RiverOfEternalDarknessData data = RiverOfEternalDarknessData.get(server);
        for (var entry : data.getAllBlessings().entrySet()) {
            UUID owner = entry.getKey();
            Set<UUID> set = ConcurrentHashMap.newKeySet();
            set.addAll(entry.getValue());
            blessingsByOwner.put(owner, set);
            for (UUID t : entry.getValue()) blessedToOwner.put(t, owner);
        }
    }
    // ── Sequence-based limit ──────────────────────────────────────────────────

    /**
     * Returns the maximum number of players the River owner may bless,
     * given their current sequence.
     *
     * @param ownerSequence the River owner's sequence number (lower = stronger)
     */
    public static int getMaxBlessings(int ownerSequence) {
        if (ownerSequence <= 2) return 2;   // Seq 2 or stronger (seq 2, 1, 0, GOO)
        if (ownerSequence <= 4) return 1;   // Seq 4 or 3
        return 0;                            // Below seq 4 → no blessings
    }

    // ── Blessing management ───────────────────────────────────────────────────

    /**
     * Attempts to bless {@code targetUUID} on behalf of {@code owner}.
     *
     * @return {@code true} if the blessing was applied; {@code false} if the
     *         owner has no available slots or the target is already blessed.
     */
    public static boolean blessPlayer(ServerPlayer owner, UUID targetUUID) {
        int max = getMaxBlessings(BeyonderData.getSequence(owner));
        if (max == 0) return false;

        Set<UUID> blessed = blessingsByOwner.computeIfAbsent(owner.getUUID(),
                k -> ConcurrentHashMap.newKeySet());

        if (blessed.contains(targetUUID)) return false; // already blessed
        if (blessed.size() >= max) return false;        // no free slots

        blessed.add(targetUUID);
        blessedToOwner.put(targetUUID, owner.getUUID());
        RiverOfEternalDarknessData.get(owner.getServer()).addBlessing(owner.getUUID(), targetUUID);
        return true;
    }

    /**
     * Removes the blessing from {@code targetUUID}.  Safe to call even if the
     * target is not currently blessed.
     */
    public static void unblessPlayer(UUID ownerUUID, UUID targetUUID) {
        Set<UUID> blessed = blessingsByOwner.get(ownerUUID);
        if (blessed != null) {
            blessed.remove(targetUUID);
            if (blessed.isEmpty()) blessingsByOwner.remove(ownerUUID);
        }
        blessedToOwner.remove(targetUUID);
    }

    /**
     * Removes the blessing from {@code targetUUID} and persists the change.
     */
    public static void unblessPlayer(UUID ownerUUID, UUID targetUUID, MinecraftServer server) {
        unblessPlayer(ownerUUID, targetUUID);
        if (server != null) RiverOfEternalDarknessData.get(server).removeBlessing(ownerUUID, targetUUID);
    }

    /**
     * Returns whether {@code playerUUID} is currently under a River blessing.
     */
    public static boolean isBlessed(UUID playerUUID) {
        return blessedToOwner.containsKey(playerUUID);
    }

    /**
     * Returns the River owner UUID who blessed this player, or null if not blessed.
     */
    public static UUID getOwner(UUID blessedUUID) {
        return blessedToOwner.get(blessedUUID);
    }

    /**
     * Returns the UUIDs of all players blessed by the given owner.
     */
    public static Set<UUID> getBlessedByOwner(UUID ownerUUID) {
        return Collections.unmodifiableSet(
                blessingsByOwner.getOrDefault(ownerUUID, Collections.emptySet()));
    }

    /**
     * Removes all blessings granted by {@code ownerUUID}.
     * Called when the owner permanently loses the River sefirot.
     */
    public static void clearBlessingsForOwner(UUID ownerUUID) {
        Set<UUID> blessed = blessingsByOwner.remove(ownerUUID);
        if (blessed != null) {
            for (UUID uuid : blessed) blessedToOwner.remove(uuid);
        }
    }

    /**
     * Clears blessings from memory AND from the saved data store.
     */
    public static void clearBlessingsForOwner(UUID ownerUUID, MinecraftServer server) {
        clearBlessingsForOwner(ownerUUID);
        if (server != null) RiverOfEternalDarknessData.get(server).clearBlessingsByOwner(ownerUUID);
    }

    // ── River audience ────────────────────────────────────────────────────────

    /** Audience safe landing spot (above the fluid, separate from River's Call trap zone). */
    public static final double AUDIENCE_X = 0;
    public static final double AUDIENCE_Y = 65;
    public static final double AUDIENCE_Z = -50;

    /** Players currently present in the river as the owner's invited audience. */
    private static final Set<UUID> CURRENTLY_IN_AUDIENCE = ConcurrentHashMap.newKeySet();

    /** Return locations saved when each audience member was summoned. */
    private static final Map<UUID, CompoundTag> AUDIENCE_RETURN_LOCATIONS = new ConcurrentHashMap<>();

    public static boolean isInAudience(UUID uuid) {
        return CURRENTLY_IN_AUDIENCE.contains(uuid);
    }

    public static void markInAudience(ServerPlayer player) {
        CompoundTag loc = new CompoundTag();
        loc.putDouble("x", player.getX());
        loc.putDouble("y", player.getY());
        loc.putDouble("z", player.getZ());
        loc.putString("dim", player.level().dimension().location().toString());
        AUDIENCE_RETURN_LOCATIONS.put(player.getUUID(), loc);
        CURRENTLY_IN_AUDIENCE.add(player.getUUID());
    }

    public static void unmarkFromAudience(UUID uuid) {
        CURRENTLY_IN_AUDIENCE.remove(uuid);
        AUDIENCE_RETURN_LOCATIONS.remove(uuid);
    }

    /**
     * Teleports all online blessed players to the river audience spot.
     * Called by {@code RiverAudienceActionPacket} with action SUMMON.
     */
    public static void summonBlessedToAudience(ServerPlayer owner, MinecraftServer server) {
        ServerLevel riverLevel = server.getLevel(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY);
        if (riverLevel == null) {
            owner.sendSystemMessage(Component.literal("§cRiver dimension not found."));
            return;
        }

        Set<UUID> blessed = getBlessedByOwner(owner.getUUID());
        int slot = 0;
        for (UUID uuid : blessed) {
            ServerPlayer target = server.getPlayerList().getPlayer(uuid);
            if (target == null || isInAudience(uuid)) continue;
            markInAudience(target);
            // Spread audience members along Z so they don't overlap
            target.teleportTo(riverLevel, AUDIENCE_X, AUDIENCE_Y, AUDIENCE_Z - (slot * 3.0), 0f, 0f);
            target.sendSystemMessage(Component.literal(
                    "You have been summoned to the River of Eternal Darkness.").withStyle(ChatFormatting.DARK_AQUA));
            slot++;
        }

        if (slot > 0) {
            owner.sendSystemMessage(Component.literal(
                    "§8Summoned " + slot + " blessed player" + (slot != 1 ? "s" : "") + " to the River."));
        } else {
            owner.sendSystemMessage(Component.literal("§cNo blessed players are currently online."));
        }
    }

    /**
     * Returns all current audience members to where they came from.
     * Called by {@code RiverAudienceActionPacket} with action DISMISS.
     */
    public static void dismissAudience(MinecraftServer server) {
        int count = 0;
        for (UUID uuid : new HashSet<>(CURRENTLY_IN_AUDIENCE)) {
            ServerPlayer target = server.getPlayerList().getPlayer(uuid);
            if (target != null) {
                returnAudienceMember(target, server);
                count++;
            } else {
                unmarkFromAudience(uuid);
            }
        }
    }

    /**
     * Teleports a single audience member back to their saved pre-summon location.
     */
    public static void returnAudienceMember(ServerPlayer player, MinecraftServer server) {
        CompoundTag loc = AUDIENCE_RETURN_LOCATIONS.get(player.getUUID());
        if (loc != null) {
            String dimStr = loc.getString("dim");
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(dimStr));
            ServerLevel level = server.getLevel(dimKey);
            if (level == null) level = server.overworld();
            player.teleportTo(level,
                    loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z"),
                    player.getYRot(), player.getXRot());
        } else {
            // Fallback: overworld spawn
            net.minecraft.core.BlockPos spawn = server.overworld().getSharedSpawnPos();
            player.teleportTo(server.overworld(),
                    spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
        }
        unmarkFromAudience(player.getUUID());
        player.sendSystemMessage(Component.literal(
                "You have been dismissed from the River.").withStyle(ChatFormatting.GRAY));
    }

    /**
     * Clears all audience members (sends them home). Called when the owner loses the River sefirot.
     */
    public static void clearAudience(MinecraftServer server) {
        dismissAudience(server);
    }

    // ── Divination blocking ───────────────────────────────────────────────────

    /**
     * Returns {@code true} if a divination attempt on {@code targetUUID} by
     * {@code diviner} should be blocked because the target is River-blessed.
     *
     * Rule: the blessing blocks diviners at the same sequence as the blessed
     * player or up to 3 sequences above them (i.e. lower sequence number).
     * Only diviners who are MORE than 3 sequences above
     * (divinerSeq &lt;= blessedSeq - 4) can pierce the protection.
     */
    public static boolean blocksDivination(UUID targetUUID, ServerPlayer diviner) {
        if (!blessedToOwner.containsKey(targetUUID)) return false;

        int divinerSeq = BeyonderData.getSequence(diviner);
        int targetSeq = BeyonderData.playerMap != null
                ? BeyonderData.playerMap.get(targetUUID).map(d -> d.sequence()).orElse(LOTMCraft.NON_BEYONDER_SEQ)
                : LOTMCraft.NON_BEYONDER_SEQ;

        if (targetSeq == LOTMCraft.NON_BEYONDER_SEQ) return false;

        // Allow piercing only if diviner is MORE than 3 sequences stronger.
        // divinerSeq <= targetSeq - 4  →  allowed (can divine)
        // divinerSeq >  targetSeq - 4  →  blocked
        return divinerSeq > targetSeq - 4;
    }
}
