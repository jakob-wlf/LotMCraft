package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class MisfortuneFieldAbility extends Ability {
    private static final int luckCost = 200;
    private static final float baseLuckDrainRatePerMinute = 80;
    private static final int luckDrainDurationTicks = 20 * 20;

    public MisfortuneFieldAbility(String id) {
        super(id, 30);
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    @Override
    public int luckCost() {
        return luckCost;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!PassiveLuckAccumulationAbility.consumeStoredLuck(entity, luckCost)) {
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cMisfortune Field requires more luck."));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_FIELD, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        Vec3 startPos = entity.position();
        float multiplier = multiplier(entity);
        float drainRate = Math.min(LuckManager.getSequenceScaledEffectRate(entity,
            Math.round(multiplier * 6.25f) * baseLuckDrainRatePerMinute), 6500);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 20*multiplier).forEach(e -> {
                LuckManager.applyLuckDrain(entity, e, LuckManager.sourceForCaster("misfortune_field", entity),
                        drainRate, luckDrainDurationTicks);
            });
        });
    }
}
