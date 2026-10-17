package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.beyonders.abilities.sun.HolyOathAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HorrorAuraAbility extends ToggleAbility {
    private static final int RADIUS = 20;
    private static final int STARE_CAP = 20 * 20;

    private final Map<UUID, UUID> effects = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> staring = new HashMap<>();

    public HorrorAuraAbility(String id) {
        super(id, "darkness");
        autoClear = false;
        doesNotIncreaseDigestion = false;
        postsUsedAbilityEventManually = true;
        interactionRadius = RADIUS;
        interactionCacheTicks = 5;
        canBeShared = false;
        tickRate = 10;
        baseDamage = 2;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        effects.put(entity.getUUID(), EffectManager.playMovableEffect(EffectIds.HORROR_AURA, serverLevel, entity, EffectParams.asInfinite()));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        staring.remove(entity.getUUID());
        UUID effect = effects.remove(entity.getUUID());
        if (effect != null && level instanceof ServerLevel serverLevel) EffectManager.cancelEffect(effect, serverLevel);
        clearArtifactScaling(entity);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (entity.level() != serverLevel) {
            cancel(serverLevel, entity);
            return;
        }

        UUID effect = effects.get(entity.getUUID());
        if (effect != null) EffectManager.updateEffectPosition(effect, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        Location loc = new Location(entity.position(), serverLevel);
        if (InteractionHandler.isInteractionPossible(loc, "purification", seq)) {
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition().subtract(0, .5, 0), 1, 30);
            return;
        }

        Map<UUID, Integer> stares = staring.computeIfAbsent(entity.getUUID(), id -> new HashMap<>());
        Set<UUID> seen = new HashSet<>();
        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), RADIUS)) {
            Location targetLoc = new Location(target.position(), serverLevel);
            int targetSeq = BeyonderData.getSequence(target);
            boolean hasMorale = InteractionHandler.isInteractionPossibleForEntity(targetLoc, "morale_boost", seq, target);
            if (!hasMorale) {
                hasMorale = ToggleAbility.getActiveAbilitiesForEntity(target).stream().anyMatch(a -> a instanceof HolyOathAbility) && targetSeq <= seq;
            }
            if (hasMorale) continue;

            seen.add(target.getUUID());
            boolean looking = AbilityUtil.getTargetEntity(target, RADIUS, 5f) == entity;
            int stare = stares.getOrDefault(target.getUUID(), 0);
            stare = looking ? Math.min(STARE_CAP, stare + tickRate) : Math.max(0, stare - tickRate);
            if (stare == 0) stares.remove(target.getUUID());
            else stares.put(target.getUUID(), stare);

            int seconds = stare / 20;
            int stareAmp = Math.min(4, seconds / 2);
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 3, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 3, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, Math.max(2, stareAmp), false, false, false));
            if (stare > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, stareAmp, false, false, false));
            }

            BeyonderData.addModifier(target, "horror_aura", .6);
            //target.hurt(ModDamageTypes.source(level, ModDamageTypes.HORROR, entity), baseDamage);

            if (!looking) continue;
            target.getData(ModAttachments.SANITY_COMPONENT.get()).decreaseSanityWithSequenceDifference(
                    sanityAmount(seq) * (1f + seconds * 0.35f), target, targetSeq, seq);
            if (seconds >= 2 && target.getRandom().nextInt(Math.max(8, 28 - seconds)) == 0) {
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 40, 0, false, true));
                target.hurt(ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity), 2f + seconds * 0.25f);
            }
            if (seconds >= 8 && target.getRandom().nextInt(60) == 0) {
                target.hurt(ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity), Float.MAX_VALUE);
            }
        }
        stares.keySet().removeIf(id -> !seen.contains(id));
    }

    private static float sanityAmount(int seq) {
        return switch (seq) {
            case 4 -> 0.04168f;
            case 3 -> 0.06168f;
            case 2 -> 0.09168f;
            case 1 -> 0.12168f;
            case 0 -> 0.19f;
            default -> 0.06168f;
        };
    }
}
