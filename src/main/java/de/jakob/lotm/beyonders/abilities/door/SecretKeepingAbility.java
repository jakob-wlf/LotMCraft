package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class SecretKeepingAbility extends Ability {
    public SecretKeepingAbility(String id) {
        super(id, 20);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if(level.isClientSide) return;

        level.playSound(null, caster.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, caster.getSoundSource(), 1, 1);
        List<LivingEntity> affectedEntities = AbilityUtil.getNearbyEntities(null, (ServerLevel) level, caster.position(), 5)
                                                .stream()
                                                .filter(e -> e.equals(caster) || AllyUtil.areAllies(caster, e))
                                                .toList();

        for(LivingEntity entity : affectedEntities) {
            ParticleUtil.createParticleSpirals((ServerLevel) level, ParticleTypes.ENCHANT, entity.position().add(0, entity.getBbHeight() / 2, 0), .8, .8, entity.getBbHeight(), .25, 5, 35, 8, 1);
            ParticleUtil.createParticleSpirals((ServerLevel) level, ParticleTypes.ENCHANTED_HIT, entity.position().add(0, entity.getBbHeight() / 2, 0), 1.2, 1.2, entity.getBbHeight(), .25, 5, 35, 10, 1);

            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 20 * 60, 4));
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("door", 4);
    }

    @Override
    protected float getSpiritualityCost() {
        return 1500;
    }
}
