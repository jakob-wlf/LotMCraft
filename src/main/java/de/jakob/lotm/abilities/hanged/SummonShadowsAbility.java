package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class SummonShadowsAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/spirit_commanding_ability.png");
    private static final float SPIRITUALITY_COST = 180.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 8.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 4.5f;
    private static final float MAX_COUNT_SCALE_SEQ1 = 3.0f;

    private static final int SUMMON_PULSE_COUNT = 8;
    private static final int SUMMON_INTERVAL_TICKS = 7;
    private static final double BACKLASH_CHANCE = 0.28;
    private static final String BACKLASH_MESSAGE = "ability.lotmcraft.summon_shadows.backlash";
    private static final String SUMMON_MESSAGE = "ability.lotmcraft.summon_shadows.success";

    public SummonShadowsAbility(String id) {
        super(id, 14f, "darkness", "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_DAMAGE_SCALE_SEQ1);
        double areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        int pulseCount = Math.min(24, HangedPathwayConstants.scaleIntForCurrentSequence(entity,
                HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, SUMMON_PULSE_COUNT, MAX_COUNT_SCALE_SEQ1));
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 26, 1.8f, true, false, false);
        Vec3 center = target != null ? target.position() : AbilityUtil.getTargetLocation(entity, 24, 1.4f);
        HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SHADOW_SUMMON, serverLevel, center.add(0, 0.8, 0), entity);
        HangedEffectUtil.spawnShadowBurst(serverLevel, center.add(0, 0.8, 0), 1.0 * areaScale, 22);

        if (random.nextDouble() < BACKLASH_CHANCE) {
            entity.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC), 8.0f);
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, false, true));
            entity.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.12f, entity);
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position(), 0.5f);
            AbilityUtil.sendActionBar(entity, Component.translatable(BACKLASH_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            clearArtifactScaling(entity);
            return;
        }

        AbilityUtil.sendActionBar(entity, Component.translatable(SUMMON_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        HangedEffectUtil.playShadowCast(serverLevel, entity.position());

        ServerScheduler.scheduleForDuration(0, SUMMON_INTERVAL_TICKS, pulseCount * SUMMON_INTERVAL_TICKS, () -> {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = (1.6 + random.nextDouble() * 3.0) * areaScale;
            Vec3 strike = center.add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);

            HangedRenderEffectUtil.playBurst(de.jakob.lotm.rendering.effectRendering.EffectManager.Effect.SHADOW_SUMMON, serverLevel, strike.add(0, 0.8, 0), entity);
            HangedEffectUtil.spawnShadowBurst(serverLevel, strike.add(0, 0.8, 0), 0.9 * areaScale, 20);
            HangedEffectUtil.playShadowPulse(serverLevel, strike, 0.6f + random.nextFloat() * 0.25f);

            for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, strike, 2.8 * areaScale)) {
                victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, 1.0f * damageScale) * multiplier(entity)));
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 1, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false, true));
            }
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
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
