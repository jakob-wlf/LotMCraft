package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FleshRugAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/mutation_creation_ability.png");
    private static final float SPIRITUALITY_COST = 20.0f;
    private static final double BASE_FIELD_RADIUS = 5.6;
    private static final float MAX_FIELD_RADIUS_SCALE_SEQ1 = 2.1f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 5.0f;
    private static final int GROWTH_DURATION_TICKS = 20 * 6;
    private static final double MIN_FIELD_RADIUS = 2.5;
    private static final Map<UUID, Integer> ACTIVE_TICKS = new HashMap<>();

    private static final String ENABLED_MESSAGE = "ability.lotmcraft.flesh_rug.enabled";
    private static final String DISABLED_MESSAGE = "ability.lotmcraft.flesh_rug.disabled";

    public FleshRugAbility(String id) {
        super(id, "corruption");
        canBeCopied = false;
        autoClear = false;
        tickRate = 5;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int activeTicks = ACTIVE_TICKS.merge(entity.getUUID(), tickRate, Integer::sum);
        double maxFieldRadius = HangedPathwayConstants.scaleDoubleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, BASE_FIELD_RADIUS, MAX_FIELD_RADIUS_SCALE_SEQ1);
        double growthProgress = Math.min(1.0, activeTicks / (double) GROWTH_DURATION_TICKS);
        double fieldRadius = Mth.lerp(growthProgress, Math.min(MIN_FIELD_RADIUS, maxFieldRadius), maxFieldRadius);
        double innerRadius = fieldRadius * 0.45;
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        Vec3 center = entity.position().add(0, 0.05, 0);
        if (entity.tickCount % 15 == 0) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.FLESH_FIELD, serverLevel, entity, 20, false);
        }
        HangedEffectUtil.spawnFleshField(serverLevel, center, fieldRadius, activeTicks / Math.max(1, tickRate));
        HangedEffectUtil.spawnFleshTrail(serverLevel, center.add(fieldRadius, 0.02, 0), center.add(-fieldRadius, 0.02, 0), 0.45);
        HangedEffectUtil.spawnFleshTrail(serverLevel, center.add(0, 0.02, fieldRadius), center.add(0, 0.02, -fieldRadius), 0.45);

        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(fieldRadius, 0.75, fieldRadius),
                nearby -> nearby != entity && nearby.isAlive());
        for (LivingEntity victim : victims) {
            double distance = victim.distanceTo(entity);
            float damageMultiplier = distance <= innerRadius ? 1.25f : 1.0f;
            victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity),
                    Math.max(3.5f, (float) (DamageLookup.lookupDamage(6, 0.13f * damageScale * damageMultiplier) * multiplier(entity))));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, distance <= innerRadius ? 3 : 2, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, distance <= innerRadius ? 2 : 1, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 30, 1, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30, 1, false, false, true));
            if (distance <= innerRadius) {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 24, 0, false, false, true));
            }

            Vec3 separation = victim.position().subtract(center);
            if (separation.lengthSqr() > 0.001) {
                separation = separation.normalize().scale(0.16 + (fieldRadius * 0.022) + (distance <= innerRadius ? 0.08 : 0.0));
                victim.setDeltaMovement(victim.getDeltaMovement().add(separation.x, 0.05, separation.z));
                victim.hurtMarked = true;
            }
        }

        for (int i = 0; i < victims.size(); i++) {
            LivingEntity first = victims.get(i);
            for (int j = i + 1; j < victims.size(); j++) {
                LivingEntity second = victims.get(j);
                if (first.distanceToSqr(second) > 7.0) {
                    continue;
                }
                Vec3 push = first.position().subtract(second.position());
                if (push.lengthSqr() < 0.001) {
                    continue;
                }
                push = push.normalize().scale(0.22 + Math.min(0.16, fieldRadius * 0.012));
                first.setDeltaMovement(first.getDeltaMovement().add(push.x, 0.02, push.z));
                second.setDeltaMovement(second.getDeltaMovement().add(-push.x, 0.02, -push.z));
                first.hurtMarked = true;
                second.hurtMarked = true;
            }
        }

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0, false, false, false));
        if (entity.tickCount % 12 == 0) {
            HangedEffectUtil.spawnFleshBurst(serverLevel, center, Math.max(1.1, fieldRadius * 0.22), 16);
            HangedEffectUtil.playFleshPulse(serverLevel, center, 0.58f);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        ACTIVE_TICKS.put(entity.getUUID(), 0);
        if (level instanceof ServerLevel serverLevel) {
            double fieldRadius = Math.min(MIN_FIELD_RADIUS, HangedPathwayConstants.scaleDoubleForCurrentSequence(entity,
                    HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, BASE_FIELD_RADIUS, MAX_FIELD_RADIUS_SCALE_SEQ1));
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.FLESH_FIELD, serverLevel, entity, 20, false);
            HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position(), 1.25, 30);
            HangedEffectUtil.spawnFleshField(serverLevel, entity.position().add(0, 0.05, 0), fieldRadius, 0);
            HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        }
        AbilityUtil.sendActionBar(entity, Component.translatable(ENABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        ACTIVE_TICKS.remove(entity.getUUID());
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        if (level instanceof ServerLevel serverLevel) {
            double fieldRadius = HangedPathwayConstants.scaleDoubleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, BASE_FIELD_RADIUS, MAX_FIELD_RADIUS_SCALE_SEQ1);
            HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position(), 1.1, 24);
            HangedEffectUtil.spawnFleshField(serverLevel, entity.position().add(0, 0.05, 0), Math.max(2.4, fieldRadius * 0.7), 6);
            HangedEffectUtil.playFleshPulse(serverLevel, entity.position(), 1.05f);
        }
        AbilityUtil.sendActionBar(entity, Component.translatable(DISABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        clearArtifactScaling(entity);
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
