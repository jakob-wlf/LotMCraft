package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncWeaknessDetectionTargetsAbilityPacket;
import de.jakob.lotm.rendering.WeaknessDetectionRenderLayer;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WeaknessDetectionAbility extends ToggleAbility {

    public static final String VIOLATION_TIER_KEY = "lotm_weakness_tier";

    // Tier 1 (yellow) = +15%, Tier 2 (orange) = +30%, Tier 3 (red) = +50%
    private static final double[] TIER_DAMAGE_BOOST = { 0.0, 0.15, 0.30, 0.50 };

    // Server-side per-player target tracking.
    // Key = caster UUID, Value = map of detected target UUID -> tier.
    private static final Map<UUID, Map<UUID, Integer>> DETECTED_BY_CASTER = new HashMap<>();

    public WeaknessDetectionAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 2.5f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        entity.getPersistentData().putBoolean("lotm_weakness_detection_active", true);
        entity.sendSystemMessage(Component.literal("§6Weakness Detection: ON"));

        ServerLevel serverLevel = (ServerLevel) level;
        RingEffectManager.createRingForAll(entity.position(), 3.1f, 20,
                0.92f, 0.76f, 0.18f, 1.0f, 0.18f, 0.9f, serverLevel);
        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                entity.position().add(0, 1, 0), 1.4, 16);

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(
                    player,
                    new SyncWeaknessDetectionTargetsAbilityPacket(false, Map.of())
            );
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        int selfSeq = BeyonderData.getSequence(entity);

        List<LivingEntity> inRange = level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(20)
        );

        Map<UUID, Integer> detectedThisTick = new HashMap<>();
        Map<Integer, Integer> clientTargets = new HashMap<>();

        // Detection pass: evaluate all nearby entities and mark their tier.
        for (LivingEntity target : inRange) {
            if (target == entity) continue;

            int targetSeq = BeyonderData.getSequence(target);
            if (BeyonderData.isBeyonder(target) && targetSeq < selfSeq) {
                clearTarget(target);
                continue;
            }

            int tier = computeViolationTier(target);
            target.getPersistentData().putInt(VIOLATION_TIER_KEY, tier);

            if (tier > 0) {
                detectedThisTick.put(target.getUUID(), tier);
                clientTargets.put(target.getId(), tier);
            } else {
                clearTarget(target);
            }
        }

        DETECTED_BY_CASTER.put(player.getUUID(), detectedThisTick);

        // Send private glow data only to the player who has the ability active.
        PacketHandler.sendToPlayer(
                player,
                new SyncWeaknessDetectionTargetsAbilityPacket(true, clientTargets)
        );

        // Debuff pass.
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(12))) {

            if (target == entity) continue;

            int targetSeq = BeyonderData.getSequence(target);
            if (BeyonderData.isBeyonder(target) && targetSeq < selfSeq) continue;

            int tier = target.getPersistentData().getInt(VIOLATION_TIER_KEY);
            if (tier <= 0) continue;

            applyTierDebuffs(serverLevel, entity, target, tier);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        entity.getPersistentData().remove("lotm_weakness_detection_active");

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(
                    player,
                    new SyncWeaknessDetectionTargetsAbilityPacket(false, Map.of())
            );
            DETECTED_BY_CASTER.remove(player.getUUID());
        }

        ServerLevel serverLevel = (ServerLevel) level;
        RingEffectManager.createRingForAll(entity.position(), 2.4f, 16,
                0.76f, 0.58f, 0.12f, 1.0f, 0.16f, 0.7f, serverLevel);
        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                entity.position().add(0, 1, 0), 1.0, 12);

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(20))) {
            clearTarget(target);
        }

        entity.sendSystemMessage(Component.literal("§cWeakness Detection: OFF"));
    }

    private int computeViolationTier(LivingEntity target) {
        int violations = 0;

        if (target.getArmorValue() < 10) violations++;
        if (target.getHealth() < target.getMaxHealth() * 0.3f) violations++;
        if (target.getDeltaMovement().horizontalDistance() > 0.22) violations++;

        return violations;
    }

    private void applyTierDebuffs(ServerLevel level, LivingEntity caster, LivingEntity target, int tier) {
        switch (tier) {
            case 1 -> target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
            case 2 -> {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, false));
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 40, 0, false, false, false));
            }
            case 3 -> {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, false));
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 40, 1, false, false, false));

                if (level.getGameTime() % 20 == 0) {
                    double bonusDmg = DamageLookup.lookupDamage(6, 0.25) * multiplier(caster);
                    target.hurt(caster.damageSources().mobAttack(caster), (float) bonusDmg);
                }
            }
        }
    }

    private void clearTarget(LivingEntity target) {
        target.getPersistentData().remove(VIOLATION_TIER_KEY);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity attacker = resolveDamageDealer(event);
        if (attacker == null) return;
        if (!(attacker instanceof ServerPlayer player)) return;

        Map<UUID, Integer> detected = DETECTED_BY_CASTER.get(player.getUUID());
        if (detected == null) return;

        Integer tier = detected.get(event.getEntity().getUUID());
        if (tier == null || tier <= 0) return;

        float multiplier = 1.0f + (float) TIER_DAMAGE_BOOST[tier];
        event.setNewDamage(event.getNewDamage() * multiplier);
    }

    private static LivingEntity resolveDamageDealer(LivingDamageEvent.Pre event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof LivingEntity living) {
            return living;
        }

        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity instanceof LivingEntity living) {
            return living;
        }

        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    public static boolean isActive(LivingEntity entity) {
        return entity.getPersistentData().getBoolean("lotm_weakness_detection_active");
    }
}