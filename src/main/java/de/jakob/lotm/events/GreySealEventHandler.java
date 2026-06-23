package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.attachments.GatheringData;
import de.jakob.lotm.attachments.GreySealData;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.packets.toClient.SyncGreyFogStatusPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sefirah.GreatOldOneManager;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the Grey Fog "Seal Surroundings" state:
 *  - Places a barrier ring at the 250-block perimeter.
 *  - Spawns grey-blue fog particles for captured players.
 *  - Blocks teleportation, dimension travel, and escape abilities for non-exempt players.
 *  - Gathering members + ATs/GOOs/sefirot-owners are fully exempt and may pass barriers freely.
 *  - Toggling OFF starts a 5-hour cooldown.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GreySealEventHandler {

    public static final int RADIUS = 250;

    /** Cooldown after manually toggling the seal off: 5 hours in milliseconds. */
    public static final long COOLDOWN_MS = 5L * 60L * 60L * 1_000L;

    /** Real-time epoch ms: when the seal cooldown expires (0 = no cooldown). */
    public static long cooldownExpiryMs = 0L;

    /** Particle colour: grey-blue (#7BB0D6 ≈ 0.48, 0.69, 0.84) */
    private static final DustParticleOptions FOG_PARTICLE =
            new DustParticleOptions(new Vector3f(0.48f, 0.69f, 0.84f), 1.5f);

    @Nullable
    public static GreySeal activeSeal = null;

    /** UUIDs of players currently inside the seal boundary (for transition detection). */
    private static final Set<UUID> playersInsideSeal = new HashSet<>();

    /** UUIDs of gathering members who were auto-given a Fool Card from the seal. */
    private static final Set<UUID> autoAntiDivMembers = new HashSet<>();

    // ─── Fool Effects persistent state ────────────────────────────────────────────
    public static long       foolEffectsExpiryTick = -1L;
    public static UUID       foolEffectsCasterUUID  = null;
    private static Vec3      foolEffectsCenter      = null;
    private static ServerLevel foolEffectsLevel     = null;

    private static final List<Holder<MobEffect>> BENEFICIAL_POOL = List.of(
            MobEffects.REGENERATION,    MobEffects.MOVEMENT_SPEED,  MobEffects.DIG_SPEED,
            MobEffects.DAMAGE_BOOST,    MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING,
            MobEffects.NIGHT_VISION,    MobEffects.ABSORPTION,      MobEffects.HEALTH_BOOST,
            MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP,          MobEffects.SLOW_FALLING);

    private static final List<Holder<MobEffect>> HARMFUL_POOL = List.of(
            MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN, MobEffects.WEAKNESS,
            MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.HUNGER,
            MobEffects.POISON,    MobEffects.WITHER,    MobEffects.LEVITATION, MobEffects.DARKNESS);

    // ─── Public API ───────────────────────────────────────────────────────────

    public static boolean isSealActive() {
        return activeSeal != null && activeSeal.active;
    }

    /** Returns true if the player is inside the seal's 400-block radius (XZ). */
    public static boolean isCaught(ServerPlayer player) {
        if (!isSealActive()) return false;
        if (!player.level().dimension().equals(activeSeal.level.dimension())) return false;
        double xzDist = xzDist(player.position(), activeSeal.center);
        return xzDist <= RADIUS + 1.0;
    }

    public static boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownExpiryMs;
    }

    public static long getCooldownRemainingMs() {
        return Math.max(0, cooldownExpiryMs - System.currentTimeMillis());
    }

    public static boolean isFoolEffectsActive(long currentTick) {
        return foolEffectsExpiryTick > 0 && currentTick < foolEffectsExpiryTick;
    }

    public static void activateFoolEffects(ServerPlayer caster, ServerLevel level) {
        foolEffectsCasterUUID = caster.getUUID();
        foolEffectsCenter     = caster.position();
        foolEffectsLevel      = level;
        foolEffectsExpiryTick = level.getGameTime() + 1200L; // 1 minute
        caster.sendSystemMessage(Component.literal("§8[Grey Fog] §7Fortune reversal active for 1 minute."));
    }

    private static boolean isFoolFriend(ServerPlayer player) {
        if (foolEffectsCasterUUID == null) return false;
        if (player.getUUID().equals(foolEffectsCasterUUID)) return true;
        if (player.getServer() == null) return false;
        return GatheringData.get(player.getServer()).isMember(foolEffectsCasterUUID, player.getUUID());
    }

    public static void activate(ServerPlayer caster, ServerLevel level) {
        if (activeSeal != null && activeSeal.active) {
            // Toggle off
            deactivate();
            caster.sendSystemMessage(Component.literal("§8[Grey Fog] §7The seal has been lifted. (5 h cooldown)"));
            return;
        }
        Vec3 center = caster.position();
        List<BlockPos> barriers = new CopyOnWriteArrayList<>();
        activeSeal = new GreySeal(caster.getUUID(), center, level, barriers);
        placeBarrierRingAsync(level, center, barriers);
        caster.sendSystemMessage(Component.literal("§8[Grey Fog] §7The seal is descending…"));
        saveData(level.getServer());
    }

    public static void deactivate() {
        if (activeSeal == null) return;
        MinecraftServer server = activeSeal.level.getServer();
        ServerLevel level = activeSeal.level;

        // Notify and clear tint for everyone inside
        for (UUID uid : playersInsideSeal) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(uid);
            if (p != null) {
                PacketDistributor.sendToPlayer(p, new SyncGreyFogStatusPacket(false));
            }
        }
        playersInsideSeal.clear();

        // Remove auto-applied Fool Cards from gathering members
        for (UUID uid : autoAntiDivMembers) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(uid);
            if (p != null) {
                p.getInventory().clearOrCountMatchingItems(
                        s -> s.is(ModItems.FOOL_Card.get()), 1, p.inventoryMenu.getCraftSlots());
            }
        }
        autoAntiDivMembers.clear();

        for (BlockPos pos : activeSeal.barriers) {
            if (level.isLoaded(pos) && level.getBlockState(pos).is(Blocks.BARRIER)) {
                level.removeBlock(pos, false);
            }
        }
        activeSeal.active = false;
        activeSeal = null;
        // Start 5-hour cooldown
        cooldownExpiryMs = System.currentTimeMillis() + COOLDOWN_MS;

        saveData(server);
    }

    /** Writes current seal state to the world's SavedData store. */
    private static void saveData(MinecraftServer server) {
        if (server == null) return;
        GreySealData data      = GreySealData.get(server);
        data.sealActive        = (activeSeal != null && activeSeal.active);
        data.cooldownExpiryMs  = cooldownExpiryMs;
        if (data.sealActive) {
            data.ownerUUID     = activeSeal.ownerUUID;
            data.centerX       = activeSeal.center.x;
            data.centerY       = activeSeal.center.y;
            data.centerZ       = activeSeal.center.z;
            data.dimensionKey  = activeSeal.level.dimension().location().toString();
        }
        data.setDirty();
    }

    // ─── Barrier placement ────────────────────────────────────────────────────

    private static void placeBarrierRingAsync(ServerLevel level, Vec3 center, List<BlockPos> barriers) {
        // 360 sample points (1 per degree), 4 blocks tall, batched 36 per tick
        int steps = 360;
        int batchSize = 36;
        int batches = steps / batchSize; // = 10 ticks total

        for (int b = 0; b < batches; b++) {
            final int batchStart = b * batchSize;
            ServerScheduler.scheduleDelayed(b, () -> {
                if (!isSealActive()) return;
                for (int i = batchStart; i < batchStart + batchSize && i < steps; i++) {
                    double angle = i * (2 * Math.PI / steps);
                    int bx = (int) Math.round(center.x + RADIUS * Math.cos(angle));
                    int bz = (int) Math.round(center.z + RADIUS * Math.sin(angle));
                    for (int dy = -1; dy <= 3; dy++) {
                        BlockPos pos = new BlockPos(bx, (int) center.y + dy, bz);
                        if (level.isLoaded(pos) && level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                            barriers.add(pos);
                        }
                    }
                }
            });
        }
    }

    // ─── Player tick: fog particles + push-back ───────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (!(player.level() instanceof ServerLevel sLevel)) return;

        long tick = sLevel.getGameTime();

        // ─── Fool Effects: persistent 1-minute aura (independent of the seal) ────────
        if (isFoolEffectsActive(tick) && foolEffectsLevel != null
                && sLevel.dimension().equals(foolEffectsLevel.dimension())) {
            double fdist = xzDist(player.position(), foolEffectsCenter);
            if (fdist <= RADIUS) {
                if (isFoolFriend(player)) {
                    // Owner / gathering member: convert harmful effects → beneficial each second
                    if (tick % 20 == 0) {
                        List<MobEffectInstance> snap = new ArrayList<>(player.getActiveEffects());
                        for (MobEffectInstance eff : snap) {
                            if (eff.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                                Holder<MobEffect> good = BENEFICIAL_POOL.get(
                                        player.getRandom().nextInt(BENEFICIAL_POOL.size()));
                                player.removeEffect(eff.getEffect());
                                player.addEffect(new MobEffectInstance(
                                        good, eff.getDuration(), eff.getAmplifier(), false, true));
                            }
                        }
                    }
                } else if (tick % 60 == 0) {
                    // Enemies: apply a random harmful effect every 3 seconds
                    Holder<MobEffect> bad = HARMFUL_POOL.get(
                            player.getRandom().nextInt(HARMFUL_POOL.size()));
                    player.addEffect(new MobEffectInstance(bad, 20 * 30, 0, false, true));
                }
            }
        }

        // ─── Sefirot-loss auto-revoke: if seal owner no longer holds Sefirah Castle ──
        if (tick % 200 == 0 && isSealActive()
                && sLevel.dimension().equals(activeSeal.level.dimension())) {
            String ownerSefirot = SefirotData.get(sLevel.getServer())
                    .getClaimedSefirot(activeSeal.ownerUUID);
            if (!"sefirah_castle".equals(ownerSefirot)) {
                for (ServerPlayer p : sLevel.getServer().getPlayerList().getPlayers()) {
                    p.sendSystemMessage(Component.literal(
                            "§8[Grey Fog] §7The Grey Fog seal has dissolved — authority was lost."));
                }
                deactivate();
            }
        }

        // ─── Seal: entry/exit tracking + fog particles + ring wall + push-back ───────
        if (!isSealActive()) return;
        if (!sLevel.dimension().equals(activeSeal.level.dimension())) return;

        double dist  = xzDist(player.position(), activeSeal.center);
        UUID   uid   = player.getUUID();
        boolean wasInside = playersInsideSeal.contains(uid);
        boolean nowInside = dist <= RADIUS;

        if (nowInside && !wasInside) {
            // ── Player has entered the seal ──────────────────────────────────
            playersInsideSeal.add(uid);
            player.sendSystemMessage(Component.literal("§8[Grey Fog] §bYou have entered the Grey Fog."));
            PacketDistributor.sendToPlayer(player, new SyncGreyFogStatusPacket(true));

            // Notify owner and gathering members
            String name = player.getName().getString();
            notifyOwnerAndMembers(sLevel, player,
                    Component.literal("§8[Grey Fog] §7" + name + " has entered the Grey Fog."));

            // Auto anti-divination: give a Fool Card to gathering members entering the seal
            boolean isMember = GatheringData.get(sLevel.getServer())
                    .isMember(activeSeal.ownerUUID, uid);
            boolean isOwner  = uid.equals(activeSeal.ownerUUID);
            if ((isMember || isOwner) && !autoAntiDivMembers.contains(uid)
                    && !player.getInventory().hasAnyOf(java.util.Set.of(ModItems.FOOL_Card.get()))) {
                player.addItem(new net.minecraft.world.item.ItemStack(ModItems.FOOL_Card.get()));
                autoAntiDivMembers.add(uid);
                player.sendSystemMessage(Component.literal(
                        "§8[Grey Fog] §7Anti-divination protection granted."));
            }

        } else if (!nowInside && wasInside) {
            // ── Player has left the seal ──────────────────────────────────────
            playersInsideSeal.remove(uid);
            player.sendSystemMessage(Component.literal("§8[Grey Fog] §7You have left the Grey Fog."));
            PacketDistributor.sendToPlayer(player, new SyncGreyFogStatusPacket(false));

            String name = player.getName().getString();
            notifyOwnerAndMembers(sLevel, player,
                    Component.literal("§8[Grey Fog] §7" + name + " has left the Grey Fog."));
        }

        // Fog particles every 10 ticks for players inside the seal
        if (tick % 10 == 0 && nowInside) {
            for (int i = 0; i < 12; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 30;
                double oy = (player.getRandom().nextDouble() - 0.5) * 10;
                double oz = (player.getRandom().nextDouble() - 0.5) * 30;
                activeSeal.level.sendParticles(player, FOG_PARTICLE, false,
                        player.getX() + ox, player.getEyeY() + oy, player.getZ() + oz,
                        1, 0.4, 0.4, 0.4, 0.01);
            }
        }

        // Ring wall particles: only when within 30 blocks of the boundary
        if (tick % 4 == 0 && dist >= RADIUS - 30 && dist <= RADIUS + 10) {
            spawnRingParticles(player);
        }

        // Push-back: non-exempt players are teleported back inside
        if (tick % 5 != 0) return;
        if (isExempt(player)) return;
        if (dist > RADIUS + 2) {
            double angle = Math.atan2(player.getZ() - activeSeal.center.z,
                                      player.getX() - activeSeal.center.x);
            double bx = activeSeal.center.x + (RADIUS - 2) * Math.cos(angle);
            double bz = activeSeal.center.z + (RADIUS - 2) * Math.sin(angle);
            player.teleportTo(bx, player.getY(), bz);
            player.sendSystemMessage(Component.literal("§8[Grey Fog] §7You cannot leave the sealed area."));
        }
    }

    /**
     * Sends {@code msg} to the seal owner (if online) and all gathering members
     * who are currently online, excluding {@code source} (who already got their own message).
     */
    private static void notifyOwnerAndMembers(ServerLevel level, ServerPlayer source, Component msg) {
        if (!isSealActive() || level.getServer() == null) return;
        GatheringData gd = GatheringData.get(level.getServer());
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (p.getUUID().equals(source.getUUID())) continue;
            boolean isOwner  = p.getUUID().equals(activeSeal.ownerUUID);
            boolean isMember = gd.isMember(activeSeal.ownerUUID, p.getUUID());
            if (isOwner || isMember) p.sendSystemMessage(msg);
        }
    }

    /**
     * Spawns 10 vertical particle columns spread across a ~50-degree arc
     * centered on the direction from the player toward the nearest point
     * of the seal boundary. Each column is a stack of particles spanning
     * from ground level to 12 blocks above, giving the appearance of
     * a solid barrier wall in front of the player.
     */
    private static void spawnRingParticles(ServerPlayer player) {
        // Angle from seal centre TO the player — the barrier is in that direction
        double towardBoundary = Math.atan2(player.getZ() - activeSeal.center.z,
                                           player.getX() - activeSeal.center.x);
        double baseY = player.getY();

        // Space columns every 5 world-units along the arc.
        // At R=400, 5-block spacing → angleStep = 5/400 = 0.0125 rad.
        // halfArc = 30/RADIUS so only the 30-block section of wall nearest the player renders.
        double halfArc   = 30.0 / RADIUS;  // ~4.3° — covers 60 blocks of wall arc
        double arcStep   = 5.0  / RADIUS;  // one column every 5 blocks

        for (double a = towardBoundary - halfArc; a <= towardBoundary + halfArc; a += arcStep) {
            double px = activeSeal.center.x + RADIUS * Math.cos(a);
            double pz = activeSeal.center.z + RADIUS * Math.sin(a);

            // Vertical column: sparse — one particle every 3 blocks
            for (int dy = -1; dy <= 12; dy += 3) {
                activeSeal.level.sendParticles(player, ModParticles.GREY_SEAL.get(), true,
                        px, baseY + dy, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    // ─── Block same-dimension teleports ───────────────────────────────────────

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (!isSealActive()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isCaught(player)) return;
        if (isExempt(player)) return;

        Vec3 dest = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        double destDist = xzDist(dest, activeSeal.center);
        if (destDist > RADIUS + 2) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§8[Grey Fog] §7Teleportation is sealed."));
        }
    }

    // ─── Block dimension travel ───────────────────────────────────────────────

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (!isSealActive()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isCaught(player)) return;
        if (isExempt(player)) return;

        // Allow sefirot owners to enter their own sefirot dimension
        String sefirot = SefirahHandler.getClaimedSefirot(player);
        ResourceKey<Level> sefirotDim = getSefirotDimension(sefirot);
        if (sefirotDim != null && event.getDimension().equals(sefirotDim)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal("§8[Grey Fog] §7Dimension travel is sealed."));
    }

    // ─── Block spirit-world & travelers-door abilities ────────────────────────

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        if (!isSealActive()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isCaught(player)) return;
        if (isExempt(player)) return;

        String id = event.getAbility().getId();
        if ("spirit_world_traversal_ability".equals(id) || "travelers_door_ability".equals(id)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§8[Grey Fog] §7That ability is sealed."));
        }
    }

    // ─── Logout: clean up tracking but keep seal active ──────────────────────

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uid = event.getEntity().getUUID();
        // Remove from inside-seal tracker so we re-detect entry on next login
        if (playersInsideSeal.remove(uid)) {
            // Send tint-off even if already disconnecting (client clears on reconnect anyway)
        }
        // If this member had an auto-granted Fool Card, leave it in their inventory
        // (they may re-enter the seal later; card removal happens on seal deactivation)
    }

    // ─── Server start: restore persisted seal ────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        GreySealData data      = GreySealData.get(server);

        // Always restore cooldown
        cooldownExpiryMs = data.cooldownExpiryMs;

        if (!data.sealActive || data.ownerUUID == null) return;

        ResourceKey<Level> dimKey = ResourceKey.create(
                Registries.DIMENSION, ResourceLocation.parse(data.dimensionKey));
        ServerLevel level = server.getLevel(dimKey);
        if (level == null) level = server.overworld(); // graceful fallback

        Vec3 center = new Vec3(data.centerX, data.centerY, data.centerZ);
        List<BlockPos> barriers = new CopyOnWriteArrayList<>();
        activeSeal = new GreySeal(data.ownerUUID, center, level, barriers);
        placeBarrierRingAsync(level, center, barriers);
        LOTMCraft.LOGGER.info("[GreySeal] Restored Grey Fog seal for owner {} at ({}, {}, {})",
                data.ownerUUID, data.centerX, data.centerY, data.centerZ);
    }

    // ─── Login: send tint if already inside the seal ─────────────────────────

    @SubscribeEvent
    public static void onPlayerLoginForFog(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isSealActive()) return;
        if (!player.level().dimension().equals(activeSeal.level.dimension())) return;
        double dist = xzDist(player.position(), activeSeal.center);
        if (dist <= RADIUS) {
            // Delay one tick so the client is fully loaded before we send the packet
            player.getServer().execute(() ->
                    PacketDistributor.sendToPlayer(player, new SyncGreyFogStatusPacket(true)));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static double xzDist(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Returns true if this player is exempt from ALL seal restrictions
     * (ability seals, teleport locks, dimension locks, barrier push-back).
     * Exempt: sefirot owners, Apostles (sequence ≤ 2), Great Old Ones,
     * and gathering members of the seal's owner.
     */
    private static boolean isExempt(ServerPlayer player) {
        if (!SefirahHandler.getClaimedSefirot(player).isEmpty()) return true;
        int seq = BeyonderData.getSequence(player);
        if (seq <= 2 || seq == GreatOldOneManager.GREAT_OLD_ONE_SEQ) return true;
        if (isSealActive() && player.getServer() != null) {
            return GatheringData.get(player.getServer()).isMember(
                    activeSeal.ownerUUID, player.getUUID());
        }
        return false;
    }

    @Nullable
    private static ResourceKey<Level> getSefirotDimension(String sefirot) {
        return de.jakob.lotm.sefirah.SefirahHandler.getSefirotDimensionKey(sefirot);
    }

    // ─── State record ─────────────────────────────────────────────────────────

    public static class GreySeal {
        public final UUID ownerUUID;
        public final Vec3 center;
        public final ServerLevel level;
        public final List<BlockPos> barriers;
        public boolean active = true;

        public GreySeal(UUID ownerUUID, Vec3 center, ServerLevel level, List<BlockPos> barriers) {
            this.ownerUUID = ownerUUID;
            this.center    = center;
            this.level     = level;
            this.barriers  = barriers;
        }
    }
}
