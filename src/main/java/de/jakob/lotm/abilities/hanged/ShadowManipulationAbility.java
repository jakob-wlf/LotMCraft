package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ShadowManipulationAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/hair_entanglement_ability.png");
    private static final float SPIRITUALITY_COST = 150.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 7.0f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 5.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.5f;
    private static final float MAX_AREA_SCALE_SEQ1 = 4.0f;
    private static final float SHEPHERD_DAMAGE_MULTIPLIER = 1.5f;

    private static final int BASE_DURATION_TICKS = 20 * 10;
    private static final int TICK_INTERVAL = 5;
    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.shadow_manipulation.no_target";

    public ShadowManipulationAbility(String id) {
        super(id, 10f, "concealment", "darkness");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DURATION_SCALE_SEQ1);
        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_RANGE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        boolean shepherdMastery = BeyonderData.getPathway(entity).equals(HangedPathwayConstants.PATHWAY_ID)
                && BeyonderData.getSequence(entity) <= HangedPathwayConstants.SEQUENCE_SHEPHERD;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, Math.max(22, Math.round(22 * rangeScale)), 1.8f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    net.minecraft.network.chat.Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        int duration = BASE_DURATION_TICKS;
        if (AbilityUtil.isTargetSignificantlyStronger(casterSeq, targetSeq)) {
            duration = 20 * 4;
        } else if (AbilityUtil.isTargetSignificantlyWeaker(casterSeq, targetSeq)) {
            duration = 20 * 16;
        }
        duration = Math.round(duration * durationScale);

        if (target instanceof Mob mob) {
            mob.setNoAi(true);
        }
        if (BeyonderData.isBeyonder(target)) {
            target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).disableAbilityUsageForTime("shadow_manipulation", duration, target);
        }
        HangedRenderEffectUtil.playMovableAt(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_BINDING, serverLevel, target, duration, false);
        HangedEffectUtil.playShadowCast(serverLevel, target.position());
        HangedEffectUtil.spawnShadowBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), (shepherdMastery ? 1.1 : 0.8) * areaScale, shepherdMastery ? 34 : 24);
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                (float) (DamageLookup.lookupDamage(7, 0.55f * damageScale * (shepherdMastery ? SHEPHERD_DAMAGE_MULTIPLIER : 1.0f)) * multiplier(entity)));

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        int finalDuration = duration;
        int[] pulseCounter = {0};
        UUID taskId = ServerScheduler.scheduleForDuration(0, TICK_INTERVAL, finalDuration, () -> {
            if (target.isDeadOrDying()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Location targetLocation = new Location(target.position(), target.level());
            if (InteractionHandler.isInteractionPossibleForEntity(targetLocation, "escape", casterSeq, target)) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Vec3 pos = target.position();
            HangedEffectUtil.spawnShadowBurst(serverLevel, pos.add(0, target.getBbHeight() * 0.5, 0), (shepherdMastery ? 1.15 : 0.85) * areaScale, shepherdMastery ? 36 : 28);
            if (pulseCounter[0] % 4 == 0) {
                HangedEffectUtil.playShadowPulse(serverLevel, pos, shepherdMastery ? 0.55f : 0.7f);
                target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, 0.18f * damageScale * (shepherdMastery ? SHEPHERD_DAMAGE_MULTIPLIER : 1.0f)) * multiplier(entity)));
                if (shepherdMastery) {
                    for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(1.8 * areaScale),
                            nearby -> nearby != entity && nearby != target && nearby.isAlive())) {
                        nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                                (float) (DamageLookup.lookupDamage(7, 0.1f * damageScale) * multiplier(entity)));
                        nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, false, false, true));
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, true));
                    }
                }
            }
            pulseCounter[0]++;

            target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20, 1, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 1, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 128, false, false, true));
            if (shepherdMastery) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1, false, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20, 0, false, false, true));
            }
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            target.teleportTo(pos.x, pos.y, pos.z);
        }, () -> {
            if (target instanceof Mob mob) {
                mob.setNoAi(false);
            }
            HangedEffectUtil.spawnShadowBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), 0.8 * areaScale, 18);
            clearArtifactScaling(entity);
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
        taskIdRef.set(taskId);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
