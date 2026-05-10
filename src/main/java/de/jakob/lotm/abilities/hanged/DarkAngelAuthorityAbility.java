package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DarkAngelAuthorityAbility extends SelectableAbility {
    private static final double SEA_OF_DARKNESS_RADIUS = 32.0 * 16.0;
    private static final double SEA_OF_DARKNESS_VISUAL_RADIUS = 18.0;
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_DARK_ANGEL);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/concealed_domain_ability.png");
    private static final float SPIRITUALITY_COST = 1600.0f;

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.dark_angel_authority.no_target";

    public DarkAngelAuthorityAbility(String id) {
        super(id, 20.0f, "darkness", "corruption", "curse");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.dark_angel_authority.sea_of_darkness",
                "ability.lotmcraft.dark_angel_authority.shadow_coat",
                "ability.lotmcraft.dark_angel_authority.betrayal"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> castSeaOfDarkness(serverLevel, entity);
            case 1 -> castShadowCoat(serverLevel, entity);
            case 2 -> castBetrayal(serverLevel, entity);
            default -> castSeaOfDarkness(serverLevel, entity);
        }
    }

    private void castSeaOfDarkness(ServerLevel level, LivingEntity entity) {
        Vec3 center = AbilityUtil.getTargetLocation(entity, 30, 1.6f);
        float damage = (float) (DamageLookup.lookupDamage(7, 1.1f) * multiplier(entity));
        double radius = SEA_OF_DARKNESS_RADIUS;
        double visualRadius = Math.min(radius, SEA_OF_DARKNESS_VISUAL_RADIUS);
        int duration = 160;

        HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SEA_OF_DARKNESS, level, center);
        HangedEffectUtil.spawnShadowBurst(level, center, 1.5, 40);
        HangedEffectUtil.spawnDepravityBurst(level, center, 1.4, 40);
        HangedEffectUtil.playShadowCast(level, center);
        HangedEffectUtil.playDepravityCast(level, center);

        AtomicInteger pulseCounter = new AtomicInteger();
        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 4, duration, () -> {
            int pulse = pulseCounter.getAndIncrement();
            int exponentialStage = Math.min(5, pulse / 6);
            int effectScale = 1 << exponentialStage;
            float pulseDamage = Math.max(4.0f, damage * 0.14f * effectScale);
            int slowAmplifier = Math.min(9, effectScale - 1);
            int weaknessAmplifier = Math.min(9, Math.max(0, (effectScale / 2) - 1));
            int blindnessAmplifier = Math.min(4, exponentialStage);
            int darknessAmplifier = Math.min(5, exponentialStage);
            int controlAmplifier = Math.min(4, Math.max(1, exponentialStage));
            float sanityDrain = Math.min(0.04f, 0.0025f * effectScale);

            HangedEffectUtil.spawnShadowField(level, center, visualRadius, pulse);
            HangedEffectUtil.spawnDepravityField(level, center, visualRadius * 0.92, pulse);

            List<LivingEntity> victims = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(center.x - radius, center.y - radius, center.z - radius,
                            center.x + radius, center.y + radius, center.z + radius),
                    victim -> victim != entity && victim.isAlive() && victim.distanceToSqr(center) <= radius * radius);
            for (LivingEntity victim : victims) {
                victim.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), pulseDamage);
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, blindnessAmplifier, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, slowAmplifier, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, weaknessAmplifier, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, darknessAmplifier, false, false, true));
                if (!BeyonderData.isBeyonder(victim) || BeyonderData.getSequence(victim) > HangedPathwayConstants.SEQUENCE_DARK_ANGEL) {
                    victim.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(sanityDrain, victim);
                }
                if (victim.getData(ModAttachments.SANITY_COMPONENT).getSanity() < 0.55f) {
                    victim.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 40, controlAmplifier, false, false, true));
                }
            }

            AABB projectileBox = new AABB(center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius);
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, projectileBox, projectile -> projectile.getOwner() != entity)) {
                HangedEffectUtil.spawnShadowBurst(level, projectile.position(), 0.45, 10);
                projectile.discard();
            }
        }, () -> clearArtifactScaling(entity), level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
        taskIdRef.set(taskId);
    }

    private void castShadowCoat(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 1.6f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        float damage = (float) (DamageLookup.lookupDamage(8, 1.05f) * multiplier(entity));
        int duration = 120;
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.45, 0);

        HangedRenderEffectUtil.playMovableAt(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_BINDING, level, target, duration, false);
        HangedEffectUtil.spawnShadowBurst(level, center, 1.1, 28);
        HangedEffectUtil.spawnDepravityBurst(level, center, 1.0, 24);
        HangedEffectUtil.playShadowCast(level, center);
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), damage);
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 0, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false, true));

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 5, duration, () -> {
            if (!target.isAlive()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Vec3 pulseCenter = target.position().add(0, target.getBbHeight() * 0.45, 0);
            HangedEffectUtil.spawnShadowBurst(level, pulseCenter, 0.65, 16);
            target.setDeltaMovement(target.getDeltaMovement().scale(0.3));
            target.hurtMarked = true;
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), Math.max(2.0f, damage * 0.08f));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 2, false, false, true));
        }, () -> clearArtifactScaling(entity), level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
        taskIdRef.set(taskId);
    }

    private void castBetrayal(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 28, 1.7f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        float damage = (float) (DamageLookup.lookupDamage(9, 1.25f) * multiplier(entity));
        int duration = 110;
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.45, 0);

        HangedRenderEffectUtil.playMovableAt(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_BINDING, level, target, duration, false);
        HangedEffectUtil.spawnDepravityBurst(level, center, 1.15, 32);
        HangedEffectUtil.playDepravityCast(level, center);
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), damage);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 3, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1, false, false, true));
        target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.08f, target);

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 6, duration, () -> {
            if (!target.isAlive()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Vec3 pulseCenter = target.position().add(0, target.getBbHeight() * 0.45, 0);
            HangedEffectUtil.spawnDepravityBurst(level, pulseCenter, 0.75, 18);
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), Math.max(2.5f, damage * 0.1f));
            if (target.getData(ModAttachments.SANITY_COMPONENT).getSanity() < 0.5f) {
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 40, 1, false, false, true));
            }
            if (target instanceof net.minecraft.world.entity.Mob mob) {
                List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(6.0), candidate -> candidate != target && candidate != entity && candidate.isAlive());
                LivingEntity livingNearby = nearbyEntities.stream()
                        .min(java.util.Comparator.comparingDouble(target::distanceToSqr))
                        .orElse(null);
                if (livingNearby != null) {
                    mob.setTarget(livingNearby);
                }
            }
        }, () -> clearArtifactScaling(entity), level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
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
