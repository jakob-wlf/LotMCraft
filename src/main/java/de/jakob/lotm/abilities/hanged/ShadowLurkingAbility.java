package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.data.Location;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShadowLurkingAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/shadow_concealment_ability.png");
    private static final float SPIRITUALITY_COST = 60.0f;

    private static final Set<UUID> LURKING_ENTITIES = new HashSet<>();
    private static final Map<UUID, Integer> TRACKED_TARGETS = new HashMap<>();

    private static final int TRACK_RADIUS = 36;
    private static final String TRACKING_MESSAGE = "ability.lotmcraft.shadow_lurking.tracking";

    public ShadowLurkingAbility(String id) {
        super(id, "concealment", "darkness");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 15, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 15, 4, false, false, false));

        if (entity.tickCount % 18 == 0) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_CLOAK, serverLevel, entity, 22, false);
        }
        if (entity.tickCount % 5 == 0) {
            HangedEffectUtil.spawnShadowAura(serverLevel, entity);
        }
        if (entity.tickCount % 24 == 0) {
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.55f);
        }

        LivingEntity trackedTarget = AbilityUtil.getTargetEntity(entity, TRACK_RADIUS, 1.4f, true, false, false);
        Integer previousTargetId = TRACKED_TARGETS.get(entity.getUUID());

        if (trackedTarget == null) {
            clearTrackedTargetGlow(serverLevel, player, previousTargetId);
            TRACKED_TARGETS.remove(entity.getUUID());
            return;
        }

        if (previousTargetId == null || previousTargetId != trackedTarget.getId()) {
            clearTrackedTargetGlow(serverLevel, player, previousTargetId);
            SpiritVisionAbility.setGlowingForPlayer(trackedTarget, player, true);
            TRACKED_TARGETS.put(entity.getUUID(), trackedTarget.getId());
        }

        AbilityUtil.sendActionBar(player,
                Component.translatable(TRACKING_MESSAGE, trackedTarget.getDisplayName(),
                                (int) Math.sqrt(trackedTarget.distanceToSqr(player)))
                        .withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        LURKING_ENTITIES.add(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_CLOAK, serverLevel, entity, 22, false);
            HangedEffectUtil.spawnShadowBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.55, 0), 0.9, 28);
            HangedEffectUtil.playShadowCast(serverLevel, entity.position());
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        LURKING_ENTITIES.remove(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnShadowBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.9, 20);
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position(), 0.95f);
        }

        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            clearTrackedTargetGlow(serverLevel, player, TRACKED_TARGETS.remove(entity.getUUID()));
        }

        entity.removeEffect(MobEffects.INVISIBILITY);
        entity.removeEffect(MobEffects.NIGHT_VISION);
        entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        entity.removeEffect(MobEffects.DAMAGE_BOOST);
        clearArtifactScaling(entity);
    }

    private static void clearTrackedTargetGlow(ServerLevel level, ServerPlayer player, Integer entityId) {
        if (entityId == null) {
            return;
        }

        Entity previous = level.getEntity(entityId);
        if (previous != null) {
            SpiritVisionAbility.setGlowingForPlayer(previous, player, false);
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

    @SubscribeEvent
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() == null || !LURKING_ENTITIES.contains(event.getNewAboutToBeSetTarget().getUUID())) {
            return;
        }

        LivingEntity lurkingTarget = event.getNewAboutToBeSetTarget();
        LivingEntity attacker = event.getEntity();
        Location targetLocation = new Location(lurkingTarget.position(), lurkingTarget.level());

        if (InteractionHandler.isInteractionPossible(targetLocation, "light_source")) {
            return;
        }

        ToggleAbility spiritVision = (ToggleAbility) LOTMCraft.abilityHandler.getById("spirit_vision_ability");
        if (spiritVision != null && spiritVision.isActiveForEntity(attacker)) {
            return;
        }

        Ability spectating = LOTMCraft.abilityHandler.getById("spectating_ability");
        if (spectating instanceof ToggleAbility spectatingToggle && spectatingToggle.isActiveForEntity(attacker)) {
            return;
        }

        Ability cull = LOTMCraft.abilityHandler.getById("cull_ability");
        if (cull instanceof ToggleAbility cullToggle && cullToggle.isActiveForEntity(attacker)) {
            return;
        }

        if (PsychologicalInvisibilityAbility.invisiblePlayers.containsKey(lurkingTarget.getUUID())) {
            return;
        }

        event.setCanceled(true);
    }
}
