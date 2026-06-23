package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAccommodationPacket;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class RiverOfEternalDarknessEventHandler {

    private static final String RIVER_SEFIROT_ID = "river_of_eternal_darkness";
    private static final int REQUIRED_TICKS = 20 * 60 * 5;
    private static final float SANITY_DRAIN_PER_SECOND = 0.05f;
    private static final float DAMAGE_PER_SECOND = 5.0f;

    private static final Set<String> ALLOWED_PATHWAYS = Set.of(
            "darkness",
            "death",
            "twilight_giant"
    );

    private static final Map<UUID, Integer> ritualTicks = new HashMap<>();
    private static final Set<UUID> ritualAnnounced = new HashSet<>();
    private static final Map<UUID, UUID> ritualBeamEffectIds = new HashMap<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // Restore blessings from NBT so they survive server restarts
        RiverBlessingManager.loadFromData(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // NOTE: blessings are intentionally NOT cleared on logout — they persist until revoked
        RiverBlessingManager.clearAudience(player.server);
        // If this player was in the audience, just unmark them (can't teleport offline player)
        if (RiverBlessingManager.isInAudience(player.getUUID())) {
            RiverBlessingManager.unmarkFromAudience(player.getUUID());
        }
        if (!ritualTicks.containsKey(player.getUUID())) return;
        resetRitual(player, true);
    }

    /**
     * Blocks unauthorised players from entering the River of Eternal Darkness dimension.
     * Authorised entries:
     *   1. The River sefirot owner (uses U-key teleport or River's Call).
     *   2. Players trapped by River's Call (already teleported in by executeRiversCall).
     *   3. Players currently marked as the owner's invited audience.
     *
     * Everyone else is immediately returned to overworld spawn with a warning.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Cancel accommodation ritual if the player leaves the Overworld
        if (ritualTicks.containsKey(player.getUUID())
                && !event.getTo().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            resetRitual(player, true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8Accommodation ritual interrupted — you must remain in the Overworld."));
        }

        // Block unauthorised entry into the River dimension
        if (!event.getTo().equals(de.jakob.lotm.dimension.ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY)) return;

        boolean isOwner    = isRiverOwner(player);
        boolean isTrapped  = DeathImprintData.get(player.server).isTrappedInRiver(player.getUUID());
        boolean isAudience = RiverBlessingManager.isInAudience(player.getUUID());

        if (isOwner || isTrapped || isAudience) return;

        // Unauthorised — teleport back to overworld spawn
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§4The River of Eternal Darkness rejects your uninvited presence."));
        net.minecraft.core.BlockPos spawn = player.server.overworld().getSharedSpawnPos();
        player.teleportTo(player.server.overworld(),
                spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ritualTicks.containsKey(player.getUUID())) return;
        resetRitual(player, true);
    }

    /**
     * The River owner AND blessed players are immune to the ASLEEP mob effect.
     * Cancels the effect before it is applied.
     */
    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == ModEffects.ASLEEP) {
            UUID uuid = event.getEntity().getUUID();
            if (RiverBlessingManager.isBlessed(uuid)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                return;
            }
            // River owner is permanently immune to sleep
            if (event.getEntity() instanceof ServerPlayer sp && isRiverOwner(sp)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    /**
     * Lock the Spirit World while any river ritual is in progress, same as the Sefirah Castle.
     */
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (ritualTicks.isEmpty()) return;
        ResourceKey<Level> target  = event.getDimension();
        ResourceKey<Level> current = event.getEntity().level().dimension();
        boolean goingIn  = target  == de.jakob.lotm.dimension.ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;
        boolean goingOut = current == de.jakob.lotm.dimension.ModDimensions.SPIRIT_WORLD_DIMENSION_KEY;
        if (!goingIn && !goingOut) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "lotm.sefirot.sefirah_castle_spirit_world_locked"));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide) {
            return;
        }

        // Update beam position every 5 ticks for smooth tracking
        if (entity instanceof ServerPlayer player && ritualBeamEffectIds.containsKey(player.getUUID())) {
            if (player.tickCount % 5 == 0 && player.level() instanceof ServerLevel sl) {
                UUID beamId = ritualBeamEffectIds.get(player.getUUID());
                if (beamId != null) {
                    MovableEffectManager.updateEffectPosition(beamId, new EntityLocation(player), sl);
                }
            }
        }

        if (entity.tickCount % 20 != 0) {
            return;
        }

        // Tick the accommodation ritual regardless of whether the player is in dark water
        if (entity instanceof ServerPlayer player && ritualTicks.containsKey(player.getUUID())) {
            tickRitual(player);
        }

        // Dark water damage / effects still apply when standing in the fluid
        if (isInDarkWater(entity)) {
            applyDarkWaterEffects(entity);
        }
    }

    private static void applyDarkWaterEffects(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            if (isRiverOwner(player) || isAccommodating(player) || isEligibleAccommodator(player)) {
                return;
            }
            ensureWellBase(player);
        }

        entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.DARKNESS_GENERIC), DAMAGE_PER_SECOND);

        if (entity instanceof Player player) {
            player.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync(-SANITY_DRAIN_PER_SECOND, player);
        }
    }

    private static boolean isAccommodating(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) return false;
        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) return false;
        if (SefirahHandler.hasSefirot(player)) return false;
        String pathway = BeyonderData.getPathway(player);
        if (!ALLOWED_PATHWAYS.contains(pathway)) return false;
        // Accommodation is now started exclusively by drinking the bottle
        return ritualTicks.containsKey(player.getUUID());
    }

    private static boolean isEligibleAccommodator(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) return false;
        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) return false;
        if (SefirahHandler.hasSefirot(player)) return false;
        String pathway = BeyonderData.getPathway(player);
        return ALLOWED_PATHWAYS.contains(pathway);
    }

    private static boolean isRiverOwner(ServerPlayer player) {
        return RIVER_SEFIROT_ID.equals(SefirahHandler.getClaimedSefirot(player));
    }

    private static void tickRitual(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            resetRitual(player, true);
            return;
        }

        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) {
            resetRitual(player, true);
            return;
        }

        if (SefirahHandler.hasSefirot(player)) {
            resetRitual(player, true);
            return;
        }

        String pathway = BeyonderData.getPathway(player);
        if (!ALLOWED_PATHWAYS.contains(pathway)) {
            resetRitual(player, true);
            return;
        }

        if (!ritualAnnounced.contains(player.getUUID())) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "lotm.sefirot.river_started",
                            player.getName().getString()),
                    false);
            ritualAnnounced.add(player.getUUID());
        }

        int ticks = ritualTicks.getOrDefault(player.getUUID(), 0) + 20;
        ritualTicks.put(player.getUUID(), ticks);

        // Particle cloud around the player during the ritual
        if (player.level() instanceof ServerLevel sl) {
            Vec3 center = player.position().add(0, player.getEyeHeight() * 0.5, 0);
            ParticleUtil.spawnParticles(sl, dustForPathway("darkness"),      center, 150, 2.5, 2.5, 2.5, 0.05);
            ParticleUtil.spawnParticles(sl, dustForPathway("death"),         center, 150, 2.5, 2.5, 2.5, 0.05);
            ParticleUtil.spawnParticles(sl, dustForPathway("twilight_giant"), center, 150, 2.5, 2.5, 2.5, 0.05);
            ParticleUtil.spawnParticles(sl, dustForPathway("darkness"),      center, 80,  0.6, 0.6, 0.6, 0.02);
            ParticleUtil.spawnParticles(sl, dustForPathway("death"),         center, 80,  0.6, 0.6, 0.6, 0.02);
            ParticleUtil.spawnParticles(sl, dustForPathway("twilight_giant"), center, 80,  0.6, 0.6, 0.6, 0.02);
        }

        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(ticks, REQUIRED_TICKS));

        if (ticks < REQUIRED_TICKS) {
            return;
        }

        // Remove the beam before claiming/resetting
        UUID finishBeamId = ritualBeamEffectIds.remove(player.getUUID());
        if (finishBeamId != null && player.level() instanceof ServerLevel sl) {
            MovableEffectManager.removeEffect(finishBeamId, sl);
        }

        boolean claimed = SefirahHandler.claimSefirot(player, RIVER_SEFIROT_ID, true);
        if (claimed) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("lotm.sefirot.river_claimed"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("lotm.sefirot.river_already_occupied"));
        }

        resetRitual(player);
    }

    private static void resetRitual(ServerPlayer player, boolean dropBottle) {
        if (dropBottle) {
            player.drop(new ItemStack(de.jakob.lotm.item.ModItems.ETERNAL_DARKNESS_RIVER_WATER_BOTTLE.get()), true);
        }
        UUID beamId = ritualBeamEffectIds.remove(player.getUUID());
        if (beamId != null && player.level() instanceof ServerLevel sl) {
            MovableEffectManager.removeEffect(beamId, sl);
        }
        ritualTicks.remove(player.getUUID());
        ritualAnnounced.remove(player.getUUID());
        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
    }

    /** Resets without dropping the bottle (used when ritual completes successfully). */
    private static void resetRitual(ServerPlayer player) {
        resetRitual(player, false);
    }

    /**
     * Called when a player drinks a Bottle of Eternal Darkness River Water on an allowed pathway.
     * Starts (or advances) the accommodation ritual, granting 25% initial progress.
     */
    public static void beginAccommodationFromBottle(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8The liquid dissipates — you have no beyonder power to resonate with it."));
            return;
        }
        if (!player.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8The River's essence requires the foundation of the Overworld. Return to the surface before attempting accommodation."));
            return;
        }
        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8The River already has an owner. The water stirs but cannot bind to you."));
            return;
        }
        if (SefirahHandler.hasSefirot(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8You already own a Sefirot. The water cannot change your nature."));
            return;
        }

        // Announce the ritual start to all players (only once)
        if (!ritualAnnounced.contains(player.getUUID())) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "lotm.sefirot.river_started", player.getName().getString()), false);
            ritualAnnounced.add(player.getUUID());
        }

        // Start ritual at 0%
        int newProgress = 0;
        ritualTicks.put(player.getUUID(), newProgress);

        // Start the sky beam (visible to all except the ritual player in first-person)
        if (!ritualBeamEffectIds.containsKey(player.getUUID())
                && player.level() instanceof ServerLevel sl) {
            UUID beamId = MovableEffectManager.playEffect(
                    MovableEffectManager.MovableEffect.RIVER_SKY_BEAM,
                    new EntityLocation(player),
                    0, true, sl, player);
            ritualBeamEffectIds.put(player.getUUID(), beamId);
            // Hide from the accommodating player themselves
            MovableEffectManager.removeEffect(beamId, player);
        }

        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(newProgress, REQUIRED_TICKS));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5The River's essence flows through you — the accommodation has begun..."));
    }

    private static DustParticleOptions dustForPathway(String pathway) {
        PathwayInfos info = BeyonderData.pathwayInfos.get(pathway);
        int color = info != null ? info.color() : 0xFFFFFF;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color & 0xFF)          / 255f;
        return new DustParticleOptions(new Vector3f(r, g, b), 1.0f);
    }

    private static boolean isInDarkWater(LivingEntity entity) {
        BlockPos feetPos = entity.blockPosition();
        if (isDarkWaterBlock(entity.level(), feetPos)) {
            return true;
        }

        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        return isDarkWaterBlock(entity.level(), eyePos);
    }

    private static boolean isDarkWaterBlock(Level level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        return state.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get())
                || state.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_FLOWING.get());
    }

    private static void ensureWellBase(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        ResourceKey<Structure> key = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_of_eternal_darkness_well")
        );

        StructureStart start = level.registryAccess()
                .registry(Registries.STRUCTURE)
                .flatMap(registry -> registry.getHolder(key)
                        .map(holder -> level.structureManager()
                                .getStructureWithPieceAt(player.blockPosition(), HolderSet.direct(holder))))
                .orElse(null);

        if (start == null || !start.isValid()) {
            return;
        }

        BoundingBox box = start.getBoundingBox();
        int y = box.minY() - 1;

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                }
            }
        }
    }

}
