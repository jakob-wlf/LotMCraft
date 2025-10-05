package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.entity.custom.LightningEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LightningStormAbility extends AbilityItem {
    public LightningStormAbility(Properties properties) {
        super(properties, 35);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.setWeatherParameters(
                0,          // clearDuration (0 = start immediately)
                20 * 20,       // rainDuration (ticks → 2400 = 2 minutes)
                true,       // raining
                true        // thundering
        );

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 25, 2, true);
        for(int i = 0; i < 35; i++) {
            BlockState state = level.getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
            if(state.getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
                targetLoc = targetLoc.subtract(0, 1, 0);
        }

        Vec3 finalTargetLoc = targetLoc;
        ServerScheduler.scheduleForDuration(0, 4, 20 * 17, () -> {
            for(int j = 0; j < random.nextInt(5, 19); j++) {
                Vec3 loc = finalTargetLoc.add(random.nextDouble(-35, 35), 6, random.nextDouble(-35, 35));
                for(int i = 0; i < 35; i++) {
                    BlockState state = level.getBlockState(BlockPos.containing(loc.subtract(0, 1, 0)));
                    if(state.getCollisionShape(level, BlockPos.containing(loc)).isEmpty())
                        loc = loc.subtract(0, 1, 0);
                }

                LightningEntity lightning = new LightningEntity(level, entity, loc, 65, 10, 30 * multiplier(entity), BeyonderData.isGriefingEnabled(entity), 8, 200, 0x4a23e8);
                level.addFreshEntity(lightning);
            }
        });


    }
}
