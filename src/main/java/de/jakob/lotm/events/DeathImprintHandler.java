package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DeathImprintHandler {

    /** River water effect interval: every 20 ticks (1 second). */
    private static final int RIVER_EFFECT_INTERVAL = 20;

    /** HP per-second damage percentage (5% of max health). */
    private static final float HP_DRAIN_PERCENT = 0.05f;

    /** Sanity/spirituality per-second drain (10% of max). */
    private static final float SANITY_DRAIN_PER_SECOND = 0.10f;

    /** Duration for River's Call trap in ticks (10 seconds). */
    private static final int RIVERS_CALL_DURATION_TICKS = 200;

    // ── Death event ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof ServerPlayer dyingPlayer)) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        DeathImprintData data = DeathImprintData.get(serverLevel.getServer());

        // Save snapshot data for GUI display and NPC
        String pathway = BeyonderData.getPathway(dyingPlayer);
        int sequence = BeyonderData.getSequence(dyingPlayer);
        String name = dyingPlayer.getGameProfile().getName();
        data.saveSnapshot(dyingPlayer.getUUID(), name, pathway, sequence);

        // Add one death imprint (capped at 3)
        int newTier = data.addImprint(dyingPlayer.getUUID());

        // Notify river owner if online
        for (ServerPlayer online : serverLevel.getServer().getPlayerList().getPlayers()) {
            if ("river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(online))) {
                online.sendSystemMessage(Component.literal(String.format(
                        "§5%s§8 has gained a Death Imprint (Tier %d).", name, newTier)));
            }
        }

        // If the player died inside the river dimension, mark as permanent river soul
        if (serverLevel.dimension().equals(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY)) {
            data.addPermanentRiverSoul(dyingPlayer.getUUID());
        }

        // If they died while trapped by River's Call, deliver their soul to any online death-path player
        if (data.isTrappedInRiver(dyingPlayer.getUUID())) {
            for (ServerPlayer online : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (de.jakob.lotm.abilities.death.InternalUnderworldAbility.tryCaptureRiverVictim(online, dyingPlayer)) {
                    break; // only the first eligible death-path player receives the soul
                }
            }
        }
    }

    // ── Logout event: kill if trapped in river ─────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        DeathImprintData data = DeathImprintData.get(serverLevel.getServer());
        if (data.isTrappedInRiver(player.getUUID())) {
            // Kill the player for leaving while trapped
            player.hurt(serverLevel.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            data.releaseFromRiver(player.getUUID());
        }
    }

    // ── Server tick: river effects + trap enforcement ──────────────────────────

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();

        DeathImprintData data = DeathImprintData.get(event.getServer());

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : List.copyOf(level.players())) {
                UUID uuid = player.getUUID();

                boolean inRiverDim = player.level().dimension()
                        .equals(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY);

                // River water effects: deal sanity / spirituality / HP drain (every 20 ticks)
                if (tick % RIVER_EFFECT_INTERVAL == 0) {
                    if (inRiverDim && isInRiverFluid(player)) {
                        applyRiverWaterEffects(player, data);
                    }
                }

                // Trap enforcement: runs every tick to freeze player in place
                if (data.isTrappedInRiver(uuid)) {
                    long expiry = data.getTrapExpiryTick(uuid);
                    if (tick >= expiry) {
                        // Trap expired — release
                        data.releaseFromRiver(uuid);
                        player.sendSystemMessage(Component.literal("§8The grip of the River releases you..."));
                    } else if (!inRiverDim) {
                        // They left the dimension — kill them
                        player.hurt(player.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                        data.releaseFromRiver(uuid);
                    } else {
                        // Freeze player at their trap position
                        net.minecraft.core.BlockPos trapPos = data.getTrapPosition(uuid);
                        if (trapPos != null) {
                            double tx = trapPos.getX() + 0.5;
                            double ty = trapPos.getY();
                            double tz = trapPos.getZ() + 0.5;
                            if (player.distanceToSqr(tx, ty, tz) > 0.25) {
                                player.teleportTo(tx, ty, tz);
                            }
                            player.setDeltaMovement(0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static boolean isInRiverFluid(ServerPlayer player) {
        FluidState fluidState = player.level().getFluidState(player.blockPosition());
        return fluidState.getType() == ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get()
                || fluidState.getType() == ModFluids.DROPS_OF_ETERNAL_DARKNESS_FLOWING.get();
    }

    private static void applyRiverWaterEffects(ServerPlayer player, DeathImprintData data) {
        // HP damage: 5% of max health per second
        float maxHp = player.getMaxHealth();
        player.hurt(player.level().damageSources().magic(), maxHp * HP_DRAIN_PERCENT);

        // Sanity drain: 10% per second
        SanityComponent sanity = player.getData(ModAttachments.SANITY_COMPONENT);
        sanity.decreaseSanityAndSync(SANITY_DRAIN_PER_SECOND, player);

        // Spirituality drain: 10% of max per second
        float maxSpirit = BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player), player);
        if (maxSpirit > 0) {
            BeyonderData.reduceSpirituality(player, maxSpirit * SANITY_DRAIN_PER_SECOND);
        }
    }

    /**
     * Called by {@code RiverAuthorityActionPacket} handler to execute River's Call on a target.
     * Teleports the target into the river water and traps them for 10 seconds.
     */
    public static void executeRiversCall(ServerPlayer riverOwner, UUID targetUUID) {
        if (riverOwner.getServer() == null) return;
        DeathImprintData data = DeathImprintData.get(riverOwner.getServer());

        if (data.getImprintCount(targetUUID) < 3) {
            riverOwner.sendSystemMessage(Component.literal("§cThis player requires 3 death imprints to use River's Call."));
            return;
        }

        ServerPlayer target = riverOwner.getServer().getPlayerList().getPlayer(targetUUID);
        if (target == null) {
            riverOwner.sendSystemMessage(Component.literal("§cTarget player is not online."));
            return;
        }

        ServerLevel riverLevel = riverOwner.getServer().getLevel(ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY);
        if (riverLevel == null) {
            riverOwner.sendSystemMessage(Component.literal("§cRiver dimension not found."));
            return;
        }

        // Teleport to river center where fluid exists (Y = 62 is inside the fluid channel at Y 61-63)
        target.teleportTo(riverLevel, 0.5, 62, 0.5, target.getYRot(), target.getXRot());
        data.trapInRiver(targetUUID, riverOwner.getServer().getTickCount() + RIVERS_CALL_DURATION_TICKS);
        data.setTrapPosition(targetUUID, new net.minecraft.core.BlockPos(0, 62, 0));

        target.sendSystemMessage(Component.literal("§4§lYou have been pulled into the River of Eternal Darkness!"));
        riverOwner.sendSystemMessage(Component.literal("§8River's Call executed on §r" + data.getSnapshotName(targetUUID)));
    }

    /**
     * Called by {@code RiverAuthorityActionPacket} handler to teleport the river owner to a target player.
     * Requires imprint tier ≥ 2.
     */
    public static void executeLocate(ServerPlayer riverOwner, UUID targetUUID) {
        if (riverOwner.getServer() == null) return;
        DeathImprintData data = DeathImprintData.get(riverOwner.getServer());

        int tier = data.getImprintCount(targetUUID);
        if (tier < 2) {
            riverOwner.sendSystemMessage(Component.literal("§cThis player requires 2 death imprints for direct teleportation."));
            return;
        }

        ServerPlayer target = riverOwner.getServer().getPlayerList().getPlayer(targetUUID);
        if (target == null) {
            riverOwner.sendSystemMessage(Component.literal("§cTarget player is not online."));
            return;
        }

        ServerLevel targetLevel = (ServerLevel) target.level();
        riverOwner.teleportTo(targetLevel, target.getX(), target.getY(), target.getZ(),
                riverOwner.getYRot(), riverOwner.getXRot());
        riverOwner.sendSystemMessage(Component.literal("§8You phase toward §r" + target.getGameProfile().getName()));
    }
}
