package de.jakob.lotm.beyonders.abilities.sun;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PurificationHaloAbility extends Ability {
    public PurificationHaloAbility(String id) {
        super(id, 9, "purification", "light_weak");
        interactionRadius = 15;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(5, 6, 7, 8, 8, 9));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(3000f, 1300f, 830f, 550f, 550f, 475f));

        baseDamage = 2f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 120;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

        RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 30, 20 * 20, 252 / 255f, 173 /255f, 3 / 255f, .85f, 1f, 2f, .24f, true, (ServerLevel) level);
        AtomicDouble radius = new AtomicDouble(.5);
        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, radius.get() - .25, radius.get() + .25, baseDamage, entity.position(), true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, entity));
            radius.addAndGet(.25f);
        });
    }
}
