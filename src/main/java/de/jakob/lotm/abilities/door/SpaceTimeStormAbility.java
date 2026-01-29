package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceTimeStormAbility extends Ability {
    public SpaceTimeStormAbility(String id) {
        super(id, 12);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 1700;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        Vec3 center = AbilityUtil.getTargetLocation(entity, 60, 3);

        EffectManager.playEffect(EffectManager.Effect.SPACE_FRAGMENTATION, center.x, center.y, center.z, serverLevel);

        List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius(serverLevel, center, 35, true, true, false);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 12, () -> {
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 35, DamageLookup.lookupDps(1, .75, 2, 20) * (float) multiplier(entity), center, true, false);

            if(griefing) {
                blocks.stream().filter(b -> random.nextInt(175) == 0 && !serverLevel.getBlockState(b).is(ModBlocks.VOID.get())).forEach(b -> serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState()));
                blocks.stream().filter(b -> random.nextInt(60) == 0 && !serverLevel.getBlockState(b).isAir()).forEach(b -> serverLevel.setBlockAndUpdate(b, ModBlocks.VOID.get().defaultBlockState()));
            }
        });
    }
}
