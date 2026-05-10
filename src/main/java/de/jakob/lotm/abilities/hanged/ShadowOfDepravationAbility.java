package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ShadowOfDepravationAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/concealed_domain_ability.png");
    private static final float SPIRITUALITY_COST = 460.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 6.5f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 4.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 3.8f;

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.shadow_of_depravation.no_target";
    private static final String PURGED_MESSAGE = "ability.lotmcraft.shadow_of_depravation.purged";

    public ShadowOfDepravationAbility(String id) {
        super(id, 13.0f, "darkness", "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.shadow_of_depravation.peel_shadow",
                "ability.lotmcraft.shadow_of_depravation.void_of_blackness",
                "ability.lotmcraft.shadow_of_depravation.shadow_guard"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> castPeelShadow(serverLevel, entity);
            case 1 -> castVoidOfBlackness(serverLevel, entity);
            case 2 -> castShadowGuard(serverLevel, entity);
            default -> castPeelShadow(serverLevel, entity);
        }
    }

    private void castPeelShadow(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DURATION_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_AREA_SCALE_SEQ1);
        LivingEntity target = entity.isShiftKeyDown() ? entity : AbilityUtil.getTargetEntity(entity, 24, 1.8f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        HangedEffectUtil.playDepravityCast(serverLevel, target.position());
        HangedEffectUtil.spawnDepravityBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.45, 0), 0.9 * areaScale, 28);
        if (target == entity) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.DEPRAVITY_ARMOR, serverLevel, entity, 48, false);
            entity.getData(ModAttachments.SANITY_COMPONENT).setVirtualPersonaStacks(0);
            entity.removeEffect(MobEffects.CONFUSION);
            entity.removeEffect(MobEffects.BLINDNESS);
            entity.removeEffect(MobEffects.DARKNESS);
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, false));
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(PURGED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            for (LivingEntity nearby : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 3.5 * areaScale)) {
                nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, 0.45f * damageScale) * multiplier(entity)));
                nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false, true));
            }
            return;
        }

        HangedRenderEffectUtil.playMovableAt(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_BINDING, serverLevel, target,
                Math.round(80 * durationScale), false);
        target.getData(ModAttachments.SANITY_COMPONENT).setVirtualPersonaStacks(0);
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                (float) (DamageLookup.lookupDamage(7, 1.0f * damageScale) * multiplier(entity)));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0, false, false, true));
        if (target instanceof Player playerTarget) {
            playerTarget.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.03f, playerTarget);
        }

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 8, Math.round(80 * durationScale), () -> {
            if (!target.isAlive()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }
            Vec3 center = target.position().add(0, target.getBbHeight() * 0.45, 0);
            HangedEffectUtil.spawnDepravityBurst(serverLevel, center, 0.7 * areaScale, 18);
            HangedEffectUtil.playDepravityPulse(serverLevel, center, 0.7f);
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(2.0 * areaScale),
                    nearby -> nearby != entity && nearby.isAlive())) {
                nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        Math.max(2.0f, (float) (DamageLookup.lookupDamage(7, 0.12f * damageScale) * multiplier(entity))));
                nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false, true));
            }
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
        taskIdRef.set(taskId);
    }

    private void castVoidOfBlackness(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DURATION_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_AREA_SCALE_SEQ1);
        Vec3 center = AbilityUtil.getTargetLocation(entity, 26, 1.7f);
        HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SHADOW_SUMMON, serverLevel, center, entity);
        HangedEffectUtil.playDepravityCast(serverLevel, center);
        HangedEffectUtil.spawnDepravityBurst(serverLevel, center, 1.0 * areaScale, 30);

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 6, Math.round(120 * durationScale), () -> {
            HangedEffectUtil.spawnDepravityBurst(serverLevel, center, 0.85 * areaScale, 22);
            HangedEffectUtil.playDepravityPulse(serverLevel, center, 0.62f);
            for (LivingEntity nearby : AbilityUtil.getNearbyEntities(entity, serverLevel, center, 3.4 * areaScale)) {
                nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, 0.18f * damageScale) * multiplier(entity)));
                nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, false, false, true));
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 2, false, false, true));
            }
            if (BeyonderData.isGriefingEnabled(entity)) {
                corrodeBlocks(serverLevel, center, areaScale);
            }
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
        taskIdRef.set(taskId);
    }

    private void castShadowGuard(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DURATION_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_AREA_SCALE_SEQ1);
        int duration = Math.round(140 * durationScale);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, false, false));
        HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.DEPRAVITY_ARMOR, serverLevel, entity, duration, false);
        HangedEffectUtil.playDepravityCast(serverLevel, entity.position());

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 8, duration, () -> {
            Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.55, 0);
            HangedEffectUtil.spawnDepravityAura(serverLevel, entity);
            HangedEffectUtil.spawnShadowBurst(serverLevel, center, 0.85 * areaScale, 18);
            for (LivingEntity nearby : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 2.5 * areaScale)) {
                nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        Math.max(2.0f, (float) (DamageLookup.lookupDamage(7, 0.08f * damageScale) * multiplier(entity))));
                nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 1, false, false, true));
            }
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
        taskIdRef.set(taskId);
    }

    private static void corrodeBlocks(ServerLevel level, Vec3 center, double areaScale) {
        int radius = Math.max(1, (int) Math.ceil(1.5 * areaScale));
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(center).offset(-radius, -1, -radius),
                BlockPos.containing(center).offset(radius, 1, radius))) {
            if (level.random.nextFloat() > 0.18f || level.getBlockState(pos).isAir() || level.getBlockState(pos).getDestroySpeed(level, pos) < 0) {
                continue;
            }
            level.destroyBlock(pos, false);
        }
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
