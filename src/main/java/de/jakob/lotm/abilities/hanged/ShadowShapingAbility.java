package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.PerformantExplosion;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShadowShapingAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/sword_of_darkness_ability.png");
    private static final float SPIRITUALITY_COST = 90.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 10.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 6.0f;
    private static final float SHADOW_BOMB_GRIEF_POWER = 1.6f;
    private static final float SHADOW_SLASH_DAMAGE = 1.28f;
    private static final float SHADOW_BOMB_DAMAGE = 1.05f;
    private static final float SHADOW_HOUND_DAMAGE = 0.62f;

    public ShadowShapingAbility(String id) {
        super(id, 7f, "darkness", "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.shadow_shaping.shadow_slash",
                "ability.lotmcraft.shadow_shaping.shadow_bomb",
                "ability.lotmcraft.shadow_shaping.shadow_hound"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> castShadowSlash(serverLevel, entity);
            case 1 -> castShadowBomb(serverLevel, entity);
            case 2 -> castShadowHound(serverLevel, entity);
            default -> castShadowSlash(serverLevel, entity);
        }
    }

    private void castShadowSlash(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DAMAGE_SCALE_SEQ1);
        double areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        Vec3 start = entity.getEyePosition().add(entity.getLookAngle().scale(1.2));
        Vec3 target = AbilityUtil.getTargetLocation(entity, 28, 2.0f);
        Vec3 rawDirection = target.subtract(start);
        if (rawDirection.lengthSqr() < 0.001) {
            clearArtifactScaling(entity);
            return;
        }
        Vec3 direction = rawDirection.normalize();

        double offsetRight = random.nextDouble(1.8, 3.8) * areaScale * (random.nextBoolean() ? 1 : -1);
        Vec3 slashStart = VectorUtil.getRelativePosition(start, direction, 0, offsetRight, 4.8 * areaScale);
        Vec3 slashEnd = VectorUtil.getRelativePosition(start, direction, 0, -offsetRight, -4.8 * areaScale);
        Set<Integer> hitEntities = new HashSet<>();

        HangedRenderEffectUtil.playShadowBlade(serverLevel, entity, slashStart, slashEnd, 18);
        HangedEffectUtil.playShadowCast(serverLevel, entity.position());
        HangedEffectUtil.spawnShadowBurst(serverLevel, start, 0.6, 18);

        AtomicDouble distance = new AtomicDouble(0);
        ServerScheduler.scheduleForDuration(0, 1, 18, () -> {
            Vec3 currentStart = slashStart.add(direction.scale(distance.get()));
            Vec3 currentEnd = slashEnd.add(direction.scale(distance.get()));
            Vec3 center = currentStart.add(currentEnd).scale(0.5);
            HangedEffectUtil.spawnShadowTrail(serverLevel, currentStart, currentEnd, 0.35);

            for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, center, 2.8 * areaScale)) {
                if (hitEntities.add(victim.getId())) {
                    victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                            (float) (DamageLookup.lookupDamage(7, SHADOW_SLASH_DAMAGE * damageScale) * multiplier(entity)));
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false, true));
                    victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false, true));
                    victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
                }
            }

            distance.addAndGet(1.15 * areaScale);
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    private void castShadowBomb(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DAMAGE_SCALE_SEQ1);
        double areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        Vec3 impact = AbilityUtil.getTargetLocation(entity, 24, 1.6f);
        HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SHADOW_SUMMON, serverLevel, impact.add(0, 0.6, 0), entity);
        HangedEffectUtil.playShadowCast(serverLevel, entity.position());
        HangedEffectUtil.spawnShadowTrail(serverLevel, entity.getEyePosition(), impact.add(0, 0.6, 0), 0.55);

        AtomicDouble radius = new AtomicDouble(0.8 * areaScale);
        ServerScheduler.scheduleForDuration(0, 2, 12, () -> {
            HangedEffectUtil.spawnShadowBurst(serverLevel, impact.add(0, 0.6, 0), radius.get(), 16);
            HangedEffectUtil.playShadowPulse(serverLevel, impact, 0.55f + random.nextFloat() * 0.2f);
            radius.addAndGet(0.35 * areaScale);
        }, () -> {
            double finalRadius = 4.8 * areaScale;
            HangedEffectUtil.spawnShadowBurst(serverLevel, impact.add(0, 0.6, 0), 1.6 * areaScale, 42);
            serverLevel.playSound(null, impact.x, impact.y, impact.z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.75f);
            float damage = (float) (DamageLookup.lookupDamage(7, SHADOW_BOMB_DAMAGE * damageScale) * multiplier(entity));
            AbilityUtil.damageNearbyEntities(serverLevel, entity, finalRadius, damage, impact, true, true,
                    ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity));
            if (BeyonderData.isGriefingEnabled(entity)) {
                PerformantExplosion.create(serverLevel, entity, impact, (float) Math.min(8.0, SHADOW_BOMB_GRIEF_POWER * areaScale),
                        false, Explosion.BlockInteraction.DESTROY);
            }
            for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, impact, finalRadius)) {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, false, true));
            }
            clearArtifactScaling(entity);
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    private void castShadowHound(ServerLevel serverLevel, LivingEntity entity) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DAMAGE_SCALE_SEQ1);
        double areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        LivingEntity directTarget = AbilityUtil.getTargetEntity(entity, 24, 1.8f, true, false, false);
        HangedEffectUtil.playShadowCast(serverLevel, entity.position());

        int waveCount = Math.max(3, Math.round((float) (3 * areaScale)));
        int[] wave = {0};
        ServerScheduler.scheduleForDuration(0, 5, waveCount * 5, () -> {
            Vec3 center = directTarget != null && directTarget.isAlive()
                    ? directTarget.position().add(0, directTarget.getBbHeight() * 0.45, 0)
                    : AbilityUtil.getTargetLocation(entity, 22, 1.5f);

            double sideOffset = (wave[0] % 2 == 0 ? 2.2 : -2.2) * areaScale;
            Vec3 launch = VectorUtil.getRelativePosition(center, entity.getLookAngle().normalize(), 0, sideOffset, -3.2 * areaScale);
            HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SHADOW_SUMMON, serverLevel, center, entity);
            HangedEffectUtil.spawnShadowTrail(serverLevel, launch, center, 0.35);
            HangedEffectUtil.spawnShadowBurst(serverLevel, center, 0.7 * areaScale, 18);
            HangedEffectUtil.playShadowPulse(serverLevel, center, 0.75f);

            for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, center, 2.5 * areaScale)) {
                victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, SHADOW_HOUND_DAMAGE * damageScale) * multiplier(entity)));
                Vec3 pull = center.subtract(victim.position()).normalize().scale(0.45 * Math.min(3.0, areaScale));
                victim.setDeltaMovement(victim.getDeltaMovement().add(pull.x, 0.08, pull.z));
                victim.hurtMarked = true;
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 2, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 1, false, false, true));
            }
            wave[0]++;
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
