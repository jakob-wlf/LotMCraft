package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PunishmentAbility extends Ability {

    public static final Map<UUID, UUID> CASTER_TO_TARGET  = new ConcurrentHashMap<>();
    public static final Map<UUID, UUID> TARGET_TO_CASTER  = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> PUNISHMENT_EXPIRY = new ConcurrentHashMap<>();

    private static final int    DURATION = 20 * 60 * 5;
    private static final Random RAND     = new Random();

    private static final DustParticleOptions GOLD_DUST      = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.0f), 1.3f);
    private static final DustParticleOptions PALE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.93f, 0.5f), 0.9f);
    private static final DustParticleOptions EMBER_DUST     = new DustParticleOptions(new Vector3f(1.0f, 0.35f, 0.05f), 1.0f);
    private static final DustParticleOptions VERDICT_DUST   = new DustParticleOptions(new Vector3f(0.95f, 0.6f, 0.0f), 1.5f);

    public PunishmentAbility(String id) {
        super(id, 5f, "punishment");
        interactionRadius = 20;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 150;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        UUID casterUUID = entity.getUUID();

        if (CASTER_TO_TARGET.containsKey(casterUUID)) {
            cancelPunishment(casterUUID, serverLevel);
            spawnDismissEffect(serverLevel, entity.position());
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.cancelled")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20 * (int) Math.max(multiplier(entity) / 4, 1), 1.5f);
        if (target == null) {
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.no_target")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        UUID targetUUID = target.getUUID();
        CASTER_TO_TARGET.put(casterUUID, targetUUID);
        TARGET_TO_CASTER.put(targetUUID, casterUUID);
        PUNISHMENT_EXPIRY.put(casterUUID, serverLevel.getGameTime() + DURATION);

        ServerScheduler.scheduleForDuration(0, 5, DURATION, () -> {
            if(target.isAlive())
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false));
        });

        spawnMarkingEffect(serverLevel, entity, target);

        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.marked_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(target.getDisplayName().getString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("ability.lotmcraft.punishment.marked_suffix").withStyle(ChatFormatting.GOLD)));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, 20 * 2));
    }

    private static void spawnMarkingEffect(ServerLevel level, LivingEntity caster, LivingEntity target) {
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 0.6f);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.5f);
        level.playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7f, 0.55f);

        Vec3 casterPos = caster.position().add(0, 1, 0);
        Vec3 targetPos = target.position().add(0, 1, 0);

        ParticleUtil.spawnSphereParticles(level, GOLD_DUST, casterPos, 0.8, 22);
        ParticleUtil.spawnSphereParticles(level, PALE_GOLD_DUST, casterPos, 0.5, 14);
        ParticleUtil.spawnCircleParticles(level, GOLD_DUST, caster.position().add(0, 0.05, 0), 0.9, 20);

        ParticleUtil.drawParticleLine(level, GOLD_DUST, caster.getEyePosition(), target.getEyePosition(), 0.15, 1);
        ParticleUtil.drawParticleLine(level, PALE_GOLD_DUST, caster.getEyePosition(), target.getEyePosition(), 0.25, 1);
        level.sendParticles(ParticleTypes.ENCHANT,
                caster.getEyePosition().x, caster.getEyePosition().y, caster.getEyePosition().z,
                20, 0.3, 0.3, 0.3, 0.12);

        ParticleUtil.spawnSphereParticles(level, VERDICT_DUST, targetPos, 1.0, 28);
        ParticleUtil.spawnSphereParticles(level, EMBER_DUST, targetPos, 0.7, 18);
        ParticleUtil.spawnCircleParticles(level, GOLD_DUST, target.position().add(0, 0.05, 0), 1.1, 24);
        level.sendParticles(ParticleTypes.ENCHANT,
                targetPos.x, targetPos.y, targetPos.z,
                25, 0.4, 0.4, 0.4, 0.1);

        Location targetLoc = new Location(target.position(), level);

        ServerScheduler.scheduleForDuration(0, 4, 30, () -> {
            Vec3 tp = target.position().add(0, 1, 0);
            level.sendParticles(GOLD_DUST, tp.x, tp.y, tp.z, 2, 0.25, 0.35, 0.25, 0);
            level.sendParticles(PALE_GOLD_DUST, tp.x, tp.y + 0.2, tp.z, 1, 0.15, 0.15, 0.15, 0);
        }, null, level, () -> AbilityUtil.getTimeInArea(caster, targetLoc));
    }

    private static void spawnDismissEffect(ServerLevel level, Vec3 pos) {
        level.playSound(null, BlockPos.containing(pos), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 0.7f);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1.6f);
        Vec3 center = pos.add(0, 1, 0);
        level.sendParticles(GOLD_DUST, center.x, center.y, center.z, 16, 0.4, 0.4, 0.4, 0);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 10, 0.3, 0.3, 0.3, 0.02);
    }

    private static void spawnTriggerEffect(ServerLevel level, LivingEntity caster, LivingEntity target) {
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4f, 0.5f);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.1f);
        level.playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 0.6f);

        if (caster != null) {
            level.playSound(null, caster.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.8f);
            Vec3 cp = caster.position().add(0, 1, 0);
            level.sendParticles(GOLD_DUST, cp.x, cp.y, cp.z, 14, 0.3, 0.3, 0.3, 0);
            level.sendParticles(ParticleTypes.ENCHANT, cp.x, cp.y, cp.z, 18, 0.4, 0.4, 0.4, 0.1);
        }

        Vec3 tp = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(level, VERDICT_DUST, tp, 1.0, 24);
        ParticleUtil.spawnSphereParticles(level, EMBER_DUST, tp, 0.7, 16);
        ParticleUtil.spawnCircleParticles(level, GOLD_DUST, target.position().add(0, 0.05, 0), 1.3, 26);
        level.sendParticles(ParticleTypes.CRIT, tp.x, tp.y, tp.z, 14, 0.4, 0.4, 0.4, 0.12);
        level.sendParticles(ParticleTypes.ENCHANT, tp.x, tp.y, tp.z, 20, 0.5, 0.5, 0.5, 0.1);
    }

    public static void cancelPunishment(UUID casterUUID, ServerLevel serverLevel) {
        UUID targetUUID = CASTER_TO_TARGET.remove(casterUUID);
        if (targetUUID != null) TARGET_TO_CASTER.remove(targetUUID);
        PUNISHMENT_EXPIRY.remove(casterUUID);

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer != null) casterPlayer.removeEffect(MobEffects.GLOWING);
    }

    private static void triggerPunishment(UUID casterUUID, LivingEntity target, ServerLevel serverLevel, String reason) {
        Long expiry = PUNISHMENT_EXPIRY.get(casterUUID);
        if (expiry == null || serverLevel.getGameTime() > expiry) {
            cancelPunishment(casterUUID, serverLevel);
            return;
        }

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer != null) {
            applyRandomBuff(casterPlayer);
            casterPlayer.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.triggered_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable(reason).withStyle(ChatFormatting.WHITE)));
        }

        applyRandomDebuff(target);
        spawnTriggerEffect(serverLevel, casterPlayer, target);
    }

    private static void applyRandomBuff(LivingEntity entity) {
        entity.addEffect(switch (RAND.nextInt(5)) {
            case 0  -> new MobEffectInstance(MobEffects.DAMAGE_BOOST,      20 * 10, 1);
            case 1  -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED,    20 * 10, 1);
            case 2  -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1);
            case 3  -> new MobEffectInstance(MobEffects.REGENERATION,      20 * 10, 1);
            default -> new MobEffectInstance(MobEffects.ABSORPTION,        20 * 10, 1);
        });
        BeyonderData.addModifierWithTimeLimit(entity, "punishment_buff", 1.2, 20 * 5);
    }

    private static void applyRandomDebuff(LivingEntity entity) {
        entity.addEffect(switch (RAND.nextInt(5)) {
            case 0  -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 1);
            case 1  -> new MobEffectInstance(MobEffects.WEAKNESS,          20 * 10, 1);
            case 2  -> new MobEffectInstance(MobEffects.BLINDNESS,         20 *  5, 0);
            case 3  -> new MobEffectInstance(MobEffects.POISON,            20 *  5, 1);
            default -> new MobEffectInstance(MobEffects.WITHER,            20 *  5, 0);
        });
        BeyonderData.addModifierWithTimeLimit(entity, "punishment_debuff", 0.7, 20 * 5);
    }

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) event.getEntity().level();

        LivingEntity hurt     = event.getEntity();
        Entity       attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        UUID markedTarget = CASTER_TO_TARGET.get(hurt.getUUID());
        if (markedTarget == null || !markedTarget.equals(livingAttacker.getUUID())) return;

        float amount = event.getNewDamage();
        event.setNewDamage(0f);
        livingAttacker.hurt(serverLevel.damageSources().magic(), amount);

        triggerPunishment(hurt.getUUID(), livingAttacker, serverLevel, "ability.lotmcraft.punishment.reason_attacking");
    }

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getLevel().isClientSide) return;
        ServerLevel serverLevel = event.getLevel();

        LivingEntity user      = event.getEntity();
        if(user == null) return;
        UUID casterUUID = TARGET_TO_CASTER.get(user.getUUID());
        if (casterUUID == null) return;

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer == null) return;

        if (event.getInteractionRadius() >= 30 && casterPlayer.distanceTo(user) <= 60) {
            triggerPunishment(casterUUID, user, serverLevel, "ability.lotmcraft.punishment.reason_large_area");
            return;
        }

        List<String> flags = event.getInteractionFlags();
        if ((flags.contains("imprison") || flags.contains("confinement"))
                && casterPlayer.distanceTo(user) <= event.getInteractionRadius()) {
            triggerPunishment(casterUUID, user, serverLevel, "ability.lotmcraft.punishment.reason_restriction");
        }
    }

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if(!(event.getSource().getEntity() instanceof LivingEntity)) return;

        UUID deadUUID = event.getEntity().getUUID();

        if(!TARGET_TO_CASTER.containsKey(deadUUID) || !TARGET_TO_CASTER.containsKey(event.getSource().getEntity().getUUID())) return;
        UUID casterOfKiller = TARGET_TO_CASTER.get(event.getSource().getEntity().getUUID());
        if (casterOfKiller != null) {
            triggerPunishment(casterOfKiller, (LivingEntity) event.getSource().getEntity(), serverLevel,
                    "ability.lotmcraft.punishment.reason_kill");
        }

        UUID casterWhoMarkedDead = TARGET_TO_CASTER.get(deadUUID);
        if (casterWhoMarkedDead != null) {
            ServerPlayer caster = serverLevel.getServer().getPlayerList().getPlayer(casterWhoMarkedDead);
            if (caster != null) {
                spawnDismissEffect(serverLevel, event.getEntity().position());
                caster.playSound(SoundEvents.BEACON_DEACTIVATE, 1.0f, 0.7f);
            }
            cancelPunishment(casterWhoMarkedDead, serverLevel);
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getEntity() instanceof LivingEntity livingPlacer)) return;

        if (event.getPlacedBlock().getBlock() != Blocks.FIRE
                && event.getPlacedBlock().getBlock() != Blocks.SOUL_FIRE) return;

        UUID casterUUID = TARGET_TO_CASTER.get(livingPlacer.getUUID());
        if (casterUUID == null) return;

        triggerPunishment(casterUUID, livingPlacer, serverLevel, "ability.lotmcraft.punishment.reason_arson");
    }
}