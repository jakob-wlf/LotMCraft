package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThunderclapAbility extends AbilityItem {
    public ThunderclapAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 50, 2);
        Vec3 entityLoc = entity.position();
        Vec3 dir = targetLoc.subtract(entityLoc.add(0, 7, 0)).normalize();
        entity.teleportTo(entityLoc.x, entityLoc.y + 7, entityLoc.z);
        ServerScheduler.scheduleDelayed(1, () -> {
            entity.setDeltaMovement(dir.scale(targetLoc.distanceTo(entityLoc.add(0, 7, 0)) + 1));
            entity.hurtMarked = true;
        });

        AtomicBoolean hasLanded = new AtomicBoolean(false);

        ServerScheduler.scheduleForDuration(0, 0, 20 * 3, () -> {
            if(hasLanded.get())
                return;
            entity.fallDistance = 0;
            if(entity.position().distanceTo(targetLoc) < 3) {
                hasLanded.set(true);
                entity.setDeltaMovement(0, 0, 0);
                entity.hurtMarked = true;
            }
        });
    }
}
