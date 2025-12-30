package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class SurgeOfDarknessAbility extends AbilityItem {
    public SurgeOfDarknessAbility(Properties properties) {
        super(properties, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        Vec3 center = entity.position();
        double maxRadius = 40.0;

        // Get all solid blocks in the sphere
        List<BlockPos> allBlocks = AbilityUtil.getBlocksInSphereRadius(
                serverLevel,
                center,
                maxRadius,
                true,  // filled
                true,  // exclude empty blocks (only solid blocks)
                false  // not only exposed
        );

        // Group blocks by distance from center
        Map<Integer, List<BlockPos>> blocksByDistance = new HashMap<>();
        for (BlockPos pos : allBlocks) {
            double distance = Math.sqrt(pos.distToCenterSqr(center));
            int radiusGroup = (int) Math.ceil(distance);
            blocksByDistance.computeIfAbsent(radiusGroup, k -> new ArrayList<>()).add(pos);
        }

        // Schedule the expansion wave (5 seconds = 100 ticks)
        int expansionTicks = 100;
        int ticksPerRadius = expansionTicks / (int) maxRadius;

        for (int radius = 1; radius <= maxRadius; radius++) {
            List<BlockPos> blocksAtRadius = blocksByDistance.getOrDefault(radius, new ArrayList<>());
            if (blocksAtRadius.isEmpty()) continue;

            int delay = (radius - 1) * ticksPerRadius;
            final int currentRadius = radius;

            ServerScheduler.scheduleDelayed(delay, () -> {
                // Send packet to nearby players (10 seconds duration = 10000ms)
                long durationMs = 10000;
                
            }, serverLevel);
        }
    }
}