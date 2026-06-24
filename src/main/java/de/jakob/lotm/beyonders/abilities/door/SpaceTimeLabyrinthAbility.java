package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.MysticalDoorEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SpaceTimeLabyrinthAbility extends Ability {
    public SpaceTimeLabyrinthAbility(String id) {
        super(id,20);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotmcraft.no_target").withColor(getColorForPathway("door")));
            return;
        }

        ServerLevel labyrinthLevel = ((ServerLevel) level).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space_time_labyrinth")));

        if(BeyonderData.getSequence(target) < BeyonderData.getSequence(entity) || labyrinthLevel == null) {
            ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), entity.getEyePosition(), 80, .8, 0.075);
            entity.knockback(0.5f, target.getX() - entity.getX(), target.getZ() - entity.getZ());
            return;
        }

        Vec3 doorPos = target.position().add(VectorUtil.getPerpendicularVector(entity.getLookAngle()).scale(4));
        MysticalDoorEntity doorEntity = new MysticalDoorEntity(labyrinthLevel, doorPos, 3, 4, 40);
        doorEntity.setRotation((float) Math.toDegrees(Math.atan2(target.getZ() - doorPos.z, target.getX() - doorPos.x)) - 90);
        level.addFreshEntity(doorEntity);

        ServerScheduler.scheduleForDuration(0, 1, 25, () -> {
            target.setDeltaMovement(doorEntity.position().subtract(target.position()).normalize().scale(0.5));
            target.hurtMarked = true;
        }, () -> {
            EffectManager.playEffect(EffectManager.Effect.TELEPORTATION, doorPos.x, doorPos.y, doorPos.z, (ServerLevel) level);
            BlockPos spawnPos = findSafeSpawn(labyrinthLevel, doorEntity.blockPosition(), 20).orElse(null);
            if(spawnPos == null) {
                return;
            }
            target.teleportTo(labyrinthLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, Set.of(), target.getYRot(), target.getXRot());
        }, (ServerLevel) level);
    }

    public static Optional<BlockPos> findSafeSpawn(ServerLevel level, BlockPos origin, int maxRadius) {
        var border = level.getWorldBorder();
        int minY   = level.getMinBuildHeight();
        int maxY   = level.getMaxBuildHeight() - 2;

        for (int radius = 0; radius <= maxRadius; radius++) {
            Iterable<BlockPos> ring = radius == 0
                    ? List.of(origin)
                    : ringPositions(origin, radius);

            for (BlockPos candidate : ring) {
                if (!border.isWithinBounds(candidate)) continue;

                for (int y = Math.min(maxY, origin.getY() + maxRadius); y >= minY + 1; y--) {
                    BlockPos feet  = new BlockPos(candidate.getX(), y,     candidate.getZ());
                    BlockPos head  = new BlockPos(candidate.getX(), y + 1, candidate.getZ());
                    BlockPos floor = new BlockPos(candidate.getX(), y - 1, candidate.getZ());

                    if (y + 1 > maxY || y - 1 < minY) continue;

                    if (!isPassable(level.getBlockState(floor), level, floor)
                            && isPassable(level.getBlockState(feet), level, feet)
                            && isPassable(level.getBlockState(head), level, head)) {
                        return Optional.of(feet);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isPassable(BlockState state, Level level, BlockPos pos) {
        return state.getCollisionShape(level, pos).isEmpty();
    }

    private static Iterable<BlockPos> ringPositions(BlockPos center, int radius) {
        List<BlockPos> positions = new ArrayList<>();
        int cx = center.getX(), cz = center.getZ(), y = center.getY();
        for (int i = -radius; i <= radius; i++) {
            positions.add(new BlockPos(cx + i,      y, cz - radius));
            positions.add(new BlockPos(cx + i,      y, cz + radius));
            positions.add(new BlockPos(cx - radius, y, cz + i));
            positions.add(new BlockPos(cx + radius, y, cz + i));
        }
        return positions;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("door", 1);
    }

    @Override
    protected float getSpiritualityCost() {
        return 4000;
    }
}
