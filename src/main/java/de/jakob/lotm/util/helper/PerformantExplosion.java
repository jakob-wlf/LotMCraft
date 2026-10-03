package de.jakob.lotm.util.helper;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * High-performance explosion for large radius explosions.
 *
 * Optimizations over vanilla / naive approach:
 *  - Direct LevelChunkSection access instead of level.getBlockState() per block
 *  - Long-encoded block positions + fastutil map to eliminate BlockPos heap allocations
 *  - BlockState cached on first read; reused in destroy pass (no double-lookup)
 *  - Dropped cosmetic distance-sort (O(n log n) for potentially 100k+ blocks)
 *  - Fire placement samples rather than iterating all blocks a third time
 *  - Chunk existence pre-check avoids touching unloaded chunks
 */
public class PerformantExplosion {

    // ---------- tuning ----------
    private static final float JAGGEDNESS       = 0.15f;
    private static final float RESISTANCE_FACTOR = 0.3f;
    private static final float DAMAGE_CAP        = 40.0f;
    private static final float DAMAGE_CAP_START  = 10.0f;
    private static final float KNOCKBACK_MULT    = 2.0f;

    // ---------- fields ----------
    private final Level level;
    private final Entity source;
    private final float  radius;
    private final boolean fire;
    private final Explosion.BlockInteraction interaction;
    private final Explosion vanillaExplosion;

    // precomputed
    private final double cx, cy, cz;
    private final float  radiusSq;

    public PerformantExplosion(Level level, Entity source, Vec3 center, float radius,
                               boolean fire, Explosion.BlockInteraction interaction) {
        this.level       = level;
        this.source      = source;
        this.radius      = radius;
        this.fire        = fire;
        this.interaction = interaction;

        this.cx       = center.x;
        this.cy       = center.y;
        this.cz       = center.z;
        this.radiusSq = radius * radius;

        this.vanillaExplosion = new Explosion(level, source, cx, cy, cz,
                radius, fire, interaction);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void explode() {
        if (interaction != Explosion.BlockInteraction.KEEP) {
            // Keys: BlockPos.asLong()   Values: cached BlockState
            Long2ObjectOpenHashMap<BlockState> affected = collectAffectedBlocks();
            if (!affected.isEmpty()) {
                destroyBlocks(affected);
                if (fire) spawnFire(affected);
            }
        }

        damageEntities();

        if (level instanceof ServerLevel sl) {
            vanillaExplosion.finalizeExplosion(true); // particles + sound packet
        }
    }

    public static void create(Level level, Vec3 pos, float power) {
        create(level, null, pos, power, false, Explosion.BlockInteraction.DESTROY);
    }

    public static void create(Level level, Entity source, Vec3 pos, float power,
                              boolean fire, Explosion.BlockInteraction interaction) {
        new PerformantExplosion(level, source, pos, power, fire, interaction).explode();
    }

    // -------------------------------------------------------------------------
    // Block collection — direct chunk section access, no per-block map lookup
    // -------------------------------------------------------------------------

    private Long2ObjectOpenHashMap<BlockState> collectAffectedBlocks() {
        int estimatedCapacity = (int) (4.0 / 3.0 * Math.PI * radiusSq * radius * 0.4);
        Long2ObjectOpenHashMap<BlockState> result = new Long2ObjectOpenHashMap<>(
                Math.max(64, estimatedCapacity), 0.75f);

        int minCX = Mth.floor((cx - radius)) >> 4;
        int maxCX = Mth.floor((cx + radius)) >> 4;
        int minCZ = Mth.floor((cz - radius)) >> 4;
        int maxCZ = Mth.floor((cz + radius)) >> 4;

        int minY = Math.max(level.getMinBuildHeight(), Mth.floor(cy - radius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, Mth.ceil(cy + radius));

        for (int chunkX = minCX; chunkX <= maxCX; chunkX++) {
            for (int chunkZ = minCZ; chunkZ <= maxCZ; chunkZ++) {
                // Skip unloaded chunks entirely — avoids forcing chunk generation
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
                processChunk(chunk, chunkX, chunkZ, minY, maxY, result);
            }
        }

        return result;
    }

    private void processChunk(ChunkAccess chunk, int chunkX, int chunkZ,
                              int minY, int maxY,
                              Long2ObjectOpenHashMap<BlockState> out) {

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minSectionY = level.getMinBuildHeight() >> 4;

        for (int lx = 0; lx < 16; lx++) {
            int worldX = baseX + lx;
            double dx   = worldX - cx;
            double dxSq = dx * dx;
            if (dxSq > radiusSq) continue;

            for (int lz = 0; lz < 16; lz++) {
                int worldZ = baseZ + lz;
                double dz     = worldZ - cz;
                double xzSq   = dxSq + dz * dz;
                if (xzSq > radiusSq) continue;

                int prevSection = Integer.MIN_VALUE;
                LevelChunkSection section = null;

                for (int y = minY; y <= maxY; y++) {
                    double dy     = y - cy;
                    double distSq = xzSq + dy * dy;
                    if (distSq > radiusSq) continue;

                    // Cache section reference — only re-fetch on section boundary
                    int sectionIdx = (y >> 4) - minSectionY;
                    if (sectionIdx != prevSection) {
                        LevelChunkSection[] sections = chunk.getSections();
                        if (sectionIdx < 0 || sectionIdx >= sections.length) continue;
                        section = sections[sectionIdx];
                        prevSection = sectionIdx;
                    }

                    // Skip entirely empty sections (huge win in air-heavy builds)
                    if (section == null || section.hasOnlyAir()) continue;

                    // Direct palette read — no map lookup, no bounds checks
                    BlockState state = section.getBlockState(lx, y & 15, lz);
                    if (state.isAir()) continue;

                    if (shouldDestroy(state, worldX, y, worldZ, distSq)) {
                        out.put(BlockPos.asLong(worldX, y, worldZ), state);
                    }
                }
            }
        }
    }

    /**
     * Deterministic jaggedness check using bit-manipulation randomness.
     * No Random object, no allocation.
     */
    private boolean shouldDestroy(BlockState state, int x, int y, int z, double distSq) {
        long seed = (long) x * 3129871L ^ (long) z * 116129781L ^ y;
        seed = seed * seed * 42317861L + seed * 11L;
        float jag = ((float) ((seed >> 16) & 0xFF) / 255.0f - 0.5f) * 2.0f;

        float jagRadius = radius * (1.0f + jag * JAGGEDNESS);
        float normDist  = (float) (Math.sqrt(distSq) / jagRadius);
        if (normDist > 1.0f) return false;

        float power      = jagRadius * (1.0f - normDist);
        float resistance = state.getBlock().getExplosionResistance();
        return power > resistance * RESISTANCE_FACTOR + RESISTANCE_FACTOR;
    }

    // -------------------------------------------------------------------------
    // Block destruction — reuses cached BlockState, no re-lookup
    // -------------------------------------------------------------------------

    private void destroyBlocks(Long2ObjectOpenHashMap<BlockState> blocks) {
        if (interaction != Explosion.BlockInteraction.DESTROY
                && interaction != Explosion.BlockInteraction.DESTROY_WITH_DECAY) return;

        boolean dropItems = interaction == Explosion.BlockInteraction.DESTROY_WITH_DECAY;

        // Iterate entries directly — no intermediate List, no sort
        blocks.long2ObjectEntrySet().fastForEach(entry -> {
            BlockPos pos   = BlockPos.of(entry.getLongKey());
            BlockState state = entry.getValue();

            // Re-read only to confirm it hasn't changed (e.g. cascade); skip if already air
            if (level.getBlockState(pos).isAir()) return;

            if (dropItems) {
                state.onBlockExploded(level, pos, vanillaExplosion);
            } else {
                level.removeBlock(pos, false);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Fire — sample from affected set rather than iterating all blocks
    // -------------------------------------------------------------------------

    private void spawnFire(Long2ObjectOpenHashMap<BlockState> affected) {
        Random rng = new Random();
        long[] keys = affected.keySet().toLongArray();

        for (long key : keys) {
            if (rng.nextInt(3) != 0) continue;

            BlockPos pos = BlockPos.of(key);
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos candidate = pos.above(dy);
                if (level.isEmptyBlock(candidate)
                        && level.getBlockState(candidate.below()).isSolid()) {
                    level.setBlockAndUpdate(candidate, Blocks.FIRE.defaultBlockState());
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Entity damage — unchanged logic, minor cleanup
    // -------------------------------------------------------------------------

    private void damageEntities() {
        AABB box = new AABB(cx - radius, cy - radius, cz - radius,
                cx + radius, cy + radius, cz + radius);
        List<Entity> entities = level.getEntities(source, box);
        DamageSource src = level.damageSources().explosion(vanillaExplosion);

        boolean  highPower   = radius > DAMAGE_CAP_START;
        float    excessPower = highPower ? radius - DAMAGE_CAP_START : 0;
        double   logDenom    = highPower ? Math.log(11 + excessPower) : 1;

        for (Entity entity : entities) {
            if (entity.ignoreExplosion(vanillaExplosion)) continue;

            double dx = entity.getX() - cx;
            double dy = entity.getY() - cy;
            double dz = entity.getZ() - cz;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > radius) continue;

            double normDist   = distance / radius;
            float  baseDamage = (float) ((1.0 - normDist) * radius * 2.0);

            float damage;
            if (highPower && baseDamage > DAMAGE_CAP) {
                float cap = (float) (Math.log(1 + excessPower) / logDenom);
                damage = DAMAGE_CAP + (baseDamage - DAMAGE_CAP) * cap;
            } else {
                damage = baseDamage;
            }

            if (damage > 0) entity.hurt(src, damage);

            if (distance > 0.001) {
                double kb = (1.0 - normDist) * KNOCKBACK_MULT
                        * (highPower ? Math.min(2.0, 1.0 + excessPower / 20.0) : 1.0);
                double inv = 1.0 / distance;
                entity.setDeltaMovement(entity.getDeltaMovement()
                        .add(dx * inv * kb, dy * inv * kb, dz * inv * kb));
            }

            if (entity instanceof LivingEntity living) {
                living.setLastHurtByMob(source instanceof LivingEntity le ? le : null);
            }
        }
    }
}