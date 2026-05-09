package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DevouringAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/mutation_creation_ability.png");
    private static final float SPIRITUALITY_COST = 270.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 8.0f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 4.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 3.0f;
    private static final float SHEPHERD_DAMAGE_MULTIPLIER = 1.45f;
    private static final float SHEPHERD_HEAL_MULTIPLIER = 1.4f;

    private static final int CHANNEL_DURATION = 20 * 4;
    private static final int CHANNEL_INTERVAL = 4;
    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.devouring.no_target";
    private static final String DEVOUR_MESSAGE = "ability.lotmcraft.devouring.active";
    private static final String BACKLASH_MESSAGE = "ability.lotmcraft.devouring.backlash";
    private static final String FEAST_MESSAGE = "ability.lotmcraft.devouring.feast";

    public DevouringAbility(String id) {
        super(id, 16f, "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_DURATION_SCALE_SEQ1);
        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_RANGE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_AREA_SCALE_SEQ1);
        boolean shepherdMastery = BeyonderData.getPathway(entity).equals(HangedPathwayConstants.PATHWAY_ID)
                && BeyonderData.getSequence(entity) <= HangedPathwayConstants.SEQUENCE_SHEPHERD;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, Math.max(6, Math.round(6 * rangeScale)), 2.0f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if (AbilityUtil.isTargetSignificantlyStronger(casterSeq, targetSeq)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 80, 1, false, false, false));
            entity.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.05f, entity);
            entity.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, target), 5.0f);
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(BACKLASH_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        AbilityUtil.sendActionBar(entity,
                Component.translatable(DEVOUR_MESSAGE, target.getDisplayName()).withColor(HangedPathwayConstants.pathwayColor()));
        HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        HangedEffectUtil.spawnFleshBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.45, 0), 0.7 * areaScale, 22);

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, CHANNEL_INTERVAL, Math.round(CHANNEL_DURATION * durationScale), () -> {
            if (!target.isAlive() || entity.distanceTo(target) > 6.5f * rangeScale) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);
            HangedEffectUtil.spawnFleshBurst(serverLevel, center, 0.55 * areaScale, 18);
            HangedEffectUtil.spawnFleshTrail(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.45, 0), center, 0.5);
            if (target.tickCount % 8 == 0) {
                HangedEffectUtil.playFleshPulse(serverLevel, center, 0.72f);
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 2, false, false, true));
            if (shepherdMastery) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1, false, false, true));
                if (target instanceof Player playerTarget) {
                    playerTarget.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.0025f, playerTarget);
                }
            }
            Vec3 pull = entity.position().subtract(target.position());
            if (pull.lengthSqr() > 0.001) {
                pull = pull.normalize().scale(shepherdMastery ? 0.34 : 0.24);
                target.setDeltaMovement(pull.x, Math.max(0.02, target.getDeltaMovement().y), pull.z);
            }

            float damage = (float) (DamageLookup.lookupDamage(6, 0.95f * damageScale * (shepherdMastery ? SHEPHERD_DAMAGE_MULTIPLIER : 1.0f)) * multiplier(entity));
            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), damage);
            entity.heal(damage * (0.5f + ((damageScale - 1.0f) * 0.05f)) * (shepherdMastery ? SHEPHERD_HEAL_MULTIPLIER : 1.0f));
            if (shepherdMastery) {
                for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(1.35 * areaScale),
                        nearby -> nearby != entity && nearby != target && nearby.isAlive())) {
                    nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity),
                            Math.max(2.0f, damage * 0.25f));
                    nearby.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 1, false, false, true));
                }
            }

            if (target.isDeadOrDying()) {
                entity.heal(shepherdMastery ? 14.0f : 8.0f);
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, shepherdMastery ? 220 : 140, shepherdMastery ? 2 : 1, false, false, false));
                HangedEffectUtil.spawnFleshBurst(serverLevel, center, shepherdMastery ? 1.5 : 1.1, shepherdMastery ? 48 : 36);
                if (shepherdMastery) {
                    HangedEffectUtil.spawnShadowBurst(serverLevel, center, 1.2 * areaScale, 32);
                    HangedEffectUtil.playShadowPulse(serverLevel, center, 0.65f);
                    if (!(target instanceof Player)) {
                        target.discard();
                    }
                }
                serverLevel.playSound(null, center.x, center.y, center.z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.1f, 1.05f);
                AbilityUtil.sendActionBar(entity,
                        Component.translatable(FEAST_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
                ServerScheduler.cancel(taskIdRef.get());
            }
        }, () -> clearArtifactScaling(entity), serverLevel);
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
