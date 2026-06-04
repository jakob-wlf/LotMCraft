package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
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

public class FearAuraAbility extends Ability {

    public FearAuraAbility(String id) {
        super(id, 30, "darkness");
        autoClear = false;
        postsUsedAbilityEventManually = true;
        interactionRadius = 20;
        interactionCacheTicks = 5;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Location loc = new Location(entity.position(), serverLevel);
        UUID effectID = MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.FEAR_AURA, loc, 20 * 25*(int) Math.max(multiplier(entity)/4, 1), false, serverLevel);

        AtomicInteger ticks = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 15*(int) Math.max(multiplier(entity)/4,1), () -> {
            loc.setPosition(entity.position());
            loc.setLevel(serverLevel);
            MovableEffectManager.updateEffectPosition(effectID, loc, serverLevel);

            NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));

            if(InteractionHandler.isInteractionPossible(new Location(entity.position(), serverLevel), "purification", AbilityUtil.getSeqWithArt(entity, this)) ||
                    InteractionHandler.isInteractionPossible(new Location(entity.position(), serverLevel), "sealing", AbilityUtil.getSeqWithArt(entity, this))) {
                ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition().subtract(0, .5, 0), 1, 30);
                return;
            }
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 20*Math.max(multiplier(entity)/4, 1), entity.position(),
                    new MobEffectInstance(MobEffects.DARKNESS, 60, 5, false, false, false),
                    new MobEffectInstance(MobEffects.BLINDNESS, 60, 4, false, false, false),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4, false, false, false));

            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20*(int) Math.max(multiplier(entity)/4,1)).forEach(e -> {
                BeyonderData.addModifier(e, "fear_aura", .4);

                int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
                int targetSeq = BeyonderData.getSequence(e);

                if (AbilityUtil.isTargetSignificantlyWeaker(entitySeq, targetSeq) && ticks.get() % 10 == 0) {
                    e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDps(3, 0.6, 10, 20) *
                           multiplier(entity)));
                }

                SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                sanityComponent.decreaseSanityWithSequenceDifference(0.02168f*(int) Math.max(multiplier(entity)/4,1), e, AbilityUtil.getSeqWithArt(entity, this), BeyonderData.getSequence(e));
            });
            ticks.getAndIncrement();
        }, () -> this.clearArtifactScaling(entity), serverLevel);
    }
}
