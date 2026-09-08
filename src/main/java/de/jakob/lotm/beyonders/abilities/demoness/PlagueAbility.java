package de.jakob.lotm.beyonders.abilities.demoness;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
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
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlagueAbility extends Ability {
    public PlagueAbility(String id) {
        super(id, 180, "plague", "disease");
        autoClear = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(13, 23, 28, 35, 40));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(10000f, 4000f, 2500f, 1700f, 1700f));

        baseDamage = 2.3f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 10f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;

        float damage = baseDamage;

        ServerScheduler.scheduleForDuration(0, 20, (20 * 20), () -> {
            if (entity.level().isClientSide)
                return;

            // Disease is suppressed by purification, cleansing, life aura, or blooming interactions
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if(InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) ||
                    InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq))
                return;

            boolean bloomingNearby = InteractionHandler.isInteractionPossible(currentLoc, "blooming", seq);
            float damageMult = (bloomingNearby) ? 0.4f : 1f;

            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 50, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), dust, entity.position(), 160, 50, 0.02);

            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*multiplier(entity), entity.position(), new MobEffectInstance(MobEffects.WITHER, 20, 3, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*multiplier(entity), entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*multiplier(entity), entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, false));

            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(),
                    entity, 45,
                    damage * damageMult,
                    entity.position(), true, false, true, 0,
                    ModDamageTypes.source(level, ModDamageTypes.PLAGUE, entity));

        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}
