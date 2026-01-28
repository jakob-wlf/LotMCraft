package de.jakob.lotm.abilities.sun;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AnimationUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PureWhiteLightAbility extends Ability {
    public PureWhiteLightAbility(String id) {
        super(id, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            if(entity instanceof Player player) {
                AnimationUtil.playOpenArmAnimation(player);
            }
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 50, 4);
        for(int i = 0; i < 50; i++) {
            BlockPos pos = BlockPos.containing(targetLoc);
            BlockState blockState = serverLevel.getBlockState(pos);
            if(blockState.getCollisionShape(serverLevel, pos).isEmpty()) {
                targetLoc = targetLoc.subtract(0, 1, 0);
            } else {
                targetLoc = targetLoc.add(0, 1, 0);
                break;
            }
        }
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 10.0f, 1.0f);

        EffectManager.playEffect(EffectManager.Effect.PURE_WHITE_LIGHT, targetLoc.x, targetLoc.y, targetLoc.z, serverLevel);

        AtomicDouble radius = new AtomicDouble(2);

        Vec3 finalTargetLoc = targetLoc;
        ServerScheduler.scheduleForDuration(29, 2, 110, () -> {
            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInSphereRadius(serverLevel, finalTargetLoc, radius.get(), true, true, false).forEach(blockPos -> {
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
                });
            }

            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get() - 2.1, radius.get() + 2.1, DamageLookup.lookupDamage(1, .8) * multiplier(entity), finalTargetLoc, true, false, true, 0);

            radius.addAndGet(0.8);
        });
    }
}
