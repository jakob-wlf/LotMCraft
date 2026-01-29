package de.jakob.lotm.util.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * High-performance explosion implementation optimized for large radius explosions.
 * Features:
 * - Spatial partitioning for efficient block processing
 * - Capped damage scaling for high power
 * - Increased knockback for high power explosions
 * - Optimized raycasting with reduced ray count
 */
public class PerformantExplosion {

    private final Level level;
    private final Vec3 center;
    private final float radius;
    private final Entity source;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final Explosion vanillaExplosion;

    // Performance tuning constants
    private static final int CHUNK_SIZE = 16;
    private static final float DAMAGE_CAP = 40.0F;
    private static final float DAMAGE_CAP_START = 10.0F;
    private static final float KNOCKBACK_MULTIPLIER = 2.0F;
    private static final float JAGGEDNESS = 0.15F;
    private static final float RESISTANCE_FACTOR = 0.3F;

    // Precomputed constants
    private final float radiusSq;
    private final double centerX, centerY, centerZ;

    public PerformantExplosion(Level level, Entity source, Vec3 center, float radius,
                               boolean fire, Explosion.BlockInteraction blockInteraction) {
        this.level = level;
        this.source = source;
        this.center = center;
        this.radius = radius;
        this.fire = fire;
        this.blockInteraction = blockInteraction;

        // Precompute frequently used values
        this.radiusSq = radius * radius;
        this.centerX = center.x;
        this.centerY = center.y;
        this.centerZ = center.z;

        this.vanillaExplosion = new Explosion(
                level, source, centerX, centerY, centerZ,
                radius, fire, blockInteraction
        );
    }

    public void explode() {
        Set<BlockPos> affectedBlocks = Collections.emptySet();

        if (blockInteraction != Explosion.BlockInteraction.KEEP) {
            affectedBlocks = calculateAffectedBlocks();

            if (!affectedBlocks.isEmpty()) {
                processBlocks(affectedBlocks);
                if (fire) {
                    addFire(affectedBlocks);
                }
            }
        }

        damageEntities();

        if (level instanceof ServerLevel serverLevel) {
            createParticlesAndSound(serverLevel);
        }
    }

    private Set<BlockPos> calculateAffectedBlocks() {
        // Pre-allocate with estimated capacity
        int estimatedSize = (int)(radiusSq * Math.PI * 4 / 3);
        Set<BlockPos> blocks = new HashSet<>(estimatedSize);

        int minChunkX = Mth.floor((centerX - radius) / CHUNK_SIZE);
        int maxChunkX = Mth.floor((centerX + radius) / CHUNK_SIZE);
        int minChunkZ = Mth.floor((centerZ - radius) / CHUNK_SIZE);
        int maxChunkZ = Mth.floor((centerZ + radius) / CHUNK_SIZE);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                processChunk(chunkX, chunkZ, blocks);
            }
        }

        return blocks;
    }

    private void processChunk(int chunkX, int chunkZ, Set<BlockPos> blocks) {
        int baseX = chunkX * CHUNK_SIZE;
        int baseZ = chunkZ * CHUNK_SIZE;

        int minY = Math.max(level.getMinBuildHeight(), Mth.floor(centerY - radius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, Mth.ceil(centerY + radius));

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            int worldX = baseX + x;
            double dx = worldX - centerX;
            double dxSq = dx * dx;

            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldZ = baseZ + z;
                double dz = worldZ - centerZ;
                double xzDistSq = dxSq + dz * dz;

                if (xzDistSq > radiusSq) continue;

                for (int y = minY; y <= maxY; y++) {
                    double dy = y - centerY;
                    double distSq = xzDistSq + dy * dy;

                    if (distSq <= radiusSq) {
                        pos.set(worldX, y, worldZ);
                        if (shouldDestroyBlock(pos, distSq)) {
                            blocks.add(pos.immutable());
                        }
                    }
                }
            }
        }
    }

    private boolean shouldDestroyBlock(BlockPos pos, double distSq) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        // Position-based deterministic randomness
        long seed = (long)pos.getX() * 3129871L ^ (long)pos.getZ() * 116129781L ^ (long)pos.getY();
        seed = seed * seed * 42317861L + seed * 11L;
        float random = ((float)((seed >> 16) & 255) / 255.0F - 0.5F) * 2.0F;

        float jaggedRadius = radius * (1.0F + random * JAGGEDNESS);
        float normalizedDist = (float)(Math.sqrt(distSq) / jaggedRadius);

        if (normalizedDist > 1.0F) return false;

        float power = jaggedRadius * (1.0F - normalizedDist);
        float resistance = state.getBlock().getExplosionResistance();

        return power > resistance * RESISTANCE_FACTOR + RESISTANCE_FACTOR;
    }

    private void processBlocks(Set<BlockPos> blocks) {
        if (blockInteraction != Explosion.BlockInteraction.DESTROY &&
                blockInteraction != Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
            return;
        }

        // Sort by distance (furthest first) for better visual effect
        List<BlockPos> sortedBlocks = new ArrayList<>(blocks);
        BlockPos centerPos = BlockPos.containing(center);
        sortedBlocks.sort((a, b) -> Double.compare(b.distSqr(centerPos), a.distSqr(centerPos)));

        boolean dropItems = blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY;
        int destroyed = 0;

        for (BlockPos pos : sortedBlocks) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                if (dropItems) {
                    state.onBlockExploded(level, pos, vanillaExplosion);
                } else {
                    level.removeBlock(pos, false);
                }
                destroyed++;
            }
        }
    }

    private void damageEntities() {
        AABB boundingBox = new AABB(
                centerX - radius, centerY - radius, centerZ - radius,
                centerX + radius, centerY + radius, centerZ + radius
        );

        List<Entity> entities = level.getEntities(source, boundingBox);
        DamageSource damageSource = level.damageSources().explosion(vanillaExplosion);

        // Precompute knockback scaling
        boolean highPower = radius > DAMAGE_CAP_START;
        float excessPower = highPower ? radius - DAMAGE_CAP_START : 0;
        float capFactor = highPower ? (float)(Math.log(1 + excessPower) / Math.log(11 + excessPower)) : 0;

        for (Entity entity : entities) {
            if (entity.ignoreExplosion(vanillaExplosion)) continue;

            Vec3 entityPos = entity.position();
            double dx = entityPos.x - centerX;
            double dy = entityPos.y - centerY;
            double dz = entityPos.z - centerZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > radius) continue;

            double normalizedDist = distance / radius;
            float baseDamage = (float)((1.0 - normalizedDist) * radius * 2.0);

            // Apply damage cap
            float damage;
            if (highPower) {
                float cappedPortion = (baseDamage - DAMAGE_CAP) * capFactor;
                damage = Math.min(baseDamage, DAMAGE_CAP + cappedPortion);
            } else {
                damage = baseDamage;
            }

            if (damage > 0) {
                entity.hurt(damageSource, damage);
            }

            // Calculate knockback
            double knockbackStrength = (1.0 - normalizedDist) * KNOCKBACK_MULTIPLIER;
            if (highPower) {
                knockbackStrength *= Math.min(2.0, 1.0 + excessPower / 20.0);
            }

            // Normalize and apply knockback (avoid division by zero)
            if (distance > 0.001) {
                double invDist = 1.0 / distance;
                Vec3 knockback = new Vec3(dx * invDist * knockbackStrength,
                        dy * invDist * knockbackStrength,
                        dz * invDist * knockbackStrength);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            }

            if (entity instanceof LivingEntity living) {
                living.setLastHurtByMob(source instanceof LivingEntity ? (LivingEntity)source : null);
            }
        }
    }

    private void addFire(Set<BlockPos> affectedBlocks) {
        Random random = new Random();
        int fireCount = 0;

        for (BlockPos pos : affectedBlocks) {
            if (random.nextInt(3) != 0) continue;

            // Try placing fire on or above the destroyed block
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos testPos = pos.above(dy);
                if (level.isEmptyBlock(testPos)) {
                    BlockState belowState = level.getBlockState(testPos.below());
                    if (belowState.isSolid()) {
                        level.setBlockAndUpdate(testPos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                        fireCount++;
                        break;
                    }
                }
            }
        }
    }

    private void createParticlesAndSound(ServerLevel level) {
        vanillaExplosion.finalizeExplosion(true);
    }

    public static void create(Level level, Vec3 position, float power) {
        create(level, null, position, power, false, Explosion.BlockInteraction.DESTROY);
    }

    public static void create(Level level, Entity source, Vec3 position, float power,
                              boolean fire, Explosion.BlockInteraction blockInteraction) {
        PerformantExplosion explosion = new PerformantExplosion(
                level, source, position, power, fire, blockInteraction
        );
        explosion.explode();
    }
}