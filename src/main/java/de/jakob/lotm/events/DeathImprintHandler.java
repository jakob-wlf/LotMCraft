package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
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

    /** Sanity/spirituality per-second drain (10% of max). */
    private static final float SANITY_DRAIN_PER_SECOND = 0.10f;

    /** Duration for River's Call trap in ticks (10 seconds). */
    private static final int RIVERS_CALL_DURATION_TICKS = 200;

    /** Grace period in ticks after trapping before dimension-check enforcement starts. */
    private static final int RIVERS_CALL_GRACE_TICKS = 20;

    /** Return positions saved before River's Call teleport — used to send players back on release. */
    private static final Map<UUID, ServerLevel> trapReturnLevel = new HashMap<>();
    private static final Map<UUID, double[]>    trapReturnCoords = new HashMap<>();

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
        // Reset the 12-hour real-time decay timer from the moment of this imprint.
        data.scheduleDecay(dyingPlayer.getUUID());

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

        // If they died while trapped by River's Call, release the trap and deliver their soul
        if (data.isTrappedInRiver(dyingPlayer.getUUID())) {
            data.releaseFromRiver(dyingPlayer.getUUID());
            trapReturnLevel.remove(dyingPlayer.getUUID());
            trapReturnCoords.remove(dyingPlayer.getUUID());
            for (ServerPlayer online : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (de.jakob.lotm.abilities.death.InternalUnderworldAbility.tryCaptureRiverVictim(online, dyingPlayer)) {
                    break; // only the first eligible death-path player receives the soul
                }
            }
        }
    }

    // ── Logout event: kill if trapped in river ────────────────────────────────

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

        // ── Imprint decay: check once per second (uses real-time 12hr timer) ───
        if (tick % 20 == 0) {
            List<java.util.UUID> decayed = data.tickDecay(event.getServer());
            for (java.util.UUID uuid : decayed) {
                int remaining = data.getImprintCount(uuid);
                // Notify the affected player if online
                net.minecraft.server.level.ServerPlayer affected =
                        event.getServer().getPlayerList().getPlayer(uuid);
                if (affected != null) {
                    if (remaining <= 0) {
                        affected.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§6The River of Eternal Darkness fades from your soul. Your imprints have been cleansed."));
                    } else {
                        affected.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§6The River's hold on you weakens. Death imprints remaining: §e" + remaining));
                    }
                }
                // Notify the river owner if online
                for (net.minecraft.server.level.ServerPlayer online :
                        event.getServer().getPlayerList().getPlayers()) {
                    if ("river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(online))) {
                        String name = data.getSnapshotName(uuid);
                        online.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§5" + name + "§8's imprint has decayed. Their tier is now §e" + remaining + "§8."));
                    }
                }
            }
        }

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
                        // Trap expired — release and teleport back
                        data.releaseFromRiver(uuid);
                        player.sendSystemMessage(Component.literal("§8The grip of the River releases you..."));
                        ServerLevel returnLevel = trapReturnLevel.remove(uuid);
                        double[] coords = trapReturnCoords.remove(uuid);
                        if (returnLevel != null && coords != null) {
                            player.teleportTo(returnLevel, coords[0], coords[1], coords[2],
                                    java.util.Set.of(), player.getYRot(), player.getXRot());
                        } else {
                            net.minecraft.core.BlockPos spawn = player.server.overworld().getSharedSpawnPos();
                            player.teleportTo(player.server.overworld(),
                                    spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                                    java.util.Set.of(), player.getYRot(), player.getXRot());
                        }
                    } else if (!inRiverDim) {
                        // Allow a grace period for the cross-dimensional teleport to commit
                        if (expiry - tick <= RIVERS_CALL_DURATION_TICKS - RIVERS_CALL_GRACE_TICKS) {
                            // Release BEFORE hurting so anti-death abilities cannot loop the enforcement kill
                            data.releaseFromRiver(uuid);
                            trapReturnLevel.remove(uuid);
                            trapReturnCoords.remove(uuid);
                            player.hurt(player.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                        }
                    } else {
                        // Strip Regeneration effect every tick so it cannot outheal the river damage
                        player.removeEffect(MobEffects.REGENERATION);

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
        // HP drain: flat 10% of max health per second, floors at 1 heart (2 HP)
        float maxHp = player.getMaxHealth();
        float newHp = Math.max(2.0f, player.getHealth() - maxHp * 0.10f);
        player.setHealth(newHp);

        // Drain food saturation to prevent natural HP regeneration — only for seq 5+
        // (seq <= 4 are Angels; their passive healing is intentionally stronger)
        int seq = BeyonderData.getSequence(player);
        if (!BeyonderData.isBeyonder(player) || seq > 4) {
            player.causeFoodExhaustion(8f);
        }

        // Sanity drain: flat 10% per second (100→90→80→...→1), floor at 1% so it never fully zeroes
        SanityComponent sanity = player.getData(ModAttachments.SANITY_COMPONENT);
        float newSanity = Math.max(0.01f, sanity.getSanity() - 0.10f);
        sanity.setSanityAndSync(newSanity, player);

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

        // Save return position BEFORE trapping so we can send them back on release
        trapReturnLevel.put(targetUUID, (ServerLevel) target.level());
        trapReturnCoords.put(targetUUID, new double[]{target.getX(), target.getY(), target.getZ()});

        // Register the trap BEFORE teleporting so the dimension-change guard sees it
        data.trapInRiver(targetUUID, riverOwner.getServer().getTickCount() + RIVERS_CALL_DURATION_TICKS);
        data.setTrapPosition(targetUUID, new net.minecraft.core.BlockPos(0, 62, 0));

        // Teleport to river center where fluid exists (Y = 62 is inside the fluid channel at Y 61-63)
        target.teleportTo(riverLevel, 0.5, 62, 0.5, java.util.Set.of(), target.getYRot(), target.getXRot());

        target.sendSystemMessage(Component.literal("§4§lYou have been pulled into the River of Eternal Darkness!"));
        riverOwner.sendSystemMessage(Component.literal("§8River's Call executed on §r" + data.getSnapshotName(targetUUID)));

        // Decrement imprint count by 1 — River's Call consumes one imprint to prevent spam.
        int newTier = data.getImprintCount(targetUUID) - 1;
        data.setImprintCount(targetUUID, newTier);
        if (newTier <= 0) {
            target.sendSystemMessage(Component.literal("§6The River's grip on your soul has faded. You are no longer imprinted."));
        } else {
            target.sendSystemMessage(Component.literal("§6The River has consumed one of its marks upon you. Imprints remaining: §e" + newTier));
        }
        riverOwner.sendSystemMessage(Component.literal("§8Imprint consumed. " + data.getSnapshotName(targetUUID) + "§8 now has §e" + newTier + "§8 imprint(s)."));
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
                java.util.Set.of(), riverOwner.getYRot(), riverOwner.getXRot());
        riverOwner.sendSystemMessage(Component.literal("§8You phase toward §r" + target.getGameProfile().getName()));
    }
    // ── Ability seal enforcement ───────────────────────────────────────────────

    /**
     * Cancels an ability use if the player has that ability sealed by the River authority.
     * Only applies when the player has ≥2 death imprints and seals are active.
     */
    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        if (event.isCanceled()) return;
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide()) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        DeathImprintData data = DeathImprintData.get(serverLevel.getServer());
        UUID uuid = entity.getUUID();

        // Only applies when the player has ≥2 imprints
        if (data.getImprintCount(uuid) < 2) return;

        String abilityId = event.getAbility().getId();
        if (data.isAbilitySealed(uuid, abilityId)) {
            event.setCanceled(true);
            if (entity instanceof ServerPlayer player) {
                de.jakob.lotm.util.helper.AbilityUtil.sendActionBar(player,
                        Component.literal("\u00a74\u2728 This ability is sealed by the River of Eternal Darkness."));
            }
        }
    }}
