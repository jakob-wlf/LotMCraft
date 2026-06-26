package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
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
        super(id, 45);
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 10000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 27*(int) multiplier(entity), 2);

        EffectManager.playEffect(EffectManager.Effect.SPACE_DISTORTION, targetLoc.x(), targetLoc.y(), targetLoc.z(), serverLevel);

        ServerScheduler.scheduleForDuration(0, 2, (int) (20 * 15*multiplier(entity)), () -> AbilityUtil.getAllNearbyEntities(entity, serverLevel, targetLoc, 70*multiplier(entity)).forEach(e -> {
            e.setDeltaMovement(targetLoc.subtract(e.position()).scale(.04));
            BlockPos nextPos = BlockPos.containing(e.position().add(targetLoc.subtract(e.position()).scale(.4)));
            if(!serverLevel.getBlockState(nextPos).getCollisionShape(serverLevel, nextPos).isEmpty()) {
                e.teleportTo(e.getX(), e.getY() + 1, e.getZ());
            }
            e.hurtMarked = true;
        }));
    }
}
