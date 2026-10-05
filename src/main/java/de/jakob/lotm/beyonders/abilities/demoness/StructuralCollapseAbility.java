package de.jakob.lotm.beyonders.abilities.demoness;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
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

import java.util.*;

public class StructuralCollapseAbility extends Ability {
    public StructuralCollapseAbility(String id) {
        super(id, 15, "destruction");
        interactionRadius = 35;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(7, 13, 15));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(15000f, 6700f, 5000f));

        baseDamage = 34f;
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

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, baseDistance, 3);

        // Collapse the area
//        boolean griefing = BeyonderData.isGriefingEnabled(entity);
//        if(griefing) {
//            collapseArea(serverLevel, targetLoc);
//        }

        // Damage entities
        AbilityUtil.damageNearbyEntities(serverLevel, entity, 35,
                baseDamage, targetLoc, true, true,
                ModDamageTypes.source(level, ModDamageTypes.CHAOS, entity));

        EffectManager.playEffect(EffectIds.COLLAPSE, targetLoc.x, targetLoc.y - 1.5, targetLoc.z, serverLevel, entity);
    }

    private void collapseArea(ServerLevel serverLevel, Vec3 targetLoc) {
        HashSet<BlockPos> blocksWithoutSolidFoundation = new HashSet<>();

        List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(serverLevel, targetLoc, 40, 23, true, true, false);

        for(BlockPos blockPos : blocks) {
            if ((!serverLevel.getBlockState(blockPos.below()).getCollisionShape(serverLevel, blockPos.below()).isEmpty() && !blocksWithoutSolidFoundation.contains(blockPos.below())) || serverLevel.getBlockState(blockPos).is(Blocks.BEDROCK)) {
                continue;
            }

            blocksWithoutSolidFoundation.add(blockPos);
        }

        int lowestY = blocksWithoutSolidFoundation.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int highestY = blocksWithoutSolidFoundation.stream().mapToInt(BlockPos::getY).max().orElse(0);

        for(int i = lowestY; i < highestY + 1; i++) {
            final int targetY = i;
            final int delay = (i - lowestY) * 8;

            ServerScheduler.scheduleDelayed(delay, () -> {
                for(BlockPos blockPos : blocksWithoutSolidFoundation.stream().filter(b -> b.getY() == targetY).toList()) {

                    BlockState blockState = serverLevel.getBlockState(blockPos);
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    FallingBlockEntity.fall(serverLevel, blockPos, blockState);
                }
            });
        }
    }
}
