package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.BloomingAreaEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PlagueAbility extends Ability {
    public PlagueAbility(String id) {
        super(id, 120, "plague");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 400;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 10f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;

        ServerScheduler.scheduleForDuration(0, 20, 20 * 80, () -> {
            if (entity.level().isClientSide)
                return;

            // Disease is suppressed by purification, cleansing, life aura, or blooming interactions
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = BeyonderData.getSequence(entity);
            if(InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) ||
                    InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq))
                return;

            boolean bloomingNearby = InteractionHandler.isInteractionPossible(currentLoc, "blooming", seq);
            float damageMult = (bloomingNearby) ? 0.4f : 1f;

            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 50, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), dust, entity.position(), 160, 50, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.WITHER, 20, 3, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 70, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 70, DamageLookup.lookupDps(4, .3, 20, 20) * (float) multiplier(entity) * damageMult, entity.position(), true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}
