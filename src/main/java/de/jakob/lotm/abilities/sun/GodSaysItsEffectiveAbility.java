package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class GodSaysItsEffectiveAbility extends AbilityItem {
    public GodSaysItsEffectiveAbility(Properties properties) {
        super(properties, 20);
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
        ServerScheduler.scheduleForDuration(0, 35, 20 * 20, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.getEyePosition().subtract(0, .4, 0), 25, 5, 0);
            RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 6, 20 * 2, 252 / 255f, 173 /255f, 3 / 255f, .65f, .5f, 1f, .5f, true, (ServerLevel) level);
        }, () -> {
            BeyonderData.removeModifier(entity, "notary_buff");
        }, (ServerLevel) level);
    }
}
