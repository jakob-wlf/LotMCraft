package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ConfinementAbility extends Ability {

    public static final List<ConfinementZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();



    // Yellow/gold dust particle for the cage outline
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.85f, 0.1f), 1.2f);
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(
            new Vector3f(0.9f, 0.3f, 0.1f), 1.0f);

    public ConfinementAbility(String id) {
        super(id, 30f, "confinement");
        interactionRadius = 15;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        // Remove any existing confinement from this caster before placing a new one
        ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst()
                .ifPresent(existing -> {
                    for (BlockPos pos : existing.barriers) {
                        if (serverLevel.getBlockState(pos).is(Blocks.BARRIER)) {
                            serverLevel.removeBlock(pos, false);
                        }
                    }
                    existing.deactivate();
                    ACTIVE_ZONES.remove(existing);
                });

        int RADIUS = 6;
        int DURATION = 1200*(int) Math.max(multiplier(entity)/4,1);


        Vec3 center = AbilityUtil.getTargetLocation(entity, 12, 2f);

        List<BlockPos> placed = new ArrayList<>();

        // Build hollow cube of BARRIER blocks (only faces of cube, not interior)
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    boolean onFace = Math.abs(dx) == RADIUS || Math.abs(dy) == RADIUS || Math.abs(dz) == RADIUS;
                    if (!onFace) continue;

                    BlockPos pos = BlockPos.containing(center.x + dx, center.y + dy, center.z + dz);
                    if (!serverLevel.getBlockState(pos).isAir()) continue;

                    serverLevel.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    placed.add(pos);
                }
            }
        }

        ConfinementZone zone = new ConfinementZone(entity.getUUID(), center, RADIUS, serverLevel, placed);
        ACTIVE_ZONES.add(zone);

        // Spawn particles along edges every 10 ticks for the duration
        ServerScheduler.scheduleForDuration(0, 10, DURATION, () -> {
            if (!zone.isActive()) return;
            spawnEdgeParticles(serverLevel, center, RADIUS);
        }, () -> {
            // Cleanup: remove barriers and deactivate zone
            for (BlockPos pos : zone.barriers) {
                if (serverLevel.getBlockState(pos).is(Blocks.BARRIER)) {
                    serverLevel.removeBlock(pos, false);
                }
            }
            zone.deactivate();
            ACTIVE_ZONES.remove(zone);
        }, serverLevel);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, RADIUS, 20 * 2));
    }

    /**
     * Spawns dust particles along the 12 edges of the cage cube.
     */
    private static void spawnEdgeParticles(ServerLevel level, Vec3 center, int r) {
        double cx = center.x, cy = center.y, cz = center.z;

        // 12 edges of the cube — iterate each edge in steps of 0.6 blocks
        double step = 0.6;

        // 4 vertical edges
        for (int sx : new int[]{-r, r}) {
            for (int sz : new int[]{-r, r}) {
                for (double dy = -r; dy <= r; dy += step) {
                    spawnEdgeDust(level, cx + sx, cy + dy, cz + sz);
                }
            }
        }

        // 4 horizontal edges along X axis
        for (int sy : new int[]{-r, r}) {
            for (int sz : new int[]{-r, r}) {
                for (double dx = -r; dx <= r; dx += step) {
                    spawnEdgeDust(level, cx + dx, cy + sy, cz + sz);
                }
            }
        }

        // 4 horizontal edges along Z axis
        for (int sx : new int[]{-r, r}) {
            for (int sy : new int[]{-r, r}) {
                for (double dz = -r; dz <= r; dz += step) {
                    spawnEdgeDust(level, cx + sx, cy + sy, cz + dz);
                }
            }
        }
    }

    private static void spawnEdgeDust(ServerLevel level, double x, double y, double z) {
        ParticleUtil.spawnParticles(level, GOLD_DUST, new Vec3(x, y, z), 1, 0.05, 0.01);
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        Vec3 destination = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        for (ConfinementZone zone : ACTIVE_ZONES) {
            if (!zone.level.equals(serverLevel)) continue;
            if (!zone.isActive()) continue;

            boolean insideNow = entity.position().distanceTo(zone.center) <= zone.radius + 1.0;
            boolean outsideDest = destination.distanceTo(zone.center) > zone.radius;

            if (insideNow && outsideDest) {
                event.setCanceled(true);
                return;
            }
        }
    }

    public static class ConfinementZone {
        public final UUID ownerId;
        public final Vec3 center;
        public final double radius;
        public final ServerLevel level;
        public final List<BlockPos> barriers;
        private boolean active = true;

        public ConfinementZone(UUID ownerId, Vec3 center, double radius, ServerLevel level, List<BlockPos> barriers) {
            this.ownerId = ownerId;
            this.center = center;
            this.radius = radius;
            this.level = level;
            this.barriers = barriers;
        }

        public boolean isActive() {
            return active;
        }

        public void deactivate() {
            active = false;
        }
    }
}
