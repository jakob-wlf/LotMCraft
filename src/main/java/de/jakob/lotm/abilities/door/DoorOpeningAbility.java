package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ApprenticeDoorEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class DoorOpeningAbility extends Ability {
    public DoorOpeningAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 9));
    }

    @Override
    public float getSpiritualityCost() {
        return 12;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        BlockPos targetLoc = AbilityUtil.getTargetBlock(entity, 25, false);

        if(level.getBlockState(targetLoc).isAir()) {
            Vec3 failureParticleLoc = AbilityUtil.getTargetBlock(entity, 4).getCenter();
            spawnFailureParticles((ServerLevel) level, failureParticleLoc);
            level.playSound(null, failureParticleLoc.x, failureParticleLoc.y, failureParticleLoc.z, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1, 1);
            return;
        }

        // Get the cardinal direction closest to facing the player
        Direction facingDirection = getClosestCardinalDirection(entity, targetLoc);

        // Check if the target block is exposed in that direction
        BlockPos adjacentPos = targetLoc.relative(facingDirection);
        if (!level.getBlockState(adjacentPos).isAir()) {
            Vec3 failureParticleLoc = AbilityUtil.getTargetBlock(entity, 4).getCenter();
            spawnFailureParticles((ServerLevel) level, failureParticleLoc);
            level.playSound(null, failureParticleLoc.x, failureParticleLoc.y, failureParticleLoc.z, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1, 1);
            return; // Block is not exposed in that direction
        }

        // Check blocks above and below
        BlockPos blockBelow = targetLoc.below();
        BlockPos blockAbove = targetLoc.above();

        BlockPos belowAdjacent = blockBelow.relative(facingDirection);
        BlockPos aboveAdjacent = blockAbove.relative(facingDirection);

        boolean belowExposed = level.getBlockState(belowAdjacent).isAir() && !level.getBlockState(blockBelow).isAir();
        boolean aboveExposed = level.getBlockState(aboveAdjacent).isAir() && !level.getBlockState(blockAbove).isAir();

        // If neither block is exposed in that direction, return
        if (!belowExposed && !aboveExposed) {
            Vec3 failureParticleLoc = AbilityUtil.getTargetBlock(entity, 4).getCenter();
            spawnFailureParticles((ServerLevel) level, failureParticleLoc);
            level.playSound(null, failureParticleLoc.x, failureParticleLoc.y, failureParticleLoc.z, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1, 1);
            return;
        }

        // Determine the starting position for the door
        BlockPos doorStartPos;
        if (belowExposed) {
            // Both are exposed, shift to the block below
            doorStartPos = blockBelow;
        } else {
            // Only above is exposed, use current position
            doorStartPos = targetLoc;
        }
        // Spawn the apprentice door
        Vec3 blockCenter = new Vec3(doorStartPos.getX() + 0.5, doorStartPos.getY(), doorStartPos.getZ() + 0.5);
        ApprenticeDoorEntity door = new ApprenticeDoorEntity(
                ModEntities.APPRENTICE_DOOR.get(), // Assuming this is your registered entity type
                level,
                facingDirection,
                blockCenter,
                20 * 10
        );

        level.addFreshEntity(door);

        level.playSound(null, blockCenter.x, blockCenter.y, blockCenter.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);
    }

    private final DustParticleOptions blueDust = new DustParticleOptions(
            new Vector3f(99 / 255f, 255 / 255f, 250 / 255f),
            1
    );

    private void spawnFailureParticles(ServerLevel level, Vec3 pos) {
        ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, pos, 35, .4, .1);
        ParticleUtil.spawnParticles(level, blueDust, pos, 35, .4, .1);
    }

    private Direction getClosestCardinalDirection(LivingEntity entity, BlockPos targetPos) {
        Vec3 entityPos = entity.position();
        Vec3 blockPos = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

        Vec3 direction = entityPos.subtract(blockPos).normalize(); // Reversed: from block to player

        // Find the cardinal direction with the largest component
        double absX = Math.abs(direction.x);
        double absZ = Math.abs(direction.z);

        if (absX > absZ) {
            return direction.x > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return direction.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
