package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommandeeringShadowsAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/hair_entanglement_ability.png");
    private static final float SPIRITUALITY_COST = 420.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 6.5f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 4.5f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 3.5f;

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.commandeering_shadows.no_target";

    public CommandeeringShadowsAbility(String id) {
        super(id, 14.0f, "darkness", "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DURATION_SCALE_SEQ1);
        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_RANGE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_AREA_SCALE_SEQ1);
        LivingEntity target = AbilityUtil.getTargetEntity(entity, Math.max(26, Math.round(26 * rangeScale)), 1.8f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        int duration = Math.round((AbilityUtil.isTargetSignificantlyStronger(casterSeq, targetSeq) ? 80 : 180) * durationScale);
        if (target instanceof Mob mob) {
            mob.setNoAi(true);
        }
        if (BeyonderData.isBeyonder(target)) {
            target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).disableAbilityUsageForTime("commandeering_shadows", duration, target);
        }

        HangedRenderEffectUtil.playMovableAt(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_BINDING, serverLevel, target, duration, false);
        HangedEffectUtil.playDepravityCast(serverLevel, target.position());
        HangedEffectUtil.spawnDepravityBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), 1.0 * areaScale, 32);
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                (float) (DamageLookup.lookupDamage(7, 0.95f * damageScale) * multiplier(entity)));

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        int[] pulse = {0};
        UUID taskId = ServerScheduler.scheduleForDuration(0, 4, duration, () -> {
            if (!target.isAlive()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Location targetLocation = new Location(target.position(), target.level());
            if (InteractionHandler.isInteractionPossibleForEntity(targetLocation, "escape", casterSeq, target)) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Vec3 center = target.position().add(0, 0.08, 0);
            HangedEffectUtil.spawnShadowBurst(serverLevel, center.add(0, target.getBbHeight() * 0.35, 0), 0.95 * areaScale, 24);
            HangedEffectUtil.spawnDepravityBurst(serverLevel, center, 0.75 * areaScale, 18);
            if (pulse[0] % 3 == 0) {
                HangedEffectUtil.playShadowPulse(serverLevel, center, 0.58f);
                target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        (float) (DamageLookup.lookupDamage(7, 0.2f * damageScale) * multiplier(entity)));
                ModDamageTypes.trueDamage(target, Math.max(2.5f, (float) (2.2f * damageScale)), serverLevel, entity);
                if (target instanceof Player playerTarget) {
                    playerTarget.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.018f, playerTarget);
                }
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 8, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 2, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 128, false, false, true));
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            Vec3 position = target.position();
            target.teleportTo(position.x, position.y, position.z);

            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(1.8 * areaScale),
                    nearby -> nearby != entity && nearby != target && nearby.isAlive())) {
                nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, entity),
                        Math.max(2.0f, (float) (DamageLookup.lookupDamage(7, 0.08f * damageScale) * multiplier(entity))));
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, true));
            }
            pulse[0]++;
        }, () -> {
            if (target instanceof Mob mob) {
                mob.setNoAi(false);
            }
            HangedEffectUtil.spawnShadowBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.4, 0), 0.9 * areaScale, 18);
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
