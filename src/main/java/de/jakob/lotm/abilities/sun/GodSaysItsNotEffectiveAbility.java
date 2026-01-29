package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
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
import java.util.List;
import java.util.Map;

public class GodSaysItsNotEffectiveAbility extends Ability {
    public GodSaysItsNotEffectiveAbility(String id) {
        super(id, 20);
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
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 15).stream().filter(BeyonderData::isBeyonder).toList();

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);
        nearbyEntities.forEach(e -> BeyonderData.addModifier(e, "notary_debuff", .8));
        ServerScheduler.scheduleForDuration(0, 35, 20 * 20, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.getEyePosition().subtract(0, .4, 0), 25, 5, 0);
            RingEffectManager.createRingForAll(entity.getEyePosition().subtract(0, .4, 0), 6, 20 * 2, 252 / 255f, 173 /255f, 3 / 255f, .65f, .5f, 1f, .5f, true, (ServerLevel) level);
        }, () -> {
            nearbyEntities.forEach(e -> BeyonderData.removeModifier(e, "notary_debuff"));
        }, (ServerLevel) level);
    }
}
