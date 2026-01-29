package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DesolateAreaEntity extends Entity {
    private static final int RADIUS = 200; // 200x200 area
    private static final int EFFECT_RADIUS = 100; // Radius for entity effects
    private static final int BLOCKS_PER_TICK = 80; // How many blocks to corrupt per tick
    private static final int ENTITY_CHECK_INTERVAL = 20; // Check entities every second

    private int tickCounter = 0;
    private int corruptionProgress = 0;
    private Random random = new Random();

    // Block conversion maps
    private static final Map<Block, Block> BLOCK_CONVERSIONS = new HashMap<>();

    static {
        // Stone conversions
        BLOCK_CONVERSIONS.put(Blocks.STONE, Blocks.BASALT);
        BLOCK_CONVERSIONS.put(Blocks.COBBLESTONE, Blocks.BLACKSTONE);
        BLOCK_CONVERSIONS.put(Blocks.ANDESITE, Blocks.BASALT);
        BLOCK_CONVERSIONS.put(Blocks.DIORITE, Blocks.BASALT);
        BLOCK_CONVERSIONS.put(Blocks.GRANITE, Blocks.BLACKSTONE);
        BLOCK_CONVERSIONS.put(Blocks.COARSE_DIRT, Blocks.SOUL_SOIL);
        BLOCK_CONVERSIONS.put(Blocks.PODZOL, Blocks.BASALT);
        BLOCK_CONVERSIONS.put(Blocks.MYCELIUM, Blocks.SOUL_SAND);

        // Sand conversions
        BLOCK_CONVERSIONS.put(Blocks.SAND, Blocks.SOUL_SAND);
        BLOCK_CONVERSIONS.put(Blocks.RED_SAND, Blocks.SOUL_SAND);
    }

    public DesolateAreaEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public DesolateAreaEntity(Level level, Vec3 pos) {
        this(ModEntities.DESOLATE_AREA.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnClientParticles();
            return;
        }

        tickCounter++;

        // Apply effects to entities every second
        if (tickCounter % ENTITY_CHECK_INTERVAL == 0) {
            applyEntityEffects();
        }

        // Corrupt blocks gradually
        corruptSurroundingBlocks();

        // Ambient sound effects every 5 seconds
        if (tickCounter % 100 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, blockPosition(), SoundEvents.WITHER_AMBIENT,
                    SoundSource.HOSTILE, 0.3f, 0.5f);
        }
    }

    private void applyEntityEffects() {
        AABB effectBox = new AABB(
                position().x - EFFECT_RADIUS, position().y - EFFECT_RADIUS, position().z - EFFECT_RADIUS,
                position().x + EFFECT_RADIUS, position().y + EFFECT_RADIUS, position().z + EFFECT_RADIUS
        );

        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, effectBox);

        for (LivingEntity entity : entities) {
            double distance = entity.distanceTo(this);

            if(BeyonderData.isBeyonder(entity) && BeyonderData.getPathway(entity).equals("mother") && BeyonderData.getSequence(entity) <= 2) {
                continue;
            }

            if (distance <= EFFECT_RADIUS) {
                // Stronger effects closer to the center
                int amplifier = (int) (3 - (distance / (EFFECT_RADIUS / 3)));
                amplifier = Math.max(0, Math.min(3, amplifier));

                // Apply negative effects
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, amplifier, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, amplifier, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, amplifier, false, false));

                entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, amplifier, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amplifier, false, false));
            }
        }
    }

    private void corruptSurroundingBlocks() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        BlockPos center = blockPosition();

        // Corrupt a few random blocks each tick
        for (int i = 0; i < BLOCKS_PER_TICK; i++) {
            // Spiral outward from center over time
            int currentRadius = Math.min(RADIUS, (corruptionProgress / 100) + 10);

            int xOffset = random.nextInt(currentRadius * 2 + 1) - currentRadius;
            int zOffset = random.nextInt(currentRadius * 2 + 1) - currentRadius;
            int yOffset = random.nextInt(37) - 18; // -10 to +10 vertical range

            BlockPos targetPos = center.offset(xOffset, yOffset, zOffset);

            corruptBlock(serverLevel, targetPos);
        }

        corruptionProgress++;
    }

    private void corruptBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // Convert grass blocks using Perlin-like noise for natural patches
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) {
            Block replacement = getGrassReplacement(pos);
            level.setBlock(pos, replacement.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Convert flowers to wither roses
        if (block instanceof FlowerBlock && !(block instanceof WitherRoseBlock)) {
            level.setBlock(pos, Blocks.WITHER_ROSE.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        if (block instanceof VineBlock) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Convert logs to polished basalt
        if (block instanceof RotatedPillarBlock && (
                block == Blocks.OAK_LOG || block == Blocks.BIRCH_LOG ||
                        block == Blocks.SPRUCE_LOG || block == Blocks.JUNGLE_LOG ||
                        block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG ||
                        block == Blocks.STRIPPED_OAK_LOG || block == Blocks.STRIPPED_BIRCH_LOG ||
                        block == Blocks.STRIPPED_SPRUCE_LOG || block == Blocks.STRIPPED_JUNGLE_LOG ||
                        block == Blocks.STRIPPED_ACACIA_LOG || block == Blocks.STRIPPED_DARK_OAK_LOG)) {
            level.setBlock(pos, Blocks.POLISHED_BASALT.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        if (block instanceof LeavesBlock) {
            level.setBlock(pos, Blocks.MANGROVE_ROOTS.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Destroy crops
        if (block instanceof CropBlock || block instanceof StemBlock || block instanceof SaplingBlock) {
            level.destroyBlock(pos, false);
            level.setBlock(pos, Blocks.SOUL_SAND.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Convert farmland to soul sand
        if (block instanceof FarmBlock) {
            level.setBlock(pos, Blocks.SOUL_SAND.defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Use conversion map
        if (BLOCK_CONVERSIONS.containsKey(block)) {
            level.setBlock(pos, BLOCK_CONVERSIONS.get(block).defaultBlockState(), 3);
            spawnCorruptionParticles(level, pos);
            return;
        }

        // Convert tall grass and plants to dead bush
        if (block instanceof BushBlock) {
            if (random.nextFloat() < 0.7f) {
                level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
            } else {
                level.removeBlock(pos, false);
            }
            spawnCorruptionParticles(level, pos);
        }
    }

    /**
     * Creates natural-looking patches using a pseudo-Perlin noise approach
     * Returns Soul Sand, Soul Soil, or Basalt based on position
     */
    private Block getGrassReplacement(BlockPos pos) {
        // Use position-based "noise" for consistent patches
        double noise = getNoiseValue(pos.getX(), pos.getZ());

        // Create three distinct zones with some blending
        if (noise < 0.33) {
            return Blocks.SOUL_SAND;
        } else if (noise < 0.66) {
            return Blocks.SOUL_SOIL;
        } else {
            return Blocks.BASALT;
        }
    }

    /**
     * Simple pseudo-Perlin noise function for natural-looking patches
     */
    private double getNoiseValue(int x, int z) {
        // Use multiple octaves for more natural patterns
        double value = 0.0;
        double amplitude = 1.0;
        double frequency = 0.03; // Controls patch size (lower = larger patches)

        // Layer multiple noise octaves
        for (int octave = 0; octave < 3; octave++) {
            value += simplexNoise(x * frequency, z * frequency) * amplitude;
            frequency *= 2.0;
            amplitude *= 0.5;
        }

        // Normalize to 0-1 range
        return (value + 1.0) / 2.0;
    }

    /**
     * Simple 2D noise function (simplified Perlin/Simplex)
     */
    private double simplexNoise(double x, double z) {
        // Simple hash-based noise
        int xi = (int) Math.floor(x);
        int zi = (int) Math.floor(z);

        double xf = x - xi;
        double zf = z - zi;

        // Smooth interpolation
        double u = fade(xf);
        double v = fade(zf);

        // Hash coordinates
        int aa = hash(xi, zi);
        int ab = hash(xi, zi + 1);
        int ba = hash(xi + 1, zi);
        int bb = hash(xi + 1, zi + 1);

        // Interpolate
        double x1 = lerp(gradient(aa, xf, zf), gradient(ba, xf - 1, zf), u);
        double x2 = lerp(gradient(ab, xf, zf - 1), gradient(bb, xf - 1, zf - 1), u);

        return lerp(x1, x2, v);
    }

    private int hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private double gradient(int hash, double x, double z) {
        int h = hash & 7;
        double u = h < 4 ? x : z;
        double v = h < 4 ? z : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private void spawnCorruptionParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 0.3, 0.3, 0.3, 0.01);
    }

    private void spawnClientParticles() {
        if (random.nextFloat() < 0.3f) {
            double offsetX = (random.nextDouble() - 0.5) * RADIUS * 2;
            double offsetY = (random.nextDouble() - 0.5) * 20;
            double offsetZ = (random.nextDouble() - 0.5) * RADIUS * 2;

            level().addParticle(ParticleTypes.ASH,
                    position().x + offsetX,
                    position().y + offsetY,
                    position().z + offsetZ,
                    0, 0.02, 0);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.corruptionProgress = tag.getInt("CorruptionProgress");
        this.tickCounter = tag.getInt("TickCounter");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("CorruptionProgress", this.corruptionProgress);
        tag.putInt("TickCounter", this.tickCounter);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }
}