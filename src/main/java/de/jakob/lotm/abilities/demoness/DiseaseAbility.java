package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DiseaseAbility extends Ability {
    public DiseaseAbility(String id) {
        super(id, 120, "disease");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        ServerScheduler.scheduleForDuration(0, 20, (int) (20 * 40*multiplier(entity)), () -> {
            if(entity.level().isClientSide)
                return;

            // Disease is suppressed by purification, cleansing, life aura, or blooming interactions
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if(InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) ||
               InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq))
                return;

            boolean bloomingNearby = InteractionHandler.isInteractionPossible(currentLoc, "blooming", seq);
            float damageMult = (bloomingNearby) ? 0.4f : 1f;

            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 30, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 20*multiplier(entity), entity.position(), new MobEffectInstance(MobEffects.POISON, 20, 0, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 20*multiplier(entity), (float) DamageLookup.lookupDps(5, .2, 35, 20) *(int) Math.max(multiplier(entity)/6,1) * damageMult, entity.position(), true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));
        }, () -> clearArtifactScaling(entity), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}
