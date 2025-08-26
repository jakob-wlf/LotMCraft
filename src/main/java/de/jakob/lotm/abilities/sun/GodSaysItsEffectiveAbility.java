package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class GodSaysItsEffectiveAbility extends AbilityItem {
    public GodSaysItsEffectiveAbility(Properties properties) {
        super(properties, 20 * 20);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 25;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
        BeyonderData.addModifier(entity, "notary_buff", 1.35);
        RingExpansionRenderer.createPulsingRingForAll(entity.getEyePosition().subtract(0, .4, 0), 8, 2, 30, 8, 252, 173, 3, .65f, .8f, .75f, (ServerLevel) level);

        ServerScheduler.scheduleForDuration(0, 5, 20 * 20, () -> {

        }, () -> {
            BeyonderData.removeModifier(entity, "notary_buff");
        }, (ServerLevel) level);
    }
}
