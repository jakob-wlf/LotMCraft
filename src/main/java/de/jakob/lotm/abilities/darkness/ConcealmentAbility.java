package de.jakob.lotm.abilities.darkness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.TemporaryChunkLoader;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConcealmentAbility extends SelectableAbility {
    public ConcealmentAbility(String id) {
        super(id, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.concealment.surroundings", "ability.lotmcraft.concealment.enter_concealed_area"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player)) abilityIndex = 0;
        switch(abilityIndex) {
            case 0 -> concealSurroundings(level, entity);
            case 1 -> enterConcealedArea(level, entity);
        }
    }

    private void enterConcealedArea(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Only works for server players
        if(!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"));
        ServerLevel concealedLevel = serverLevel.getServer().getLevel(dimension);
        if (concealedLevel == null) {
            return;
        }

        // Check if player is currently in the concealed world
        boolean isInConcealedWorld = serverLevel.dimension().equals(dimension);

        ServerLevel targetLevel;
        if (isInConcealedWorld) {
            // Teleport back to overworld
            targetLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        } else {
            // Teleport to concealed world
            targetLevel = concealedLevel;
        }

        if (targetLevel == null) {
            return;
        }

        // Get current position
        BlockPos currentPos = entity.blockPosition();
        double x = entity.getX();
        double z = entity.getZ();
        int startY = currentPos.getY();

        // Find safe Y position in target dimension
        // When returning to overworld, prefer searching upward to find surface
        BlockPos safePos = findSafePosition(targetLevel, x, startY, z, isInConcealedWorld);

        // Teleport the player
        serverPlayer.teleportTo(targetLevel,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
        );
    }

    /**
     * Finds a safe position to teleport to - solid ground beneath, air above
     * @param searchUpFirst if true, searches upward first (useful when returning to overworld surface)
     */
    private BlockPos findSafePosition(ServerLevel level, double x, int startY, double z, boolean searchUpFirst) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);

        // Start searching from the given Y position
        int searchY = Math.max(level.getMinBuildHeight(), Math.min(level.getMaxBuildHeight() - 3, startY));

        if (searchUpFirst) {
            // First search upward (to find surface when returning to overworld)
            for (int y = searchY; y < level.getMaxBuildHeight() - 2; y++) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }

            // If nothing found above, try searching down
            for (int y = searchY - 1; y >= level.getMinBuildHeight() + 1; y--) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }
        } else {
            // Original behavior: search downward first
            for (int y = searchY; y >= level.getMinBuildHeight() + 1; y--) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }

            // If no ground found below, search upward
            for (int y = searchY + 1; y < level.getMaxBuildHeight() - 2; y++) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }
        }

        // Fallback: generate or find the highest solid block
        return findHighestSafePosition(level, blockX, blockZ);
    }

    /**
     * Check if a position is safe: solid ground below, air at position and above
     */
    private boolean isSafePosition(ServerLevel level, BlockPos pos, BlockPos below, BlockPos above) {
        BlockState belowState = level.getBlockState(below);
        BlockState currentState = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(above);

        // Need solid ground below
        boolean hasSolidGround = !belowState.isAir() &&
                belowState.isSolidRender(level, below) &&
                belowState.isCollisionShapeFullBlock(level, below);

        // Need air at position and above (2 blocks of air for player)
        boolean hasSpace = currentState.isAir() && aboveState.isAir();

        return hasSolidGround && hasSpace;
    }

    /**
     * Find the highest safe position at given X, Z coordinates
     */
    private BlockPos findHighestSafePosition(ServerLevel level, int x, int z) {
        // Search from top down for the first solid block
        for (int y = level.getMaxBuildHeight() - 3; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && state.isSolidRender(level, checkPos)) {
                // Found solid ground, check if there's space above
                BlockPos teleportPos = checkPos.above();
                BlockPos twoAbove = teleportPos.above();

                if (level.getBlockState(teleportPos).isAir() &&
                        level.getBlockState(twoAbove).isAir()) {
                    return teleportPos;
                }
            }
        }

        // Ultimate fallback: place at Y=65 (above sea level in concealment world)
        return new BlockPos(x, 65, z);
    }

    private void concealSurroundings(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Check if we're currently in the concealment world
        boolean isInConcealmentWorld = serverLevel.dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY);

        // Determine source and destination levels based on current dimension
        ServerLevel destinationLevel;

        if (isInConcealmentWorld) {
            // If in concealment world, move blocks back to overworld
            destinationLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        } else {
            // If in overworld (or other dimension), move blocks to concealment world
            destinationLevel = serverLevel.getServer().getLevel(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY);
        }

        if(destinationLevel == null) {
            return; // Exit early if destination world doesn't exist
        }

        EffectManager.playEffect(EffectManager.Effect.CONCEALMENT, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        AtomicDouble radius = new AtomicDouble(2);
        Vec3 finalTargetLoc = entity.position();

        final HashSet<BlockPos> processedBlocks = new HashSet<>();

        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInSphereRadius(serverLevel, finalTargetLoc, radius.get(), true, false, false).forEach(blockPos -> {
                    if(blockPos.getY() < entity.position().y) return;

                    if(processedBlocks.contains(blockPos)) return;

                    // Get the block state before removing it
                    BlockState blockState = serverLevel.getBlockState(blockPos);

                    if(blockState.getDestroySpeed(serverLevel, blockPos) < 0) {
                        processedBlocks.add(blockPos);
                        return;
                    }

                    if(blockState.isAir()) { // Skip air blocks when returning to overworld
                        processedBlocks.add(blockPos);
                        return;
                    }

                    // Set the block in the destination world
                    destinationLevel.setBlockAndUpdate(blockPos, blockState);

                    // Remove the block from the source world
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());

                    processedBlocks.add(blockPos);
                });
            }

            AbilityUtil.getNearbyEntities(entity, serverLevel, finalTargetLoc, radius.get()).forEach(targetEntity -> {
                if(AbilityUtil.isTargetSignificantlyStronger(entity, targetEntity)) return;

                Vec3 originalPos = targetEntity.position();

                // Teleport the entity to the concealment world
                BlockPos safePos = findSafePosition(destinationLevel, targetEntity.getX(), targetEntity.blockPosition().getY(), targetEntity.getZ(), false);

                TemporaryChunkLoader.forceChunksTemporarily(destinationLevel, safePos.getX(), safePos.getZ(), 10, 20 * 10);

                targetEntity.teleportTo(destinationLevel,
                        safePos.getX() + 0.5,
                        safePos.getY(),
                        safePos.getZ() + 0.5,
                        Set.of(),
                        targetEntity.getYRot(),
                        targetEntity.getXRot()
                );

                LivingEntity teleportedEntity = (LivingEntity) destinationLevel.getEntity(targetEntity.getUUID());

                if(teleportedEntity == null) return;

                if(AbilityUtil.isTargetSignificantlyWeaker(entity, teleportedEntity)) {
                    teleportedEntity.setHealth(1);
                    if(!(targetEntity instanceof Player))
                        return;
                }

                int returnTime = AbilityUtil.isTargetSignificantlyWeaker(entity, teleportedEntity) ? 20 * 60 * 2 :
                        BeyonderData.getSequence(teleportedEntity) < BeyonderData.getSequence(entity) ? 20 * 5 : 20 * 25;

                ServerScheduler.scheduleDelayed(returnTime, () -> {
                    teleportedEntity.teleportTo(serverLevel,
                            originalPos.x() + 0.5,
                            originalPos.y(),
                            originalPos.z() + 0.5,
                            Set.of(),
                            teleportedEntity.getYRot(),
                            teleportedEntity.getXRot()
                    );
                });
            });

            radius.addAndGet(0.5);
        });
    }
}