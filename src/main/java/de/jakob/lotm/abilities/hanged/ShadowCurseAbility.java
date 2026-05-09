package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ShadowCurseAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/curse_ability.png");
    private static final float SPIRITUALITY_COST = 120.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 6.0f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 5.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.5f;
    private static final float MAX_AREA_SCALE_SEQ1 = 3.5f;

    private static final int CURSE_DURATION_TICKS = 20 * 35;
    private static final int CURSE_INTERVAL_TICKS = 20;
    private static final String NO_MEDIUM_MESSAGE = "ability.lotmcraft.shadow_curse.no_medium";
    private static final String TARGET_LOST_MESSAGE = "ability.lotmcraft.shadow_curse.target_lost";
    private static final String APPLIED_MESSAGE = "ability.lotmcraft.shadow_curse.applied";
    private static final String BACKLASH_MESSAGE = "ability.lotmcraft.shadow_curse.backlash";

    public ShadowCurseAbility(String id) {
        super(id, 8f, "curse", "corruption");
        canBeUsedByNPC = false;
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
        double areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_AREA_SCALE_SEQ1);
        LivingEntity target = resolveTarget(serverLevel, entity);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, net.minecraft.network.chat.Component.translatable(NO_MEDIUM_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if (AbilityUtil.isTargetSignificantlyStronger(casterSeq, targetSeq)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 6, 2, false, false, false));
            entity.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.08f, entity);
            entity.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, target), 6.0f);
            AbilityUtil.sendActionBar(entity, net.minecraft.network.chat.Component.translatable(BACKLASH_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        AbilityUtil.sendActionBar(entity,
                net.minecraft.network.chat.Component.translatable(APPLIED_MESSAGE, target.getDisplayName()).withColor(HangedPathwayConstants.pathwayColor()));
        HangedEffectUtil.playShadowCast(serverLevel, entity.position());
        HangedEffectUtil.spawnShadowBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), 0.7 * areaScale, 20);

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, CURSE_INTERVAL_TICKS, Math.round(CURSE_DURATION_TICKS * durationScale), () -> {
            if (target.isDeadOrDying()) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            Location targetLocation = new Location(target.position(), target.level());
            if (InteractionHandler.isInteractionPossibleForEntity(targetLocation, "cleansing", casterSeq, target)) {
                ServerScheduler.cancel(taskIdRef.get());
                return;
            }

            HangedEffectUtil.spawnShadowBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), 0.55 * areaScale, 16);
            HangedEffectUtil.playShadowPulse(serverLevel, target.position(), 0.65f);

            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity),
                    (float) (DamageLookup.lookupDamage(7, 0.92f * damageScale) * multiplier(entity)));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
        }, () -> clearArtifactScaling(entity), serverLevel);
        taskIdRef.set(taskId);
    }

    private LivingEntity resolveTarget(ServerLevel level, LivingEntity entity) {
        ItemStack medium = entity instanceof ServerPlayer player ? player.getItemInHand(InteractionHand.OFF_HAND) : ItemStack.EMPTY;
        double rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC, MAX_RANGE_SCALE_SEQ1);
        if (medium.is(ModItems.BLOOD)) {
            String owner = medium.getOrDefault(ModDataComponents.BLOOD_OWNER, "");
            if (!owner.isEmpty()) {
                Entity bloodTarget = level.getEntity(UUID.fromString(owner));
                if (entity instanceof ServerPlayer player) {
                    medium.consume(1, player);
                }
                if (bloodTarget instanceof LivingEntity livingTarget) {
                    return livingTarget;
                }

                AbilityUtil.sendActionBar(entity, net.minecraft.network.chat.Component.translatable(TARGET_LOST_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
                return null;
            }
        }

        return AbilityUtil.getTargetEntity(entity, Math.max(24, (int) Math.round(24 * rangeScale)), 1.6f, true, false, false);
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
