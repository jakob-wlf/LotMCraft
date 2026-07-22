package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.beyonders.abilities.sun.HolyOathAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HorrorAuraAbility extends Ability {
    public HorrorAuraAbility(String id) {
        super(id, 30, "darkness");
        autoClear = false;
        postsUsedAbilityEventManually = true;
        interactionRadius = 20;
        interactionCacheTicks = 5;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 2500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        float multiplier = multiplier(entity);
        Location loc = new Location(entity.position(), serverLevel);
        UUID effectID = MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.HORROR_AURA, loc, 20 * 25 *(int) Math.max(multiplier/2,1), false, serverLevel, entity);

        AtomicInteger ticks = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 15*(int) Math.max(multiplier(entity)/3, 1), () -> {
            loc.setPosition(entity.position());
            loc.setLevel(serverLevel);
            MovableEffectManager.updateEffectPosition(effectID, loc, serverLevel);

            NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));

            // Horror Aura is suppressed by purification
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if(InteractionHandler.isInteractionPossible(loc, "purification", seq)) {
                ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition().subtract(0, .5, 0), 1, 30);
                return;
            }


            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20*Math.max(multiplier/2, 1)).forEach(e -> {
                // Entity is freed from Horror Aura by morale-boosting abilities
                Location eLoc = new Location(e.position(), serverLevel);
                int eSeq = BeyonderData.getSequence(e);
                boolean hasMorale = InteractionHandler.isInteractionPossibleForEntity(eLoc, "morale_boost", seq, e);
                // Also check if the entity has an active HolyOath (ToggleAbility)
                if(!hasMorale) {
                    hasMorale = ToggleAbility.getActiveAbilitiesForEntity(e).stream()
                            .anyMatch(a -> a instanceof HolyOathAbility) && eSeq <= seq;
                }
                if(hasMorale)
                    return;

                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 5, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 4, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4, false, false, false));

                BeyonderData.addModifier(e, "horror_aura", .4);
                if(AbilityUtil.isTargetSignificantlyWeaker(seq, BeyonderData.getSequence(e)) && ticks.get() % 10 == 0) {
                    e.hurt(ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity), (float) (DamageLookup.lookupDps(3, .95, 10, 20) *
                            multiplier(entity)));
                }

                SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                sanityComponent.decreaseSanityWithSequenceDifference(0.02168f*multiplier(entity), e, AbilityUtil.getSeqWithArt(entity, this), BeyonderData.getSequence(e));
            });
            ticks.getAndIncrement();
        }, () -> this.clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
}
