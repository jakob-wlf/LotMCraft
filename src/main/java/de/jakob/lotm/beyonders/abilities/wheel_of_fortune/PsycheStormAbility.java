package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryLoosingControlHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PsycheStormAbility extends Ability {
    public PsycheStormAbility(String id) {
        super(id, 7);

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 2, 3, 5, 6, 7));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(11000f, 4100f, 2500f, 1750f, 1700f, 1100f, 1000f));

        baseDamage = 4f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 80;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(192 / 255f, 246 / 255f, 252 / 255f),
            3.5f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        float multiplier = multiplier(entity);
        int seq = AbilityUtil.getSeqWithArt(entity, this);

        float damage = baseDamage;

        if(seq > 4) {
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 10 * Math.max(multiplier, 1), ModDamageTypes.SPIRITUAL, damage, entity.getEyePosition(), true, false);
        }
        else{
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 10 * Math.max(multiplier, 1), ModDamageTypes.SPIRITUAL, damage/2, entity.getEyePosition(), true, false);
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 10 * Math.max(multiplier, 1), ModDamageTypes.AWE, damage/2, entity.getEyePosition(), true, false);
        }

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.getEyePosition(), 10 * Math.max(multiplier, 1)).forEach(e -> {
            boolean shouldFail = VisionaryHandler.shouldFailAndTrigger(seq, entity, e, this, false);

            if(!shouldFail) {
                VisionaryLoosingControlHandler.applyEffect(entity, e, this);
            }
        });

        Location loc = new Location(entity.position(), serverLevel);

        ParticleUtil.createExpandingParticleSpirals(dust, loc, 3, 4 * Math.max(multiplier / 2, 1), 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 5, 6 * Math.max(multiplier / 2, 1), 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 7, 8 * Math.max(multiplier / 2, 1), 2, .5, 4, 90, 7, 2);
        ParticleUtil.createExpandingParticleSpirals(dust, loc, 9, 10 * Math.max(multiplier / 2, 1), 2, .5, 4, 90, 7, 2);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition(), 150, 7, 3, 7, 0);

        serverLevel.playSound(null, BlockPos.containing(loc.getPosition()), SoundEvents.BREEZE_DEATH, SoundSource.BLOCKS);

    }
}
