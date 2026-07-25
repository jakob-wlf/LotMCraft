package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ServerLocation;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class SefrotInvasionManager {
    private static final String WHEEL_MIGRATION_TAG = "lotm_sefrot_invasion_wheel_migrated";
    private static final String AUTHORITY_SEAL_CAUSE = "sefrot_invasion";
    private static final long INVASION_WINDOW_TICKS = 15L * 20L;
    private static final double INVASION_RANGE_SQUARED = 30.0 * 30.0;
    private static final int MIN_TELEPORT_DISTANCE = 16;
    private static final int MAX_TELEPORT_DISTANCE = 64;
    private static final int TELEPORT_ATTEMPTS = 64;

    private static final Map<UUID, EntryOpportunity> ENTRY_OPPORTUNITIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Invasion> ACTIVE_INVASIONS = new ConcurrentHashMap<>();

    private SefrotInvasionManager() {
    }

    public static void recordOwnerEntry(ServerPlayer owner) {
        String sefirot = SefirahHandler.getClaimedSefirot(owner);
        ServerLocation origin = SefirotData.get(owner.server).getReturnLocationForPlayer(owner);
        if (sefirot.isEmpty() || origin == null) return;

        ENTRY_OPPORTUNITIES.put(owner.getUUID(), new EntryOpportunity(
                owner.getUUID(), sefirot, origin.getLevel().dimension(), origin.getPosition(),
                owner.serverLevel().getGameTime() + INVASION_WINDOW_TICKS));
    }

    public static void tryInvade(ServerPlayer invader) {
        if (!BeyonderData.isBeyonder(invader) || BeyonderData.getSequence(invader) > 9) return;
        if (ACTIVE_INVASIONS.containsKey(invader.getUUID())) {
            notify(invader, "You are already part of a Sefrot invasion.");
            return;
        }

        long now = invader.serverLevel().getGameTime();
        ENTRY_OPPORTUNITIES.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);

        EntryOpportunity opportunity = ENTRY_OPPORTUNITIES.values().stream()
                .filter(entry -> canInvade(invader, entry, now))
                .min(Comparator.comparingDouble(entry -> entry.origin().distanceToSqr(invader.position())))
                .orElse(null);

        if (opportunity == null) {
            notify(invader, "No neighboring Sefrot can be invaded nearby.");
            return;
        }

        ServerPlayer defender = invader.server.getPlayerList().getPlayer(opportunity.ownerId());
        if (defender == null) return;

        ResourceKey<Level> sefirotDimension = SefirahHandler.getSefirotDimensionKey(opportunity.sefirot());
        if (sefirotDimension == null || !defender.level().dimension().equals(sefirotDimension)) return;

        BlockPos destination = findSafeInvasionPosition(
            defender.serverLevel(), defender.blockPosition(), invader.getRandom());
        if (destination == null) {
            notify(invader, "The Sefrot could not find a safe place for you to enter.");
            return;
        }

        Invasion invasion = new Invasion(defender.getUUID(), invader.getUUID(), opportunity.sefirot(), sefirotDimension);
        ACTIVE_INVASIONS.put(defender.getUUID(), invasion);
        ACTIVE_INVASIONS.put(invader.getUUID(), invasion);
        ENTRY_OPPORTUNITIES.remove(defender.getUUID());

        SefirotData data = SefirotData.get(invader.server);
        data.setLastReturnLocation(invader);
        data.setIsInSefirot(invader.getUUID(), true);
        invader.teleportTo(defender.serverLevel(), destination.getX() + 0.5, destination.getY(),
            destination.getZ() + 0.5, defender.getYRot(), 0);

        sealAuthority(defender);
        setOpponentGlow(defender, invader, true);
        setOpponentGlow(invader, defender, true);
        announceInvasion(invader.server, invasion, invader, defender);
    }

    public static boolean isDefenderLocked(ServerPlayer player) {
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        return invasion != null && invasion.defenderId().equals(player.getUUID());
    }

    public static boolean isActiveParticipant(UUID playerId) {
        return ACTIVE_INVASIONS.containsKey(playerId);
    }

    /** Include this check in new Sefrot dimension entry guards alongside their normal authorization rules. */
    public static boolean isAuthorizedInvasionEntry(ServerPlayer player, ResourceKey<Level> targetDimension) {
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        return invasion != null && invasion.dimension().equals(targetDimension);
    }

    public static boolean forfeitForResurrection(ServerPlayer player) {
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        if (invasion == null) return false;

        ServerLocation returnLocation = SefirotData.get(player.server).getReturnLocationForPlayer(player);
        finishInvasion(player.server, invasion, player.getUUID(), "attempted to resurrect");

        player.setHealth(player.getMaxHealth());
        player.deathTime = 0;
        player.hurtTime = 0;
        player.invulnerableTime = 0;
        player.fallDistance = 0;

        if (returnLocation != null && !returnLocation.getLevel().dimension().equals(invasion.dimension())) {
            Vec3 position = returnLocation.getPosition();
            player.teleportTo(returnLocation.getLevel(), position.x, position.y, position.z,
                    player.getYRot(), player.getXRot());
        } else {
            ServerLevel overworld = player.server.overworld();
            Vec3 spawn = overworld.getSharedSpawnPos().getCenter();
            player.teleportTo(overworld, spawn.x, spawn.y, spawn.z, player.getYRot(), player.getXRot());
        }

        player.sendSystemMessage(Component.literal(
                "Resurrection is forbidden during a Sefrot invasion. You have been defeated.")
                .withStyle(ChatFormatting.DARK_RED));
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;

        if (!player.getPersistentData().getBoolean(WHEEL_MIGRATION_TAG)) {
            SefirahHandler.removeSefrotInvasionAbility(player);
            player.getPersistentData().putBoolean(WHEEL_MIGRATION_TAG, true);
        }

        boolean eligible = BeyonderData.isBeyonder(player)
                && BeyonderData.getSequence(player) <= 9
                && !SefirahHandler.hasSefirot(player);
        if (!eligible) {
            SefirahHandler.removeSefrotInvasionAbility(player);
        }

        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        if (invasion == null) return;

        ServerPlayer opponent = player.server.getPlayerList().getPlayer(invasion.other(player.getUUID()));
        if (opponent != null && opponent.level().dimension().equals(player.level().dimension())) {
            setOpponentGlow(opponent, player, true);
        }
    }

    private static boolean canInvade(ServerPlayer invader, EntryOpportunity entry, long now) {
        if (entry.expiresAt() < now || entry.ownerId().equals(invader.getUUID())) return false;
        if (!entry.originDimension().equals(invader.level().dimension())) return false;
        if (entry.origin().distanceToSqr(invader.position()) > INVASION_RANGE_SQUARED) return false;
        if (ACTIVE_INVASIONS.containsKey(entry.ownerId())) return false;
        if (!SefirotAuthorityManager.NEIGHBORING_PATHS.getOrDefault(entry.sefirot(), java.util.List.of())
                .contains(BeyonderData.getPathway(invader))) return false;

        ServerPlayer owner = invader.server.getPlayerList().getPlayer(entry.ownerId());
        return owner != null
                && SefirotData.get(invader.server).isInSefirot(owner)
                && entry.sefirot().equals(SefirahHandler.getClaimedSefirot(owner));
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        if (invasion == null || event.getTo().equals(invasion.dimension())) return;
        finishInvasion(player.server, invasion, player.getUUID(), "left the Sefirah");
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        if (invasion != null) finishInvasion(player.server, invasion, player.getUUID(), "died");
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ENTRY_OPPORTUNITIES.remove(player.getUUID());
        Invasion invasion = ACTIVE_INVASIONS.get(player.getUUID());
        if (invasion != null) finishInvasion(player.server, invasion, player.getUUID(), "disconnected");
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).clearCause(AUTHORITY_SEAL_CAUSE);
    }

    private static void finishInvasion(MinecraftServer server, Invasion invasion, UUID loserId, String reason) {
        if (!ACTIVE_INVASIONS.remove(invasion.defenderId(), invasion)) return;
        ACTIVE_INVASIONS.remove(invasion.invaderId(), invasion);

        UUID winnerId = invasion.other(loserId);
        SefirotData.get(server).setIsInSefirot(loserId, false);
        ServerPlayer winner = server.getPlayerList().getPlayer(winnerId);
        ServerPlayer loser = server.getPlayerList().getPlayer(loserId);
        ServerPlayer defender = server.getPlayerList().getPlayer(invasion.defenderId());
        if (defender != null) {
            restoreAuthority(defender);
        }
        if (winner == null) return;

        if (loser != null) {
            setOpponentGlow(loser, winner, false);
            setOpponentGlow(winner, loser, false);
        }

        if (!invasion.sefirot().equals(SefirahHandler.getClaimedSefirot(winner))) {
            SefirahHandler.transferOwnership(winner, loser, invasion.sefirot());
        }
        announceVictory(server, invasion, winner, loser, reason);
        if (loser != null) {
            AbilityUtil.sendActionBar(loser, Component.literal("SEFROT LOST")
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED));
        }
    }

    @Nullable
    private static BlockPos findSafeInvasionPosition(ServerLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = MIN_TELEPORT_DISTANCE
                    + random.nextDouble() * (MAX_TELEPORT_DISTANCE - MIN_TELEPORT_DISTANCE);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);

            if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, origin.getY(), z))) continue;
            level.getChunk(x >> 4, z >> 4);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (isSafeStandingPosition(level, feet)) return feet;
        }

        return isSafeStandingPosition(level, origin) ? origin : null;
    }

    private static boolean isSafeStandingPosition(ServerLevel level, BlockPos feet) {
        if (feet.getY() <= level.getMinBuildHeight() || feet.getY() >= level.getMaxBuildHeight() - 1) return false;

        BlockPos floor = feet.below();
        BlockState floorState = level.getBlockState(floor);
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(feet.above());
        boolean dangerousFloor = floorState.is(Blocks.MAGMA_BLOCK)
                || floorState.is(Blocks.CACTUS)
                || floorState.is(Blocks.CAMPFIRE)
                || floorState.is(Blocks.SOUL_CAMPFIRE);

        return !dangerousFloor
                && floorState.getFluidState().isEmpty()
                && floorState.isCollisionShapeFullBlock(level, floor)
                && feetState.getCollisionShape(level, feet).isEmpty()
                && headState.getCollisionShape(level, feet.above()).isEmpty()
                && feetState.getFluidState().isEmpty()
                && headState.getFluidState().isEmpty();
    }

    private static void announceInvasion(MinecraftServer server, Invasion invasion,
                                         ServerPlayer invader, ServerPlayer defender) {
        Component sefrotName = Component.literal(formatSefrotName(invasion.sefirot()))
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        Component message = Component.literal("[ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("SEFROT INVASION").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                .append(Component.literal(" ] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(sefrotName)
                .append(Component.literal(" is under siege: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(invader.getGameProfile().getName()).withStyle(ChatFormatting.RED))
                .append(Component.literal(" challenges ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(defender.getGameProfile().getName()).withStyle(ChatFormatting.GOLD));
        server.getPlayerList().broadcastSystemMessage(message, false);

        showTitle(defender,
            Component.literal("UNDER INVASION")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            Component.literal("[ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("INVADER").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(" ]  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(invader.getGameProfile().getName())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        showTitle(invader,
            Component.literal("SEFROT INVASION")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            Component.literal("[ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("TARGET").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" ]  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(defender.getGameProfile().getName())
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));

        defender.sendSystemMessage(Component.literal("Your exit is sealed until the invader is defeated.")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        invader.sendSystemMessage(Component.literal("Claim the Sefrot. Death or escape means defeat.")
                .withStyle(ChatFormatting.RED));
    }

    private static void announceVictory(MinecraftServer server, Invasion invasion, ServerPlayer winner,
                                        ServerPlayer loser, String reason) {
        String loserName = loser == null ? "the defeated challenger" : loser.getGameProfile().getName();
        Component message = Component.literal("[ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("SEFROT CLAIMED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" ] ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(winner.getGameProfile().getName()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(Component.literal(" claimed ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(formatSefrotName(invasion.sefirot())).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" after " + loserName + " " + reason + ".").withStyle(ChatFormatting.DARK_GRAY));
        server.getPlayerList().broadcastSystemMessage(message, false);

        showTitle(winner,
                Component.literal("VICTORY").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.literal(formatSefrotName(invasion.sefirot()) + " is yours")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (loser != null) {
            showTitle(loser,
                    Component.literal("DEFEAT").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    Component.literal("The Sefrot has a new owner").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void showTitle(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    private static void sealAuthority(ServerPlayer defender) {
        defender.closeContainer();
        DisabledAbilitiesComponent disabled = defender.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        disabled.clearCause(AUTHORITY_SEAL_CAUSE);
        disabled.disableSpecificAbility("sefirot_authority_ability", AUTHORITY_SEAL_CAUSE);
        for (String abilityId : SefirotAuthorityManager.getUnlockedAbilityIds(defender)) {
            disabled.disableSpecificAbility(abilityId, AUTHORITY_SEAL_CAUSE);
        }
        SefirotAuthorityManager.SEFIROT_DIVINATION_IMMUNE.remove(defender.getUUID());
        SefirotAuthorityManager.RIVER_CONCEALMENT_ACTIVE.remove(defender.getUUID());
    }

    private static void restoreAuthority(ServerPlayer defender) {
        defender.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).clearCause(AUTHORITY_SEAL_CAUSE);
        if (SefirahHandler.hasSefirot(defender)) {
            SefirotAuthorityManager.updatePlayerAuthority(defender);
        }
    }

    private static void setOpponentGlow(Entity target, ServerPlayer viewer, boolean glowing) {
        EntityDataAccessor<Byte> flagsAccessor = EntityAccessor.getSharedFlagsId();
        byte flags = target.getEntityData().get(flagsAccessor);
        if (glowing) flags |= 0x40;

        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(flagsAccessor, flags));
        viewer.connection.send(new ClientboundSetEntityDataPacket(target.getId(), values));
    }

    private static String formatSefrotName(String sefrot) {
        StringBuilder result = new StringBuilder();
        for (String word : sefrot.split("_")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static void notify(ServerPlayer player, String message) {
        AbilityUtil.sendActionBar(player, Component.literal(message));
    }

    private record EntryOpportunity(UUID ownerId, String sefirot, ResourceKey<Level> originDimension,
                                    Vec3 origin, long expiresAt) {
    }

    private record Invasion(UUID defenderId, UUID invaderId, String sefirot, ResourceKey<Level> dimension) {
        private UUID other(UUID playerId) {
            return defenderId.equals(playerId) ? invaderId : defenderId;
        }
    }
}