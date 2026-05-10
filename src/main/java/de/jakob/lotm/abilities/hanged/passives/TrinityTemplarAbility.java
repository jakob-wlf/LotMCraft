package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.hanged.HangedEffectUtil;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.hanged.HangedRenderEffectUtil;
import de.jakob.lotm.abilities.justiciar.LawAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TrinityTemplarAbility extends PassiveAbilityItem {
    private static final long SHADOW_SUBSTITUTE_COOLDOWN_MS = 12_000L;
    private static final long FLESH_REBUILD_COOLDOWN_MS = 45_000L;
    private static final float SHADOW_DAMAGE_PORTION = 0.2f;
    private static final float SHADOW_HEAVY_HIT_THRESHOLD = 6.0f;
    private static final float SHADOW_PRESERVED_HEALTH_PORTION = 0.16f;
    private static final float FLESH_REBUILD_HEALTH_PORTION = 0.4f;

    private static final String SHADOW_SUBSTITUTE_MESSAGE = "ability.lotmcraft.trinity_templar.shadow_substitute";
    private static final String FLESH_REBUILD_MESSAGE = "ability.lotmcraft.trinity_templar.flesh_rebuild";

    private static final Map<UUID, Long> SHADOW_SUBSTITUTE_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> FLESH_REBUILD_COOLDOWNS = new HashMap<>();

    public TrinityTemplarAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_TRINITY_TEMPLAR);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            return;
        }

        if (entity.getHealth() < entity.getMaxHealth() && entity.tickCount % 10 == 0) {
            float missingHealthRatio = (entity.getMaxHealth() - entity.getHealth()) / entity.getMaxHealth();
            entity.heal(Math.min(1.0f, 0.25f + missingHealthRatio));
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (entity.tickCount % 40 == 0) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.SHADOW_CLOAK, serverLevel, entity, 26, false);
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.FLESH_CLOAK, serverLevel, entity, 26, false);
        }
        if (entity.getHealth() < entity.getMaxHealth() && entity.tickCount % 20 == 0) {
            HangedEffectUtil.spawnFleshAura(serverLevel, entity);
            HangedEffectUtil.playFleshPulse(serverLevel, entity.position(), 0.72f);
        }
        if (entity.tickCount % 30 == 0) {
            HangedEffectUtil.spawnShadowAura(serverLevel, entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!((TrinityTemplarAbility) PassiveAbilityHandler.TRINITY_TEMPLAR.get()).shouldApplyTo(entity)) {
            return;
        }

        if (event.getAmount() <= 0.0f
                || event.getSource().is(ModDamageTypes.LOOSING_CONTROL)
                || LawAbility.SOLACE_KILLED.contains(entity.getUUID())) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean lethal = event.getAmount() >= entity.getHealth();
        if (shouldTriggerShadowSubstitute(entity, event.getAmount(), lethal, now)) {
            triggerShadowSubstitute(entity, serverLevel, event, lethal, now);
            return;
        }

        if (!lethal || !canRebuild(entity, now)) {
            return;
        }

        if (entity instanceof Player player && !RoseBishopRegenerationAbility.consumeBlood(player)) {
            return;
        }

        triggerFleshRebuild(entity, serverLevel, event, now);
    }

    private static boolean shouldTriggerShadowSubstitute(LivingEntity entity, float incomingDamage, boolean lethal, long now) {
        long lastTrigger = SHADOW_SUBSTITUTE_COOLDOWNS.getOrDefault(entity.getUUID(), 0L);
        if (now - lastTrigger < SHADOW_SUBSTITUTE_COOLDOWN_MS) {
            return false;
        }

        return lethal || incomingDamage >= Math.max(SHADOW_HEAVY_HIT_THRESHOLD, entity.getMaxHealth() * 0.3f);
    }

    private static void triggerShadowSubstitute(LivingEntity entity, ServerLevel level, LivingIncomingDamageEvent event,
                                                boolean lethal, long now) {
        SHADOW_SUBSTITUTE_COOLDOWNS.put(entity.getUUID(), now);

        float reducedDamage;
        if (lethal) {
            float preservedHealth = Math.max(2.0f, entity.getMaxHealth() * SHADOW_PRESERVED_HEALTH_PORTION);
            reducedDamage = Math.max(0.0f, entity.getHealth() - preservedHealth);
        } else {
            reducedDamage = Math.max(0.75f, event.getAmount() * SHADOW_DAMAGE_PORTION);
        }

        event.setAmount(reducedDamage);
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0, false, false, false));
        HangedEffectUtil.spawnShadowBurst(level, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 1.0, 28);
        HangedEffectUtil.playShadowCast(level, entity.position());
        AbilityUtil.sendActionBar(entity, Component.translatable(SHADOW_SUBSTITUTE_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }

    private static boolean canRebuild(LivingEntity entity, long now) {
        long lastTrigger = FLESH_REBUILD_COOLDOWNS.getOrDefault(entity.getUUID(), 0L);
        return now - lastTrigger >= FLESH_REBUILD_COOLDOWN_MS;
    }

    private static void triggerFleshRebuild(LivingEntity entity, ServerLevel level, LivingIncomingDamageEvent event, long now) {
        FLESH_REBUILD_COOLDOWNS.put(entity.getUUID(), now);
        event.setCanceled(true);
        entity.setHealth(Math.min(entity.getMaxHealth(), Math.max(6.0f, entity.getMaxHealth() * FLESH_REBUILD_HEALTH_PORTION)));
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WITHER);
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 180, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, false, false, false));
        HangedEffectUtil.spawnFleshBurst(level, entity.position().add(0, entity.getBbHeight() * 0.45, 0), 1.15, 34);
        HangedEffectUtil.playFleshCast(level, entity.position());
        AbilityUtil.sendActionBar(entity, Component.translatable(FLESH_REBUILD_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }
}
