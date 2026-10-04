package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class FloatAbility extends ToggleAbility {
    private static final double SEARCH_PADDING = 0.1; // how far out to look for water please keep this low or else it will activate on land
    public FloatAbility(String id) {
        super(id);
        tickRate = 1;
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 6));
    }
    @Override
    public float getSpiritualityCost() {
        return 1;
    }
    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;}
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        Double platformTop = findWaterPlatformTop(level, entity);
        if (platformTop != null) {
            entity.setNoGravity(true);
            entity.setPos(entity.getX(), platformTop, entity.getZ());
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, 0, motion.z);
            entity.fallDistance = 0;
            entity.setOnGround(true);
        } else {
            entity.setNoGravity(false);
        }
    }
    private Double findWaterPlatformTop(Level level, LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(SEARCH_PADDING, 0, SEARCH_PADDING);
        double[] xs = {box.minX, entity.getX(), box.maxX};
        double[] zs = {box.minZ, entity.getZ(), box.maxZ};
        int bestBlockY = Integer.MIN_VALUE;
        boolean found = false;
        for (double x : xs) {
            for (double z : zs) {
                BlockPos pos = BlockPos.containing(x, entity.getY() - 0.1, z);
                FluidState fluid = level.getFluidState(pos);
                if (fluid.is(FluidTags.WATER)) {
                    if (pos.getY() > bestBlockY) bestBlockY = pos.getY();
                    found = true;
                }
            }
        }
        return found ? (double) (bestBlockY + 1) : null;
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.setNoGravity(false);
        entity.setOnGround(false);
    }
}
