package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpaceDistortionAbility extends Ability {
    public SpaceDistortionAbility(String id) {
        super(id, 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27, 2);

        EffectManager.playEffect(EffectManager.Effect.SPACE_DISTORTION, targetLoc.x(), targetLoc.y(), targetLoc.z(), serverLevel);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 60, () -> AbilityUtil.getAllNearbyEntities(entity, serverLevel, targetLoc, 70).forEach(e -> {
            e.setDeltaMovement(targetLoc.subtract(e.position()).scale(.04));
            BlockPos nextPos = BlockPos.containing(e.position().add(targetLoc.subtract(e.position()).scale(.4)));
            if(!serverLevel.getBlockState(nextPos).getCollisionShape(serverLevel, nextPos).isEmpty()) {
                e.teleportTo(e.getX(), e.getY() + 1, e.getZ());
            }
            e.hurtMarked = true;
        }));
    }
}
