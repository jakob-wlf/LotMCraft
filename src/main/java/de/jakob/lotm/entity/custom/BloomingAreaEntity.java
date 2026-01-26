package de.jakob.lotm.entity.custom;

import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BloomingAreaEntity extends Entity {
    private static final int RADIUS = 200;
    private static final int CHUNKS_PER_TICK = 2; // Process 2 chunks per tick

    // Map to store all crop positions in the area
    private final Map<BlockPos, BlockState> cropMap = new HashMap<>();

    // Queue of chunks to scan
    private final Queue<ChunkPos> chunksToScan = new LinkedList<>();

    // Track when we last updated the crop map
    private int lastMapUpdate = 0;
    private static final int MAP_UPDATE_INTERVAL = 100; // Update every 5 seconds (100 ticks)

    private boolean initialScanComplete = false;

    private final Map<BlockPos, String> designatedAreas = new HashMap<>(); // "mushroom" or "flower"
    private int lastPlantSpawn = 0;
    private static final int PLANT_SPAWN_INTERVAL = 5;

    private int lastBonemealApplication = 0;
    private static final int BONEMEAL_INTERVAL = 20; // Apply bonemeal every second

    public BloomingAreaEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public BloomingAreaEntity(Level level, Vec3 pos) {
        this(ModEntities.BLOOMING_AREA.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
        scheduleInitialScan();
    }

    private void scheduleInitialScan() {
        BlockPos center = this.blockPosition();
        int chunkRadius = RADIUS / 16;
        ChunkPos centerChunk = new ChunkPos(center);

        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                chunksToScan.add(new ChunkPos(centerChunk.x + cx, centerChunk.z + cz));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(!level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level();

            // Scan chunks progressively
            scanChunks(serverLevel);

            // Periodically refresh the crop map
            if (initialScanComplete && tickCount - lastMapUpdate >= MAP_UPDATE_INTERVAL) {
                scheduleInitialScan();
                lastMapUpdate = tickCount;
                initialScanComplete = false;
            }

            // Apply effects
            applyEffects(serverLevel);

            if (initialScanComplete) {
                spawnPlants(serverLevel);
                applyRandomBonemeal(serverLevel);
            }

            // Grow crops from the map
            if(tickCount % 20 == 0) {
                instantGrowAllCrops(serverLevel);
            }
        }
    }

    private void designateGrowthAreas() {
        BlockPos center = this.blockPosition();
        Random random = new Random();

        // Create 15-25 random patches - much more than before!
        int patchCount = 15 + random.nextInt(11);

        for (int i = 0; i < patchCount; i++) {
            // Random position within the 200x200 area
            int offsetX = random.nextInt(RADIUS * 2) - RADIUS;
            int offsetZ = random.nextInt(RADIUS * 2) - RADIUS;
            BlockPos patchCenter = center.offset(offsetX, 0, offsetZ);

            // Randomly choose mushroom or flower patch
            String type = random.nextBoolean() ? "mushroom" : "flower";

            // Mark a larger area around this center
            int patchRadius = 5 + random.nextInt(6); // 5-10 block radius - bigger patches!
            for (int x = -patchRadius; x <= patchRadius; x++) {
                for (int z = -patchRadius; z <= patchRadius; z++) {
                    BlockPos pos = patchCenter.offset(x, 0, z);
                    designatedAreas.put(pos, type);
                }
            }
        }
    }

    private void scanChunks(ServerLevel level) {
        for (int i = 0; i < CHUNKS_PER_TICK && !chunksToScan.isEmpty(); i++) {
            ChunkPos chunkPos = chunksToScan.poll();
            scanChunkForCrops(level, chunkPos);
        }

        if (chunksToScan.isEmpty() && !initialScanComplete) {
            initialScanComplete = true;
            designateGrowthAreas(); // Designate patches after scan
        }
    }

    private void spawnPlants(ServerLevel level) {
        if (tickCount - lastPlantSpawn < PLANT_SPAWN_INTERVAL) return;
        lastPlantSpawn = tickCount;

        Random random = new Random();

        // Spawn 3-7 plants per interval - much more frequent!
        int spawns = 3 + random.nextInt(5);

        for (int i = 0; i < spawns; i++) {
            // Pick a random designated area
            if (designatedAreas.isEmpty()) continue;

            List<BlockPos> positions = new ArrayList<>(designatedAreas.keySet());
            BlockPos randomPos = positions.get(random.nextInt(positions.size()));
            String type = designatedAreas.get(randomPos);

            // Find the actual ground level
            BlockPos.MutableBlockPos mutablePos = randomPos.mutable();
            for (int y = -5; y <= 5; y++) {
                mutablePos.setY(this.blockPosition().getY() + y);
                BlockState groundState = level.getBlockState(mutablePos);
                BlockState aboveState = level.getBlockState(mutablePos.above());

                // Check if it's a valid grass block with air above
                if (groundState.is(Blocks.GRASS_BLOCK) && aboveState.isAir()) {

                    BlockPos spawnPos = mutablePos.above();

                    if (type.equals("mushroom")) {
                        // Spawn red or brown mushroom
                        BlockState mushroom = random.nextBoolean() ?
                                Blocks.RED_MUSHROOM.defaultBlockState() :
                                Blocks.BROWN_MUSHROOM.defaultBlockState();
                        level.setBlock(spawnPos, mushroom, 3);

                    } else if (type.equals("flower")) {
                        // Spawn random flower
                        BlockState flower = getRandomFlower(random);
                        level.setBlock(spawnPos, flower, 3);
                    }
                    break;
                }
            }
        }
    }

    private void applyRandomBonemeal(ServerLevel serverLevel) {
        if (tickCount - lastBonemealApplication < BONEMEAL_INTERVAL) return;
        lastBonemealApplication = tickCount;

        Random random = new Random();
        BlockPos center = this.blockPosition();

        // Apply bonemeal to 15-30 random positions per second - creates very lush environment!
        int bonemealApplications = 15 + random.nextInt(16);

        for (int i = 0; i < bonemealApplications; i++) {
            // Pick a random position within the radius
            int offsetX = random.nextInt(RADIUS * 2) - RADIUS;
            int offsetZ = random.nextInt(RADIUS * 2) - RADIUS;
            int offsetY = random.nextInt(11) - 5; // -5 to +5 from center Y

            BlockPos targetPos = center.offset(offsetX, offsetY, offsetZ);

            // Check if position is loaded
            if (!serverLevel.isLoaded(targetPos)) continue;

            BlockState blockState = serverLevel.getBlockState(targetPos);

            // Apply bonemeal with a preference for grass patches (30% of the time)
            boolean shouldBonemealGrass = random.nextFloat() < 0.3f;

            applyBonemeal(serverLevel, targetPos, blockState, shouldBonemealGrass);
        }
    }

    private void applyBonemeal(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, boolean shouldBonemealGrass) {
        if(blockState.is(Blocks.GRASS_BLOCK) && !shouldBonemealGrass) {
            return; // Don't want grass to grow everywhere always
        }

        if(blockState.is(Blocks.SHORT_GRASS)) {
            return; // It looks weird if all grass is tall
        }

        if(!(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock)) {
            if(blockState.is(Blocks.SUGAR_CANE)) {
                tryGrowSugarCane(serverLevel, blockPos);
            }
            return;
        }

        if(blockState.is(Blocks.COCOA) && blockState.getValue(CocoaBlock.AGE) >= 2) {
            return; // Cocoa is fully grown and will crash if we try to bonemeal it
        }

        if(bonemealableBlock.isBonemealSuccess(serverLevel, serverLevel.random, blockPos, blockState)) {
            bonemealableBlock.performBonemeal(serverLevel, serverLevel.random, blockPos, blockState);
        }
    }

    private void tryGrowSugarCane(ServerLevel serverLevel, BlockPos blockPos) {
        // Find the top of the sugar cane
        BlockPos.MutableBlockPos mutablePos = blockPos.mutable();
        while (serverLevel.getBlockState(mutablePos.above()).is(Blocks.SUGAR_CANE)) {
            mutablePos.move(0, 1, 0);
        }

        // Try to grow if not at max height (3 blocks)
        if (mutablePos.getY() - blockPos.getY() < 2 && serverLevel.getBlockState(mutablePos.above()).isAir()) {
            serverLevel.setBlock(mutablePos.above(), Blocks.SUGAR_CANE.defaultBlockState(), 3);
        }
    }

    private BlockState getRandomFlower(Random random) {
        Block[] flowers = {
                Blocks.DANDELION,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER,
                Blocks.LILY_OF_THE_VALLEY,
                Blocks.SUNFLOWER,
                Blocks.LILAC,
                Blocks.ROSE_BUSH,
                Blocks.PEONY
        };
        return flowers[random.nextInt(flowers.length)].defaultBlockState();
    }

    private void scanChunkForCrops(ServerLevel level, ChunkPos chunkPos) {
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }

        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        BlockPos center = this.blockPosition();

        LevelChunkSection[] sections = chunk.getSections();
        int minSectionY = level.getMinBuildHeight() / 16;

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir()) continue;

            int sectionY = (minSectionY + sectionIndex) * 16;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);

                        // Check for crops OR saplings
                        if (state.getBlock() instanceof CropBlock ||
                                state.getBlock() instanceof SaplingBlock) {
                            BlockPos worldPos = new BlockPos(
                                    chunk.getPos().getMinBlockX() + x,
                                    sectionY + y,
                                    chunk.getPos().getMinBlockZ() + z
                            );

                            if (worldPos.distSqr(center) <= RADIUS * RADIUS) {
                                cropMap.put(worldPos, state);
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyEffects(ServerLevel serverLevel) {
        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, null, RADIUS, this.position(),
                new MobEffectInstance(MobEffects.SATURATION, 200, 20, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 200, 20, false, false, false),
                new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 200, 5, false, false, false));
    }

    // Public method to access the crop map
    public Map<BlockPos, BlockState> getCropMap() {
        return Collections.unmodifiableMap(cropMap);
    }

    // Method to manually force instant growth of all crops
    public void instantGrowAllCrops(ServerLevel level) {
        for (Map.Entry<BlockPos, BlockState> entry : cropMap.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            if (state.getBlock() instanceof CropBlock crop && level.getBlockState(pos).getBlock() instanceof CropBlock) {
                level.setBlock(pos, state.trySetValue(CropBlock.AGE, crop.getMaxAge()), 2);
            } else if (state.getBlock() instanceof SaplingBlock sapling && level.getBlockState(pos).getBlock() instanceof SaplingBlock) {
                // Force sapling to grow into a tree
                sapling.advanceTree(level, pos, state, level.random);
            }
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
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
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