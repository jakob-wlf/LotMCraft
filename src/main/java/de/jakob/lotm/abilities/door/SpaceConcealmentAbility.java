package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ApprenticeDoorEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.DisplaySpaceConcealmentParticlesPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpaceConcealmentAbility extends SelectableAbilityItem {
    private static final Map<UUID, List<ConcealedSpace>> playerSpaces = new ConcurrentHashMap<>();
    private static final Map<UUID, List<ApprenticeDoorEntity>> playerDoors = new ConcurrentHashMap<>();

    public SpaceConcealmentAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.space_concealment.other", "ability.lotmcraft.space_concealment.self", "ability.lotmcraft.space_concealment.collapse"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;

        if(!(entity instanceof Player) && abilityIndex == 1) {
            abilityIndex = random.nextInt(3) == 0 ? 0 : 2;
        }

        if(abilityIndex == 2) {
            collapseSpaces(level, entity);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // If sneaking, remove all concealed spaces for this entity
        if(entity.isCrouching()) {
            removeAllSpaces(serverLevel, entity);
            return;
        }

        // Determine target location based on ability index
        Vec3 targetPos;
        if(abilityIndex == 0) {
            // Target other location
            targetPos = AbilityUtil.getTargetLocation(entity, 20, 2, false);
        } else {
            // Target self
            targetPos = entity.position();
        }

        // Create the concealed space
        createConcealedSpace(serverLevel, entity, targetPos);
    }

    private void collapseSpaces(Level level, LivingEntity entity) {
        List<ConcealedSpace> spaces = playerSpaces.get(entity.getUUID());
        if(spaces == null || spaces.isEmpty())
            return;

        // Remove all spaces
        for(ConcealedSpace space : new ArrayList<>(spaces)) {
            space.collapse(entity, BeyonderData.getMultiplier(entity));
            ServerScheduler.cancel(space.getTaskId());
            if(space.getParticleTaskId() != null) {
                ServerScheduler.cancel(space.getParticleTaskId());
            }
        }

        playerSpaces.remove(entity.getUUID());
    }

    private void createConcealedSpace(ServerLevel level, LivingEntity entity, Vec3 center) {
        ConcealedSpace space = new ConcealedSpace(center, level, 5, entity.getUUID());

        // Add to player's list of spaces
        playerSpaces.computeIfAbsent(entity.getUUID(), k -> new ArrayList<>()).add(space);

        // Initialize the barrier box
        space.createBarriers();
        space.spawnDoorAtValidPosition();

        // Schedule periodic barrier checking (every 5 ticks)
        UUID checkTaskId = ServerScheduler.scheduleForDuration(
                5, 5, 600, // Start after 5 ticks, check every 5 ticks, for 30 seconds (600 ticks)
                space::repairBarriers,
                () -> {
                    // On finish, remove the space
                    space.removeBarriers();
                    removeSpace(entity.getUUID(), space);
                },
                level
        );

        space.setTaskId(checkTaskId);

        // If entity is a player, schedule particle display
        if(entity instanceof ServerPlayer player) {
            UUID particleTaskId = ServerScheduler.scheduleForDuration(
                    0, 10, 600, // Every 10 ticks for 30 seconds
                    () -> space.showParticlesToPlayer(player),
                    level
            );
            space.setParticleTaskId(particleTaskId);
        }
    }

    private void removeAllSpaces(ServerLevel level, LivingEntity entity) {
        List<ConcealedSpace> spaces = playerSpaces.get(entity.getUUID());
        if(spaces == null || spaces.isEmpty())
            return;

        // Remove all spaces
        for(ConcealedSpace space : new ArrayList<>(spaces)) {
            space.removeBarriers();
            ServerScheduler.cancel(space.getTaskId());
            if(space.getParticleTaskId() != null) {
                ServerScheduler.cancel(space.getParticleTaskId());
            }
        }

        playerSpaces.remove(entity.getUUID());
    }

    private void removeSpace(UUID playerUUID, ConcealedSpace space) {
        List<ConcealedSpace> spaces = playerSpaces.get(playerUUID);
        if(spaces != null) {
            spaces.remove(space);
            if(spaces.isEmpty()) {
                playerSpaces.remove(playerUUID);
            }
        }
        if(space.getParticleTaskId() != null) {
            ServerScheduler.cancel(space.getParticleTaskId());
        }
    }

    private static class ConcealedSpace {
        private final Vec3 center;
        private final ServerLevel level;
        private final Set<BlockPos> barrierPositions;
        private final Map<BlockPos, BlockState> originalBlocks;
        private final Set<BlockPos> outlinePositions;
        private UUID taskId;
        private UUID particleTaskId;
        private UUID entityUUID;
        private int radius;

        public ConcealedSpace(Vec3 center, ServerLevel level, int radius, UUID entityUUID) {
            this.center = center;
            this.level = level;
            this.barrierPositions = new HashSet<>();
            this.originalBlocks = new HashMap<>();
            this.outlinePositions = new HashSet<>();
            this.radius = radius;
            this.entityUUID = entityUUID;
        }

        public void setTaskId(UUID taskId) {
            this.taskId = taskId;
        }

        public UUID getTaskId() {
            return taskId;
        }

        public void setParticleTaskId(UUID particleTaskId) {
            this.particleTaskId = particleTaskId;
        }

        public UUID getParticleTaskId() {
            return particleTaskId;
        }

        public void spawnDoorAtValidPosition() {
            BlockPos centerPos = BlockPos.containing(center);

            // Search for a valid door position on the inside of the box
            // We check positions one block inside the barrier walls
            for(int x = -(radius - 1); x <= (radius - 1); x++) {
                for(int y = -(radius - 1); y <= (radius - 1); y++) {
                    for(int z = -(radius - 1); z <= (radius - 1); z++) {
                        // Only check positions that are adjacent to a wall
                        boolean isAdjacentToWall = Math.abs(x) == (radius - 1) ||
                                Math.abs(y) == (radius - 1) ||
                                Math.abs(z) == (radius - 1);

                        if(isAdjacentToWall) {
                            BlockPos pos = centerPos.offset(x, y, z);
                            BlockState currentState = level.getBlockState(pos);
                            BlockState aboveState = level.getBlockState(pos.above());

                            // Check if this position and the one above are both non-solid
                            if(!currentState.isSolid() && !aboveState.isSolid()) {
                                // Determine which cardinal direction faces outward to the wall
                                Direction facingDirection = getDirection(x, z, y);

                                // Verify the adjacent position (through the wall) is solid barrier
                                BlockPos adjacentPos = pos.relative(facingDirection);
                                if (!level.getBlockState(adjacentPos).is(Blocks.BARRIER)) {
                                    continue; // Not facing a barrier wall, skip
                                }

                                // Check blocks above and below for proper placement
                                BlockPos blockBelow = pos.below();
                                BlockPos blockAbove = pos.above();

                                BlockPos belowAdjacent = blockBelow.relative(facingDirection);
                                BlockPos aboveAdjacent = blockAbove.relative(facingDirection);

                                boolean belowExposed = level.getBlockState(belowAdjacent).is(Blocks.BARRIER) &&
                                        !level.getBlockState(blockBelow).isSolid();
                                boolean aboveExposed = level.getBlockState(aboveAdjacent).is(Blocks.BARRIER) &&
                                        !level.getBlockState(blockAbove).isSolid();

                                // Need at least one exposed position
                                if (!belowExposed && !aboveExposed) {
                                    continue;
                                }

                                // Determine which solid barrier block to use for door position
                                Vec3 blockCenter = getCenter(belowExposed, belowAdjacent, adjacentPos);
                                ApprenticeDoorEntity door = new ApprenticeDoorEntity(
                                        ModEntities.APPRENTICE_DOOR.get(),
                                        level,
                                        facingDirection,
                                        blockCenter,
                                        20 * 30
                                );

                                level.addFreshEntity(door);
                                playerDoors.computeIfAbsent(entityUUID, k -> new ArrayList<>()).add(door);

                                // Spawn a second door at the same position facing the opposite direction
                                Direction oppositeDirection = facingDirection.getOpposite();
                                ApprenticeDoorEntity oppositeDoor = new ApprenticeDoorEntity(
                                        ModEntities.APPRENTICE_DOOR.get(),
                                        level,
                                        oppositeDirection,
                                        blockCenter,
                                        20 * 30
                                );

                                level.addFreshEntity(oppositeDoor);

                                LivingEntity owner = level.getEntity(entityUUID) instanceof LivingEntity e ? e : null;
                                if(owner != null) {
                                    door.setOnlyVisibleForCertainPlayer(entityUUID);
                                    oppositeDoor.setOnlyVisibleForCertainPlayer(entityUUID);
                                }

                                playerDoors.computeIfAbsent(entityUUID, k -> new ArrayList<>()).add(oppositeDoor);
                                return; // Door spawned successfully, exit
                            }
                        }
                    }
                }
            }
        }

        @NotNull
        private static Vec3 getCenter(boolean belowExposed, BlockPos belowAdjacent, BlockPos adjacentPos) {
            BlockPos doorBarrierPos;
            if (belowExposed) {
                doorBarrierPos = belowAdjacent; // Use the solid barrier below
            } else {
                doorBarrierPos = adjacentPos; // Use the solid barrier at current level
            }

            // Spawn the apprentice door at the solid barrier block
            Vec3 blockCenter = new Vec3(doorBarrierPos.getX() + 0.5, doorBarrierPos.getY(), doorBarrierPos.getZ() + 0.5);
            return blockCenter;
        }

        @Nullable
        private Direction getDirection(int x, int z, int y) {
            Direction facingDirection = null;

            if(Math.abs(x) == (radius - 1)) {
                facingDirection = x > 0 ? Direction.EAST : Direction.WEST;
            } else if(Math.abs(z) == (radius - 1)) {
                facingDirection = z > 0 ? Direction.SOUTH : Direction.NORTH;
            } else if(Math.abs(y) == (radius - 1)) {
                facingDirection = y > 0 ? Direction.UP : Direction.DOWN;
            }
            return facingDirection;
        }

        public void createBarriers() {
            BlockPos centerPos = BlockPos.containing(center);
            // Create a hollow box of barriers
            for(int x = -radius; x <= radius; x++) {
                for(int y = -radius; y <= radius; y++) {
                    for(int z = -radius; z <= radius; z++) {
                        // Only create barriers on the outer shell (hollow box)
                        boolean isEdge = Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius;

                        if(isEdge) {
                            BlockPos pos = centerPos.offset(x, y, z);
                            BlockState currentState = level.getBlockState(pos);

                            // Only replace non-solid blocks
                            if(!currentState.isSolid() || currentState.isAir()) {
                                originalBlocks.put(pos, currentState);
                                level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                                barrierPositions.add(pos);
                            }

                            // Add all edge positions to outline (for particles)
                            outlinePositions.add(pos);
                        }
                    }
                }
            }
        }

        public void repairBarriers() {
            BlockPos centerPos = BlockPos.containing(center);

            // Check all positions in the shell
            for(int x = -radius; x <= radius; x++) {
                for(int y = -radius; y <= radius; y++) {
                    for(int z = -radius; z <= radius; z++) {
                        boolean isEdge = Math.abs(x) == radius || Math.abs(y) == radius || Math.abs(z) == radius;

                        if(isEdge) {
                            BlockPos pos = centerPos.offset(x, y, z);
                            BlockState currentState = level.getBlockState(pos);

                            // If a solid block was broken or changed, replace with barrier
                            if(!currentState.is(Blocks.BARRIER) && (!currentState.isSolid() || currentState.isAir())) {
                                // If this position wasn't originally tracked, track it now
                                if(!originalBlocks.containsKey(pos)) {
                                    originalBlocks.put(pos, Blocks.AIR.defaultBlockState());
                                }

                                level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                                barrierPositions.add(pos);
                            }
                        }
                    }
                }
            }
        }

        public void removeBarriers() {
            // Restore original blocks
            for(BlockPos pos : barrierPositions) {
                BlockState currentState = level.getBlockState(pos);

                // Only restore if it's still a barrier
                if(currentState.is(Blocks.BARRIER)) {
                    BlockState original = originalBlocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
                    level.setBlock(pos, original, 3);
                }
            }

            playerDoors.get(entityUUID).removeIf(door -> {
                door.discard();
                return true;
            });
        }

        public void collapse(LivingEntity source, double multiplier) {
            removeBarriers();

            BlockPos centerPos = BlockPos.containing(center);

            // Remove all blocks inside the concealed space
            for(int x = -(radius - 1); x <= (radius - 1); x++) {
                for(int y = -(radius - 1); y <= (radius - 1); y++) {
                    for(int z = -(radius - 1); z <= (radius - 1); z++) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        BlockState currentState = level.getBlockState(pos);

                        ParticleUtil.spawnParticles(level, new Random().nextBoolean() ? ModParticles.STAR.get() : new DustParticleOptions(new Vector3f(0, 0, 0), 5f), pos.getCenter(), 1, .2, 0);

                        // Replace all blocks with air
                        if(!currentState.isAir() && BeyonderData.isGriefingEnabled(source)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }

            // Deal damage to all entities inside
            AABB bounds = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );

            List<Entity> entitiesInside = level.getEntities((Entity) null, bounds, entity -> true);

            for(Entity entity : entitiesInside) {
                // Skip the door entities we created
                if(entity instanceof ApprenticeDoorEntity) {
                    continue;
                }

                // Deal damage to living entities
                if(entity instanceof LivingEntity && AbilityUtil.mayDamage(source, (LivingEntity) entity)) {
                    entity.hurt(level.damageSources().magic(), (float) (35 * multiplier)); // Adjust damage amount as needed
                }
            }
        }

        public void showParticlesToPlayer(ServerPlayer player) {
            // Spawn particles at key outline positions visible only to the owner
            // Show particles at corners and edges to outline the box
            BlockPos centerPos = BlockPos.containing(center);

            // Show particles on edges of the box for visibility
            for(int x = -radius; x <= radius; x++) {
                for(int y = -radius; y <= radius; y++) {
                    for(int z = -radius; z <= radius; z++) {
                        // Only show particles at edges/corners with reduced density
                        int edgeCount = 0;
                        if(Math.abs(x) == radius) edgeCount++;
                        if(Math.abs(y) == radius) edgeCount++;
                        if(Math.abs(z) == radius) edgeCount++;

                        // Show particles on edges (where 2+ coordinates are at max)
                        if(edgeCount >= 2) {
                            BlockPos pos = centerPos.offset(x, y, z);
                            Vec3 particlePos = pos.getCenter();
                            DisplaySpaceConcealmentParticlesPacket packet = new DisplaySpaceConcealmentParticlesPacket(particlePos.x, particlePos.y, particlePos.z);
                            PacketHandler.sendToPlayer(player, packet);
                        }
                    }
                }
            }
        }
    }
}