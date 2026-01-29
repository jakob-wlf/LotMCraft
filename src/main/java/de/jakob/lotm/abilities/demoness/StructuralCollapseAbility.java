package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class StructuralCollapseAbility extends Ability {
    public StructuralCollapseAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 20, 3);

        // Collapse the area
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        if(griefing) {
            collapseArea(serverLevel, targetLoc);
        }

        // Damage entities
        AbilityUtil.damageNearbyEntities(serverLevel, entity, 35, DamageLookup.lookupDamage(2, .8) * (float) multiplier(entity), targetLoc, true, true);

        // Play Effect
        EffectManager.playEffect(EffectManager.Effect.COLLAPSE, targetLoc.x, targetLoc.y - 1.5, targetLoc.z, serverLevel);
    }

    private void collapseArea(ServerLevel serverLevel, Vec3 targetLoc) {
        HashSet<BlockPos> blocksWithoutSolidFoundation = new HashSet<>();

        // Get all blocks in radius
        List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(serverLevel, targetLoc, 40, 23, true, true, false);

        // Get all blocks that can fall
        for(BlockPos blockPos : blocks) {
            if ((!serverLevel.getBlockState(blockPos.below()).getCollisionShape(serverLevel, blockPos.below()).isEmpty() && !blocksWithoutSolidFoundation.contains(blockPos.below())) || serverLevel.getBlockState(blockPos).is(Blocks.BEDROCK)) {
                continue;
            }

            blocksWithoutSolidFoundation.add(blockPos);
        }

        // Sort by Y levels and make them fall one level at a time
        int lowestY = blocksWithoutSolidFoundation.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int highestY = blocksWithoutSolidFoundation.stream().mapToInt(BlockPos::getY).max().orElse(0);

        for(int i = lowestY; i < highestY + 1; i++) {
            final int targetY = i;
            final int delay = (i - lowestY) * 8;

            // Schedule falling blocks delayed
            ServerScheduler.scheduleDelayed(delay, () -> {
                for(BlockPos blockPos : blocksWithoutSolidFoundation.stream().filter(b -> b.getY() == targetY).toList()) {

                    // Make block fall
                    BlockState blockState = serverLevel.getBlockState(blockPos);
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    FallingBlockEntity.fall(serverLevel, blockPos, blockState);
                }
            });
        }
    }
}
