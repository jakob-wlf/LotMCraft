package de.jakob.lotm.abilities.sun;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PurificationHaloAbility extends AbilityItem {
    public PurificationHaloAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 40;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 30, 20 * 20, 252 / 255f, 173 /255f, 3 / 255f, .85f, 1f, 2f, .24f, true, (ServerLevel) level);
        AtomicDouble radius = new AtomicDouble(.5);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, radius.get() - .25, radius.get() + .25,15 * multiplier(entity), entity.position(), true, false, true, 0);
            radius.addAndGet(.25f);
        });
    }
}
